# SPARQL QED

SPAQRL QED is a system generating out-of-the-box datasets for SPARQL queries over linked data. It distinguishes the queries according to the different SPARQL features and creates, for each query, a small but exhaustive dataset comprising linked data and the query answers over this data. We ensure that the created datasets are diverse and cover various practical use cases and, of course, that the sets of answers included are the correct ones.

For a more detailed introduction into the approach and a coarse overview of the system see qed/paper.pdf

You can #RUN the core application qed/qed-core as follows using the command line:
- build the project qed-core: mvn clean install
- run the generated .jar file: java -jar PATH_ON_YOUR_MACHINE/qed/qed-core/target/qed-core-0.0.1-SNAPSHOT-stand-alone.jar

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
- query log: the LSQ endpoint http://lsq.aksw.org/sparql
- query log uri: the queries for DBpedia, i.e., the uri http://dbpedia.org
- data: assumed to be hosted somewhere by you and available at http://localhost:8080/sparql
- maximal number of queries extracted per feature set: 10
- minimum size of each query extracted (number of triples): 3
- minimum size of the result of each query extracted and answered (number of triples): 1
- directory containing the test suite: data, located at the root of the project

To #CHANGE PARAMETERS, use the following (optional) options when running the jar, e.g: java -jar -m 20 PATH_ON_YOUR_MACHINE/qed/qed-core/target/qed-core-0.0.1-SNAPSHOT-stand-alone.jar
- query log: -q YOUR_URL
- graph uri: -u YOUR_URI
- data: -d YOUR_URL
- maximal number of queries extracted per feature set: -m YOUR_NUMBER
- minimum size of each query extracted (number of triples): -s YOUR_NUMBER
- minimum size of the result of each query extracted and answered (number of triples): -r YOUR_NUMBER
- location of the data folder containing the test suite: -l YOUR_FILE_PATH


#NOTE
We recommend to host the DBpedia data from the time point at which the LSQ data set was created (DBpedia
v.3.5.1, available at http://wiki.dbpedia.org/services-resources/datasets/data-set-35/data-set-351), since some properties represented in the LSQ data set are specific to that (see the LSQ paper for a more detailed overview of that query format, it is linked at http://aksw.github.io/LSQ/). However, if you want to just get an idea of QED you can also take the current DBpedia version hosted at http://dbpedia.org/sparql.

Currently, the feature configuration can only be customized if you #RUN THE SOURCE CODE. For that, check out class qed-core/Application!

This is the initial version of QED, and we are heavily working on a more stable version. If you have questions, ideas for extensions, or other issues, do not hesitate to contact us under veronika.thostATgmail.com or jdolbyATgmail.com!



