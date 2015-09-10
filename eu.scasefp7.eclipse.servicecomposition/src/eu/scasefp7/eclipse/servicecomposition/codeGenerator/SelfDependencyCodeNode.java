package eu.scasefp7.eclipse.servicecomposition.codeGenerator;
//package eu.scasefp7.eclipse.servicecomposition.codeGenerator;
//
//import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
//import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
//import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
//
//import java.util.ArrayList;
//
//import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
//import eu.scasefp7.eclipse.servicecomposition.codeGenerator.CodeGenerator.CodeNode;
//import edu.uci.ics.jung.graph.Graph;
//
///**
// * <h1>SelfDependencyCodeNode</h1>
// * This class is used to generate code for calling services using this project's classes.
// * To do so, this project must be exported into a .jar library and included in the generated
// * code's project setup.<br/>
// * Using this class presents the distinct advantage of being independent to the interface
// * between this project and WSDL caller.
// * @author Manios Krasanakis
// */
//public class SelfDependencyCodeNode extends FunctionCodeNode{
//
//	public static CodeNode createInstance(OwlService service, CodeGenerator codeGenerator){
//		return new FunctionCodeNode(service, codeGenerator);
//	}
//	
//	public SelfDependencyCodeNode(OwlService service, CodeGenerator codeGenerator) {
//		super(service, codeGenerator);
//	}
//	
//	@Override
//	protected String generateOperationCode(Operation operation) throws Exception{
//		String ret = "";
//		if(operation.getDomain().isLocal())
//			ret += "operationCaller.PythonCaller.callOperation(operation);";
//		else
//			ret += "operationCaller.WSDLCaller.callOperation(operation);";
//		return ret;
//	}
//	@Override
//	protected String generateInputSynchronizationCode(Operation operation, ArrayList<Argument> allVariables){
//		String ret = "";
//		return ret;
//	}
//	
//	@Override
//	protected String generateOutputSynchronizationCode(Operation operation, ArrayList<Argument> allVariables){
//		String ret = "";
//		return ret;
//	}
//	@Override
//	public String createFunctionCode(Graph<OwlService, Connector> graph, ArrayList<Argument> allVariables) throws Exception {
//		String ret = "";
//		return ret;
//	}
//	public static String generateImports(){
//		return 	"import importer.Importer.Operation;\n"+
//				"import importer.Importer.Argument;\n"+
//				"import codeInterpreter.Value;\n";
//	}
//}
