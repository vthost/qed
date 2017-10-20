package lsd;

public class Application {
	
	private String defaultLog = "http://dbpedia.org";
	
	private String getEndpoint(String logUri) {
		
		switch(logUri) {
		
		case "http://dbpedia.org": 
			return "http://dbpedia.org/sparql";
		case "http://linkedgeodata.org": 
//		TODO ?	
//		case "http://data.semanticweb.org": 
//			logEndpoint = http://www.scholarlydata.org/sparql/
//			break;
		case "http://bm.rkbexplorer.com": 
			return "http://bm.rkbexplorer.com/sparql/";
		default: 
			return "http://dbpedia.org/sparql";
		}
	}

//		logUri can be one of	
//		DBpedia: http://dbpedia.org
//		Linked Geo Data: http://linkedgeodata.org
//		Semantic Web Dog Food: http://data.semanticweb.org
//		British Museum: http://bm.rkbexplorer.com
	
//	TODO we could add also parameters querySizeMax...
	public void createDataSet(String logUri, String[][] configs, int queryNumMax, int querySizeMin, 
			int queryResultSizeMin, int datasetSizeMax) {
		
		logUri = logUri == "" || logUri == null ? defaultLog : logUri;
		
		(new LogQueryExtractor()).extractQueries(logUri, configs, queryNumMax, querySizeMin, queryResultSizeMin);
		(new LogQueryDataExtractor()).extractQueryDataAndResults(getEndpoint(logUri), datasetSizeMax);
	}
	
	public void createDataSet(String logUri, String[] queryIds) {
		
		if(queryIds == null || queryIds.length == 0) return;
		
		logUri = logUri == "" || logUri == null ? defaultLog : logUri;

		(new LogQueryExtractor()).extractQueries(queryIds);
		(new LogQueryDataExtractor()).extractQueryDataAndResults(getEndpoint(logUri), queryIds.length);
	}
	
	
	public static void main(String[] args) {
		
//		(new Application()).createDataSet(null, null, 0, 0, 0, 0);
//		
		String[] ids = {"DBpedia-q710195"};//DBpedia-q482443","DBpedia-q330584"};
		(new Application()).createDataSet(null, ids);
	}

}
