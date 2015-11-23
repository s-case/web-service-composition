package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

import java.util.ArrayList;

public class MDEOperation {

	private String belongsToURL;
	private String belongsToWSType;
	private String hasCRUDVerb;
	private String hasName;
	private String hasResourcePath;
	private ArrayList<String> hasResponseType;
	private ArrayList<MDERepresentation> hasURIParameters;
	private ArrayList<MDERepresentation> hasInput;
	private ArrayList<MDERepresentation> hasOutput;
	private ArrayList<MDERepresentation> hasQueryParameters;

	MDEOperation(String belongsToURL, String belongsToWSType, String hasCRUDVerb, String hasName,
			String hasResourcePath, ArrayList<String> hasResponseType, ArrayList<MDERepresentation> hasURIParameters,
			ArrayList<MDERepresentation> hasInput, ArrayList<MDERepresentation> hasOutput,
			ArrayList<MDERepresentation> hasQueryParameters) {

		this.belongsToURL = belongsToURL;
		this.belongsToWSType = belongsToWSType;
		this.hasCRUDVerb = hasCRUDVerb;
		this.hasName = hasName;
		this.hasResourcePath = hasResourcePath;
		this.hasResponseType = hasResponseType;
		this.hasURIParameters = hasURIParameters;
		this.hasInput = hasInput;
		this.hasOutput = hasOutput;
		this.hasQueryParameters = hasQueryParameters;

	}

	public String getBelongsToURL() {
		return this.belongsToURL;
	}

	public String getBelongsToWSType() {
		return this.belongsToWSType;
	}

	public String getHasCRUDVerb() {
		return this.hasCRUDVerb;
	}
	public String getHasName(){
		return this.hasName;
	}
	public String gethasResourcePath(){
		return this.hasResourcePath;
	}
	public ArrayList<String> getHasResponseType(){
		return this.hasResponseType;
	}
	public ArrayList<MDERepresentation> getHasURIParameters(){
		return this.hasURIParameters;
	}
	public ArrayList<MDERepresentation> getHasInput(){
		return this.hasInput;
	}
	public ArrayList<MDERepresentation> getHasOutput(){
		return this.hasOutput;
	}
	public ArrayList<MDERepresentation> getHasQueryParameters(){
		return this.hasQueryParameters;
	}
}
