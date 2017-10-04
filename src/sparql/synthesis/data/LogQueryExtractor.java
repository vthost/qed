package sparql.synthesis.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

public class LogQueryExtractor {
	
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

//	one configuration specifies the SPARQL queries we look for
//	(every query needs to contain all features from one array)
	public static String[][] FEATURE_CONFIG_SIMPLE = {
		{FEATURE_OPTIONAL},
//		{FEATURE_UNION}, 
//		{FEATURE_FILTER},
//		{FEATURE_REGEX}
	};
	
	private String defaultLog = "http://dbpedia.org";
	private int defaultNum = 20;
	private String[][] defaultConfig = FEATURE_CONFIG_SIMPLE;	


//	TODO check if it is possible to bound the number of nestings
	
	private String SPARQL_TEMPLATE_START = 
			"PREFIX lsqv: <http://lsq.aksw.org/vocab#> "
			+ "PREFIX sp: <http://spinrdf.org/sp#>  "
					
			+ "SELECT ?id ?text WHERE { "
			+ "?id a sp:Select. "
			+ "?id sp:text ?text ; lsqv:resultSize ?rs ; lsqv:runTimeMs ?rt ; lsqv:triplePatterns ?tp; ";

	private String SPARQL_TEMPLATE_END = 
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
			+ "} ORDER BY ASC(?vcountsum) LIMIT ";
	
	private String SPARQL_TEMPLATE_FEATURE = "lsqv:usesFeature lsqv:";
	
//TODO	num is per config
//	we might change this by using a big union
	public void extractQueries(String logUri, int num, String[][] configs) {
		
		//clean data directory
		for(File file: (new File(Config.DATA_DIR)).listFiles()) file.delete();
		

		for(String[] config: configs == null ? defaultConfig : configs) {
			String query = 
					SPARQL_TEMPLATE_START 
					+ SPARQL_TEMPLATE_FEATURE
					+ String.join("; "+SPARQL_TEMPLATE_FEATURE, config)
					+ ". "
					+ SPARQL_TEMPLATE_END + (num > 0 ? num : defaultNum);
			
	        List<String> logQueryIds = new ArrayList<String>();
	        List<String> logQueries = new ArrayList<String>();
	        
			try ( QueryEngineHTTP qexec =  
					(QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://lsq.aksw.org/sparql", query)  ) {

	        	qexec.addParam("default-graph-uri", logUri == null ? defaultLog : logUri) ;
	        	
	            ResultSet rs = qexec.execSelect();
	            
	            while(rs.hasNext()) {
	            		QuerySolution s = rs.next();
	            		logQueryIds.add(s.getResource("?id").toString());
	            		logQueries.add(s.getLiteral("?text").getString());
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        } 
			
	        for (int i = 0; i < logQueries.size(); i++) {
	      	  Utils.writeQueryFile(logQueryIds.get(i), logQueries.get(i));
	        }
		}

	}

	
	public static void main(String[] args) {
		LogQueryExtractor qe = new LogQueryExtractor();
		qe.extractQueries(null, 20, null);
	}

}
