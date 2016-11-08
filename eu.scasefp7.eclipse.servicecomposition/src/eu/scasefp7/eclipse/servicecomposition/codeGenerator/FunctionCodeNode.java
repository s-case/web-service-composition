package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

import eu.scasefp7.eclipse.servicecomposition.codeGenerator.CodeGenerator.CodeNode;
import eu.scasefp7.eclipse.servicecomposition.codeInterpreter.Value;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.transformer.Similarity;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.transformer.Similarity.ComparableName;
import eu.scasefp7.eclipse.servicecomposition.views.ServiceCompositionView;

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.uci.ics.jung.graph.Graph;
import gr.iti.wsdl.wsdlToolkit.ComplexObject;
import gr.iti.wsdl.wsdlToolkit.InvocationResult;
import gr.iti.wsdl.wsdlToolkit.NativeObject;

/**
 * <h1>FunctionCodeNode</h1> This class is used for generating functions to call
 * individual services.
 * 
 * @author Manios Krasanakis
 */
public class FunctionCodeNode extends CodeNode {
	/**
	 * tab indentation
	 */
	protected static String TAB = "   ";
	/**
	 * code generator instance
	 */
	protected CodeGenerator codeGenerator;

	public static CodeNode createInstance(OwlService service, CodeGenerator codeGenerator) {
		return new FunctionCodeNode(service, codeGenerator);
	}

	public FunctionCodeNode(OwlService service, CodeGenerator codeGenerator) {
		super(service);
		this.codeGenerator = codeGenerator;
		outputSynchronizer = "this.";
	}

	@Override
	protected String generateOperationCode(Operation operation, ArrayList<OwlService> allVariables, boolean isRepeated)
			throws Exception {
		// return a dummy Python call
		if (operation.getDomain().isLocal())
			return super.generateOperationCode(operation, allVariables, false);
		// similar to interpreter implementation
		String ret = "";
		ret += "ParsedWSDLDefinition parsedWS = service.parsedWSMap.get(\"" + operation.getDomain().getURI() + "\");\n";
		ret += "InvocationResult result = Axis2WebServiceInvoker.invokeWebService(\n";
		ret += TAB + TAB + "parsedWS.getWsdlURL(),\n";
		ret += TAB + TAB + "new QName(parsedWS.getTargetNamespaceURI(), wsOperation.getOperationName()),\n";
		ret += TAB + TAB + "wsOperation.getHasInput(),\n";
		ret += TAB + TAB + "wsOperation,\n";
		ret += TAB + TAB + "parsedWS);\n";
		ret += TAB + TAB + "ArrayList<Variable> operationOutputs = new ArrayList<Variable>();\n";

		return ret;
	}

	@Override
	protected String generateInputSynchronizationCode(Operation operation, ArrayList<OwlService> allVariables,
			boolean hasBodyInput, boolean isRepeated, Graph<OwlService, Connector> graph) {
		// return a dummy Python call
		if (operation.getDomain().isLocal())
			return super.generateInputSynchronizationCode(operation, allVariables, hasBodyInput, isRepeated, graph);
		String ret = "CallWSDLService service =new CallWSDLService();\n";
		ret += "WSOperation wsOperation = service.domainOperation(\"" + operation.getName().toString() + "\", \""
				+ operation.getDomain().getURI() + "\");\n";
		for (Argument arg : operation.getInputs())
			for (OwlService var : allVariables) {
				if (var.getArgument().equals(arg)) {
					ret += assignment(var, "", "wsOperation.getHasInput().getHasNativeOrComplexObjects()", true,
							allVariables);
				}
			}
		return ret;
	}

	protected String assignment(OwlService service, String localTab, String listName, boolean input,
			ArrayList<OwlService> allVariables) {
		String ret = "";
		if (service.getArgument().getSubtypes().isEmpty()) {
			ret += localTab + "for(Object obj : " + listName + ") {\n";
			ret += localTab + TAB + "NativeObject no = (NativeObject)obj;\n";
			ret += localTab + TAB
					+ "if(no.getObjectName().getLocalPart().replaceAll(\"[0123456789]\", \"\").equalsIgnoreCase(\""
					+ service.getName().getContent().toString().replaceAll("[0123456789]", "") + "\")){\n";
			if (input)
				ret += localTab + TAB + TAB + "no.setHasValue(" + service.getName().getContent().toString()
						+ ".value);\n";
			else
				ret += localTab + TAB + TAB + service.getName().getContent().toString() + " = no.getHasValue();\n";
			ret += localTab + TAB + TAB + "break;\n";
			ret += localTab + TAB + "}\n";
			ret += localTab + "}\n";
		} else {
			ret += localTab + "for(Object obj : " + listName + ") {\n";
			// ret += localTab+TAB+"ComplexObject no = (ComplexObject)obj;\n";
			int i = 0;
			for (Argument sub : service.getArgument().getSubtypes()) {
				for (OwlService var : allVariables) {
					if (var.getArgument().equals(sub)) {
						if (input) {
							ret += "((NativeObject)((ComplexObject)obj).getHasNativeObjects().get(" + i
									+ ")).setHasValue(" + var.getName().getContent().toString() + ".value);\n";
						} else {

						}
					}
				}
				i++;
			}
			ret += localTab + "}\n";
		}
		return ret;
	}

	@Override
	protected String generateOutputSynchronizationCode(Operation operation, ArrayList<OwlService> allVariables,
			boolean isRepeated, Graph<OwlService, Connector> graph) {
		// return a dummy Python call
		// if (operation.getDomain().isLocal())
		// return super.generateInputSynchronizationCode(operation,
		// allVariables, false, false, graph);
		String ret = "";
		String varArguments = "";
		String varInstance = "";
		String response = "";
		for (Argument arg : operation.getOutputs()) {
			if (arg.getSubtypes().isEmpty()) {
				for (OwlService var : allVariables) {
					if (var.getArgument().equals(arg)) {
						ret += "operationOutputs.add(" + var.getName().getContent().toString() + ");\n";

					}
				}
			} else {
				int primitive = 0;
				for (Argument sub : arg.getSubtypes()) {
					if (sub.getSubtypes().isEmpty()) {
						primitive++;
					}
				}
				if (primitive == arg.getSubtypes().size()) {
					for (OwlService var : allVariables) {
						if (var.getArgument().equals(arg)) {
							Collection<OwlService> subList = new ArrayList<OwlService>();
							subList = (Collection<OwlService>) graph.getSuccessors(var);
							for (OwlService sub : subList) {
								if (sub.getArgument() != null) {
									ret += "operationOutputs.add(" + sub.getName().getContent().toString() + ");\n";
									if (!varArguments.isEmpty()) {
										varArguments += ", ";
									}
									String subType = sub.getArgument().getType();
									
									if (subType.equals("String")){
										varArguments += sub.getName().getContent().toString() + ".value";
									} else if (subType.equals("int")){
										subType = "Integer";
										varArguments += subType + ".parseInt" + "(" + sub.getName().getContent().toString() + ".value" + ")";
									}else {
										subType = subType.substring(0, 1).toUpperCase() + subType.substring(1);
										varArguments += subType + ".parse" + subType + "(" + sub.getName().getContent().toString() + ".value" + ")";
									}
									
								}
							}
						}
					}
					// for (Argument sub : arg.getSubtypes()) {
					// for (OwlService var : allVariables) {
					// if (var.getArgument().equals(sub)) {
					// ret += "operationOutputs.add(" +
					// var.getName().getContent().toString() + ");\n";
					// if (!varArguments.isEmpty()) {
					// varArguments += ", ";
					// }
					// varArguments += var.getName().getContent().toString() +
					// ".value";
					// }
					// }
					// }

					varInstance = arg.getName().toString().substring(0, 1).toUpperCase()
							+ arg.getName().toString().substring(1) + " " + arg.getName().getContent().toString()
							+ " = new " + arg.getName().toString().substring(0, 1).toUpperCase()
							+ arg.getName().toString().substring(1) + "(";
					response += arg.getBelongsToOperation().getName().getContent() + "_response.set"
							+ arg.getName().toString().replaceAll("[0123456789]", "") + "("
							+ arg.getName().toString().replaceAll("[0123456789]", "") + ");\n";
					if (!varArguments.isEmpty()) {
						varInstance += varArguments + ");\n";
					}

				}

			}

		}

		ret += "if (result.getResponseHasNativeOrComplexObjects().get(0) instanceof NativeObject && operationOutputs.size() > 1) {\n";
		ret += TAB
				+ "String xmlString = ((NativeObject) ((InvocationResult) result).getResponseHasNativeOrComplexObjects().get(0)).getHasValue();\n"
				+ TAB + "Document doc = CallWSDLService.loadXMLFromString(xmlString);\n" + TAB
				+ "Element element = doc.getDocumentElement();\n" + TAB + "NodeList nodes = element.getChildNodes();\n"
				+ TAB + "for (int i = 0; i < nodes.getLength(); i++) {\n";
		ret += TAB + TAB + "if (nodes.item(i).getNodeName() != null) {\n";
		ret += TAB + TAB + TAB + "for (Variable var : operationOutputs) {\n";
		ret += TAB + TAB + TAB + TAB + "if (nodes.item(i).getNodeName().equalsIgnoreCase(var.name)) {\n";
		ret += TAB + TAB + TAB + TAB + TAB + "var.value = nodes.item(i).getTextContent();\n";
		ret += TAB + TAB + TAB + TAB + "}\n" + TAB + TAB + TAB + "}\n" + TAB + TAB + "}\n" + TAB + "}\n";

		ret += varInstance;
		ret += response;

		ret += "} else {\n";
		ret += TAB + "XMLInputFactory xif = XMLInputFactory.newFactory();\n";
		ret += TAB + "String response = result.getHasResponseInString();\n";
		ret += TAB + "System.out.println(response);\n";
		ret += TAB + "StringReader xml = new StringReader(response);\n";
		ret += TAB + "XMLStreamReader xsr = xif.createXMLStreamReader(xml);\n";
		ret += TAB + "try{\n";
		ret += TAB + TAB + "xsr.nextTag();\n";
		ret += TAB + TAB + "while (xsr.hasNext()) {\n";
		ret += TAB + TAB + TAB
				+ "if (xsr.getEventType() == XMLStreamConstants.START_ELEMENT && xsr.getLocalName().equals(\""
				+ operation.getName().toString() + "Response\")) {\n";
		ret += TAB + TAB + TAB + TAB + "break;\n";
		ret += TAB + TAB + TAB + "}\n" + TAB + TAB + TAB + "xsr.nextTag();\n";
		ret += TAB + TAB + "}\n" + TAB + TAB + "JAXBContext jc = JAXBContext.newInstance("
				+ operation.getName().toString() + "Response.class);\n" + TAB + TAB
				+ "Unmarshaller unmarshaller = jc.createUnmarshaller();\n" + TAB + TAB
				+ "unmarshaller.setEventHandler(new ValidationEventHandler() {\n";
		ret += TAB + TAB + TAB + "public boolean handleEvent(ValidationEvent event) {\n";
		ret += TAB + TAB + TAB + TAB + "throw new RuntimeException(event.getMessage(), event.getLinkedException());\n";
		ret += TAB + TAB + TAB + "}\n";
		ret += TAB + TAB + "});\n" + TAB + TAB + "JAXBElement<" + operation.getName().toString()
				+ "Response> jb = unmarshaller.unmarshal(xsr," + operation.getName().toString() + "Response.class);\n"
				+ TAB + TAB + "xsr.close();\n" + TAB + TAB + "xml.close();\n" + TAB + TAB + ""
				+ operation.getName().toString() + "_response = jb.getValue();\n";
		ret += TAB + "} catch (JAXBException | XMLStreamException | IllegalStateException e) {\n";
		ret += TAB + TAB + "xsr.close();\n" + TAB + TAB + "e.printStackTrace();\n";
		ret += TAB + "}\n";
		// ret += TAB + "for (Object obj :
		// result.getResponseHasNativeOrComplexObjects()) {\n";
		// ret += TAB + TAB + "if (obj instanceof ComplexObject) {\n";
		// ret += TAB + TAB + TAB + "for (int i = 0; i < ((ComplexObject)
		// obj).getHasNativeObjects().size(); i++) {\n";
		// ret += TAB + TAB + TAB + TAB + "for (Variable var : operationOutputs)
		// {\n";
		// ret += TAB + TAB + TAB + TAB + TAB
		// + "if (((NativeObject) ((ComplexObject)
		// obj).getHasNativeObjects().get(i)).getObjectName().getLocalPart().equalsIgnoreCase(var.name))
		// {\n";
		// ret += TAB + TAB + TAB + TAB + TAB + TAB
		// + "var.value = ((NativeObject) ((ComplexObject)
		// obj).getHasNativeObjects().get(i)).getHasValue();\n";
		// ret += TAB + TAB + TAB + TAB + TAB + "}\n" + TAB + TAB + TAB + TAB +
		// "}\n" + TAB + TAB + TAB + "}\n" + TAB + TAB
		// + "}else if (obj instanceof NativeObject) {\n";
		// ret += TAB + TAB + TAB + "operationOutputs.get(0).value =
		// ((NativeObject) obj).getHasValue();\n";
		// ret += TAB + TAB + "}\n" + TAB + "}\n"
		ret += "}\n";
		// ret += varInstance;
		// ret += response;
		return ret;
	}

	@Override
	public String createFunctionCode(Graph<OwlService, Connector> graph, ArrayList<OwlService> allVariables,
			boolean hasBodyInput, boolean isRepeated, boolean hasOutput) throws Exception {
		if (service == null || service.getArgument() != null)
			return "";
		String tabIndent = getTab();
		applyTab();
		String code = tabIndent + "protected String " + codeGenerator.getFunctionName(service) + "()  throws Exception"
				+ "{\n" + getCode(allVariables, hasBodyInput, isRepeated, graph, hasOutput);
		boolean hardReturn = false;
		for (OwlService next : graph.getSuccessors(service))
			if (next.getArgument() == null && !next.getName().isEmpty()) {
				if (!graph.findEdge(service, next).toString().isEmpty()) {
					String varName = "";
					String conditionName = service.getName().getComparableForm();
					String conditionValue = "";
					int index;
					String symbol = "";
					if ((index = conditionName.indexOf("==")) != -1) {
						conditionValue = conditionName.substring(index + 2).trim();
						conditionName = conditionName.substring(0, index).trim();
						symbol = "";
					} else if ((index = conditionName.indexOf("!=")) != -1) {
						conditionValue = conditionName.substring(index + 2).trim();
						conditionName = conditionName.substring(0, index).trim();
						symbol = "!=";
					} else if ((index = conditionName.indexOf("=")) != -1) {
						conditionValue = conditionName.substring(index + 1).trim();
						conditionName = conditionName.substring(0, index).trim();
						symbol = "";
						if ((index = conditionValue.indexOf("<")) != -1) {
							conditionValue = conditionValue.substring(index + 1);
							symbol = "=<";
						}
						if ((index = conditionValue.indexOf(">")) != -1) {
							conditionValue = conditionValue.substring(index + 1);
							symbol = "=<";
						}
					} else if ((index = conditionName.indexOf("<")) != -1) {
						conditionValue = conditionName.substring(index + 1).trim();
						conditionName = conditionName.substring(0, index).trim();
						symbol = "<";
						if ((index = conditionValue.indexOf("=")) != -1) {
							conditionValue = conditionValue.substring(index + 1);
							symbol = "<=";
						}
					} else if ((index = conditionName.indexOf(">")) != -1) {
						conditionValue = conditionName.substring(index + 1).trim();
						conditionName = conditionName.substring(0, index).trim();
						symbol = ">";
						if ((index = conditionValue.indexOf("=")) != -1) {
							conditionValue = conditionValue.substring(index + 1);
							symbol = ">=";
						}
					}
					String type = "string";
					if (!conditionValue.replaceAll("[^\\.0123456789]", "").isEmpty()) {
						conditionValue = conditionValue.replaceAll("[^\\.0123456789]", "");
						type = "double";
					}

					if (conditionValue.equalsIgnoreCase("")) {
						conditionValue = graph.findEdge(service, next).toString();

					}
					ArrayList<Argument> previousServiceOutVariables = new ArrayList<Argument>();

					for (OwlService previousOperation : graph.getPredecessors(service)) {
						if (previousOperation.getOperation() != null) {
							Operation op = previousOperation.getOperation();
							double bestMatch = 0;

							for (Argument output : previousOperation.getOperation().getOutputs()) {
								if (!output.isArray()) {
									if (output.getSubtypes().isEmpty()) {
										previousServiceOutVariables.add(output);
									}
									for (Argument sub : output.getSubtypes()) {
										if (!sub.isArray()) {
											getSubtypes(previousServiceOutVariables, sub, true);
										}

									}
								}
							}

							for (Argument output : previousServiceOutVariables) {
								double match = Similarity.similarity(new ComparableName(conditionName),
										output.getName());
								if (match >= bestMatch) {
									for (OwlService outputService : allVariables) {
										if (outputService.getArgument().equals(output)) {
											String ret = ".get" + outputService.getName().getContent()
													.replaceAll("[0123456789]", "") + "()";
											ret = roadToSub(outputService, graph, ret);
											bestMatch = match;
											if (type == "double") {
												varName = "Double.parseDouble(" + op.getName() + "_response" + ret
														+ ")";
											} else if (output.getType().equals("boolean")) {
												varName = "Boolean.toString(" + op.getName() + "_response" + ret + ")";
											} else {
												varName = op.getName() + "_response" + ret;

											}
										}
									}
								}
							}
							if (type == "string") {
								conditionValue = ".equalsIgnoreCase(\"" + conditionValue + "\")";
							}
						}
					}
					String[] TRUE_PREDICATES = { "true", "yes", "success", "OK" };
					String[] FALSE_PREDICATES = { "false", "no", "invalid" };
					boolean result = false;
					boolean hasPredicate = false;
					for (String predicate : FALSE_PREDICATES)
						if (Similarity.comparePredicates(graph.findEdge(service, next).toString(), predicate)) {
							hasPredicate = true;
							result = false;
							break;
						}
					for (String predicate : TRUE_PREDICATES)
						if (Similarity.comparePredicates(graph.findEdge(service, next).toString(), predicate)) {
							hasPredicate = true;
							result = true;
							break;
						}
					code += tabIndent + TAB + "if("
							+ generateCondition(varName, symbol, conditionValue, result, hasPredicate) + ")\n";
					code += tabIndent + TAB + TAB + "return \"" + codeGenerator.getFunctionName(next) + "\";\n";
				} else {
					code += tabIndent + TAB + "return \"" + codeGenerator.getFunctionName(next) + "\";\n";
					hardReturn = true;
				}
			}
		if (!hardReturn)
			code += tabIndent + TAB + "return null;\n";
		code += tabIndent + "}\n";
		return code;
	}

	public static String generateImports(boolean restServiceExists, boolean hasDeserializer, boolean wsdlServiceExists,
			boolean mailgun) {

		String imports = "";
		if (restServiceExists) {
			imports += "import com.google.gson.Gson;\n" + "import com.google.gson.GsonBuilder;\n";
			if (hasDeserializer) {
				imports += "import com.google.gson.JsonDeserializationContext;\n"
						+ "import com.google.gson.JsonDeserializer;\n" + "import com.google.gson.JsonElement;\n"
						+ "import com.google.gson.JsonParseException;\n"
						+ "import com.google.gson.annotations.SerializedName;\n"
						+ "import com.google.gson.reflect.TypeToken;\n" + "import java.lang.reflect.Type;\n";
			}
			if (mailgun) {
				imports += "import java.net.URLEncoder;\nimport org.apache.http.NameValuePair;\nimport org.apache.http.message.BasicNameValuePair;\nimport java.util.List;\n";
			}
		}
		if (wsdlServiceExists) {
			imports +=
			// "import gr.iti.wsdl.wsdlToolkit.ITIWSDLParser;\n"
			"import gr.iti.wsdl.wsdlToolkit.InvocationResult;\n" + "import gr.iti.wsdl.wsdlToolkit.NativeObject;\n"
					+ "import gr.iti.wsdl.wsdlToolkit.ComplexObject;\n"
					+ "import gr.iti.wsdl.wsdlToolkit.ParsedWSDLDefinition;\n"
					+ "import gr.iti.wsdl.wsdlToolkit.WSOperation;\n"
					+ "import gr.iti.wsdl.wsdlToolkit.invocation.Axis2WebServiceInvoker;\n"
					+ "import javax.xml.namespace.QName;\n" + "import javax.xml.stream.XMLInputFactory;\n"
					+ "import javax.xml.stream.XMLStreamConstants;\n" + "import javax.xml.stream.XMLStreamException;\n"
					+ "import javax.xml.stream.XMLStreamReader;\n" + "import org.w3c.dom.Document;\n"
					+ "import org.w3c.dom.Element;\n" + "import org.w3c.dom.NodeList;\n"
					+ "import java.io.StringReader;\n" + "import javax.xml.bind.JAXBContext;\n"
					+ "import javax.xml.bind.JAXBElement;\n" + "import javax.xml.bind.JAXBException;\n"
					+ "import javax.xml.bind.Unmarshaller;\n" + "import javax.xml.bind.ValidationEvent;\n"
					+ "import javax.xml.bind.ValidationEventHandler;\n"
					+ "import javax.xml.bind.annotation.XmlAccessType;\n"
					+ "import javax.xml.bind.annotation.XmlAccessorType;\n";
		}
		return imports + "import java.util.ArrayList;\n" + "import javax.xml.bind.annotation.XmlElement;\n"
				+ "import javax.xml.bind.annotation.XmlRootElement;\n";
	}

	private String roadToSub(OwlService sub, Graph<OwlService, Connector> graph, String ret) {
		for (OwlService service : graph.getPredecessors(sub)) {
			if (service.getArgument() != null) {
				ret = ".get" + service.getName().getContent().replaceAll("[0123456789]", "") + "()" + ret;
				ret = roadToSub(service, graph, ret);
			} else {
				return ret;
			}
		}
		return ret;
	}

	private ArrayList<Argument> getSubtypes(ArrayList<Argument> suboutputVariables, Argument sub, boolean noObjects) {
		if (!suboutputVariables.contains(sub)) {
			if (noObjects) {
				if (sub.getSubtypes().isEmpty()) {
					suboutputVariables.add(sub);
				}
			} else {
				suboutputVariables.add(sub);
			}
		}
		if (!sub.getSubtypes().isEmpty()) {
			for (Argument subsub : sub.getSubtypes()) {
				if (noObjects) {
					if (!sub.isArray()) {
						getSubtypes(suboutputVariables, subsub, true);
					}
				} else {
					getSubtypes(suboutputVariables, subsub, true);
				}
			}
		}
		return suboutputVariables;
	}
}
