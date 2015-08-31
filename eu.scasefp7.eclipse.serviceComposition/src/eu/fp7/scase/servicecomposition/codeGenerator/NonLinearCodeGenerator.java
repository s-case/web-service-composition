package eu.fp7.scase.servicecomposition.codeGenerator;

import eu.fp7.scase.servicecomposition.importer.JungXMIImporter.Connector;
import eu.fp7.scase.servicecomposition.importer.Importer.Argument;
import eu.fp7.scase.servicecomposition.importer.Importer.Operation;
import eu.fp7.scase.servicecomposition.importer.JungXMIImporter.Service;

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

import eu.fp7.scase.servicecomposition.transformer.PathFinding;
import eu.fp7.scase.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * <h1>NonLinearCodeGenerator</h1> This class is used for generating non-linear
 * code. The way to actually implement individual service calls is
 * parameterized, defining different types of generated code, according to
 * packagedependences.
 * 
 * @author Manios Krasanakis
 *
 * @param <FunctionCodeNodeType>
 *            : an instance of <code>FunctionCodeName</code> to generate
 *            individual service calls
 */
public class NonLinearCodeGenerator<FunctionCodeNodeType extends FunctionCodeNode> extends CodeGenerator {

	// Graph<OwlService, Connector> graph = new SparseMultigraph<OwlService,
	// Connector>();
	HashMap<OwlService, OwlService> map = new HashMap<OwlService, OwlService>();

	public String generateCode(final Graph<OwlService, Connector> jungGraph, String functionName,
			boolean addConnectionsToGraph) throws Exception {

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

		//
		// detect all variables
		ArrayList<OwlService> allVariables = new ArrayList<OwlService>();
		ArrayList<OwlService> allWSDLVariables = new ArrayList<OwlService>();
		ArrayList<OwlService> allPythonVariables = new ArrayList<OwlService>();
		ArrayList<Argument> outputs = new ArrayList<Argument>();

		for (OwlService service : graph.getVertices()) {
			for (OwlService service2 : graph.getVertices()) {
				if (service.getName().getContent().toString()
						.equalsIgnoreCase(service2.getName().getContent().toString())
						&& !service.equals(service2) && service.getName().getContent().toString() != "") {
					service2.getName().setContent(service.getName().getContent().toString() + service2.getId());
				}
			}
		}

		for (OwlService service : graph.getVertices()) {
			Operation op = service.getOperation();
			if (op != null) {

				// for (Argument arg : op.getOutputs()) {
				// outputs.add(arg);
				// if (!allVariables.contains(arg))
				// allVariables.add(arg);

				// if ((!service.getOperation().getDomain().isLocal())) {
				// if (!allWSDLVariables.contains(arg) &&
				// !allPythonVariables.contains(arg)) {
				// allWSDLVariables.add(arg);
				// }
				// } else {
				// if (!allPythonVariables.contains(arg) &&
				// !allWSDLVariables.contains(arg)) {
				// allPythonVariables.add(arg);
				// }
				// }
				// for (Argument sub : arg.getSubtypes()) {
				// outputs.add(sub);
				// if (!allVariables.contains(arg))
				// allVariables.add(sub);

				// if ((!service.getOperation().getDomain().isLocal())) {
				// if (!allWSDLVariables.contains(sub) &&
				// !allPythonVariables.contains(sub)) {
				// allWSDLVariables.add(sub);
				// }
				// } else {
				// if (!allPythonVariables.contains(sub) &&
				// !allWSDLVariables.contains(sub)) {
				// allPythonVariables.add(sub);
				// }
				// // }
				// }
				//
				// }

				// for (Argument arg : op.getInputs()) {
				//
				// if (!allVariables.contains(arg))
				// allVariables.add(arg);

				// if ((!service.getOperation().getDomain().isLocal())) {
				// if (!allWSDLVariables.contains(arg) &&
				// !allPythonVariables.contains(arg)) {
				// allWSDLVariables.add(arg);
				// }
				// } else {
				// if (!allPythonVariables.contains(arg) &&
				// !allWSDLVariables.contains(arg)) {
				// allPythonVariables.add(arg);
				// }
				// }
				// }
			}
		}
		for (OwlService service : graph.getVertices()) {
			if (service.getType().equalsIgnoreCase("Property")) {

				if (!allVariables.contains(service))
					allVariables.add(service);

				for (OwlService pre : graph.getPredecessors(service)) {
					if (pre.getOperation() != null) {
						if ((!pre.getOperation().getDomain().isLocal())) {
							if (!allWSDLVariables.contains(service) && !allPythonVariables.contains(service)) {
								allWSDLVariables.add(service);
							}
						} else {
							if (!allPythonVariables.contains(service) && !allWSDLVariables.contains(service)) {
								allPythonVariables.add(service);
							}
						}
						for (OwlService suc : graph.getSuccessors(service)) {
							if (suc.getType().equalsIgnoreCase("Property")) {
								if ((!pre.getOperation().getDomain().isLocal())) {
									if (!allWSDLVariables.contains(suc) && !allPythonVariables.contains(suc)) {
										allWSDLVariables.add(suc);
									}
								} else {
									if (!allPythonVariables.contains(suc) && !allWSDLVariables.contains(suc)) {
										allPythonVariables.add(suc);
									}
								}
							}
						}
					}
				}
				for (OwlService suc : graph.getSuccessors(service)) {
					if (suc.getOperation() != null) {
						if ((!suc.getOperation().getDomain().isLocal())) {
							if (!allWSDLVariables.contains(service) && !allPythonVariables.contains(service)) {
								allWSDLVariables.add(service);
							}
						} else {
							if (!allPythonVariables.contains(service) && !allWSDLVariables.contains(service)) {
								allPythonVariables.add(service);
							}
						}
					}
				}
			}

		}
		// assert correct subtypes (for debugging purposes)
		// for(Argument arg : allVariables)
		// arg.assertCorrectSubtypes();
		// initialize variable lists
		ArrayList<OwlService> inputVariables = new ArrayList<OwlService>();
		ArrayList<OwlService> outputVariables = new ArrayList<OwlService>();
		ArrayList<OwlService> subOutputVariables = new ArrayList<OwlService>();
		ArrayList<OwlService> matchedIO = new ArrayList<OwlService>();
		ArrayList<OwlService> matchedInputs = new ArrayList<OwlService>();
		ArrayList<OwlService> matchedOutputs = new ArrayList<OwlService>();
		// detect all operations
		ArrayList<OwlService> remainingOperations = new ArrayList<OwlService>();
		for (OwlService service : graph.getVertices()) {
			if (service.getOperation() != null)
				remainingOperations.add(service);
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
		// for (OwlService service : graph.getVertices()) {
		// if (service.getOperation() != null)
		// for (Argument arg : service.getOperation().getOutputs()) {
		// if (!outputVariables.contains(arg))
		// outputVariables.add(arg);
		// for (Argument sub : arg.getSubtypes()) {
		// if (!subOutputVariables.contains(sub))
		// subOutputVariables.add(sub);
		// }
		// }
		// }

		for (OwlService service : graph.getVertices()) {
			if (service.getArgument() != null) {
				if (graph.getOutEdges(service).size() == 0) {
					Collection<OwlService> predecessors = (Collection<OwlService>) graph.getPredecessors(service);
					for (OwlService predecessor : predecessors) {
						if (predecessor.getType().equalsIgnoreCase("Property")) {
							subOutputVariables.add(service);
						} else {
							outputVariables.add(service);
						}
					}

				} else {
					Collection<OwlService> successors = (Collection<OwlService>) graph.getSuccessors(service);
					boolean matched = false;
					boolean output = false;
					for (OwlService successor : successors) {
						if (successor.getisMatchedIO() && service.getisMatchedIO()) {
							matchedInputs.add(successor);
							matched = true;
						}

						if (successor.getType().equalsIgnoreCase("Property") && !successor.getisMatchedIO()
								&& !service.getisMatchedIO()) {
							output = true;
						}
					}
					if (matched) {
						matchedOutputs.add(service);

						Collection<OwlService> predecessors = (Collection<OwlService>) graph.getPredecessors(service);
						for (OwlService predecessor : predecessors) {
							if (predecessor.getType().equalsIgnoreCase("Property")) {
								subOutputVariables.add(service);
							} else {
								outputVariables.add(service);
							}
						}
					}
					if (output) {
						outputVariables.add(service);
					}
				}
			}
		}

		// for (OwlService service : graph.getVertices()) {
		// if (service.getOperation() != null)
		// for (Argument arg : service.getOperation().getInputs())
		// outputVariables.remove(arg);
		// }
		// detect input variables
		for (OwlService service : graph.getVertices()) {
			if (service.getArgument() != null) {
				if (graph.getInEdges(service).size() == 0) {
					if (!service.getisMatchedIO() && !inputVariables.contains(service)) {
						inputVariables.add(service);
					}
				}
			}
		}
		// generate function code for all services
		String functionCode = "";
		for (OwlService service : graph.getVertices()) {
			if (service.getArgument() == null) {
				CodeNode code = FunctionCodeNodeType.createInstance(service, this);
				code.applyTab();
				functionCode += code.createFunctionCode(graph, allVariables);
			}
		}
		// create class name
		String createdClassName = functionName.substring(0, 1).toUpperCase() + functionName.substring(1) + "Class";
		// create variable declarations
		String declaredVariables = "";

//		for (OwlService service : allWSDLVariables) {
//			if (service.getArgument().getSubtypes().isEmpty()) {
//				declaredVariables += TAB + "private " + service.getArgument().getType() + " "
//						+ service.getName().getContent().toString() + ";\n";
//			}
			// else {
			// declaredVariables += TAB + "private " + arg.getType() +
			// arg.getName().toString() + " = new "
			// + arg.getType() + "();\n";
			// }
//		}

		for (OwlService service : allVariables) {
			// if(arg.isNative() || inputVariables.contains(arg))
			declaredVariables += TAB + "private Variable " + service.getName().getContent().toString()
					+ " = new Variable(\"" + service.getName().getContent().replaceAll("[0123456789]", "") + "\",\"\");\n";
			// else
			// declaredVariables +=
			// TAB+"private "+arg.toString()+" = new "+arg.getType()+"();\n";
		}
		if (!declaredVariables.isEmpty())
			declaredVariables = TAB + "//variables\n" + declaredVariables;
		// create class constructor (assigns to variable declarations)
		String declaredInputs = "";
		for (OwlService input : inputVariables) {
			if (!declaredInputs.isEmpty())
				declaredInputs += ", ";
			if (input.getArgument().getType() == "") {
				declaredInputs += "String " + input.getName().getContent();
			} else {
				declaredInputs += input.getArgument().getType() + " " + input.getName().getContent();
			}
		}

		// create result class instance
		String resultClassName = "void";
		String resultClassDeclaration = "";
		String resultObjectDeclaration = "";
		ArrayList<OwlService> resultVariables=new ArrayList<OwlService>();
		if (outputVariables.size() == 1 && subOutputVariables.size() == 0) {
			resultClassName = outputVariables.get(0).getArgument().getType();
			resultObjectDeclaration = TAB + TAB + "return " + outputVariables.get(0).getName().getContent() + ";";
		} else if (!outputVariables.isEmpty()) {
			//String resultObjectName = functionName.substring(0, 1).toLowerCase() + functionName.substring(1) + "Result";
			String resultObjectName ="response";
			//resultClassName = functionName.substring(0, 1).toUpperCase() + functionName.substring(1) + "Result";
			resultClassName="Response";
			//resultClassDeclaration = TAB + "public class " + resultClassName + "{\n";
			resultClassDeclaration =TAB+"@XmlRootElement(name = \"Response\")\n"+ TAB+"public static class " + resultClassName + "{\n";
			for (OwlService arg : outputVariables) {
				if (arg.getArgument().getSubtypes().isEmpty()) {
					//if (arg.getArgument().getType() == "") {
						resultClassDeclaration += TAB+TAB+"private String " + arg.getName().getContent().replaceAll("[0123456789]", "") + ";\n";
						if (!resultVariables.contains(arg)){
						resultVariables.add(arg);
						}
//					} else {
//						resultClassDeclaration += TAB + TAB + arg.getArgument().getType() + " "
//								+ arg.getName().getContent().toString() + ";\n";
//					}
				}
			}
			for (OwlService arg : subOutputVariables) {
				//if (arg.getArgument().getType().isEmpty()) {
					resultClassDeclaration += TAB+TAB+"private String " + arg.getName().getContent().replaceAll("[0123456789]", "") + ";\n";
					if (!resultVariables.contains(arg)){
						resultVariables.add(arg);
						}
//				} else {
//					resultClassDeclaration += TAB + TAB + arg.getArgument().getType() + " "
//							+ arg.getName().getContent().toString() + ";\n";
//				}
			}
			resultClassDeclaration+="\n\n";
			
			resultClassDeclaration+=TAB+TAB+"public Response() {\n";
			for (OwlService arg : resultVariables) {
				resultClassDeclaration+=TAB+TAB+TAB+"this."+arg.getName().getContent().replaceAll("[0123456789]", "")+" = \"\";\n";
			}
			
			resultClassDeclaration+=TAB+TAB+"}\n\n";
			
			
			for (OwlService arg : resultVariables) {
				resultClassDeclaration+=TAB+ TAB+ "public void set"+arg.getName().getContent().replaceAll("[0123456789]", "")+"(String "+arg.getName().getContent().replaceAll("[0123456789]", "")+") {\n"+TAB+TAB+TAB+"this."+arg.getName().getContent().replaceAll("[0123456789]", "")+" = "+arg.getName().getContent().replaceAll("[0123456789]", "")+";\n"+TAB+TAB+"}\n";
				resultClassDeclaration+=TAB+TAB+"@XmlElement\n"+TAB+TAB+"public String get"+arg.getName().getContent().replaceAll("[0123456789]", "")+"() {\n"+TAB+TAB+TAB+"return "+arg.getName().getContent().replaceAll("[0123456789]", "")+";\n"+TAB+TAB+"}\n";
			}
		

			resultClassDeclaration += TAB + "}\n\n";

			resultObjectDeclaration = TAB + TAB + "//create class instance to be returned\n";
			resultObjectDeclaration += TAB + TAB + resultClassName + " " + resultObjectName + " = new "
					+ resultClassName + "();\n";
			for (OwlService arg : resultVariables) {
				if (arg.getArgument().getSubtypes().isEmpty()){
					resultObjectDeclaration += TAB + TAB + resultObjectName + ".set" + arg.getName().getContent().replaceAll("[0123456789]", "") + "("
							+ arg.getName().getContent() + ".value);\n";
				}
			}

			resultObjectDeclaration += TAB + TAB + "return " + resultObjectName + ";\n";
		}
		// create result class declaration
		declaredInputs = TAB + "public " + resultClassName + " " + "xmlResponse" + "(" + declaredInputs
				+ ") throws Exception{\n";
		declaredInputs += TAB + TAB + "//assign inputs to variables\n";
		for (OwlService arg : inputVariables) {
			
				declaredInputs += TAB + TAB + "this." + arg.getName().getContent() + ".value" + " = "
						+ arg.getName().getContent() + ";\n";		
		}
		
		//Generate Response class of RESTful web service
		String responseClass=TAB+"@XmlRootElement(name = \"Response\")\n"+ TAB+"public class Response {\n";
		for (OwlService arg : resultVariables) {
		responseClass+=TAB+TAB+"private String " + arg.getName().getContent().replaceAll("[0123456789]", "") + ";\n";
			}
		
		responseClass+=TAB+"}\n";
		
		
		
		// Variable class declaration
		String variableClassDeclaration = TAB + "public class Variable{\n";
		variableClassDeclaration += TAB + TAB + "public String name;\n" + TAB + TAB + "public String value;\n" + TAB
				+ TAB + "Variable(String name,String value){\n";
		variableClassDeclaration += TAB + TAB + TAB + "this.name = name;\n" + TAB + TAB + TAB + "this.value = value;\n";
		variableClassDeclaration += TAB + TAB + "}\n" + TAB + "}\n";

		// call all services
		declaredInputs += TAB + TAB + "//call services iterately\n";
		declaredInputs += TAB + TAB + "String _nextCall = \"" + getFunctionName(startingService) + "\";\n";
		declaredInputs += TAB + TAB + "while(_nextCall!=null){\n";
		declaredInputs += TAB + TAB + TAB + "_nextCall = call(_nextCall);\n";
		// update matchedIO values

		declaredInputs += TAB + TAB + TAB + "//update matchedIO variable values\n";
		// for (OwlService service : graph.getVertices()) {
		// if (service.getArgument()!=null) {
		// if (service.getisMatchedIO()&&
		// outputs.contains(service.getArgument())){
		// matchedIO.add(service);
		// }
		// }
		// }
		for (OwlService matchedOutput : matchedOutputs) {
			for (OwlService matchedInput : graph.getSuccessors(matchedOutput)) {
				
					declaredInputs += TAB + TAB + TAB + matchedInput.getName().getContent().toString() + ".value = "
							+ matchedOutput.getName().getContent().toString() + ".value;\n";
				
			}
		}
		declaredInputs += TAB + TAB + TAB + "}\n";
		// make call switcher
		functionCode += TAB + "public String call(String name)throws Exception{\n";
		for (OwlService service : graph.getVertices())
			if (service.getArgument() == null) {
				if (service.getOperation() == null)
					functionCode += TAB + TAB + "if(name.equals(\"" + getFunctionName(service) + "\"))\n" + TAB + TAB
							+ TAB + "return " + getFunctionName(service) + "();\n";
				else
					functionCode += TAB + TAB + "if(name.equals(\"" + getFunctionName(service) + "\"))\n" + TAB + TAB
							+ TAB + "return " + getFunctionName(service) + "();\n";
			}
		functionCode += TAB + TAB + "return null;\n" + TAB + "}\n";

		String parsedWSDLMap = "private HashMap<String, ParsedWSDLDefinition> parsedWSMap = new HashMap<String, ParsedWSDLDefinition> ();\n\n";
		parsedWSDLMap += TAB + "WSOperation domainOperation(String operationName, String operationURI){\n";
		parsedWSDLMap += TAB + TAB + "//get domain\n";
		parsedWSDLMap += TAB + TAB + "ParsedWSDLDefinition parsedWS = parsedWSMap.get(operationURI);\n";
		parsedWSDLMap += TAB + TAB + "if(parsedWS==null){\n";
		parsedWSDLMap += TAB + TAB + TAB + "parsedWS = ITIWSDLParser.parseWSDLwithAxis(operationURI, true, true);\n";
		parsedWSDLMap += TAB + TAB + TAB + "parsedWSMap.put(operationURI, parsedWS);\n";
		parsedWSDLMap += TAB + TAB + "}\n";
		parsedWSDLMap += TAB + TAB + "//find WSOperation\n";
		parsedWSDLMap += TAB + TAB + "WSOperation wsOperation = null;\n";
		parsedWSDLMap += TAB + TAB + "for(Object op : parsedWS.getWsdlOperations())\n";
		parsedWSDLMap += TAB + TAB + TAB
				+ "if(((WSOperation) op).getOperationName().equalsIgnoreCase(operationName)){\n";
		parsedWSDLMap += TAB + TAB + TAB + "wsOperation = (WSOperation) op;\n";
		parsedWSDLMap += TAB + TAB + TAB + "break;\n";
		parsedWSDLMap += TAB + TAB + "}\n";
		parsedWSDLMap += TAB + TAB + "return wsOperation;\n";
		parsedWSDLMap += TAB + "}\n";

		String xmlFromStringMethod = "public static Document loadXMLFromString(String xml) throws Exception{\n";
		xmlFromStringMethod += TAB + "DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();\n" + TAB
				+ "DocumentBuilder db = null;\n" + TAB + "db = dbf.newDocumentBuilder();\n" + TAB
				+ "InputSource is = new InputSource();\n" + TAB + "is.setCharacterStream(new StringReader(xml));\n"
				+ TAB + "Document doc = db.parse(is);\n" + TAB + "return doc;\n";
		
		xmlFromStringMethod += "}\n";
		// return code
		return FunctionCodeNodeType.generateImports() + "\npublic class " + createdClassName + "{\n" + TAB
				+ parsedWSDLMap +xmlFromStringMethod + "\n" + resultClassDeclaration + variableClassDeclaration + declaredVariables + "\n"
				+ functionCode + declaredInputs + resultObjectDeclaration + TAB + "}\n}";
	}
}
