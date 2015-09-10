package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;

import java.util.ArrayList;
import java.util.HashMap;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

//public class LinearCodeGenerator extends CodeGenerator{
//	public class LinearCodeNode extends CodeNode{
//		public LinearCodeNode(OwlService service) {
//			super(service);
//		}
//		public String createFollowingCode(HashMap<OwlService, LinearCodeNode> codeMap, ArrayList<OwlService> visited, Graph<OwlService, Connector> graph, ArrayList<Argument> allVariables) throws Exception {
//			if(service==null || service.getType().equals("Variable"))
//				return "";
//			applyTab();
//			visited.add(service);
//			String code = getCode(allVariables);
//			boolean makeIfClause = graph.getSuccessorCount(service)>1;
//
//			String tabIndent = getTab();
//			for(OwlService next : graph.getSuccessors(service))
//				if(!visited.contains(next)){
//					if(makeIfClause && !graph.findEdge(service, next).toString().isEmpty()){
//						codeMap.get(next).applyTab();
//						String toAdd = codeMap.get(next).createFollowingCode(codeMap, visited, graph, allVariables);
//						if(!toAdd.isEmpty()){
//							code += tabIndent+"if("+generateCondition(service.getName().toString(), graph.findEdge(service, next).toString())+"){\n";
//							code += toAdd;
//							code += tabIndent+"}\n";
//						}
//					}
//					else
//						code += codeMap.get(next).createFollowingCode(codeMap, visited, graph, allVariables);
//				}
//				else{
//					if(makeIfClause && !graph.findEdge(service, next).toString().isEmpty()){
//						//throw new Exception("Service "+service.getName()+" loops back to "+next.getName());
//					}
//				}
//			return code;
//		}
//	}
//	
//	
//	public String generateCode(Graph<OwlService, Connector> graph, String functionName, boolean addConnectionsToGraph) throws Exception{
//		//detect all variables
//		ArrayList<Argument> allVariables = new ArrayList<Argument>();
//		for(OwlService service : graph.getVertices()){
//			Operation op = service.getOperation();
//			if(op!=null){
//				for(Argument arg : op.getInputs())
//					if(!allVariables.contains(arg))
//						allVariables.add(arg);
//				for(Argument arg : op.getOutputs())
//					if(!allVariables.contains(arg))
//						allVariables.add(arg);
//			}
//		}
//		for(OwlService service : graph.getVertices()){
//			Argument arg = service.getArgument();
//			if(arg!=null && !allVariables.contains(arg))
//				allVariables.add(arg);
//		}
//		//assert correct subtypes (for debugging purposes)
//		//for(Argument arg : allVariables)
//		//	arg.assertCorrectSubtypes();
//		//initialize variable lists
//		ArrayList<Argument> inputVariables  = new ArrayList<Argument>(allVariables);
//		ArrayList<Argument> outputVariables = new ArrayList<Argument>(allVariables);
//		ArrayList<Argument> requiredInputVariables  = new ArrayList<Argument>();
//		ArrayList<Argument> directAssignVariables   = new ArrayList<Argument>();
//		//detect all operations
//		ArrayList<OwlService> remainingOperations = new ArrayList<OwlService>();
//		for(OwlService service : graph.getVertices()){
//			if(service.getOperation()!=null)
//				remainingOperations.add(service);
//		}
//		//detect starting service
//		OwlService startingService = null;
//		for(OwlService service : graph.getVertices()){
//			if(service.getType().trim().equals("StartNode")){
//				startingService = service;
//				break;
//			}
//		}
//		if(startingService==null)
//			throw new Exception("StartNode was not detected on graph vertices");
//		//generate code for each service
//		HashMap<OwlService, LinearCodeNode> codeMap = new HashMap<OwlService, LinearCodeNode>();
//		for(OwlService service : graph.getVertices()){
//			if(!service.getType().equals("Argument"))
//				codeMap.put(service, new LinearCodeNode(service));
//			if(service.getOperation()!=null){
//				for(Argument arg : service.getOperation().getInputs()){
//					if(!requiredInputVariables.contains(arg))
//						requiredInputVariables.add(arg);
//					outputVariables.remove(arg);
//				}
//				for(Argument arg : service.getOperation().getOutputs()){
//					if(!requiredInputVariables.contains(arg))
//						inputVariables.remove(arg);
//					for(Argument sub : arg.getSubtypes())
//						if(!requiredInputVariables.contains(sub))
//							inputVariables.remove(sub);
//					if(!requiredInputVariables.contains(arg))
//						directAssignVariables.add(arg);
//				}
//			}
//		}
//		//create body code by making starting code expand itself to all its next nodes
//		String body = codeMap.get(startingService).createFollowingCode(codeMap, new ArrayList<OwlService>(), graph, allVariables);
//		//
//		//create variable declarations
//		allVariables.removeAll(inputVariables);
//		allVariables.removeAll(directAssignVariables);
//		String declaredVariables = "";
//		for(Argument arg : allVariables){
//			if(arg.isNative())
//				declaredVariables += TAB+arg.toString()+";\n";
//			else
//				declaredVariables += TAB+arg.toString()+" = new "+arg.getType()+"();\n";
//		}
//		if(!declaredVariables.isEmpty())
//			declaredVariables = TAB+"//declare variables\n"+declaredVariables;
//		//create input declarations
//		String declaredInputs = "";
//		for(Argument arg : inputVariables){
//			if(!declaredInputs.isEmpty())
//				declaredInputs += ", ";
//			declaredInputs += arg.toString();
//		}
//		//create result class
//		String resultClassName = "void";
//		String resultClassDeclaration = "";
//		String resultObjectDeclaration = "";
//		if(outputVariables.size()==1){
//			resultClassName = outputVariables.get(0).getType();
//			resultObjectDeclaration = TAB+"return "+outputVariables.get(0).getName()+";";
//		}
//		else if(!outputVariables.isEmpty()){
//			String resultObjectName = functionName.substring(0, 1).toLowerCase()+functionName.substring(1)+"Result";
//			resultClassName = functionName.substring(0, 1).toUpperCase()+functionName.substring(1)+"Result";
//			
//			resultClassDeclaration = "class "+resultClassName+"{\n";
//			for(Argument arg : outputVariables)
//				resultClassDeclaration += TAB+arg.toString()+";\n";
//			resultClassDeclaration += "}\n\n";
//
//			resultObjectDeclaration = TAB+"//create class instance to be returned\n";
//			resultObjectDeclaration += TAB+resultClassName+" "+resultObjectName+" = new "+resultClassName+"();\n";
//			for(Argument arg : outputVariables)
//				resultObjectDeclaration += TAB+resultObjectName+"."+arg.getName()+" = "+arg.getName()+";\n";
//			resultObjectDeclaration += TAB+"return "+resultObjectName+";\n";
//		}
//		//generate function head
//		declaredInputs = resultClassDeclaration+"public "+resultClassName+" "+functionName+"("+declaredInputs+"){\n";
//		//add connections with inputs and outputs to graph
//		if(addConnectionsToGraph){
//			OwlService start = null;
//			OwlService end = null;
//			for(OwlService service : graph.getVertices()){
//				if(service.getType().trim().equals("StartNode"))
//					start = service;
//				if(service.getType().trim().equals("EndNode"))
//					end = service;
//			}
//			for(OwlService service : graph.getVertices()){
//				Argument arg = service.getArgument();
//				if(arg!=null && start!=null && inputVariables.contains(arg))
//					graph.addEdge(new Connector(start, service, ""), start, service, EdgeType.DIRECTED);
//				if(arg!=null && end!=null && outputVariables.contains(arg))
//					graph.addEdge(new Connector(service, end, ""), service, end, EdgeType.DIRECTED);
//			}
//		}
//		//return code
//		return declaredInputs+declaredVariables+body+resultObjectDeclaration+"}";
//	}
//}
