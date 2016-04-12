package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;

import eu.scasefp7.eclipse.core.ontology.LinkedOntologyAPI;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.operationCaller.RAMLCaller;
import edu.uci.ics.jung.graph.Graph;

public class ConnectToMDEOntology {

	/**
	 * the operation to be written in the ontology
	 */
	MDEOperation operation;
	/**
	 * primitive datatypes
	 */
	String[] datatypes = new String[] { "string", "long", "int", "float", "double", "dateTime", "boolean" };

	ConnectToMDEOntology() {

	}

	public MDEOperation createObjects(String projectName, ArrayList<OwlService> inputs,
			ArrayList<Argument> uriParameters, ArrayList<OwlService> outputs, final Graph<OwlService, Connector> graph) {
		ArrayList<MDERepresentation> hasQueryParameters = new ArrayList<MDERepresentation>();
		ArrayList<MDERepresentation> hasInput = new ArrayList<MDERepresentation>();
		ArrayList<MDERepresentation> hasURIParameters = new ArrayList<MDERepresentation>();
		
		ArrayList<MDERepresentation> hasOutput = new ArrayList<MDERepresentation>();
		// Query Parameters
		for (OwlService input : inputs) {
			String url = ((Operation) input.getArgument().getBelongsToOperation()).getDomain().getURI();
			if (((Operation) input.getArgument().getBelongsToOperation()).getDomain().getResourcePath() != null) {
				url = url + ((Operation) input.getArgument().getBelongsToOperation()).getDomain().getResourcePath();
			}
			String type = "Primitive";
			if (input.getArgument().isArray()) {
				type = "Array";
			}else if (!input.getArgument().isArray()&& !RAMLCaller.stringIsItemFromList(input.getArgument().getType(), datatypes)){
				type = "Object";
			}
			
			if (input.getArgument().getSubtypes().isEmpty()&& input.getArgument().isTypeOf().equals("QueryParameter")) {
				ArrayList<String> subNames = new ArrayList<String>();
				subNames.add(input.getArgument().getType().toLowerCase());
				MDERepresentation inputRepresentation = new MDERepresentation(false, !input.getArgument().isRequired(),
						url, input.getName().getComparableForm(), type, null, subNames);
				hasQueryParameters.add(inputRepresentation);
			}

			else {
				ArrayList<String> subNames = new ArrayList<String>();
				for (Argument sub : input.getArgument().getSubtypes()) {
					subNames.add(sub.getName().getComparableForm());
				}
				MDERepresentation inputRepresentation = new MDERepresentation(false, !input.getArgument().isRequired(),
						url, input.getName().getComparableForm(), type, null, subNames);
				hasInput.add(inputRepresentation);
			}
		}

		for (OwlService uriParameter : inputs) {
			String url = ((Operation) uriParameter.getArgument().getBelongsToOperation()).getDomain().getURI();
			if (((Operation) uriParameter.getArgument().getBelongsToOperation()).getDomain().getResourcePath() != null) {
				url = url + ((Operation) uriParameter.getArgument().getBelongsToOperation()).getDomain().getResourcePath();
			}
			ArrayList<String> subNames = new ArrayList<String>();
			subNames.add("string");
			MDERepresentation inputRepresentation = new MDERepresentation(false, !uriParameter.getArgument().isRequired(), url,
					uriParameter.getArgument().getName().getComparableForm(), "Primitive", null, subNames);
			hasURIParameters.add(inputRepresentation);
		}

		// Outputs
		for (OwlService output : outputs) {
			String url = ((Operation) output.getArgument().getBelongsToOperation()).getDomain().getURI();
			String type = "Primitive";
			if (output.getArgument().isArray()) {
				type = "Array";
			}else if (!output.getArgument().isArray()&& !RAMLCaller.stringIsItemFromList(output.getArgument().getType(), datatypes)){
				type = "Object";
			}
			if (output.getArgument().getSubtypes().isEmpty()) {
				ArrayList<String> subNames = new ArrayList<String>();
				subNames.add(output.getArgument().getType().toLowerCase());
				MDERepresentation outputRepresentation = new MDERepresentation(false, !output.getArgument().isRequired(),
						url, output.getName().getComparableForm(), type, null, subNames);
				hasOutput.add(outputRepresentation);
			} else {
				ArrayList<String> subNames = new ArrayList<String>();
				ArrayList<MDERepresentation> hasSubs=new ArrayList<MDERepresentation>();
				for (OwlService sub : graph.getSuccessors(output)) {
					hasSubs.add(addSubtypes(sub, graph, hasSubs));
					subNames.add(sub.getName().getComparableForm());
				}
				MDERepresentation outputRepresentation = new MDERepresentation(false, !output.getArgument().isRequired(),
						url, output.getName().getComparableForm(), type, hasSubs, subNames);
				hasOutput.add(outputRepresentation);
				
			}
		}

		ArrayList<String> hasResponseType = new ArrayList<String>();
		hasResponseType.add("JSON");
		hasResponseType.add("SOAP");
		String url = "http://109.231.127.61:8080/" + projectName + "-0.0.1-SNAPSHOT/";
		operation = new MDEOperation(url, "RESTful", "GET", projectName, "rest/result/query", hasResponseType, hasURIParameters,
				hasInput, hasOutput, hasQueryParameters);

		return operation;

	}

	public static void writeToOntology(IProject project, MDEOperation operation) {

		// Create a new file for the linked ontology and instantiate it
		LinkedOntologyAPI linkedOntology = new LinkedOntologyAPI(project,false);

		// Add a new resource in the linked ontology

		linkedOntology.addResource(project.getName() + "_Resource", true);
		linkedOntology.connectProjectToElement(project.getName() + "_Resource");

		// Add a new operation for the resource
		linkedOntology.addOperationToResource(project.getName() + "_Resource", operation.getHasName(),
				operation.getBelongsToURL(), operation.gethasResourcePath(), operation.getBelongsToWSType(),
				operation.getHasCRUDVerb(), operation.getHasResponseType().get(0));

		// Add the query parameters of the operation

		if (operation.getHasQueryParameters() != null) {
			ArrayList<MDERepresentation> queryParams = operation.getHasQueryParameters();
			List<String> queryNames = new ArrayList<String>();
			for (MDERepresentation queryParam : queryParams) {
				if (queryParam.getHasElements()==null){
				linkedOntology.addInputParameter(queryParam.getHasName(), queryParam.getIsType(),
						queryParam.isAuthToken(), queryParam.getBelongsToURL(), queryParam.isOptional(),
						queryParam.getHasPrimitiveElements());
				queryNames.add(queryParam.getHasName());
				}else{
					List<String> subNames = new ArrayList<String>();
					for (MDERepresentation sub : queryParam.getHasElements()){
						subNames.add(sub.getHasName());
					}
					linkedOntology.addInputParameter(queryParam.getHasName(), queryParam.getIsType(),
							queryParam.isAuthToken(), queryParam.getBelongsToURL(), queryParam.isOptional(),
							subNames);
				}
			}
			linkedOntology.addQueryParametersToOperation(operation.getHasName(), queryNames);
		}

		// Add the output parameters of the operation
		if (operation.getHasOutput() != null) {
			List<String> outputNames = new ArrayList<String>();
			for (MDERepresentation output : operation.getHasOutput()) {
				
					addOutputs(output,linkedOntology);
					outputNames.add(output.getHasName());
				
			}
			linkedOntology.addOutputParametersToOperation(operation.getHasName(), outputNames);
		}

		// Close the linked ontology. The other two ontologies are not closed
		// since they do not need to be saved.
		linkedOntology.close();
	}
	
	private MDERepresentation addSubtypes(OwlService sub, final Graph<OwlService, Connector> graph, ArrayList<MDERepresentation> hasSubs) {
	
		String type="Primitive";
		if (sub.getArgument().isArray()) {
			type = "Array";
		}else if (!sub.getArgument().isArray()&& !RAMLCaller.stringIsItemFromList(sub.getArgument().getType(), datatypes)){
			type = "Object";
		}
		MDERepresentation outputRepresentation= new MDERepresentation();
		if (sub.getArgument().getSubtypes().isEmpty()) {
			ArrayList<String> subNames = new ArrayList<String>();
			subNames.add(sub.getArgument().getType().toLowerCase());
			outputRepresentation = new MDERepresentation(false, !sub.getArgument().isRequired(),
					"", sub.getName().getComparableForm(), type, null, subNames);
			//hasSubs.add(outputRepresentation);
		}else{
			ArrayList<String> subNames = new ArrayList<String>();
			ArrayList<MDERepresentation> hasSubSubs = new ArrayList<MDERepresentation>();
			for (OwlService subsub : graph.getSuccessors(sub)) {
				
				hasSubSubs.add(addSubtypes(subsub,graph, hasSubSubs));
				
				subNames.add(subsub.getName().getComparableForm());
			}
			
			outputRepresentation = new MDERepresentation(false, !sub.getArgument().isRequired(),
					"", sub.getName().getComparableForm(), type, hasSubSubs, subNames);
			//hasSubs.add(outputRepresentation);
		}
		return outputRepresentation;
		
		
		
	
	
	
}
	
	private static void addOutputs(MDERepresentation output,  LinkedOntologyAPI linkedOntology){
		if (output.getHasElements()==null){
			linkedOntology.addOutputParameter(output.getHasName(), output.getIsType(), output.getHasPrimitiveElements());
			
		}else{
		for(MDERepresentation sub: output.getHasElements()){
			
			addOutputs(sub,linkedOntology);
		}
		linkedOntology.addOutputParameter(output.getHasName(), output.getIsType(), output.getHasPrimitiveElements());
		}
	}
}
