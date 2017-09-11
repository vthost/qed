package sparql.synthesis.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementUnion;

public class LogQueryTranslator {
	
	public Query toConstructQuery(String qs) {

		Query q1 = QueryFactory.create(qs);
		Element p = q1.getQueryPattern();
		
		Query q = QueryFactory.make();
		q.setPrefixMapping(q1.getPrefixMapping());
		q.setBaseURI(q1.getBaseURI());
		q.setQueryPattern(q1.getQueryPattern());                              
		q.setQueryConstructType();
		
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

	


	public static void main(String[] args) {
		
		List<String> logQueryIds = new ArrayList<String>();
		List<String> logQueries = new ArrayList<String>();
		try { 
			Scanner s = new Scanner(new File(LogQueryExtractor.QUERY_FILE));
					
			while (s.hasNextLine()){
				logQueryIds.add(s.nextLine());
				logQueries.add(s.nextLine());
			}
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
//        System.out.println(logQueryIds);
//        System.out.println(logQueries);
		LogQueryTranslator t = new LogQueryTranslator();
		
		List<Query> qs = new ArrayList<Query>();
		for (String lq : logQueries) {
			Query q = t.toConstructQuery(lq); qs.add(q);
//			qs.add(t.toConstructQuery(lq));
			System.out.println(q);
		}
		
//		System.out.println(qs);
		
		for (Query query : qs) {
			try ( QueryExecution qexec = 
	        		QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query) ) {

	            ((QueryEngineHTTP)qexec).addParam("timeout", "10000") ;

	            // execute
	            Model m = qexec.execConstruct();
	            m.write(System.out, "TURTLE");
	            System.out.println();

	        } catch (Exception e) {
	            e.printStackTrace();
	        } 
		}
		
		
		
	}

}
