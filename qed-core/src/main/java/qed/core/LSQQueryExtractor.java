package qed.core;

import java.io.File;

import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

public class LSQQueryExtractor implements QueryExtractor {
	
	
	public static String LSQR_RESOURCE_URI = "http://lsq.aksw.org/res/";
	public static String LSQR_SPARQL_EP = "http://lsq.aksw.org/sparql";

	private static Feature[][] defaultConfig = Feature.getValuesAsConfig();
	
	private int defaultQueryNumMax = 10;
	private int defaultQuerySizeMin = 3;
	private int defaultQueryResultSizeMin = 1;
	
	private void queryLogAndWriteFiles(String logEndpoint, String graphUri, String query, File directory) {
		
		try ( QueryEngineHTTP qexec =  
				(QueryEngineHTTP) QueryExecutionFactory.sparqlService(
						logEndpoint, query)  ) {
			
			qexec.addParam("timeout", "10000") ;
			
			if(!Strings.isNullOrEmpty(graphUri))
				qexec.addParam("default-graph-uri", graphUri) ;
       	
            ResultSet rs = qexec.execSelect();
            
            while(rs.hasNext()) {
            		QuerySolution s = rs.next();  	
            		Utils.writeQueryFile(directory, s.getResource("?id").toString(), s.getLiteral("?text").getString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } 

	}
	
//  queryNumMax is per config
	public void extractQueries(String graphUri, Feature[][] configs, 
			int queryNumMax, int querySizeMin, int queryResultSizeMin, String path) {
		
		String logEndpoint = LSQR_SPARQL_EP;
		Utils.makeDir(path);
		
		for(Feature[] config: configs == null ? defaultConfig : configs) {

			String filter = 
					" FILTER("//?rt < 100  && "
					+ " ?rs >= " + (queryResultSizeMin >= 0 ? queryResultSizeMin : defaultQueryResultSizeMin) 
					+ " && ?tp >= " + (querySizeMin > 0 ? querySizeMin : defaultQuerySizeMin) + ") ";
			
			String query =  "PREFIX lsqv: <http://lsq.aksw.org/vocab#> "
			+ "PREFIX sp: <http://spinrdf.org/sp#>  "
									
			+ "SELECT ?id ?text WHERE { "
//			TODO consider also forms other than select!
			+ "?id a sp:Select; "
			+ "sp:text ?text ; lsqv:resultSize ?rs ; lsqv:runTimeMs ?rt ; lsqv:triplePatterns ?tp; "		
			+ "lsqv:usesFeature lsqv:" + String.join("; lsqv:usesFeature lsqv:", Feature.toStringArray(config)) + ". "
			+ "FILTER NOT EXISTS {?id lsqv:parseError ?error .}. FILTER NOT EXISTS {?id lsqv:usesFeature lsqv:Regex .}."
			+ filter + " . "

			//this is a shortcut to sort out "bot" queries that only differ in some values in the queries
//			TODO this could be enhanced maybe..
			//subquery start
			+ "{ SELECT (SAMPLE(?text0) AS ?text) WHERE { "
			+ "?y sp:text ?text0 . BIND (STRBEFORE(?text0,\"WHERE\") AS ?textstart)"			
			+ "} GROUP BY ?textstart } ."
			//subquery end
		
//			TODO get rid of that!
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

			
			+ "} ORDER BY ASC(?tp) ASC(?vcountsum) LIMIT " + (queryNumMax > 0 ? queryNumMax : defaultQueryNumMax); 

			File f = Utils.makeSubDir(path,config);
			queryLogAndWriteFiles(logEndpoint, graphUri, query, f);
			//delete directory if no queries were retrieved
			if(f.list().length == 0) f.delete();
			
		}

	}

	public void extractQueries(String logEndpoint, String graphUri, String[] queryIds, String path) {
		
		if(Strings.isNullOrEmpty(logEndpoint) || queryIds == null || queryIds.length == 0) return;
		
		Utils.makeDir(path);
		
		String query =  "PREFIX  sp:   <http://spinrdf.org/sp#> "
				+ "PREFIX  lsqv: <http://lsq.aksw.org/vocab#>  "
				+ "SELECT  ?id ?text WHERE { "
				+ "?id sp:text ?text . "
				+ "FILTER ( (?id=<" + LSQR_RESOURCE_URI
				+ String.join(">) || (?id=<" + LSQR_RESOURCE_URI, queryIds) + ">) ) }";

		Feature[] dummyConfig = {};
		
		queryLogAndWriteFiles(logEndpoint, graphUri, query, Utils.makeSubDir(path,dummyConfig));
	}

}
