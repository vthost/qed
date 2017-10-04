package sparql.synthesis.data;

public class Application {

	public static void main(String[] args) {
		
		(new LogQueryExtractor()).extractQueries(null, 10, null);
		(new LogQueryDataExtractor()).extractQueryDataAndResults(10);
	}

}
