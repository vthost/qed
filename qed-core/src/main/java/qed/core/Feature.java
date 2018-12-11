/**
 * 
 */
package qed.core;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * features we consider
 * are those from LSQ
 */
public enum Feature {
	
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
	NAMED_GRAPH,
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

	
	public static Feature[][] getValuesAsConfig(){

		Feature[][] result = new Feature[Feature.values().length][1];//Arrays.asList(Feature.values()).stream().map(f -> {f}).toArray(String[]::new);
		int i = 0;
		for (Feature feature : Feature.values()) {
			result[i++][0] = feature;
		}
		
		return result;
	}

    public String toString1() {
    	String n = name();
        return n.charAt(0)+n.substring(1).toLowerCase();
    }
	
	public static String[] toStringArray(Feature[] config){

		return Arrays.asList(config).stream().map(Feature::toString1).toArray(String[]::new);		
	}
	
//	public static String[][] toStringArrays(Feature[][] configs){
//		
//		return Arrays.asList(configs).stream().map(c -> toStringArray(c)).toArray(String[][]::new);
//		
//	}


	// currently only returns true for features we want to consider 
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
			break; //query.matches("(?i).*\\slimit\\s+[0-9].*");
		case MAX:
			break;
		case MIN:
			break;
		case MINUS:
			return query.matches("(?i).*\\sminus\\s+\\{.*");
		case NAMED_GRAPH:
			break;
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
