/**
 * 
 */
package qed.core;

/**
 * @author veronika.thost
 *
 */
public class LogFileDataset extends Dataset {
	private String location = null;//of queries
	private String idStr = null;
	
	public LogFileDataset(String name, String location, String endpoint, String idsStr) {
		super(name, endpoint);
		this.setLocation(location);
		this.setIdStr(idsStr);	
	}

	public String getIdStr() {
		return idStr;
	}

	public void setIdStr(String idStr) {
		this.idStr = idStr;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
