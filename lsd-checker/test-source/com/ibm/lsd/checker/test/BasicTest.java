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
	
	public void testExpandEach(String query) throws Exception {
		URL url = BasicTest.class.getClassLoader().getResource(query);
		assert url != null;
		
		LSDExpandEachDriver.main(new String[] {url.toString()});
	}

	@Test
	public void testExpandEach626806() throws Exception {
		testExpandEach("DBpedia-q626806.rq");
	}

	public void testExpandAll(String query) throws Exception {
		URL url = BasicTest.class.getClassLoader().getResource(query);
		assert url != null;
		
		LSDExpandAllDriver.main(new String[] {url.toString()});
	}

	@Test
	public void testExpandAll626806() throws Exception {
		testExpandAll("DBpedia-q626806.rq");
	}

}
