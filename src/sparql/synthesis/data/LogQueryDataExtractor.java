package sparql.synthesis.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementUnion;

public class LogQueryDataExtractor {
	
	private int defaultLimit = 10;
	
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
		for (ElementPathBlock b: processElement(p)) {
			l.add(b.toString());
		}
		
		String s =  String.join(".", l);		
		q.setConstructTemplate(QueryFactory.createTemplate("{"+s+"}"));
		
		return q;		
	}
	
	private Query toConstructQueryWithoutFilters(String qs, int limit) {

		Query q1 = QueryFactory.create(qs);
		Element p = q1.getQueryPattern();
		
		Query q = QueryFactory.make();
		q.setPrefixMapping(q1.getPrefixMapping());
		q.setBaseURI(q1.getBaseURI());                          
		q.setQueryConstructType();
		q.setLimit(limit > 0 ? limit : defaultLimit);
		
		List<String> l = new ArrayList<String>();
		for (ElementPathBlock b: processElement(p)) {
			l.add(b.toString());
		}
		
		String s =  String.join(".", l);		
		q.setConstructTemplate(QueryFactory.createTemplate("{"+s+"}"));
		
//		TODO pattern remove filters and set!
		
		return q;		
	}
	
	private List<ElementPathBlock> processElement(Element e) {	
		
		if(e instanceof ElementGroup) {
			
			return processElementList(((ElementGroup) e).getElements());
		
		} else if(e instanceof ElementPathBlock) {
			
			List<ElementPathBlock> l = new ArrayList<ElementPathBlock>();		
			l.add((ElementPathBlock) e);
			return l;
			
		} else if(e instanceof ElementOptional) {
			
			return processElement(((ElementOptional) e).getOptionalElement());
			
		} else if(e instanceof ElementUnion) {
			
			return processElementList(((ElementUnion) e).getElements());
		}
//		TODO do we need to cover other cases? not sure...
		
		return new ArrayList<ElementPathBlock>();		
	}
		
	private List<ElementPathBlock> processElementList(List<Element> l) {		
		
		List<ElementPathBlock> l2 = new ArrayList<ElementPathBlock>();		
		for (Element e : l) l2.addAll(processElement(e));

		return l2;
	}
	
	private List<ElementPathBlock> processElementF(Element e) {	
		
		if(e instanceof ElementGroup) {
			
			return processElementListF(((ElementGroup) e).getElements());
		
		} else if(e instanceof ElementPathBlock) {
			
			List<ElementPathBlock> l = new ArrayList<ElementPathBlock>();		
			l.add((ElementPathBlock) e);
			return l;
			
		} else if(e instanceof ElementOptional) {
			
			return processElementF(((ElementOptional) e).getOptionalElement());
			
		} else if(e instanceof ElementUnion) {
			
			return processElementListF(((ElementUnion) e).getElements());
		}
//		TODO do we need to cover other cases? not sure...
		
		return new ArrayList<ElementPathBlock>();		
	}
		
	private List<ElementPathBlock> processElementListF(List<Element> l) {		
		
		List<ElementPathBlock> l2 = new ArrayList<ElementPathBlock>();		
		for (Element e : l) l2.addAll(processElement(e));

		return l2;
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
			
			try ( QueryEngineHTTP qexec = 
					(QueryEngineHTTP) QueryExecutionFactory.sparqlService(logEndpoint, q) ) {

	            qexec.addParam("timeout", "10000") ;

	            ResultSet rs = qexec.execSelect();            
	            if(!rs.hasNext()) {
	            	System.out.println("NO RESULT "+ qid);
//	            	System.out.println(q);
	            	
	            	//delete query file
//	            	(new File(Utils.getQueryFilePath(qid))).delete();
	            	continue;
	            } else {
	            	Utils.writeQueryResultFile(qid, rs);
	            }

	        } catch (Exception e) { 

	        	System.out.println("EXCEPTION " + qid);
	        	System.out.println("------------------ Query failed START");
	        	System.out.println(qid);
	        	System.out.println(e);e.printStackTrace();
	        	System.out.println("------------------ ");
	        	System.out.println(q);
	        	System.out.println("------------------ Query failed END");
            	//TODO delete?
//            	(new File(Utils.getQueryFilePath(qid))).delete();
	        	continue; // queryLoop;
	        } 
			
			Query cq = toConstructQuery(q, datasetSizeMax); 
			
			try ( QueryEngineHTTP qexec = 
	        		(QueryEngineHTTP) QueryExecutionFactory.sparqlService(logEndpoint, cq) ) {

	            qexec.addParam("timeout", "10000") ;

	            Model m = qexec.execConstruct();
	            
	            if(!m.listStatements().hasNext()) {
	            	System.out.println("NO DATA "+ qid);
//	            	System.out.println(query);
	            	
	            	//delete other files
//	            	(new File(Utils.getQueryFilePath(qid))).delete();
//	            	(new File(Utils.getQueryResultFilePath(qid))).delete();
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
            	//TODO delete?
//            	(new File(Utils.getQueryFilePath(qid))).delete();
	        } 
			
		}
		
	}
	
	
	public static void main(String[] args) {
		LogQueryDataExtractor de = new LogQueryDataExtractor();
		de.extractQueryDataAndResults("http://dbpedia.org/sparql", 0);
	}


}
