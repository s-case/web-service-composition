package eu.scasefp7.eclipse.servicecomposition.importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.jena.riot.RDFDataMgr;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * <h1>OwlImporter</h1>
 * This class can be used to import services from <i>.owl</i> files as operations.
 * @author Manios Krasanakis
 */
public class OwlImporter extends Importer{
	/**
	 * <h1>importOwl</h1>
	 * This function generates a list of all operations from an OWL file.
	 * @param owlPath : the path of the OWL file
	 * @param importWSDLOnly : true to import only WSDL services
	 * @param singleOutputsOnly : true to import only operations with single output
	 * @return an <code>ArrayList</code> containing all detected OWL classes (which contain all operations)
	 * @throws Exception
	 */
	public static ArrayList<Operation> importOwl(String owlPath, boolean importWSDLOnly, boolean singleOutputsOnly) throws Exception{
		ArrayList<Operation> allOperations = new ArrayList<Operation>();
		
		//load the model and set the various properties to be used
		Model base = RDFDataMgr.loadModel(owlPath);
		ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, base);	
		belongsToURL = ontologyModel.getDatatypeProperty(prefix + "belongsToURL");
		hasResourcePath = ontologyModel.getDatatypeProperty(prefix + "hasResourcePath");
		belongsToWSType = ontologyModel.getDatatypeProperty(prefix + "belongsToWSType");
		hasServiceDomain=ontologyModel.getObjectProperty(prefix + "hasServiceDomain");
		belongsToUser = ontologyModel.getDatatypeProperty(prefix + "belongsToUser");
		hasName = ontologyModel.getDatatypeProperty(prefix + "hasName");
		isPrototype = ontologyModel.getDatatypeProperty(prefix + "isPrototype");
		hasQueryParameters= ontologyModel.getObjectProperty(prefix + "hasQueryParameters");
		hasInput = ontologyModel.getObjectProperty(prefix + "hasInput");
		hasURIParameters = ontologyModel.getDatatypeProperty(prefix + "hasURIParameters");
		hasCRUDVerb = ontologyModel.getDatatypeProperty(prefix + "hasCRUDVerb");
		hasSecurityScheme = ontologyModel.getDatatypeProperty(prefix + "hasSecurityScheme");
		isRequired = ontologyModel.getDatatypeProperty(prefix + "isRequired");
		hasOutput = ontologyModel.getObjectProperty(prefix + "hasOutput");
		hasType = ontologyModel.getObjectProperty(prefix + "hasType");
		isArray = ontologyModel.getDatatypeProperty(prefix + "isArray");
		belongsToPrototype = ontologyModel.getObjectProperty(prefix + "belongsToPrototype");
		OwlMetadata.updateDatatypes(prefix, ontologyModel);
		
		//load domains
		OntClass cl = ontologyModel.getOntClass(prefix + "ApplicationDomains");
		List<? extends OntResource> domainListing = cl.listInstances().toList();
		for(int i = 0; i < domainListing.size(); i++) {
			Individual ind = (Individual) domainListing.get(i);
			ApplicationDomain domain = new ApplicationDomain(ind);
			domainList.put(domain.getURI(), domain);
		}
		//load operations they are prototype, belong to user and have WSDL
		HashMap<String, Operation> operationsByName = new HashMap<String, Operation>();
		OntClass className = ontologyModel.getOntClass(prefix + "operation");
		List<? extends OntResource> list = className.listInstances().toList();
		for(int i = 0; i < list.size(); i++){
			Individual ind = (Individual) list.get(i);
			if((ind.getPropertyValue(isPrototype) == null || ind.getPropertyValue(isPrototype).asLiteral().getString().equals("true"))) {
				if(!importWSDLOnly || (ind.getPropertyValue(belongsToUser)!=null && ind.getPropertyValue(belongsToURL) != null && !ind.getPropertyValue(belongsToURL).asLiteral().getString().isEmpty())){
					Operation operation = new Operation(ind);
					if(!singleOutputsOnly || operation.getOutputs().size()<=1){
						operationsByName.put(operation.getName().toString(), operation);
						allOperations.add(operation);
					}
				}
			}
		}
		
		//load implementations
		for(int i = 0; i < list.size(); i++){
			Individual ind = (Individual) list.get(i);
			
			
			if(ind.getPropertyValue(isPrototype) != null && ind.getPropertyValue(isPrototype).asLiteral().getString().equals("false")) {
				if(ind.getPropertyValue(belongsToPrototype) != null)
				{
					Operation baseOperation = operationsByName.get(ind.getPropertyValue(belongsToPrototype).asResource().toString());
					if(baseOperation!=null){
						Operation operation = new Operation(ind);
						if(!singleOutputsOnly || operation.getOutputs().size()<=1)
							baseOperation.addRealOperation(operation);
					}
				}
				else if(!importWSDLOnly || (ind.getPropertyValue(belongsToUser)!=null && ind.getPropertyValue(belongsToURL) != null && !ind.getPropertyValue(belongsToURL).asLiteral().getString().isEmpty())){
					Operation operation = new Operation(ind);
					if(!singleOutputsOnly || operation.getOutputs().size()<=1){
						operationsByName.put(operation.getName().toString(), operation);
						allOperations.add(operation);
					}
				}
			}
		}
		
		//return
		return allOperations;
	}
}
