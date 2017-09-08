package sparql.synthesis.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;

public class LogQueryTranslator {
	
	public Query toConstructQuery(String qs) {

		Query q1 = QueryFactory.create(qs);
		Element p = q1.getQueryPattern();
		
		Query q = QueryFactory.make();
		q.setPrefixMapping(q1.getPrefixMapping());
		q.setBaseURI(q1.getBaseURI());
		q.setQueryPattern(q1.getQueryPattern());                              
		q.setQueryConstructType();
		
		if(p instanceof ElementGroup) {
			
			String s = processElementGroup((ElementGroup) p, q1);
			
			q.setConstructTemplate(QueryFactory.createTemplate("{"+s+"}"));

		}
		
		return q;		
	}
	
	
	private String processElementGroup(ElementGroup g, Query q) {
		
		for (Element e : g.getElements()) {
			
			if(e instanceof ElementGroup)  return processElementGroup((ElementGroup) e, q);
			
			else if(e instanceof ElementPathBlock) return e.toString();
		}
		
		return null;
	}

	


	public static void main(String[] args) {
		
		
		List<String> logQueries = new ArrayList<String>();
		try {
			Scanner s = new Scanner(new File(LogQueryExtractor.QUERY_FILE));
			
			
			while (s.hasNextLine()){
				logQueries.add(s.nextLine());
			}
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		LogQueryTranslator t = new LogQueryTranslator();
		Query q = t.toConstructQuery(logQueries.get(0));
		System.out.println(q);
		
//		TODO we then can query dbpedia in the same way we query the endpoint in the other class using ...
//		QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query) 
		
	}

}
