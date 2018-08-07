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
import kodkod.ast.IntConstant;
import kodkod.ast.Relation;

public class LSDExpandEachDriver extends LSDExpanderBase {

	public LSDExpandEachDriver(String queryFile, boolean minimal) {
		super(queryFile, minimal);
	}

	public static void main(String[] args) throws Exception {
		main(args[0], (String s) -> { LSDExpandEachDriver exp = new LSDExpandEachDriver(s, Boolean.parseBoolean(args[1])); exp.mainLoop(exp.new EachPath()); });
	}
	
	class EachPath implements Process {
		public void process(Query ast, Op query, BasicUniverse U, JenaTranslator xlator)
				throws URISyntaxException, FileNotFoundException {
			Set<Pair<Formula, Pair<Formula, Formula>>> fs = xlator.translateSingle(Collections.<String,Object>emptyMap(), true);
			formulae: for(Pair<Formula, Pair<Formula, Formula>> p : fs) {
				//System.out.println("p: "+p);
				for(Relation r : ASTUtils.gatherRelations(p.fst)) {
					if (r.name().equals("solution")) {
						Formula thisf = p.fst.and(ensureSolutions(r));
						if ((Drivers.check(U, Pair.make(thisf, p.snd), "solution")) == null) {
							continue formulae;
						}
						checkExpanded(ast, query, U, thisf, p.snd.fst, p.snd.snd);
					}
				}
			}
		}
	}
}
