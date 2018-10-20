package com.ibm.lsd.checker.drivers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import com.ibm.research.rdf.store.sparql11.semantics.ASTUtils;
import com.ibm.research.rdf.store.sparql11.semantics.BasicUniverse;
import com.ibm.research.rdf.store.sparql11.semantics.Drivers;
import com.ibm.research.rdf.store.sparql11.semantics.JenaTranslator;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.Pair;

import kodkod.ast.Expression;
import kodkod.ast.Formula;
import kodkod.ast.Relation;
import kodkod.ast.Variable;
import kodkod.ast.visitor.AbstractReplacer;
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
				throws URISyntaxException, MalformedURLException, IOException {
			Formula f = Formula.TRUE;
			Formula s1 = Formula.TRUE;
			Formula s2 = Formula.TRUE;

			Set<Pair<Formula, Pair<Formula, Formula>>> fs = xlator.translateSingle(Collections.<String,Object>emptyMap(), true);

			int i = 0;
			Instance t = null;			
			for(Pair<Formula, Pair<Formula, Formula>> p : fs) {
				class Renamer extends AbstractReplacer {
					private Map<Variable,Variable> newNames = HashMapFactory.make();
					private final int i;
					
					Renamer(int i) {
						super(Collections.emptySet());
						this.i = i;
					}
					
					@Override
					public Expression visit(Variable arg0) {
						if (! newNames.containsKey(arg0)) {
							newNames.put(arg0, Variable.nary(arg0.name() + i, arg0.arity()));
						}

						return newNames.get(arg0);
					}
					
				};

				//f = f.and(p.fst.accept(new Renamer(i)));
				f = f.and(p.fst);
				s1 = p.snd.fst==null? s1: p.snd.fst.and(s1);
				s2 = p.snd.snd==null? s2: p.snd.snd.and(s2);
				
				i++;
			}
			
			Set<Relation> relations = ASTUtils.gatherRelations(f);
			for(Relation q : relations) {
				if (q.name().equals("quads")) {		
					f = f.and(limitData(q));
				}
			}

			if (! USE_EXISTENTIALS) {
				for(Relation s : relations) {
					if (s.name().equals("solution")) {		
						f = f.and(ensureSolutions(s));
					}
				}				
			}
			
			t = Drivers.check(U, SATFactory.MiniSat, Pair.make(f,  Pair.make(s1, s2)));
			if (t != null) {
				checkExpanded(ast, query, U, t, f, s1, s2);
			} else {
				System.err.println("unsatisfiable expansion");
			}
		}
	}
}
