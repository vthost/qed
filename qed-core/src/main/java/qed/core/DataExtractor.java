package qed.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.expr.E_Exists;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_NotEquals;
import org.apache.jena.sparql.expr.E_NotExists;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunctionOp;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.path.P_Alt;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.P_ZeroOrMore1;
import org.apache.jena.sparql.path.PathVisitorBase;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.Element1;
import org.apache.jena.sparql.syntax.ElementExists;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementNotExists;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCleanGroupsOfOne;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformSubst;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;


public class DataExtractor {
	
	private String BVAR = "QED_BVAR";
	private String NVAR = "QED_VAR";
	
	private int defaultDataLimit = 10;
	private int nextVarId = 0;
	private Map<Var,Var> renames = new HashMap<Var,Var>();

		
	private boolean isBlankVarNode(Node n) {
		if(n.isBlank())
			return true;
		
		if(n.isVariable() && ((Var)n).isBlankNodeVar())
			return true;
		
		return false;
//			
//			try {
//				Integer.parseInt(n.getName().substring(1));
//				return true;
//			} catch (Exception E) {}
//			return false;
	}

private Node getSecurePart(TriplePath t, int i) {
		if(i == 0) {
			if(isBlankVarNode(t.getSubject()))
				return Var.alloc(BVAR + (this.nextVarId++));
			else
				return t.getSubject();
		} else if(i == 1) {
			if(isBlankVarNode(t.getPredicate()))
				return Var.alloc(BVAR + (this.nextVarId++));
			else
				return t.getPredicate();
		} else {
			if(isBlankVarNode(t.getObject()))
				return Var.alloc(BVAR + (this.nextVarId++));
			else
				return t.getObject();
		}
		
	}

	private Element getVarRenamedCopy(Element e) {

	for (Var var : extractVars(e)) {
		if(!renames.containsKey(var))
		renames.put(var, Var.alloc(var.getName()+this.nextVarId++));
	}
	
	Element e1 = ElementTransformer.transform(e, new ElementTransformSubst(renames));
	
	return e1;
}

	private List<Var> extractVars(Element e) {
		List<Var> vs = new ArrayList<Var>();
		
		
		ElementWalker.walk(e,
			    // For each element...
			    new ElementVisitorBase() {
			        // ...when it's a block of triples...
			        public void visit(ElementPathBlock el) {
			            // ...go through all the triples...
			            Iterator<TriplePath> triples = el.patternElts();
			            while (triples.hasNext()) {
			            	Triple t = triples.next().asTriple();
			            	if(t == null) continue;
			            	Node[] ns = {t.getSubject(),t.getPredicate(),t.getObject()};
			            	for (Node n : ns) {
			            		if(n.isVariable())
			            			vs.add((Var) n);
							}
			            }
			        }
			        
			        public void visit(ElementTriplesBlock el) {
			            // ...go through all the triples...
			            Iterator<Triple> triples = el.patternElts();
			            while (triples.hasNext()) {
			            	Triple t = triples.next();
			            	Node[] ns = {t.getSubject(),t.getPredicate(),t.getObject()};
			            	for (Node n : ns) {
			            		if(n.isVariable())
			            			vs.add((Var) n);
							}
			            }
			        }
			    }
			);
		return vs;
	}

	//	jena sparql syntax Element that can be ignored:
//	ElementAssign, ElementBind: neither jena sparql syntax Elements nor bgps in expressions
//	(except in (not) exists, but that may only occur in filters - according to the spec)
//	ElementData: (looks as if it) represents rdf data, bindings of variables to nodes
//	maybe also the sparql values clause
	private List<Element> extractBGPs(Element e) {	

		if(e instanceof Element1) {
			
			return extractBGPs(((Element1) e).getElement());
			
		} else if(e instanceof ElementFilter && //one of E_Exists, E_NotExists
				((ElementFilter) e).getExpr() instanceof ExprFunctionOp) { 
			
			Element el = ((ExprFunctionOp) ((ElementFilter) e).getExpr()).getElement();//TODO adapt this
			if(el instanceof ElementGroup && ((ElementGroup)el).getElements().stream().
					map(el1 -> el1 instanceof ElementPathBlock || el1 instanceof ElementTriplesBlock? true:false).reduce(
						       true, (a, b) -> a && b))
				return new ArrayList<Element>();
			
			return extractBGPs(((ExprFunctionOp) ((ElementFilter) e).getExpr()).getElement());
		
		} else if(e instanceof ElementGroup) {
			
			return extractBGPs(((ElementGroup) e).getElements());
					
		} else if(e instanceof ElementMinus) {
			
			return extractBGPs(((ElementMinus) e).getMinusElement());
		
		} else if(e instanceof ElementNamedGraph) {
			
			return extractBGPs(((ElementNamedGraph) e).getElement());
			
		} else if(e instanceof ElementOptional) {
			
			return extractBGPs(((ElementOptional) e).getOptionalElement());
			
		} else if(e instanceof ElementPathBlock) {
			
			List<Element> l = new ArrayList<Element>();		
			l.add((Element) e);
			return l;
			
		} else if(e instanceof ElementService) {
			
			return extractBGPs(((ElementService) e).getElement());
					
		} else if(e instanceof ElementSubQuery) {
				
			return extractBGPs(((ElementSubQuery) e).getQuery().getQueryPattern());
						
		} else if(e instanceof ElementTriplesBlock) {
			
			List<Element> l = new ArrayList<Element>();		
			l.add((Element) e);
			return l;
			
		} else if(e instanceof ElementUnion) {
			
			return extractBGPs(((ElementUnion) e).getElements());			
		} 
		
		return new ArrayList<Element>();		
	}

	private List<Element> extractBGPs(List<Element> l) {		
		
		List<Element> l2 = new ArrayList<Element>();		
		for (Element e : l) l2.addAll(extractBGPs(e));
	
		return l2;
	}
	

	//	http://jvalentino.blogspot.de/2007/02/shortcut-to-calculating-power-set-using.html
	private <T> List<List<T>> powerset(Collection<T> list) {
		  List<List<T>> ps = new ArrayList<List<T>>();
		  ps.add(new ArrayList<T>());   // add the empty set
		 
		  // for every item in the original list
		  for (T item : list) {
		    List<List<T>> newPs = new ArrayList<List<T>>();
		 
		    for (List<T> subset : ps) {
		      // copy all of the current powerset's subsets
		      newPs.add(subset);
		 
		      // plus the subsets appended with the current item
		      List<T> newSubset = new ArrayList<T>(subset);
		      newSubset.add(item);
		      newPs.add(newSubset);
		    }
		 
		    // powerset is now powerset of list.subList(0, list.indexOf(item)+1)
		    ps = newPs;
		  }
		  return ps;
	}
	
	private <T> List<List<T>> oneElementPowerset(List<List<T>> list) {
		
		if(list.size() == 1) {
			return list.get(0).stream().map(e -> { 
				List<T>  l = new ArrayList<T>();
				l.add(e);
				return l;
			}).collect(Collectors.toList());	
		}
		
		List<T> first = list.get(0);
		list.remove(0);		
		list = oneElementPowerset(list); 

		List<List<T>> newlist = new ArrayList<List<T>>();
		
		for (T t : first) {
			for (List<T> l : list) {
				List<T> l2 = new ArrayList<T>();
				l2.addAll(l);
				l2.add(t);
				newlist.add(l2);
			}
		}

		return newlist;
	}
	
	
//	we ignore ElementDataset since it is unused by the parser (according to the JavaDoc)
	private List<Element> getInVariability(Element e, String qs) {	
		
//		not sure where this class, Element(Not)Exists, is actually used. 
//		in filter this is not possible given the type?!
//		TODO add variability?
		if(e instanceof ElementExists) {
			System.out.println("!!!!!!!!!___________________"+e);
			List<Element> els = getInVariability(
						(((ElementExists) e).getElement()),qs);
			els.stream().map(e1 -> new ElementExists(e1));
			return els;
		
		} else if(e instanceof ElementNotExists) {
			System.out.println("!!!!!!!!!___________________"+e);
			List<Element> els = getInVariability(
						(((ElementExists) e).getElement()),qs);
			els.stream().map(e1 -> new ElementNotExists(e1));
			return els;
	

		} else if(e instanceof ElementFilter) { 
			
			List<Element> els = new ArrayList<Element>();			
			
			//first, recursion
			if(((ElementFilter) e).getExpr() instanceof E_Exists) {
				
				List<Element> l1 = getInVariability(
						((ExprFunctionOp) ((ElementFilter) e).getExpr()).getElement(),qs);

				els.addAll(l1);
				
				els.addAll(l1.stream().
						map(e1 -> new ElementFilter(new E_NotExists(e1))).collect(Collectors.toList()));

				els.addAll(l1.stream().
						map(e1 -> getVarRenamedCopy(e1)).collect(Collectors.toList()));
				

				
			} else if(((ElementFilter) e).getExpr() instanceof E_NotExists) {
				
				List<Element> l1 = getInVariability(
						((ExprFunctionOp) ((ElementFilter) e).getExpr()).getElement(),qs);
				
				els.addAll(l1);
				
				els.addAll(l1.stream().
						map(e1 -> new ElementFilter(new E_NotExists(e1))).collect(Collectors.toList()));
				
				els.addAll(l1.stream().
						map(e1 -> getVarRenamedCopy(e1)).collect(Collectors.toList()));
				
				
			} else {
				els.add(e);
				els.add(new ElementFilter(new E_LogicalNot(((ElementFilter) e).getExpr())));
			}
			
			return els;
		
		} else if(e instanceof ElementGroup) {
			
			List<Element> els = new ArrayList<Element>();
			List<List<Element>> l1 = getInVariability(((ElementGroup) e).getElements(),qs);

			for (List<Element> els2 : oneElementPowerset(l1)) {

				ElementGroup e1 = new ElementGroup(); 
				e1.getElements().addAll(els2);

				els.add(e1);
			}

			return els;
		
		} else if(e instanceof ElementMinus) { 
			
			List<Element> l1 = getInVariability(((ElementMinus) e).getMinusElement(),qs);
			
			List<Element> els = new ArrayList<Element>();
			els.addAll(l1);
			els.addAll(l1.stream().
					map(e1 -> new ElementFilter(new E_NotExists(e1))).collect(Collectors.toList()));	
			els.addAll(l1.stream().
					map(e1 -> getVarRenamedCopy(e1)).collect(Collectors.toList()));	
			
			return els;
		
		}  else if(e instanceof ElementNamedGraph) {
			
			ElementNamedGraph e1 = (ElementNamedGraph) e;
			List<Element> els = getInVariability(e1.getElement(),qs);
			els.stream().map(e2 -> new ElementNamedGraph(e1.getGraphNameNode(), e2));
			return els;	
			
		} else if(e instanceof ElementOptional) {
			
			List<Element> l1 = getInVariability(((ElementOptional) e).getOptionalElement(),qs);

			List<Element> els = new ArrayList<Element>();
			els.addAll(l1);

			els.addAll(l1.stream().map(e1 -> new ElementOptional(getVarRenamedCopy(e1))).collect(Collectors.toList()));	
			
			//make trivial before  case (optional false) more secure for us only.. ie is ensure that it appears
			els.addAll(l1.stream().map(e1 -> new ElementFilter(new E_NotExists(e1))).collect(Collectors.toList()));	
			
			return els;
		
		} else if(e instanceof ElementService) {
			
			ElementService e1 = (ElementService) e;
			List<Element> els = getInVariability(e1.getElement(),qs);
			els.stream().map(e2 -> new ElementService(e1.getServiceNode(), e2, e1.getSilent()));
			return els;	
			
		} else if(e instanceof ElementSubQuery) {
			
			List<Element> els = getInVariability(((ElementSubQuery) e).getQuery().getQueryPattern(),qs);
			els.stream().map(e2 -> {
				Query q = ((ElementSubQuery) e).getQuery().cloneQuery(); 
				q.setQueryPattern(e2); 
				return q;});
			return els;	
			
		} else if(e instanceof ElementUnion) {

			List<Element> els = new ArrayList<Element>();
			List<List<Element>> l1 = getInVariability(((ElementUnion) e).getElements(),qs);
			//optimization
			if(l1.stream().allMatch(l2 -> l2.size() == 1)) {
				
				for (List<Element> l : powerset(((ElementUnion) e).getElements())) {
					
					if(l.size() == 0) continue;
					
					ElementGroup e1 = new ElementGroup();
					e1.getElements().addAll(l);
					els.add(e1);
				}			
				
			} else {
				List<List<Element>> processed = new ArrayList<List<Element>>();
				
				List<List<Element>> l2 = oneElementPowerset(l1);
				for (List<Element> els2 : l2) {
					
					for (List<Element> l : powerset(els2)) {
						
						if(l.size() == 0) continue;
						
						if(processed.contains(l)) continue;
						processed.add(l);
						
						ElementGroup e1 = new ElementGroup();
						e1.getElements().addAll(l);
						els.add(e1);
					}			
				
				}
			} 
			return els;
		
		} 
		else if(e instanceof ElementPathBlock) {
			
			ElementPathBlock e2 = new ElementPathBlock();
			
			
            Iterator<TriplePath> triples = ((ElementPathBlock) e).patternElts();
            while (triples.hasNext()) {
            	TriplePath tp = triples.next();
            	Triple t = tp.asTriple();
            	if(t != null) {
            		Boolean[] blanks  = {
            				isBlankVarNode(t.getSubject()), isBlankVarNode(t.getPredicate()), isBlankVarNode(t.getObject())};

            		if (Arrays.asList(blanks).stream().anyMatch(v -> v)) {
            			
                		e2.addTriple(Triple.create(
                				blanks[0]? Var.alloc(BVAR+t.getSubject().getName().substring(1)) : t.getSubject(), 
                						blanks[1]? Var.alloc(BVAR+t.getPredicate().getName().substring(1)) : t.getPredicate(),
                								blanks[2]? Var.alloc(BVAR+t.getObject().getName().substring(1)) : t.getObject()));
            			
            		} else {
            			e2.addTriple(t);
            		}
            		
            		continue;
            	}
            	
            	List<List<Node>> stack = new ArrayList<List<Node>>();
            	
            	tp.getPath().visit(new PathVisitorBase() {
            		
            		public void visit(P_Seq pathSeq) {

//            			left = true;
            			pathSeq.getLeft().visit(this);
            			List<Node> ns = stack.get(0);
            			stack.remove(0);

            			pathSeq.getRight().visit(this);
            			ns.addAll(stack.get(0));
            			stack.remove(0);
            			
            			stack.add(0,ns);
//            			System.out.println(pathSeq);
			        }
            		
            		//TODO FOR NOW ignore treat as one ie treat as zero
            		public void visit(P_ZeroOrMore1 node) {
               			//should be at stack 0
               			node.getSubPath().visit(this);
			        }
            		//TODO FOR NOW ignore ie treat as one
               		public void visit(P_OneOrMore1 node) {
               			//should be at stack 0
               			node.getSubPath().visit(this);
			        }
  
               		public void visit(P_Alt node) {
               			//should be at stack 0
               			node.getLeft().visit(this);
               		}
            		
            		public void visit(P_Link node) {
            			List<Node> ns = new ArrayList<Node>();
            			ns.add(node.getNode());
            			stack.add(0,ns);

			        }
            	});
            	
            	List<Node> nodes = stack.get(0);
            	
            	if(nodes.size() == 1)
            		e2.addTriple(Triple.create(getSecurePart(tp, 0), nodes.get(0), getSecurePart(tp, 2)));
            	else if(nodes.size() > 1){
            		e2.addTriple(Triple.create(getSecurePart(tp, 0), nodes.get(0), Var.alloc(NVAR + this.nextVarId++)));
            		for (int i = 1; i < nodes.size()-1; i++) {
            			e2.addTriple(Triple.create(Var.alloc(NVAR + (this.nextVarId-1)), nodes.get(i), Var.alloc(NVAR + this.nextVarId++)));
                    	
					}
            		e2.addTriple(Triple.create(Var.alloc(NVAR + (this.nextVarId-1)), nodes.get(nodes.size()-1), getSecurePart(tp, 2)));
            	}

            }
//        }
//        
//        public void visit(ElementTriplesBlock el) {
//            // ...go through all the triples...
//            Iterator<Triple> triples = el.patternElts();
//            while (triples.hasNext()) {
//            	Triple t = triples.next();
//            	Node[] ns = {t.getSubject(),t.getPredicate(),t.getObject()};
//            	for (Node n : ns) {
//            		if(n.isVariable())
//            			vs.add((Var) n);
//				}
//            }

			
			List<Element> l = new ArrayList<Element>();		
			l.add((Element) e2);
			return l;
			
		} else if(e instanceof ElementTriplesBlock) {
			
			List<Element> l = new ArrayList<Element>();		
			l.add((Element) e);
			return l;
			
		} 
		
//		jena sparql syntax Element that are treated correctly in default case:
//		ElementAssign, ElementBind: no jena sparql syntax Elements in expressions
//		(except in (not) exists, but that may only occur in filters - according to the spec)
//		ElementData: (looks as if it) represents rdf data, bindings of variables to nodes
//		maybe also the sparql values clause
//		ElementPathBlock, ElementTriplesBlock: bgps are no jena sparql syntax Elements
		
		List<Element> els = new ArrayList<Element>();
		els.add(e);		
		return els;	
		
	}
	
	private List<List<Element>> getInVariability(List<Element> elements, String queryString) {		
		
		List<List<Element>> els = new ArrayList<List<Element>>();		
		for (Element e : elements) 
			els.add(getInVariability(e, queryString));
	
		return els;
	}

	private List<Query> createConstructQueries(String selectOrAskQueryString, int datasetSizeMax) {
		
//		TODO check if it's a selectOrAsk ...

		Query q = QueryFactory.create(selectOrAskQueryString, Syntax.syntaxARQ);
		Element p = q.getQueryPattern();

		List<Query> cqs = new ArrayList<Query>();

		for (Element e : getInVariability(p,selectOrAskQueryString)) {

			
			if(e instanceof ElementGroup) {

				List<Expr> es =  new ArrayList<Expr>();
				
				List<Var> vs = extractVars(e);
				for (Var var : vs) {
					if (renames.containsKey(var) && vs.contains(renames.get(var))) {
						es.add(new E_NotEquals(NodeValue.makeNode(var),NodeValue.makeNode(renames.get(var))));
//								.makeString(var.getName()), NodeValue.makeString(renames.get(var).getName())));					
					}
				}
				
				
				if(es.size() == 1)
					((ElementGroup) e).getElements().add(new ElementFilter(es.get(0)));
				else if(es.size() > 0){
					Expr ex = es.get(0);
					for (int j = 1; j < es.size(); j++) {
						ex = new E_LogicalOr(ex, es.get(j));
					}
					((ElementGroup) e).getElements().add(new ElementFilter(ex));
				}			
				
				
			}
			
			Query cq = QueryFactory.make();
			cq.setQueryConstructType();
			cq.setBaseURI(q.getBaseURI());
			cq.setPrefixMapping(q.getPrefixMapping());	
//			the WHERE clause is the original one, 
//			but possibly adapted to make the dataset more variable
			cq.setQueryPattern(e);                              
			cq.setLimit(datasetSizeMax > 0 ? datasetSizeMax : defaultDataLimit);
			
//			create the construct part of the query
			List<String> l1 = new ArrayList<String>();
			for (Element b: extractBGPs(e)) { //TODO make method dependent on kind of SELECT clause
				l1.add(b.toString());
			}
			
			cq.setConstructTemplate(QueryFactory.createTemplate("{"+ String.join(".", l1) +"}"));
			
			ElementTransform transform = new ElementTransformCleanGroupsOfOne();
			Element el2 = ElementTransformer.transform(cq.getQueryPattern(), transform);
			cq.setQueryPattern(el2);     
			
			cqs.add(cq);
			
//			if (cqs.size() > 10) {
//				break;
//			}
		}
				
//		TODO We currently only consider BGPs in the WHERE clause
//		TODO What if subgraphs, data sets etc. occur in the SELECT clause?

		return cqs;		
	}

	public void extractAllQueryDataAndResults(String logEndpoint, int datasetSizeMax, String directory) {
		File[] dirs = Utils.listDirectories(new File(directory));

//		for each config directory
		for(File d: dirs) {
			extractQueryDataAndResults(logEndpoint, datasetSizeMax, d.getAbsolutePath());
		}
	}
	
	public void extractQueryDataAndResults(String logEndpoint, int datasetSizeMax, String directory) {
//		PREPARATION -------------------------------------------------------------------------------------				
		File d = new File(directory);
		
		String[] todelete = { Utils.CONSTRUCT_QUERIES_FILE_EXT, 
				Utils.DATA_FILE_EXT, Utils.RESULT_FILE_EXT};		
		
		Utils.cleanDir(d, todelete);
//	   -------------------------------------------------------------------------------------			
	
		List<String[]> lqs = new ArrayList<String[]>();
		List<String> ids = new ArrayList<String>();
		List<int[]> stats = new ArrayList<int[]>();
		List<List<Feature>> fs = new ArrayList<List<Feature>>();
		
		try { 		
			
			for(File f: d.listFiles((dir,name) -> name.endsWith(Utils.QUERY_FILE_EXT))) {
				lqs.add(Utils.readQueryFile(f));
			}	
			
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.out.println(lqs.size());	

//	DATA -------------------------------------------------------------------------------------		
		for (String[] lq: lqs) {
			
			int cqsWithData = 0; 
			int triplesTotal = 0;
			
			String qid = lq[0]; System.out.println(qid);
			String q = lq[1];	
			fs.add(Feature.extractFeatures(q));
			
			List<Query> cqs = new ArrayList<Query>();
			try {
				cqs = createConstructQueries(q, datasetSizeMax);
			} catch (Exception e) {
				System.out.println("Exception (cqs): "+ qid);
			} 
		
//				uncomment the following line to get a file with all the cqs
			if(cqs.size()>0)
				Utils.writeConstructQueriesFile(d,qid,cqs);
			int i = 0;
			for (Query cq : cqs) {
				try (QueryEngineHTTP qe = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(logEndpoint, cq)) {		
										
		            qe.setTimeout(100000);
		            Model m = qe.execConstruct();


		            if(m.listStatements().hasNext()) {
		            		cqsWithData++;
		            		triplesTotal = Utils.writeDataFile(d,qid, m);
		            } else System.out.println("NO DATA: "+ i);
		            i++;
		             
		        } catch (Exception e) {
		        	System.out.println("Exception (http-cq): "+ cqs.indexOf(cq));

		        }
				if(i >100) break;
			}
					
			if(cqsWithData == 0) { 	
	            	System.out.println("NO DATA: "+ qid);
			}
			
//			RESULTS 1 -------------------------------------------------------------------------------------					
//			USE code like this if you want to generate result sets using Jena (ie instead of with qed-gen)
//			
//			InputStream in = null; Model m;
//			try {
//				
//				in = new FileInputStream(new File(d.getPath() + File.separator + Utils.getQueryDataFileName(qid)));
//				 
//				// Create an empty in-memory model and populate it from the data
//				m = ModelFactory.createMemModelMaker().createModel(qid+"");
//				m.read(in,null,"TURTLE"); // null base URI, since model URIs are absolute
//				
//				in.close();
//
//			} catch (Exception e) {
//				e.printStackTrace();
//				continue;
//			} 
//
//			System.out.println(q);
			Query query = null;
			try {
				query = QueryFactory.create(q);
			} catch(Exception e) {
				e.printStackTrace();
				System.out.println(q);
				continue;
			}
		
////				TODO	we sometimes cannot test this given our restricted dataset size
////				fix maybe reset to smaller value
//			query.setOffset(0); 
//
		
//			RESULTS 2
//			
//			QueryExecution qe1 = QueryExecutionFactory.create(query, m);
//			ResultSet rs = qe1.execSelect();
////			int rss = rs.getRowNumber();
//			if(!rs.hasNext()) {			
//				System.out.println("NO RESULT "+ qid);
////			            	System.out.println(q);
//            	
//            	//delete files
////				(new File(d.getPath() + File.separator + Utils.getQueryFileName(qid))).delete();
////				(new File(d.getPath() + File.separator + Utils.getQueryDataFileName(qid))).delete();
//            } else {
//            	Utils.writeQueryResultFile(d, qid, rs);
//            }
//
//			qe1.close();
			
			int qtriples = 0;
			List<Element> es = extractBGPs(query.getQueryPattern());
			for (Element e : es) {
				if(e instanceof ElementPathBlock) {
					Iterator<TriplePath> it = ((ElementPathBlock) e).patternElts();
					while(it.hasNext()) {
						it.next();
						qtriples++;
					}
				} else if(e instanceof ElementTriplesBlock) {
					Iterator<Triple> it = ((ElementTriplesBlock) e).patternElts();
					while(it.hasNext()) {
						it.next();
						qtriples++;
					}
				}
			}
			
			
			System.out.println(qid + ": cq nbr/cqs with data/total data: "+
			cqs.size()+ "/" +cqsWithData+"/"+ triplesTotal );	
			ids.add(qid);
			int[] ns = {qtriples,cqs.size(), cqsWithData, triplesTotal};
			stats.add(ns);
		}
		
		Statistics.writeStatisticsFile(ids, stats, fs, directory);
	}

		
	public static void main(String[] args) {
		
		LogFileDataset wd = new LogFileDataset("wikidata", Constants.DATA_DIR+"wikidata.txt", "https://query.wikidata.org/bigdata/namespace/wdq/sparql", "Wikidata-");

		DataExtractor de = new DataExtractor();
		de.extractQueryDataAndResults(wd.getEndpoint(),2, Constants.DATA_DIR+"test");
//		"http://localhost:8080/sparql", 2, Constants.DATA_DIR+"dbpedia2");

	}

}
