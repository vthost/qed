package com.ibm.lsd.checker.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import com.ibm.qed.checker.drivers.CheckerDriver;
import com.ibm.qed.checker.drivers.ExpandAllDriver;
import com.ibm.qed.checker.drivers.ExpandEachDriver;

public class BasicTest {

	public void testQuery(String query) throws URISyntaxException, IOException {
		URL url = BasicTest.class.getClassLoader().getResource(query);
		assert url != null;
		
		CheckerDriver.mainLoop(url.toString());
	}

    @Test
	public void testCheck626806() throws URISyntaxException, IOException {
		testQuery("DBpedia-q626806.rq");
	}
	
	public void testExpandEach(String query, int solutionLimit, int datasetLimit, String originalDataset) throws Exception {
		URL url = BasicTest.class.getClassLoader().getResource(query);
		assert url != null;

		URL ds = BasicTest.class.getClassLoader().getResource(originalDataset);
		assert ds != null;

		ExpandEachDriver.main(new String[] {url.toString(), String.valueOf(solutionLimit), String.valueOf(datasetLimit), "test-data/data-each/", ds.toString()});
	}

	public void testExpandEach(String query, int solutionLimit, int datasetLimit) throws Exception {
		URL url = BasicTest.class.getClassLoader().getResource(query);
		assert url != null;
		
		ExpandEachDriver.main(new String[] {url.toString(), String.valueOf(solutionLimit), String.valueOf(datasetLimit), "test-data/data-each/"});
	}
 
	@Test
	public void testExpandEach626806() throws Exception {
		testExpandEach("DBpedia-q626806.rq", 1, 5);
	}

	@Test
	public void testExpandEachV1() throws Exception {
		testExpandEach("veronika1.rq", 1, 5);
	}

	@Test
	public void testExpandEachFilter1() throws Exception {
		testExpandEach("filter1.rq", 1, 5);
	}

	@Test
	public void testExpandEach1733Minimal() throws Exception {
		testExpandEach("DBpedia-q1733.rq", 1, 5);
	}

	@Test
	public void testExpandEach1733() throws Exception {
		testExpandEach("DBpedia-q1733.rq", -1, -1);
	}

	@Test
	public void testExpandEach194196() throws Exception {
		testExpandEach("DBpedia-q194196.rq", 1, 5);
	}

	@Test
	public void testExpandEach759686() throws Exception {
		testExpandEach("DBpedia-759686.rq", 1, 5);
	}

	@Test
	public void testExpandEachPath() throws Exception {
		testExpandEach("path1.rq", 1, 5);
	}

	@Test
	public void testExpandEachPathDs() throws Exception {
		testExpandEach("path1.rq", 1, 5, "pathOrigData.ttl");
	}

	@Test
	public void testExpandEachMinus() throws Exception {
		testExpandEach("minus1.rq", 1, 5);
	}

	@Test
	public void testExpandAllMinus() throws Exception {
		testExpandAll("minus1.rq", 1, 10);
	}

	public void testExpandAll(String query, int solutionLimit, int datasetLimit) throws Exception {
		URL url = BasicTest.class.getClassLoader().getResource(query);
		assert url != null;
		
		ExpandAllDriver.main(new String[] {url.toString(), String.valueOf(solutionLimit), String.valueOf(datasetLimit), "test-data/data-all/"});
	}

	@Test
	public void testExpandAll626806() throws Exception {
		testExpandAll("DBpedia-q626806.rq", -1, 10);
	}

	@Test
	public void testExpandAllV1() throws Exception {
		testExpandAll("veronika1.rq", 1, 10);
	}

	@Test
	public void testExpandAllFilter1() throws Exception {
		testExpandAll("filter1.rq", 1, 10);
	}

	@Test
	public void testExpandAll1733Minimal() throws Exception {
		testExpandAll("DBpedia-q1733.rq", 1, 10);
	}

	@Test
	public void testExpandAll1733() throws Exception {
		testExpandAll("DBpedia-q1733.rq", -1, 10);
	}

	@Test
	public void testExpandAll194196() throws Exception {
		testExpandAll("DBpedia-q194196.rq", 1, 10);
	}

	@Test
	public void testExpandAll759686() throws Exception {
		testExpandAll("DBpedia-759686.rq", 1, 10);
	}

	@Test
	public void testExpandAllOptional1() throws Exception {
		testExpandAll("optional1.rq", 1, 10);
	}

	@Test
	public void testExpandEachOptional1() throws Exception {
		testExpandEach("optional1.rq", 1, 10);
	}
}
