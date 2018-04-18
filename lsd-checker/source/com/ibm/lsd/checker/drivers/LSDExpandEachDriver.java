package com.ibm.lsd.checker.drivers;

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

public class LSDExpandEachDriver extends LSDExpanderBase {

	public static void main(String[] args) throws Exception {
		main(args[0], (String s) -> { new LSDExpandEachDriver().mainLoop(s, new EachPath()); });
	}
	
	static class EachPath implements Process {
		public void process(Query ast, Op query, BasicUniverse U, JenaTranslator xlator)
				throws URISyntaxException {
			Set<Pair<Formula, Pair<Formula, Formula>>> fs = xlator.translateSingle(Collections.<String,Object>emptyMap(), true);
			formulae: for(Pair<Formula, Pair<Formula, Formula>> p : fs) {
				for(Relation r : ASTUtils.gatherRelations(p.fst)) {
					if (r.name().equals("solution")) {
						Formula thisf = p.fst.and(r.some());
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