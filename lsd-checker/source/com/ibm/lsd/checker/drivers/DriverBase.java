package com.ibm.lsd.checker.drivers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import com.ibm.research.rdf.store.sparql11.semantics.BasicUniverse;
import com.ibm.research.rdf.store.sparql11.semantics.JenaTranslator;

import qed.core.Constants;

public abstract class DriverBase implements Constants {

	@FunctionalInterface
	interface Process {
		void process(Query ast, Op query, BasicUniverse U, JenaTranslator xlator) throws URISyntaxException, FileNotFoundException, MalformedURLException, IOException;
	}

	@FunctionalInterface
	interface ThrowingConsumer<T, E extends Exception> {
		void call(T v) throws E;
	}

	private static void runit(ThrowingConsumer<String,Exception> process, String name) {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		final Future<?> future = executor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					process.call(name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		executor.shutdown(); // This does not cancel the already-scheduled task.

		try { 
		  future.get(2, TimeUnit.MINUTES); 
		}
		catch (InterruptedException ie) { 
		  /* Handle the interruption. Or ignore it. */ 
		}
		catch (ExecutionException ee) { 
		  /* Handle the error. Or ignore it. */ 
		}
		catch (TimeoutException te) { 
		  /* Handle the timeout. Or ignore it. */ 
		}
		if (!executor.isTerminated())
		    executor.shutdownNow();
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
				runit(process, f.toURI().toString());
			}
		} else {
//			TODO what's this case for/
			runit(process, ff);
		}
	}

	public DriverBase() {
		super();
	}

}