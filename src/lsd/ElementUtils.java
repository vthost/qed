package lsd;

import java.util.List;

import org.apache.jena.sparql.expr.ExprFunctionOp;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.Element1;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementUnion;

public class ElementUtils {
	
//	find e1 in e2
//	(our method to copy jena sparql syntax Element e1, 
//	because Element neither can be cloned nor is serializable)
//	
//	kinds of elements:
//	Element1 (ElementDataset - Unused in parser???, ElementExists, ElementNotExists), 
//	ElementAssign, ElementBind, ElementData, ElementFilter, ElementGroup, ElementMinus, 
//	ElementNamedGraph, ElementOptional, ElementPathBlock, ElementService, ElementSubQuery, 
//	ElementTriplesBlock, ElementUnion
//	
//	can be ignored:
//	ElementAssign, ElementBind: no jena sparql syntax Elements in expressions
//	(except in (not) exists, but that may only occur in filters - according to the spec)
//	ElementData: (looks as if it) represents rdf data, bindings of variables to nodes
//	maybe also the sparql values clause
//	ElementPathBlock, ElementTriplesBlock: bgps are no jena sparql syntax Elements
//	TODO we currently (erroneously?) ignore: 
//	ElementNamedGraph
	public static Element findElement(Element e1, Element e2) {
				
		if(e1 == null || e2 == null) return null;
		
		if(e1.equals(e2)) return e2;
		
		
		if(e2 instanceof Element1) {
			
			return findElement(e1, ((Element1) e2).getElement());
			
		} else if(e2 instanceof ElementFilter && //one of E_Exists, E_NotExists
				((ElementFilter) e2).getExpr() instanceof ExprFunctionOp) { 

			return findElement(e1, ((ExprFunctionOp) ((ElementFilter) e2).getExpr()).getElement());
		
		} else if(e2 instanceof ElementGroup) {
			
			return findElement(e1, ((ElementGroup) e2).getElements());
					
		} else if(e2 instanceof ElementMinus) {
			
			return findElement(e1, ((ElementMinus) e2).getMinusElement());
		
		} else if(e2 instanceof ElementOptional) {
			
			return findElement(e1, ((ElementOptional) e2).getOptionalElement());
			
		} else if(e2 instanceof ElementService) {
			
			return findElement(e1, ((ElementService) e2).getElement());
					
		} else if(e2 instanceof ElementSubQuery) {
			
			return findElement(e1, ((ElementSubQuery) e2).getQuery().getQueryPattern());
					
		} else if(e2 instanceof ElementUnion) {
			
			return findElement(e1, ((ElementUnion) e2).getElements());
			
		} // else System.out.println(e2.getClass());

		return null;
	}
	
	private static Element findElement(Element e, List<Element> els) {
		
		for (Element e2 : els) {
			Element e3 = findElement(e, e2);
			if(e3 != null) return e3;
		}
		
		return null;
	}
}
