package com.ibm.lsd.checker.drivers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.ibm.research.rdf.store.sparql11.model.BlankNodeVariable;
import com.ibm.research.rdf.store.sparql11.model.Constant;
import com.ibm.research.rdf.store.sparql11.model.IRI;
import com.ibm.research.rdf.store.sparql11.model.QueryTripleTerm;
import com.ibm.research.rdf.store.sparql11.model.StringLiteral;
import com.ibm.research.rdf.store.sparql11.model.Variable;
import com.ibm.research.rdf.store.sparql11.semantics.BasicUniverse;
import com.ibm.research.rdf.store.sparql11.semantics.DatasetUniverse;
import com.ibm.research.rdf.store.sparql11.semantics.Drivers;
import com.ibm.research.rdf.store.sparql11.semantics.JenaTranslator;
import com.ibm.research.rdf.store.sparql11.semantics.JenaUtil;
import com.ibm.research.rdf.store.sparql11.semantics.OpenDatasetUniverse;
import com.ibm.research.rdf.store.sparql11.semantics.SolutionRelation;
import com.ibm.research.rdf.store.utilities.io.SparqlSelectResult;
import com.ibm.wala.util.collections.MapIterator;
import com.ibm.wala.util.collections.Pair;

import kodkod.ast.Formula;
import kodkod.instance.TupleSet;

public abstract class LSDExpanderBase extends DriverBase {

	protected static void checkExpanded(Query ast, Op query, BasicUniverse U, Formula f, Formula s1, Formula s2)
			throws URISyntaxException {
		SolutionRelation s;
		JenaTranslator xlator;
		Pair<Formula, Pair<Formula, Formula>> xlation;
		TupleSet t = Drivers.check(U, Pair.make(f,  Pair.make(s1, s2)), "quads");
		Dataset dataset = DatasetFactory.createMem();
		Graph G = dataset.asDatasetGraph().getDefaultGraph();
		if (t != null) {
			JenaUtil.addTupleSet(G, t);
		}

		QueryExecution exec = QueryExecutionFactory.create(ast, dataset);
		SparqlSelectResult rr = new SparqlSelectResult() {
			private final ResultSet results = exec.execSelect();

			@Override
			public Iterator<Row> rows() {
				return new Iterator<Row>() {
					@Override
					public boolean hasNext() {
						return results.hasNext();
					}

					@Override
					public Row next() {
						return new Row() {
							private final QuerySolution row = results.next();

							@Override
							public QueryTripleTerm get(Variable v) {
								RDFNode val = row.get(v.getName());
								if (val == null) return null;
								else return (QueryTripleTerm) val.visitWith(new RDFVisitor() {
									@Override
									public Object visitBlank(Resource r, AnonId id) {
										return new QueryTripleTerm(new BlankNodeVariable(id.getLabelString()));
									}
									@Override
									public Object visitURI(Resource r, String uri) {
										return new QueryTripleTerm(new IRI(uri));									}
									@Override
									public Object visitLiteral(Literal l) {
										StringLiteral s = new StringLiteral(l.getLexicalForm());
										if (l.getLanguage() != null) {
											s.setLanguage(l.getLanguage());
										}
										if (l.getDatatypeURI() != null) {
											s.setType(new IRI(l.getDatatypeURI()));
										}
										return new QueryTripleTerm(new Constant(s));
									}
								});
							}

							@Override
							public Iterator<Variable> variables() {
								return new MapIterator<String,Variable>(row.varNames(), (String name) -> {
									return new Variable(name);
								});
							}
						};
					}
				};
			}

			@Override
			public Iterator<Variable> variables() {
				Iterator<String> vars = results.getResultVars().iterator();
				return new MapIterator<String,Variable>(vars, (String name) -> {
					return new Variable(name);
				});
			}
		};

		U = new DatasetUniverse(dataset);
		s = new SolutionRelation(rr, ast.getProjectVars(), Collections.<String,Object>emptyMap());
		s.init(U);
		xlator = JenaTranslator.make(ast.getProjectVars(), Collections.singleton(query), U, s);
		xlation = xlator.translateSingle(Collections.<String,Object>emptyMap(), false).iterator().next();
		
		
		System.out.println("\n\nthe solution:");
		System.out.println(Drivers.check(U, xlation, "solution"));
		System.out.println("the dataset:");
		RDFDataMgr.write(System.out, dataset, Lang.NQ);
		System.out.println("\n\n");
	}

	public void mainLoop(String queryFile, Process p) throws URISyntaxException, IOException {
		Query ast = JenaUtil.parse(queryFile);
		Op query = JenaUtil.compile(ast);

		String stem = queryFile.substring(0, queryFile.length()-3);

		BasicUniverse U = new OpenDatasetUniverse(new URL(stem + "-data.ttl"));
		JenaTranslator xlator = JenaTranslator.make(ast.getProjectVars(), Collections.singleton(query), U, null);

		p.process(ast, query, U, xlator);
	}

	public LSDExpanderBase() {
		super();
	}

}