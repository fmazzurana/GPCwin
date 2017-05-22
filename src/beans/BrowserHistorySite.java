package beans;

public class BrowserHistorySite {
	
	// properties
	private String lastVisit;	// in the format %d/%m/%Y %H:%M:%S
	private int typed;			// typed into the address bar (1) or not (0)
	private String url;
	private String title;		// could be empty
	
	// getters & setters
	public String getLastVisit() {
		return lastVisit;
	}
	public void setLastVisit(String lastVisit) {
		this.lastVisit = lastVisit;
	}

	public int getTyped() {
		return typed;
	}
	public void setTyped(int typed) {
		this.typed = typed;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
