package com.ibm.lsd.checker.drivers;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.ibm.research.rdf.store.sparql11.semantics.BasicUniverse;
import com.ibm.research.rdf.store.sparql11.semantics.JenaTranslator;

import qed.core.Constants;

public abstract class DriverBase implements Constants {

	@FunctionalInterface
	interface Process {
		void process(Query ast, Op query, BasicUniverse U, JenaTranslator xlator) throws URISyntaxException, FileNotFoundException;
	}

	@FunctionalInterface
	interface ThrowingConsumer<T, E extends Exception> {
		void call(T v) throws E;
	}

	public static void main(String ff, ThrowingConsumer<String,Exception> process) throws Exception {
		File f = new File(ff);
		System.err.println(f);
		if (f.exists()) {
			if (f.isDirectory()) {
				for(File rq : f.listFiles()) {
					main(rq.toString(), process);
				}
			} else if (f.getName().endsWith(QUERY_FILE_EXT)) {
				process.call(f.toURI().toString());
			}
		} else {
//			TODO what's this case for/
			process.call(ff);
		}
	}

	public DriverBase() {
		super();
	}

}