/**
 * 
 */
package com.ibm.lsd.checker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ibm.lsd.checker.drivers.LSDExpandAllDriver;
import com.ibm.lsd.checker.drivers.LSDExpandEachDriver;

import qed.core.Utils;

/**
 * @author veronika.thost@ibm.com
 *
 */
public class Application {
	
	public static String DATA_DIR =  System.getProperty("user.dir")+ File.separator +".."+File.separator +".."+File.separator + "data" + File.separator;	

	
	public void verifyResults(String queryId) {}
	
	public void generateDatasets() {}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		File[] dirs = (new File(DATA_DIR)).listFiles(
				(current, name) -> new File(current+File.separator+name).isDirectory());
		for(File d: dirs) {
			for(File f:d.listFiles()) {
				System.out.println(f);
				String[] data = {f.toURI().toURL().toString(),String.valueOf(true), DATA_DIR}; //false)};
				LSDExpandEachDriver.main(data);
			}

		}
		

		
//		String q =  "test-data/DBpedia-q194198.rq"; 
//		String[] data = {q,String.valueOf(true)}; //false)};
//
//		// LSDCheckerDriver.main(data);
////		LSDExpandEachDriver.main(data);
//		 LSDExpandAllDriver.main(data);

	}

}
