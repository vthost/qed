package qed.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
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

//helper class to generate statistics for paper
public class Statistics {
	
	public static void writeStatisticsFile(List<String> ids, List<int[]> stats, List<List<Feature>> features, String path) {
		try {		
			FileWriter writer = new FileWriter(path + File.separator + "stats_detail.txt");		  	
			writer.write("qid;qtriples;cqs;cqs-with-data;triples;features\n");
			
			for (int i = 0; i < ids.size(); i++) {
				try {
					int[] stat = stats.get(i);
					writer.write(ids.get(i) + ";" + stat[0] + ";" + stat[1] + ";" + stat[2] + ";" + stat[3] + ";" +
							String.join(",", features.get(i).stream().map(f -> f.name().replace("_", "\\_")).
									collect(Collectors.toList())) + "\n");

				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			writer.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	//set merge to true if there are statistics files in subdirectories to be merged into one 
	//(i.e., result of LSQ based data retrieval)
	//genstr is suffix of directory that contains the counts of generated data
	public static void finalizeStatistics(String path, int features, boolean merge, String genstr) { 
		if(merge) mergeStatisticsFiles(path);
		integrateGenerationStatistics(path, genstr);
		statsSummary(path, features);
	}

	public static void mergeStatisticsFiles(String path){
			try(FileWriter writer = new FileWriter(path + File.separator + "stats_detail.txt")) {		
					  	
				writer.write("qid;qtriples;cqs;cqs-with-data;triples;features\n");
				
	//			for each config directory
				for(File d: Utils.listDirectories(new File(path))) {
					InputStream in = Files.newInputStream(Paths.get(d + File.separator + "stats_detail.txt"));
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					String line = reader.readLine();
					while ((line = reader.readLine()) != null) 
						writer.write(line+'\n');
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}


	public static void integrateGenerationStatistics(String path, String genstr){
		try(FileWriter writer = new FileWriter(path + File.separator + "stats_detail+generated.txt")) {		
//				  	System.out.println(path.substring(path.lastIndexOf(File.separator)+1));
			Map<String,Integer> gcs = getGeneratedCounts(path+genstr);
//					generatedCounts(path.substring(path.lastIndexOf(File.separator)+1));

			writer.write("qid;qtriples;cqs;cqs-with-data;extriples;alltriples;features\n");
			
			InputStream in = Files.newInputStream(Paths.get(path + File.separator + "stats_detail.txt"));
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String id = line.substring(0,line.indexOf(";"));
	    			int i1 = line.lastIndexOf(";");
	    			int i2 = line.substring(0,i1).lastIndexOf(";");
	    			int c = (int) Double.parseDouble(line.substring(i2+1,i1));
	    			
	    			if (c == 0 && !gcs.containsKey(id) ||gcs.containsKey(id) &&gcs.get(id) == 0 )
	    			{
            			System.out.println(id);
            			continue;
        			} 
	    			
	    			writer.write(line.substring(0,i1+1));
        			if(gcs.containsKey(id)) {
        				writer.write(gcs.get(id) + line.substring(i1)+'\n');
        			} else {
        				writer.write(c + line.substring(i1)+'\n');
        			} 
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	//the maxFeats most common features are considered
	//and a summary is written out, based on numbers in path + "/stats_detail+generated.txt"
	public static void statsSummary(String path, int maxFeats) {
			Map<String,List<String>> fqs = new HashMap<String,List<String>>();
			Map<String,List<List<Integer>>> fss = new HashMap<String,List<List<Integer>>>();
			
			Map<String,Integer> ok = new HashMap<String,Integer>();
			for (Feature f : Feature.values()) {
				ok.put(f.name(),0);
			}
			try (InputStream in = Files.newInputStream(Paths.get(path + "/stats_detail+generated.txt"));
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
	
			System.out.println("Feature -> Nbr of Qs where it occurs:");
			System.out.println(ok);
			List<Integer> is = new ArrayList<Integer>();
			is.addAll(ok.values());
			List<String> ks = new ArrayList<String>();
			ks.addAll(ok.keySet());
			List<String> ks1 = new ArrayList<String>();
			for (int i = 0; i < maxFeats; i++) {
				int k = is.stream().mapToInt(j -> j).max().orElse(0);
				int index = is.indexOf(k);
	//			int e = 
				is.remove(index);
				ks1.add(ks.remove(index));
	//			System.out.println(e == k);
			}
			System.out.println("Considered Features:");
			System.out.println(ks1);
	
			try (InputStream in = Files.newInputStream(Paths.get(path + "/stats_detail+generated.txt"));
			    BufferedReader reader =
			      new BufferedReader(new InputStreamReader(in))) {
			    String line = reader.readLine();
			    while ((line = reader.readLine()) != null) {
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
				        	
				        	for (int i = 0; i < 5; i++) {
								stats.add(new ArrayList<Integer>());
							}
			        } else {
				        	ids = fqs.get(fs);
				        	stats = fss.get(fs);
			        }
			        
			        int j = line.indexOf(";");
		        		ids.add(line.substring(0, j));
		        	
		        	for (int i = 0; i < 5; i++) {
		        		int j2 = line.indexOf(";", j+1);
		        		int co = (int)Double.parseDouble(line.substring(j+1,j2));
		        			
						stats.get(i).add(co);
						j = j2;
					}
			    }
			} catch (IOException e) {
			    System.err.println(e);
			}
	//		System.out.println(fqs);
	//		System.out.println(fss);
			
			Map<String, List<List<Integer>>> treeMap = new TreeMap<>(
			                (Comparator<String>) (o1, o2) -> o1.length() < o2.length() ? -1 : o2.length() < o1.length() ? 1 : o1.compareTo(o2)
			        );
			
	        treeMap.putAll(fss);
	
	
			
			try {		
				FileWriter writer = new FileWriter(path + "/stats.txt");		  	
				writer.write("config& qs& qsize &cqs& cqs-with-data& extriples& alltriples\\\\hline\n");
				
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
							(int)vs.get(3).stream().mapToInt(i -> i).average().orElse(0)+"&"+ //extriples
							(int)vs.get(4).stream().mapToInt(i -> i).average().orElse(0)+ //alltriples
							"\\Tstrut\\Bstrut\\\\\\hline\n");
	
				}
				writer.close();
				
			} catch (IOException x) {
			    System.err.println(x);
			}
			
		}

	//get triple counts of generated data files (assuming they were computed before)
	public static Map<String,Integer> getGeneratedCounts(String path) {
		Map<String,Integer> m = new HashMap<String,Integer>();
		
		for(File f: new File(path).listFiles((dir,name) -> name.endsWith("-all-data.ttl.count"))) {
			try (Scanner s = new Scanner(f);) {
//				System.out.println(f.getName());
				
				int i = f.getName().indexOf("-all-data.ttl.count");
				i = i>0 ? i : f.getName().indexOf("-data.ttl.count");
				String id = f.getName().substring(0,i);
				id = id.substring(0,id.lastIndexOf("-"));

				m.put(id, Integer.valueOf(s.nextLine().strip())) ;

				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
				
		}	

		return m;
	}

	
	public static void main(String[] args) {
		
		Statistics.finalizeStatistics(Constants.DATA_DIR+"wikidata", 7, false, "-12-10");

	}
}
