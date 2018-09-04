package qed.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
	
	public static String DATA_DIR =  System.getProperty("user.dir")+ File.separator +".."+File.separator  + "data" + File.separator;	

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
	
	public static File[] listQueryFiles(File directory) {
		return directory.listFiles(
				(current, name) -> name.endsWith(Constants.QUERY_FILE_EXT));
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
	public static File cleanDataSubDir(Feature[] config) {

		String p = DATA_DIR + (config == null ? "data" : toString(config)) + File.separator;
		
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
	
//	do not delete files with extensions
	public static void cleanDir(File directory, String[] delExtensions) {
		
		for(File f: directory.listFiles(
				(f1, name1) -> Arrays.asList(delExtensions).stream().anyMatch(ext -> name1.endsWith(ext)))) {
		    
			f.delete();
		}
	}
	
	public static String toString(Feature[] config) {
		return String.join("_", Arrays.asList(config).
				stream().map(Feature::toString).map(String::toLowerCase).collect(Collectors.toList()));
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
	
//	extend existing file
	public static int writeQueryDataFile(File directory, String lsqIdUrl, Model m) {

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
		return (int) size;
	}
	
//	public static void writeQueryDataFile2(File directory, String lsqIdUrl, Dataset dataset) {
//
//		String path = directory.getPath() + File.separator + getQueryDataFileName(lsqIdUrl);
//		
//		try {
//			RDFDataMgr.write(new FileOutputStream(path), dataset, Lang.TURTLE);
//			
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//
//	}
	
	public static void writeQueryResultFile(File directory, String lsqIdUrl, ResultSet rs) {

		try {
			ResultSetFormatter.output(new FileOutputStream(
					directory.getPath() + File.separator + getQueryResultFileName(lsqIdUrl)), 
					rs, ResultsFormat.FMT_RDF_TURTLE);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public static void writeStatisticsFile1(List<String> ids, List<int[]> stats, List<List<Feature>> features, String path) {
		try {		
			FileWriter writer = new FileWriter(path + File.separator + "stats_detail.txt");		  	
			writer.write("qid;qtriples;cqs;cqs-with-data;triples;features\n");//rtriples;
			
			for (int i = 0; i < ids.size(); i++) {
				try {
					int[] stat = stats.get(i);
					writer.write(ids.get(i) + ";" + stat[0] + ";" + stat[1] + ";" + stat[2] + ";" + stat[3] + ";" +//stat[4] + ";" +
//							( stat[1] > 0 ? stat[2]/stat[1] : 0) + ";" + 
							String.join(",", features.get(i).stream().map(f -> f.toString().replace("_", "\\_")).
									collect(Collectors.toList())) + "\n");

				} catch (IOException e1) {//config.replace("_", "\\_")
					e1.printStackTrace();
				}
			}
			writer.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	public static void statsSummary(String path, int maxFeats) {
		Map<String,List<String>> fqs = new HashMap<String,List<String>>();
		Map<String,List<List<Integer>>> fss = new HashMap<String,List<List<Integer>>>();
		
		Map<String,Integer> ok = new HashMap<String,Integer>();
		for (Feature f : Feature.values()) {
			ok.put(f.name(),0);
		}
		try (InputStream in = Files.newInputStream(Paths.get(path + "stats_detail.txt"));
			    BufferedReader reader =
			      new BufferedReader(new InputStreamReader(in))) {
			    String line = reader.readLine();
			    while ((line = reader.readLine()) != null) {
//			    System.out.println(line);
			    for (Feature f : Feature.values()) {
			    	if(line.contains(f.name()))
					ok.put(f.toString(),ok.get(f.name()) + 1);
				}}
		} catch (IOException x) {
		    System.err.println(x);
		}

		System.out.println(ok);
		List<Integer> is = new ArrayList<Integer>();
		is.addAll(ok.values());
		List<String> ks = new ArrayList<String>();
		ks.addAll(ok.keySet());
		List<String> ks1 = new ArrayList<String>();
		for (int i = 0; i < maxFeats; i++) {
			int k = is.stream().mapToInt(j -> j).max().orElse(0);
			int index = is.indexOf(k);
			int e = is.remove(index);
			ks1.add(ks.remove(index));
			System.out.println(e == k);
		}
System.out.println(ks1);

		Map<String,Integer> gcs = path.contains("dbpedia")? generatedCounts(Dataset.DBPEDIA) : generatedCounts(Dataset.WIKIDATA);
		try (InputStream in = Files.newInputStream(Paths.get(path + "stats_detail.txt"));
		    BufferedReader reader =
		      new BufferedReader(new InputStreamReader(in))) {
		    String line = reader.readLine();int c = 0;
		    while ((line = reader.readLine()) != null) {c++;
		        String fs = String.join(",", Arrays.asList(line.substring(line.lastIndexOf(";")+1).split(",")).stream().
		        		filter(s -> ks1.contains(s)).
		        		map(s -> s.length() > 1? s.substring(0, 2):s).toArray(String[]::new));
		        
		        List<String> ids = null; //new ArrayList<String>();
		        List<List<Integer>> stats = null;//new ArrayList<List<Integer>>();
		        if(!fqs.containsKey(fs)) {
			        	ids = new ArrayList<String>();
			        	fqs.put(fs, ids);
			        	stats = new ArrayList<List<Integer>>();
			        	fss.put(fs, stats);
			        	
			        	for (int i = 0; i < 4; i++) {
							stats.add(new ArrayList<Integer>());
						}
		        } else {
			        	ids = fqs.get(fs);
			        	stats = fss.get(fs);
		        }
		        
		        int j = line.indexOf(";");
	        		ids.add(line.substring(0, j));
	        	
	        	for (int i = 0; i < 4; i++) {
	        		int j2 = line.indexOf(";", j+1);
	        		int co = (int)Double.parseDouble(line.substring(j+1,j2));
	        		if(i == 3 && gcs.containsKey(line.substring(0,line.indexOf(";")))) {
//	        			String li = line.substring(0,line.lastIndexOf(";"));
//	        			System.out.println(li + " "+co);
	        			co = co + gcs.get(line.substring(0,line.indexOf(";")));//Integer.parseInt(li.substring(li.lastIndexOf(";")+1));
//	        			System.out.println(co);
	        		}
	        			
					stats.get(i).add(co);//
					j = j2;
				}
		    }System.out.println(c);
		} catch (IOException x) {
		    System.err.println(x);
		}
//		System.out.println(fqs);
//		System.out.println(fss);
		
		Map<String, List<List<Integer>>> treeMap = new TreeMap<>(
		                (Comparator<String>) (o1, o2) -> o1.length() < o2.length() ? -1 : o2.length() < o1.length() ? 1 : o1.compareTo(o2)
		        );
		
        treeMap.putAll(fss);


		
		try {		
			FileWriter writer = new FileWriter(path + "stats.txt");		  	
			writer.write("config& qs& qsize &cqs& cqs-with-data& triples\\\\hline\n");
			
			for (Entry<String, List<List<Integer>>> e : treeMap.entrySet()) {
				List<List<Integer>> vs = e.getValue();
				
//				int s = com.google.common.collect.Streams.zip(vs.get(1).stream(), vs.get(2).stream(), (cqs,cqsd) -> cqsd/cqs));
				List<Integer> tmp = IntStream
				  .range(0, vs.get(1).size())
				  .mapToObj(i -> 10000 * vs.get(2).get(i)/vs.get(1).get(i)).collect(Collectors.toList());
				
				writer.write(e.getKey()+"&"+vs.get(0).size()+"&"+
						(int)vs.get(0).stream().mapToInt(i -> i).average().orElse(0)+"&"+ //qtriples
						(int)vs.get(1).stream().mapToInt(i -> i).average().orElse(0)+"&"+ //cqs
						(int)tmp.stream().mapToInt(i -> i).average().orElse(0)/100 +"&"+ 
						(int)vs.get(3).stream().mapToInt(i -> i).average().orElse(0)+ //triples
						"\\\\\\hline\n");

			}
			writer.close();
			
		} catch (IOException x) {
		    System.err.println(x);
		}
		
	}
	
	public static void statsSummary2(String directory) {
		Map<String,List<String>> fqs = new HashMap<String,List<String>>();
		Map<String,List<List<Integer>>> fss = new HashMap<String,List<List<Integer>>>();
		
		
		Map<String,Integer> gcs = directory.contains("dbpedia")? generatedCounts(Dataset.DBPEDIA) : generatedCounts(Dataset.WIKIDATA);

		File[] dirs = Utils.listDirectories(new File(directory));

//		for each config directory
		for(File d: dirs) {
			try (InputStream in = Files.newInputStream(Paths.get(d + File.separator+"stats_detail.txt"));
				    BufferedReader reader =
				      new BufferedReader(new InputStreamReader(in))) {
				    String line = reader.readLine();
				    while ((line = reader.readLine()) != null) {
				        String fs = String.join(",", Arrays.asList(line.substring(line.lastIndexOf(";")+1).split(",")).stream().map(s -> s.length() > 1? s.substring(0, 2):s).toArray(String[]::new));
				        
				        List<String> ids = null; //new ArrayList<String>();
				        List<List<Integer>> stats = null;//new ArrayList<List<Integer>>();
				        if(!fqs.containsKey(fs)) {
				        	ids = new ArrayList<String>();
				        	fqs.put(fs, ids);
				        	stats = new ArrayList<List<Integer>>();
				        	fss.put(fs, stats);
				        	
				        	for (int i = 0; i < 4; i++) {
								stats.add(new ArrayList<Integer>());
							}
				        } else {
				        	ids = fqs.get(fs);
				        	stats = fss.get(fs);
				        }
				        
				        int j = line.indexOf(";");
			        	ids.add(line.substring(0, j));
			        	
			        	for (int i = 0; i < 4; i++) {
			        		int j2 = line.indexOf(";", j+1);
			        		int co = (int)Double.parseDouble(line.substring(j+1,j2));
			        		if(i == 3 && gcs.containsKey(line.substring(0,line.indexOf(";")))) {
//			        			String li = line.substring(0,line.lastIndexOf(";"));
//			        			System.out.println(li + " "+co);
			        			co = co + gcs.get(line.substring(0,line.indexOf(";")));//Integer.parseInt(li.substring(li.lastIndexOf(";")+1));
//			        			System.out.println(co);
			        		}

							stats.get(i).add(co);
							j = j2;
						}
				    }
				} catch (IOException x) {
				    System.err.println(x);
				}
		}

		
//		System.out.println(fqs);
//		System.out.println(fss);
		
		Map<String, List<List<Integer>>> treeMap = new TreeMap<>(
		                (Comparator<String>) (o1, o2) -> o1.length() < o2.length() ? -1 : o2.length() < o1.length() ? 1 : o1.compareTo(o2)
		        );
		
        treeMap.putAll(fss);


		
		try {		
			FileWriter writer = new FileWriter(directory + "stats.txt");		  	
			writer.write("config& qs& qsize &cqs& cqs-with-data& triples\\\\hline\n");
			
			for (Entry<String, List<List<Integer>>> e : treeMap.entrySet()) {
				List<List<Integer>> vs = e.getValue();
				
//				int s = com.google.common.collect.Streams.zip(vs.get(1).stream(), vs.get(2).stream(), (cqs,cqsd) -> cqsd/cqs));
				List<Integer> tmp = IntStream
				  .range(0, vs.get(1).size())
				  .mapToObj(i -> 10000 * vs.get(2).get(i)/vs.get(1).get(i)).collect(Collectors.toList());
				
				writer.write(e.getKey()+"&"+vs.get(0).size()+"&"+
						(int)vs.get(0).stream().mapToInt(i -> i).average().orElse(0)+"&"+ //qtriples
						(int)vs.get(1).stream().mapToInt(i -> i).average().orElse(0)+"&"+ //cqs
						(int)tmp.stream().mapToInt(i -> i).average().orElse(0)/100 +"&"+ 
						(int)vs.get(3).stream().mapToInt(i -> i).average().orElse(0)+ //triples
						"\\\\\\hline\n");

			}
			writer.close();
			
		} catch (IOException x) {
		    System.err.println(x);
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
				
				DecimalFormat f = new DecimalFormat("#.##");
				try {
					writer2.write(config.replace("_", "\\_") + ";"+v.size()+ ";"  
				+ cqsmin+ ";"  +cqsmed+ ";"+cqsavg+ ";" +cqsmax   + ";" 
				+f.format(cqs2min)+ ";"
				+f.format(cqs2med)+ ";"
				+f.format(cqs2avg)+ ";"
				+f.format(cqs2max)+ ";"
				+smin +";" + smed+ ";" + savg  +";" + smax  + "\n" );

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
	
	public static Map<String,Integer> generatedCounts(Dataset d) {
		Map<String,Integer> m = new HashMap<String,Integer>();
		String d1 = System.getProperty("user.dir")+File.separator  + "../generated" + File.separator;	
		try (InputStream in = Files.newInputStream(Paths.get(d1+ d.toString().toLowerCase() + ".txt"));
			    BufferedReader reader =
			      new BufferedReader(new InputStreamReader(in))) {
			    String line = reader.readLine();
			    while ((line = reader.readLine()) != null) {
			    	int i = line.indexOf(" ");
			    	int j = line.indexOf("-0-data.ttl");
			    	m.put(line.substring(0, j), Integer.valueOf(line.substring(i+1).replaceAll(" ", "")));
			    }
		} catch (IOException x) {
		    System.err.println(x);
		}
		System.out.println(m);
		return m;
	}
	
public static void main(String[] args) {
		Utils.generatedCounts(Dataset.DBPEDIA);//.writeManifestFiles();
//		System.out.println(ResultsFormat.FMT_RDF_TURTLE);
//		String[] a = {CONSTRUCT_QUERIES_FILE_EXT,"-data.xml","-result.xml"};
//		Utils.cleanDir(new File("/Users/thost/Desktop/git/2017/code/workspace_lsd/lsd/data/optional"), a);
	}

	

}
