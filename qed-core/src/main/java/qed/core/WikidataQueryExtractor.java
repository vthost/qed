/**
 * 
 */
package qed.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;





/**
 * @author veronika.thost@ibm.com
 *
 */
public class WikidataQueryExtractor {
	
	String sourceFilename = "wikidata.txt";
	String directory = "wikidata";
	String COMMENT = "#";
	
	public void readSourceFile(String path) {
		path = path + sourceFilename;
		File dir = new File(Utils.DATA_DIR + directory);
		int id = 0;
		
		try (InputStream in = Files.newInputStream(Paths.get(path));
		    BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
		    
			String line = null;
			String q = "";
			boolean readingPrefixes = true; 
			
		    while ((line = reader.readLine()) != null) {
		    	
		    	int i = line.indexOf(COMMENT); 

		    	if(i == 0)
		    		continue;
		    	if(i > 0 && ((line.length() == i+1) || (line.charAt(i+1) != '>'))) 
		    		line = line.substring(0, i);

		    	if(line.startsWith("PREFIX")) {
		    		if(!readingPrefixes) {
		    			readingPrefixes = true;
//		    			if(q.matches("(?i).*prov:wasDerivedFrom/.*"))System.out.println("1"+q);
//		    			if(q.matches("(?i).*\\w*:\\w+/.*"))System.out.println("0"+q);
		    			if(!q.contains("CONSTRUCT") && 
		    					!q.matches("(?i).*\\^(\\w*:\\w+|<(:|/|\\.|\\w)+>)\\s.*") &&
		    					!q.matches("(?i).*(\\w*:\\w+|<(:|/|\\.|\\w)+>)(\\*|\\+)\\s.*") &&
		    					!q.matches("(?i).*(\\w*:\\w+|<(:|/|\\.|\\w)+>)\\??(/|\\|)\\s*(\\w*:\\w+|<(:|/|\\.|\\w)+>).*") &&
		    					!q.matches("(?i).*\\[.*\\].*") &&
//		    					!q.toLowerCase().contains("wasderivedfrom/") &&
//		    					!q.matches("(?i).*\\s\\w*:\\w+\\/.*") &&
//		    					!q.toLowerCase().contains(" group by ") && !q.matches("(?i).*\\/\\s*\\w*:\\w+(\\s|\\)).*") &&
//		    					!q.contains("OFFSET") && 
//		  TODO SERVICE take qs but drop pattern  					!q.contains("GROUP BY") && 
		    					!q.contains("SERVICE")) {
		    				
//		    				if(q.contains("SERVICE"))System.out.println("2 "+id +" "+q);
//		    				System.out.println(q);
//		    				id++;
		    				Utils.writeQueryFile(dir, "/Wikidata-"+id++, q);
//		    				int ix = q.toLowerCase().indexOf("select");
//		    				ix = q.toLowerCase().indexOf("select",ix+1);
//		    				if(ix>0) {
//		    					System.out.println(q);
//		    					System.out.println(Feature.containsFeature(q, Feature.SUBQUERY));
//		    				}
//		    				if(q.matches("(?i).*\\*(\\s|\\)).*"))System.out.println("2 "+id +" "+q);
//		    				if(q.matches("(?i).*(\\w*:\\w+|<(:|/|\\.|\\w)+>)/\\s*(\\w*:\\w+|<(:|/|\\.|\\w)+>).*"))System.out.println("---- "+id +" "+q);
//		    				if(q.matches("(?i).*(\\w*:\\w+|<(:|/|\\w)+>)/.*"))System.out.println("---- "+id +" "+q);
//		    				id++;
		    			} 
		    			
//		    			if(q.matches("(?i).*prov:wasDerivedFrom/.*"))System.out.println("2 "+id +" "+q);
//		    			if(q.matches("(?i).*\\w*:\\w+/.*"))
//		    					System.out.println("3 "+id );
		    			q = "";
		    		}
		    	} else if (readingPrefixes && !line.isEmpty() && !line.matches("\\s+")) {
		    		readingPrefixes = false; 
		    	}
		    	
		    	q += " " + line;
		    }
System.out.println(id+1);
		} catch (IOException x) {
		    System.err.println(x);
		}
	}

	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new WikidataQueryExtractor().readSourceFile(Utils.DATA_DIR);

	}

}
