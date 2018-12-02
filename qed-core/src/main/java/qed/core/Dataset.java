package qed.core;

public class Dataset {
	
	private String name = null;
	private String endpoint = null;//where to find data
	
	public Dataset(String name, String endpoint) {
		this.setName(name);
		this.setEndpoint(endpoint);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

}
