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
	
	public static Feature[] exfs = {
			Feature.FILTER,Feature.MINUS,Feature.OPTIONAL,Feature.OFFSET,
			Feature.UNION, Feature.ORDER_BY};
	
	@SuppressWarnings("unused")
	private static Feature[][] createSimpleConfigs() {
		
		return Arrays.asList(Feature.values()).stream().map(f -> {Feature [] f1 = {f}; return f1;}).
				toArray(Feature[][]::new);
	}

	public static Feature[][] createBinaryConfigs(Feature[] fs) {
		List<Feature[]> cs = new ArrayList<Feature[]>();
		for (int i = 0; i < fs.length; i++) {
			for (int j = i+1; j < fs.length; j++) {
				Feature[] c = {fs[i],fs[j]};
				cs.add(c);
			}
		}
		return cs.stream().toArray(Feature[][]::new);
	}
	
	public static String getNewDirectory(String basepath) {
		int i = 0;
		String path = basepath + File.separator;
		while(new File(path).exists()) {
			path = basepath + i++ + File.separator;
		}
		Utils.makeDir(path);
		return path;
	}

	public void extractLSQDatasets(String path, LSQDataset dataset, Feature[][] cs) {
		 
		path = getNewDirectory(path + dataset.toString().toLowerCase());
		
		new LSQQueryExtractor().extractQueries(dataset.getGraphUri(), cs, 15, 0, 0, path);

		new DataExtractor().extractAllQueryDataAndResults(dataset.getEndpoint(), 3, path);
		
		Utils.writeManifestFiles(path);
	}
	
	public String extractLogFileBasedDatasets(String path, LogFileDataset dataset) {
		 
		return extractLogFileBasedDatasets(path, dataset.getName(), dataset);
	}
	
	public String extractLogFileBasedDatasets(String path, String name, LogFileDataset dataset) {
		 
		path = getNewDirectory(path + name);
		
		new LogFileQueryExtractor().extractQueries(dataset.getLocation(),path,dataset.getIdStr());

		new DataExtractor().extractQueryDataAndResults(dataset.getEndpoint(), 2, path);
		
		Utils.writeManifestFiles(path);
		
		return path;
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
			queryLog(LSQQueryExtractor.LSQR_SPARQL_EP, null, query);
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

	public static void main(String[] args) {
		
		Demo d = new Demo();
		
		LogFileDataset wd = new LogFileDataset("wikidata", Constants.DATA_DIR+"wikidata.txt", "https://query.wikidata.org/bigdata/namespace/wdq/sparql", "Wikidata-");
		d.extractLogFileBasedDatasets(Constants.DATA_DIR, wd);
		

		
//		LSQDataset dbp = new LSQDataset("dbpedia", "http://localhost:8080/sparql", "http://dbpedia.org");
//		new DataExtractor3().extractQueryDataAndResults(dbp.getEndpoint(), 2, Constants.DATA_DIR+"dbpedia2-new");
//		d.extractLSQDatasets(Constants.DATA_DIR, dbp, createBinaryConfigs(exfs));
	}
}
