package com.ibm.lsd.checker.drivers;

import java.io.File;
import java.io.FileFilter;
import java.net.URISyntaxException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.ibm.research.rdf.store.sparql11.semantics.BasicUniverse;
import com.ibm.research.rdf.store.sparql11.semantics.JenaTranslator;

public abstract class DriverBase {

	@FunctionalInterface
	interface Process {
		void process(Query ast, Op query, BasicUniverse U, JenaTranslator xlator) throws URISyntaxException;
	}

	@FunctionalInterface
	interface ThrowingConsumer<T, E extends Exception> {
		void call(T v) throws E;
	}

	public static void main(String ff, ThrowingConsumer<String,Exception> process) throws Exception {
		File f = new File(ff);
		if (f.exists()) {
			if (f.isDirectory()) {
				for(File rq : f.listFiles(new FileFilter() {
					@Override
					public boolean accept(File name) {
						return name.getName().endsWith(".rq");
					}
				})) {
					main(rq.toURI().toURL().toString(), process);
				}
			} else {
				process.call(f.toURI().toString());
			}
		} else {
			process.call(ff);
		}
	}

	public DriverBase() {
		super();
	}

}