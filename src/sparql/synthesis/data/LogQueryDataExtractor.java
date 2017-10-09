package sparql.synthesis.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
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
	
	private int defaultLimit = 10;
	
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

	private Element removeFilters(Element e) {
			
			if(e instanceof ElementFilter) {
			
				return null;
				
			} else if(e instanceof ElementGroup) {
				
				ElementGroup e1 = (ElementGroup) e;
				
				List<Element> fElements = removeFilters(e1.getElements());
				e1.getElements().clear();
				e1.getElements().addAll(fElements);
				
				return e1;
			
			} 
	//		else if(e instanceof ElementPathBlock) {
	//			
	//			List<ElementPathBlock> l = new ArrayList<ElementPathBlock>();		
	//			l.add((ElementPathBlock) e);
	//			return l;
	//			
	//		} 
			else if(e instanceof ElementOptional) {
				
				Element e1 = removeFilters(((ElementOptional) e).getOptionalElement());
				
				return e1 == null ? null : new ElementOptional(e1);
				
			} else if(e instanceof ElementUnion) {
				
				ElementUnion e1 = (ElementUnion) e;
				List<Element> fElements = removeFilters(e1.getElements());
				e1.getElements().clear();
				e1.getElements().addAll(fElements);
				
				return e1;
			}
			//		TODO do we need to cover other cases? not sure how regarding:
			//		ElementDataSet, ElementNamedGraph, ElementService, ElementSubQuery,

			
			return e;		
		}

	private List<Element> removeFilters(List<Element> l) {
		
		List<Element> l1 = new ArrayList<Element> ();
		for (Element e : l) {
			Element e1 = removeFilters(e);
			if(e1 != null) l1.add(e1);
		}
		 
		return l1;
	}

	private Query toConstructQuery(String qs, int limit) {

		Query q1 = QueryFactory.create(qs);
		Element p = q1.getQueryPattern();
		
		Query q = QueryFactory.make();
		q.setPrefixMapping(q1.getPrefixMapping());
		q.setBaseURI(q1.getBaseURI());
		q.setQueryPattern(p);                              
		q.setQueryConstructType();
		q.setLimit(limit > 0 ? limit : defaultLimit);
		
		List<String> l = new ArrayList<String>();
		for (ElementPathBlock b: extractBGPs(p)) {
			l.add(b.toString());
		}
		
//		TODO We currently only consider BGPs in the WHERE clause
//		What if subgraphs, data sets etc. occur in the SELECT clause?
		String s =  String.join(".", l);		
		q.setConstructTemplate(QueryFactory.createTemplate("{"+s+"}"));
		
		return q;		
	}
	
//	TODO cover also MINUS (and others?)
	private Query toConstructQueryWithoutFilters(String qs, int limit) {

		Query q1 = QueryFactory.create(qs);
		Element p = q1.getQueryPattern();
		
		Query q = QueryFactory.make();
		q.setPrefixMapping(q1.getPrefixMapping());
		q.setBaseURI(q1.getBaseURI());                          
		q.setQueryConstructType();
		q.setLimit(limit > 0 ? limit : defaultLimit);
		
		List<String> l = new ArrayList<String>();
		for (ElementPathBlock b: extractBGPs(p)) {
			l.add(b.toString());
		}
		
		String s =  String.join(".", l);		
		q.setConstructTemplate(QueryFactory.createTemplate("{"+s+"}"));
		
		q.setQueryPattern(removeFilters(p));   
		
		return q;		
	}
	
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

			String qid = lq[0];
			String q = lq[1];			
			//some queries are erroneous; missing whitespace
			q = q.replace("FROM <http://dbpedia.org>", " FROM <http://dbpedia.org> ");

			Query cq = toConstructQuery(q, datasetSizeMax); //System.out.println("------------------ ");System.out.println(cq);
			
			try ( QueryEngineHTTP qe = 
	        		(QueryEngineHTTP) QueryExecutionFactory.sparqlService(logEndpoint, cq) ) {

	            qe.addParam("timeout", "10000") ;

	            Model m = qe.execConstruct();
	            
	            if(!m.listStatements().hasNext()) {
	            	
	            	System.out.println("NO DATA "+ qid);
//	            	System.out.println(query);
	            	
	            	//delete other files
	            	(new File(Utils.getQueryFilePath(qid))).delete();

	            } else {
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
//            	(new File(Utils.getQueryFilePath(qid))).delete();
	        	continue;
	        } 
			
			
			InputStream in; Model m;
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

			QueryExecution qe = QueryExecutionFactory.create(query, m);
			ResultSet rs = qe.execSelect();
			 
			if(!rs.hasNext()) {
				
				System.out.println("NO RESULT "+ qid);
//	            	System.out.println(q);
            	
            	//TODO delete files
//	            (new File(Utils.getQueryFilePath(qid))).delete();
//				(new File(Utils.getQueryDataFilePath(qid))).delete();
            	continue;
            	
            } else {
            	Utils.writeQueryResultFile(qid, rs);
            }

			qe.close();
		}
		
	}
	
	
	public static void main(String[] args) {
		LogQueryDataExtractor de = new LogQueryDataExtractor();
		de.extractQueryDataAndResults("http://dbpedia.org/sparql", 0);
	}


}
