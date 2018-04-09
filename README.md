# SPARQL QED

SPAQRL QED is a system generating out-of-the-box datasets for SPARQL queries over linked data. It distinguishes the queries according to the different SPARQL features and creates, for each query, a small but exhaustive dataset comprising linked data and the query answers over this data. We ensure that the created datasets are diverse and cover various practical use cases and, of course, that the sets of answers included are the correct ones.

For a more detailed introduction into the approach and a coarse overview of the system see qed/paper.pdf

You can #RUN the application using the qed/qed.jar file: java -jar qed.jar 

The #DEFAULT CONFIGURATION uses the following parameters:
- selected features: 
QED creates test cases for each feature set in 
FEATURE_CONFIG_SIMPLE = {
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
	}.
- query log: DBpedia provided by the LSQ endpoint http://lsq.aksw.org/sparql
- data: assumed to be hosted somewhere by you and available at http://localhost:8080/sparql
- maximal number of queries extracted per feature set: 10
- minimum size of each query extracted (number of triples): 3
- minimum size of the result of each query extracted and answered (number of triples): 1

To #CHANGE PARAMETERS, use the following as options when running the jar, e.g: java -jar -Dm=20 qed.jar 
- query log: -Dq=YOUR_ADDRESS
- data: -Dd=YOUR_ADDRESS
- maximal number of queries extracted per feature set: -Dm=YOUR_NUMBER
- minimum size of each query extracted (number of triples): -Ds=YOUR_NUMBER
- minimum size of the result of each query extracted and answered (number of triples): -Dr=YOUR_NUMBER

#NOTE
We recommend to host the DBpedia data from the time point at which the LSQ data set was created (DBpedia
v.3.5.1, available at http://wiki.dbpedia.org/services-resources/datasets/data-set-35/data-set-351), since some properties represented in the LSQ data set are specific to that (see the LSQ paper for a more detailed overview of that query format, it is linked at http://aksw.github.io/LSQ/). However, if you want to just get an idea of QED you can also take the current DBpedia version hosted at http://dbpedia.org/sparql.

Currently, the feature configuration can only be customized if you #RUN THE SOURCE CODE. For that, check out class qed/QED!

