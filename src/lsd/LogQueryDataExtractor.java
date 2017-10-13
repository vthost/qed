package lsd;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.expr.E_NotExists;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.Element1;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementUnion;

public class LogQueryDataExtractor {
//	TODO method gather optionals returns map opt to set of opts
//	then make powersets of sets
//	one construct query per possible opt to opt set map
	
	private int defaultDataLimit = 10;
	
	private List<ElementPathBlock> extractBGPs(Element e) {	
			
			if(e instanceof ElementGroup) {
				
				return extractBGPs(((ElementGroup) e).getElements());
			
			} else if(e instanceof ElementPathBlock) {
				
				List<ElementPathBlock> l = new ArrayList<ElementPathBlock>();		
				l.add((ElementPathBlock) e);
				return l;
				
			} 
	//TODO what to do here? is that still used?
	//		else if(e instanceof ElementTriplesBlock) {
	//			
	//			List<ElementPathBlock> l = new ArrayList<ElementPathBlock>();		
	//			l.add((ElementTriplesBlock) e);
	//			return l;
	//			
	//		} 
			else if(e instanceof ElementOptional) {
				
				return extractBGPs(((ElementOptional) e).getOptionalElement());
				
			} else if(e instanceof ElementUnion) {
				
				return extractBGPs(((ElementUnion) e).getElements());
			}
//			TODO the below were not tested yet
			else if(e instanceof Element1) {
				
				return extractBGPs(((Element1) e).getElement());
				
			} else if(e instanceof ElementMinus) {
				
				return extractBGPs(((ElementMinus) e).getMinusElement());
			
			} else if(e instanceof ElementData) {
//				TODO implement
//				return extractBGPs(((ElementData) e).);
			}
//			cases currently ignored: ElementNamedGraph, ElementService, ElementSubQuery,
		
			return new ArrayList<ElementPathBlock>();		
	}

	private List<ElementPathBlock> extractBGPs(List<Element> l) {		
		
		List<ElementPathBlock> l2 = new ArrayList<ElementPathBlock>();		
		for (Element e : l) l2.addAll(extractBGPs(e));
	
		return l2;
	}

	//	http://jvalentino.blogspot.de/2007/02/shortcut-to-calculating-power-set-using.html
	private <T> List<List<T>> powerset(Collection<T> list) {
		  List<List<T>> ps = new ArrayList<List<T>>();
		  ps.add(new ArrayList<T>());   // add the empty set
		 
		  // for every item in the original list
		  for (T item : list) {
		    List<List<T>> newPs = new ArrayList<List<T>>();
		 
		    for (List<T> subset : ps) {
		      // copy all of the current powerset's subsets
		      newPs.add(subset);
		 
		      // plus the subsets appended with the current item
		      List<T> newSubset = new ArrayList<T>(subset);
		      newSubset.add(item);
		      newPs.add(newSubset);
		    }
		 
		    // powerset is now powerset of list.subList(0, list.indexOf(item)+1)
		    ps = newPs;
		  }
		  return ps;
	}

//	get all subsets of size size satisfying additional conditions of the powerset of 
//	a merged version of set where the elements are represented as integers
	private List<List<Integer>> oneElementPowerset(List<List<Element>> set, int size) {
		
//		elements in first list in set are represented as 00 01 02 ...
//		elements in second list by 10 11 12 ...
//		etc.
		List<Integer> iset = new ArrayList<Integer>();
		for (int i = 0; i < set.size(); i++) {		
			for (int j = 0; j < set.get(i).size(); j++) {
				iset.add(10*i+j);
			}
		}
		
		List<List<Integer>> ps = powerset(iset);  
		
		outer: for (int i = ps.size()-1; i >= 0 ; i--) {
			if(ps.get(i).size() != size) {
				ps.remove(ps.get(i));
			} else {
				for (Integer i1 : ps.get(i)) {
					for (Integer i2 : ps.get(i)) {
						if((i1 / 10) == (i2 / 10) && i1 != i2) {
							ps.remove(ps.get(i));
							continue outer;
						}
					}
				}
			}
		}
		
	    return ps;
	}

//	TODO add also variability for union, and negation
	private List<Element> getInVariability(Element e, String qs) {	
		
		if(e instanceof ElementGroup) {

			List<Element> l1 = new ArrayList<Element>();
			
			List<List<Element>> l = getInVariability(((ElementGroup) e).getElements(),qs);
//			TODO opimize size one
			List<List<Integer>> l2 = oneElementPowerset(l,l.size());
//			TODO optimize
			for (List<Integer> indices : l2) {
				ElementGroup e1 = (ElementGroup) ElementUtils.findElement(e, QueryFactory.create(qs).getQueryPattern()); //(ElementGroup) DeepCopy.copy(e);
				e1.getElements().clear();
				
				for (Integer i : indices) {
					e1.getElements().add(l.get(i/10).get(i%10));
				}
				l1.add(e1);
			}

			return l1;
		
		} else if(e instanceof ElementOptional) {
			
			Element e1 = ((ElementOptional) e).getOptionalElement();
			Element e2 = ElementUtils.findElement(e1, QueryFactory.create(qs).getQueryPattern()); 
				
			List<Element> l1 = getInVariability(e1,qs);
			List<Element> l2 = getInVariability(e2,qs);

//					l1.addAll(l2.stream().
//							map(e3 -> new ElementFilter(new E_NotExists(e3))).collect(Collectors.toList()));
			
			for (Element e3 : l2) {
				l1.add(new ElementFilter(new E_NotExists(e3))); 
			}
			return l1;
			
		} else if(e instanceof ElementUnion) {
			
			List<Element> l1 = new ArrayList<Element>();
			
			List<List<Element>> l = getInVariability(((ElementUnion) e).getElements(),qs);
			List<List<Integer>> l2 = oneElementPowerset(l,l.size());
//			TODO optimize
			for (List<Integer> indices : l2) {
				ElementUnion e1 = (ElementUnion) ElementUtils.findElement(e, QueryFactory.create(qs).getQueryPattern()); //(ElementGroup) DeepCopy.copy(e);
				e1.getElements().clear();
				
				for (Integer i : indices) {
					e1.getElements().add(l.get(i/10).get(i%10));
				}
				l1.add(e1);
			}

			return l1;			
		}	
//		TODO else if(e instanceof Element1) { //is ElementDataset, ElementExists, ElementNotExists. 
//					
//					return extractOptionals(((Element1) e).getElement());
//					
//				} else if(e instanceof ElementMinus) {
//					
//					return extractOptionals(((ElementMinus) e).getMinusElement());
//				
//				} 
//		System.out.println(e.getClass());
//		cases currently ignored: ElementNamedGraph, ElementService, ElementSubQuery,
//		TODO we currently do not go into the inners of (filter etc.) expressions.
//		check if this is necessary
		
		List<Element> l = new ArrayList<Element>();
		l.add(e);		
		return l;		
	}

	private List<List<Element>> getInVariability(List<Element> elements, String queryString) {		
		
		List<List<Element>> els = new ArrayList<List<Element>>();		
		for (Element e : elements) 
			els.add(getInVariability(e, queryString));
	
		return els;
	}

	private List<Query> createConstructQueries(String queryString, int datasetSizeMax) {

		Query q = QueryFactory.create(queryString);
		Element p = q.getQueryPattern();

		List<Query> cqs = new ArrayList<Query>();
		
		for (Element e : getInVariability(p,queryString)) {
			
			Query cq = QueryFactory.make();
			cq.setQueryConstructType();
			cq.setBaseURI(q.getBaseURI());
			cq.setPrefixMapping(q.getPrefixMapping());	
//			the WHERE clause is the original one, 
//			but possibly adapted to make the dataset more variable
			cq.setQueryPattern(e);                              
			cq.setLimit(datasetSizeMax > 0 ? datasetSizeMax : defaultDataLimit);
			
//			create the construct part of the query
			List<String> l1 = new ArrayList<String>();
			for (ElementPathBlock b: extractBGPs(p)) { //TODO make method dependent on kind of SELECT clause
				l1.add(b.toString());
			}
			
			cq.setConstructTemplate(QueryFactory.createTemplate("{"+ String.join(".", l1) +"}"));
			
			cqs.add(cq);
		}
				
//		TODO We currently only consider BGPs in the WHERE clause
//		TODO What if subgraphs, data sets etc. occur in the SELECT clause?

		return cqs;		
	}
//	
////	TODO cover also MINUS (and others?)
//	private Query toConstructQueryWithoutFilters(String qs, int limit) {
//
//		Query q1 = QueryFactory.create(qs);
//		Element p = q1.getQueryPattern();
//		
//		Query q = QueryFactory.make();
//		q.setPrefixMapping(q1.getPrefixMapping());
//		q.setBaseURI(q1.getBaseURI());                          
//		q.setQueryConstructType();
//		q.setLimit(limit > 0 ? limit : defaultLimit);
//		
//		List<String> l = new ArrayList<String>();
//		for (ElementPathBlock b: extractBGPs(p)) {
//			l.add(b.toString());
//		}
//		
//		String s =  String.join(".", l);		
//		q.setConstructTemplate(QueryFactory.createTemplate("{"+s+"}"));
//		
//		q.setQueryPattern(removeFilters(p));   
//		
//		return q;		
//	}
	
	public void extractQueryDataAndResults(String logEndpoint, int datasetSizeMax) {
		
		//clean data directory 
		File dir = new File(Utils.DATA_DIR);
		for(File file: dir.listFiles()) {
		    if (file.getName().endsWith(Utils.QUERY_DATA_FILE_EXT) || 
		    		file.getName().endsWith(Utils.QUERY_RESULT_FILE_EXT) ) {
		        file.delete();
		    }
		}


		List<String[]> lqs = new ArrayList<String[]>();
		
		try { 		
			
			for(File f: dir.listFiles()) {
				lqs.add(Utils.readQueryFile(f));
			}	
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (String[] lq: lqs) {
			
			boolean hasData = false;

			String qid = lq[0];
			String q = lq[1];			
			//some queries are erroneous; missing whitespace
			q = q.replace("FROM <http://dbpedia.org>", " FROM <http://dbpedia.org> ");

			List<Query> qss = createConstructQueries(q, datasetSizeMax);
			
			for (Query cq : qss) {
//				Query cq = toConstructQuery(q, datasetSizeMax); //System.out.println("------------------ ");System.out.println(cq);
				System.out.println("------------------ ");System.out.println(cq);
//				
				QueryEngineHTTP qe = null;
				try {			 
					qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(logEndpoint, cq);
		            qe.addParam("timeout", "10000") ;

		            Model m = qe.execConstruct();
		            
		            if(!m.listStatements().hasNext()) {
		            	
		            	System.out.println("NO DATA "+ qid);
//		            	System.out.println(query);
		            	
		            	//delete other files
//		            	(new File(Utils.getQueryFilePath(qid))).delete();
		            	continue;
		            	
		            } else {
		            	hasData = true;
		            	Utils.writeQueryDataFile(qid, m);
		            }

		        } catch (Exception e) { 

		        	System.out.println("EXCEPTION " + qid);
		        	System.out.println("------------------ Query failed START");
		        	System.out.println(qid);
		        	System.out.println(e);
		        	System.out.println("------------------ ");
		        	System.out.println(q);
		        	System.out.println("------------------ Query failed END");
	            	//TODO delete - just leave it in currently to be able to look at the query
//	            	(new File(Utils.getQueryFilePath(qid))).delete();
		        	continue;
		        } finally {
		        	if(qe != null) qe.close();
		        }
			}
			
			if(!hasData) continue;
			InputStream in = null; Model m;
			try {
				
				in = new FileInputStream(new File(Utils.getQueryDataFilePath(qid)));
				 
				// Create an empty in-memory model and populate it from the data
				m = ModelFactory.createMemModelMaker().createModel(qid+"");
				m.read(in, null); // null base URI, since model URIs are absolute
				
				in.close();

			} catch (Exception e) {
				e.printStackTrace();
				continue;
			} 

			Query query = QueryFactory.create(q);
//			we cannot allow/test this given our restricted dataset size
			query.setOffset(0);

			QueryExecution qe1 = QueryExecutionFactory.create(query, m);
			ResultSet rs = qe1.execSelect();
			 
			if(!rs.hasNext()) {
				
				System.out.println("NO RESULT "+ qid);
//	            	System.out.println(q);
            	
            	//TODO delete files
//	            (new File(Utils.getQueryFilePath(qid))).delete();
//				(new File(Utils.getQueryDataFilePath(qid))).delete();
            	
            } else {
            	Utils.writeQueryResultFile(qid, rs);
            }

			qe1.close();
		}
		
	}
public static void main(String[] args) {
		LogQueryDataExtractor de = new LogQueryDataExtractor();
		de.extractQueryDataAndResults("http://dbpedia.org/sparql", 0);
	}


}
