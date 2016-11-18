package eu.scasefp7.eclipse.servicecomposition.importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.jena.riot.RDFDataMgr;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * <h1>OwlImporter</h1> This class can be used to import services from
 * <i>.owl</i> files as operations.
 * 
 * @author Manios Krasanakis
 */
public class OwlImporter extends Importer {
	/**
	 * <h1>importOwl</h1> This function generates a list of all operations from
	 * an OWL file.
	 * 
	 * @param owlPath
	 *            : the path of the OWL file
	 * @param importWSDLOnly
	 *            : true to import only WSDL services
	 * @param singleOutputsOnly
	 *            : true to import only operations with single output
	 * @return an <code>ArrayList</code> containing all detected OWL classes
	 *         (which contain all operations)
	 * @throws Exception
	 */
	public static ArrayList<Operation> importOwl(String owlPath, boolean importWSDLOnly, boolean singleOutputsOnly)
			throws Exception {
		ArrayList<Operation> allOperations = new ArrayList<Operation>();

		// load the model and set the various properties to be used
		Model base = RDFDataMgr.loadModel(owlPath);
		ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, base);
		belongsToURL = ontologyModel.getDatatypeProperty(prefix + "belongsToURL");
		hasResourcePath = ontologyModel.getDatatypeProperty(prefix + "hasResourcePath");
		hasDescription = ontologyModel.getDatatypeProperty(prefix + "hasDescription");
		belongsToWSType = ontologyModel.getDatatypeProperty(prefix + "belongsToWSType");
		hasServiceDomain = ontologyModel.getObjectProperty(prefix + "hasServiceDomain");
		belongsToUser = ontologyModel.getDatatypeProperty(prefix + "belongsToUser");
		hasName = ontologyModel.getDatatypeProperty(prefix + "hasName");
		isPrototype = ontologyModel.getDatatypeProperty(prefix + "isPrototype");
		hasInput = ontologyModel.getObjectProperty(prefix + "hasInput");
		hasCRUDVerb = ontologyModel.getDatatypeProperty(prefix + "hasCRUDVerb");
		hasSecurityScheme = ontologyModel.getDatatypeProperty(prefix + "hasSecurityScheme");
		hasRequestHeader = ontologyModel.getDatatypeProperty(prefix + "hasRequestHeader");
		isRequired = ontologyModel.getDatatypeProperty(prefix + "isRequired");
		hasOutput = ontologyModel.getObjectProperty(prefix + "hasOutput");
		hasType = ontologyModel.getObjectProperty(prefix + "hasType");
		isTypeOf = ontologyModel.getDatatypeProperty(prefix + "isTypeOf");
		isArray = ontologyModel.getDatatypeProperty(prefix + "isArray");
		belongsToPrototype = ontologyModel.getObjectProperty(prefix + "belongsToPrototype");
		// IPR properties
		hasServiceAccessInfo = ontologyModel.getObjectProperty(prefix + "hasServiceAccessInfo");
		hasDescription = ontologyModel.getDatatypeProperty(prefix + "hasDescription");

		hasURLforAccess = ontologyModel.getDatatypeProperty(prefix + "hasURLforAccess");
		hasLicense = ontologyModel.getObjectProperty(prefix + "hasLicense");
		hasLicenseDescription = ontologyModel.getDatatypeProperty(prefix + "hasLicenseDescription");
		hasLicenseName = ontologyModel.getDatatypeProperty(prefix + "hasLicenseName");
		isProprietary = ontologyModel.getDatatypeProperty(prefix + "isProprietary");
		hasCommercialCostSchema = ontologyModel.getObjectProperty(prefix + "hasCommercialCostSchema");
		hasCommercialCost = ontologyModel.getDatatypeProperty(prefix + "hasCommecrialCost");
		hasCommercialCostCurrency = ontologyModel.getDatatypeProperty(prefix + "hasCommercialCostCurrency");
		hasCostPaymentChargeType = ontologyModel.getDatatypeProperty(prefix + "hasCostPaymentChargeType");
		hasTrialSchema = ontologyModel.getObjectProperty(prefix + "hasTrialSchema");
		hasDurationInDays = ontologyModel.getDatatypeProperty(prefix + "hasDurationInDays");
		hasLimitedFunctionalityDescription = ontologyModel
				.getDatatypeProperty(prefix + "hasLimitedFunctionalityDescription");
		hasDurationInUsages = ontologyModel.getDatatypeProperty(prefix + "hasDurationInUsages");
		offersFullFunctionalityDuringTrial = ontologyModel
				.getDatatypeProperty(prefix + "offersFullFunctionalityDuringTrial");
		hasDiscountIfUsedWithOtherSolution = ontologyModel
				.getObjectProperty(prefix + "hasDiscountIfUsedWithOtherSolution");
		hasDiscount = ontologyModel.getDatatypeProperty(prefix + "hasDiscount");
		hasDiscountReason = ontologyModel.getDatatypeProperty(prefix + "hasDiscountReason");
		hasPairedService = ontologyModel.getDatatypeProperty(prefix + "hasPairedService");
		isValidForCountries = ontologyModel.getDatatypeProperty(prefix + "isValidForCountries");
		hasTitle = ontologyModel.getDatatypeProperty(prefix + "hasTitle");
		
		OwlMetadata.updateDatatypes(prefix, ontologyModel);

		// load domains
		OntClass cl = ontologyModel.getOntClass(prefix + "ApplicationDomains");
		List<? extends OntResource> domainListing = cl.listInstances().toList();
		for (int i = 0; i < domainListing.size(); i++) {
			Individual ind = (Individual) domainListing.get(i);
			ApplicationDomain domain = new ApplicationDomain(ind);
			domainList.put(domain.getURI(), domain);
		}
		// load operations they are prototype, belong to user and have WSDL
		HashMap<String, Operation> operationsByName = new HashMap<String, Operation>();
		OntClass className = ontologyModel.getOntClass(prefix + "operation");
		List<? extends OntResource> list = className.listInstances().toList();
		

		// load implementations
		for (int i = 0; i < list.size(); i++) {
			System.out.println(i + " " + list.get(i).getPropertyValue(hasName));
			Individual ind = (Individual) list.get(i);

			if (ind.getPropertyValue(belongsToUser) != null && ind.getPropertyValue(isPrototype) != null
					&& ind.getPropertyValue(isPrototype).asLiteral().getString().equals("false")) {

				Operation operation = new Operation(ind);
				allOperations.add(operation);

			}
		}

		// return
		return allOperations;
	}
}
