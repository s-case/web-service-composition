package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

import java.util.ArrayList;

public class MDERepresentation {

	/**
	 * ontology property
	 */
	private boolean isAuthToken;
	/**
	 * ontology property
	 */
	private boolean isOptional;
	/**
	 * ontology property
	 */
	private String belongsToURL;
	/**
	 * ontology property
	 */
	private String hasName;
	/**
	 * ontology property
	 */
	private String isType;
	/**
	 * ontology property
	 */
	private ArrayList<MDERepresentation> hasElements;
	/**
	 * ontology property
	 */
	private ArrayList<String> hasPrimitiveElements;
	

	MDERepresentation(){
		
	}

	MDERepresentation(boolean isAuthToken, boolean isOptional, String belongsToURL, String hasName, String isType,
			ArrayList<MDERepresentation> hasElements, ArrayList<String> hasPrimitiveElements) {
		this.isAuthToken= isAuthToken;
		this.isOptional = isOptional;
		this.belongsToURL = belongsToURL;
		this.hasName = hasName;
		this.isType = isType;
		this.hasElements =hasElements;
		this.hasPrimitiveElements = hasPrimitiveElements;

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
	public ArrayList<MDERepresentation> getHasElements(){
		return this.hasElements;
	}
	public ArrayList<String> getHasPrimitiveElements(){
		return this.hasPrimitiveElements;
	}
}
