package lsd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;

public class Utils {
	
	public static String DATA_DIR = System.getProperty("user.dir") + File.separator + "data" + File.separator;	
	public static String QUERY_FILE_EXT = ".txt";
	public static String CONSTRUCT_QUERIES_FILE_EXT = "-cqs.txt";
	public static String QUERY_DATA_FILE_EXT = "-data.xml";
	public static String QUERY_RESULT_FILE_EXT = "-result.xml";
	
//	private static void deleteDir(File file) {
//		
//		if(!file.isFile()) return;
//		
//	    File[] contents = file.listFiles();
//	    if (contents != null) {
//	        for (File f : contents) {
//	            deleteDir(f);
//	        }
//	    }
//	    file.delete();
//	}
	
	public static void cleanDataDir() {
		
		File f = new File(DATA_DIR);
		if(f.exists()) {
			try {
				FileUtils.deleteDirectory(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		new File(DATA_DIR).mkdir();
	}
	
	public static File cleanDataSubDir(String[] config) {
		
		String p = DATA_DIR + (config == null ? "" : String.join("_", config) + File.separator);
		
		File f = new File(p);
		if(f.exists()) {
			try {
				FileUtils.deleteDirectory(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		f = new File(p); 
		f.mkdir();
		return f;
	}

	public static String getQueryId(String lsqIdUrl) {
		return lsqIdUrl.substring(lsqIdUrl.lastIndexOf(File.separator) + 1);
	}
	
//	public static String getQueryFilePath(String lsqIdUrl, String[] config) {
//		return DATA_DIR + (config == null ? "" : String.join("_", config) + File.separator) + 
//				getQueryId(lsqIdUrl) + QUERY_FILE_EXT;
//	}
	public static String getQueryFileName(String lsqIdUrl) {
		return getQueryId(lsqIdUrl) + QUERY_FILE_EXT;
	}
	
	public static String getConstructQueriesFileName(String lsqIdUrl) {
		return getQueryId(lsqIdUrl) + CONSTRUCT_QUERIES_FILE_EXT;
	}
	
	public static String getQueryDataFileName(String lsqIdUrl) {
		return getQueryId(lsqIdUrl) + QUERY_DATA_FILE_EXT;
	}
	
	public static String getQueryResultFileName(String lsqIdUrl) {
		return getQueryId(lsqIdUrl) + QUERY_RESULT_FILE_EXT;
	}
	
	public static void writeQueryFile(File directory, String lsqIdUrl, String query) {

		try {
			String p = directory == null ? Utils.DATA_DIR : directory.getPath() + File.separator;
			
			FileWriter writer = new FileWriter(p + Utils.getQueryFileName(lsqIdUrl));
		  	writer.write(lsqIdUrl);
		  	writer.write("\n");
//		  	using the factory we get a formatting that is more readable. 
//		  	but sometimes it then writes no whitespace ?! (http://lsq.aksw.org/res/DBpedia-q390826)
		  	writer.write(query);//QueryFactory.create(query).toString()); 
		  	writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeConstructQueriesFile(File directory, String lsqIdUrl, List<Query> queries) {

		try {
			FileWriter writer = new FileWriter(directory.getPath() + File.separator + getConstructQueriesFileName(lsqIdUrl));
		  	for (Query query : queries) {
		  		writer.write(query + "\n----------------------------------------------\n"); 
			}
		  	writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeQueryDataFile(File directory, String lsqIdUrl, Model m) {

		try {
			String path = directory.getPath() + File.separator + getQueryDataFileName(lsqIdUrl);
			if(new File(path).isFile())
				m.read(path);
			
			FileWriter writer = new FileWriter(path);
//    	  	writer.write(logQueryIds.get(i));
//    	  	writer.write("\n");
			
            m.write(writer, "RDF/XML");
    	  		writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeQueryResultFile(File directory, String lsqIdUrl, ResultSet rs) {

		try {
			FileWriter writer = new FileWriter(directory.getPath() + File.separator + getQueryResultFileName(lsqIdUrl));
//    	  	writer.write(logQueryIds.get(i));
//    	  	writer.write("\n");
			writer.write(ResultSetFormatter.asXMLString(rs));
//			while(rs.hasNext()) {
//				writer.write(rs.next().toString());
//			}
    	  	writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	returns array with: 0 : id, 1 : query
	public static String[] readQueryFile(File f) {
		try {
			Scanner s = new Scanner(f);
			String id = s.nextLine();
			String q = s.nextLine();
			while(s.hasNextLine()) q += s.nextLine();
			String[] result = {id, q};
			s.close();
			
			return result;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
			
		return null;
	}
	
	public static void main(String[] args) {
		
	}
	

}
