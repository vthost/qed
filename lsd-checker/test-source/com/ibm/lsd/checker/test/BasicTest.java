package com.ibm.lsd.checker.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import com.ibm.lsd.checker.drivers.LSDCheckerDriver;
import com.ibm.lsd.checker.drivers.LSDExpandAllDriver;
import com.ibm.lsd.checker.drivers.LSDExpandEachDriver;

public class BasicTest {

	public void testQuery(String query) throws URISyntaxException, IOException {
		URL url = BasicTest.class.getClassLoader().getResource(query);
		assert url != null;
		
		LSDCheckerDriver.mainLoop(url.toString());
	}

	@Test
	public void testCheck626806() throws URISyntaxException, IOException {
		testQuery("DBpedia-q626806.rq");
	}
	
	public void testExpandEach(String query, boolean minimal) throws Exception {
		URL url = BasicTest.class.getClassLoader().getResource(query);
		assert url != null;
		
		LSDExpandEachDriver.main(new String[] {url.toString(), String.valueOf(minimal)});
	}

	@Test
	public void testExpandEach626806() throws Exception {
		testExpandEach("DBpedia-q626806.rq", true);
	}

	@Test
	public void testExpandEachV1() throws Exception {
		testExpandEach("veronika1.rq", true);
	}

	@Test
	public void testExpandEachFilter1() throws Exception {
		testExpandEach("filter1.rq", true);
	}

	@Test
	public void testExpandEach1733Minimal() throws Exception {
		testExpandEach("DBpedia-q1733.rq", true);
	}

	@Test
	public void testExpandEach1733() throws Exception {
		testExpandEach("DBpedia-q1733.rq", false);
	}

	@Test
	public void testExpandEach194196() throws Exception {
		testExpandEach("DBpedia-q194196.rq", true);
	}

	@Test
	public void testExpandEach759686() throws Exception {
		testExpandEach("DBpedia-759686.rq", true);
	}

	public void testExpandAll(String query) throws Exception {
		URL url = BasicTest.class.getClassLoader().getResource(query);
		assert url != null;
		
		LSDExpandAllDriver.main(new String[] {url.toString(), "false"});
	}

	@Test
	public void testExpandAll626806() throws Exception {
		testExpandAll("DBpedia-q626806.rq");
	}

}
