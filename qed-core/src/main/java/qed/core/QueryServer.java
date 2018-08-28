package qed.core;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

public class QueryServer {

	public static void main(String[] args) {
		
		String cq = 
				"PREFIX  :     <http://dbpedia.org/resource/>\n" + 
				"PREFIX  owl:  <http://www.w3.org/2002/07/owl#>\n" + 
				"PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n" + 
				"PREFIX  skos: <http://www.w3.org/2004/02/skos/core#>\n" + 
				"PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
				"PREFIX  dbpedia: <http://dbpedia.org/>\n" + 
				"PREFIX  dbpedia2: <http://dbpedia.org/property/>\n" + 
				"PREFIX  foaf: <http://xmlns.com/foaf/0.1/>\n" + 
				"PREFIX  dc:   <http://purl.org/dc/elements/1.1/>\n" + 
				"\n" + 
				"CONSTRUCT \n" + 
				"  { \n" + 
				"    ?musician skos:subject <http://dbpedia.org/resource/Category:German_musicians> .\n" + 
				"    ?musician foaf:name ?name .\n" + 
				"    ?musician rdfs:comment ?description_en .\n" + 
				"  }\n" + 
				"WHERE\n" + 

				  "{ { FILTER ( !( lang(?description_en) = \"en\" ) )"
				  + " ?musician  rdfs:comment  ?description_en   }" +
				  "  ?musician  skos:subject  <http://dbpedia.org/resource/Category:German_musicians> ; foaf:name     ?name "

//				"  { ?musician  skos:subject  <http://dbpedia.org/resource/Category:German_musicians> ;  foaf:name     ?name . "
//				+ "FILTER NOT EXISTS { ?musician  rdfs:comment  ?description_en . FILTER ( lang(?description_en) = \"en\" )}" 
				+"  }" + 
				"LIMIT   1000";
				
				
//				"PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX : <http://dbpedia.org/resource/> PREFIX dbpedia2: <http://dbpedia.org/property/> PREFIX dbpedia: <http://dbpedia.org/> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> SELECT ?name ?description_en  ?musician WHERE {      ?musician skos:subject <http://dbpedia.org/resource/Category:German_musicians> .      ?musician foaf:name ?name .      OPTIONAL {          ?musician rdfs:comment ?description_en .          FILTER (LANG(?description_en) = 'en') .      } }";
		
		
		
		QueryEngineHTTP qe = null;
		try {		
			
			qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://localhost:8080/sparql", cq);
            qe.addParam("timeout", "10000") ;

            Model m = qe.execConstruct();
             StmtIterator i = m.listStatements();
            while(i.hasNext()) {
            	System.out.println(i.next());
            } 


	}catch(Exception e) {}
	}

}
