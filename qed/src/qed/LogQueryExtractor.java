package qed;

import java.io.File;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

public class LogQueryExtractor {
	
//	LSQ features we can query for 
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
	{ FEATURE_AVG },
	{ FEATURE_BIND },
	{ FEATURE_COUNT },
	{ FEATURE_DISTINCT },
	{ FEATURE_FILTER},
	{ FEATURE_FROM_NAMED },
	{ FEATURE_GROUP_BY },
	{ FEATURE_HAVING },
	{ FEATURE_LIMIT },
	{ FEATURE_MAX },
	{ FEATURE_MIN },
	{ FEATURE_MINUS },
	{ FEATURE_NAMED_GRAPH },
	{ FEATURE_OFFSET },
	{ FEATURE_OPTIONAL },
	{ FEATURE_ORDER_BY },
	{ FEATURE_REGEX },
	{ FEATURE_SERVICE },
	{ FEATURE_SUBQUERY },
	{ FEATURE_SUM },
	{ FEATURE_UNION },
	{ FEATURE_VALUES }
	};
	public static String[][] FEATURE_CONFIG_EXAMPLE = {
	{ FEATURE_FILTER,FEATURE_ORDER_BY },
	{ FEATURE_FILTER,FEATURE_REGEX },
	{ FEATURE_UNION,FEATURE_DISTINCT },
	{ FEATURE_UNION,FEATURE_FILTER },
	{ FEATURE_UNION,FEATURE_ORDER_BY },
	{ FEATURE_UNION,FEATURE_REGEX },
	{ FEATURE_OPTIONAL,FEATURE_REGEX },
	{ FEATURE_OPTIONAL,FEATURE_LIMIT }
	};
	
	public static String[][] FEATURE_CONFIG_TEST_ONE = {
//			{ FEATURE_AVG },
//			{ FEATURE_BIND },
//			{ FEATURE_COUNT },
//			{ FEATURE_DISTINCT },
//			{ FEATURE_FILTER},
//			{ FEATURE_FROM_NAMED },
//			{ FEATURE_GROUP_BY },
//			{ FEATURE_HAVING },
//			{ FEATURE_LIMIT },
//			{ FEATURE_MAX },
//			{ FEATURE_MIN },
//			{ FEATURE_MINUS },
//			{ FEATURE_NAMED_GRAPH },
////			{ FEATURE_OFFSET },
//			{ FEATURE_OPTIONAL },
//			{ FEATURE_ORDER_BY },
//			{ FEATURE_REGEX },
//			{ FEATURE_SERVICE },
//			{ FEATURE_SUBQUERY },
//			{ FEATURE_SUM },
			{ FEATURE_UNION },
//			{ FEATURE_VALUES }
			};
	
	public static String[][] defaultConfig = FEATURE_CONFIG_SIMPLE;
	private int defaultQueryNumMax = 10;
	private int defaultQuerySizeMin = 3;
	private int defaultQueryResultSizeMin = 1;

	
	private void queryLogAndWriteFiles(String query, String logUri, File directory) {
		
		try ( QueryEngineHTTP qexec =  
				(QueryEngineHTTP) QueryExecutionFactory.sparqlService(
						"http://lsq.aksw.org/sparql?", query)  ) {
			
			qexec.addParam("timeout", "10000") ;
			if(logUri != null && !logUri.equals(""))
				qexec.addParam("default-graph-uri", logUri) ;
       	
            ResultSet rs = qexec.execSelect();
            
            while(rs.hasNext()) {
            		QuerySolution s = rs.next();  //System.out.println(s.getResource("?id").toString());		
            		Utils.writeQueryFile(directory, s.getResource("?id").toString(), s.getLiteral("?text").getString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } 

	}
	
//  queryNumMax is per config
//	we might change this by using a big union
	public void extractQueries(String logUri, String[][] configs, int queryNumMax, int querySizeMin, int queryResultSizeMin) {
		
		Utils.cleanDataDir();
		
		for(String[] config: configs == null ? defaultConfig : configs) {
			
			String filter = 
					" FILTER(?rt < 100"
					+ " && ?rs >= " + (queryResultSizeMin > 0 ? queryResultSizeMin : defaultQueryResultSizeMin) 
					+ " && ?tp >= " + (querySizeMin > 0 ? querySizeMin : defaultQuerySizeMin) + ") ";
			
			String query =  "PREFIX lsqv: <http://lsq.aksw.org/vocab#> "
			+ "PREFIX sp: <http://spinrdf.org/sp#>  "
									
			+ "SELECT ?id ?text WHERE { "
//			TODO consider also forms other than select!
			+ "?id a sp:Select; "
			+ "sp:text ?text ; lsqv:resultSize ?rs ; lsqv:runTimeMs ?rt ; lsqv:triplePatterns ?tp; "		
			+ "lsqv:usesFeature lsqv:" + String.join("; lsqv:usesFeature lsqv:", config) + ". "
			+ filter + " . "

			//this is a shortcut to sort out "bot" queries that only differ in some values in the queries
//			TODO this could be enhanced..
			//subquery start
			+ "{ SELECT (SAMPLE(?text0) AS ?text) WHERE { "
			+ "?y sp:text ?text0 . BIND (STRBEFORE(?text0,\"WHERE\") AS ?textstart)"			
			+ "} GROUP BY ?textstart } ."
			//subquery end
			
//			we currently select queries with few variables to make the data set generation faster
			//subquery start
			+ "{ SELECT ?id (SUM(?vcount) as ?vcountsum) WHERE { " 
			+ "{ SELECT ?id (0 as ?vcount) WHERE { ?id lsqv:mentionsSubject ?s. }} UNION "
			
			+ "{ SELECT ?id (COUNT(DISTINCT ?v) as ?vcount) WHERE { " 
			+ "{ SELECT ?id ?v WHERE { ?id lsqv:mentionsSubject ?v. FILTER (regex(?v,\"^[?]\"))}} UNION "
			+ "{ SELECT ?id ?v WHERE { ?id lsqv:mentionsPredicate ?v. FILTER (regex(?v,\"^[?]\"))}} UNION "
			+ "{ SELECT ?id ?v WHERE { ?id lsqv:mentionsObject ?v. FILTER (regex(?v,\"^[?]\"))}} "
			+ "} GROUP BY ?id } "
			
			+ "} GROUP BY ?id } "
			//subquery end

			
			+ "} ORDER BY ASC(?vcountsum) LIMIT " + (queryNumMax > 0 ? queryNumMax : defaultQueryNumMax); 

			queryLogAndWriteFiles(query, logUri, Utils.cleanDataSubDir(config));
			
		}

	}

	public void extractQueries(String[] queryIds) {
		
		if(queryIds == null || queryIds.length == 0) return;
		
		Utils.cleanDataDir();
		
		String query =  "PREFIX  sp:   <http://spinrdf.org/sp#> "
				+ "PREFIX  lsqv: <http://lsq.aksw.org/vocab#>  "
				+ "SELECT  ?id ?text WHERE { "
				+ "?id sp:text ?text . "
				+ "FILTER ( (?id=<" + Utils.LSQR_RESOURCE_URI
				+ String.join(">) || (?id=<" + Utils.LSQR_RESOURCE_URI, queryIds) + ">) ) }";

		String[] dummyConfig = {"data"};
		
		queryLogAndWriteFiles(query, null, Utils.cleanDataSubDir(dummyConfig));
	}

	public static void main(String[] args) {
		LogQueryExtractor qe = new LogQueryExtractor();
		qe.extractQueries("http://dbpedia.org", FEATURE_CONFIG_EXAMPLE, 0, 0, 0);
		
//		String[] ids = {"DBpedia-q482443","DBpedia-q330584"};
//		qe.extractQueries(ids);
	}

}
