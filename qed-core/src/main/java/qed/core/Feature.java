/**
 * 
 */
package qed.core;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author veronika.thost@ibm.com
 *
 */
public enum Feature {
//	LSQ features we can query for 
	AVG,
	BIND,
	COUNT,
	DISTINCT,
	FILTER,
	FROM_NAMED,
	GROUP_BY,
	HAVING,
	LIMIT,
	MAX,
	MIN,
	MINUS,
	//<!-- typo in the current LSQ data ... should be NamedGraph, not namedGraph ... -->
	NAMED_GRAPH,
	//rdfs:label="NamedGraph"><owl:sameAs rdf:resource="http://lsq.aksw.org/vocab#namedGraph" /></sd:Feature>
	OFFSET,
	OPTIONAL,
	ORDER_BY,
	REGEX,
	SERVICE,
	SUBQUERY,
	SUM,
	UNION,
	VALUES;
//	in addition (not in LSQ)
//	PROPERTY_PATH;
	
	

    public String toString() {
    		String n = name();
        return n.charAt(0)+n.substring(1).toLowerCase();
    }
	
	
	public static Feature[][] getValuesAsConfig(){

		Feature[][] result = new Feature[Feature.values().length][1];//Arrays.asList(Feature.values()).stream().map(f -> {f}).toArray(String[]::new);
		int i = 0;
		for (Feature feature : Feature.values()) {
			result[i++][0] = feature;
		}
		
		return result;
	}
	
	public static String[] toStringArray(Feature[] config){

		return Arrays.asList(config).stream().map(Feature::toString).toArray(String[]::new);		
	}
	
//	public static String[][] toStringArrays(Feature[][] configs){
//		
//		return Arrays.asList(configs).stream().map(c -> toStringArray(c)).toArray(String[][]::new);
//		
//	}


	public static boolean containsFeature(String query, Feature feature) {
		switch(feature) {
		case AVG:
			break;
		case BIND:
			return query.matches("(?i).*\\sbind\\s*\\(.*");
		case COUNT:
			break;
		case DISTINCT:
			break;
		case FILTER:
			return query.matches("(?i).*\\s(filter not exists|filter\\s*(langmatches\\s*)?\\().*");
		case FROM_NAMED:
			break;
		case GROUP_BY:
			return query.matches("(?i).*\\sgroup\\s+by.*");
		case HAVING:
			return query.matches("(?i).*\\shaving\\s*\\(.*");
		case LIMIT:
			return query.matches("(?i).*\\slimit\\s+[0-9].*");
		case MAX:
			break;
		case MIN:
			break;
		case MINUS:
			return query.matches("(?i).*\\sminus\\s+\\{.*");
			//<!-- typo in the current LSQ data ... should be NamedGraph:
			//not namedGraph ... -->
		case NAMED_GRAPH:
			break;
			//rdfs:label="NamedGraph"><owl:sameAs rdf:resource="http://lsq.aksw.org/vocab#namedGraph" /></sd:Feature>
		case OFFSET:
			return query.matches("(?i).*(\\s|\\})offset\\s+[0-9].*");
		case OPTIONAL:
			return query.matches("(?i).*\\soptional\\s*\\{.*");
		case ORDER_BY:
			return query.matches("(?i).*\\sorder by\\s+(asc|desc|\\?).*");
		case REGEX:
			return query.matches("(?i).*\\sregex\\s*\\(.*");
		case SERVICE:
			break;
		case SUBQUERY:
			return query.matches("(?i).*\\sselect\\s.*select\\s.*");
		case SUM:
			break;
		case UNION:
			return query.matches("(?i).*\\sunion\\s+\\{.*");
		case VALUES:
			return query.matches("(?i).*\\svalues\\s+(\\{|\\?|\\().*");
//				in addition
//		case PROPERTY_PATH:
//			break;
		default:
			break;
		}
		return false;
	}
	
	public static List<Feature> extractFeatures(String query){
		return Arrays.asList(Feature.values()).stream().filter(f -> Feature.containsFeature(query, f)).collect(Collectors.toList());
	}
}
