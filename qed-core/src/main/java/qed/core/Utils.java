package qed.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

public class Utils implements Constants {
	

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
	
	public static String toString(Feature[] config) {
		return String.join("_", Arrays.asList(config).
				stream().map(Feature::toString).map(String::toLowerCase).collect(Collectors.toList()));
	}
	
	public static File[] listDirectories(File directory) {
		return directory.listFiles(
				(current, name) -> new File(current+File.separator+name).isDirectory());
	}
	
	public static File[] listQueryFiles(File directory) {
		return directory.listFiles(
				(current, name) -> name.endsWith(Constants.QUERY_FILE_EXT));
	}
	
//	make sure that there is an empty data directory 
	public static File makeDir(String path) {
		
		File f = new File(path);
		if(f.exists()) {
			try {
				FileUtils.deleteDirectory(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		File dir = new File(path);
		dir.mkdir();
		return dir;
	}
	
//	make sure that there is a (clean) directory for the given config
	public static File makeSubDir(String path, Feature[] config) {

		String p = path + (config == null ? "data" : toString(config)) + File.separator;
		
		File f = new File(p);
		if(f.exists()) {//should not be the case where we use this method currently
			try {
				FileUtils.deleteDirectory(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		f = new File(p); 
		f.mkdir();//System.out.println(f);
		return f;
	}
	
//	delete files with extensions in delExtensions
	public static void cleanDir(File directory, String[] delExtensions) {
		
		for(File f: directory.listFiles(
				(f1, name1) -> Arrays.asList(delExtensions).stream().anyMatch(ext -> name1.endsWith(ext)))) {
		    
			f.delete();
		}
	}
	
	public static String getQueryIdFromQueryFileName(String queryFileName) {
		return queryFileName.replace(QUERY_FILE_EXT, "");
	}
	
	public static String getQueryFileName(String qid) {
		return qid + QUERY_FILE_EXT;
	}
	
	public static String getConstructQueriesFileName(String qid) {
		return qid + CONSTRUCT_QUERIES_FILE_EXT;
	}
	
	public static String getDataFileName(String qid) {
		return qid + DATA_FILE_EXT;
	}
	
	public static String getResultFileName(String qid) {
		return qid + RESULT_FILE_EXT;
	}
	
	public static void writeQueryFile(File directory, String qid, String query) {

		try {

			FileWriter writer = new FileWriter(directory.getPath() + File.separator + Utils.getQueryFileName(qid));
//		  	using the factory we would get a formatting that is more readable. 
//		  	but sometimes it then writes no whitespace ?! (http://lsq.aksw.org/res/DBpedia-q390826)
		  	writer.write(query);//QueryFactory.create(query).toString()); 
		  	writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeConstructQueriesFile(File directory, String qid, List<Query> queries) {

		try {
			FileWriter writer = new FileWriter(directory.getPath() + File.separator + getConstructQueriesFileName(qid));
		  	for (Query query : queries) {
		  		writer.write(query + "\n----------------------------------------------\n"); 
			}
		  	writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	extend existing file
	public static int writeDataFile(File directory, String qid, Model m) {

		long size = 0;
		try {
			String path = directory.getPath() + File.separator + getDataFileName(qid);
			if(new File(path).isFile())
				m.read(path);

			FileWriter writer = new FileWriter(path);
			m.write(writer, "TURTLE");
			size = m.size();
    	  		writer.close();
    	  	
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (int) size;
	}
	
	public static void writeResultFile(File directory, String qid, ResultSet rs) {

		try {
			ResultSetFormatter.output(new FileOutputStream(
					directory.getPath() + File.separator + getResultFileName(qid)), 
					rs, ResultsFormat.FMT_RDF_TURTLE);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//	one simple in each subdir and manifest-all.ttl
	public static void writeManifestFiles(String path, String qbaseURI) { 

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
		
		
		File[] dirs = listDirectories(new File(path));		
		boolean onedir = false;
		if(dirs.length == 0) {
			onedir = true;
			dirs = new File[1];
			dirs[0] = new File(path);
		}
		
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

				String qid = getQueryIdFromQueryFileName(qf.getName());
//				String qidUrl = qbaseURI + qid;
//				TODO test if below we neeed uris insead iof qids
				Resource test = m.createResource(dummyURI+"dawg-"+config+"-"+qid).
						addProperty(RDF.type, queryEvaluationTest).
						addProperty(name, config + " semantics: " + getQueryIdFromQueryFileName(qf.getName())).
						addProperty(action, m.createResource().
								addProperty(query, m.createResource(getQueryFileName(qid))).
								addProperty(data, m.createResource(getDataFileName(qid)))).							
						addProperty(result, m.createResource(getResultFileName(qid)));
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
						path + (onedir? "":f.getName() + File.separator) + MANIFEST_FILE_NAME),
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
					path + MANIFEST_EVALUATION_FILE_NAME),
					"TURTLE");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	//	returns array with: 0 : id, 1 : query
	public static String[] readQueryFile(File f) {
		try {
			Scanner s = new Scanner(f);
			String id = getQueryIdFromQueryFileName(f.getName()) ;//s.nextLine();
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
		Utils.writeManifestFiles(DATA_DIR+"wikidata/", "wikidatauri");
	}

	

}
