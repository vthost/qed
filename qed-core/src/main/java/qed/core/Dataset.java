package qed.core;

public enum Dataset {
	DBPEDIA ("http://dbpedia.org", "http://localhost:8080/sparql"),
	GEODATA ("http://linkedgeodata.org", "http://linkedgeodata.org/sparql"),
//	SEMANTIC_WEB ("http://data.semanticweb.org", "http://www.scholarlydata.org/sparql/"),
//	BM ( "http://bm.rkbexplorer.com", "https://collection.britishmuseum.org/resource/sparql"),
	WIKIDATA ("","https://query.wikidata.org/bigdata/namespace/wdq/sparql");
	
	public String graphUri = null;
	String endpoint = null;
	private Dataset(String graphUri, String endpoint) {
		this.graphUri = graphUri;
		this.endpoint = endpoint;
	}

}
