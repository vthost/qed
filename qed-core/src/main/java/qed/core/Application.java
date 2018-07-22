package qed.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.jena.ext.com.google.common.base.Strings;


/**
 */
public class Application 
{

////		with LSQ logUri can be one of	
//		DBpedia: http://dbpedia.org
//		Linked Geo Data: http://linkedgeodata.org
//		Semantic Web Dog Food: http://data.semanticweb.org
//		British Museum: http://bm.rkbexplorer.com
	
//	TODO we could add also parameters querySizeMax...
	public void createDataSet(String logEndpoint, String graphUri, String[][] configs, int queryNumMax, int querySizeMin, 
			int queryResultSizeMin, String dataEndpoint, int datasetSizeMax, String dataFile) {
		
		if(!Strings.isNullOrEmpty(dataFile))//TODO check if valid file path
			Utils.DATA_DIR = dataFile;	
//System.out.println(Utils.DATA_DIR);
		(new LogQueryExtractor()).extractQueries(logEndpoint, graphUri, configs, queryNumMax, querySizeMin, queryResultSizeMin);
		(new LogQueryDataExtractor()).extractQueryDataAndResults(dataEndpoint, datasetSizeMax);
	}
	
	public void createDataSet(String logEndpoint, String graphUri, String[] queryIds, String dataEndpoint, String dataFile) {
		
		if(queryIds == null) return;
		
		if(!Strings.isNullOrEmpty(dataFile))
			Utils.DATA_DIR = dataFile;	

		(new LogQueryExtractor()).extractQueries(logEndpoint, graphUri, queryIds);
		(new LogQueryDataExtractor()).extractQueryDataAndResults(dataEndpoint, queryIds.length);
	}
	
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {


		String[] opts = {"q","u","d","m","s","r","f"};
		String[] longOpts = {"query-log-endpoint","query-log-uri","data-endpoint",
				"max-query-num","min-query-size","min-query-result-num","file"};
		
		Options options = new Options();
		for (int i = 0; i < opts.length; i++) {
			options.addOption(Option.builder(opts[i])
				     .required(false)
				     .hasArg(true)
				     .longOpt(longOpts[i])
				     .build());
		}
		
		
		Properties ps = new Properties();
		
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		InputStream is = cl.getResourceAsStream("config.properties");
		try {
			ps.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		CommandLineParser parser = new DefaultParser();
		try {
		    // parse the command line arguments
		    CommandLine line = parser.parse( options, args );
		    
		    for (String opt : opts) {
		    		if(line.hasOption(opt)) ps.put(opt, line.getOptionValue(opt));
			}
		}
		catch( ParseException exp ) {
//		    System.out.println( "Unexpected exception:" + exp.getMessage() );
		}
		

		
		(new Application()).createDataSet(
		ps.getProperty("q"), ps.getProperty("u"), null, 
		Integer.valueOf(ps.getProperty("m")),
		Integer.valueOf(ps.getProperty("s")),
		Integer.valueOf(ps.getProperty("r")), 
		ps.getProperty("d"),0,
		ps.getProperty("f"));


//		
//		String[] ids = {"DBpedia-q710195"};//DBpedia-q482443","DBpedia-q330584"};
//		(new Application()).createDataSet(null, ids);
	}

	

//    	Veronikas-MacBook:qed Veronika$ rm -r ~/.m2/repository
//    	Veronikas-MacBook:qed Veronika$ java -version
//    	java version "1.8.0_05"
//    	Java(TM) SE Runtime Environment (build 1.8.0_05-b13)
//    	Java HotSpot(TM) 64-Bit Server VM (build 25.5-b02, mixed mode)
//    	Veronikas-MacBook:qed Veronika$ export JAVA_HOME="$(/usr/libexec/java_home -v 1.8)"
//    	Veronikas-MacBook:qed Veronika$ echo $JAVA_HOME
//    	/Library/Java/JavaVirtualMachines/jdk1.8.0_05.jdk/Contents/Home

}
