
package qed.core;

/**
 *
 */
public class LSQDataset extends Dataset {
//	currently one of:
//		DBpedia: http://dbpedia.org
//		Linked Geo Data: http://linkedgeodata.org
//		Semantic Web Dog Food: http://data.semanticweb.org
//		British Museum: http://bm.rkbexplorer.com	
	private String graphUri = null; // identifying the dataset at LSQ
	
	
	public LSQDataset(String name, String endpoint, String graphUri) {
		super(name, endpoint);
		this.setGraphUri(graphUri);		
	}

	public String getGraphUri() {
		return graphUri;
	}

	public void setGraphUri(String graphUri) {
		this.graphUri = graphUri;
	}

}
