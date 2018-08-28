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
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Pair;

import kodkod.ast.Formula;
import kodkod.ast.Relation;
import kodkod.engine.satlab.SATFactory;
import kodkod.instance.Instance;

public class LSDExpandAllDriver extends LSDExpanderBase {

	public LSDExpandAllDriver(String queryFile, int solutionLimit, int datasetLimit, String dataDir) {
		super(queryFile, solutionLimit, datasetLimit, dataDir);
	}

	public static void main(String[] args) throws Exception {
		main(args[0], (String s) -> { 
			LSDExpandAllDriver exp = new LSDExpandAllDriver(s, Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3]); 
			AllPaths paths = exp.new AllPaths();
			exp.mainLoop(paths); });
	}
		
	class AllPaths implements Process {
		public void process(Query ast, Op query, BasicUniverse U, JenaTranslator xlator)
				throws URISyntaxException, FileNotFoundException {
			Formula f = Formula.TRUE;
			Formula s1 = Formula.TRUE;
			Formula s2 = Formula.TRUE;

			Set<Pair<Formula, Pair<Formula, Formula>>> fs = xlator.translateSingle(Collections.<String,Object>emptyMap(), true);

			Instance t = null;			
			for(Pair<Formula, Pair<Formula, Formula>> p : fs) {
				f = f.and(p.fst);
				s1 = p.snd.fst==null? s1: p.snd.fst.and(s1);
				s2 = p.snd.snd==null? s2: p.snd.snd.and(s2);
			}
			
			Set<Relation> relations = ASTUtils.gatherRelations(f);
			for(Relation q : relations) {
				if (q.name().equals("quads")) {		
					f = f.and(limitData(q));
				}
			}

			t = Drivers.check(U, SATFactory.MiniSat, Pair.make(f,  Pair.make(s1, s2)));
			if (t != null) {
				checkExpanded(ast, query, U, t, f, s1, s2);
			}
		}
	}
}
