# SPARQL QED

SPAQRL QED is a system generating out-of-the-box datasets for SPARQL queries over linked data. It distinguishes the queries according to the different SPARQL features and creates, for each query, a small but exhaustive dataset comprising linked data and the query answers over this data. We ensure that the created datasets are diverse and cover various practical use cases and, of course, that the sets of answers included are the correct ones.

For a more detailed introduction into the approach and a coarse overview of the system see /paper.pdf.

# /qed-core
Contains the query and data extraction. It also contains code for retrieving the answers based on only the extracted datasets. 

# /qed-generator
Contains the data generation, integration of generated and extracted data, and the answer generation. 

# Requirements
- qed-core: [maven](https://maven.apache.org/)
- qed-generator: qed-core, [ant](https://ant.apache.org/), [maven](https://maven.apache.org/), [quetzal](https://github.com/Quetzal-RDF/quetzal)

# Setup
If you only want to use qed-core: Run 'mvn clean install' in the top-level directory.

Otherwise:
- Clone the quetzal project 
- Run 'checkout Jena_3.4.0' in the top-level directory of quetzal
- Run 'ant' in quetzal/rdfstore-checker
- Run 'mvn clean install' in the top-level directory of qed

# Examples
The datasets described in the paper can be found in /data. Demo classes in both qed-core and qed-generator show how to run the system, a description of the generator demo follows below. 
The /data directory contains a text file with the example queries from Wikidata for running the query and data extraction, and the generation. 
For reproducing the DBpedia experiments, we recommend to host the DBpedia data from the time point at which the LSQ data set was created ([DBpedia
v.3.5.1](http://wiki.dbpedia.org/services-resources/datasets/data-set-35/data-set-351)), since some properties represented in the LSQ data set are specific to that (see the LSQ paper for a more detailed overview of that query format, it is linked at http://aksw.github.io/LSQ/). However, if you want to just get an idea of QED (maybe with custom queries over DBpedia) you can also query the (current) [DBpedia endpoint](http://dbpedia.org/sparql).

# /qed-generator Demo
To illustrate how the generator system can work, take a look at the qed-generator/source/com/ibm/qed/generator/Demo class.  That class downloads data from Wikidata, and then generates additional data to fill out the dataset.  There is a run configuration in qed-generator/launchers/Demo.launch that provides aguments to run on the provided test queries in qed-generator/demo/wikidata.txt; it specifies both the data file and also the library path needed for native libraries required by the solver.

# Note
This is the initial version of QED, and we are heavily working on a more stable version. If you have questions, ideas for extensions, or other issues, do not hesitate to contact us under veronika.thostATgmail.com or juliandolbyATgmail.com!



