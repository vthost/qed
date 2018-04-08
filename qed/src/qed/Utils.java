package qed;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class Utils {
	
	public static String DATA_DIR = System.getProperty("user.dir") + File.separator + "data" + File.separator;	
	public static String MANIFEST_EVALUATION_FILE_NAME = "manifest_evaluation.ttl";
	public static String MANIFEST_FILE_NAME = "manifest.ttl";
	public static String CONSTRUCT_QUERIES_FILE_EXT = "-cqs.txt";
	public static String QUERY_FILE_EXT = ".rq";
	public static String QUERY_DATA_FILE_EXT = "-data.ttl";
	public static String QUERY_RESULT_FILE_EXT = "-result.ttl";
	
	public static String LSQR_RESOURCE_URI = "http://lsq.aksw.org/res/";
	
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
	
	public static File[] listDirectories(File directory) {
		return directory.listFiles(
				(current, name) -> new File(current+File.separator+name).isDirectory());
	}
	
//	make sure that there is an empty data directory 
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
	
//	make sure that there is a (clean) directory for each name in config
	public static File cleanDataSubDir(String[] config) {

		String p = DATA_DIR + (config == null ? "" : toString(config) + File.separator);
		
		File f = new File(p);
		if(f.exists()) {//should not be the case where we use this method currently
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
	
//	do not delete files with extensions
	public static void cleanDir(File directory, String[] delExtensions) {
		
		for(File f: directory.listFiles(
				(f1, name1) -> Arrays.asList(delExtensions).stream().anyMatch(ext -> name1.endsWith(ext)))) {
		    
			f.delete();
		}
	}
	
	public static String toString(String[] config) {
		return String.join("_", Arrays.asList(config).
				stream().map(String::toLowerCase).collect(Collectors.toList()));
	}
	
//	public static String getConfigTestsName(String config) {
//		return config + " test cases";
//	}
	
//	public static String getTestName(String config, String lsqId) {
//		return config + " semantics: " + lsqId;
//	}

	public static String getQueryId(String lsqIdUrl) {
		return lsqIdUrl.substring(lsqIdUrl.lastIndexOf(File.separator) + 1);
	}
	
	public static String getQueryIdUrl(String lsqId) {
		return LSQR_RESOURCE_URI + lsqId;
	}

	
//	public static String getQueryFilePath(String lsqIdUrl, String[] config) {
//		return DATA_DIR + (config == null ? "" : String.join("_", config) + File.separator) + 
//				getQueryId(lsqIdUrl) + QUERY_FILE_EXT;
//	}
	public static String getQueryFileName(String lsqIdUrl) {
		return getQueryId(lsqIdUrl) + QUERY_FILE_EXT;
	}
	
	public static String getQueryIdFromFileName(String queryFileName) {
		return queryFileName.replace(QUERY_FILE_EXT, "");
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
//		  	writer.write(lsqIdUrl);
//		  	writer.write("\n");
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
	
	public static long writeQueryDataFile(File directory, String lsqIdUrl, Model m) {

		long size = 0;
		try {
			String path = directory.getPath() + File.separator + getQueryDataFileName(lsqIdUrl);
			if(new File(path).isFile())
				m.read(path);
			
			FileWriter writer = new FileWriter(path);
			m.write(writer, "TURTLE");
			size = m.size();
    	  		writer.close();
    	  	
		} catch (IOException e) {
			e.printStackTrace();
		}
		return size;
	}
	
	public static void writeQueryResultFile(File directory, String lsqIdUrl, ResultSet rs) {

		try {
			ResultSetFormatter.output(new FileOutputStream(
					directory.getPath() + File.separator + getQueryResultFileName(lsqIdUrl)), 
					rs, ResultsFormat.FMT_RDF_TURTLE);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeStatisticsFile(Map<String,List<int[]>> stats) {
		try {		
			FileWriter writer = new FileWriter(Utils.DATA_DIR + File.separator + "stats_detail.txt");		  	
			writer.write("config;cqs;cqs-with-data;stmt-avg\n");
			
			stats.entrySet().stream().forEach(e -> {
								
				String config = e.getKey();
				e.getValue().stream().forEach(ns -> {
					try {
						writer.write(config.replace("_", "\\_") + ";" + ns[0] + ";" + ns[1] + ";" + ( ns[1] > 0 ? ns[2]/ns[1] : 0) + "\n");
	
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				});	
			});
			writer.close();
			
			
			FileWriter writer2 = new FileWriter(Utils.DATA_DIR + File.separator + "stats.txt");
			writer2.write("config;cqs;cmin;cmed;cavg;cmax;ecmin;ecmed;ecavg;ecmax;smin;smed;savg;smax\n");
//			writer2.write("config;cqs-avg;cqs-with-data-agv;stmt-avg\n");//TODO add quartiles, min...?

			stats.entrySet().stream().forEach(e -> {
				
				String config = e.getKey();
				List<int[]> v = e.getValue();
				if (v.size() > 0) {
				int[] a = v.stream().mapToInt(ns -> ns[0]).sorted().toArray();
				int l = a.length;
				int cqsmin = v.stream().mapToInt(ns -> ns[0]).min().orElse(0);
				int cqsmed = l % 2 == 0 ? (a[l/2-1] + a[l/2]) / 2 : a[l/2];
				int cqsavg = (int) v.stream().mapToInt(ns -> ns[0]).average().orElse(0);
				int cqsmax = v.stream().mapToInt(ns -> ns[0]).max().orElse(0);

				double[] a2 = v.stream().mapToDouble(ns -> ns[0]==0? 0:ns[1]/ns[0]).sorted().toArray();
				int l2 = a2.length;
				double cqs2min = v.stream().mapToDouble(ns -> ns[0]==0? 0:ns[1]/ns[0]).min().orElse(0);
				double cqs2med = l2 % 2 == 0 ? (a2[l2/2-1] + a2[l2/2]) / 2 : a2[l2/2];
				double cqs2avg = v.stream().mapToDouble(ns -> ns[0]==0? 0:ns[1]/ns[0]).average().orElse(0);
				double cqs2max = v.stream().mapToDouble(ns -> ns[0]==0? 0:ns[1]/ns[0]).max().orElse(0);
				
				int[] a3 = v.stream().mapToInt(ns -> ns[2]).sorted().toArray();
				int l3 = a3.length;
				int smin = v.stream().mapToInt(ns -> ns[2]).min().orElse(0);
				int smed = l3 % 2 == 0 ? (a3[l3/2-1] + a3[l3/2]) / 2 : a3[l3/2];
				int savg = (int) v.stream().mapToInt(ns -> ns[2]).average().orElse(0);
				int smax = v.stream().mapToInt(ns -> ns[2]).max().orElse(0);
				try {
					writer2.write(config.replace("_", "\\_") + ";"+v.size()+ ";"  
				+ cqsmin+ ";"  +cqsmed+ ";"+cqsavg+ ";" +cqsmax   + ";" 
				+cqs2min+ ";"  +cqs2med+ ";" +cqs2avg+ ";"  +cqs2max + ";" + smin +";" + smed+ ";" + savg  +";" + smax  + "\n" );

				} catch (IOException e1) {
					e1.printStackTrace();
				}
				}
			});
			
			writer2.close();
						
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
//	one simple in each subdir and manifest-all.ttl
	public static void writeManifestFiles() { //, String lsqId) {

		String mfURI = "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest/";
		String qtURI = "http://www.w3.org/2001/sw/DataAccess/tests/test-query/";
//		to add properties approval/approvedBy
//		String dawgtURI = "http://www.w3.org/2001/sw/DataAccess/tests/test-dawg/";
		
		Resource manifest = ResourceFactory.createResource(mfURI + "Manifest");
		Resource queryEvaluationTest = ResourceFactory.createResource(mfURI + "QueryEvaluationTest");

//		TODO discuss: do we want to add other properties, e.g., describing that the tests are from us?
		Property include = ResourceFactory.createProperty(mfURI, "include");
		Property entries = ResourceFactory.createProperty(mfURI, "entries"); 
		Property name = ResourceFactory.createProperty(mfURI, "name"); 
		Property action = ResourceFactory.createProperty(mfURI, "action"); 
		Property result = ResourceFactory.createProperty(mfURI, "result"); 
		Property query = ResourceFactory.createProperty(qtURI, "query"); 
		Property data = ResourceFactory.createProperty(qtURI, "data"); 
		
		File[] dirs = listDirectories(new File(DATA_DIR));
		
		for(File f: dirs) {		
			//create one manifest file for each test config
			String config = f.getName();			
//				TODO fix sth like:
			String dummyURI = "http://www.w3.org/2001/sw/DataAccess/tests/data-r2/"+config+"/manifest/";
			
			Model m = ModelFactory.createDefaultModel();
			m.setNsPrefix("rdf", RDF.getURI());
			m.setNsPrefix("rdfs", RDFS.getURI());//System.out.println(RDFS.getURI());
			m.setNsPrefix("mf", mfURI);
			m.setNsPrefix("qt", qtURI);
			m.setNsPrefix("", dummyURI);

			List<RDFNode> testList = new ArrayList<RDFNode>();

			Resource tests = m.createResource("").
					addProperty(RDF.type, manifest).
					addProperty(RDFS.comment, config + " test cases");

			for(File qf: f.listFiles(
					(dir, name1) -> name1.toLowerCase().endsWith(QUERY_FILE_EXT))) {

				String lsqId = getQueryIdFromFileName(qf.getName());
				String lsqIdUrl = getQueryIdUrl(lsqId);
				
				Resource test = m.createResource(dummyURI+"dawg-"+config+"-"+lsqId).
						addProperty(RDF.type, queryEvaluationTest).
						addProperty(name, config + " semantics: " + getQueryIdFromFileName(qf.getName())).
						addProperty(action, m.createResource().
								addProperty(query, m.createResource(getQueryFileName(lsqIdUrl))).
								addProperty(data, m.createResource(getQueryDataFileName(lsqIdUrl)))).							
						addProperty(result, m.createResource(getQueryResultFileName(lsqIdUrl)));
	//							to describe syntax tree
	//							test.addProperty(RDFS.comment, );
	//							to add instance of dawgt:Approved
	//							test.addProperty(approval, );
	//							test.addProperty(approvedBy, );
				testList.add(test);
			}
			
			tests.addProperty(entries, m.createList(testList.toArray(new RDFNode[0])));
			
			try {
				m.write(new FileOutputStream(
						DATA_DIR + f.getName() + File.separator + MANIFEST_FILE_NAME),
						"TURTLE");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		//one manifest file for collecting everything
		Model m = ModelFactory.createDefaultModel();
		m.setNsPrefix("rdf", RDF.getURI()).
		setNsPrefix("rdfs", RDFS.getURI()).
		setNsPrefix("mf", mfURI);	
		m.createResource("").
				addProperty(RDF.type, manifest).
				addProperty(RDFS.label, "SPARQL Query Evaluation tests").
				addProperty(include, m.createList(
						Arrays.asList(dirs).stream().
						map(dir -> { 
							return m.createResource(dir.getName()+File.separator+MANIFEST_FILE_NAME);}).
						collect(Collectors.toList()).toArray(new RDFNode[0])));

		
		try {
			m.write(new FileOutputStream(
					Utils.DATA_DIR + MANIFEST_EVALUATION_FILE_NAME),
					"TURTLE");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	//	returns array with: 0 : id, 1 : query
	public static String[] readQueryFile(File f) {
		try {
			Scanner s = new Scanner(f);
			String id = getQueryIdFromFileName(f.getName()) ;//s.nextLine();
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
//		Utils.writeManifestFiles();
		System.out.println(ResultsFormat.FMT_RDF_TURTLE);
//		String[] a = {CONSTRUCT_QUERIES_FILE_EXT,"-data.xml","-result.xml"};
//		Utils.cleanDir(new File("/Users/thost/Desktop/git/2017/code/workspace_lsd/lsd/data/optional"), a);
	}
	

}
