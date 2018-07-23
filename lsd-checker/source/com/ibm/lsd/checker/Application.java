/**
 * 
 */
package com.ibm.lsd.checker;

import com.ibm.lsd.checker.drivers.LSDExpandEachDriver;

/**
 * @author veronika.thost@ibm.com
 *
 */
public class Application {
	
	public void verifyResults(String queryId) {}
	
	public void generateDatasets() {}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String q =  "test-data/DBpedia-q194196.rq"; 
		String[] data = {q,String.valueOf(true)};

		// LSDCheckerDriver.main(data);
		LSDExpandEachDriver.main(data);
		// LSDExpandAllDriver.main(data);

	}

}
