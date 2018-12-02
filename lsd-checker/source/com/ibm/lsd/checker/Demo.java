package com.ibm.lsd.checker;

import com.ibm.lsd.checker.drivers.LSDExpandAllDriver;

import qed.core.Constants;
import qed.core.DataExtractor;
import qed.core.LSQDataset;
import qed.core.LogFileDataset;
import qed.core.LogFileQueryExtractor;
import qed.core.Utils;

public class Demo {
		
	public static void generateLogFileBasedDatasets(String path, String name, LogFileDataset dataset) { 

		
			path = qed.core.Demo.getNewDirectory(path + name);
			
			new LogFileQueryExtractor().extractQueries(dataset.getLocation(),path,dataset.getIdStr());

			new DataExtractor().extractQueryDataAndResults(dataset.getEndpoint(), 2, path);
			
			Utils.finalizeStatistics(path, 7);
			
			try {
				LSDExpandAllDriver.main(new String[] { path, "1", "10", Constants.DATA_DIR });
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	
	public static void main(String[] args) throws Exception {
//		Feature[][] cs = new Feature[][] {
//			new Feature[] { Feature.OPTIONAL }
//		};
	
//		generateDatasets(args[0], args[1], Dataset.valueOf(args[2]), cs);
		
		
//		Demo d = new Demo();
		
//		LogFileDataset wd = new LogFileDataset("wikidata", Constants.DATA_DIR+"wikidata.txt", "https://query.wikidata.org/bigdata/namespace/wdq/sparql", "Wikidata-");
//		d.generateLogFileBasedDatasets( ,wd);
//		LSQDataset dbp = new LSQDataset("dbpedia", "http://localhost:8080/sparql", "http://dbpedia.org");
		
		
		// First test with extracted data...
		String path = Constants.DATA_DIR + "wikidata0";
		
		try {
			LSDExpandAllDriver.main(new String[] { path, "1", "10", Constants.DATA_DIR });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		

	}

}