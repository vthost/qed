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
	
	public String toConstructQuery(String qs) {
		

		

		Query q1 = QueryFactory.create(qs);
		Element p = q1.getQueryPattern();
		
		Query q = QueryFactory.make();
		q.setPrefixMapping(q1.getPrefixMapping());
		q.setBaseURI(q1.getBaseURI());
		q.setQueryPattern(p);                              
		q.setQueryConstructType();
		q.setConstructTemplate(QueryFactory.createTemplate("{}"));
		
//		constructing it in this way seems to work neither
//		String q2s = "CONSTRUCT {  } WHERE{ }";
//		Query q2 = QueryFactory.create(q2s, Syntax.syntaxARQ);

		
		if(p instanceof ElementGroup) {
			
			processElementGroup((ElementGroup) p, q);
		}

		
		return null;
		
	}
	
	
	private void processElementGroup(ElementGroup g, Query q) {
		
		for (Element e : g.getElements()) {
			
			if(e instanceof ElementGroup)  processElementGroup((ElementGroup) e, q);
			
			else if(e instanceof ElementPathBlock) processElementPathBlock((ElementPathBlock) e, q);
		}
	}

	private void processElementPathBlock(ElementPathBlock pb, Query q) {
		for(TriplePath tp: pb.getPattern().getList()) {

			Quad quad = new Quad(null, tp.asTriple());
//			TODO the quads list is unmodifiable...
			q.getConstructTemplate().getQuads().add(quad);
		}

		System.out.println("test");
		System.out.println(q);
		
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
		t.toConstructQuery(logQueries.get(0));
		
		
//		TODO we then can query dbpedia in the same way we query the endpoint in the other class using ...
//		QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query) 
		
	}

}
