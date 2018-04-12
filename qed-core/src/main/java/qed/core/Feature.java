package qed.core;

public class Feature {

//	LSQ features we can query for 
	public static String FEATURE_AVG ="Avg";
	public static String FEATURE_BIND ="Bind";
	public static String FEATURE_COUNT ="Count";
	public static String FEATURE_DISTINCT ="Distinct";
	public static String FEATURE_FILTER ="Filter";
	public static String FEATURE_FROM_NAMED ="FromNamed";
	public static String FEATURE_GROUP_BY ="GroupBy";
	public static String FEATURE_HAVING ="Having";
	public static String FEATURE_LIMIT ="Limit";
	public static String FEATURE_MAX ="Max";
	public static String FEATURE_MIN ="Min";
	public static String FEATURE_MINUS ="Minus";
	//<!-- typo in the current LSQ data ... should be NamedGraph, not namedGraph ... -->
	public static String FEATURE_NAMED_GRAPH ="NamedGraph";
	//rdfs:label="NamedGraph"><owl:sameAs rdf:resource="http://lsq.aksw.org/vocab#namedGraph" /></sd:Feature>
	public static String FEATURE_OFFSET ="Offset";
	public static String FEATURE_OPTIONAL ="Optional";
	public static String FEATURE_ORDER_BY ="OrderBy";
	public static String FEATURE_REGEX ="Regex";
	public static String FEATURE_SERVICE ="Service";
	public static String FEATURE_SUBQUERY ="SubQuery";
	public static String FEATURE_SUM ="Sum";
	public static String FEATURE_UNION ="Union";
	public static String FEATURE_VALUES ="Values";

//	one configuration specifies the SPARQL queries we look for
//	(we look for queries that contain all features from one array)
	public static String[][] FEATURE_CONFIG_SIMPLE = {
	{ FEATURE_AVG },
	{ FEATURE_BIND },
	{ FEATURE_COUNT },
	{ FEATURE_DISTINCT },
	{ FEATURE_FILTER},
	{ FEATURE_FROM_NAMED },
	{ FEATURE_GROUP_BY },
	{ FEATURE_HAVING },
	{ FEATURE_LIMIT },
	{ FEATURE_MAX },
	{ FEATURE_MIN },
	{ FEATURE_MINUS },
	{ FEATURE_NAMED_GRAPH },
	{ FEATURE_OFFSET },
	{ FEATURE_OPTIONAL },
	{ FEATURE_ORDER_BY },
	{ FEATURE_REGEX },
	{ FEATURE_SERVICE },
	{ FEATURE_SUBQUERY },
	{ FEATURE_SUM },
	{ FEATURE_UNION },
	{ FEATURE_VALUES }
	};
	public static String[][] FEATURE_CONFIG_EXAMPLE = {
	{ FEATURE_FILTER,FEATURE_ORDER_BY },
	{ FEATURE_FILTER,FEATURE_REGEX },
	{ FEATURE_UNION,FEATURE_DISTINCT },
	{ FEATURE_UNION,FEATURE_FILTER },
	{ FEATURE_UNION,FEATURE_ORDER_BY },
	{ FEATURE_UNION,FEATURE_REGEX },
	{ FEATURE_OPTIONAL,FEATURE_REGEX },
	{ FEATURE_OPTIONAL,FEATURE_LIMIT }
	};
	
	public static String[][] FEATURE_CONFIG_TEST_ONE = {
//			{ FEATURE_AVG },
//			{ FEATURE_BIND },
//			{ FEATURE_COUNT },
//			{ FEATURE_DISTINCT },
//			{ FEATURE_FILTER},
//			{ FEATURE_FROM_NAMED },
//			{ FEATURE_GROUP_BY },
//			{ FEATURE_HAVING },
//			{ FEATURE_LIMIT },
//			{ FEATURE_MAX },
//			{ FEATURE_MIN },
//			{ FEATURE_MINUS },
//			{ FEATURE_NAMED_GRAPH },
////			{ FEATURE_OFFSET },
//			{ FEATURE_OPTIONAL },
//			{ FEATURE_ORDER_BY },
//			{ FEATURE_REGEX },
//			{ FEATURE_SERVICE },
//			{ FEATURE_SUBQUERY },
//			{ FEATURE_SUM },
			{ FEATURE_UNION },
//			{ FEATURE_VALUES }
			};
}
