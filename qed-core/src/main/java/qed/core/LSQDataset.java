/**
 * 
 */
package qed.core;

/**
 * @author veronika.thost
 *
 */
public class LSQDataset extends Dataset {
	private String graphUri = null;
	
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
