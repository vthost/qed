package qed;

import java.io.File;
import java.util.Properties;

public class QED {

	private String defaultLogEndpoint = "http://lsq.aksw.org/sparql?";
	private String defaultLog = "http://dbpedia.org";
	private String defaultDataEndpoint = "http://localhost:8080/sparql";
	
	
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

//		with LSQ logUri can be one of	
//		DBpedia: http://dbpedia.org
//		Linked Geo Data: http://linkedgeodata.org
//		Semantic Web Dog Food: http://data.semanticweb.org
//		British Museum: http://bm.rkbexplorer.com
	
//	TODO we could add also parameters querySizeMax...
	public void createDataSet(String logEndpoint, String logUri, String[][] configs, int queryNumMax, int querySizeMin, 
			int queryResultSizeMin, String dataEndpoint, int datasetSizeMax, String dataLoc) {
		
		if(dataLoc != "" && dataLoc != null)
			Utils.DATA_DIR = dataLoc+File.separator + "data" + File.separator;	
		
		logEndpoint = logEndpoint == "" || logEndpoint == null ? defaultLogEndpoint : logEndpoint;
		logUri = logUri == "" || logUri == null ? defaultLog : logUri;
		dataEndpoint = dataEndpoint == "" || dataEndpoint == null ? defaultDataEndpoint : dataEndpoint;
				
		(new LogQueryExtractor()).extractQueries(logEndpoint, logUri, configs, queryNumMax, querySizeMin, queryResultSizeMin);
		(new LogQueryDataExtractor()).extractQueryDataAndResults(dataEndpoint, datasetSizeMax);
	}
	
	public void createDataSet(String logEndpoint, String logUri, String[] queryIds, String dataEndpoint) {
		
		if(queryIds == null || queryIds.length == 0) return;
		
		logUri = logUri == "" || logUri == null ? defaultLog : logUri;

		(new LogQueryExtractor()).extractQueries(logEndpoint, logUri, queryIds);
		(new LogQueryDataExtractor()).extractQueryDataAndResults(dataEndpoint, queryIds.length);
	}
	
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		
		Properties prop = new Properties(System.getProperties());

		(new QED()).createDataSet(
				prop.getProperty("q"),prop.getProperty("u"),
				null, 
				prop.getProperty("m") == null ? 0 :Integer.valueOf(prop.getProperty("m")),
				prop.getProperty("s") == null ? 0 :Integer.valueOf(prop.getProperty("s")),
				prop.getProperty("r") == null ? 0 :Integer.valueOf(prop.getProperty("r")), 
				prop.getProperty("d"),0,prop.getProperty("l"));

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
