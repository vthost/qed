package qed.core;

import java.io.File;

public interface Constants {

	public static String DATA_DIR = System.getProperty("user.dir")+ File.separator +".."+File.separator +//".."+File.separator +
			"data" + File.separator;	
	
	public static String MANIFEST_EVALUATION_FILE_NAME = "manifest_evaluation.ttl";
	public static String MANIFEST_FILE_NAME = "manifest.ttl";
	public static String CONSTRUCT_QUERIES_FILE_EXT = "-cqs.txt";
	public static String QUERY_FILE_EXT = ".rq";
	public static String DATA_FILE_EXT = "-data.ttl";
	public static String RESULT_FILE_EXT = "-result.ttl";

}
