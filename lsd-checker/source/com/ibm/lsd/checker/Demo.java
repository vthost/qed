package com.ibm.lsd.checker;

import java.io.File;

import com.ibm.lsd.checker.drivers.LSDExpandAllDriver;

import qed.core.Constants;
import qed.core.Utils;

public class Demo {
	

	String[] graphs = {"http://dbpedia.org", "http://linkedgeodata.org", "http://data.semanticweb.org", "http://bm.rkbexplorer.com"};
	String[] confignames = {"simple", "ex"};
	
	public void extendDatasets() {
		
		String tmp = System.getProperty("user.dir")+File.separator +".."+ File.separator + "data" + File.separator + "data-" ;
		
		for (String g : graphs) {
			for (int i = 0; i < confignames.length; i++) {
				String d0 = tmp + g.substring(7,g.length()-4) + "-" + confignames[i] + File.separator;
				
				File[] dirs = (new File(d0)).listFiles(
						(current, name) -> new File(current+File.separator+name).isDirectory());
				
				for(File d: dirs) {
					File[] fs = d.listFiles(
							(current, name) -> name.endsWith(Constants.QUERY_FILE_EXT));
					
					for(File f: fs) {
						System.out.println(f);
						try {
							String[] data = {f.toURI().toURL().toString(),String.valueOf(true), d0}; 
							LSDExpandAllDriver.main(data);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
			}		
		}
	

	}

	public static void main(String[] args) {
		new Demo().extendDatasets();
	}
}
