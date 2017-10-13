package sparql.synthesis.data;

import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.Element1;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementUnion;

public class ElementUtils {
	
//	find 1 in 2
//	(our method to copy element 1)
	public static Element findElement(Element e1, Element e2) {
		
		if(e1 == null || e2 == null) return null;
		
		if(e1.equals(e2)) return e2;
		
		if(e2 instanceof ElementGroup) {
			
			for (Element e3 : ((ElementGroup) e2).getElements()) {
				Element e4 = findElement(e1, e3);
				if(e4 != null) return e4;
			}
					
		} else if(e2 instanceof ElementOptional) {
			
			return findElement(e1, ((ElementOptional) e2).getOptionalElement());
			
		} else if(e2 instanceof ElementUnion) {
			
			for (Element e3 : ((ElementUnion) e2).getElements()) {
				Element e4 = findElement(e1, e3);
				if(e4 != null) return e4;
			}
		} else if(e2 instanceof Element1) {
			
			return findElement(e1, ((Element1) e2).getElement());
			
		} else if(e2 instanceof ElementMinus) {
			
			return findElement(e1, ((ElementMinus) e2).getMinusElement());
		
		} 
//		TODO look also into expressions
//		same for ElementAssign, ElementBind
//		else if(e2 instanceof ElementFilter) { 
//			
//			return findElement(e1, ((ElementFilter) e2).getExpr();
//		
//		} 
//		else System.out.println(e2.getClass());
//		can be ignored: ElementPathBlock, ElementData, ElementTriplesBlock, 
//		TODO cases currently ignored: ElementNamedGraph, ElementService, ElementSubQuery,
		
		return null;
	}
}
