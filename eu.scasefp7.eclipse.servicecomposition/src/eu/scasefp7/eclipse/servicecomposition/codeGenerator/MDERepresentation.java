package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

import java.util.ArrayList;

public class MDERepresentation {

	private boolean isAuthToken;
	private boolean isOptional;
	private String belongsToURL;
	private String hasName;
	private String isType;
	private ArrayList<String> hasElements;

	MDERepresentation(boolean isAuthToken, boolean isOptional, String belongsToURL, String hasName, String isType,
			ArrayList<String> hasElements) {
		this.isAuthToken= isAuthToken;
		this.isOptional = isOptional;
		this.belongsToURL = belongsToURL;
		this.hasName = hasName;
		this.isType = isType;
		this.hasElements =hasElements;

	}
	
	public boolean isAuthToken(){
		return this.isAuthToken;
	}
	public boolean isOptional(){
		return this.isOptional;
	}
	public String getBelongsToURL(){
		return this.belongsToURL;
	}
	public String getHasName(){
		return this.hasName;
	}
	public String getIsType(){
		return this.isType;
	}
	public ArrayList<String> getHasElements(){
		return this.hasElements;
	}
}
