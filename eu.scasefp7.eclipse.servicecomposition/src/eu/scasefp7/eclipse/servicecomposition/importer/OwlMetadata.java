package eu.scasefp7.eclipse.servicecomposition.importer;



import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;











import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Individual;

import eu.scasefp7.eclipse.servicecomposition.Activator;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Service;
import eu.scasefp7.eclipse.servicecomposition.transformer.Similarity;
import eu.scasefp7.eclipse.servicecomposition.transformer.Similarity.ComparableName;

/**
 * <h1>OwlMetadata</h1>
 * WHEN ALTERING THIS CLASS DONNOT FORGET TO ALSO ADD APPROPRIATE PROPERTIES
 * IN THE matcher.properties FILE
 */
public class OwlMetadata {
	//ontology datatypes
	private static DatatypeProperty hasDescription;
	private static DatatypeProperty hasCost;
	
	//datatype insances
	private ComparableName description;
	private double cost;
	
	//instance weights
	private static double DESCRIPTION_WEIGHT = 0;
	private static double COST_WEIGHT = 0;

	/**
	 * <h1>loadProperties</h1>
	 * Loads matching properties from the file <i>matcher.properties</i>.
	 */
	public static void loadProperties(){
		Properties prop = new Properties();
		String propFileName = "matcher.properties";
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		try{
			URL fileURL = bundle.getEntry("matcher.properties");
			InputStream inputStream = new FileInputStream(new File(FileLocator.resolve(fileURL).toURI()));
			prop.load(inputStream);
			DESCRIPTION_WEIGHT = Double.parseDouble(prop.getProperty("metadata.DESCRIPTION_WEIGHT"));
			COST_WEIGHT = Double.parseDouble(prop.getProperty("metadata.COST_WEIGHT"));
		}
		catch(Exception e){
			System.err.println("Error occured while trying to load matcher settings from "+propFileName);
		}
	}
	
	public static void updateDatatypes(String prefix, OntModel ontologyModel){
		hasDescription = ontologyModel.getDatatypeProperty(prefix + "hasDescription");
		hasCost = ontologyModel.getDatatypeProperty(prefix + "hasCost");
	}
	
	/**
	 * Generates metadata for an OWL service.
	 * @param ind
	 */
	public OwlMetadata(Individual ind){
		if(ind.getPropertyValue(hasDescription)!=null)
			description = new ComparableName(ind.getPropertyValue(hasDescription).asLiteral().getString());
		else
			description = new ComparableName("");
		if(ind.getPropertyValue(hasCost)!=null)
			cost = 0;//Double.parseDouble(ind.getPropertyValue(hasCost).asLiteral().getString());
	}
	
	/**
	 * <h1>getSimilarity</h1>
	 * This function generates a bias for the <code>Matcher.match</code> function to use.
	 * @param action : the action to possibly compare some metadata features with
	 * @return the similarity of the metadata to the action
	 */
	public double getSimilarity(Service action){
		return cost*COST_WEIGHT + Similarity.similarity(description, action.getName())*DESCRIPTION_WEIGHT;
	}
	
	@Override
	public String toString(){
		String ret = "";
		if(cost!=0)
			ret += "Cost: "+cost+"\n";
		if(!description.isEmpty())
			ret += "Description: "+description+"\n";
		return ret;
	}
	
}
