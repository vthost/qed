package com.ibm.lsd.checker;

import com.ibm.lsd.checker.drivers.LSDExpandAllDriver;

import qed.core.Constants;
import qed.core.Dataset;
import qed.core.Feature;
import qed.core.LogQueryExtractor;
import qed.core.Utils;

public class Demo {
		
	public static void generateDatasets(String queries, String outputDir, Dataset dataset, Feature[][] cs) throws Exception {
		Utils.DATA_DIR = queries;
		new LogQueryExtractor().extractQueries(Constants.LSQR_SPARQL_EP, dataset.graphUri, cs, 100, 1, 0);

		LSDExpandAllDriver.main(new String[] { queries, "1", "10", outputDir });
	}
	
	public static void main(String[] args) throws Exception {
		Feature[][] cs = new Feature[][] {
			new Feature[] { Feature.UNION }
		};
	
		generateDatasets(args[0], args[1], Dataset.valueOf(args[2]), cs);
	}

}