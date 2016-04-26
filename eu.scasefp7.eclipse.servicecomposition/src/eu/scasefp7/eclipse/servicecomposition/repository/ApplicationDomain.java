package eu.scasefp7.eclipse.servicecomposition.repository;

public class ApplicationDomain {
	private String name = "";
	private String uri = "";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (((ApplicationDomain) obj).getUri().equals(uri)) {
			return true;
		} else
			return false;
	}

}
