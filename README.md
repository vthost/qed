# SPARQL QED

SPAQRL QED is a system generating out-of-the-box datasets for SPARQL queries over linked data. It distinguishes the queries according to the different SPARQL features and creates, for each query, a small but exhaustive dataset comprising linked data and the query answers over this data. We ensure that the created datasets are diverse and cover various practical use cases and, of course, that the sets of answers included are the correct ones.

For a more detailed introduction into the approach and a coarse overview of the system see paper.pdf.

# /qed-core
Contains he query and data extraction. 

# /qed-gen
Contains the data and answer generation. 


# Requirements:
- qed-core: [maven](https://maven.apache.org/)
- qed-gen: qed-core, [maven](https://maven.apache.org/), [quetzal](https://github.com/Quetzal-RDF/quetzal)

# Setup:
- qed-core: Run 'mvn clean install' in qed-core.
- qed-gen:


# Examples
The datasets described in the paper can be found in /data. Demo classes in both qed-core and qed-gen show how to run the system. 
The /data directory also contains a text file with the example queries from Wikidata for running the query and data extraction, and the generation. 
For reproducing the DBpedia experiments, we recommend to host the DBpedia data from the time point at which the LSQ data set was created (DBpedia
v.3.5.1, available at http://wiki.dbpedia.org/services-resources/datasets/data-set-35/data-set-351), since some properties represented in the LSQ data set are specific to that (see the LSQ paper for a more detailed overview of that query format, it is linked at http://aksw.github.io/LSQ/). However, if you want to just get an idea of QED (maybe with custom queries over DBpedia) you can also take the current DBpedia version hosted at http://dbpedia.org/sparql.

# Note
This is the initial version of QED, and we are heavily working on a more stable version. If you have questions, ideas for extensions, or other issues, do not hesitate to contact us under veronika.thostATgmail.com or jdolbyATgmail.com!



