package com.ibm.lsd.checker;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.resultset.ResultsFormat;

import com.ibm.research.rdf.store.sparql11.semantics.JenaUtil;

public class JenaRunner {
	
	public static void main(String[] args) throws MalformedURLException, IOException {
		Query ast = JenaUtil.parse(args[0]);
		Dataset dataset = RDFDataMgr.loadDataset(args[1]);
		QueryExecution exec = QueryExecutionFactory.create(ast, dataset);
		ResultSet results = exec.execSelect();
		ResultSetFormatter.output(
			System.out, 
			results, 
			ResultsFormat.FMT_RDF_TURTLE);

	}
}
