package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;

import eu.scasefp7.eclipse.core.ontology.LinkedOntologyAPI;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;

public class ConnectToMDEOntology {

	MDEOperation operation;

	ConnectToMDEOntology() {

	}

	public MDEOperation createObjects(String projectName, ArrayList<OwlService> inputs,
			ArrayList<Argument> uriParameters, ArrayList<OwlService> resultVariables) {
		ArrayList<MDERepresentation> hasQueryParameters = new ArrayList<MDERepresentation>();
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
			}
			if (input.getArgument().getSubtypes().isEmpty()) {
				ArrayList<String> subNames = new ArrayList<String>();
				subNames.add("string");
				MDERepresentation inputRepresentation = new MDERepresentation(false, !input.getArgument().isRequired(),
						url, input.getName().toString(), type, subNames);
				hasQueryParameters.add(inputRepresentation);
			}

			else {
				ArrayList<String> subNames = new ArrayList<String>();
				for (Argument sub : input.getArgument().getSubtypes()) {
					subNames.add(sub.getName().getContent().toString());
				}
				MDERepresentation inputRepresentation = new MDERepresentation(false, !input.getArgument().isRequired(),
						url, input.getName().toString(), type, subNames);
				hasQueryParameters.add(inputRepresentation);
			}
		}

		for (Argument uriParameter : uriParameters) {
			String url = ((Operation) uriParameter.getBelongsToOperation()).getDomain().getURI();
			if (((Operation) uriParameter.getBelongsToOperation()).getDomain().getResourcePath() != null) {
				url = url + ((Operation) uriParameter.getBelongsToOperation()).getDomain().getResourcePath();
			}
			ArrayList<String> subNames = new ArrayList<String>();
			subNames.add("string");
			MDERepresentation inputRepresentation = new MDERepresentation(false, !uriParameter.isRequired(), url,
					uriParameter.getName().getContent().toString(), "Primitive", subNames);
			hasQueryParameters.add(inputRepresentation);
		}

		// Outputs
		for (OwlService output : resultVariables) {
			String url = ((Operation) output.getArgument().getBelongsToOperation()).getDomain().getURI();
			String type = "Primitive";
			if (output.getArgument().isArray()) {
				type = "Array";
			}
			if (output.getArgument().getSubtypes().isEmpty()) {
				ArrayList<String> subNames = new ArrayList<String>();
				subNames.add("string");
				MDERepresentation inputRepresentation = new MDERepresentation(false, !output.getArgument().isRequired(),
						url, output.getName().toString(), type, subNames);
				hasOutput.add(inputRepresentation);
			} else {
				ArrayList<String> subNames = new ArrayList<String>();
				for (Argument sub : output.getArgument().getSubtypes()) {
					subNames.add(sub.getName().getContent().toString());
				}
				MDERepresentation inputRepresentation = new MDERepresentation(false, !output.getArgument().isRequired(),
						url, output.getName().toString(), type, subNames);
				hasOutput.add(inputRepresentation);
			}
		}

		ArrayList<String> hasResponseType = new ArrayList<String>();
		hasResponseType.add("JSON");
		hasResponseType.add("SOAP");
		String url = "http://109.231.126.106:8080/" + projectName + "-0.0.1-SNAPSHOT/";
		operation = new MDEOperation(url, "RESTful", "GET", projectName, "rest/result/query", hasResponseType, null,
				null, hasOutput, hasQueryParameters);

		return operation;

	}

	public static void writeToOntology(IProject project, MDEOperation operation) {

		// Create a new file for the linked ontology and instantiate it
		LinkedOntologyAPI linkedOntology = new LinkedOntologyAPI(project);

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
				linkedOntology.addInputParameter(queryParam.getHasName(), queryParam.getIsType(),
						queryParam.isAuthToken(), queryParam.getBelongsToURL(), queryParam.isOptional(),
						queryParam.getHasElements());
				queryNames.add(queryParam.getHasName());
			}
			linkedOntology.addQueryParametersToOperation(operation.getHasName(), queryNames);
		}

		// Add the output parameters of the operation
		if (operation.getHasOutput() != null) {
			List<String> outputNames = new ArrayList<String>();
			for (MDERepresentation output : operation.getHasOutput()) {

				linkedOntology.addOutputParameter(output.getHasName(), output.getIsType(), output.getHasElements());
				outputNames.add(output.getHasName());
			}
			linkedOntology.addOutputParametersToOperation(operation.getHasName(), outputNames);
		}

		// Close the linked ontology. The other two ontologies are not closed
		// since they do not need to be saved.
		linkedOntology.close();
	}
}
