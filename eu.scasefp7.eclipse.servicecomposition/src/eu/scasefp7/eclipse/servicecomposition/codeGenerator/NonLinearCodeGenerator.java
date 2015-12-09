package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Service;
import eu.scasefp7.eclipse.servicecomposition.operationCaller.RAMLCaller;
import eu.scasefp7.eclipse.servicecomposition.repository.WSOntology;
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

	protected ConnectToMDEOntology instance = new ConnectToMDEOntology();
	protected MDEOperation operation;
	
	
	String[] datatypes = new String[] { "string", "long", "int", "float", "double", "dateTime", "boolean" };

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

		for (OwlService service : graph.getVertices()) {
			for (OwlService service2 : graph.getVertices()) {
				if (service.getName().getContent().toString()
						.equalsIgnoreCase(service2.getName().getContent().toString()) && !service.equals(service2)
						&& service.getName().getContent().toString() != "") {
					service2.getName().setContent(service.getName().getContent().toString() + service2.getId());
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

		// detect uri parameters
		for (OwlService service : graph.getVertices()) {
			if (service.getOperation() != null) {
				for (Argument param : service.getOperation().getUriParameters()) {
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
									+ "\",\"\", \"" + arg.getType() + "\");\n";
						}
						restServiceExists = true;
					} else if (service.getOperation().getType().equalsIgnoreCase("SOAP")
							|| service.getOperation().getDomain().isLocal()) {
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
			if (inputVariables.contains(service) || uriParameters.contains(service.getArgument())
					|| service.getArgument().getBelongsToOperation().getType().equalsIgnoreCase("SOAP")
					|| matchedInputs.contains(service)) {

				if (service.getArgument().getSubtypes().isEmpty() && !service.getArgument().isArray()) {
					declaredVariables += TAB + "private Variable " + service.getName().getContent().toString()
							+ " = new Variable(\"" + service.getName().getContent().replaceAll("[0123456789]", "")
							+ "\",\"\", \"" + service.getArgument().getType() + "\");\n";
				} else if (service.getArgument().getSubtypes().isEmpty() && service.getArgument().isArray()) {
					declaredVariables += TAB + "private Variable " + service.getName().getContent().toString()
							+ " = new Variable(\"" + service.getName().getContent().replaceAll("[0123456789]", "")
							+ "\",\"\", \"array\");\n";
				} else if (!service.getArgument().getSubtypes().isEmpty() && service.getArgument().isArray()) {
					declaredVariables += TAB + "private Variable " + service.getName().getContent().toString()
							+ " = new Variable(\"" + service.getName().getContent().replaceAll("[0123456789]", "")
							+ "\",\"\", \"arrayOfObjects\");\n";
				} else {
					declaredVariables += TAB + "private Variable " + service.getName().getContent().toString()
							+ " = new Variable(\"" + service.getName().getContent().replaceAll("[0123456789]", "")
							+ "\",\"\", \"object\");\n";
				}
			}

		}
		String operationResponseObjects = "";
		for (OwlService op : remainingOperations) {
			//if (op.getOperation().getType().equalsIgnoreCase("RESTful")) {
				declaredVariables += TAB + "private " + op.getName().getContent() + "Response " + op.getName().getContent()
						+ "_response = new " + op.getName().getContent() + "Response();\n";
			//}
			operationResponseObjects += TAB + TAB + "public static class " + op.getName().getContent() + "Response {\n";
			String VarDeclaration = "";
			String getSet = "\n\n";
			for (Argument output : op.getOperation().getOutputs()) {
				String subType = output.getType();

				if (output.getSubtypes().isEmpty() && !output.isArray()) {
					VarDeclaration += TAB + TAB + TAB + "private String " + output.getName().getContent().replaceAll("[0123456789]", "")
							+ " = \"\";\n";

					getSet += TAB + TAB + "public void set" + output.getName().getContent().replaceAll("[0123456789]", "") + "(String "
							+ output.getName().getContent().replaceAll("[0123456789]", "") + ") {\n" + TAB + TAB + TAB
							+ "this." + output.getName().getContent().replaceAll("[0123456789]", "") + " = "
							+ output.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
					getSet += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public String get"
							+ output.getName().getContent().replaceAll("[0123456789]", "") + "() {\n" + TAB + TAB + TAB + "return "
							+ output.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
				} else if (output.isArray() && RAMLCaller.stringContainsItemFromList(output.getType(), datatypes)) {
					VarDeclaration += TAB + TAB + TAB + "private ArrayList <String> " + output.getName().getContent().replaceAll("[0123456789]", "")
							+ "= new ArrayList<String>();\n";

					getSet += TAB + TAB + "public void set" + output.getName().getContent().replaceAll("[0123456789]", "") + "(ArrayList<String> "
							+ output.getName().getContent().replaceAll("[0123456789]", "") + ") {\n" + TAB + TAB + TAB
							+ "this." + output.getName().getContent().replaceAll("[0123456789]", "") + " = "
							+ output.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
					getSet += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public ArrayList<String> get"
							+ output.getName().getContent().replaceAll("[0123456789]", "") + "() {\n" + TAB + TAB + TAB + "return "
							+ output.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
				} else if (output.isArray() && !RAMLCaller.stringContainsItemFromList(output.getType(), datatypes)) {
					VarDeclaration += TAB + TAB + TAB + "private ArrayList <" + subType.substring(0, 1).toUpperCase()
							+ subType.substring(1) + "> " + output.getName().getContent().replaceAll("[0123456789]", "") + " = new ArrayList<"
							+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + ">();\n";

					getSet += TAB + TAB + "public void set" + output.getName().getContent().replaceAll("[0123456789]", "") + "(ArrayList<"
							+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + "> "
							+ output.getName().getContent().replaceAll("[0123456789]", "") + ") {\n" + TAB + TAB + TAB
							+ "this." + output.getName().getContent().replaceAll("[0123456789]", "") + " = "
							+ output.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
					getSet += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public ArrayList<"
							+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + "> get"
							+ output.getName().getContent().replaceAll("[0123456789]", "") + "() {\n" + TAB + TAB + TAB + "return "
							+ output.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
				} else if (!output.isArray() && !output.getSubtypes().isEmpty()) {
					VarDeclaration += TAB + TAB + TAB + "private " + subType.substring(0, 1).toUpperCase()
							+ subType.substring(1) + " " + output.getName().getContent().replaceAll("[0123456789]", "") + " = new "
							+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + "();\n";

					getSet += TAB + TAB + "public void set" + output.getName().getContent().replaceAll("[0123456789]", "") + "("
							+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + " "
							+ output.getName().getContent().replaceAll("[0123456789]", "") + ") {\n" + TAB + TAB + TAB
							+ "this." + output.getName().getContent().replaceAll("[0123456789]", "") + " = "
							+ output.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
					getSet += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public "
							+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + " get"
							+ output.getName().getContent().replaceAll("[0123456789]", "") + "() {\n" + TAB + TAB + TAB + "return "
							+ output.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
				}

			}
			operationResponseObjects += VarDeclaration;
			operationResponseObjects += getSet;
			operationResponseObjects += TAB + TAB + "}\n";
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
			}else if(input.getArgument().getType().equals("int")){
				declaredInputs += "Integer " + input.getName().getContent();
			} else {
				declaredInputs += input.getArgument().getType().substring(0, 1).toUpperCase()
						+ input.getArgument().getType().substring(1) + " " + input.getName().getContent();
			}
		}
		for (Argument param : uriParameters) {
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
		// ArrayList<OwlService> resultVariables = new ArrayList<OwlService>();
		for (OwlService output : outputVariables) {
			if (!output.getArgument().getSubtypes().isEmpty()) {
				resultClassDeclaration += generateResultObjects(output.getArgument());
			}
		}
		resultClassDeclaration += operationResponseObjects;

//		if (outputVariables.size() == 1 && subOutputVariables.size() == 0) {
//			resultClassName = outputVariables.get(0).getArgument().getType();
//			resultObjectDeclaration = TAB + TAB + "return " + outputVariables.get(0).getName().getContent() + ";";
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
			for (OwlService arg : outputVariables) {
				if (!constractorInstanceVars.isEmpty()) {
					constractorInstanceVars += ", ";
				}
				constractorInstanceVars += arg.getArgument().getBelongsToOperation().getName().getContent()
						+ "_response.get" + arg.getName().toString().replaceAll("[0123456789]", "")  + "()";
				if (!constractorVars.isEmpty()) {
					constractorVars += ", ";
				}
				constractorVarsBody += TAB + TAB + TAB + "this." + arg.getName().getContent().replaceAll("[0123456789]", "") + " = "
						+ arg.getName().getContent().replaceAll("[0123456789]", "") + ";\n";
				String type = arg.getArgument().getType();
				if (arg.getArgument().getSubtypes().isEmpty() && !arg.getArgument().isArray()) {
					constractorVars += "String " + arg.getName().getContent().replaceAll("[0123456789]", "") ;
					resultClassDeclaration += TAB + TAB + "private String " + arg.getName().getContent().replaceAll("[0123456789]", "") + " = \"\";\n";
					resultClassConstractor1 += TAB + TAB + TAB + "this." + arg.getName().getContent().replaceAll("[0123456789]", "") + " = \"\";\n";
				} else if (arg.getArgument().isArray()
						&& RAMLCaller.stringIsItemFromList(arg.getArgument().getType(), datatypes)) {
					constractorVars += "ArrayList <String> " + arg.getName().getContent().replaceAll("[0123456789]", "") ;
					resultClassDeclaration += TAB + TAB + "private ArrayList <String> " + arg.getName().getContent().replaceAll("[0123456789]", "")
							+ " = new ArrayList<String>();\n";
					resultClassConstractor1 += TAB + TAB + TAB + "this." + arg.getName().getContent().replaceAll("[0123456789]", "")
							+ " = new ArrayList<String>();\n";
				} else if (arg.getArgument().isArray()
						&& !RAMLCaller.stringIsItemFromList(arg.getArgument().getType(), datatypes)) {
					constractorVars += "ArrayList <" + type.substring(0, 1).toUpperCase() + type.substring(1) + "> "
							+ arg.getName().getContent().replaceAll("[0123456789]", "") ;
					resultClassDeclaration += TAB + TAB + "private ArrayList <" + type.substring(0, 1).toUpperCase()
							+ type.substring(1) + "> " + arg.getName().getContent().replaceAll("[0123456789]", "") + " = new ArrayList<"
							+ type.substring(0, 1).toUpperCase() + type.substring(1) + ">();\n";
					resultClassConstractor1 += TAB + TAB + TAB + "this." + arg.getName().getContent().replaceAll("[0123456789]", "")
							+ " = new ArrayList<" + type.substring(0, 1).toUpperCase() + type.substring(1) + ">();\n";

				} else if (!arg.getArgument().isArray() && !arg.getArgument().getSubtypes().isEmpty()) {
					constractorVars += type.substring(0, 1).toUpperCase() + type.substring(1) + " "
							+ arg.getName().getContent().replaceAll("[0123456789]", "") ;
					resultClassDeclaration += TAB + TAB + "private " + type.substring(0, 1).toUpperCase()
							+ type.substring(1) + " " + arg.getName().getContent().replaceAll("[0123456789]", "") + " = new "
							+ type.substring(0, 1).toUpperCase() + type.substring(1) + "();\n";
					resultClassConstractor1 += TAB + TAB + TAB + "this." + arg.getName().getContent().replaceAll("[0123456789]", "") + " = new "
							+ type.substring(0, 1).toUpperCase() + type.substring(1) + "();\n";
				}

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
			// resultClassConstractor1 += TAB + TAB + "public Response() {\n";
			// for (OwlService arg : outputVariables) {
			// String type = arg.getArgument().getType();
			// if (arg.getArgument().getSubtypes().isEmpty()&&
			// !arg.getArgument().isArray()) {
			// resultClassConstractor1 += TAB + TAB + TAB + "this." +
			// arg.getName().getContent() + " = \"\";\n";
			// } else if (arg.getArgument().isArray()
			// && RAMLCaller.stringIsItemFromList(arg.getArgument().getType(),
			// datatypes)) {
			// resultClassConstractor1 += TAB + TAB + TAB + "this." +
			// arg.getName().getContent()
			// + " = new ArrayList<String>();\n";
			// } else if (arg.getArgument().isArray()
			// && !RAMLCaller.stringIsItemFromList(arg.getArgument().getType(),
			// datatypes)) {
			//
			// resultClassConstractor1 += TAB + TAB + TAB + "this." +
			// arg.getName().getContent() + " = new ArrayList<"
			// + type.substring(0, 1).toUpperCase() + type.substring(1) +
			// ">();\n";
			// }else if (!arg.getArgument().isArray() &&
			// !arg.getArgument().getSubtypes().isEmpty()){
			// resultClassConstractor1 += TAB + TAB + TAB + "this." +
			// arg.getName().getContent() + " = new " + type.substring(0,
			// 1).toUpperCase()
			// + type.substring(1) + "();\n";
			// }
			// }
			//
			// resultClassConstractor1 += TAB + TAB + "}\n\n";

			for (OwlService arg : outputVariables) {
				String type = arg.getArgument().getType();
				if (arg.getArgument().getSubtypes().isEmpty() && !arg.getArgument().isArray()) {
					resultClassDeclaration += TAB + TAB + "public void set" + arg.getName().getContent().replaceAll("[0123456789]", "") + "(String "
							+ arg.getName().getContent().replaceAll("[0123456789]", "") + ") {\n" + TAB + TAB + TAB
							+ "this." + arg.getName().getContent().replaceAll("[0123456789]", "") + " = "
							+ arg.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
					resultClassDeclaration += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public String get"
							+ arg.getName().getContent().replaceAll("[0123456789]", "") + "() {\n" + TAB + TAB + TAB + "return "
							+ arg.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
				} else if (arg.getArgument().isArray()
						&& RAMLCaller.stringIsItemFromList(arg.getArgument().getType(), datatypes)) {
					resultClassDeclaration += TAB + TAB + "public void set" + arg.getName().getContent().replaceAll("[0123456789]", "")
							+ "(ArrayList<String> " + arg.getName().getContent().replaceAll("[0123456789]", "")
							+ ") {\n" + TAB + TAB + TAB + "this." + arg.getName().getContent() + " = "
							+ arg.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
					resultClassDeclaration += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public ArrayList<String> get"
							+ arg.getName().getContent().replaceAll("[0123456789]", "") + "() {\n" + TAB + TAB + TAB + "return "
							+ arg.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
				} else if (arg.getArgument().isArray()
						&& !RAMLCaller.stringIsItemFromList(arg.getArgument().getType(), datatypes)) {

					resultClassDeclaration += TAB + TAB + "public void set" + arg.getName().getContent().replaceAll("[0123456789]", "") + "(ArrayList<"
							+ type.substring(0, 1).toUpperCase() + type.substring(1) + "> "
							+ arg.getName().getContent().replaceAll("[0123456789]", "") + ") {\n" + TAB + TAB + TAB
							+ "this." + arg.getName().getContent().replaceAll("[0123456789]", "") + " = "
							+ arg.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
					resultClassDeclaration += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public ArrayList<"
							+ type.substring(0, 1).toUpperCase() + type.substring(1) + "> get"
							+ arg.getName().getContent().replaceAll("[0123456789]", "") + "() {\n" + TAB + TAB + TAB + "return "
							+ arg.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
				} else if (!arg.getArgument().isArray() && !arg.getArgument().getSubtypes().isEmpty()) {
					resultClassDeclaration += TAB + TAB + "public void set" + arg.getName().getContent().replaceAll("[0123456789]", "") + "("
							+ type.substring(0, 1).toUpperCase() + type.substring(1) + " "
							+ arg.getName().getContent().replaceAll("[0123456789]", "") + ") {\n" + TAB + TAB + TAB
							+ "this." + arg.getName().getContent().replaceAll("[0123456789]", "") + " = "
							+ arg.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
					resultClassDeclaration += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public "
							+ type.substring(0, 1).toUpperCase() + type.substring(1) + " get"
							+ arg.getName().getContent().replaceAll("[0123456789]", "") + "() {\n" + TAB + TAB + TAB + "return "
							+ arg.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
				}
			}

			resultClassDeclaration += TAB + "}\n\n";

			resultObjectDeclaration = TAB + TAB + "//create class instance to be returned\n";
			resultObjectDeclaration += resultClassInstance;

			resultObjectDeclaration += TAB + TAB + "return " + resultObjectName + ";\n";
		}
		// create result class declaration
		declaredInputs = TAB + "public " + resultClassName + " " + "parseResponse" + "(" + declaredInputs
				+ ") throws Exception{\n";
		declaredInputs += TAB + TAB + "//assign inputs to variables\n";
		for (OwlService arg : inputVariables) {
			String type = arg.getArgument().getType();

			declaredInputs += TAB + TAB + "if (" + arg.getName().getContent() + " == null) {\n";
			declaredInputs += TAB + TAB + TAB + "this." + arg.getName().getContent() + ".value" + " = \"\";\n";
			declaredInputs += TAB + TAB + "} else {\n";
			if (type.equalsIgnoreCase("String")) {
				declaredInputs += TAB + TAB + TAB + "this." + arg.getName().getContent() + ".value" + " = "
						+ arg.getName().getContent() + ";\n";
				
			}else if(type.equalsIgnoreCase("int")){
				declaredInputs += TAB + TAB + TAB + "this." + arg.getName().getContent() + ".value" + " = Integer.toString("
						+ arg.getName().getContent() + ");\n";
			} else {
				declaredInputs += TAB + TAB + TAB + "this." + arg.getName().getContent() + ".value" + " = "
						+ type.substring(0, 1).toUpperCase() + type.substring(1) + ".toString("
						+ arg.getName().getContent() + ");\n";
			}
			declaredInputs += TAB + TAB + "}\n";
		}
		declaredInputs += TAB + TAB + "//assign uri parameters to variables\n";
		for (Argument param : uriParameters) {
			declaredInputs += TAB + TAB + "if (" + param.getName().getContent() + " == null) {\n";
			declaredInputs += TAB + TAB + TAB + "this." + param.getName().getContent() + ".value" + " = \"\";\n";
			declaredInputs += TAB + TAB + "} else {\n";
			declaredInputs += TAB + TAB + TAB + "this." + param.getName().getContent() + ".value" + " = "
					+ param.getName().getContent() + ";\n";
			declaredInputs += TAB + TAB + "}\n";
		}

		// Generate Response class of RESTful web service
		String responseClass = TAB + "@XmlRootElement(name = \"Response\")\n" + TAB + "public class Response {\n";
		for (OwlService arg : outputVariables) {
			responseClass += TAB + TAB + "private String " + arg.getName().getContent().replaceAll("[0123456789]", "")
					+ ";\n";
		}

		responseClass += TAB + "}\n";

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
		declaredInputs += TAB + TAB + "while(_nextCall!=null){\n";
		declaredInputs += TAB + TAB + TAB + "_nextCall = call(_nextCall);\n";
		// update matchedIO values

		declaredInputs += TAB + TAB + TAB + "//update matchedIO variable values\n";

		for (OwlService matchedOutput : matchedOutputs) {
			for (OwlService matchedInput : graph.getSuccessors(matchedOutput)) {
//				if (matchedInput.getArgument().getBelongsToOperation().getType().equalsIgnoreCase("SOAP")) {
//					declaredInputs += TAB + TAB + TAB + matchedInput.getName().getContent().toString() + ".value = "
//							+ matchedOutput.getName().getContent().toString() + ".value;\n";
//				} else {
					String ret=".get"+matchedOutput.getName().getContent().replaceAll("[0123456789]", "")+"()";
					ret=roadToSub(matchedOutput, graph, ret);
					declaredInputs += TAB + TAB + TAB + matchedInput.getName().getContent().toString() + ".value = "
							+ matchedOutput.getArgument().getBelongsToOperation().getName()+ "_response"+ret+";\n";
				//}

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

		returnCode += declaredVariables + "\n" + resultClassDeclaration + variableClassDeclaration + functionCode
				+ declaredInputs + resultObjectDeclaration + TAB + "}\n}";

		operation = instance.createObjects(ProjectName, inputVariables, uriParameters, outputVariables, graph);

		return returnCode;
	}

	public MDEOperation getOperation() {
		return this.operation;
	}

	public ArrayList<OwlService> getInputVariables() {
		return this.inputVariables;
	}

	public ArrayList<OwlService> getOutputVariables() {
		return this.outputVariables;
	}

	public ArrayList<Argument> geturiParameters() {
		return this.uriParameters;
	}

	private String generateResultObjects(Argument output) {
		String type = output.getType();
		String ObjectClasses = "\n";
		ObjectClasses += TAB + "public static class " + type.substring(0, 1).toUpperCase() + type.substring(1) + " {\n";
		String VarDeclaration = "";
		String ConstractorClasses = "";
		ConstractorClasses += TAB + TAB + type.substring(0, 1).toUpperCase() + type.substring(1) + "(){\n";
		ConstractorClasses += TAB + TAB + "}\n\n";
		ConstractorClasses += TAB + TAB + type.substring(0, 1).toUpperCase() + type.substring(1) + "(";
		String constractorVars = "";
		String getSet = "\n\n";
		for (Argument sub : output.getSubtypes()) {
			String subType = sub.getType();
			if (!constractorVars.isEmpty()) {
				constractorVars += ", ";
			}
			if (sub.getSubtypes().isEmpty() && !sub.isArray()) {
				VarDeclaration += TAB + TAB + "private String " + sub.getName().getContent() + " = \"\";\n";
				constractorVars += "String " + sub.getName().getContent();
				getSet += TAB + TAB + "public void set" + sub.getName().getContent() + "(String "
						+ sub.getName().getContent().replaceAll("[0123456789]", "") + ") {\n" + TAB + TAB + TAB
						+ "this." + sub.getName().getContent() + " = "
						+ sub.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
				getSet += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public String get" + sub.getName().getContent()
						+ "() {\n" + TAB + TAB + TAB + "return " + sub.getName().getContent() + ";\n" + TAB + TAB
						+ "}\n";
			} else if (sub.isArray() && RAMLCaller.stringContainsItemFromList(sub.getType(), datatypes)) {
				VarDeclaration += TAB + TAB + "private ArrayList <String> " + sub.getName().getContent()
						+ "= new ArrayList<String>();\n";
				constractorVars += "ArrayList <String> " + sub.getName().getContent();
				getSet += TAB + TAB + "public void set" + sub.getName().getContent() + "(ArrayList<String> "
						+ sub.getName().getContent().replaceAll("[0123456789]", "") + ") {\n" + TAB + TAB + TAB
						+ "this." + sub.getName().getContent() + " = "
						+ sub.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
				getSet += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public ArrayList<String> get"
						+ sub.getName().getContent() + "() {\n" + TAB + TAB + TAB + "return "
						+ sub.getName().getContent() + ";\n" + TAB + TAB + "}\n";
			} else if (sub.isArray() && !RAMLCaller.stringContainsItemFromList(sub.getType(), datatypes)) {
				VarDeclaration += TAB + TAB + "private ArrayList <" + subType.substring(0, 1).toUpperCase()
						+ subType.substring(1) + "> " + sub.getName().getContent() + " = new ArrayList<"
						+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + ">();\n";
				constractorVars += "ArrayList <" + subType.substring(0, 1).toUpperCase() + subType.substring(1) + "> "
						+ sub.getName().getContent();
				getSet += TAB + TAB + "public void set" + sub.getName().getContent() + "(ArrayList<"
						+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + "> "
						+ sub.getName().getContent().replaceAll("[0123456789]", "") + ") {\n" + TAB + TAB + TAB
						+ "this." + sub.getName().getContent() + " = "
						+ sub.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
				getSet += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public ArrayList<"
						+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + "> get"
						+ sub.getName().getContent() + "() {\n" + TAB + TAB + TAB + "return "
						+ sub.getName().getContent() + ";\n" + TAB + TAB + "}\n";
			} else if (!sub.isArray() && !sub.getSubtypes().isEmpty()) {
				VarDeclaration += TAB + TAB + "private " + subType.substring(0, 1).toUpperCase() + subType.substring(1)
						+ " " + sub.getName().getContent() + " = new " + subType.substring(0, 1).toUpperCase()
						+ subType.substring(1) + "();\n";
				constractorVars += subType.substring(0, 1).toUpperCase() + subType.substring(1) + " "
						+ sub.getName().getContent();
				getSet += TAB + TAB + "public void set" + sub.getName().getContent() + "("
						+ subType.substring(0, 1).toUpperCase() + subType.substring(1) + " "
						+ sub.getName().getContent().replaceAll("[0123456789]", "") + ") {\n" + TAB + TAB + TAB
						+ "this." + sub.getName().getContent() + " = "
						+ sub.getName().getContent().replaceAll("[0123456789]", "") + ";\n" + TAB + TAB + "}\n";
				getSet += TAB + TAB + "@XmlElement\n" + TAB + TAB + "public " + subType.substring(0, 1).toUpperCase()
						+ subType.substring(1) + " get" + sub.getName().getContent() + "() {\n" + TAB + TAB + TAB
						+ "return " + sub.getName().getContent() + ";\n" + TAB + TAB + "}\n";
			}

		}
		VarDeclaration += "\n\n";
		ObjectClasses += VarDeclaration;
		ConstractorClasses += constractorVars + "){\n";
		for (Argument sub : output.getSubtypes()) {
			ConstractorClasses += TAB + TAB + TAB + "this." + sub.getName().getContent() + " = "
					+ sub.getName().getContent() + ";\n";
		}
		ConstractorClasses += TAB + TAB + "}\n";
		ObjectClasses += ConstractorClasses;
		ObjectClasses += getSet;
		ObjectClasses += "}\n";

		for (Argument sub : output.getSubtypes()) {
			if (!sub.getSubtypes().isEmpty()) {
				ObjectClasses += generateResultObjects(sub);
			}

		}

		return ObjectClasses;

	}
	private String roadToSub(OwlService sub,Graph<OwlService, Connector> graph, String ret){
		for (OwlService service: graph.getPredecessors(sub)){
			if (service.getArgument()!=null){
				ret=".get"+service.getName().getContent().replaceAll("[0123456789]", "")+"()"+ret;
				ret=roadToSub(service, graph, ret);
			}else{
				return ret;
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

	public void setUriParameters(ArrayList<Argument> uriParameters) {
		this.uriParameters = uriParameters;
	}

}
