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
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Pair;

import kodkod.ast.Formula;
import kodkod.ast.Relation;
import kodkod.instance.TupleSet;

public class LSDExpandAllDriver extends LSDExpanderBase {

	public static void main(String[] args) throws Exception {
		main(args[0], (String s) -> { new LSDExpandAllDriver().mainLoop(s, new AllPaths()); });
	}
		
	static class AllPaths implements Process {
		public void process(Query ast, Op query, BasicUniverse U, JenaTranslator xlator)
				throws URISyntaxException {
			Formula f = Formula.TRUE;
			Formula s1 = null;
			Formula s2 = null;

			Set<Pair<Formula, Pair<Formula, Formula>>> fs = xlator.translateSingle(Collections.<String,Object>emptyMap(), true);

			Set<Relation> prs = HashSetFactory.make();
			formulae: for(Pair<Formula, Pair<Formula, Formula>> p : fs) {
				for(Relation r : ASTUtils.gatherRelations(p.fst)) {
					if (r.name().equals("solution")) {

						Formula thisf = p.fst.and(r.some());
						for(Relation pr : prs) {
							if (r.arity() == pr.arity()) {
								thisf = thisf.and(pr.eq(r).not());
							}
						}

						prs.add(r);

						TupleSet x;
						if ((x = Drivers.check(U, Pair.make(thisf, p.snd), "solution")) == null) {
							prs.clear();
							continue formulae;
						}

						System.err.println("tuples:" + x);
						
						Formula nextf = f.and(thisf);
						Formula nexts1 = s1==null? p.snd.fst: p.snd.fst==null? s1: p.snd.fst.and(s1);
						Formula nexts2 = s2==null? p.snd.snd: p.snd.snd==null? s2: p.snd.snd.and(s2);

						TupleSet t = Drivers.check(U, Pair.make(nextf,  Pair.make(nexts1, nexts2)), "quads");
						if (t == null) {
							checkExpanded(ast, query, U, f, s1, s2);
							f = thisf;
							s1 = p.snd.fst;
							s2 = p.snd.snd;
						} else {
							f = nextf;
							s1 = nexts1;
							s2 = nexts2;
						}

						break;
					}
				}
			}

			checkExpanded(ast, query, U, f, s1, s2);
		}
	}
}
