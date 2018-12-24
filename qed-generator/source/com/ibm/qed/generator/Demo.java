package com.ibm.qed.generator;

import com.ibm.qed.generator.drivers.ExpandAllDriver;

import qed.core.LogFileDataset;

public class Demo extends qed.core.Demo {
			
	public static void main(String[] args) {

		Demo d = new Demo();
		
		LogFileDataset wd = new LogFileDataset("wikidata", "demo/wikidata.txt", "https://query.wikidata.org/bigdata/namespace/wdq/sparql", "Wikidata-");
		String path = d.extractLogFileBasedDatasets("demo/", wd);

		try {
			ExpandAllDriver.main(new String[] { path, "1", "10", path });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		

	}

}