/**
 * 
 */
package com.ibm.lsd.checker;

import com.ibm.lsd.checker.drivers.LSDCheckerDriver;
import com.ibm.lsd.checker.drivers.LSDExpandAllDriver;

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
		
		String[] data = {"test-data"};
		
//		LSDCheckerDriver.main(data);
//		LSDExpandEachDriver.main(data);
		LSDExpandAllDriver.main(data);

	}

}
