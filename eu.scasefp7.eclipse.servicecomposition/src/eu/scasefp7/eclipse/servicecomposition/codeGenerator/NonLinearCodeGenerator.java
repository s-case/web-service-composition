package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Service;
import eu.scasefp7.eclipse.servicecomposition.operationCaller.RAMLCaller;
import eu.scasefp7.eclipse.servicecomposition.transformer.PathFinding;
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
 * @author Manios Krasanakis
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
	
	protected ConnectToMDEOntology instance= new ConnectToMDEOntology();
	protected MDEOperation operation;
	
	String[] datatypes = new String[] { "string", "long", "int", "float", "double", "dateTime", "boolean" };

	// Graph<OwlService, Connector> graph = new SparseMultigraph<OwlService,
	// Connector>();
	HashMap<OwlService, OwlService> map = new HashMap<OwlService, OwlService>();

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

		//
		// detect all variables
		ArrayList<OwlService> allVariables = new ArrayList<OwlService>();
		ArrayList<OwlService> allWSDLVariables = new ArrayList<OwlService>();
		ArrayList<OwlService> allPythonVariables = new ArrayList<OwlService>();
		

		for (OwlService service : graph.getVertices()) {
			for (OwlService service2 : graph.getVertices()) {
				if (service.getName().getContent().toString()
						.equalsIgnoreCase(service2.getName().getContent().toString()) && !service.equals(service2)
						&& service.getName().getContent().toString() != "") {
					service2.getName().setContent(service.getName().getContent().toString() + service2.getId());
				}
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
		
		//detect output variables
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
		
		//detect uri parameters
		for (OwlService service : graph.getVertices()) {
			if (service.getOperation() != null) {
				for (Argument param :service.getOperation().getUriParameters()){
					uriParameters.add(param);
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
					if (service.getOperation().getType().equalsIgnoreCase("RESTful")) {
						CodeNode code = RestFunctionCodeNode.createInstance(service, this);
						code.applyTab();
						functionCode += code.createFunctionCode(graph, allVariables);
						for (Argument arg : service.getOperation().getUriParameters()) {
							declaredVariables += TAB + "private Variable " + arg.getName().getContent().toString()
									+ " = new Variable(\"" + arg.getName().getContent().replaceAll("[0123456789]", "")
									+ "\",\"\", \""+arg.getType()+"\");\n";
						}
						restServiceExists = true;
					} else if (service.getOperation().getType().equalsIgnoreCase("SOAP")|| service.getOperation().getDomain().isLocal()) {
						CodeNode code = FunctionCodeNode.createInstance(service, this);
						code.applyTab();
						functionCode += code.createFunctionCode(graph, allVariables);
						wsdlServiceExists = true;
					}
				} else {
					CodeNode code = FunctionCodeNode.createInstance(service, this);
					code.applyTab();
					functionCode += code.createFunctionCode(graph, allVariables);
				}

			}
		}
		// create class name
		String createdClassName = functionName.substring(0, 1).toUpperCase() + functionName.substring(1) + "Class";
		


		for (OwlService service : allVariables) {
			// if(arg.isNative() || inputVariables.contains(arg))
			if (service.getArgument().getSubtypes().isEmpty()&& !service.getArgument().isArray()){
			declaredVariables += TAB + "private Variable " + service.getName().getContent().toString()
					+ " = new Variable(\"" + service.getName().getContent().replaceAll("[0123456789]", "")
					+ "\",\"\", \""+service.getArgument().getType()+"\");\n";
			}else if(service.getArgument().getSubtypes().isEmpty() && service.getArgument().isArray()){
				declaredVariables += TAB + "private Variable " + service.getName().getContent().toString()
						+ " = new Variable(\"" + service.getName().getContent().replaceAll("[0123456789]", "")
						+ "\",\"\", \"array\");\n";
			}else if(!service.getArgument().getSubtypes().isEmpty() && service.getArgument().isArray()){
				declaredVariables += TAB + "private Variable " + service.getName().getContent().toString()
						+ " = new Variable(\"" + service.getName().getContent().replaceAll("[0123456789]", "")
						+ "\",\"\", \"arrayOfObjects\");\n";
			}else{
				declaredVariables += TAB + "private Variable " + service.getName().getContent().toString()
						+ " = new Variable(\"" + service.getName().getContent().replaceAll("[0123456789]", "")
						+ "\",\"\", \"object\");\n";
			}
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
		for (Argument param: uriParameters){
			if (!declaredInputs.isEmpty())
				declaredInputs += ", ";
			if (param.getType() == "") {
				declaredInputs += "String " + param.getName().getContent();
			} else {
				declaredInputs += param.getType() + " " + param.getName().getContent();
			}
		}

		// create result class instance
		String resultClassName = "void";
		String resultClassDeclaration = "";
		String resultObjectDeclaration = "";
		ArrayList<OwlService> resultVariables = new ArrayList<OwlService>();
		if (outputVariables.size() == 1 && subOutputVariables.size() == 0) {
			resultClassName = outputVariables.get(0).getArgument().getType();
			resultObjectDeclaration = TAB + TAB + "return " + outputVariables.get(0).getName().getContent() + ";";
		} else if (!outputVariables.isEmpty()) {
			// String resultObjectName = functionName.substring(0,
			// 1).toLowerCase() + functionName.substring(1) + "Result";
			String resultObjectName = "response";
			// resultClassName = functionName.substring(0, 1).toUpperCase() +
			// functionName.substring(1) + "Result";
			resultClassName = "Response";
			// resultClassDeclaration = TAB + "public class " + resultClassName
			// + "{\n";
			resultClassDeclaration = TAB + "@XmlRootElement(name = \"Response\")\n" + TAB + "public static class "
					+ resultClassName + "{\n";
			for (OwlService arg : outputVariables) {
				if (arg.getArgument().getSubtypes().isEmpty()) {
					// if (arg.getArgument().getType() == "") {
					resultClassDeclaration += TAB + TAB + "private String "
							+ arg.getName().getContent() + ";\n";
					if (!resultVariables.contains(arg)) {
						resultVariables.add(arg);
					}
					// } else {
					// resultClassDeclaration += TAB + TAB +
					// arg.getArgument().getType() + " "
					// + arg.getName().getContent().toString() + ";\n";
					// }
				}else if (arg.getArgument().isArray()&& RAMLCaller.stringContainsItemFromList(arg.getArgument().getType(), datatypes)){
					resultClassDeclaration += TAB + TAB + "private ArrayList <String> "
							+ arg.getName().getContent() + ";\n";
					if (!resultVariables.contains(arg)) {
						resultVariables.add(arg);
					}
				}else if (arg.getArgument().isArray()&& !RAMLCaller.stringContainsItemFromList(arg.getArgument().getType(), datatypes)){
					resultClassDeclaration += TAB + TAB + "private ArrayList <" + arg.getArgument().getType() + "> "
							+ arg.getName().getContent() + ";\n";
					if (!resultVariables.contains(arg)) {
						resultVariables.add(arg);
					}
				}
					
			}
			for (OwlService arg : subOutputVariables) {
				// if (arg.getArgument().getType().isEmpty()) {
				resultClassDeclaration += TAB + TAB + "private String "
						+ arg.getName().getContent() + ";\n";
				if (!resultVariables.contains(arg)) {
					resultVariables.add(arg);
				}
				// } else {
				// resultClassDeclaration += TAB + TAB +
				// arg.getArgument().getType() + " "
				// + arg.getName().getContent().toString() + ";\n";
				// }
			}
			resultClassDeclaration += "\n\n";

			resultClassDeclaration += TAB + TAB + "public Response() {\n";
			for (OwlService arg : resultVariables) {
				resultClassDeclaration += TAB + TAB + TAB + "this."
						+ arg.getName().getContent() + " = \"\";\n";
			}

			resultClassDeclaration += TAB + TAB + "}\n\n";

			for (OwlService arg : resultVariables) {
				resultClassDeclaration += TAB + TAB + "public void set"
						+ arg.getName().getContent() + "(String "
						+ arg.getName().getContent().replaceAll("[0123456789]", "") + ") {\n" + TAB + TAB + TAB
						+ "this." + arg.getName().getContent() + " = "
						+ arg.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
				resultClassDeclaration += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public String get"
						+ arg.getName().getContent() + "() {\n" + TAB + TAB + TAB
						+ "return " + arg.getName().getContent() + ";\n" + TAB + TAB
						+ "}\n";
			}

			resultClassDeclaration += TAB + "}\n\n";

			resultObjectDeclaration = TAB + TAB + "//create class instance to be returned\n";
			resultObjectDeclaration += TAB + TAB + resultClassName + " " + resultObjectName + " = new "
					+ resultClassName + "();\n";
			for (OwlService arg : resultVariables) {
				if (arg.getArgument().getSubtypes().isEmpty()) {
					resultObjectDeclaration += TAB + TAB + resultObjectName + ".set"
							+ arg.getName().getContent().replaceAll("[0123456789]", "") + "("
							+ arg.getName().getContent() + ".value);\n";
				}
			}

			resultObjectDeclaration += TAB + TAB + "return " + resultObjectName + ";\n";
		}
		// create result class declaration
		declaredInputs = TAB + "public " + resultClassName + " " + "parseResponse" + "(" + declaredInputs
				+ ") throws Exception{\n";
		declaredInputs += TAB + TAB + "//assign inputs to variables\n";
		for (OwlService arg : inputVariables) {

			declaredInputs += TAB + TAB + "this." + arg.getName().getContent() + ".value" + " = "
					+ arg.getName().getContent() + ";\n";
		}
		declaredInputs += TAB + TAB + "//assign uri parameters to variables\n";
		for (Argument param:uriParameters){
			declaredInputs += TAB + TAB + "this." + param.getName().getContent() + ".value" + " = "
					+ param.getName().getContent() + ";\n";
		}

		// Generate Response class of RESTful web service
		String responseClass = TAB + "@XmlRootElement(name = \"Response\")\n" + TAB + "public class Response {\n";
		for (OwlService arg : resultVariables) {
			responseClass += TAB + TAB + "private String " + arg.getName().getContent().replaceAll("[0123456789]", "")
					+ ";\n";
		}

		responseClass += TAB + "}\n";

		// Variable class declaration
		String variableClassDeclaration = TAB + "public class Variable{\n";
		variableClassDeclaration += TAB + TAB + "public String name;\n" + TAB + TAB + "public String value;\n"  + TAB + TAB + "public String type;\n" + TAB
				+ TAB + "public ArrayList<Variable> subtypes=new ArrayList<Variable>();\n" + TAB +TAB + "Variable(String name, String value, String type){\n";
		variableClassDeclaration += TAB + TAB + TAB + "this.name = name;\n" + TAB + TAB + TAB + "this.value = value;\n"+ TAB + TAB + TAB + "this.type = type;\n";
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


		// return code
		String returnCode = "";
		returnCode = FunctionCodeNode.generateImports(restServiceExists, wsdlServiceExists) + "\npublic class "
				+ createdClassName + "{\n" + TAB;
//		if (wsdlServiceExists) {
//			returnCode += parsedWSDLMap+xmlFromStringMethod+ "\n";
//		}
		returnCode += declaredVariables
				+ "\n" +resultClassDeclaration + variableClassDeclaration +  functionCode + declaredInputs + resultObjectDeclaration + TAB + "}\n}";

		
		operation=instance.createObjects(ProjectName, inputVariables, uriParameters, resultVariables);
		
		return returnCode;
	}
	
	public MDEOperation getOperation(){
		return this.operation;
	}
	
	public ArrayList<OwlService> getInputVariables(){
		return this.inputVariables;
	}
	public ArrayList<OwlService> getOutputVariables(){
		return this.outputVariables;
	}
	public ArrayList<Argument> geturiParameters(){
		return this.uriParameters;
	}
}
