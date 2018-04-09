package qed;

import java.util.Properties;

public class QED {
	
	private String defaultLog = "http://dbpedia.org";
	
//	private String getEndpoint(String logUri) {
//		
//		switch(logUri) {
//		
//		case "http://dbpedia.org": 
//			return "http://localhost:8080/sparql";
//		case "http://linkedgeodata.org": 
////		TODO ?	
////		case "http://data.semanticweb.org": 
////			logEndpoint = http://www.scholarlydata.org/sparql/
////			break;
//		case "http://bm.rkbexplorer.com": 
//			return "http://bm.rkbexplorer.com/sparql/";
//		default: 
//			return "http://localhost:8080/sparql";
//		}
//	}
	
//	private void createDirectoryStructure(String[][] configs) {
//		
//		File f = new File(Utils.DATA_DIR);
//		if(f.isFile()) f.delete();
//
//		new File(Utils.DATA_DIR).mkdir();
//		
//		if(configs == null) return;
//		
//		for (String[] c : configs) {
//			new File(Utils.getDirPath(c)).mkdir();
//		}
//	}

//		logUri can be one of	
//		DBpedia: http://dbpedia.org
//		Linked Geo Data: http://linkedgeodata.org
//		Semantic Web Dog Food: http://data.semanticweb.org
//		British Museum: http://bm.rkbexplorer.com
	
//	TODO we could add also parameters querySizeMax...
	public void createDataSet(String logUri, String[][] configs, int queryNumMax, int querySizeMin, 
			int queryResultSizeMin, int datasetSizeMax) {
		
		logUri = logUri == "" || logUri == null ? defaultLog : logUri;
		
//		createDirectoryStructure(configs == null ? LogQueryExtractor.defaultConfig : configs);
		
		(new LogQueryExtractor()).extractQueries(logUri, configs, queryNumMax, querySizeMin, queryResultSizeMin);
		(new LogQueryDataExtractor()).extractQueryDataAndResults("http://localhost:8080/sparql", datasetSizeMax);
	}
	
	public void createDataSet(String logUri, String[] queryIds) {
		
		if(queryIds == null || queryIds.length == 0) return;
		
		logUri = logUri == "" || logUri == null ? defaultLog : logUri;

		(new LogQueryExtractor()).extractQueries(queryIds);
		(new LogQueryDataExtractor()).extractQueryDataAndResults("http://localhost:8080/sparql", queryIds.length);
	}
	
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		
		Properties prop = new Properties(System.getProperties());
		(new QED()).createDataSet(
				prop.getProperty("q"),
				null, 
				prop.getProperty("m") == null ? 0 :Integer.valueOf(prop.getProperty("m")),
				prop.getProperty("s") == null ? 0 :Integer.valueOf(prop.getProperty("s")),
				prop.getProperty("r") == null ? 0 :Integer.valueOf(prop.getProperty("r")), 0);

//				new Properties();
//		InputStream input = null;
//
//		try {
//
//			input = new FileInputStream("config.properties");
//
//			// load a properties file
//			prop.load(input);
//
//			(new QED()).createDataSet(
//					prop.getProperty("q"), null, 
//					Integer.valueOf(prop.getProperty("m")),
//					Integer.valueOf(prop.getProperty("s")),
//					Integer.valueOf(prop.getProperty("r")), 0);
//
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		} finally {
//			if (input != null) {
//				try {
//					input.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
		
		
//		
//		String[] ids = {"DBpedia-q710195"};//DBpedia-q482443","DBpedia-q330584"};
//		(new Application()).createDataSet(null, ids);
	}

}
