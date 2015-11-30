package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

import java.util.ArrayList;

import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import edu.uci.ics.jung.graph.Graph;

/**
 * <h1>CodeGenerator</h1> This is the base class for generating code from an OWL
 * graph.
 * 
 * @author Manios Krasanakis
 */
public abstract class CodeGenerator {
	/**
	 * <h1>generateCode</h1> This function generates the appropriate code for
	 * turning an OwlService graph into <i>Java</i> code.
	 * 
	 * @param graph
	 *            : a graph of OwlServices to generate the code from
	 * @param functionName
	 *            : the base name of the function or class of the generated code
	 * @param addConnectionsToGraph
	 *            : if false then the graph is not altered in any way
	 * @return the code generated
	 * @throws Exception
	 *             if an error has occurred
	 */
	public abstract String generateCode(Graph<OwlService, Connector> graph, String functionName,
			boolean addConnectionsToGraph, String ProjectName) throws Exception;

	// the tab character
	protected static String TAB = "   ";

	/**
	 * <h1>CodeNode</h1> This class contains operations that enable the correct
	 * call of various operations, as well as their synchronization with global
	 * variables. For optimization reasons, code is generated only when the
	 * function <code>getCode</code> is called.
	 */
	public static abstract class CodeNode {
		protected String code = "";
		protected OwlService service = null;
		protected boolean updateCode = false;
		protected int tabs = 0;
		protected String outputSynchronizer = "";

		/**
		 * Constructor for the code node from its basic service.
		 * 
		 * @param service
		 */
		public CodeNode(OwlService service) {
			this.service = service;
			updateCode = true;
		}

		/**
		 * <h1>applyTab</h1> Indent the code generated by this
		 * <code>CodeNode</h1> by 1 tab.
		 */
		public void applyTab() {
			tabs++;
			updateCode = true;
		}

		/**
		 * <h1>getCode</h1> Generated code for the given service operation
		 * contents. A list of all variables is needed in order to synchronize
		 * operation variables.
		 * 
		 * @param allVariables
		 * @return the code of the service
		 * @throws Exception
		 */
		public String getCode(ArrayList<OwlService> allVariables) throws Exception {
			if (updateCode)
				updateCode(allVariables);
			return code;
		}

		/**
		 * <h1>getTab</h1>
		 * 
		 * @return the indentation before each code line
		 */
		public String getTab() {
			String tabIndent = "";
			for (int tab = 0; tab < tabs; tab++)
				tabIndent += TAB;
			return tabIndent;
		}

		/**
		 * <h1>updateCode</h1> Updates the given code given the list of all
		 * variables. This is called by <code>getCode</code> whenever changes
		 * are detected (changes in variables are <b>NOT</b> detected
		 * automatically).
		 * 
		 * @param allVariables
		 * @throws Exception
		 */
		protected void updateCode(ArrayList<OwlService> allVariables) throws Exception {
			Operation operation = service != null ? service.getOperation() : null;
			code = "";
			if (operation != null) {
				code += "//call service: " + operation.getName().getContent() + "\n";
				code += generateInputSynchronizationCode(operation, allVariables);
				code += generateOperationCode(operation, allVariables);
				code += generateOutputSynchronizationCode(operation, allVariables);
			}
			String lines[] = code.split("\n");
			code = "";
			String tabIndent = "";
			for (int tab = 0; tab < tabs; tab++)
				tabIndent += TAB;
			for (String line : lines) {
				if (line.trim().isEmpty())
					continue;
				code += tabIndent + line + "\n";
			}
			updateCode = false;
		}

		/**
		 * <h1>generateInputSynchronizationCode</h1> Generates code for
		 * synchronizing Operation inputs and the list of all variables. (Takes
		 * into account class members.)
		 * 
		 * @param operation
		 * @param allVariables
		 * @return the generated code
		 */
		protected String generateInputSynchronizationCode(Operation operation, ArrayList<OwlService> allVariables) {
			// create synchronization with other variables
			String finalizeCommands = "";
			String inputName = "";
			for (Argument arg : operation.getInputs()) {
				for (OwlService input : allVariables) {
					if (input.getArgument().equals(arg)) {
						inputName = input.getName().getContent();
					}
				}
				for (Argument sub : arg.getSubtypes()) {
					for (OwlService subinput : allVariables) {
						if (subinput.getArgument().equals(sub))
							finalizeCommands += inputName + "." + subinput.getName().getContent() + " = "
									+ subinput.getName().getContent() + ";\n";
					}
				}
			}
			return finalizeCommands;
		}

		/**
		 * <h1>generateOperationCode</h1> Generates code for calling an
		 * operation.
		 * 
		 * @param operation
		 * @return the generated code
		 * @throws Exception
		 */
		protected String generateOperationCode(Operation operation, ArrayList<OwlService> allVariables)
				throws Exception {
			// detect outputs
			if (operation.getOutputs().size() > 1)
				throw new Exception("Operation " + operation.getName().getContent() + " has more than one outputs");
			String outputList = "";
			for (Argument arg : operation.getOutputs()) {
				for (OwlService output : allVariables) {
					if (output.getArgument().equals(arg)) {
						if (!outputList.isEmpty())
							outputList += ", ";
						outputList += output.getName().getContent().toString();
					}
				}
			}
			if (operation.getOutputs().size() != 1)
				outputList = "(" + outputList + ")";
			// detect inputs
			String inputList = "";
			for (Argument arg : operation.getInputs()) {
				for (OwlService input : allVariables) {
					if (input.getArgument().equals(arg)) {
						if (!inputList.isEmpty())
							inputList += ", ";
						inputList += input.getName().getContent().replaceAll("[0123456789]", "");
					}
				}
			}
			String ret = "ArrayList<Variable> inputs=new ArrayList<Variable>();\n";
			if (!inputList.isEmpty()) {
				for (Argument arg : operation.getInputs()) {
					for (OwlService input : allVariables) {
						if (input.getArgument().equals(arg)) {
							ret = ret + "inputs.add(" + input.getName().getContent() + ");\n";
						}
					}
				}
			}
			return ret + outputList + ".value" + " = " + "PythonCaller.call(\"" + outputList + " = "
					+ operation.getName().getContent() + "(" + inputList + ")\",inputs);\n";
		}

		/**
		 * <h1>generateOutputSynchronizationCode</h1> Generates code for
		 * 
		 * @param operation
		 * @param allVariables
		 * @return the generated code
		 */
		protected String generateOutputSynchronizationCode(Operation operation, ArrayList<OwlService> allVariables) {
			// create synchronization with other variables
			String finalizeCommands = "";
			String outputName = "";
			for (Argument arg : operation.getOutputs()) {
				for (OwlService output : allVariables) {
					if (output.getArgument().equals(arg)) {
						outputName = output.getName().getContent();
					}
				}
				if (arg.getSubtypes().isEmpty())
					finalizeCommands += outputSynchronizer + outputName + " = " + outputName + ";\n";
				else
					for (Argument sub : arg.getSubtypes()) {

						for (OwlService suboutput : allVariables) {
							if (suboutput.getArgument().equals(sub)) {
								finalizeCommands += outputSynchronizer + suboutput.getName().getContent() + " = "
										+ outputName + "." + suboutput.getName().getContent() + ";\n";
							}
						}
					}
			}
			return finalizeCommands;
		}

		/**
		 * <h1>generateCondition</h1> Generates a condition according to the
		 * value to be checked and the appropriate result.
		 * 
		 * @param check
		 * @param result
		 * @return the generated condition
		 */
		protected static String generateCondition(String check, String symbol, String value, boolean result) {
			String resultString;
			if (result) {
				resultString = "true";
			} else {
				resultString = "false";
			}
			if (symbol.equals("!=")){
				symbol="";
				return "(!" + check + symbol + value + ")" + " == " + resultString;
			}
			return "(" + check + symbol + value + ")" + " == " + resultString;
		}

		/**
		 * <h1>createFunctionCode</h1>
		 * 
		 * @param graph
		 * @param allVariables
		 * @return generates
		 * @throws Exception
		 */
		public String createFunctionCode(Graph<OwlService, Connector> graph,ArrayList<OwlService> allVariables)
				throws Exception {
			throw new Exception("CodeNode cannot create function code.Use a FunctionCodeNode instance instead.");
		}

	}

	/**
	 * <h1>getFunctionName</h1> Generates a function name for a function that
	 * implements a service call.<br/>
	 * Also removes spaces and special characters from conditions.
	 * 
	 * @param service
	 *            : the service to generate a function name for
	 * @return the generated function name
	 */
	public String getFunctionName(OwlService service) {
		if (service.getType().equals("Condition"))
			return "decision" + service.getName().toString().replaceAll("[^\\w]", "");
		return "call" + (service.getName().isEmpty() ? service.getType() : service.getName().getContent());
	}
}
