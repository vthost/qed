package sparql.synthesis.data;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

public class LogQueryExtractor {
	
	public static String QUERY_FILE = "data/logqueries.txt";

//LSQ features we can query for 
//	
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#Avg" rdfs:label="Avg" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#Bind" rdfs:label="Bind" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#Count" rdfs:label="Count" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#Distinct" rdfs:label="Distinct" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#Filter" rdfs:label="Filter" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#FromNamed" rdfs:label="FromNamed" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#GroupBy" rdfs:label="GroupBy" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#Having" rdfs:label="Having" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#Limit" rdfs:label="Limit" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#Max" rdfs:label="Max" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#Min" rdfs:label="Min" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#Minus" rdfs:label="Minus" />
//<!-- typo in the current data ... should be NamedGraph, not namedGraph ... -->
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#NamedGraph" rdfs:label="NamedGraph"><owl:sameAs rdf:resource="http://lsq.aksw.org/vocab#namedGraph" /></sd:Feature>
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#Offset" rdfs:label="Offset" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#Optional" rdfs:label="Optional" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#OrderBy" rdfs:label="OrderBy" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#Regex" rdfs:label="Regex" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#Service" rdfs:label="Service" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#SubQuery" rdfs:label="SubQuery" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#Sum" rdfs:label="Sum" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#Union" rdfs:label="Union" />
//<sd:Feature rdf:about="http://lsq.aksw.org/vocab#Values" rdfs:label="Values" />
	
//	TODO complete...
	public static String FEATURE_OPTIONAL = "Optional";
	public static String FEATURE_UNION = "Union";
	public static String FEATURE_FILTER = "Filter";
	public static String FEATURE_REGEX = "Regex";

//	one config specifies the SPAQL queries we look for
//	every query needs to contain all features from one array
	public static String[][] FEATURE_CONFIG_SIMPLE1 = {
		{FEATURE_OPTIONAL},{FEATURE_UNION}
	};
	public static String[][] FEATURE_CONFIG_SIMPLE2 = {
		{FEATURE_FILTER},{FEATURE_REGEX}
	};
	
	public static String[][] FEATURE_CONFIG_MIXED1 = {
		{FEATURE_OPTIONAL,FEATURE_UNION}, {FEATURE_FILTER,FEATURE_REGEX}
	};

		
//	TODO add condition to look only for SELECT queries (instance of sp:Select)
	
//	TODO check if it is possible to bound the number of nestings
	
//	TODO check if this works as intended
	public static String SPARQL_TEMPLATE_START = 
			"PREFIX lsqv: <http://lsq.aksw.org/vocab#> "
			+ "PREFIX sp: <http://spinrdf.org/sp#>  "
			+ "SELECT ?text WHERE { "
			+ "?id sp:text ?text ; lsqv:resultSize ?rs ; lsqv:runTimeMs ?rt ; lsqv:triplePatterns ?tp; ";

	public static String SPARQL_TEMPLATE_END = 
			" FILTER(?rs > 0 && ?rt < 100 && ?tp > 3). "
			+ "{"
			+ "SELECT ?id (SUM(?vcount) as ?vcountsum) WHERE { "
			+ "{ SELECT ?id (0 as ?vcount) WHERE { ?id lsqv:mentionsSubject ?s. }} UNION "
			+ "{ SELECT ?id (1 as ?vcount) WHERE { ?id lsqv:mentionsSubject ?s. FILTER (regex(?s,\"^[?]\"))}} UNION "
			+ "{ SELECT ?id (1 as ?vcount) WHERE { ?id lsqv:mentionsPredicate ?p. FILTER (regex(?p,\"^[?]\"))}} UNION "
			+ "{ SELECT ?id (1 as ?vcount) WHERE { ?id lsqv:mentionsObject ?o. FILTER (regex(?o,\"^[?]\"))}} "
			+ "}  "
			+ "GROUP BY ?id "
			+ "} "
			+ "} "
			+ "ORDER BY ASC(?vcountsum) "
			+ "LIMIT 5 ";
	
	public static String SPARQL_TEMPLATE_FEATURE = "lsqv:usesFeature lsqv:";

	
	public static void main(String[] args) {
//		TODO add check if graph uri arg is applied
		String[][] config = FEATURE_CONFIG_SIMPLE1;
		
		String query = 
				SPARQL_TEMPLATE_START 
				+ SPARQL_TEMPLATE_FEATURE
				+ String.join("; "+SPARQL_TEMPLATE_FEATURE, config[0])
				+ ". "
				+ SPARQL_TEMPLATE_END;
		
		System.out.println(query);
        // remote execution
        try ( QueryExecution qexec = 
        		QueryExecutionFactory.sparqlService("http://lsq.aksw.org/sparql", query) ) {

            ((QueryEngineHTTP)qexec).addParam("timeout", "10000") ;
            ((QueryEngineHTTP)qexec).addParam("default-graph-uri", "http://dbpedia.org") ;

            // execute
            ResultSet rs = qexec.execSelect();
            
            List<String> logQueries = new ArrayList<String>();
            while(rs.hasNext()) {
            	logQueries.add(rs.next().getLiteral("?text").getString());
            }
            System.out.println(logQueries);
//          ResultSetFormatter.out(rs);
            
            FileWriter writer = new FileWriter(QUERY_FILE); 
            int i = 0;
            for(String str: logQueries) {
              writer.write(str);
              if(++i < logQueries.size()) writer.write("\n");
            }
            writer.close();


        } catch (Exception e) {
            e.printStackTrace();
        } 
//        check if necessary?
//        finally {
//        	qexec.close();
//        }
        


	}

}
