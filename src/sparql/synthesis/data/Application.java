package sparql.synthesis.data;

public class Application {
	
	private String defaultLog = "http://dbpedia.org/sparql";

	public void createDataSet(String logUri, String[][] configs, int queryNumMax, int querySizeMin, 
			int queryResultSizeMin, int datasetSizeMax) {
		
		logUri = logUri == "" || logUri == null ? defaultLog : logUri;

		(new LogQueryExtractor()).extractQueries(logUri, configs, queryNumMax, querySizeMin, queryResultSizeMin);
		(new LogQueryDataExtractor()).extractQueryDataAndResults(logUri, datasetSizeMax);
	}
	
	public static void main(String[] args) {
		
		(new Application()).createDataSet(null, null, 0, 0, 0, 0);
	}

}
