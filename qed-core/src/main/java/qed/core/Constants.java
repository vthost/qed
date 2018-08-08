package qed.core;

public interface Constants {

//	public String DATA_DIR = System.getProperty("user.dir")+ File.separator +".."+File.separator +".."+File.separator + "data" + File.separator;	
	
	public static String MANIFEST_EVALUATION_FILE_NAME = "manifest_evaluation.ttl";
	public static String MANIFEST_FILE_NAME = "manifest.ttl";
	public static String CONSTRUCT_QUERIES_FILE_EXT = "-cqs.txt";
	public static String QUERY_FILE_EXT = ".rq";
	public static String QUERY_DATA_FILE_EXT = "-data.ttl";
	public static String QUERY_RESULT_FILE_EXT = "-result.ttl";
	
	public static String LSQR_RESOURCE_URI = "http://lsq.aksw.org/res/";
	public static String LSQR_SPARQL_EP = "http://lsq.aksw.org/sparql";
}
