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
import java.util.ArrayList;
import java.util.List;





/**
 * @author veronika.thost@ibm.com
 *
 */
public class LogFileQueryExtractor {
	
	String sourceFilename = "wikidata.txt";
	String directory = "wikidata";
	String COMMENT = "#";
	String SELECT = "select";
	
	public List<String>  readSourceFile(String path) { //, String idstr) { , String dir) {
		
		List<String> qs = new ArrayList<String>();
//		String srcpath = path + sourceFilename;
//		File dir = Utils.makeDir(path + directory);
//		int id = 0;
		
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

		    	if(line.startsWith("PREFIX") || line.toLowerCase().startsWith(SELECT)) {
		    		if(!readingPrefixes) {
		    			readingPrefixes = true;
//		    			if(q.matches("(?i).*prov:wasDerivedFrom/.*"))System.out.println("1"+q);
//		    			if(q.matches("(?i).*\\w*:\\w+/.*"))System.out.println("0"+q);
		    			if(!q.contains("CONSTRUCT") && 
//		    					!q.matches("(?i).*\\^(\\w*:\\w+|<(:|/|\\.|\\w)+>)\\s.*") &&
//		    					!q.matches("(?i).*(\\w*:\\w+|<(:|/|\\.|\\w)+>)(\\*|\\+)\\s.*") &&
//		    					!q.matches("(?i).*(\\w*:\\w+|<(:|/|\\.|\\w)+>)\\??(/|\\|)\\s*(\\w*:\\w+|<(:|/|\\.|\\w)+>).*") &&
//		    					!q.matches("(?i).*\\[.*\\].*") &&
//		    					!q.toLowerCase().contains("wasderivedfrom/") &&
//		    					!q.matches("(?i).*\\s\\w*:\\w+\\/.*") &&
//		    					!q.toLowerCase().contains(" group by ") && !q.matches("(?i).*\\/\\s*\\w*:\\w+(\\s|\\)).*") &&
//		    					!q.contains("OFFSET") && 
//		  TODO SERVICE take qs but drop pattern  					!q.contains("GROUP BY") && 
		    					!q.contains("SERVICE")) {
		    				
//		    				if(q.contains("SERVICE"))System.out.println("2 "+id +" "+q);
//		    				System.out.println(q);
//		    				id++;
		    				String[][] ps = {
		    						{"wd:",	"PREFIX wd: <http://www.wikidata.org/entity/>"},
		    						{"wdt:",	"PREFIX wdt: <http://www.wikidata.org/prop/direct/>"},
		    						{"wikibase:",	"PREFIX wikibase: <http://wikiba.se/ontology#>"},
		    						{"p:",	"PREFIX p: <http://www.wikidata.org/prop/>"},
		    						{"ps:",	"PREFIX ps: <http://www.wikidata.org/prop/statement/>"},
		    						{"pq:",	"PREFIX pq: <http://www.wikidata.org/prop/qualifier/>"},
		    						{"rdfs:",	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"},
		    						{"bd:",	"PREFIX bd: <http://www.bigdata.com/rdf#>"},
		    						{"psv:",	"PREFIX psv: <http://www.wikidata.org/prop/statement/value/>"},
		    						{"prov:",	"PREFIX prov: <http://www.w3.org/ns/prov#>"},
		    						{"pr:",	"PREFIX pr: <http://www.wikidata.org/prop/reference/>"},
		    						{"schema:",	"PREFIX schema: <http://schema.org/>"},
		    						{"geo:",	"PREFIX geo:  <http://www.opengis.net/ont/geosparql#>"},
		    						{"rdf:",	"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"},
		    						{"xsd:",	"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"},
		    						{"hint:",	"PREFIX hint: <http://www.bigdata.com/queryHints#>"}};
		    				
		    				for (String[] p : ps) {
								if(q.contains(p[0]) && !q.contains("PREFIX "+p[0]))
//									System.out.println(p[0]);
//									System.out.println(q);
									q = p[1]+ " " + q;
							}
//		    				String[] qq = {"Wikidata-"+id++,q};
		    				qs.add(q);
//		    				Utils.writeQueryFile(dir, "Wikidata-"+id++, q);
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
//		    			System.out.println(q);
//		    			if(q.matches("(?i).*prov:wasDerivedFrom/.*"))System.out.println("2 "+id +" "+q);
//		    			if(q.matches("(?i).*\\w*:\\w+/.*"))
//		    					System.out.println("3 "+id );
		    			
		    		} q = "";
		    	} else if (readingPrefixes && !line.isEmpty() && !line.matches("\\s+")) {
		    		readingPrefixes = false; 
		    	}
		    	
		    	q += " " + line;
		    }
//System.out.println(id+1);
		} catch (IOException x) {
		    System.err.println(x);
		}
		
		return qs;
	}

	public void extractQueries(String src, String dst, String idstr) {
		File dir = new File(dst);
		List<String> qs = readSourceFile(src);
		int id = 0;
		for (String q : qs) {
			Utils.writeQueryFile(dir, idstr+id++, q);
		}
		
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		new LogFileQueryExtractor().extractQueries(src, dst, idstr);

	}

}
