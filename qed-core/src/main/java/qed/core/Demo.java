package qed.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

public class Demo {
	
	public static Feature[] exfs = {Feature.FILTER,Feature.OPTIONAL,//Feature.REGEX, 
			Feature.UNION, Feature.SUBQUERY};
	
	private static Feature[][] createSimpleConfigs(Feature[] fs) {
		
		return Arrays.asList(fs).stream().map(f -> {Feature [] f1 = {f}; return f1;}).
				toArray(Feature[][]::new);
	}
	
	private static Feature[][] createBinaryConfigs(Feature[] fs) {
		List<Feature[]> cs = new ArrayList<Feature[]>();
		for (int i = 0; i < fs.length; i++) {
			for (int j = i+1; j < fs.length; j++) {
				Feature[] c = {fs[i],fs[j]};
				cs.add(c);
			}
		}
		return cs.stream().toArray(Feature[][]::new);
	}

	public void extractQueries(Dataset dataset, Feature[][] cs) {
		
		Utils.DATA_DIR = Utils.DATA_DIR + dataset.toString().toLowerCase() + File.separator;
		
		new LogQueryExtractor().extractQueries(Constants.LSQR_SPARQL_EP, dataset.graphUri, cs, 15, 0, 0);
	}
	
	public void extractData(Dataset dataset) {
		
		LogQueryDataExtractor de = new LogQueryDataExtractor();
		
		if(dataset == Dataset.WIKIDATA) {
			de.extractQueryDataAndResults(dataset.endpoint, 3, Utils.DATA_DIR + dataset);
			Utils.statsSummary(Utils.DATA_DIR + dataset + File.separator, 7);
		} else {
			de.extractAllQueryDataAndResults(dataset.endpoint, 3, Utils.DATA_DIR + dataset);
			Utils.statsSummary2(Utils.DATA_DIR + dataset + File.separator);
		}
	}
	
	public void printQueriesInDir(String directory) {
		File d = new File(directory);
		List<String[]> lqs = new ArrayList<String[]>();		
		try { 		
			
			for(File f: d.listFiles((dir,name) -> name.endsWith(Utils.QUERY_FILE_EXT))) {
				lqs.add(Utils.readQueryFile(f));
			}	
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(lqs.size());	
		for (String[] lq: lqs) {
			System.out.println(lq[0]);
			System.out.println(lq[1]);
		}
	}
	
	public void queryLog(String logEndpoint, String graphUri, String query) {
		
		try ( QueryEngineHTTP qexec =  
				(QueryEngineHTTP) QueryExecutionFactory.sparqlService(
						logEndpoint, query)  ) {
			
			qexec.addParam("timeout", "10000") ;
			
			if(!Strings.isNullOrEmpty(graphUri))
				qexec.addParam("default-graph-uri", graphUri) ;
       	
            ResultSet rs = qexec.execSelect();
            
            while(rs.hasNext()) {
            		QuerySolution s = rs.next();
            		System.out.println(s);
//            		Utils.writeQueryFile(directory, s.getResource("?id").toString(), s.getLiteral("?text").getString());
//            		System.out.println(s.getResource("?id").toString());	
//            		System.out.println(s.getLiteral("?text").toString().substring(s.getLiteral("?text").toString().toLowerCase().indexOf("select")));	
            }

        } catch (Exception e) {
            e.printStackTrace();
        } 

	}

	
	public void checkQueryFeatureOccurrence(String dir) {

		List<String> qs = new ArrayList<String>();
		for(File d2: new File(dir).listFiles(
				(current, name) -> new File(current+File.separator+name).isDirectory())) {

			for(File f: d2.listFiles((d3,name) -> name.endsWith(Utils.QUERY_FILE_EXT))) {
				qs.add( f.getName().substring(0, f.getName().indexOf(".")) );

			}	
		}
		
		for (String q : qs) {
			String query = "PREFIX lsqv: <http://lsq.aksw.org/vocab#> SELECT ?f WHERE {  <http://lsq.aksw.org/res/"
					+ q + "> lsqv:usesFeature ?f . }";
			queryLog(Constants.LSQR_SPARQL_EP, null, query);
		}
		


	}

	public String removeCriticalFunctions(String s) {
		String[] fs = {"YEAR"};
		int i = s.indexOf(fs[0]);
		int j = s.indexOf(")", i);
//		System.out.println(s.re);
		return s;
	}

	public static void main(String[] args) {
//		for (Feature[] fs : new Demo().createBinaryConfigs(exfs)) {
//			System.out.println(fs[0].name()+fs[1]);
//		}

		Demo d = new Demo();
		
//		Feature[][] cs = createBinaryConfigs(exfs);
//		for (Feature[] fs : cs) {
////		System.out.println(fs[0].name()+fs[1]);
//	}
		
//		d.extractQueries(Dataset.DBPEDIA, createSimpleConfigs(exfs));
		d.extractData(Dataset.DBPEDIA);
//		d.extractData(Dataset.WIKIDATA);
		
//		Utils.statsSummary(Utils.DATA_DIR + Dataset.WIKIDATA + File.separator, 7);
//
		
//		String tmp = Utils.DATA_DIR+File.separator +"test" ;// "wikidata" ;//
//		LogQueryDataExtractor de = new LogQueryDataExtractor();
//		de.extractQueryDataAndResults(Dataset.WIKIDATA.endpoint, 3, tmp);
//		de.extractQueryDataAndResults("http://localhost:8080/sparql", 30);
	}
	
	
//	public void generateAllDatasets() {
//		
//		LogQueryExtractor qe = new LogQueryExtractor();
//		LogQueryDataExtractor de = new LogQueryDataExtractor();
//		String tmp = System.getProperty("user.dir")+ File.separator +".."+File.separator +"data"+File.separator + "data-" ;
//		
//		for (int i = 0; i < graphs.length; i++) {
//			String g = graphs[i];
//			for (int j = 0; j < configs.length; j++) {			
//				Utils.DATA_DIR = tmp + g.substring(7,g.length()-4) + "-" + confignames[j] + File.separator;
//				System.out.println(Utils.DATA_DIR);
//				qe.extractQueries(Constants.LSQR_SPARQL_EP, g, configs[j], 10, 0, 0);
////				if(i != 2)
////				de.extractQueryDataAndResults(eps[i], 3);
//			}
//		}
//	}
}
