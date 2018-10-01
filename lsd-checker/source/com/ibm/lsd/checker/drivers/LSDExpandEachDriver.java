package com.ibm.lsd.checker.drivers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import com.ibm.research.rdf.store.sparql11.semantics.ASTUtils;
import com.ibm.research.rdf.store.sparql11.semantics.BasicUniverse;
import com.ibm.research.rdf.store.sparql11.semantics.Drivers;
import com.ibm.research.rdf.store.sparql11.semantics.JenaTranslator;
import com.ibm.wala.util.collections.Pair;

import kodkod.ast.Formula;
import kodkod.ast.Relation;
import kodkod.engine.CapacityExceededException;
import kodkod.engine.satlab.SATFactory;
import kodkod.instance.Instance;

public class LSDExpandEachDriver extends LSDExpanderBase {

	public LSDExpandEachDriver(String queryFile, int solutionLimit, int datasetLimit, String dataDir) {
		super(queryFile, solutionLimit, datasetLimit, dataDir);
	}

	public static void main(String[] args) throws Exception {
		main(args[0], (String s) -> { LSDExpandEachDriver exp = new LSDExpandEachDriver(s, Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3]); exp.mainLoop(exp.new EachPath()); });
	}
	
	class EachPath implements Process {
		public void process(Query ast, Op query, BasicUniverse U, JenaTranslator xlator)
				throws URISyntaxException, MalformedURLException, IOException {
			Set<Pair<Formula, Pair<Formula, Formula>>> fs = xlator.translateSingle(Collections.<String,Object>emptyMap(), true);
			formulae: for(Pair<Formula, Pair<Formula, Formula>> p : fs) {
				//System.out.println("p: "+p);
				Formula thisf = p.fst;
				
				Set<Relation> relations = ASTUtils.gatherRelations(p.fst);
				
				if (! USE_EXISTENTIALS) {
					for(Relation r : relations) {
						if (r.name().equals("solution")) {		
							thisf = thisf.and(ensureSolutions(r));
						}
					}
				}
				
				if (datasetLimit > 0) {
					for(Relation q : relations) {
						if (q.name().equals("quads")) {		
							thisf = p.fst.and(limitData(q));
						}
					}
				}
				
				Instance bindings = Drivers.check(U, SATFactory.MiniSat, Pair.make(thisf, p.snd));
				if (bindings == null) {
					continue formulae;
				}
				
				try {
					checkExpanded(ast, query, U, bindings, thisf, p.snd.fst, p.snd.snd);
				} catch (CapacityExceededException e) {
					
				}
			}
		}
	}
}
