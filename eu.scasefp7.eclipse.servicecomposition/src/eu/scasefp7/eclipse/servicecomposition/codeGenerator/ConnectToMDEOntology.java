package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.scasefp7.eclipse.core.ontology.LinkedOntologyAPI;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.RequestHeader;
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
	static String[] datatypes = new String[] { "string", "long", "int", "float", "double", "dateTime", "boolean" };

	public ConnectToMDEOntology() {

	}

	public MDEOperation createObjects(String projectName, ArrayList<OwlService> inputs,
			ArrayList<Argument> uriParameters, ArrayList<Argument> authParameters, ArrayList<RequestHeader> requestHeaderParameters, ArrayList<OwlService> outputs, ArrayList<Operation> repeatedOperations,
			String crudVerb, final Graph<OwlService, Connector> graph) {
		ArrayList<MDERepresentation> hasQueryParameters = new ArrayList<MDERepresentation>();
		ArrayList<MDERepresentation> hasInput = new ArrayList<MDERepresentation>();
		ArrayList<MDERepresentation> hasURIParameters = new ArrayList<MDERepresentation>();

		ArrayList<MDERepresentation> hasOutput = new ArrayList<MDERepresentation>();
		// Query, URI and Input Parameters
		// removed .replaceAll("[0123456789]", "")
		for (OwlService input : inputs) {
			if (!input.getisMatchedIO()) {
				String url = "";
				if (((Operation) input.getArgument().getBelongsToOperation()) != null) {
					url = ((Operation) input.getArgument().getBelongsToOperation()).getDomain().getURI();
					if (((Operation) input.getArgument().getBelongsToOperation()).getDomain()
							.getResourcePath() != null) {
						url = url + ((Operation) input.getArgument().getBelongsToOperation()).getDomain()
								.getResourcePath();
					}
				}
				String type = "Primitive";
				if (input.getArgument().isArray()) {
					type = "Array";
				} else if (!input.getArgument().isArray()
						&& !RAMLCaller.stringIsItemFromList(input.getArgument().getType(), datatypes)) {
					type = "Object";
				}

				if (input.getArgument().getSubtypes().isEmpty()
						&& input.getArgument().isTypeOf().equals("QueryParameter")) {
					ArrayList<String> subNames = new ArrayList<String>();
					subNames.add(input.getArgument().getType().toLowerCase());
					MDERepresentation inputRepresentation = new MDERepresentation(false,
							!input.getArgument().isRequired(), url, input.getName().getJavaValidContent(), type, null, subNames);
					hasQueryParameters.add(inputRepresentation);
				} else if (input.getArgument().getSubtypes().isEmpty()
						&& input.getArgument().isTypeOf().equals("URIParameter")) {
					ArrayList<String> subNames = new ArrayList<String>();
					subNames.add("string");
					MDERepresentation inputRepresentation = new MDERepresentation(false,
							!input.getArgument().isRequired(), url, input.getArgument().getName().getJavaValidContent(),
							"Primitive", null, subNames);
					hasQueryParameters.add(inputRepresentation);
				} else {

					ArrayList<String> subNames = new ArrayList<String>();
					ArrayList<MDERepresentation> hasSubs = new ArrayList<MDERepresentation>();

					for (OwlService sub : graph.getPredecessors(input)) {
						if (!sub.getisMatchedIO()) {
							hasSubs.add(addSubtypes(sub, graph, hasSubs, true));
							subNames.add(sub.getName().getJavaValidContent());
						}
					}
					MDERepresentation inputRepresentation;
					if (input.getArgument().getSubtypes().isEmpty()) {
						subNames.add(input.getArgument().getType().toLowerCase());
						inputRepresentation = new MDERepresentation(false, !input.getArgument().isRequired(), url,
								input.getName().getJavaValidContent(), type, null, subNames);
					} else {
						inputRepresentation = new MDERepresentation(false, !input.getArgument().isRequired(), url,
								input.getName().getJavaValidContent(), type, hasSubs, subNames);
					}
					hasInput.add(inputRepresentation);
				}
			}
		}
		
		for (Argument auth :authParameters){
			String url = "";
			if (((Operation) auth.getBelongsToOperation()) != null) {
				url = ((Operation) auth.getBelongsToOperation()).getDomain().getURI();
				if (((Operation) auth.getBelongsToOperation()).getDomain()
						.getResourcePath() != null) {
					url = url + ((Operation) auth.getBelongsToOperation()).getDomain()
							.getResourcePath();
				}
			}
			
			ArrayList<String> subNames = new ArrayList<String>();
			subNames.add(auth.getType().toLowerCase());
			MDERepresentation inputRepresentation = new MDERepresentation(true,
					false, url, auth.getName().getJavaValidContent().toLowerCase(), "Primitive", null, subNames);
			hasQueryParameters.add(inputRepresentation);
		}
		
		for (RequestHeader header : requestHeaderParameters){
			String url = "";
			if (((Operation) header.getBelongsToOperation()) != null) {
				url = ((Operation) header.getBelongsToOperation()).getDomain().getURI();
				if (((Operation) header.getBelongsToOperation()).getDomain()
						.getResourcePath() != null) {
					url = url + ((Operation) header.getBelongsToOperation()).getDomain()
							.getResourcePath();
				}
			}
			
			ArrayList<String> subNames = new ArrayList<String>();
			subNames.add("string");
			MDERepresentation inputRepresentation = new MDERepresentation(true,
					false, url, header.getName().replaceAll("[^A-Za-z0-9()_\\[\\]]", ""), "Primitive", null, subNames);
			hasQueryParameters.add(inputRepresentation);
		}

		// for (OwlService uriParameter : inputs) {
		// String url = ((Operation)
		// uriParameter.getArgument().getBelongsToOperation()).getDomain().getURI();
		// if (((Operation)
		// uriParameter.getArgument().getBelongsToOperation()).getDomain().getResourcePath()
		// != null) {
		// url = url + ((Operation)
		// uriParameter.getArgument().getBelongsToOperation()).getDomain().getResourcePath();
		// }
		// ArrayList<String> subNames = new ArrayList<String>();
		// subNames.add("string");
		// MDERepresentation inputRepresentation = new MDERepresentation(false,
		// !uriParameter.getArgument().isRequired(), url,
		// uriParameter.getArgument().getName().getComparableForm(),
		// "Primitive", null, subNames);
		// hasURIParameters.add(inputRepresentation);
		// }

		// Outputs
		// removed .replaceAll("[0123456789]", "")
		for (OwlService output : outputs) {
			if (!repeatedOperations.contains(output.getArgument().getBelongsToOperation())) {
				String url = "";
				if (((Operation) output.getArgument().getBelongsToOperation()) != null)
					url = ((Operation) output.getArgument().getBelongsToOperation()).getDomain().getURI();
				String type = "Primitive";
				if (output.getArgument().isArray()) {
					type = "Array";
				} else if (!output.getArgument().isArray()
						&& !RAMLCaller.stringIsItemFromList(output.getArgument().getType(), datatypes)) {
					type = "Object";
				}
				if (output.getArgument().getSubtypes().isEmpty()) {
					ArrayList<String> subNames = new ArrayList<String>();
					subNames.add(output.getArgument().getType().toLowerCase());
					MDERepresentation outputRepresentation = new MDERepresentation(false,
							!output.getArgument().isRequired(), url, output.getName().getJavaValidContent(), type, null,
							subNames);
					hasOutput.add(outputRepresentation);
				} else {
					ArrayList<String> subNames = new ArrayList<String>();
					ArrayList<MDERepresentation> hasSubs = new ArrayList<MDERepresentation>();
					for (OwlService sub : graph.getSuccessors(output)) {
						hasSubs.add(addSubtypes(sub, graph, hasSubs, false));
						subNames.add(sub.getName().getJavaValidContent());
					}
					MDERepresentation outputRepresentation = new MDERepresentation(false,
							!output.getArgument().isRequired(), url, output.getName().getJavaValidContent(), type, hasSubs,
							subNames);
					hasOutput.add(outputRepresentation);

				}
			}
		}

		for (Operation op : repeatedOperations) {
			String url = op.getDomain().getURI();
			ArrayList<String> operationSubNames = new ArrayList<String>();
			ArrayList<MDERepresentation> operationHasSubs = new ArrayList<MDERepresentation>();
			for (OwlService output : outputs) {
				if (output.getArgument().getBelongsToOperation().equals(op)) {
					String type = "Primitive";
					if (output.getArgument().isArray()) {
						type = "Array";
					} else if (!output.getArgument().isArray()
							&& !RAMLCaller.stringIsItemFromList(output.getArgument().getType(), datatypes)) {
						type = "Object";
					}
					MDERepresentation outputRepresentation;
					if (output.getArgument().getSubtypes().isEmpty()) {
						ArrayList<String> subNames = new ArrayList<String>();
						subNames.add(output.getArgument().getType().toLowerCase());
						outputRepresentation = new MDERepresentation(false, !output.getArgument().isRequired(), url,
								output.getName().getJavaValidContent(), type, null, subNames);
					} else {
						ArrayList<String> subNames = new ArrayList<String>();
						ArrayList<MDERepresentation> hasSubs = new ArrayList<MDERepresentation>();
						for (OwlService sub : graph.getSuccessors(output)) {
							hasSubs.add(addSubtypes(sub, graph, hasSubs, false));
							subNames.add(sub.getName().getJavaValidContent());
						}
						outputRepresentation = new MDERepresentation(false, !output.getArgument().isRequired(), url,
								output.getName().getJavaValidContent(), type, hasSubs, subNames);

					}
					operationHasSubs.add(outputRepresentation);
					operationSubNames.add(output.getName().getJavaValidContent());
				}
			}

			MDERepresentation outputRepresentation2 = new MDERepresentation(false, false, url,
					op.getName().getJavaValidContent().toLowerCase() + "_response", "Array", operationHasSubs,
					operationSubNames);
			hasOutput.add(outputRepresentation2);

		}

		ArrayList<String> hasResponseType = new ArrayList<String>();
		hasResponseType.add("JSON");
		hasResponseType.add("XML");
		String url = "http://109.231.122.51:8080/" + projectName + "-0.0.1-SNAPSHOT/";
		operation = new MDEOperation(url, "RESTful", crudVerb, projectName, "rest/result/query", hasResponseType,
				hasURIParameters, hasInput, hasOutput, hasQueryParameters);

		return operation;

	}

	/**
	 * This function creates the MDEOperation from the xml (.cservice) file in
	 * order to be written in linked ontology.
	 * 
	 * @return
	 */
	public static MDEOperation createMDEOperation(Document doc) {
		ArrayList<MDERepresentation> hasQueryParameters = new ArrayList<MDERepresentation>();
		ArrayList<MDERepresentation> hasInput = new ArrayList<MDERepresentation>();
		ArrayList<MDERepresentation> hasURIParameters = new ArrayList<MDERepresentation>();
		ArrayList<MDERepresentation> hasOutput = new ArrayList<MDERepresentation>();

		// parse ProjectName
		Node root = doc.getDocumentElement();
		String projectName = root.getAttributes().getNamedItem("name").getNodeValue();

		NodeList operationList = doc.getElementsByTagName("operation");
		NodeList queryList = doc.getElementsByTagName("queryParameters");
		NodeList inputList = doc.getElementsByTagName("hasInput");
		NodeList outputList = doc.getElementsByTagName("hasOutput");
		String name = "";
		String belongsToURL = "";
		String hasResourcePath = "";
		String BelongsToWSType = "";
		String CRUDVerb = "";
		ArrayList<String> ResponseType = new ArrayList<String>();

		for (int temp = 0; temp < operationList.getLength(); temp++) {

			Node nNode = operationList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				name = eElement.getAttribute("name");
				belongsToURL = eElement.getAttribute("belongsToURL");
				hasResourcePath = eElement.getAttribute("hasResourcePath");
				BelongsToWSType = eElement.getAttribute("BelongsToWSType");
				CRUDVerb = eElement.getAttribute("CRUDVerb");
				ResponseType.add(eElement.getAttribute("ResponseType"));

			}
		}
		// parse query parameters

		for (int temp2 = 0; temp2 < queryList.getLength(); temp2++) {
			Node nNode2 = queryList.item(temp2);

			if (nNode2.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement2 = (Element) nNode2;
				if (nNode2.hasChildNodes()) {
					NodeList elementsList = nNode2.getChildNodes();
					for (int temp3 = 0; temp3 < elementsList.getLength(); temp3++) {
						Node elementNode = elementsList.item(temp3);
						if (elementNode.getNodeType() == Node.ELEMENT_NODE) {
							ArrayList<String> subNames = new ArrayList<String>();
							Element hasElement = (Element) elementNode;
							subNames.add(hasElement.getElementsByTagName("hasElements").item(0).getTextContent());

							MDERepresentation inputRepresentation = new MDERepresentation(
									Boolean.parseBoolean(hasElement.getAttribute("isAuthToken")),
									Boolean.parseBoolean(hasElement.getAttribute("isOptional")),
									hasElement.getAttribute("belongsToURL"), hasElement.getNodeName(),
									hasElement.getAttribute("type"), null, subNames);
							hasQueryParameters.add(inputRepresentation);
						}
					}
				}

			}
		}

		// parse inputs

		for (int temp4 = 0; temp4 < inputList.getLength(); temp4++) {
			Node inputNode = inputList.item(temp4);
			ArrayList<String> subNames = new ArrayList<String>();
			if (inputNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement2 = (Element) inputNode;
				if (inputNode.hasChildNodes()) {
					NodeList elementsList = inputNode.getChildNodes();
					for (int temp5 = 0; temp5 < elementsList.getLength(); temp5++) {
						Node elementNode = elementsList.item(temp5);
						if (elementNode.getNodeType() == Node.ELEMENT_NODE) {
							ArrayList<MDERepresentation> subElements = getSubElements(elementNode);

							subNames.add(subElements.get(0).getHasName());

							hasInput.add(subElements.get(0));
						}
					}
				}

			}
		}

		// parse outputs
		for (int temp6 = 0; temp6 < outputList.getLength(); temp6++) {
			Node nNode3 = outputList.item(temp6);
			ArrayList<String> subNames = new ArrayList<String>();
			if (nNode3.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement2 = (Element) nNode3;
				if (nNode3.hasChildNodes()) {
					NodeList elementsList = nNode3.getChildNodes();
					for (int temp7 = 0; temp7 < elementsList.getLength(); temp7++) {
						Node elementNode = elementsList.item(temp7);
						if (elementNode.getNodeType() == Node.ELEMENT_NODE) {
							ArrayList<MDERepresentation> subElements = getSubElements(elementNode);

							subNames.add(subElements.get(0).getHasName());

							hasOutput.add(subElements.get(0));
						}
					}
				}

			}
		}

		MDEOperation operation = new MDEOperation(belongsToURL, BelongsToWSType, CRUDVerb, projectName, hasResourcePath,
				ResponseType, hasURIParameters, hasInput, hasOutput, hasQueryParameters);
		;
		return operation;
	}

	private static ArrayList<MDERepresentation> getSubElements(Node parentNode) {

		ArrayList<MDERepresentation> elements = new ArrayList<MDERepresentation>();
		ArrayList<MDERepresentation> subElements = new ArrayList<MDERepresentation>();
		// points
		if (parentNode.hasChildNodes()) {
			NodeList elementsList = parentNode.getChildNodes();
			for (int temp = 1; temp < elementsList.getLength(); temp++) {
				// has elements
				Node hasElementNode = elementsList.item(temp);
				if (hasElementNode.getNodeType() == Node.ELEMENT_NODE) {
					if (hasElementNode.hasChildNodes()) {
						NodeList eList = hasElementNode.getChildNodes();
						for (int temp2 = 0; temp2 < eList.getLength(); temp2++) {
							// pointId,pointName
							Node elementNode = eList.item(temp2);
							if (elementNode.getNodeType() == Node.ELEMENT_NODE) {
								ArrayList<MDERepresentation> subsubElements = new ArrayList<MDERepresentation>();

								if (elementNode.hasChildNodes()) {
									subsubElements = getSubElements(elementNode);
								}

								subElements.add(subsubElements.get(0));

							} else if (RAMLCaller.stringContainsItemFromList(elementNode.getNodeValue(), datatypes)) {
								ArrayList<String> subsubNames = new ArrayList<String>();
								Element hasElement = (Element) hasElementNode;
								subsubNames.add(elementNode.getNodeValue());
								MDERepresentation representation = new MDERepresentation(
										Boolean.parseBoolean(((Element) parentNode).getAttribute("isAuthToken")),
										Boolean.parseBoolean(((Element) parentNode).getAttribute("isOptional")),
										((Element) parentNode).getAttribute("belongsToURL"), parentNode.getNodeName(),
										((Element) parentNode).getAttribute("type"), null, subsubNames);
								subElements.add(representation);
								return subElements;
							}
						}
					}
					ArrayList<String> subNames = new ArrayList<String>();
					for (MDERepresentation rep : subElements) {
						subNames.add(rep.getHasName());
					}
					MDERepresentation representation;

					representation = new MDERepresentation(
							Boolean.parseBoolean(((Element) parentNode).getAttribute("isAuthToken")),
							Boolean.parseBoolean(((Element) parentNode).getAttribute("isOptional")),
							((Element) parentNode).getAttribute("belongsToURL"), ((Element) parentNode).getNodeName(),
							((Element) parentNode).getAttribute("type"), subElements, subNames);

					elements.add(representation);
				}
			}
		}

		return elements;
	}

	public static void writeToOntology(IProject project, MDEOperation operation) {

		// Create a new file for the linked ontology and instantiate it
		LinkedOntologyAPI linkedOntology = new LinkedOntologyAPI(project, false);

		// Add a new resource in the linked ontology

		linkedOntology.addResource(operation.getHasName() + "_Resource", true);
		linkedOntology.connectProjectToElement(operation.getHasName() + "_Resource");

		// Add a new operation for the resource
		linkedOntology.addOperationToResource(operation.getHasName() + "_Resource", operation.getHasName(),
				operation.getBelongsToURL(), operation.gethasResourcePath(), operation.getBelongsToWSType(),
				operation.getHasCRUDVerb(), operation.getHasResponseType().get(0));

		// Add the query parameters of the operation

		if (operation.getHasQueryParameters() != null) {
			ArrayList<MDERepresentation> queryParams = operation.getHasQueryParameters();
			List<String> queryNames = new ArrayList<String>();
			for (MDERepresentation queryParam : queryParams) {
				if (queryParam.getHasElements() == null) {
					linkedOntology.addInputParameter(queryParam.getHasName(), queryParam.getIsType(),
							queryParam.isAuthToken(), queryParam.getBelongsToURL(), queryParam.isOptional(),
							queryParam.getHasPrimitiveElements());
					queryNames.add(queryParam.getHasName());
				} else {
					List<String> subNames = new ArrayList<String>();
					for (MDERepresentation sub : queryParam.getHasElements()) {
						subNames.add(sub.getHasName());
					}
					linkedOntology.addInputParameter(queryParam.getHasName(), queryParam.getIsType(),
							queryParam.isAuthToken(), queryParam.getBelongsToURL(), queryParam.isOptional(), subNames);
				}
			}
			linkedOntology.addQueryParametersToOperation(operation.getHasName(), queryNames);
		}

		// Add the input parameters of the operation
		if (operation.getHasInput() != null) {
			List<String> inputNames = new ArrayList<String>();
			for (MDERepresentation input : operation.getHasInput()) {

				addInputs(input, linkedOntology);
				inputNames.add(input.getHasName());

			}
			linkedOntology.addInputParametersToOperation(operation.getHasName(), inputNames);
		}

		// Add the output parameters of the operation
		if (operation.getHasOutput() != null) {
			List<String> outputNames = new ArrayList<String>();
			for (MDERepresentation output : operation.getHasOutput()) {

				addOutputs(output, linkedOntology);
				outputNames.add(output.getHasName());

			}
			linkedOntology.addOutputParametersToOperation(operation.getHasName(), outputNames);
		}

		// Close the linked ontology. The other two ontologies are not closed
		// since they do not need to be saved.
		linkedOntology.close();
	}

	public static void writeToXMLFile(IProject project, MDEOperation operation, IContainer container) {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("project");
			doc.appendChild(rootElement);

			// set attributes to project element
			Attr name = doc.createAttribute("name");
			name.setValue(operation.getHasName());
			rootElement.setAttributeNode(name);

			createElement(doc, rootElement, "resource", operation.getHasName() + "_Resource");

			// operation
			Element MDEoperation = doc.createElement("operation");
			rootElement.appendChild(MDEoperation);

			createAttribute(doc, MDEoperation, "name", operation.getHasName());

			createAttribute(doc, MDEoperation, "belongsToURL", operation.getBelongsToURL());

			createAttribute(doc, MDEoperation, "hasResourcePath", operation.gethasResourcePath());

			createAttribute(doc, MDEoperation, "BelongsToWSType", operation.getBelongsToWSType());

			createAttribute(doc, MDEoperation, "CRUDVerb", operation.getHasCRUDVerb());

			createAttribute(doc, MDEoperation, "ResponseType", operation.getHasResponseType().get(0));

			// query params
			if (operation.getHasQueryParameters() != null) {
				Element queryParameters = doc.createElement("queryParameters");
				MDEoperation.appendChild(queryParameters);
				ArrayList<MDERepresentation> queryParams = operation.getHasQueryParameters();

				for (MDERepresentation queryParam : queryParams) {
					if (queryParam.getHasElements() == null) {
						Element query = doc.createElement(queryParam.getHasName().replaceAll("\\s+", ""));
						queryParameters.appendChild(query);
						createAttribute(doc, query, "type", queryParam.getIsType());
						createAttribute(doc, query, "isAuthToken", Boolean.toString(queryParam.isAuthToken()));
						createAttribute(doc, query, "belongsToURL", queryParam.getBelongsToURL());
						createAttribute(doc, query, "isOptional", Boolean.toString(queryParam.isOptional()));
						for (String element : queryParam.getHasPrimitiveElements()) {
							createElement(doc, query, "hasElements", element);
						}

					}
				}

			}
			// inputs
			if (operation.getHasInput() != null) {
				Element hasInput = doc.createElement("hasInput");
				MDEoperation.appendChild(hasInput);

				for (MDERepresentation input : operation.getHasInput()) {

					addSubElements(input, hasInput, doc);

				}
			}

			// outputs
			if (operation.getHasOutput() != null) {
				Element hasOutput = doc.createElement("hasOutput");
				MDEoperation.appendChild(hasOutput);

				for (MDERepresentation output : operation.getHasOutput()) {

					addSubElements(output, hasOutput, doc);

				}
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(baos);

			// Output to console for testing
			// StreamResult result2 = new StreamResult(System.out);

			transformer.transform(source, result);
			IFile file = container.getFile(new Path(operation.getHasName() + ".cservice"));
			if (!file.exists())
				file.create(new ByteArrayInputStream(baos.toByteArray()), true, null);
			else
				file.setContents(new ByteArrayInputStream(baos.toByteArray()), true, false, null);

			System.out.println(".cservice file generated!");

		} catch (ParserConfigurationException | TransformerException | CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void createElement(Document doc, Element parent, String childName, String value) {

		Element child = doc.createElement(childName);
		child.appendChild(doc.createTextNode(value));
		parent.appendChild(child);
	}

	private static void createAttribute(Document doc, Element parent, String childName, String value) {
		Attr child = doc.createAttribute(childName);
		child.setValue(value);
		parent.setAttributeNode(child);
	}

	private MDERepresentation addSubtypes(OwlService sub, final Graph<OwlService, Connector> graph,
			ArrayList<MDERepresentation> hasSubs, boolean isInput) {

		String type = "Primitive";
		if (sub.getArgument().isArray()) {
			type = "Array";
		} else if (!sub.getArgument().isArray()
				&& !RAMLCaller.stringIsItemFromList(sub.getArgument().getType(), datatypes)) {
			type = "Object";
		}
		MDERepresentation outputRepresentation = new MDERepresentation();
		if (sub.getArgument().getSubtypes().isEmpty()) {
			ArrayList<String> subNames = new ArrayList<String>();
			subNames.add(sub.getArgument().getType().toLowerCase());
			outputRepresentation = new MDERepresentation(false, !sub.getArgument().isRequired(), "",
					sub.getName().getJavaValidContent().replaceAll("[0123456789]", ""), type, null, subNames);
			// hasSubs.add(outputRepresentation);
		} else {
			ArrayList<String> subNames = new ArrayList<String>();
			ArrayList<MDERepresentation> hasSubSubs = new ArrayList<MDERepresentation>();
			if (isInput) {
				for (OwlService subsub : graph.getPredecessors(sub)) {
					if (!subsub.getisMatchedIO()) {
						hasSubSubs.add(addSubtypes(subsub, graph, hasSubSubs, true));

						subNames.add(subsub.getName().getJavaValidContent().replaceAll("[0123456789]", ""));
					}
				}
			} else {
				for (OwlService subsub : graph.getSuccessors(sub)) {

					hasSubSubs.add(addSubtypes(subsub, graph, hasSubSubs, false));

					subNames.add(subsub.getName().getJavaValidContent().replaceAll("[0123456789]", ""));
				}
			}

			outputRepresentation = new MDERepresentation(false, !sub.getArgument().isRequired(), "",
					sub.getName().getJavaValidContent().replaceAll("[0123456789]", ""), type, hasSubSubs, subNames);
			// hasSubs.add(outputRepresentation);
		}
		return outputRepresentation;

	}

	private static void addOutputs(MDERepresentation output, LinkedOntologyAPI linkedOntology) {
		if (output.getHasElements() == null) {
			linkedOntology.addOutputParameter(output.getHasName(), output.getIsType(),
					output.getHasPrimitiveElements());

		} else {
			for (MDERepresentation sub : output.getHasElements()) {

				addOutputs(sub, linkedOntology);
			}
			linkedOntology.addOutputParameter(output.getHasName(), output.getIsType(),
					output.getHasPrimitiveElements());
		}
	}

	private static void addInputs(MDERepresentation input, LinkedOntologyAPI linkedOntology) {
		if (input.getHasElements() == null) {
			linkedOntology.addInputParameter(input.getHasName(), input.getIsType(), input.isAuthToken(),
					input.getBelongsToURL(), input.isOptional(), input.getHasPrimitiveElements());

		} else {
			for (MDERepresentation sub : input.getHasElements()) {

				addInputs(sub, linkedOntology);
			}
			linkedOntology.addInputParameter(input.getHasName(), input.getIsType(), input.isAuthToken(),
					input.getBelongsToURL(), input.isOptional(), input.getHasPrimitiveElements());
		}
	}

	private static void addSubElements(MDERepresentation var, Element parent, Document doc) {

		Element varElement = doc.createElement(var.getHasName().replaceAll("\\s+", ""));
		parent.appendChild(varElement);

		if (var.getHasElements() == null) {
			for (String element : var.getHasPrimitiveElements()) {
				createElement(doc, varElement, "hasElements", element);
			}
		} else {
			Element hasElements = doc.createElement("hasElements");
			varElement.appendChild(hasElements);
			for (MDERepresentation sub : var.getHasElements()) {

				addSubElements(sub, hasElements, doc);
			}

		}

		createAttribute(doc, varElement, "type", var.getIsType());
		createAttribute(doc, varElement, "isAuthToken", Boolean.toString(var.isAuthToken()));
		createAttribute(doc, varElement, "belongsToURL", var.getBelongsToURL());
		createAttribute(doc, varElement, "isOptional", Boolean.toString(var.isOptional()));

	}
}
