package qed.core;

import java.io.File;

import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

public class LogQueryExtractor {

	private static String[][] defaultConfig = Feature.FEATURE_CONFIG_SIMPLE;
	
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
            		System.out.println(s.getResource("?id").toString());	
            		System.out.println(s.getLiteral("?text").toString().substring(s.getLiteral("?text").toString().toLowerCase().indexOf("select")));	
            }

        } catch (Exception e) {
            e.printStackTrace();
        } 

	}
	
//  queryNumMax is per config
//	we might change this by using a big union
	public void extractQueries(String logEndpoint, String graphUri, String[][] configs, 
			int queryNumMax, int querySizeMin, int queryResultSizeMin) {//, String dataDir
		
		if(Strings.isNullOrEmpty(logEndpoint)) return;
		
		Utils.cleanDataDir();
		
		for(String[] config: configs == null ? defaultConfig : configs) {
			
			String filter = 
					" FILTER(?rt < 100  && "
					+ " ?rs >= " + (queryResultSizeMin >= 0 ? queryResultSizeMin : defaultQueryResultSizeMin) 
					+ " && ?tp >= " + (querySizeMin > 0 ? querySizeMin : defaultQuerySizeMin) + ") ";
			
			String query =  "PREFIX lsqv: <http://lsq.aksw.org/vocab#> "
			+ "PREFIX sp: <http://spinrdf.org/sp#>  "
									
			+ "SELECT ?id ?text WHERE { "
//			TODO consider also forms other than select!
			+ "?id a sp:Select; "
			+ "sp:text ?text ; lsqv:resultSize ?rs ; lsqv:runTimeMs ?rt ; lsqv:triplePatterns ?tp; "		
			+ "lsqv:usesFeature lsqv:" + String.join("; lsqv:usesFeature lsqv:", config) + ". "
			+ "FILTER NOT EXISTS {?id lsqv:parseError ?error .}. "
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

			
			+ "} ORDER BY ASC(?vcountsum) LIMIT " + (queryNumMax > 0 ? queryNumMax : defaultQueryNumMax); 

			File f = Utils.cleanDataSubDir(config);
			queryLogAndWriteFiles(logEndpoint, graphUri, query, f);
			if(f.list().length == 0) f.delete();
			
		}

	}

	public void extractQueries(String logEndpoint, String graphUri, String[] queryIds) {
		
		if(Strings.isNullOrEmpty(logEndpoint) || queryIds == null || queryIds.length == 0) return;
		
		Utils.cleanDataDir();
		
		String query =  "PREFIX  sp:   <http://spinrdf.org/sp#> "
				+ "PREFIX  lsqv: <http://lsq.aksw.org/vocab#>  "
				+ "SELECT  ?id ?text WHERE { "
				+ "?id sp:text ?text . "
				+ "FILTER ( (?id=<" + Utils.LSQR_RESOURCE_URI
				+ String.join(">) || (?id=<" + Utils.LSQR_RESOURCE_URI, queryIds) + ">) ) }";

		String[] dummyConfig = {"data"};
		
		queryLogAndWriteFiles(logEndpoint, graphUri, query, Utils.cleanDataSubDir(dummyConfig));
	}

	public static void main(String[] args) {
		
//		LogQueryExtractor qe = new LogQueryExtractor();
//		qe.extractQueries("http://lsq.aksw.org/sparql", "http://dbpedia.org", Feature.FEATURE_CONFIG_THREE, 30, 0, 0);
		
//		String[] ids = {"DBpedia-q626806"//"DBpedia-q482443","DBpedia-q330584"
//				};
//		
//		qe.extractQueries("http://lsq.aksw.org/sparql", "http://dbpedia.org",ids);
	}

}
