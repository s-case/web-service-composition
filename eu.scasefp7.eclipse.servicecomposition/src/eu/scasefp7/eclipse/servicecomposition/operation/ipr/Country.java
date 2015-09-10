package eu.scasefp7.eclipse.servicecomposition.operation.ipr;

public class Country{
	private String country = "";
	private String language = "";
	
	public Country(){
		this.country = "";
		this.language = "";
	}
	
	public Country(String country,String language){
		this.country = country;
		this.language = language;
	}
	
	public void setCountry(String country){
		this.country=country;
	}
	
	public void setLanguage(String language){
		this.language=language;
	}
	
	public String getCountry(){
		return this.country;
	}
	
	public String getLanguage(){
		return this.language;
	}
	
}
