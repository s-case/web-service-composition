package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

import eu.scasefp7.eclipse.servicecomposition.codeInterpreter.Value;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Service;
import eu.scasefp7.eclipse.servicecomposition.operationCaller.RAMLCaller;
import eu.scasefp7.eclipse.servicecomposition.repository.WSOntology;
import eu.scasefp7.eclipse.servicecomposition.toolbar.RunWorkflow;
import eu.scasefp7.eclipse.servicecomposition.transformer.PathFinding;
import eu.scasefp7.eclipse.servicecomposition.views.ServiceCompositionView;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * <h1>NonLinearCodeGenerator</h1> This class is used for generating non-linear
 * code. The way to actually implement individual service calls is
 * parameterized, defining different types of generated code, according to
 * packagedependences.
 * 
 * @author Manios Krasanakis, mkoutli
 *
 * @param <FunctionCodeNodeType>
 *            : an instance of <code>FunctionCodeName</code> to generate
 *            individual service calls
 */
public class NonLinearCodeGenerator extends CodeGenerator {

	// initialize variable lists
	protected ArrayList<OwlService> inputVariables = new ArrayList<OwlService>();
	protected ArrayList<OwlService> outputVariables = new ArrayList<OwlService>();
	protected ArrayList<OwlService> subOutputVariables = new ArrayList<OwlService>();
	protected ArrayList<OwlService> matchedIO = new ArrayList<OwlService>();
	protected ArrayList<OwlService> matchedInputs = new ArrayList<OwlService>();
	protected ArrayList<OwlService> matchedOutputs = new ArrayList<OwlService>();
	protected ArrayList<Argument> uriParameters = new ArrayList<Argument>();
	protected ArrayList<Argument> authParameters = new ArrayList<Argument>();
	protected ArrayList<OwlService> inputsWithoutMatchedVariables = new ArrayList<OwlService>();
	protected ArrayList<OwlService> nativeInputsMatchedWithArrays = new ArrayList<OwlService>();
	protected ArrayList<Operation> repeatedOperations = new ArrayList<Operation>();
	protected ArrayList<OwlService> outputVariables2 = new ArrayList<OwlService>();

	/**
	 * instance of ConnectToMDEOntology
	 */
	protected ConnectToMDEOntology instance = new ConnectToMDEOntology();
	/**
	 * operation to be written in MDE ontology
	 */
	protected MDEOperation operation;

	/**
	 * primitive datatypes
	 */
	String[] datatypes = new String[] { "string", "long", "int", "float", "double", "dateTime", "boolean" };

	HashMap<OwlService, OwlService> map = new HashMap<OwlService, OwlService>();

	boolean hasDeserializer = false;

	public String generateCode(final Graph<OwlService, Connector> jungGraph, String functionName,
			boolean addConnectionsToGraph, String ProjectName) throws Exception {

		Graph<OwlService, Connector> graph = new SparseMultigraph<OwlService, Connector>();

		for (OwlService v : jungGraph.getVertices()) {
			int tempId = v.getId();
			boolean tempIsMatchedIO = v.getisMatchedIO();
			OwlService tempService = v.clone(v, tempId, tempIsMatchedIO);
			map.put(v, tempService);
			graph.addVertex(tempService);
		}

		for (Connector e : jungGraph.getEdges()) {
			OwlService tempSource = map.get(e.getSource());
			OwlService tempTarget = map.get(e.getTarget());
			String tempCondition = e.getCondition();
			Connector tempConnector = e.clone(tempSource, tempTarget, tempCondition);
			graph.addEdge(tempConnector, tempSource, tempTarget, EdgeType.DIRECTED);
		}

		for (OwlService service : graph.getVertices()) {
			for (OwlService service2 : graph.getVertices()) {
				if (service.getName().getJavaValidContent().toString()
						.equals(service2.getName().getJavaValidContent().toString()) && !service.equals(service2)
						&& service.getName().getJavaValidContent().toString() != "") {
					service2.getName()
							.setContent(service.getName().getJavaValidContent().toString() + service2.getId());
				}
			}
		}

		// detect all variables
		ArrayList<OwlService> allVariables = new ArrayList<OwlService>();

		for (OwlService service : graph.getVertices()) {
			if (service.getType().equalsIgnoreCase("Property")) {

				if (!allVariables.contains(service))
					allVariables.add(service);
			}

		}

		// detect all operations
		// check if mailgun operation exists in the workflow
		boolean hasMailgun = false;
		ArrayList<OwlService> remainingOperations = new ArrayList<OwlService>();
		ArrayList<String> authURL = new ArrayList<String>();
		for (OwlService service : graph.getVertices()) {
			if (service.getOperation() != null) {
				remainingOperations.add(service);
				if (service.getOperation().getDomain().getURI().contains("mailgun")) {
					hasMailgun = true;
				}
				if (!service.getOperation().getAuthenticationParameters().isEmpty()) {
					for (Argument arg : service.getOperation().getAuthenticationParameters()) {
						if (!authURL.contains(service.getOperation().getDomain().getURI()))
							authParameters.add(arg);
					}
					authURL.add(service.getOperation().getDomain().getURI());
				}
			}

		}
		// detect starting service
		OwlService startingService = null;
		for (OwlService service : graph.getVertices()) {
			if (service.getType().trim().equals("StartNode")) {
				startingService = service;
				break;
			}
		}
		if (startingService == null)
			throw new Exception("StartNode was not detected on graph vertices");

		// detect first operation
		for (OwlService service : graph.getSuccessors(startingService)) {
			if (startingService.getArgument() == null) {
				startingService = service;
				break;
			}
		}

		// detect output variables
		for (OwlService service : graph.getVertices()) {
			if (service.getOperation() != null) {
				Collection<OwlService> successors = (Collection<OwlService>) graph.getSuccessors(service);
				for (OwlService successor : successors) {
					if (successor.getArgument() != null) {
						outputVariables.add(successor);
					}
				}
			}
		}
		// detect input variables
		for (OwlService service : graph.getVertices()) {
			if (service.getOperation() != null) {
				Collection<OwlService> predecessors = (Collection<OwlService>) graph.getPredecessors(service);
				for (OwlService predecessor : predecessors) {
					if (predecessor.getArgument() != null) {
						inputVariables.add(predecessor);
					}
				}
			}
		}
		boolean hasBodyInput = false;
		for (OwlService input : inputVariables) {
			if (input.getArgument().isTypeOf().equals("BodyParameter")) {
				hasBodyInput = true;
			}
		}

		boolean hasOutput = true;
		if (outputVariables.isEmpty()) {
			hasOutput = false;
		}

		// detect matched io
		for (OwlService service : graph.getVertices()) {
			if (service.getArgument() != null) {
				if (graph.getOutEdges(service).size() > 0) {

					Collection<OwlService> successors = (Collection<OwlService>) graph.getSuccessors(service);
					boolean matched = false;

					for (OwlService successor : successors) {
						if (successor.getisMatchedIO() && service.getisMatchedIO()) {
							matchedInputs.add(successor);
							matched = true;
						}

					}
					if (matched) {
						matchedOutputs.add(service);

					}

				}
			}
		}
		// detect inputs that are matched with arrays but do not belong to
		// arrays
		for (OwlService service : matchedInputs) {
			if (!service.getArgument().isArray()) {
				Collection<OwlService> predecessors = (Collection<OwlService>) graph.getPredecessors(service);
				boolean outputIsMemberOfArray = false;
				boolean inputIsMemberOfArray = false;
				for (OwlService predecessor : predecessors) {
					if (predecessor.getArgument() != null && predecessor.getisMatchedIO()) {
						for (Object parent : predecessor.getArgument().getParent()) {
							if (parent instanceof Argument)
								if (((Argument) parent).isArray()) {
									outputIsMemberOfArray = true;
								}
						}
					}
				}
				for (Object parent : service.getArgument().getParent()) {
					if (parent instanceof Argument)
						if (((Argument) parent).isArray()) {
							inputIsMemberOfArray = true;
						}
				}
				if (!inputIsMemberOfArray && outputIsMemberOfArray) {
					nativeInputsMatchedWithArrays.add(service);
					if (!repeatedOperations.contains(service.getArgument().getBelongsToOperation()))
						repeatedOperations.add(service.getArgument().getBelongsToOperation());
				}
			}
		}

		// detect input variables
		// for (OwlService service : graph.getVertices()) {
		// if (service.getArgument() != null) {
		// if (graph.getInEdges(service).size() == 0) {
		// if (!service.getisMatchedIO() && !inputVariables.contains(service)) {
		// inputVariables.add(service);
		// }
		// }
		// }
		// }

		// detect uri parameters
		for (OwlService service : graph.getVertices()) {
			if (service.getOperation() != null) {
				for (Argument param : service.getOperation().getInputs()) {
					if (param.isTypeOf().equals("URIParameter") && !uriParameters.contains(param)) {
						uriParameters.add(param);
					}
				}
			}
		}
		// generate function code for all services
		String functionCode = "";
		// create variable declarations
		String declaredVariables = "";
		boolean wsdlServiceExists = false;
		boolean restServiceExists = false;
		for (OwlService service : graph.getVertices()) {
			if (service.getArgument() == null) {

				if (service.getOperation() != null) {
					boolean isRepeated = false;
					if (repeatedOperations.contains(service.getOperation())) {
						isRepeated = true;
					}
					if (service.getOperation().getType().equalsIgnoreCase("RESTful")) {
						CodeNode code = RestFunctionCodeNode.createInstance(service, this);
						code.applyTab();
						functionCode += code.createFunctionCode(graph, allVariables, hasBodyInput, isRepeated,
								hasOutput);
						// for (Argument arg : uriParameters) {
						// declaredVariables += TAB + "private Variable " +
						// arg.getName().getJavaValidContent().toString()
						// + " = new Variable(\"" +
						// arg.getName().getJavaValidContent().replaceAll("[0123456789]",
						// "")
						// + "\",\"\", \"" + arg.getType() + "\");\n";
						// }
						restServiceExists = true;
					} else if (service.getOperation().getType().equalsIgnoreCase("SOAP")
							|| service.getOperation().getDomain().isLocal()) {
						CodeNode code = FunctionCodeNode.createInstance(service, this);
						code.applyTab();
						functionCode += code.createFunctionCode(graph, allVariables, false, false, hasOutput);
						wsdlServiceExists = true;
					}
				} else {
					CodeNode code = FunctionCodeNode.createInstance(service, this);
					code.applyTab();
					functionCode += code.createFunctionCode(graph, allVariables, false, false, hasOutput);
				}

			}
		}
		// create class name
		String createdClassName = functionName.substring(0, 1).toUpperCase() + functionName.substring(1) + "Class";

		for (OwlService service : allVariables) {
			if (inputVariables.contains(service) || uriParameters.contains(service.getArgument())
					|| service.getArgument().getBelongsToOperation().getType().equalsIgnoreCase("SOAP")
					|| matchedInputs.contains(service)) {

				if (nativeInputsMatchedWithArrays.contains(service)) {
					declaredVariables += TAB + "private ArrayList<Variable> "
							+ service.getName().getJavaValidContent().toString() + " = new ArrayList<Variable>();\n";
					declaredVariables += TAB + "private Variable " + service.getName().getJavaValidContent().toString()
							+ "_num = new Variable(\"" + service.getName().getJavaValidContent().toString()
							+ "_num\", \"\", \"int\");\n";
				} else {
					String type = service.getArgument().getType();
					if (service.getArgument().getSubtypes().isEmpty() && type.equals("object")) {
						type = "String";
					}
					if (service.getArgument().getSubtypes().isEmpty() && !service.getArgument().isArray()) {
						declaredVariables += TAB + "private Variable "
								+ service.getName().getJavaValidContent().toString() + " = new Variable(\""
								+ service.getName().getJavaValidContent().replaceAll("[0123456789]", "") + "\",\"\", \""
								+ type + "\");\n";
					} else if (service.getArgument().getSubtypes().isEmpty() && service.getArgument().isArray()) {
						declaredVariables += TAB + "private Variable "
								+ service.getName().getJavaValidContent().toString() + " = new Variable(\""
								+ service.getName().getJavaValidContent().replaceAll("[0123456789]", "")
								+ "\",\"\", \"array\");\n";
					} else if (!service.getArgument().getSubtypes().isEmpty() && service.getArgument().isArray()) {
						declaredVariables += TAB + "private Variable "
								+ service.getName().getJavaValidContent().toString() + " = new Variable(\""
								+ service.getName().getJavaValidContent().replaceAll("[0123456789]", "")
								+ "\",\"\", \"arrayOfObjects\");\n";
					} else {
						declaredVariables += TAB + "private Variable "
								+ service.getName().getJavaValidContent().toString() + " = new Variable(\""
								+ service.getName().getJavaValidContent().replaceAll("[0123456789]", "")
								+ "\",\"\", \"object\");\n";
					}
				}
			}

		}

		for (Argument arg : authParameters) {
			declaredVariables += TAB + "private Variable " + arg.getName().getJavaValidContent().toLowerCase()
					+ " = new Variable(\"" + arg.getName().getJavaValidContent().toLowerCase() + "\",\"\", \""
					+ arg.getType() + "\");\n";
		}

		ArrayList<OwlService> requestVariables = new ArrayList<OwlService>();
		for (OwlService input : inputVariables) {
			requestVariables.add(input);
		}
		String requestClassDeclaration = "";
		if (hasBodyInput) {
			for (OwlService input : inputVariables) {
				boolean existsInOutputs = false;

				if (!input.getArgument().getSubtypes().isEmpty()) {
					for (OwlService output : outputVariables) {
						if ((output.getName().getComparableForm().equals(input.getName().getComparableForm())
								&& output.getArgument().getType().equals(input.getArgument().getType()))) {
							int subs = 0;
							for (Argument subIn : input.getArgument().getSubtypes()) {
								boolean hasSub = false;
								for (Argument subOut : output.getArgument().getSubtypes()) {
									if ((subOut.getName().getComparableForm()
											.equals(subIn.getName().getComparableForm())
											&& subOut.getType().equals(subIn.getType()))) {
										hasSub = true;
										break;
									}
								}
								if (hasSub) {
									subs++;
								}
							}
							if (subs == output.getArgument().getSubtypes().size()) {
								existsInOutputs = true;
								requestVariables.remove(input);
								break;
							}
						}
					}
					// if (!existsInOutputs) {
					// requestClassDeclaration +=
					// generateResultObjects(input.getArgument(), classObjects);
					// }
				}
			}

		}
		// for (Argument param : uriParameters) {
		// if (!declaredInputs.isEmpty())
		// declaredInputs += ", ";
		// if (param.getType() == "") {
		// declaredInputs += "String " + param.getName().getJavaValidContent();
		// } else {
		// declaredInputs += param.getType() + " " +
		// param.getName().getJavaValidContent();
		// }
		// }

		// create result class instance
		String resultClassName = "void";
		String resultClassDeclaration = "";
		String resultObjectDeclaration = "";
		ArrayList<OwlService> resultVariables = new ArrayList<OwlService>();
		for (OwlService output : outputVariables) {
			resultVariables.add(output);
		}
		for (OwlService output : outputVariables) {
			// boolean existsInOutputs = false;

			if (!output.getArgument().getSubtypes().isEmpty()) {
				for (OwlService input : resultVariables) {
					if ((input.getName().getComparableForm().equals(output.getName().getComparableForm())
							&& input.getArgument().getType().equals(output.getArgument().getType()))
							&& !input.equals(output)) {
						int subs = 0;
						for (Argument subIn : input.getArgument().getSubtypes()) {
							boolean hasSub = false;
							for (Argument subOut : output.getArgument().getSubtypes()) {
								if ((subOut.getName().getComparableForm().equals(subIn.getName().getComparableForm())
										&& subOut.getType().equals(subIn.getType()))) {
									hasSub = true;
									break;
								}
							}
							if (hasSub) {
								subs++;
							}
						}
						if (subs == output.getArgument().getSubtypes().size()) {

							resultVariables.remove(output);
							break;
						}
					}
				}
				// if (!existsInOutputs ) {
				// requestClassDeclaration +=
				// generateResultObjects(output.getArgument());

				// }
			}

		}

		ArrayList<OwlService> classObjects = new ArrayList<OwlService>();
		for (OwlService obj : graph.getVertices()) {
			if (obj.getArgument() != null && !obj.getArgument().getSubtypes().isEmpty()
					&& !classObjects.contains(obj)) {
				classObjects.add(obj);
			}
		}
		// for (OwlService output : resultVariables) {
		// classObjects.add(output);
		// }
		// for (OwlService input : requestVariables) {
		// classObjects.add(input);
		// }

		ArrayList<String> classNames = new ArrayList<String>();
		for (OwlService output : resultVariables) {
			resultClassDeclaration += generateResultObjects(output, classObjects, classNames, graph, false);
		}
		for (OwlService input : requestVariables) {
			requestClassDeclaration += generateResultObjects(input, classObjects, classNames, graph, true);
		}

		/////////// create Response/Request for EACH operation
		String operationResponseObjects = "";
		String operationRequestObjects = "";
		String constractorInstanceInputVars = "";

		for (OwlService op : remainingOperations) {
			// if (op.getOperation().getType().equalsIgnoreCase("RESTful")) {
			declaredVariables += TAB + "private " + op.getName().getJavaValidContent() + "Response "
					+ op.getName().getJavaValidContent() + "_response = new " + op.getName().getJavaValidContent()
					+ "Response();\n";
			if (repeatedOperations.contains(op.getOperation())) {
				declaredVariables += TAB + "private ArrayList<" + op.getName().getJavaValidContent() + "Response> "
						+ op.getName().getJavaValidContent() + "_response_arraylist = new ArrayList<"
						+ op.getName().getJavaValidContent() + "Response>();\n";

			}
			if (hasBodyInput)
				declaredVariables += TAB + "private " + op.getName().getJavaValidContent() + "Request "
						+ op.getName().getJavaValidContent() + "_request = new " + op.getName().getJavaValidContent()
						+ "Request();\n";
			// }
			if (op.getType().equals("SOAP")) {

				operationResponseObjects += TAB + TAB + "@XmlRootElement\n" + TAB + TAB
						+ "@XmlAccessorType(XmlAccessType.FIELD)\n";
			}
			operationResponseObjects += TAB + TAB + "public static class " + op.getName().getJavaValidContent()
					+ "Response {\n";

			String VarDeclaration = "";
			String getSet = "\n\n";
			String RequestVarDeclaration = "";
			String RequestgetSet = "\n\n";
			for (Argument output : op.getOperation().getOutputs()) {
				String subType = output.getType();
				String subName = output.getName().getJavaValidContent().replaceAll("[0123456789]", "");
				OwlService outputOwl = null;
				for (OwlService arg : allVariables) {
					if (output.getName().toString().equals(arg.getName().toString().replaceAll("[0123456789]", ""))
							&& output.getOwlService().getId() == arg.getId()) {

						outputOwl = arg;
					}
				}
				if (!output.getSubtypes().isEmpty()) {
					if (classNames.contains(outputOwl.getName().getJavaValidContent())) {
						subType = outputOwl.getName().getJavaValidContent();
						subName = subType;
					}
				} else {
					// avoid non existing java primitive type object
					if (subType.equals("object")) {
						subType = "String";
					}
				}
				if (output.getSubtypes().isEmpty() && !output.isArray()) {
					VarDeclaration += TAB + TAB + TAB + "private " + subType + " " + subName + " ;\n";

					getSet += TAB + TAB + "public void set" + subName + "(" + subType + " " + subName + ") {\n" + TAB
							+ TAB + TAB + "this." + subName + " = " + subName + ";\n" + TAB + TAB + "}\n";
					if (!output.getBelongsToOperation().getType().equals("SOAP"))
						getSet += TAB + TAB + "@XmlElement\n";
					getSet += TAB + TAB + "public " + subType + " " + "get" + subName + "() {\n" + TAB + TAB + TAB
							+ "return " + subName + ";\n" + TAB + TAB + "}\n";
				} else if (output.isArray() && RAMLCaller.stringContainsItemFromList(output.getType(), datatypes)) {

					if (subType.equals("int")) {
						subType = "Integer";
					} else {
						subType = subType.substring(0, 1).toUpperCase() + subType.substring(1);
					}
					VarDeclaration += TAB + TAB + TAB + "private ArrayList <" + subType + "> " + subName
							+ "= new ArrayList<" + subType + ">();\n";

					getSet += TAB + TAB + "public void set" + subName + "(ArrayList<" + subType + "> " + subName
							+ ") {\n" + TAB + TAB + TAB + "this." + subName + " = " + subName + ";\n" + TAB + TAB
							+ "}\n";
					if (!output.getBelongsToOperation().getType().equals("SOAP"))
						getSet += TAB + TAB + "@XmlElement\n";
					getSet += TAB + TAB + "public ArrayList<" + subType + "> get" + subName + "() {\n" + TAB + TAB + TAB
							+ "return " + subName + ";\n" + TAB + TAB + "}\n";
				} else if (output.isArray() && !RAMLCaller.stringContainsItemFromList(output.getType(), datatypes)) {
					// name as in the operation, type might get an id
					VarDeclaration += TAB + TAB + TAB + "private ArrayList <" + subType.substring(0, 1).toUpperCase()
							+ subType.substring(1) + "> "
							+ output.getName().getJavaValidContent().replaceAll("[0123456789]", "")
							+ " = new ArrayList<" + subType.substring(0, 1).toUpperCase() + subType.substring(1)
							+ ">();\n";

					getSet += TAB + TAB + "public void set" + subName + "(ArrayList<"
							+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + "> " + subName + ") {\n"
							+ TAB + TAB + TAB + "this."
							+ output.getName().getJavaValidContent().replaceAll("[0123456789]", "") + " = " + subName
							+ ";\n" + TAB + TAB + "}\n";
					if (!output.getBelongsToOperation().getType().equals("SOAP"))
						getSet += TAB + TAB + "@XmlElement\n";
					getSet += TAB + TAB + "public ArrayList<" + subType.substring(0, 1).toUpperCase()
							+ subType.substring(1) + "> get" + subName + "() {\n" + TAB + TAB + TAB + "return "
							+ output.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB
							+ "}\n";
				} else if (!output.isArray() && !output.getSubtypes().isEmpty()) {
					VarDeclaration += TAB + TAB + TAB + "private " + subType.substring(0, 1).toUpperCase()
							+ subType.substring(1) + " "
							+ output.getName().getJavaValidContent().replaceAll("[0123456789]", "") + " = new "
							+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + "();\n";

					getSet += TAB + TAB + "public void set" + subName + "(" + subType.substring(0, 1).toUpperCase()
							+ subType.substring(1) + " " + subName + ") {\n" + TAB + TAB + TAB + "this."
							+ output.getName().getJavaValidContent().replaceAll("[0123456789]", "") + " = " + subName
							+ ";\n" + TAB + TAB + "}\n";
					if (!output.getBelongsToOperation().getType().equals("SOAP"))
						getSet += TAB + TAB + "@XmlElement\n";
					getSet += TAB + TAB + "public " + subType.substring(0, 1).toUpperCase() + subType.substring(1)
							+ " get" + subName + "() {\n" + TAB + TAB + TAB + "return "
							+ output.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB
							+ "}\n";
				}

			}

			String operationRequestConstructor0 = TAB + TAB + TAB + "public " + op.getName().getJavaValidContent()
					+ "Request(){" + "\n" + TAB + TAB + TAB + "}\n";
			String operationRequestConstructor = TAB + TAB + TAB + "public " + op.getName().getJavaValidContent()
					+ "Request(";
			String operationRequestConstructorVars = "";
			String operationRequestConstractorVarsBody = "";
			String name = op.getOperation().getName().getJavaValidContent();
			constractorInstanceInputVars += TAB + TAB + name + "_request" + " = new " + name + "Request(";
			String vars = "";
			for (Argument input : op.getOperation().getInputs()) {
				if (input.isTypeOf().equals("BodyParameter")) {
					// request instance of each operation
					if (!vars.isEmpty()) {
						vars += ", ";
					}
					OwlService inputOwl = null;
					for (OwlService arg : allVariables) {
						if (input.getName().toString().equals(arg.getName().toString().replaceAll("[0123456789]", ""))
								&& input.getOwlService().getId() == arg.getId()) {
							vars += "request.get" + arg.getName().getJavaValidContent() + "()";
							inputOwl = arg;
						}
					}

					String subType = input.getType();
					String subName = input.getName().getJavaValidContent().replaceAll("[0123456789]", "");
					if (!input.getSubtypes().isEmpty()) {
						if (classNames.contains(inputOwl.getName().getJavaValidContent())) {
							subType = inputOwl.getName().getJavaValidContent();
							subName = subType;
						}
					} else {
						// avoid non existing java primitive type object
						if (subType.equals("object")) {
							subType = "String";
						}
					}
					if (!operationRequestConstructorVars.isEmpty()) {
						operationRequestConstructorVars += ", ";
					}
					operationRequestConstractorVarsBody += TAB + TAB + TAB + TAB + "this."
							+ input.getName().getJavaValidContent().replaceAll("[0123456789]", "") + " = " + subName
							+ ";\n";
					if (input.getSubtypes().isEmpty() && !input.isArray()) {
						operationRequestConstructorVars += subType + " " + subName;
						RequestVarDeclaration += TAB + TAB + TAB + "private " + subType + " " + subName + ";\n";

						RequestgetSet += TAB + TAB + "public void set" + subName + "(" + subType + " " + subName
								+ ") {\n" + TAB + TAB + TAB + "this." + subName + " = " + subName + ";\n" + TAB + TAB
								+ "}\n";
						RequestgetSet += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public " + subType + " get"
								+ subName + "() {\n" + TAB + TAB + TAB + "return " + subName + ";\n" + TAB + TAB
								+ "}\n";
					} else if (input.isArray() && RAMLCaller.stringContainsItemFromList(input.getType(), datatypes)) {
						if (subType.equals("int")) {
							subType = "Integer";
						} else {
							subType = subType.substring(0, 1).toUpperCase() + subType.substring(1);
						}
						operationRequestConstructorVars += "ArrayList <" + subType + "> " + subName;
						RequestVarDeclaration += TAB + TAB + TAB + "private ArrayList <" + subType + "> " + subName
								+ "= new ArrayList<" + subType + ">();\n";

						RequestgetSet += TAB + TAB + "public void set" + subName + "(ArrayList<" + subType + "> "
								+ subName + ") {\n" + TAB + TAB + TAB + "this." + subName + " = " + subName + ";\n"
								+ TAB + TAB + "}\n";
						RequestgetSet += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public ArrayList<" + subType
								+ "> get" + subName + "() {\n" + TAB + TAB + TAB + "return " + subName + ";\n" + TAB
								+ TAB + "}\n";
					} else if (input.isArray() && !RAMLCaller.stringContainsItemFromList(input.getType(), datatypes)) {
						operationRequestConstructorVars += "ArrayList <" + subType.substring(0, 1).toUpperCase()
								+ subType.substring(1) + "> " + subName;
						RequestVarDeclaration += TAB + TAB + TAB + "private ArrayList <"
								+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + "> "
								+ input.getName().getJavaValidContent().replaceAll("[0123456789]", "")
								+ " = new ArrayList<" + subType.substring(0, 1).toUpperCase() + subType.substring(1)
								+ ">();\n";

						RequestgetSet += TAB + TAB + "public void set" + subName + "(ArrayList<"
								+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + "> " + subName
								+ ") {\n" + TAB + TAB + TAB + "this."
								+ input.getName().getJavaValidContent().replaceAll("[0123456789]", "") + " = " + subName
								+ ";\n" + TAB + TAB + "}\n";
						RequestgetSet += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public ArrayList<"
								+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + "> get" + subName
								+ "() {\n" + TAB + TAB + TAB + "return "
								+ input.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ";\n" + TAB
								+ TAB + "}\n";
					} else if (!input.isArray() && !input.getSubtypes().isEmpty()) {
						operationRequestConstructorVars += subType.substring(0, 1).toUpperCase() + subType.substring(1)
								+ " " + subName;
						RequestVarDeclaration += TAB + TAB + TAB + "private " + subType.substring(0, 1).toUpperCase()
								+ subType.substring(1) + " "
								+ input.getName().getJavaValidContent().replaceAll("[0123456789]", "") + " = new "
								+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + "();\n";

						RequestgetSet += TAB + TAB + "public void set" + subName + "("
								+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + " " + subName + ") {\n"
								+ TAB + TAB + TAB + "this."
								+ input.getName().getJavaValidContent().replaceAll("[0123456789]", "") + " = " + subName
								+ ";\n" + TAB + TAB + "}\n";
						RequestgetSet += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public "
								+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + " get" + subName
								+ "() {\n" + TAB + TAB + TAB + "return "
								+ input.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ";\n" + TAB
								+ TAB + "}\n";
					}
				}

			}
			// operation request instance
			constractorInstanceInputVars += vars;
			constractorInstanceInputVars += ");\n";
			// response
			operationResponseObjects += VarDeclaration;
			operationResponseObjects += getSet;
			operationResponseObjects += TAB + TAB + "}\n";
			// request
			operationRequestObjects += TAB + TAB + "public static class " + op.getName().getJavaValidContent()
					+ "Request {\n";
			operationRequestObjects += RequestVarDeclaration + "\n\n";
			operationRequestObjects += operationRequestConstructor0;
			if (!operationRequestConstructorVars.isEmpty()) {
				operationRequestObjects += operationRequestConstructor + operationRequestConstructorVars + "){\n";
				operationRequestObjects += operationRequestConstractorVarsBody + "\n" + TAB + TAB + TAB + "}\n";
			}

			operationRequestObjects += RequestgetSet;
			operationRequestObjects += TAB + TAB + "}\n";
		}

		if (!declaredVariables.isEmpty())
			declaredVariables = TAB + "//variables\n" + declaredVariables;
		// create class constructor (assigns to variable declarations)
		String declaredInputs = "";
		for (OwlService input : inputVariables) {
			if ((input.getArgument().isTypeOf().equals("QueryParameter")
					|| input.getArgument().isTypeOf().equals("URIParameter")
					|| input.getArgument().isTypeOf().equals("")) && !input.getisMatchedIO()) {
				if (!declaredInputs.isEmpty())
					declaredInputs += ", ";
				if (input.getArgument().getType() == "") {
					declaredInputs += "String " + input.getName().getJavaValidContent();
				} else if (input.getArgument().getType().equals("int")) {
					declaredInputs += "Integer " + input.getName().getJavaValidContent();
				} else {
					declaredInputs += input.getArgument().getType().substring(0, 1).toUpperCase()
							+ input.getArgument().getType().substring(1) + " " + input.getName().getJavaValidContent();
				}
			}

		}
		for (Argument auth : authParameters) {
			if (!declaredInputs.isEmpty())
				declaredInputs += ", ";
			declaredInputs += "String " + auth.getName().getJavaValidContent().toLowerCase();
		}
		for (OwlService matchedInput : nativeInputsMatchedWithArrays) {
			if (!declaredInputs.isEmpty())
				declaredInputs += ", ";
			declaredInputs += "Integer " + matchedInput.getName().getJavaValidContent() + "_num";
		}

		if (hasBodyInput) {
			if (!declaredInputs.isEmpty())
				declaredInputs += ", ";
			declaredInputs += "Request request";
		}

		// create request class instance
		// ArrayList<Argument> classObjects = new ArrayList<Argument>();
		// ArrayList<OwlService> requestVariables = new ArrayList<OwlService>();
		// for (OwlService input : inputVariables) {
		// requestVariables.add(input);
		// }
		// String requestClassDeclaration = "";
		// if (hasBodyInput) {
		// for (OwlService input : inputVariables) {
		// boolean existsInOutputs = false;
		//
		// if (!input.getArgument().getSubtypes().isEmpty()) {
		// for (OwlService output : outputVariables) {
		// if
		// ((output.getName().getComparableForm().equals(input.getName().getComparableForm())
		// &&
		// output.getArgument().getType().equals(input.getArgument().getType())))
		// {
		// int subs = 0;
		// for (Argument subIn : input.getArgument().getSubtypes()) {
		// boolean hasSub = false;
		// for (Argument subOut : output.getArgument().getSubtypes()) {
		// if ((subOut.getName().getComparableForm()
		// .equals(subIn.getName().getComparableForm())
		// && subOut.getType().equals(subIn.getType()))) {
		// hasSub = true;
		// break;
		// }
		// }
		// if (hasSub) {
		// subs++;
		// }
		// }
		// if (subs == output.getArgument().getSubtypes().size()) {
		// existsInOutputs = true;
		// requestVariables.remove(input);
		// break;
		// }
		// }
		// }
		// // if (!existsInOutputs) {
		// // requestClassDeclaration +=
		// // generateResultObjects(input.getArgument(), classObjects);
		// // }
		// }
		// }
		//
		// }
		// // for (Argument param : uriParameters) {
		// // if (!declaredInputs.isEmpty())
		// // declaredInputs += ", ";
		// // if (param.getType() == "") {
		// // declaredInputs += "String " +
		// param.getName().getJavaValidContent();
		// // } else {
		// // declaredInputs += param.getType() + " " +
		// // param.getName().getJavaValidContent();
		// // }
		// // }
		//
		// // create result class instance
		// String resultClassName = "void";
		// String resultClassDeclaration = "";
		// String resultObjectDeclaration = "";
		// ArrayList<OwlService> resultVariables = new ArrayList<OwlService>();
		// for (OwlService output : outputVariables) {
		// resultVariables.add(output);
		// }
		// for (OwlService output : outputVariables) {
		// // boolean existsInOutputs = false;
		//
		// if (!output.getArgument().getSubtypes().isEmpty()) {
		// for (OwlService input : resultVariables) {
		// if
		// ((input.getName().getComparableForm().equals(output.getName().getComparableForm())
		// &&
		// input.getArgument().getType().equals(output.getArgument().getType()))
		// && !input.equals(output)) {
		// int subs = 0;
		// for (Argument subIn : input.getArgument().getSubtypes()) {
		// boolean hasSub = false;
		// for (Argument subOut : output.getArgument().getSubtypes()) {
		// if
		// ((subOut.getName().getComparableForm().equals(subIn.getName().getComparableForm())
		// && subOut.getType().equals(subIn.getType()))) {
		// hasSub = true;
		// break;
		// }
		// }
		// if (hasSub) {
		// subs++;
		// }
		// }
		// if (subs == output.getArgument().getSubtypes().size()) {
		//
		// resultVariables.remove(output);
		// break;
		// }
		// }
		// }
		// // if (!existsInOutputs ) {
		// // requestClassDeclaration +=
		// // generateResultObjects(output.getArgument());
		//
		// // }
		// }
		//
		// }
		// ArrayList<Argument> classObjects = new ArrayList<Argument>();
		// for (OwlService output : resultVariables) {
		// classObjects.add(output.getArgument());
		// }
		// for (OwlService input : requestVariables) {
		// classObjects.add(input.getArgument());
		// }
		// ArrayList<String> classNames = new ArrayList<String>();
		// for (OwlService output : resultVariables) {
		// requestClassDeclaration +=
		// generateResultObjects(output.getArgument(), classObjects,
		// classNames);
		// }
		// for (OwlService input : requestVariables) {
		// requestClassDeclaration += generateResultObjects(input.getArgument(),
		// classObjects, classNames);
		// }
		requestClassDeclaration += operationRequestObjects;
		resultClassDeclaration += operationResponseObjects;

		// if (outputVariables.size() == 1 && subOutputVariables.size() == 0) {
		// resultClassName = outputVariables.get(0).getArgument().getType();
		// resultObjectDeclaration = TAB + TAB + "return " +
		// outputVariables.get(0).getName().getJavaValidContent() + ";";
		if (!outputVariables.isEmpty()) {

			String resultObjectName = "response";

			resultClassName = "Response";
			String resultClassConstractor1 = TAB + TAB + "public Response() {\n";
			String resultClassConstractor2 = TAB + TAB + "public Response(";
			String constractorVars = "";
			String constractorVarsBody = "";
			String constractorInstanceVars = "";
			String resultClassInstance = TAB + TAB + resultClassName + " " + resultObjectName + " = new "
					+ resultClassName + "(";
			resultClassDeclaration += TAB + "@XmlRootElement(name = \"Response\")\n" + TAB + "public static class "
					+ resultClassName + "{\n";
			// remove .replaceAll("[0123456789]", "") for duplicate vars

			for (OwlService arg : outputVariables) {
				if (!repeatedOperations.contains(arg.getArgument().getBelongsToOperation())) {

					if (!constractorInstanceVars.isEmpty()) {
						constractorInstanceVars += ", ";
					}
					String name = "";
					if (!arg.getArgument().getSubtypes().isEmpty()
							&& classNames.contains(arg.getName().getJavaValidContent())) {
						name = arg.getName().getJavaValidContent();
					} else {
						name = arg.getName().getJavaValidContent().replaceAll("[0123456789]", "");
					}
					constractorInstanceVars += arg.getArgument().getBelongsToOperation().getName().getJavaValidContent()
							+ "_response.get" + name + "()";
					if (!constractorVars.isEmpty()) {
						constractorVars += ", ";
					}
					constractorVarsBody += TAB + TAB + TAB + "this." + arg.getName().getJavaValidContent() + " = "
							+ arg.getName().getJavaValidContent() + ";\n";
					String type = arg.getArgument().getType();
					if (!arg.getArgument().getSubtypes().isEmpty()) {
						if (classNames.contains(arg.getName().getJavaValidContent())) {
							type = arg.getName().getJavaValidContent();
						}
					}
					if (arg.getArgument().getSubtypes().isEmpty() && !arg.getArgument().isArray()) {
						constractorVars += "" + type + " " + arg.getName().getJavaValidContent();
						resultClassDeclaration += TAB + TAB + "private " + type + " "
								+ arg.getName().getJavaValidContent() + ";\n";
						// resultClassConstractor1 += TAB + TAB + TAB + "this."
						// +
						// arg.getName().getJavaValidContent().replaceAll("[0123456789]",
						// "") + ";\n";
					} else if (arg.getArgument().isArray()
							&& RAMLCaller.stringIsItemFromList(arg.getArgument().getType(), datatypes)) {

						if (type.equals("int")) {
							type = "Integer";
						} else {
							type = type.substring(0, 1).toUpperCase() + type.substring(1);
						}
						constractorVars += "ArrayList <" + type + "> " + arg.getName().getJavaValidContent();
						resultClassDeclaration += TAB + TAB + "private ArrayList <" + type + "> "
								+ arg.getName().getJavaValidContent() + " = new ArrayList<" + type + ">();\n";
						resultClassConstractor1 += TAB + TAB + TAB + "this." + arg.getName().getJavaValidContent()
								+ " = new ArrayList<" + type + ">();\n";
					} else if (arg.getArgument().isArray()
							&& !RAMLCaller.stringIsItemFromList(arg.getArgument().getType(), datatypes)) {

						constractorVars += "ArrayList <" + type.substring(0, 1).toUpperCase() + type.substring(1) + "> "
								+ arg.getName().getJavaValidContent();
						resultClassDeclaration += TAB + TAB + "private ArrayList <" + type.substring(0, 1).toUpperCase()
								+ type.substring(1) + "> " + arg.getName().getJavaValidContent() + " = new ArrayList<"
								+ type.substring(0, 1).toUpperCase() + type.substring(1) + ">();\n";
						resultClassConstractor1 += TAB + TAB + TAB + "this." + arg.getName().getJavaValidContent()
								+ " = new ArrayList<" + type.substring(0, 1).toUpperCase() + type.substring(1)
								+ ">();\n";

					} else if (!arg.getArgument().isArray() && !arg.getArgument().getSubtypes().isEmpty()) {

						constractorVars += type.substring(0, 1).toUpperCase() + type.substring(1) + " "
								+ arg.getName().getJavaValidContent();
						resultClassDeclaration += TAB + TAB + "private " + type.substring(0, 1).toUpperCase()
								+ type.substring(1) + " " + arg.getName().getJavaValidContent() + " = new "
								+ type.substring(0, 1).toUpperCase() + type.substring(1) + "();\n";
						resultClassConstractor1 += TAB + TAB + TAB + "this." + arg.getName().getJavaValidContent()
								+ " = new " + type.substring(0, 1).toUpperCase() + type.substring(1) + "();\n";
					}
					outputVariables2.add(arg);
				}
			}

			for (Operation op : repeatedOperations) {
				if (!constractorInstanceVars.isEmpty()) {
					constractorInstanceVars += ", ";
				}
				constractorInstanceVars += op.getName().getJavaValidContent() + "_response_arraylist";
				if (!constractorVars.isEmpty()) {
					constractorVars += ", ";
				}
				constractorVarsBody += TAB + TAB + TAB + "this." + op.getName().getJavaValidContent().toLowerCase()
						+ "_response" + " = " + op.getName().getJavaValidContent().toLowerCase() + "_response" + ";\n";
				constractorVars += "ArrayList <" + op.getName().getJavaValidContent() + "Response" + "> "
						+ op.getName().getJavaValidContent().toLowerCase() + "_response";
				resultClassDeclaration += TAB + TAB + "private ArrayList <" + op.getName().getJavaValidContent()
						+ "Response" + "> " + op.getName().getJavaValidContent().toLowerCase() + "_response"
						+ " = new ArrayList<" + op.getName().getJavaValidContent() + "Response" + ">();\n";
				resultClassConstractor1 += TAB + TAB + TAB + "this." + op.getName().getJavaValidContent().toLowerCase()
						+ "_response" + " = new ArrayList<" + op.getName().getJavaValidContent() + "Response"
						+ ">();\n";
				Argument arg = new Argument(op.getName().getJavaValidContent().toLowerCase() + "_response",
						op.getName().getJavaValidContent() + "_response", "", true, false, op.getOutputs());
				arg.setIsRequired(true);
				outputVariables2.add(new OwlService(arg));
			}

			resultClassDeclaration += "\n\n";
			resultClassConstractor1 += TAB + TAB + "}\n\n";
			resultClassDeclaration += resultClassConstractor1;
			resultClassConstractor2 += constractorVars;
			resultClassConstractor2 += ") {\n";
			resultClassConstractor2 += constractorVarsBody;
			resultClassConstractor2 += "}\n";
			resultClassDeclaration += resultClassConstractor2;
			resultClassInstance += constractorInstanceVars;
			resultClassInstance += ");\n";

			for (OwlService arg : outputVariables) {
				if (!repeatedOperations.contains(arg.getArgument().getBelongsToOperation())) {
					String type = arg.getArgument().getType();
					if (!arg.getArgument().getSubtypes().isEmpty()) {
						if (classNames.contains(arg.getName().getJavaValidContent())) {
							type = arg.getName().getJavaValidContent();
						}
					}
					if (arg.getArgument().getSubtypes().isEmpty() && !arg.getArgument().isArray()) {
						resultClassDeclaration += TAB + TAB + "public void set" + arg.getName().getJavaValidContent()
								+ "(" + type + " " + arg.getName().getJavaValidContent().replaceAll("[0123456789]", "")
								+ ") {\n" + TAB + TAB + TAB + "this." + arg.getName().getJavaValidContent() + " = "
								+ arg.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB
								+ "}\n";
						if (!arg.getArgument().getBelongsToOperation().getType().equals("SOAP"))
							resultClassDeclaration += TAB + TAB + "@XmlElement\n";
						resultClassDeclaration += TAB + TAB + "public " + type + " get"
								+ arg.getName().getJavaValidContent() + "() {\n" + TAB + TAB + TAB + "return "
								+ arg.getName().getJavaValidContent() + ";\n" + TAB + TAB + "}\n";
					} else if (arg.getArgument().isArray()
							&& RAMLCaller.stringIsItemFromList(arg.getArgument().getType(), datatypes)) {
						if (type.equals("int")) {
							type = "Integer";
						} else {
							type = type.substring(0, 1).toUpperCase() + type.substring(1);
						}
						resultClassDeclaration += TAB + TAB + "public void set" + arg.getName().getJavaValidContent()
								+ "(ArrayList<" + type + "> "
								+ arg.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ") {\n" + TAB
								+ TAB + TAB + "this." + arg.getName().getJavaValidContent() + " = "
								+ arg.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB
								+ "}\n";
						if (!arg.getArgument().getBelongsToOperation().getType().equals("SOAP"))
							resultClassDeclaration += TAB + TAB + "@XmlElement\n";
						resultClassDeclaration += TAB + TAB + "public ArrayList<" + type + "> get"
								+ arg.getName().getJavaValidContent() + "() {\n" + TAB + TAB + TAB + "return "
								+ arg.getName().getJavaValidContent() + ";\n" + TAB + TAB + "}\n";
					} else if (arg.getArgument().isArray()
							&& !RAMLCaller.stringIsItemFromList(arg.getArgument().getType(), datatypes)) {

						resultClassDeclaration += TAB + TAB + "public void set" + arg.getName().getJavaValidContent()
								+ "(ArrayList<" + type.substring(0, 1).toUpperCase() + type.substring(1) + "> "
								+ arg.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ") {\n" + TAB
								+ TAB + TAB + "this." + arg.getName().getJavaValidContent() + " = "
								+ arg.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB
								+ "}\n";
						if (!arg.getArgument().getBelongsToOperation().getType().equals("SOAP"))
							resultClassDeclaration += TAB + TAB + "@XmlElement\n";
						resultClassDeclaration += TAB + TAB + "public ArrayList<" + type.substring(0, 1).toUpperCase()
								+ type.substring(1) + "> get" + arg.getName().getJavaValidContent() + "() {\n" + TAB
								+ TAB + TAB + "return " + arg.getName().getJavaValidContent() + ";\n" + TAB + TAB
								+ "}\n";
					} else if (!arg.getArgument().isArray() && !arg.getArgument().getSubtypes().isEmpty()) {

						resultClassDeclaration += TAB + TAB + "public void set" + arg.getName().getJavaValidContent()
								+ "(" + type.substring(0, 1).toUpperCase() + type.substring(1) + " "
								+ arg.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ") {\n" + TAB
								+ TAB + TAB + "this." + arg.getName().getJavaValidContent() + " = "
								+ arg.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB
								+ "}\n";
						if (!arg.getArgument().getBelongsToOperation().getType().equals("SOAP"))
							resultClassDeclaration += TAB + TAB + "@XmlElement\n";
						resultClassDeclaration += TAB + TAB + "public " + type.substring(0, 1).toUpperCase()
								+ type.substring(1) + " get" + arg.getName().getJavaValidContent() + "() {\n" + TAB
								+ TAB + TAB + "return " + arg.getName().getJavaValidContent() + ";\n" + TAB + TAB
								+ "}\n";
					}
				}
			}
			for (Operation op : repeatedOperations) {
				resultClassDeclaration += TAB + TAB + "public void set"
						+ op.getName().getJavaValidContent().toLowerCase() + "_response" + "(ArrayList<"
						+ op.getName().getJavaValidContent() + "Response" + "> "
						+ op.getName().getJavaValidContent().toLowerCase() + "_response" + ") {\n" + TAB + TAB + TAB
						+ "this." + op.getName().getJavaValidContent().toLowerCase() + "_response" + " = "
						+ op.getName().getJavaValidContent().toLowerCase() + "_response" + ";\n" + TAB + TAB + "}\n";
				resultClassDeclaration += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public ArrayList<"
						+ op.getName().getJavaValidContent() + "Response" + "> get"
						+ op.getName().getJavaValidContent().toLowerCase() + "_response" + "() {\n" + TAB + TAB + TAB
						+ "return " + op.getName().getJavaValidContent().toLowerCase() + "_response" + ";\n" + TAB + TAB
						+ "}\n";
			}

			resultClassDeclaration += TAB + "}\n\n";

			resultObjectDeclaration = TAB + TAB + "//create class instance to be returned\n";
			resultObjectDeclaration += resultClassInstance;

			resultObjectDeclaration += TAB + TAB + "return " + resultObjectName + ";\n";
		} else {
			// class declaration
			resultClassName = "Response";
			resultClassDeclaration += TAB + "@XmlRootElement(name = \"Response\")\n" + TAB + "public static class "
					+ resultClassName + "{\n";
			resultClassDeclaration += TAB + TAB + "public Response() {\n";
			resultClassDeclaration += TAB + TAB + "}\n\n";
			resultClassDeclaration += TAB + "}\n\n";
			// object declaration
			resultObjectDeclaration += TAB + TAB + "//create class instance to be returned\n";
			resultObjectDeclaration += TAB + TAB + "Response response = new Response();\n";
			resultObjectDeclaration += TAB + TAB + "return response;\n";
		}
		String requestClassName = "void";
		String requestObjectDeclaration = "";

		if (!inputVariables.isEmpty()) {
			if (hasBodyInput) {
				String requestObjectName = "request";

				requestClassName = "Request";
				String requestClassConstractor1 = TAB + TAB + "public Request() {\n";
				String requestClassConstractor2 = TAB + TAB + "public Request(";
				String constractorVars = "";
				String constractorVarsBody = "";

				requestClassDeclaration += TAB + "@XmlRootElement(name = \"Request\")\n" + TAB + "public static class "
						+ requestClassName + "{\n";
				for (OwlService arg : inputVariables) {
					// String name
					// =arg.getArgument().getBelongsToOperation().getName().getJavaValidContent()
					// ;
					// constractorInstanceInputVars += TAB + TAB + name +
					// "_request"
					// ;
					// constractorInstanceInputVars+= " = new " + name +
					// "Request(request.get" +
					// arg.getName().toString().replaceAll("[0123456789]", "") +
					// "());\n";
					if (arg.getArgument().isTypeOf().equals("BodyParameter")) {
						if (!constractorVars.isEmpty()) {
							constractorVars += ", ";
						}
						constractorVarsBody += TAB + TAB + TAB + "this." + arg.getName().getJavaValidContent() + " = "
								+ arg.getName().getJavaValidContent() + ";\n";
						String type = arg.getArgument().getType();
						if (!arg.getArgument().getSubtypes().isEmpty()) {
							if (classNames.contains(arg.getName().getJavaValidContent())) {
								type = arg.getName().getJavaValidContent();
							}
						}
						if (arg.getArgument().getSubtypes().isEmpty() && !arg.getArgument().isArray()) {
							constractorVars += "" + type + " " + arg.getName().getJavaValidContent();
							requestClassDeclaration += TAB + TAB + "private " + type + " "
									+ arg.getName().getJavaValidContent() + ";\n";
							// requestClassConstractor1 += TAB + TAB + TAB +
							// "this."
							// +
							// arg.getName().getJavaValidContent().replaceAll("[0123456789]",
							// "") + ";\n";
						} else if (arg.getArgument().isArray()
								&& RAMLCaller.stringIsItemFromList(arg.getArgument().getType(), datatypes)) {
							if (type.equals("int")) {
								type = "Integer";
							} else {
								type = type.substring(0, 1).toUpperCase() + type.substring(1);
							}
							constractorVars += "ArrayList <" + type + "> " + arg.getName().getJavaValidContent();
							requestClassDeclaration += TAB + TAB + "private ArrayList <" + type + "> "
									+ arg.getName().getJavaValidContent() + " = new ArrayList<" + type + ">();\n";
							requestClassConstractor1 += TAB + TAB + TAB + "this." + arg.getName().getJavaValidContent()
									+ " = new ArrayList<" + type + ">();\n";
						} else if (arg.getArgument().isArray()
								&& !RAMLCaller.stringIsItemFromList(arg.getArgument().getType(), datatypes)) {
							constractorVars += "ArrayList <" + type.substring(0, 1).toUpperCase() + type.substring(1)
									+ "> " + arg.getName().getJavaValidContent();
							requestClassDeclaration += TAB + TAB + "private ArrayList <"
									+ type.substring(0, 1).toUpperCase() + type.substring(1) + "> "
									+ arg.getName().getJavaValidContent() + " = new ArrayList<"
									+ type.substring(0, 1).toUpperCase() + type.substring(1) + ">();\n";
							requestClassConstractor1 += TAB + TAB + TAB + "this." + arg.getName().getJavaValidContent()
									+ " = new ArrayList<" + type.substring(0, 1).toUpperCase() + type.substring(1)
									+ ">();\n";

						} else if (!arg.getArgument().isArray() && !arg.getArgument().getSubtypes().isEmpty()) {
							constractorVars += type.substring(0, 1).toUpperCase() + type.substring(1) + " "
									+ arg.getName().getJavaValidContent();
							requestClassDeclaration += TAB + TAB + "private " + type.substring(0, 1).toUpperCase()
									+ type.substring(1) + " " + arg.getName().getJavaValidContent() + " = new "
									+ type.substring(0, 1).toUpperCase() + type.substring(1) + "();\n";
							requestClassConstractor1 += TAB + TAB + TAB + "this." + arg.getName().getJavaValidContent()
									+ " = new " + type.substring(0, 1).toUpperCase() + type.substring(1) + "();\n";
						}
					}
				}
				String requestClassInstance = "";
				requestClassDeclaration += "\n\n";
				requestClassConstractor1 += TAB + TAB + "}\n\n";
				requestClassDeclaration += requestClassConstractor1;
				requestClassConstractor2 += constractorVars;
				requestClassConstractor2 += ") {\n";
				requestClassConstractor2 += constractorVarsBody;
				requestClassConstractor2 += "}\n";
				requestClassDeclaration += requestClassConstractor2;
				// requestClassInstance += constractorInstanceVars;
				// requestClassInstance += ");\n";

				for (OwlService arg : inputVariables) {
					if (arg.getArgument().isTypeOf().equals("BodyParameter")) {
						String type = arg.getArgument().getType();
						if (!arg.getArgument().getSubtypes().isEmpty()) {
							if (classNames.contains(arg.getName().getJavaValidContent())) {
								type = arg.getName().getJavaValidContent();
							}
						}
						if (arg.getArgument().getSubtypes().isEmpty() && !arg.getArgument().isArray()) {
							requestClassDeclaration += TAB + TAB + "public void set"
									+ arg.getName().getJavaValidContent() + "(" + type + " "
									+ arg.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ") {\n" + TAB
									+ TAB + TAB + "this." + arg.getName().getJavaValidContent() + " = "
									+ arg.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ";\n" + TAB
									+ TAB + "}\n";
							requestClassDeclaration += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public " + type
									+ " get" + arg.getName().getJavaValidContent() + "() {\n" + TAB + TAB + TAB
									+ "return " + arg.getName().getJavaValidContent() + ";\n" + TAB + TAB + "}\n";
						} else if (arg.getArgument().isArray()
								&& RAMLCaller.stringIsItemFromList(arg.getArgument().getType(), datatypes)) {
							if (type.equals("int")) {
								type = "Integer";
							} else {
								type = type.substring(0, 1).toUpperCase() + type.substring(1);
							}
							requestClassDeclaration += TAB + TAB + "public void set"
									+ arg.getName().getJavaValidContent() + "(ArrayList<" + type + "> "
									+ arg.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ") {\n" + TAB
									+ TAB + TAB + "this." + arg.getName().getJavaValidContent() + " = "
									+ arg.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ";\n" + TAB
									+ TAB + "}\n";
							requestClassDeclaration += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public ArrayList<"
									+ type + "> get" + arg.getName().getJavaValidContent() + "() {\n" + TAB + TAB + TAB
									+ "return " + arg.getName().getJavaValidContent() + ";\n" + TAB + TAB + "}\n";
						} else if (arg.getArgument().isArray()
								&& !RAMLCaller.stringIsItemFromList(arg.getArgument().getType(), datatypes)) {

							requestClassDeclaration += TAB + TAB + "public void set"
									+ arg.getName().getJavaValidContent() + "(ArrayList<"
									+ type.substring(0, 1).toUpperCase() + type.substring(1) + "> "
									+ arg.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ") {\n" + TAB
									+ TAB + TAB + "this." + arg.getName().getJavaValidContent() + " = "
									+ arg.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ";\n" + TAB
									+ TAB + "}\n";
							requestClassDeclaration += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public ArrayList<"
									+ type.substring(0, 1).toUpperCase() + type.substring(1) + "> get"
									+ arg.getName().getJavaValidContent() + "() {\n" + TAB + TAB + TAB + "return "
									+ arg.getName().getJavaValidContent() + ";\n" + TAB + TAB + "}\n";
						} else if (!arg.getArgument().isArray() && !arg.getArgument().getSubtypes().isEmpty()) {
							requestClassDeclaration += TAB + TAB + "public void set"
									+ arg.getName().getJavaValidContent() + "(" + type.substring(0, 1).toUpperCase()
									+ type.substring(1) + " "
									+ arg.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ") {\n" + TAB
									+ TAB + TAB + "this." + arg.getName().getJavaValidContent() + " = "
									+ arg.getName().getJavaValidContent().replaceAll("[0123456789]", "") + ";\n" + TAB
									+ TAB + "}\n";
							requestClassDeclaration += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public "
									+ type.substring(0, 1).toUpperCase() + type.substring(1) + " get"
									+ arg.getName().getJavaValidContent() + "() {\n" + TAB + TAB + TAB + "return "
									+ arg.getName().getJavaValidContent() + ";\n" + TAB + TAB + "}\n";
						}
					}
				}
				requestClassDeclaration += TAB + "}\n\n";

				requestObjectDeclaration = TAB + TAB + "//create class instance to be returned\n";
				requestObjectDeclaration += requestClassInstance;
			}
		}

		// create result class declaration
		declaredInputs = TAB + "public " + resultClassName + " " + "parseResponse" + "(" + declaredInputs
				+ ") throws Exception{\n";
		declaredInputs += TAB + TAB + "//assign inputs of services to variables\n";
		if (hasBodyInput)
			declaredInputs += constractorInstanceInputVars;
		for (OwlService arg : inputVariables) {
			if ((arg.getArgument().isTypeOf().equals("QueryParameter") || arg.getArgument().isTypeOf().equals(""))
					&& !arg.getisMatchedIO()) {
				String type = arg.getArgument().getType();

				declaredInputs += TAB + TAB + "if (" + arg.getName().getJavaValidContent() + " == null) {\n";
				declaredInputs += TAB + TAB + TAB + "this." + arg.getName().getJavaValidContent() + ".value"
						+ " =\"\";\n";
				declaredInputs += TAB + TAB + "} else {\n";
				if (type.equalsIgnoreCase("String")) {
					declaredInputs += TAB + TAB + TAB + "this." + arg.getName().getJavaValidContent() + ".value" + " = "
							+ arg.getName().getJavaValidContent() + ";\n";

				} else if (type.equalsIgnoreCase("int")) {
					declaredInputs += TAB + TAB + TAB + "this." + arg.getName().getJavaValidContent() + ".value"
							+ " = Integer.toString(" + arg.getName().getJavaValidContent() + ");\n";
				} else {
					declaredInputs += TAB + TAB + TAB + "this." + arg.getName().getJavaValidContent() + ".value" + " = "
							+ type.substring(0, 1).toUpperCase() + type.substring(1) + ".toString("
							+ arg.getName().getJavaValidContent() + ");\n";
				}
				declaredInputs += TAB + TAB + "}\n";
			}
		}
		for (Argument arg : authParameters) {
			declaredInputs += TAB + TAB + "if (" + arg.getName().getJavaValidContent().toLowerCase() + " == null) {\n";
			declaredInputs += TAB + TAB + TAB + "this." + arg.getName().getJavaValidContent().toLowerCase() + ".value"
					+ " =\"\";\n";
			declaredInputs += TAB + TAB + "} else {\n";
			declaredInputs += TAB + TAB + TAB + "this." + arg.getName().getJavaValidContent().toLowerCase() + ".value"
					+ " = " + arg.getName().getJavaValidContent().toLowerCase() + ";\n";
			declaredInputs += TAB + TAB + "}\n";
		}
		declaredInputs += TAB + TAB + "//assign uri parameters of services to variables\n";
		for (OwlService param : inputVariables) {
			if (param.getArgument().isTypeOf().equals("URIParameter") && !param.getisMatchedIO()) {
				declaredInputs += TAB + TAB + "if (" + param.getName().getJavaValidContent() + " == null) {\n";
				declaredInputs += TAB + TAB + TAB + "this." + param.getName().getJavaValidContent() + ".value"
						+ " =\"\";\n";
				declaredInputs += TAB + TAB + "} else {\n";
				declaredInputs += TAB + TAB + TAB + "this." + param.getName().getJavaValidContent() + ".value" + " = "
						+ param.getName().getJavaValidContent() + ";\n";
				declaredInputs += TAB + TAB + "}\n";
			}
		}
		declaredInputs += TAB + TAB + "//assign other variables\n";
		for (OwlService arg : nativeInputsMatchedWithArrays) {
			declaredInputs += TAB + TAB + "if (" + arg.getName().getJavaValidContent() + "_num" + " == null) {\n";
			declaredInputs += TAB + TAB + TAB + "this." + arg.getName().getJavaValidContent() + "_num" + ".value"
					+ " =\"\";\n";
			declaredInputs += TAB + TAB + "} else {\n";
			declaredInputs += TAB + TAB + TAB + "this." + arg.getName().getJavaValidContent() + "_num" + ".value"
					+ " = Integer.toString(" + arg.getName().getJavaValidContent() + "_num" + ");\n";
			declaredInputs += TAB + TAB + "}\n";
		}

		// Generate Response class of RESTful web service
		// String responseClass = TAB + "@XmlRootElement(name = \"Response\")\n"
		// + TAB + "public class Response {\n";
		// for (OwlService arg : outputVariables) {
		// responseClass += TAB + TAB + "private " + arg.getArgument().getType()
		// + " "
		// + arg.getName().getJavaValidContent().replaceAll("[0123456789]", "")
		// + ";\n";
		// }
		//
		// responseClass += TAB + "}\n";

		// Variable class declaration
		String variableClassDeclaration = TAB + "public static class Variable{\n";
		variableClassDeclaration += TAB + TAB + "public String name;\n" + TAB + TAB + "public String value;\n" + TAB
				+ TAB + "public String type;\n" + TAB + TAB
				+ "public ArrayList<Variable> subtypes=new ArrayList<Variable>();\n" + TAB + TAB
				+ "public ArrayList<Variable> arrayElements = new ArrayList<Variable>();\n" + TAB + TAB
				+ "Variable(String name, String value, String type){\n";
		variableClassDeclaration += TAB + TAB + TAB + "this.name = name;\n" + TAB + TAB + TAB + "this.value = value;\n"
				+ TAB + TAB + TAB + "this.type = type;\n";
		variableClassDeclaration += TAB + TAB + "}\n";
		variableClassDeclaration += TAB + TAB + "Variable(Variable prototype) {\n";
		variableClassDeclaration += TAB + TAB + TAB + "this.name = prototype.name;\n";
		variableClassDeclaration += TAB + TAB + TAB + "this.value = prototype.value;\n";
		variableClassDeclaration += TAB + TAB + TAB + "this.type = prototype.type;\n";
		variableClassDeclaration += TAB + TAB + TAB + "for (Variable sub : prototype.subtypes) {\n";
		variableClassDeclaration += TAB + TAB + TAB + TAB + "Variable arg = new Variable(sub);\n";
		variableClassDeclaration += TAB + TAB + TAB + TAB + "subtypes.add(arg);\n";
		variableClassDeclaration += TAB + TAB + TAB + "}\n";
		variableClassDeclaration += TAB + TAB + TAB + "for (Variable el : prototype.arrayElements) {\n";
		variableClassDeclaration += TAB + TAB + TAB + TAB + "Variable arg = new Variable(el);\n";
		variableClassDeclaration += TAB + TAB + TAB + TAB + "arrayElements.add(arg);\n";
		variableClassDeclaration += TAB + TAB + TAB + "}\n" + TAB + TAB + "}\n";
		variableClassDeclaration += TAB + TAB + "public Variable getSubtype(String name) {\n";
		variableClassDeclaration += TAB + TAB + TAB + "for (Variable sub : subtypes) {\n";
		variableClassDeclaration += TAB + TAB + TAB + TAB
				+ "if (sub.name.equals(name.replaceAll(\"[^A-Za-z]\", \"\"))) {\n";
		variableClassDeclaration += TAB + TAB + TAB + TAB + TAB + "return sub;\n";
		variableClassDeclaration += TAB + TAB + TAB + TAB + "}\n" + TAB + TAB + TAB + "}\n";
		variableClassDeclaration += TAB + TAB + TAB + "return null;\n";
		variableClassDeclaration += TAB + TAB + "}\n";
		variableClassDeclaration += TAB + "}\n";

		// call all services
		declaredInputs += TAB + TAB + "//call services iterately\n";
		declaredInputs += TAB + TAB + "String _nextCall = \"" + getFunctionName(startingService) + "\";\n";
		String call = "";
		String setPreviousCall = "";
		String increaseCallCounters = "";

		if (!repeatedOperations.isEmpty() || hasBodyInput) {
			declaredInputs += TAB + TAB + "String _previousCall = \"StartNode\";\n";
			setPreviousCall = TAB + TAB + TAB + "_previousCall = _nextCall;\n";
		}
		if (!repeatedOperations.isEmpty()) {
			// declaredInputs += TAB + TAB + "String _previousCall =
			// \"StartNode\";\n";
			// setPreviousCall = TAB + TAB + TAB + "_previousCall =
			// _nextCall;\n";
			for (Operation op : repeatedOperations) {
				declaredInputs += TAB + TAB + "int " + op.getName().getJavaValidContent() + "_num = 0;\n";
				call += ", " + op.getName().getJavaValidContent() + "_num";
				increaseCallCounters += TAB + TAB + TAB + "if (_previousCall.equals(\"call"
						+ op.getName().getJavaValidContent() + "\")) {\n";
				increaseCallCounters += TAB + TAB + TAB + TAB + op.getName().getJavaValidContent() + "_num++;\n";
				increaseCallCounters += TAB + TAB + TAB + "}\n";
			}

		}
		declaredInputs += TAB + TAB + "while(_nextCall!=null){\n";

		// update matchedIO values

		declaredInputs += TAB + TAB + TAB + "//update matchedIO variable values\n";

		String matchedArrays = "";
		String matchedArrays2 = "";
		String matchedArrays3 = "";
		for (OwlService matchedOutput : matchedOutputs) {
			boolean isMemberOfArray = false;

			for (Object parent : matchedOutput.getArgument().getParent()) {
				if (parent instanceof Argument)
					if (((Argument) parent).isArray()) {
						isMemberOfArray = true;
					}
			}
			// if the output is member of an array
			if (isMemberOfArray || matchedOutput.getArgument().isArray()) {
				OwlService initialArray = RunWorkflow.getInitialArray(matchedOutput, graph, false);
				int subNum = 0;
				for (Argument sub : initialArray.getArgument().getSubtypes()) {
					if (sub.getOwlService().getisMatchedIO())
						subNum++;
				}
				boolean arrayIsMatched = false;

				if (subNum == initialArray.getArgument().getSubtypes().size()) {
					arrayIsMatched = true;

				}
				// and all the elements of the output array are matched
				if (arrayIsMatched) {

					String array = "";
					String ret = ".get" + matchedOutput.getName().getJavaValidContent().replaceAll("[0123456789]", "")
							+ "()";
					ret = roadToSub(initialArray, matchedOutput, graph, ret, "0", classNames, true);
					int index = ret.indexOf(".get(0)");
					array = ret.substring(0, index);
					matchedArrays = TAB + TAB + TAB + "if (!"
							+ matchedOutput.getArgument().getBelongsToOperation().getName() + "_response" + array
							+ ".isEmpty() && _nextCall != null";

					for (OwlService matchedInput : graph.getSuccessors(matchedOutput)) {
						matchedArrays += "&& _nextCall.equals(\"call"
								+ matchedInput.getArgument().getBelongsToOperation().getName() + "\")";
					}
					matchedArrays += "){\n";
					String type = initialArray.getName().toString().replaceAll("[0123456789]", "");
					matchedArrays += TAB + TAB + TAB + TAB + type.substring(0, 1).toUpperCase() + type.substring(1)
							+ " " + type + " = new " + type.substring(0, 1).toUpperCase() + type.substring(1) + "();\n";
					for (OwlService matchedInput : graph.getSuccessors(matchedOutput)) {
						matchedArrays3 += TAB + TAB + TAB + TAB + type + "."
								+ matchedInput.getName().toString().replaceAll("[0123456789]", "") + " = "
								+ matchedOutput.getArgument().getBelongsToOperation().getName() + "_response" + ret
								+ ";\n";
						matchedArrays2 = TAB + TAB + TAB + TAB
								+ matchedInput.getArgument().getBelongsToOperation().getName() + "_request." + type
								+ ".add(" + type + ");\n";
					}

					matchedArrays2 += TAB + TAB + TAB + "}\n";

					// and NOT all the elements of the output array are matched
				} else {
					for (OwlService matchedInput : graph.getSuccessors(matchedOutput)) {
						String array = "";
						String ret = ".get"
								+ matchedOutput.getName().getJavaValidContent().replaceAll("[0123456789]", "") + "()";
						ret = roadToSub(initialArray, matchedOutput, graph, ret, "i", classNames, true);
						int index = ret.indexOf(".get(i)");
						array = ret.substring(0, index);
						String type = matchedOutput.getArgument().getType();
						declaredInputs += TAB + TAB + TAB + "if (_previousCall.equals(\"call"
								+ matchedOutput.getArgument().getBelongsToOperation().getName() + "\") && !"
								+ matchedOutput.getArgument().getBelongsToOperation().getName() + "_response" + array
								+ ".isEmpty()){\n";
						declaredInputs += TAB + TAB + TAB + TAB + "int size = Math.min("
								+ matchedInput.getName().getJavaValidContent().toString() + "_num, "
								+ matchedOutput.getArgument().getBelongsToOperation().getName() + "_response" + array
								+ ".size());\n";
						declaredInputs += TAB + TAB + TAB + TAB + "for (int i = 0; i < size; i++) {\n";
						if (type.equalsIgnoreCase("String")) {
							declaredInputs += TAB + TAB + TAB + TAB + "this."
									+ matchedInput.getName().getJavaValidContent().toString() + ".add(new Variable(\""
									+ matchedInput.getName().getComparableForm() + "\", "
									+ matchedOutput.getArgument().getBelongsToOperation().getName() + "_response" + ret
									+ ",\"String\"));\n";

						} else if (type.equalsIgnoreCase("int")) {
							declaredInputs += TAB + TAB + TAB + TAB + "this."
									+ matchedInput.getName().getJavaValidContent().toString() + ".add(new Variable(\""
									+ matchedInput.getName().getComparableForm() + "\", Integer.toString("
									+ matchedOutput.getArgument().getBelongsToOperation().getName() + "_response" + ret
									+ ",\"int\"));\n";
						} else {
							declaredInputs += TAB + TAB + TAB + TAB + "this."
									+ matchedInput.getName().getJavaValidContent().toString() + ".add(new Variable(\""
									+ matchedInput.getName().getComparableForm() + "\", "
									+ type.substring(0, 1).toUpperCase() + type.substring(1) + ".toString("
									+ matchedOutput.getArgument().getBelongsToOperation().getName() + "_response" + ret
									+ ",\"" + type + "\"));\n";
						}
						declaredInputs += TAB + TAB + TAB + TAB + "}\n" + TAB + TAB + TAB + "}\n";
					}
				}
				// if output doesn't belong to an array
			} else {
				for (OwlService matchedInput : graph.getSuccessors(matchedOutput)) {

					boolean isMemberOfObject = false;

					for (Object parent : matchedInput.getArgument().getParent()) {
						if (parent instanceof Argument)
							isMemberOfObject = true;
						break;
					}
					String retOutput = ".get"
							+ matchedOutput.getName().getJavaValidContent().replaceAll("[0123456789]", "") + "()";
					retOutput = roadToSub(null, matchedOutput, graph, retOutput, "", classNames, true);
					// if input is member of an object
					if (isMemberOfObject || matchedInput.getArgument().isTypeOf().equals("BodyParameter")) {
						declaredInputs += TAB + TAB + TAB + "if (_previousCall.equals(\"call"
								+ matchedOutput.getArgument().getBelongsToOperation().getName() + "\")) {\n";
						String retInput = matchedInput.getName().getJavaValidContent().replaceAll("[0123456789]", "")
								+ "(";
						retInput = roadToSub(null, matchedInput, graph, retInput, "", classNames, false);
						retInput = matchedInput.getArgument().getBelongsToOperation().getName().toString()
								+ "_request.set" + retInput;

						String type = matchedOutput.getArgument().getType();
						if (type.equalsIgnoreCase("String")) {
							declaredInputs += TAB + TAB + TAB + retInput
									+ matchedOutput.getArgument().getBelongsToOperation().getName() + "_response"
									+ retOutput + ");\n";

						} else if (type.equalsIgnoreCase("int")) {
							declaredInputs += TAB + TAB + TAB + retInput + "Integer.toString("
									+ matchedOutput.getArgument().getBelongsToOperation().getName() + "_response"
									+ retOutput + "));\n";
						} else {
							declaredInputs += TAB + TAB + TAB + retInput + type.substring(0, 1).toUpperCase()
									+ type.substring(1) + ".toString("
									+ matchedOutput.getArgument().getBelongsToOperation().getName() + "_response"
									+ retOutput + "));\n";
						}
						declaredInputs += TAB + TAB + TAB + "}\n";
						// if input is primitive
					} else {

						String type = matchedOutput.getArgument().getType();
						if (type.equalsIgnoreCase("String")) {
							declaredInputs += TAB + TAB + TAB + "this."
									+ matchedInput.getName().getJavaValidContent().toString() + ".value = "
									+ matchedOutput.getArgument().getBelongsToOperation().getName() + "_response"
									+ retOutput + ";\n";

						} else if (type.equalsIgnoreCase("int")) {
							declaredInputs += TAB + TAB + TAB + "this."
									+ matchedInput.getName().getJavaValidContent().toString() + ".value = "
									+ " Integer.toString("
									+ matchedOutput.getArgument().getBelongsToOperation().getName() + "_response"
									+ retOutput + ");\n";
						} else {
							declaredInputs += TAB + TAB + TAB + "this."
									+ matchedInput.getName().getJavaValidContent().toString() + ".value = "
									+ type.substring(0, 1).toUpperCase() + type.substring(1) + ".toString("
									+ matchedOutput.getArgument().getBelongsToOperation().getName() + "_response"
									+ retOutput + ");\n";
						}
					}

				}
			}
		}
		declaredInputs += matchedArrays + matchedArrays3 + matchedArrays2;

		declaredInputs += setPreviousCall;
		declaredInputs += TAB + TAB + TAB + "_nextCall = call(_nextCall";
		declaredInputs += call + ");\n\n";
		declaredInputs += increaseCallCounters;

		declaredInputs += TAB + TAB + TAB + "}\n";
		// make call switcher
		functionCode += TAB + "public String call(String name";
		for (Operation op : repeatedOperations) {
			functionCode += ", int " + op.getName().getJavaValidContent() + "_num";
		}
		functionCode += ")throws Exception{\n";
		for (OwlService service : graph.getVertices()) {
			if (service.getArgument() == null) {
				if (service.getOperation() == null) {
					functionCode += TAB + TAB + "if(name.equals(\"" + getFunctionName(service) + "\"))\n" + TAB + TAB
							+ TAB + "return " + getFunctionName(service) + "();\n";
				} else {
					functionCode += TAB + TAB + "if(name.equals(\"" + getFunctionName(service) + "\"))\n" + TAB + TAB
							+ TAB + "return " + getFunctionName(service) + "(";
					if (repeatedOperations.contains(service.getOperation()))
						functionCode += service.getName().getJavaValidContent() + "_num";
					functionCode += ");\n";
				}
			}
		}
		functionCode += TAB + TAB + "return null;\n" + TAB + "}\n";

		// return code
		String returnCode = "";
		returnCode = FunctionCodeNode.generateImports(restServiceExists, hasDeserializer, wsdlServiceExists, hasMailgun)
				+ "\npublic class " + createdClassName + "{\n" + TAB;

		returnCode += declaredVariables + "\n" + requestClassDeclaration + resultClassDeclaration
				+ variableClassDeclaration + functionCode + declaredInputs + requestObjectDeclaration
				+ resultObjectDeclaration + TAB + "}\n}";

		String crudVerb = "GET";
		if (hasBodyInput) {
			crudVerb = "POST";
		}

		for (OwlService input : inputVariables) {
			if (input.getArgument().getSubtypes().size() != 0) {
				int subNum = 0;
				for (Argument sub : input.getArgument().getSubtypes()) {
					if (sub.getOwlService().getisMatchedIO())
						subNum++;
				}

				if (subNum == input.getArgument().getSubtypes().size()) {
					// don't write to ontology since the whole object is matched
					// if (subNum < input.getArgument().getSubtypes().size())
				} else {
					inputsWithoutMatchedVariables.add(input);
				}
			} else {
				if (!input.getisMatchedIO()) {
					inputsWithoutMatchedVariables.add(input);
				}
			}
		}
		for (OwlService matched : nativeInputsMatchedWithArrays) {
			Argument arg = new Argument(matched.getName().getJavaValidContent() + "_num", "int", "QueryParameter",
					false, true, null);
			arg.setIsRequired(true);
			inputsWithoutMatchedVariables.add(new OwlService(arg));
		}
		operation = instance.createObjects(ProjectName, inputsWithoutMatchedVariables, uriParameters, authParameters,
				outputVariables, repeatedOperations, crudVerb, graph);

		return returnCode;
	}

	public MDEOperation getOperation() {
		return this.operation;
	}

	public ArrayList<Operation> getRepeatedOperations() {
		return this.repeatedOperations;
	}

	public ArrayList<OwlService> getInputVariables() {
		return this.inputVariables;
	}

	public ArrayList<OwlService> getNativeInputsMatchedWithArrays() {
		return this.nativeInputsMatchedWithArrays;
	}

	public ArrayList<OwlService> getInputsWithoutMatchedVariables() {
		return this.inputsWithoutMatchedVariables;
	}

	public ArrayList<OwlService> getOutputVariables() {
		return this.outputVariables;
	}

	public ArrayList<Argument> geturiParameters() {
		return this.uriParameters;
	}

	private String generateResultObjects(OwlService variable, ArrayList<OwlService> classObjects,
			ArrayList<String> classNames, Graph<OwlService, Connector> graph, boolean isInput) {

		String type = variable.getArgument().getType();
		String ObjectClasses = "\n";
		if (!variable.getArgument().getSubtypes().isEmpty()) {

			for (OwlService object : classObjects) {
				if (variable.getName().getComparableForm().equals(object.getName().getComparableForm())
						&& !variable.equals(object)) {
					if (variable.getArgument().getSubtypes().size() == object.getArgument().getSubtypes().size()) {
						if (classNames.contains(variable.getName().getComparableForm()))
							return ObjectClasses;
						else
							type = variable.getName().getComparableForm();
					} else {
						type = variable.getName().getJavaValidContent();
					}
				}
			}
			String deserializer = "";
			// if output is either array or object
			if (variable.getArgument().getObjectOrArray()) {
				deserializer = TAB + "public static class " + type.substring(0, 1).toUpperCase() + type.substring(1)
						+ "Deserializer implements JsonDeserializer<" + type.substring(0, 1).toUpperCase()
						+ type.substring(1) + "> {\n";
				deserializer += TAB + TAB + "\n" + TAB + TAB + "@Override\n";
				deserializer += TAB + TAB + "public " + type.substring(0, 1).toUpperCase() + type.substring(1)
						+ " deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)throws JsonParseException {\n";
				hasDeserializer = true;
			}

			String VarDeclaration = "";
			String ConstractorClasses = "";
			ConstractorClasses += TAB + TAB + type.substring(0, 1).toUpperCase() + type.substring(1) + "(){\n";
			ConstractorClasses += TAB + TAB + "}\n\n";
			ConstractorClasses += TAB + TAB + type.substring(0, 1).toUpperCase() + type.substring(1) + "(";
			String constractorVars = "";
			String getSet = "\n\n";
			String deserializerBody = "";

			Collection<OwlService> subList = new ArrayList<OwlService>();
			if (isInput) {
				subList = (Collection<OwlService>) graph.getPredecessors(variable);
			} else {
				subList = (Collection<OwlService>) graph.getSuccessors(variable);
			}
			for (OwlService sub : subList) {
				if (sub.getArgument() != null) {
					String subType = sub.getArgument().getType();
					if (sub.getArgument().getSubtypes().isEmpty() && subType.equals("object")) {
						subType = "String";
					}
					String subName = sub.getArgument().getName().getJavaValidContent();

					if (!constractorVars.isEmpty()) {
						constractorVars += ", ";
					}
					if (variable.getArgument().getObjectOrArray()) {
						VarDeclaration += TAB + TAB + "@SerializedName(\"" + subName + "\")\n";
						deserializerBody += TAB + TAB + "ArrayList<" + subType.substring(0, 1).toUpperCase()
								+ subType.substring(1) + "> " + type + "List = new ArrayList<"
								+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + ">();\n";
						deserializerBody += TAB + TAB + "try {\n" + TAB + TAB + TAB + "Type " + subType
								+ "Type = new TypeToken<" + subType.substring(0, 1).toUpperCase() + subType.substring(1)
								+ "[]>() {}.getType();\n";
						deserializerBody += TAB + TAB + TAB + subType.substring(0, 1).toUpperCase()
								+ subType.substring(1) + "[] " + subType
								+ "Table = new Gson().fromJson(json.getAsJsonObject().getAsJsonArray(\"" + subType
								+ "\"), " + subType + "Type);\n";
						deserializerBody += TAB + TAB + TAB + "for (Event event : eventTable){\n";
						deserializerBody += TAB + TAB + TAB + TAB + "eventsList.add(event);\n" + TAB + TAB + TAB
								+ "}\n";
						deserializerBody += TAB + TAB + "} catch (Exception e) {\n" + TAB + TAB + TAB
								+ "Event child = context.deserialize(json.getAsJsonObject().getAsJsonObject(\""
								+ subType + "\"), Event.class);\n" + TAB + TAB + TAB + "eventsList.add(child);\n" + TAB
								+ TAB + "}\n";
						deserializerBody += TAB + TAB + "Events events = new Events(eventsList);\n" + TAB + TAB
								+ "return events;\n";
						deserializerBody += TAB + TAB + "}\n" + TAB + "}\n";
					}
					if (sub.getArgument().getSubtypes().isEmpty() && !sub.getArgument().isArray()) {
						VarDeclaration += TAB + TAB + "private " + subType + " " + subName + ";\n";
						constractorVars += "" + subType + " " + subName;
						getSet += TAB + TAB + "public void set" + subName + "(" + subType + " "
								+ subName.replaceAll("[0123456789]", "") + ") {\n" + TAB + TAB + TAB + "this." + subName
								+ " = " + subName.replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
						if (!sub.getArgument().getBelongsToOperation().getType().equals("SOAP"))
							getSet += TAB + TAB + "@XmlElement\n";
						getSet += TAB + TAB + "public " + subType + " get" + subName + "() {\n" + TAB + TAB + TAB
								+ "return " + subName + ";\n" + TAB + TAB + "}\n";
					} else if (sub.getArgument().isArray() && RAMLCaller.stringIsItemFromList(sub.getType(), datatypes)
							|| variable.getArgument().getObjectOrArray()) {
						if (subType.equals("int")) {
							subType = "Integer";
						} else {
							subType = subType.substring(0, 1).toUpperCase() + subType.substring(1);
						}
						VarDeclaration += TAB + TAB + "private ArrayList <" + subType + "> " + subName
								+ "= new ArrayList<" + subType + ">();\n";
						constractorVars += "ArrayList <" + subType + "> " + subName;
						getSet += TAB + TAB + "public void set" + subName + "(ArrayList<" + subType + "> "
								+ subName.replaceAll("[0123456789]", "") + ") {\n" + TAB + TAB + TAB + "this." + subName
								+ " = " + subName.replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
						if (!sub.getArgument().getBelongsToOperation().getType().equals("SOAP"))
							getSet += TAB + TAB + "@XmlElement\n";
						getSet += TAB + TAB + "public ArrayList<" + subType + "> get" + subName + "() {\n" + TAB + TAB
								+ TAB + "return " + subName + ";\n" + TAB + TAB + "}\n";
					} else
						if (sub.getArgument().isArray() && !RAMLCaller.stringIsItemFromList(sub.getType(), datatypes)) {

						VarDeclaration += TAB + TAB + "private ArrayList <" + subType.substring(0, 1).toUpperCase()
								+ subType.substring(1) + "> " + subName + " = new ArrayList<"
								+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + ">();\n";
						constractorVars += "ArrayList <" + subType.substring(0, 1).toUpperCase() + subType.substring(1)
								+ "> " + subName;
						getSet += TAB + TAB + "public void set" + subName + "(ArrayList<"
								+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + "> "
								+ subName.replaceAll("[0123456789]", "") + ") {\n" + TAB + TAB + TAB + "this." + subName
								+ " = " + subName.replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
						if (!sub.getArgument().getBelongsToOperation().getType().equals("SOAP"))
							getSet += TAB + TAB + "@XmlElement\n";
						getSet += TAB + TAB + "public ArrayList<" + subType.substring(0, 1).toUpperCase()
								+ subType.substring(1) + "> get" + subName + "() {\n" + TAB + TAB + TAB + "return "
								+ subName + ";\n" + TAB + TAB + "}\n";
					} else if (!sub.getArgument().isArray() && !sub.getArgument().getSubtypes().isEmpty()) {

						VarDeclaration += TAB + TAB + "private " + subType.substring(0, 1).toUpperCase()
								+ subType.substring(1) + " " + subName + " = new "
								+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + "();\n";
						constractorVars += subType.substring(0, 1).toUpperCase() + subType.substring(1) + " " + subName;
						getSet += TAB + TAB + "public void set" + subName + "(" + subType.substring(0, 1).toUpperCase()
								+ subType.substring(1) + " " + subName.replaceAll("[0123456789]", "") + ") {\n" + TAB
								+ TAB + TAB + "this." + subName + " = " + subName.replaceAll("[0123456789]", "") + ";\n"
								+ TAB + TAB + "}\n";
						if (!sub.getArgument().getBelongsToOperation().getType().equals("SOAP"))
							getSet += TAB + TAB + "@XmlElement\n";
						getSet += TAB + TAB + "public " + subType.substring(0, 1).toUpperCase() + subType.substring(1)
								+ " get" + subName + "() {\n" + TAB + TAB + TAB + "return " + subName + ";\n" + TAB
								+ TAB + "}\n";
					}
				}
			}

			VarDeclaration += "\n\n";
			ObjectClasses += deserializer + deserializerBody;
			if (variable.getArgument().getBelongsToOperation().getType().equals("SOAP")) {
				ObjectClasses += TAB + "@XmlAccessorType(XmlAccessType.FIELD)\n";
			}
			ObjectClasses += TAB + "public static class " + type.substring(0, 1).toUpperCase() + type.substring(1)
					+ " {\n";
			classNames.add(type);
			ObjectClasses += VarDeclaration;
			ConstractorClasses += constractorVars + "){\n";
			for (OwlService sub : subList) {
				if (sub.getArgument() != null) {
					ConstractorClasses += TAB + TAB + TAB + "this." + sub.getArgument().getName().getJavaValidContent()
							+ " = " + sub.getArgument().getName().getJavaValidContent() + ";\n";
				}
			}
			ConstractorClasses += TAB + TAB + "}\n";
			ObjectClasses += ConstractorClasses;
			ObjectClasses += getSet;
			ObjectClasses += "}\n";

			// if (!classObjects.contains(variable)) {
			// classObjects.add(variable);
			// }
			for (OwlService sub : subList) {
				if (sub.getArgument() != null) {
					if (!sub.getArgument().getSubtypes().isEmpty()) {
						ObjectClasses += generateResultObjects(sub, classObjects, classNames, graph, isInput);
					}
				}

			}

		}
		return ObjectClasses;

	}

	private String roadToSub(OwlService initialArray, OwlService sub, Graph<OwlService, Connector> graph, String ret,
			String num, ArrayList<String> classNames, boolean isOutput) {
		if (isOutput) {
			for (OwlService service : graph.getPredecessors(sub)) {
				if (service.getArgument() != null) {
					String name = "";
					if (!service.getArgument().getSubtypes().isEmpty()
							&& classNames.contains(service.getName().getJavaValidContent())) {
						name = service.getName().getJavaValidContent();
					} else {
						name = service.getName().getJavaValidContent().replaceAll("[0123456789]", "");
					}
					if (initialArray != null && service.equals(initialArray)) {
						ret = ".get" + name + "().get(" + num + ")" + ret;
					} else {
						ret = ".get" + name + "()" + ret;
					}
					ret = roadToSub(initialArray, service, graph, ret, num, classNames, true);
				} else {
					return ret;
				}
			}
		} else {
			for (OwlService service : graph.getSuccessors(sub)) {
				if (service.getArgument() != null) {
					String name = "";
					if (!service.getArgument().getSubtypes().isEmpty()
							&& classNames.contains(service.getName().getJavaValidContent())) {
						name = service.getName().getJavaValidContent();
					} else {
						name = service.getName().getJavaValidContent().replaceAll("[0123456789]", "");
					}
					if (initialArray != null && service.equals(initialArray)) {
						ret = ".get" + name + "().get(" + num + ")" + ret;
					} else {
						ret = ".get" + name + "()" + ret;
					}
					ret = roadToSub(initialArray, service, graph, ret, num, classNames, false);
				} else {
					return ret;
				}
			}
		}
		return ret;
	}

	public void setInputVariables(ArrayList<OwlService> inputVariables) {
		this.inputVariables = inputVariables;
	}

	public void setOutputVariables(ArrayList<OwlService> outputVariables) {
		this.outputVariables = outputVariables;
	}

	public void setOperation(MDEOperation operation) {
		this.operation = operation;
	}

	public ArrayList<Argument> getUriParameters() {
		return uriParameters;
	}

	public ArrayList<Argument> getAuthParameters() {
		return authParameters;
	}

	public void setUriParameters(ArrayList<Argument> uriParameters) {
		this.uriParameters = uriParameters;
	}

}
