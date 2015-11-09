package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

import java.util.ArrayList;

import edu.uci.ics.jung.graph.Graph;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.CodeGenerator.CodeNode;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;

public class RestFunctionCodeNode extends CodeNode {

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
	public String createFunctionCode(Graph<OwlService, Connector> graph, ArrayList<OwlService> allVariables)
			throws Exception {
		if (service == null || service.getArgument() != null)
			return "";
		String tabIndent = getTab();
		applyTab();
		String code = tabIndent + "protected String " + codeGenerator.getFunctionName(service) + "()  throws Exception"
				+ "{\n" + getCode(allVariables);
		for (OwlService next : graph.getSuccessors(service)) {
			if (next.getArgument() == null && !next.getName().isEmpty()) {
				code += tabIndent + TAB + "return \"" + codeGenerator.getFunctionName(next) + "\";\n";
				code += tabIndent + "}\n";
			}else if (next.getType().equalsIgnoreCase("EndNode")){
				code += tabIndent + TAB + "return \"callEndNode\";\n";
				code += tabIndent + "}\n";
				
			}
		}
		return code;
	}

	@Override
	protected String generateInputSynchronizationCode(Operation operation, ArrayList<OwlService> allVariables) {

		String ret = "";
		ret += "ArrayList<Variable> inputs = new ArrayList<Variable>();\n";
		for (Argument arg : operation.getInputs())
			for (OwlService var : allVariables) {
				if (var.getArgument().equals(arg)) {
					ret += "inputs.add(" + var.getName().getContent().toString() + ");\n";
				}
			}
		return ret;
	}

	@Override
	protected String generateOutputSynchronizationCode(Operation operation, ArrayList<OwlService> allVariables) {

		String ret = "";
		ret += "ArrayList<Variable> outputs = new ArrayList<Variable>();\n";
		for (Argument arg : operation.getOutputs())
			for (OwlService var : allVariables) {
				if (var.getArgument().equals(arg)) {
					if (!var.getArgument().getSubtypes().isEmpty()) {
						for (Argument sub : var.getArgument().getSubtypes()) {
							ret += var.getName().getContent().toString() + ".subtypes.add("
									+ sub.getName().getContent().toString() + ");\n";
						}
					}
					ret += "outputs.add(" + var.getName().getContent().toString() + ");\n";
				}
			}

		ret += "try {\n";
		ret += TAB + "JSONObject json = (JSONObject) JSONValue.parseWithException(result);\n";
		ret += TAB + "for (Variable output:outputs){\n";
		ret += TAB + TAB + "CallRESTfulService.assignResult(json, output);\n";
		ret += TAB + "}\n";
		ret += "} catch (ParseException e) {\n";
		ret += "}\n";

		return ret;
	}

	@Override
	protected String generateOperationCode(Operation operation, ArrayList<OwlService> allVariables) throws Exception {

		String ret = "";
		ret += "String wsUrl =\"" + operation.getDomain().getURI() + operation.getDomain().getResourcePath() + "\";\n";
		for (Argument arg : operation.getUriParameters()) {
			ret += "wsUrl = wsUrl.replace(\"{" + arg.getName().toString() + "}\", " + arg.getName().toString()
					+ ".value);\n";
		}
		ret += "String crudVerb=\"" + operation.getDomain().getCrudVerb() + "\";\n";

		ret += "String result=CallRESTfulService.callService(wsUrl, crudVerb, inputs);\n";

		return ret;
	}

}
