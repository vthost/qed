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

import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;

/**
 *
 */
public class LogFileQueryExtractor implements QueryExtractor {
	
	String COMMENT = "#";
	
	private List<String> readSourceFile(String path) {
		
		List<String> qs = new ArrayList<String>();

		try (InputStream in = Files.newInputStream(Paths.get(path));
				BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
		    
			String line = null;
			String q = "";
			boolean readingPrefixes = true; 
			
		    while ((line = reader.readLine()) != null) {
		    	
		    	int i = line.indexOf(COMMENT); 

		    	if(i == 0)
		    		continue;
		    	
		    	if(i > 0 && ((line.length() == i+1) || (line.charAt(i+1) != '>'))) //if comment sign is part of iri, then ignore it
		    		line = line.substring(0, i);

		    	if(line.toLowerCase().startsWith("prefix") || line.toLowerCase().startsWith("select")) {
		    		
		    		//starting new query, so finalize previously collected query
		    		if(!readingPrefixes) {
		    			readingPrefixes = true;

		    			if(!q.toLowerCase().contains("construct") &&  !q.toLowerCase().contains("service")) {
		    				
		    				//add prefix if necessary
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

									q = p[1]+ " " + q;
							}
		    				
		    				try {
		    					//only consider query if no parse exception
		    					QueryFactory.create(q, Syntax.syntaxARQ);
		    					
		    					qs.add(q);
		    					
		    				} catch (Exception e) {}	
		    			} 	
		    		} 
		    		
		    		q = "";
		    		
		    	} else if (readingPrefixes && !line.isEmpty() && !line.matches("\\s+")) {
		    		readingPrefixes = false; 
		    	}
		    	
		    	q += " " + line;
		    }

		} catch (IOException e) {
		    System.err.println(e);
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

}
