package eu.scasefp7.eclipse.servicecomposition.operation.ipr;

public class Description{
	private String description = "";
	private String language = "";
	
	public Description(){
		this.description = "";
		this.language = "";
	}
	
	public Description(String description,String language){
		this.description = description;
		this.language = language;
	}
	
	public void setDescription(String description){
		this.description=description;
	}
	
	public void setLanguage(String language){
		this.language=language;
	}
	
	public String getDescription(){
		return this.description;
	}
	
	public String getLanguage(){
		return this.language;
	}
	
}
