package sparql.synthesis.data;

public class Application {
	
	private String defaultLog = "http://dbpedia.org";

//logUri must be one of	
//		DBpedia: http://dbpedia.org
//		Linked Geo Data: http://linkedgeodata.org
//		Semantic Web Dog Food: http://data.semanticweb.org
//		British Museum: http://bm.rkbexplorer.com
	
//	TODO we could add also parameters querySizeMax...
	public void createDataSet(String logUri, String[][] configs, int queryNumMax, int querySizeMin, 
			int queryResultSizeMin, int datasetSizeMax) {
		
		logUri = logUri == "" || logUri == null ? defaultLog : logUri;
		
		String logEndpoint = null;
		switch(logUri) {
		case "http://dbpedia.org": 
			logEndpoint = "http://dbpedia.org/sparql";
			break;
		case "http://linkedgeodata.org": 
			logEndpoint = "http://linkedgeodata.org/sparql";
			break;
//		TODO ?	
//		case "http://data.semanticweb.org": 
//			logEndpoint = http://www.scholarlydata.org/sparql/
//			break;
		case "http://bm.rkbexplorer.com": 
			logEndpoint = "http://bm.rkbexplorer.com/sparql/";
			break;
		default: 
			logEndpoint = "http://dbpedia.org/sparql";
			break;
		}

		(new LogQueryExtractor()).extractQueries(logUri, configs, queryNumMax, querySizeMin, queryResultSizeMin);
		(new LogQueryDataExtractor()).extractQueryDataAndResults(logEndpoint, datasetSizeMax);
	}
	
	public static void main(String[] args) {
		
		(new Application()).createDataSet(null, null, 0, 0, 0, 0);
	}

}
