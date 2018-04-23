package com.ibm.lsd.checker.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import com.ibm.lsd.checker.drivers.LSDCheckerDriver;

public class BasicTest {

	public void testQuery(String query) throws URISyntaxException, IOException {
		URL url = BasicTest.class.getClassLoader().getResource(query);
		assert url != null;
		
		LSDCheckerDriver.mainLoop(url.toString());
	}

	@Test
	public void test626806() throws URISyntaxException, IOException {
		testQuery("DBpedia-q626806.rq");
	}
}
