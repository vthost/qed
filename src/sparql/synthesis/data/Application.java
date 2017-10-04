package sparql.synthesis.data;

public class Application {

	public void createDataSet(String logUri, String[][] configs, int queryNumMax, int querySizeMin, 
			int queryResultSizeMin, int datasetSizeMax) {
		(new LogQueryExtractor()).extractQueries(logUri, configs, queryNumMax, querySizeMin, queryResultSizeMin);
		(new LogQueryDataExtractor()).extractQueryDataAndResults(logUri, datasetSizeMax);
	}
	
	public static void main(String[] args) {
		
//		(new Application()).
	}

}
