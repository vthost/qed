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
	
	public Query toConstructQuery(String qs, int limit) {

		Query q1 = QueryFactory.create(qs);
		Element p = q1.getQueryPattern();
		
		Query q = QueryFactory.make();
		q.setPrefixMapping(q1.getPrefixMapping());
		q.setBaseURI(q1.getBaseURI());
		q.setQueryPattern(q1.getQueryPattern());                              
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
//		TODO cover other cases
		
		return new ArrayList<ElementPathBlock>();		
	}
		
	private List<ElementPathBlock> processElementList(List<Element> l) {		
		
		List<ElementPathBlock> l2 = new ArrayList<ElementPathBlock>();		
		for (Element e : l) l2.addAll(processElement(e));

		return l2;
	}

	
	public void extractQueryDataAndResults(int dataLimit) {
		//clean data directory 
		for(File file: (new File(Config.DATA_DIR)).listFiles()) {
		    if (file.getName().endsWith(Config.QUERY_DATA_FILE_EXT) || 
		    		file.getName().endsWith(Config.QUERY_RESULT_FILE_EXT) ) {
		        file.delete();
		    }
		}
		
		List<String> logQueryIds = new ArrayList<String>();
		List<String> logQueries = new ArrayList<String>();
		try { 		
			for(File f: (new File(Config.DATA_DIR)).listFiles()) {
				
				String[] q = Utils.readQueryFile(f);
				logQueryIds.add(q[0]);
				logQueries.add(q[1]);
			}	
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < logQueries.size(); i++) {
			
			String q = logQueries.get(i);
			
			try ( QueryEngineHTTP qexec = 
					(QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", q) ) {

	            qexec.addParam("timeout", "10000") ;

	            ResultSet rs = qexec.execSelect();            
	            if(!rs.hasNext()) {
	            	System.out.println("NO RESULT "+ logQueryIds.get(i));
//	            	System.out.println(q);
	            	
	            	//delete query file
	            	(new File(Utils.getQueryFilePath(logQueryIds.get(i)))).delete();
	            	continue;
	            } else {
	            	Utils.writeQueryResultFile(logQueryIds.get(i), rs);
	            }

	        } catch (Exception e) { 

	        	System.out.println("EXCEPTION " + logQueryIds.get(i));
	        	System.out.println("------------------ Query failed START");
	        	System.out.println(logQueryIds.get(i));
	        	System.out.println(e);
	        	System.out.println("------------------ ");
	        	System.out.println(q);
	        	System.out.println("------------------ Query failed END");
	        	continue; // queryLoop;
	        } 
			
			//TODO add limit here in construct to get a small data set
			Query cq = toConstructQuery(q, dataLimit); 
			
			try ( QueryEngineHTTP qexec = 
	        		(QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", cq) ) {

	            qexec.addParam("timeout", "10000") ;

	            Model m = qexec.execConstruct();
	            
	            if(!m.listStatements().hasNext()) {
	            	System.out.println("NO DATA "+ logQueryIds.get(i));
//	            	System.out.println(query);
	            	
	            	//delete other files
	            	(new File(Utils.getQueryFilePath(logQueryIds.get(i)))).delete();
	            	(new File(Utils.getQueryResultFilePath(logQueryIds.get(i)))).delete();
	            } else {
	            	Utils.writeQueryDataFile(logQueryIds.get(i), m);
	            }

	        } catch (Exception e) { 

	        	System.out.println("EXCEPTION " + logQueryIds.get(i));
	        	System.out.println("------------------ Query failed START");
	        	System.out.println(logQueryIds.get(i++));
	        	System.out.println(e);
	        	System.out.println("------------------ ");
	        	System.out.println(q);
	        	System.out.println("------------------ Query failed END");
	        } 
			
		}
		
	}
	
	
	public static void main(String[] args) {
		LogQueryDataExtractor de = new LogQueryDataExtractor();
		de.extractQueryDataAndResults(0);
	}


}
