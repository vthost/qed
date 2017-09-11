package sparql.synthesis.data;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
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

		

//	TODO check if it is possible to bound the number of nestings
	
	public static String SPARQL_TEMPLATE_START = 
			"PREFIX lsqv: <http://lsq.aksw.org/vocab#> "
			+ "PREFIX sp: <http://spinrdf.org/sp#>  "
					
			+ "SELECT ?id ?text WHERE { "
			+ "?id a sp:Select. "
			+ "?id sp:text ?text ; lsqv:resultSize ?rs ; lsqv:runTimeMs ?rt ; lsqv:triplePatterns ?tp; ";

	public static String SPARQL_TEMPLATE_END = 
			" FILTER(?rs > 0 && ?rt < 100 && ?tp > 3). "
			//subquery start
			+ "{ SELECT ?id (SUM(?vcount) as ?vcountsum) WHERE { " 
			+ "{ SELECT ?id (0 as ?vcount) WHERE { ?id lsqv:mentionsSubject ?s. }} UNION "
			
			+ "{ SELECT ?id (COUNT(DISTINCT ?v) as ?vcount) WHERE { " 
			+ "{ SELECT ?id ?v WHERE { ?id lsqv:mentionsSubject ?v. FILTER (regex(?v,\"^[?]\"))}} UNION "
			+ "{ SELECT ?id ?v WHERE { ?id lsqv:mentionsPredicate ?v. FILTER (regex(?v,\"^[?]\"))}} UNION "
			+ "{ SELECT ?id ?v WHERE { ?id lsqv:mentionsObject ?v. FILTER (regex(?v,\"^[?]\"))}} "
			+ "} GROUP BY ?id } "
			
			+ "} GROUP BY ?id }"
			//subquery end
			+ "} ORDER BY ASC(?vcountsum) LIMIT 10 ";
	
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

//            ((QueryEngineHTTP)qexec).addParam("timeout", "10000") ;
            ((QueryEngineHTTP)qexec).addParam("default-graph-uri", "http://dbpedia.org") ;

            // execute
            ResultSet rs = qexec.execSelect();
            
            List<String> logQueryIds = new ArrayList<String>();
            List<String> logQueries = new ArrayList<String>();
            while(rs.hasNext()) {
            		QuerySolution s = rs.next();
            		logQueryIds.add(s.getResource("?id").toString());
            		logQueries.add(s.getLiteral("?text").getString());
            }
            System.out.println(logQueryIds);
            System.out.println(logQueries);

            FileWriter writer = new FileWriter(QUERY_FILE); 
            
            for (int i = 0; i < logQueries.size(); i++) {
            		writer.write(logQueryIds.get(i));
            		writer.write("\n");
            		writer.write(logQueries.get(i));
            		if(i < logQueries.size()-1) writer.write("\n");
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
