package com.ibm.lsd.checker.drivers;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.ibm.research.rdf.store.sparql11.semantics.ASTUtils;
import com.ibm.research.rdf.store.sparql11.semantics.BasicUniverse;
import com.ibm.research.rdf.store.sparql11.semantics.Drivers;
import com.ibm.research.rdf.store.sparql11.semantics.JenaTranslator;
import com.ibm.wala.util.collections.Pair;

import kodkod.ast.Formula;
import kodkod.ast.Relation;
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
				throws URISyntaxException, FileNotFoundException {
			Set<Pair<Formula, Pair<Formula, Formula>>> fs = xlator.translateSingle(Collections.<String,Object>emptyMap(), true);
			formulae: for(Pair<Formula, Pair<Formula, Formula>> p : fs) {
				//System.out.println("p: "+p);
				Set<Relation> relations = ASTUtils.gatherRelations(p.fst);
				for(Relation r : relations) {
					if (r.name().equals("solution")) {		
						Formula thisf = null;
						if (datasetLimit > 0) {
							for(Relation q : relations) {
								if (q.name().equals("quads")) {		
									thisf = p.fst.and(ensureSolutions(r, q));
								}
							}
						} else {
							thisf = p.fst.and(ensureSolutions(r));
						}
						Instance bindings = Drivers.check(U, SATFactory.MiniSat, Pair.make(thisf, p.snd));
						if (bindings == null) {
							continue formulae;
						}
						checkExpanded(ast, query, U, bindings, thisf, p.snd.fst, p.snd.snd);
					}
				}
			}
		}
	}
}
