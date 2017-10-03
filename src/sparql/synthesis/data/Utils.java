package sparql.synthesis.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;

public class Utils {
	
	public static String getQueryId(String lsqIdUrl) {
		return lsqIdUrl.substring(lsqIdUrl.lastIndexOf("/") + 1);
	}
	
	public static String getQueryFilePath(String lsqIdUrl) {
		return Config.DATA_DIR + Utils.getQueryId(lsqIdUrl) + Config.QUERY_FILE_EXT;
	}
	
	public static String getQueryDataFilePath(String lsqIdUrl) {
		return Config.DATA_DIR + Utils.getQueryId(lsqIdUrl) + Config.QUERY_DATA_FILE_EXT;
	}
	
	public static String getQueryResultFilePath(String lsqIdUrl) {
		return Config.DATA_DIR + Utils.getQueryId(lsqIdUrl) + Config.QUERY_RESULT_FILE_EXT;
	}
	
	public static void writeQueryFile(String lsqIdUrl, String query) {

		try {
			FileWriter writer = new FileWriter(Utils.getQueryFilePath(lsqIdUrl));
		  	writer.write(lsqIdUrl);
		  	writer.write("\n");
		  	writer.write(query);
		  	writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeQueryDataFile(String lsqIdUrl, Model m) {

		try {
			FileWriter writer = new FileWriter(Utils.getQueryDataFilePath(lsqIdUrl));
//    	  	writer.write(logQueryIds.get(i));
//    	  	writer.write("\n");
            m.write(writer, "RDF/XML");
    	  	writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeQueryResultFile(String lsqIdUrl, ResultSet rs) {

		try {
			FileWriter writer = new FileWriter(Utils.getQueryResultFilePath(lsqIdUrl));
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
			String[] result = {s.nextLine(), s.nextLine()};
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
