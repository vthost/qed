package com.ibm.lsd.checker.drivers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFVisitor;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.resultset.ResultsFormat;

import com.ibm.research.rdf.store.sparql11.model.BlankNodeVariable;
import com.ibm.research.rdf.store.sparql11.model.Constant;
import com.ibm.research.rdf.store.sparql11.model.IRI;
import com.ibm.research.rdf.store.sparql11.model.QueryTripleTerm;
import com.ibm.research.rdf.store.sparql11.model.StringLiteral;
import com.ibm.research.rdf.store.sparql11.model.Variable;
import com.ibm.research.rdf.store.sparql11.semantics.BasicUniverse;
import com.ibm.research.rdf.store.sparql11.semantics.BoundedUniverse;
import com.ibm.research.rdf.store.sparql11.semantics.Drivers;
import com.ibm.research.rdf.store.sparql11.semantics.JenaTranslator;
import com.ibm.research.rdf.store.sparql11.semantics.JenaUtil;
import com.ibm.research.rdf.store.sparql11.semantics.QuadTableRelations;
import com.ibm.research.rdf.store.sparql11.semantics.SolutionRelation;
import com.ibm.research.rdf.store.utilities.io.SparqlSelectResult;
import com.ibm.research.rdf.store.utilities.io.SparqlSelectResult.Row;
import com.ibm.wala.util.collections.EmptyIterator;
import com.ibm.wala.util.collections.MapIterator;
import com.ibm.wala.util.collections.NonNullSingletonIterator;

import kodkod.ast.Formula;
import kodkod.ast.IntConstant;
import kodkod.ast.Relation;
import kodkod.instance.Instance;
import qed.core.Constants;

public abstract class LSDExpanderBase extends DriverBase {

	protected final static boolean VERBOSE = false;
			
	protected final static boolean USE_EXISTENTIALS = true;
	
	private final String queryFile;
	
	private int datasets = 0;
	
	protected final String originalDataset;
	
	protected int solutionLimit;
	protected int datasetLimit;
	
	private String dataDir;
	
	public LSDExpanderBase(String queryFile, int solutionLimit, int datasetLimit, String dataDir, String originalDataset) {
		super();
		this.queryFile = queryFile;
		this.solutionLimit = solutionLimit;
		this.datasetLimit = datasetLimit;
		this.originalDataset = originalDataset;
		this.dataDir = dataDir == null || new File(dataDir).isDirectory() ? dataDir : this.dataDir;
	}

//	TODO a check only occurs in the very beginning, no?
	@SuppressWarnings("unused")
	protected void checkExpanded(Query ast, Op query, BasicUniverse U, Instance t, Formula f, Formula s1, Formula s2)
			throws URISyntaxException, MalformedURLException, IOException {//System.out.println("check2: "+f);
			
		String originalDatasetDefault = queryFile.replace(Constants.QUERY_FILE_EXT,"") + Constants.DATA_FILE_EXT;
		Dataset dataset = originalDataset == null ? 
				new File(originalDatasetDefault).exists() ? RDFDataMgr.loadDataset(originalDatasetDefault) :  DatasetFactory.create()
						: RDFDataMgr.loadDataset(originalDataset);

		if (t != null) {
			JenaUtil.addTupleSet(dataset, t.tuples(QuadTableRelations.quads), U, t);
		}

		QueryExecution exec = QueryExecutionFactory.create(ast, dataset);
		ResultSet results = exec.execSelect();
		ResultSetFormatter.output(
			new FileOutputStream(dataDir + stem().substring(stem().lastIndexOf('/'))  + "-" + datasets + Constants.RESULT_FILE_EXT), 
			results, 
			ResultsFormat.FMT_RDF_TURTLE);

		RDFDataMgr.write(new FileOutputStream(
			dataDir + stem().substring(stem().lastIndexOf('/'))  + "-" + datasets++ + Constants.DATA_FILE_EXT), dataset, Lang.NQ);
	
		List<Row> jenaResult = new LinkedList<>();
		Iterator<Row> jenaRows = runQuery(ast, dataset).rows();
		while (jenaRows.hasNext()) {
			jenaResult.add(jenaRows.next());
		}
		
		if (VERBOSE && !jenaResult.isEmpty()) {
			System.err.println("results:");
			jenaResult.forEach((Row r) -> {
				r.variables().forEachRemaining((Variable v) -> {
					System.err.print(r.get(v) + " ");
				});
				System.err.println();
			});
		}
		
		Set<List<Object>> result = Drivers.tryToCheck(dataset, runQuery(ast, dataset), ast, ast.getProjectVars(), Collections.emptyMap(), "solution", false);
		
		if (result == null? jenaResult.size() != 0: result.size() != jenaResult.size()) {
			disagreement(jenaResult, result, ast, dataset, f);
		}
	}

	private void disagreement(List<Row> jenaResult, Set<List<Object>> result, Query ast, Dataset dataset, Formula f) {
		System.out.println("========================================");
		System.out.println("============= DISAGREEMENT =============");
		System.out.println("========================================");
		System.out.println(ast);
		System.out.println("----------------------------------------");
		System.out.println(f);
		System.out.println("----------------------------------------");
		RDFDataMgr.write(System.out, dataset, Lang.NQ);
		System.out.println("----------------------------------------");
		jenaResult.forEach((Row r) -> {
			r.variables().forEachRemaining((Variable v) -> {
				System.out.print(r.get(v) + " ");
			});
			System.out.println();
		});
		System.out.println("----------------------------------------");
		result.forEach((List<Object> row) -> { 
			System.out.println(row);
		});
		System.out.println("========================================");
	}

	private SparqlSelectResult runQuery(Query ast, Dataset dataset) {
		SparqlSelectResult rr = new SparqlSelectResult() {			
			@Override
			public Iterator<Row> rows() {
				QueryExecution exec = QueryExecutionFactory.create(ast, dataset);
				ResultSet results = exec.execSelect();
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
										if (l.getLanguage() != null && !"".equals(l.getLanguage())) {
											s.setLanguage(l.getLanguage());
										} else if (l.getDatatypeURI() != null) {
											s.setType(new IRI(l.getDatatypeURI()));
										}
										assert s.getLanguage() != null  || s.getType() != null;
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
				QueryExecution exec = QueryExecutionFactory.create(ast, dataset);
				ResultSet results = exec.execSelect();
				Iterator<String> vars = results.getResultVars().iterator();
				return new MapIterator<String,Variable>(vars, (String name) -> {
					return new Variable(name);
				});
			}
		};
		return rr;
	}

	public void mainLoop(Process p) {
		try {
		Query ast = JenaUtil.parse(queryFile);
		Op query = JenaUtil.compile(ast);

		BasicUniverse U = new BoundedUniverse();
//		try {
//			U = new OpenDatasetUniverse(new URL(stem + QUERY_DATA_FILE_EXT));
//		} catch (RiotNotFoundException e) {
//			U = new BoundedUniverse();
//		}

		SparqlSelectResult ask = new SparqlSelectResult() {
			@Override
			public Iterator<Row> rows() {
				return new NonNullSingletonIterator<>(new Row() {
					@Override
					public QueryTripleTerm get(Variable v) {
						assert false;
						return null;
					}
					@Override
					public Iterator<Variable> variables() {
						return EmptyIterator.instance();
					}					
				});
			}

			@Override
			public Iterator<Variable> variables() {
				return EmptyIterator.instance();
			}
		};
		
		JenaTranslator xlator = JenaTranslator.make(ast.getProjectVars(), Collections.singleton(query), U, USE_EXISTENTIALS? new SolutionRelation(ask, Collections.emptyList(), Collections.emptyNavigableMap()): null);

		p.process(ast, query, U, xlator);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	private String stem() {
		return queryFile.substring(0, queryFile.length()-QUERY_FILE_EXT.length());
	}

	protected Formula ensureSolutions(Relation r) {
		Formula some = r.count().gt(IntConstant.constant(0));
		return solutionLimit > 0? r.count().lte(IntConstant.constant(solutionLimit)).and(some): some;
	}

	protected Formula ensureSolutions(Relation r, Relation q) {
		return ensureSolutions(r).and(limitData(q));
	}

	protected Formula limitData(Relation q) {
		return q.count().lte(IntConstant.constant(datasetLimit));
	}

}
