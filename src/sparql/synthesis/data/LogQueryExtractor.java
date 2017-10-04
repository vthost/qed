package sparql.synthesis.data;

import java.io.File;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

public class LogQueryExtractor {
	
//	LSQ features we can query for 
//TODO	in a recent gitlab version, there should be a feature for property paths 
	public static String FEATURE_AVG ="Avg";
	public static String FEATURE_BIND ="Bind";
	public static String FEATURE_COUNT ="Count";
	public static String FEATURE_DISTINCT ="Distinct";
	public static String FEATURE_FILTER ="Filter";
	public static String FEATURE_FROM_NAMED ="FromNamed";
	public static String FEATURE_GROUP_BY ="GroupBy";
	public static String FEATURE_HAVING ="Having";
	public static String FEATURE_LIMIT ="Limit";
	public static String FEATURE_MAX ="Max";
	public static String FEATURE_MIN ="Min";
	public static String FEATURE_MINUS ="Minus";
	//<!-- typo in the current LSQ data ... should be NamedGraph, not namedGraph ... -->
	public static String FEATURE_NAMED_GRAPH ="NamedGraph";
	//rdfs:label="NamedGraph"><owl:sameAs rdf:resource="http://lsq.aksw.org/vocab#namedGraph" /></sd:Feature>
	public static String FEATURE_OFFSET ="Offset";
	public static String FEATURE_OPTIONAL ="Optional";
	public static String FEATURE_ORDER_BY ="OrderBy";
	public static String FEATURE_REGEX ="Regex";
	public static String FEATURE_SERVICE ="Service";
	public static String FEATURE_SUBQUERY ="SubQuery";
	public static String FEATURE_SUM ="Sum";
	public static String FEATURE_UNION ="Union";
	public static String FEATURE_VALUES ="Values";

//	one configuration specifies the SPARQL queries we look for
//	(we look for queries that contain all features from one array)
	public static String[][] FEATURE_CONFIG_SIMPLE = {
//	{ FEATURE_AVG },
//	{ FEATURE_BIND },
//	{ FEATURE_COUNT },
//	{ FEATURE_DISTINCT },
//	{ FEATURE_FILTER },
//	{ FEATURE_FROM_NAMED },
//	{ FEATURE_GROUP_BY },
//	{ FEATURE_HAVING },
//	{ FEATURE_LIMIT },
//	{ FEATURE_MAX },
//	{ FEATURE_MIN },
//	{ FEATURE_MINUS },
//	{ FEATURE_NAMED_GRAPH },
//	{ FEATURE_OFFSET },
	{ FEATURE_OPTIONAL },
//	{ FEATURE_ORDER_BY },
//	{ FEATURE_REGEX },
//	{ FEATURE_SERVICE },
//	{ FEATURE_SUBQUERY },
//	{ FEATURE_SUM },
//	{ FEATURE_UNION },
//	{ FEATURE_VALUES }
	};
	
	private String[][] defaultConfig = FEATURE_CONFIG_SIMPLE;
	private int defaultQueryNumMax = 20;
	private int defaultQuerySizeMin = 3;
	private int defaultQueryResultSizeMin = 1;
//	TODO check if it is possible to bound the number of nestings
	
	private String SPARQL_TEMPLATE_START = 
			"PREFIX lsqv: <http://lsq.aksw.org/vocab#> "
			+ "PREFIX sp: <http://spinrdf.org/sp#>  "
					
			+ "SELECT ?id ?text WHERE { "
			+ "?id a sp:Select. "
			+ "?id sp:text ?text ; lsqv:resultSize ?rs ; lsqv:runTimeMs ?rt ; lsqv:triplePatterns ?tp; ";

	private String SPARQL_TEMPLATE_END = 
			//subquery start
			"{ SELECT ?id (SUM(?vcount) as ?vcountsum) WHERE { " 
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
	
//  num is per config
//	we might change this by using a big union
	public void extractQueries(String logUri, String[][] configs, int queryNumMax, int querySizeMin, int queryResultSizeMin) {
		
		//clean data directory
		for(File file: (new File(Config.DATA_DIR)).listFiles()) file.delete();
		

		for(String[] config: configs == null ? defaultConfig : configs) {
			String filter = 
					" FILTER(?rs >= " + (queryResultSizeMin > 0 ? queryResultSizeMin : defaultQueryResultSizeMin) 
					+ " && ?rt < 100 && ?tp >= " + (querySizeMin > 0 ? querySizeMin : defaultQuerySizeMin) + "). ";
					
			String query = 
					SPARQL_TEMPLATE_START 
					+ SPARQL_TEMPLATE_FEATURE
					+ String.join("; "+SPARQL_TEMPLATE_FEATURE, config)
					+ ". "
					+ filter
					+ SPARQL_TEMPLATE_END + (queryNumMax > 0 ? queryNumMax : defaultQueryNumMax); //System.out.println(QueryFactory.create(query));

			try ( QueryEngineHTTP qexec =  
					(QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://lsq.aksw.org/sparql", query)  ) {

	        	qexec.addParam("default-graph-uri", logUri) ;
	        	
	            ResultSet rs = qexec.execSelect();
	            
	            while(rs.hasNext()) {
	            		QuerySolution s = rs.next();  //System.out.println(s.getResource("?id").toString());		
	            		Utils.writeQueryFile(s.getResource("?id").toString(), s.getLiteral("?text").getString());
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        } 

		}

	}

	
	public static void main(String[] args) {
		LogQueryExtractor qe = new LogQueryExtractor();
		qe.extractQueries("http://dbpedia.org", null, 0, 0, 0);
	}

}
