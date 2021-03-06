package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import edu.uci.ics.jung.graph.Graph;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.CodeGenerator.CodeNode;
import eu.scasefp7.eclipse.servicecomposition.codeInterpreter.Value;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.RequestHeader;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.toolbar.RunWorkflow;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;

public class RestFunctionCodeNode extends CodeNode {
	/**
	 * tab indentation
	 */
	protected static String TAB = "   ";
	protected CodeGenerator codeGenerator;

	public static CodeNode createInstance(OwlService service, CodeGenerator codeGenerator) {
		return new RestFunctionCodeNode(service, codeGenerator);
	}

	public RestFunctionCodeNode(OwlService service, CodeGenerator codeGenerator) {
		super(service);
		this.codeGenerator = codeGenerator;
		outputSynchronizer = "this.";
	}

	@Override
	public String createFunctionCode(Graph<OwlService, Connector> graph, ArrayList<OwlService> allVariables,
			boolean hasBodyInput, boolean isRepeated, boolean hasOutput, ArrayList<Operation> repeatedOperations) throws Exception {
		if (service == null || service.getArgument() != null)
			return "";
		String tabIndent = getTab();
		applyTab();
		String code = tabIndent + "protected String " + codeGenerator.getFunctionName(service);
		if (isRepeated) {
			code += "(int i)";
		} else {
			code += "()";
		}

		code += "  throws Exception" + "{\n" + getCode(allVariables, hasBodyInput, isRepeated, graph, hasOutput);
		String matchedInputName = "";
		if (isRepeated) {
			for (OwlService matched : allVariables) {
				if (matched.getisMatchedIO() && matched.getArgument().getBelongsToOperation().getName().toString()
						.equals(service.getName().toString())) {

					for (OwlService output : graph.getPredecessors(matched)) {
						boolean isMemberOfArray = false;
						for (Object parent : output.getArgument().getParent()) {
							if (parent instanceof Argument)
								if (((Argument) parent).isArray()) {
									isMemberOfArray = true;
								}
						}
						if (output.getisMatchedIO() && isMemberOfArray) {
							matchedInputName = matched.getName().getJavaValidContent();
						}
					}
				}
			}
		}
		for (OwlService next : graph.getSuccessors(service)) {
			if (next.getArgument() == null && !next.getName().isEmpty()) {
				if (isRepeated) {
					code += tabIndent + TAB + "if (i == " + matchedInputName + ".size() - 1) {\n";
					code += tabIndent + TAB + TAB + "return \"" + codeGenerator.getFunctionName(next) + "\";\n";
					code += tabIndent + TAB + "} else {\n";
					code += tabIndent + TAB + TAB + "return \"" + codeGenerator.getFunctionName(service) + "\";\n";
					code += tabIndent + TAB + "}\n";
				} else {
					code += tabIndent + TAB + "return \"" + codeGenerator.getFunctionName(next) + "\";\n";
				}
				code += tabIndent + "}\n";
			} else if (next.getType().equalsIgnoreCase("EndNode")) {
				if (isRepeated) {
					code += tabIndent + TAB + "if (i == " + matchedInputName + ".size() - 1) {\n";
					code += tabIndent + TAB + TAB + "return \"callEndNode\";\n";
					code += tabIndent + TAB + "} else {\n";
					code += tabIndent + TAB + TAB + "return \"" + codeGenerator.getFunctionName(service) + "\";\n";
					code += tabIndent + TAB + "}\n";
				} else {
					code += tabIndent + TAB + "return \"callEndNode\";\n";
				}
				code += tabIndent + "}\n";

			}
		}
		return code;
	}

	@Override
	protected String generateInputSynchronizationCode(Operation operation, ArrayList<OwlService> allVariables,
			boolean hasBodyInput, boolean isRepeated, Graph<OwlService, Connector> graph) {

		String ret = "";

		String body = "";
		if (hasBodyInput) {
			if (operation.getDomain().getURI().contains("mailgun")) {
				ret += "List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();\n";
				for (Argument input : operation.getInputs()) {
					if (input.isTypeOf().equals("FormEncodedParameter") && !input.getOwlService().getisMatchedIO()) {
						ret += "urlParameters.add(new BasicNameValuePair("
								+ input.getName().getJavaValidContent().toString() + ".name , "
								+ operation.getName().toString() + "_request.get"
								+ input.getName().getJavaValidContent().toString() + "()));\n";
					}
					// else {
					// ret += "urlParameters.add(new BasicNameValuePair(" +
					// input.getName().getJavaValidContent().toString()
					// + ".name , " +
					// input.getName().getJavaValidContent().toString() +
					// ".value));\n";
					// }
				}

				ret += "StringBuilder sb = new StringBuilder();\n" + "boolean first = true;\n" +

						"for (NameValuePair pair : urlParameters) {\n" + TAB + "if (first)\n" + TAB + TAB
						+ "first = false;\n" + TAB + "else\n" + TAB + TAB + "sb.append(\"&\");\n" + TAB +

						"sb.append(URLEncoder.encode(pair.getName(), \"UTF-8\"));\n" + TAB + "sb.append(\"=\");\n" + TAB
						+ "sb.append(URLEncoder.encode(pair.getValue(), \"UTF-8\"));\n" + TAB + "}\n"
						+ "String entity = sb.toString();\n";

			} else {
				body += "Gson body = new Gson();\n";
				body += "String entity = body.toJson(" + operation.getName().toString() + "_request);\n";
				
			}
		} else {
			ret += "String entity = \"\";\n";
			
		}

		ret += "ArrayList<Variable> inputs = new ArrayList<Variable>();\n";
		if (operation.getDomain().getURI().contains("mailgun")) {
			ret += "inputs.add(apikey);\n";
		}
		ArrayList<Argument> inputs = new ArrayList<Argument>();
		for (Argument input : operation.getInputs()) {
			
				if (input.getSubtypes().isEmpty()) {
					inputs.add(input);
				}
				for (Argument sub : input.getSubtypes()) {
					
						FunctionCodeNode.getSubtypes(inputs, sub, true);
					

				}
			
		}
		for (Argument arg : inputs)
			for (OwlService var : allVariables) {
				if (var.getArgument().equals(arg) && var.getId() == arg.getOwlService().getId()) {
					boolean isMemberOfArray = false;
					for (OwlService predesseccor : graph.getPredecessors(var)) {

						for (Object parent : predesseccor.getArgument().getParent()) {
							if (parent instanceof Argument)
								if (((Argument) parent).isArray()) {
									isMemberOfArray = true;
								}
						}
					}
					if (arg.isTypeOf().equals("QueryParameter")) {

						ret += "inputs.add(" + var.getName().getJavaValidContent().toString();
						if (isRepeated && var.getisMatchedIO() && isMemberOfArray) {
							ret += ".get(i)";
						}
						ret += ");\n";
					} else if (arg.isTypeOf().equals("BodyParameter")){
						if (isRepeated && var.getisMatchedIO() && isMemberOfArray){
							ArrayList<String> classNames = new ArrayList<String>();
							OwlService initialArray = RunWorkflow.getInitialArray(var, graph, true);
							String varSet = ".set"
									+ var.getName().getJavaValidContent().replaceAll("[0123456789]", "");
							varSet = NonLinearCodeGenerator.roadToSub(initialArray, var, graph, varSet, "0", classNames, false);
							
							ret += operation.getName().toString() + "_request" + varSet + "(" + var.getName().getJavaValidContent() + ".get(i).value);\n";
						}
					}
				}
			}
		ret += body;
		// request headers
		ret += "ArrayList<Variable> requestHeaderList = new  ArrayList<Variable>();\n";
		if (operation.getRequestHeaders() != null && !operation.getRequestHeaders().isEmpty()) {
			for (RequestHeader header : operation.getRequestHeaders()) {
				ret += "requestHeaderList.add(" + header.getName().replaceAll("[^A-Za-z0-9()_\\[\\]]", "") + ");\n";
			}
		}
		return ret;
	}

	@Override
	protected String generateOutputSynchronizationCode(Operation operation, ArrayList<OwlService> allVariables,
			boolean isRepeated, Graph<OwlService, Connector> graph) {

		String ret = "";
		String var = "";
		for (OwlService variable : allVariables) {
			if (variable.getArgument().getObjectOrArray()) {
				String type = variable.getArgument().getName().toString();
				var += "gsonBuilder.registerTypeAdapter(" + type.substring(0, 1).toUpperCase() + type.substring(1)
						+ ".class, new " + type.substring(0, 1).toUpperCase() + type.substring(1)
						+ "Deserializer());\n";
			}
		}

		ret += "GsonBuilder gsonBuilder = new GsonBuilder();\n" + var + "Gson gson = gsonBuilder.create();\n";

		// ret += "Gson gson = new Gson();\n";

		ret += operation.getName().toString() + "_response = gson.fromJson(result, " + operation.getName().toString()
				+ "Response.class);\n";
		if (isRepeated) {
			ret += operation.getName().toString() + "_response_arraylist.add(" + operation.getName().toString()
					+ "_response);\n";
		}
		// ret += "ArrayList<Variable> outputs = new ArrayList<Variable>();\n";
		//
		// for (OwlService var : outputVariables) {
		//
		// if (!var.getArgument().getSubtypes().isEmpty()) {
		// for (OwlService sub : graph.getSuccessors(var)) {
		// if (sub.getArgument() != null && !sub.getisMatchedIO()) {
		// ret += addSubtypes(sub, var, graph);
		// }
		// }
		// }
		// ret += "outputs.add(" +
		// var.getName().getJavaValidContent().toString() +
		// ");\n";
		//
		// }
		//
		// ret += "try {\n";
		// ret += TAB + "JSONObject json = (JSONObject)
		// JSONValue.parseWithException(result);\n";
		// ret += TAB + "for (Variable output:outputs){\n";
		// ret += TAB + TAB + "CallRESTfulService.assignResult(json,
		// output);\n";
		// ret += TAB + "}\n";
		// ret += "} catch (ParseException e) {\n";
		// ret += "}\n";

		return ret;
	}

	@Override
	protected String generateOperationCode(Operation operation, ArrayList<OwlService> allVariables, boolean isRepeated)
			throws Exception {

		String ret = "";
		if (operation.getDomain().getResourcePath() != null) {
			ret += "String wsUrl =\"" + operation.getDomain().getURI() + operation.getDomain().getResourcePath()
					+ "\";\n";
		} else {
			ret += "String wsUrl =\"" + operation.getDomain().getURI() + "\";\n";
		}
		for (Argument arg : operation.getInputs()) {
			if (arg.isTypeOf().equals("URIParameter")) {
				for (OwlService var : allVariables) {
					if (var.getArgument().equals(arg) && var.getId() == arg.getOwlService().getId()) {
						ret += "wsUrl = wsUrl.replace(\"{"
								+ var.getName().getJavaValidContent().replaceAll("[0123456789]", "") + "}\", "
								+ var.getName().getJavaValidContent().toString();
						if (isRepeated) {
							ret += ".get(i)";
						}
						ret += ".value);\n";
					}
				}
			}
		}
		ret += "String crudVerb=\"" + operation.getDomain().getCrudVerb() + "\";\n";
		if (operation.getDomain().getSecurityScheme() != null) {
			if (operation.getDomain().getSecurityScheme().equalsIgnoreCase("Basic Authentication")) {
				ret += "boolean hasAuth= true;\n";
				ret += "String auth= username.value+\":\"+password.value;\n";
			}
		} else {
			ret += "boolean hasAuth= false;\n";
			ret += "String auth=\"\";\n";
		}

		ret += "String result=CallRESTfulService.callService(wsUrl, crudVerb, inputs, entity, hasAuth, auth, requestHeaderList);\n";

		return ret;
	}

	// private String addSubtypes(OwlService sub, OwlService var, final
	// Graph<OwlService, Connector> graph) {
	// String ret="";
	// ret += var.getName().getJavaValidContent().toString() + ".subtypes.add("
	// +
	// sub.getName().getJavaValidContent().toString()
	// + ");\n";
	// for (OwlService subsub : graph.getSuccessors(sub)) {
	// if (subsub.getArgument() != null && !subsub.getisMatchedIO()) {
	// ret += addSubtypes(subsub, sub, graph);
	// }
	// }
	// return ret;
	// }

}
