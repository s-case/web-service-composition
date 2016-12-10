package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

import eu.scasefp7.eclipse.servicecomposition.codeGenerator.CodeGenerator.CodeNode;
import eu.scasefp7.eclipse.servicecomposition.codeInterpreter.Value;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.toolbar.RunWorkflow;
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
//		ret += "ParsedWSDLDefinition parsedWS = service.parsedWSMap.get(\"" + operation.getDomain().getURI() + "\");\n";
//		ret += "InvocationResult result = Axis2WebServiceInvoker.invokeWebService(\n";
//		ret += TAB + TAB + "parsedWS.getWsdlURL(),\n";
//		ret += TAB + TAB + "new QName(parsedWS.getTargetNamespaceURI(), wsOperation.getOperationName()),\n";
//		ret += TAB + TAB + "wsOperation.getHasInput(),\n";
//		ret += TAB + TAB + "wsOperation,\n";
//		ret += TAB + TAB + "parsedWS);\n";
//		ret += TAB + TAB + "ArrayList<Variable> operationOutputs = new ArrayList<Variable>();\n";

		return ret;
	}

	@Override
	protected String generateInputSynchronizationCode(Operation operation, ArrayList<OwlService> allVariables,
			boolean hasBodyInput, boolean isRepeated, Graph<OwlService, Connector> graph) {
		String ret = "String response = \"\";\n";
		ret += "try {\n";
		ret += TAB + "// Create SOAP Connection\n" + TAB
				+ "SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();\n" + TAB
				+ "SOAPConnection soapConnection = soapConnectionFactory.createConnection();\n" + TAB
				+ "// Create SOAP message\n" + "MessageFactory messageFactory = MessageFactory.newInstance();\n" + TAB
				+ "SOAPMessage soapMessage = messageFactory.createMessage();\n" + TAB
				+ "SOAPPart soapPart = soapMessage.getSOAPPart();\n";
		// server name
		int index = operation.getDomain().getURI().indexOf(".com/");
		String serverName = operation.getDomain().getURI().substring(0, index - 1);
		ret += TAB + "String serverURI = " + "\"" + serverName + "\";\n";

		ret += TAB + "// SOAP Envelope\n" + "SOAPEnvelope envelope = soapPart.getEnvelope();\n" + TAB
				+ "envelope.addNamespaceDeclaration(\"example\", serverURI);\n\n" + "// SOAP Body\n";

		ret += TAB + "SOAPBody soapBody = envelope.getBody();\n"
				+ "SOAPElement soapBodyElem = soapBody.addChildElement(\"" + operation.getName().toString()
				+ "\", \"example\");\n";
		// inputs assignment
		for (int i = 0; i < operation.getInputs().size(); i++) {
			for (OwlService var : allVariables) {
				if (var.getArgument().equals(operation.getInputs().get(i))) {
					if (var.getArgument().getSubtypes().isEmpty()) {

					} else {
						// complex input
						ret += TAB + "SOAPElement soapBodyElem" + i + " = soapBodyElem.addChildElement(\""
								+ var.getArgument().getName().getContent() + "\", \"example\");\n";
						for (int j = 0; j < var.getArgument().getSubtypes().size(); j++) {
							for (OwlService sub : allVariables) {
								if (sub.getArgument().equals(var.getArgument().getSubtypes().get(j))) {
									if (sub.getArgument().getSubtypes().isEmpty()) {
										ret += TAB + sub.getName().getJavaValidContent() + ".value = "
												+ operation.getName().toString() + "_request.get"
												+ var.getArgument().getName().getContent() + "().get"
												+ sub.getArgument().getName().getContent() + "();\n";
										ret += TAB + "SOAPElement soapBodySubElem" + j + "= soapBodyElem" + i
												+ ".addChildElement(\"" + sub.getArgument().getName().getContent()
												+ "\", \"example\");\n";
										ret += TAB + "soapBodySubElem" + j + ".addTextNode("
												+ sub.getName().getJavaValidContent() + ".value);\n";
									}
								}
							}
						}
					}
				}
			}
		}

		ret += TAB + "MimeHeaders headers = soapMessage.getMimeHeaders();\n";
		ret += TAB + "headers.addHeader(\"SOAPAction\", serverURI + \"" + operation.getName().toString() + "\");\n\n";

		ret += TAB + "soapMessage.saveChanges();\n";

		ret += TAB + "//Print the request message\n" + TAB + "System.out.print(\"Request SOAP Message = \");\n" + TAB
				+ "soapMessage.writeTo(System.out);\n\n";

		ret += TAB + "// Send SOAP Message to SOAP Server\n" + TAB
				+ "String url = \"http://v1.fraudlabs.com/ip2locationwebservice.asmx\";\n" + TAB
				+ "SOAPMessage soapResponse = soapConnection.call(soapMessage, url);\n\n" + TAB
				+ "// Process the SOAP Response\n" + TAB
				+ "response = CallWSDLService.printSOAPResponse(soapResponse);\n\n" + TAB + "soapConnection.close();\n";
		ret += "} catch (Exception e) {\n" + TAB
				+ "System.err.println(\"Error occurred while sending SOAP Request to Server\");\n" + TAB
				+ "e.printStackTrace();\n" + "}\n";

		// // return a dummy Python call
		// if (operation.getDomain().isLocal())
		// return super.generateInputSynchronizationCode(operation,
		// allVariables, hasBodyInput, isRepeated, graph);
		// String ret = "CallWSDLService service =new CallWSDLService();\n";
		// ret += "WSOperation wsOperation = service.domainOperation(\"" +
		// operation.getName().toString() + "\", \""
		// + operation.getDomain().getURI() + "\");\n";
		// for (Argument arg : operation.getInputs())
		// for (OwlService var : allVariables) {
		// if (var.getArgument().equals(arg)) {
		// ret += assignment(var, "",
		// "wsOperation.getHasInput().getHasNativeOrComplexObjects()", true,
		// allVariables);
		// }
		// }
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
		// String varArguments = "";
		// String varInstance = "";
		// String response = "";
		// for (Argument arg : operation.getOutputs()) {
		// if (arg.getSubtypes().isEmpty()) {
		// for (OwlService var : allVariables) {
		// if (var.getArgument().equals(arg)) {
		// ret += "operationOutputs.add(" +
		// var.getName().getContent().toString() + ");\n";
		//
		// }
		// }
		// } else {
		// int primitive = 0;
		// for (Argument sub : arg.getSubtypes()) {
		// if (sub.getSubtypes().isEmpty()) {
		// primitive++;
		// }
		// }
		// if (primitive == arg.getSubtypes().size()) {
		// for (OwlService var : allVariables) {
		// if (var.getArgument().equals(arg)) {
		// Collection<OwlService> subList = new ArrayList<OwlService>();
		// subList = (Collection<OwlService>) graph.getSuccessors(var);
		// for (OwlService sub : subList) {
		// if (sub.getArgument() != null) {
		// ret += "operationOutputs.add(" +
		// sub.getName().getContent().toString() + ");\n";
		// if (!varArguments.isEmpty()) {
		// varArguments += ", ";
		// }
		// String subType = sub.getArgument().getType();
		//
		// if (subType.equals("String")) {
		// varArguments += sub.getName().getContent().toString() + ".value";
		// } else if (subType.equals("int")) {
		// subType = "Integer";
		// varArguments += subType + ".parseInt" + "("
		// + sub.getName().getContent().toString() + ".value" + ")";
		// } else {
		// subType = subType.substring(0, 1).toUpperCase() +
		// subType.substring(1);
		// varArguments += subType + ".parse" + subType + "("
		// + sub.getName().getContent().toString() + ".value" + ")";
		// }
		//
		// }
		// }
		// }
		// }
		// // for (Argument sub : arg.getSubtypes()) {
		// // for (OwlService var : allVariables) {
		// // if (var.getArgument().equals(sub)) {
		// // ret += "operationOutputs.add(" +
		// // var.getName().getContent().toString() + ");\n";
		// // if (!varArguments.isEmpty()) {
		// // varArguments += ", ";
		// // }
		// // varArguments += var.getName().getContent().toString() +
		// // ".value";
		// // }
		// // }
		// // }
		//
		// varInstance = arg.getName().toString().substring(0, 1).toUpperCase()
		// + arg.getName().toString().substring(1) + " " +
		// arg.getName().getJavaValidContent()
		// + " = new " + arg.getName().toString().substring(0, 1).toUpperCase()
		// + arg.getName().toString().substring(1) + "(";
		// response += arg.getBelongsToOperation().getName().getContent() +
		// "_response.set"
		// + arg.getName().toString().replaceAll("[0123456789]", "") + "("
		// + arg.getName().getJavaValidContent() + ");\n";
		// if (!varArguments.isEmpty()) {
		// varInstance += varArguments + ");\n";
		// }
		//
		// }
		//
		// }
		//
		// }
		//
		// ret += "if (result.getResponseHasNativeOrComplexObjects().get(0)
		// instanceof NativeObject && operationOutputs.size() > 1) {\n";
		// ret += TAB
		// + "String xmlString = ((NativeObject) ((InvocationResult)
		// result).getResponseHasNativeOrComplexObjects().get(0)).getHasValue();\n"
		// + TAB + "Document doc =
		// CallWSDLService.loadXMLFromString(xmlString);\n" + TAB
		// + "Element element = doc.getDocumentElement();\n" + TAB + "NodeList
		// nodes = element.getChildNodes();\n"
		// + TAB + "for (int i = 0; i < nodes.getLength(); i++) {\n";
		// ret += TAB + TAB + "if (nodes.item(i).getNodeName() != null) {\n";
		// ret += TAB + TAB + TAB + "for (Variable var : operationOutputs) {\n";
		// ret += TAB + TAB + TAB + TAB + "if
		// (nodes.item(i).getNodeName().equalsIgnoreCase(var.name)) {\n";
		// ret += TAB + TAB + TAB + TAB + TAB + "var.value =
		// nodes.item(i).getTextContent();\n";
		// ret += TAB + TAB + TAB + TAB + "}\n" + TAB + TAB + TAB + "}\n" + TAB
		// + TAB + "}\n" + TAB + "}\n";
		//
		// ret += varInstance;
		// ret += response;
		//
		// ret += "} else {\n";
		//

		ret += TAB + "XMLInputFactory xif = XMLInputFactory.newFactory();\n";

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

		return ret;
	}

	@Override
	public String createFunctionCode(Graph<OwlService, Connector> graph, ArrayList<OwlService> allVariables,
			boolean hasBodyInput, boolean isRepeated, boolean hasOutput, ArrayList<Operation> repeatedOperations)
			throws Exception {
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

					Operation op = null;
					boolean isOpRepeated = false;
					boolean isMemberOfArray = false;
					String array = "";
					for (OwlService previousOperation : graph.getPredecessors(service)) {
						if (previousOperation.getOperation() != null) {
							op = previousOperation.getOperation();

							if (repeatedOperations.contains(op)) {
								isOpRepeated = true;
							}
							double bestMatch = 0;

							for (Argument output : previousOperation.getOperation().getOutputs()) {

								if (output.getSubtypes().isEmpty()) {
									previousServiceOutVariables.add(output);
								}
								for (Argument sub : output.getSubtypes()) {
									// if (!sub.isArray()) {
									getSubtypes(previousServiceOutVariables, sub, true);
									// }

								}

							}

							Argument bestValue = null;
							for (Argument output : previousServiceOutVariables) {
								double match = Similarity.similarity(new ComparableName(conditionName),
										output.getName());
								if (match >= bestMatch) {
									bestMatch = match;
									bestValue = output;
								}
							}
							for (OwlService outputService : allVariables) {
								// if
								// (outputService.getArgument().equals(bestValue))
								// {
								if (outputService.equals(bestValue.getOwlService())) {
									String ret = ".get"
											+ outputService.getName().getContent().replaceAll("[0123456789]", "")
											+ "()";

									for (Object parent : bestValue.getParent()) {
										if (parent instanceof Argument)
											if (((Argument) parent).isArray()) {
												isMemberOfArray = true;
											}
									}

									ArrayList<String> classNames = new ArrayList<String>();
									if (isMemberOfArray) {
										OwlService initialArray = RunWorkflow.getInitialArray(outputService, graph,
												false);
										ret = NonLinearCodeGenerator.roadToSub(initialArray, outputService, graph, ret,
												"i", classNames, true);
										int ind = ret.indexOf(".get(i)");
										array = ret.substring(0, ind);
									} else {
										ret = roadToSub(outputService, graph, ret);
									}

									if (type == "double") {
										varName = "Double.parseDouble(";
										if (isOpRepeated) {
											varName += "r";
										} else {
											varName += op.getName() + "_response";
										}
										varName += ret + ".replaceAll(\"[^\\\\.0123456789]\", \"\"))";
									} else if (bestValue.getType().equals("boolean")) {
										// TODO : if is Repeated
										varName = "Boolean.toString(" + op.getName() + "_response" + ret + ")";
									} else {
										// TODO : if is Repeated
										varName = op.getName() + "_response" + ret;

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
					if (isOpRepeated) {
						code += tabIndent + TAB + "for (" + op.getName().toString() + "Response r : "
								+ op.getName().toString() + "_response_arraylist) {\n";
						if (isMemberOfArray) {
							code += tabIndent + TAB + "for (int i = 0; i < r" + array + ".size(); i++) {\n";
						}
					}
					if (isMemberOfArray && !isOpRepeated) {
						code += tabIndent + TAB + "for (int i = 0; i < " + op.getName().toString() + "_response" + array
								+ ".size(); i++) {\n";
					}
					code += tabIndent + TAB + TAB + "if("
							+ generateCondition(varName, symbol, conditionValue, result, hasPredicate) + ")\n";
					code += tabIndent + TAB + TAB + TAB + "return \"" + codeGenerator.getFunctionName(next) + "\";\n";
					if (isOpRepeated) {
						code += tabIndent + TAB + "}\n";
					}
					if (isMemberOfArray) {
						code += tabIndent + TAB + "}\n";
					}
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
					//"import gr.iti.wsdl.wsdlToolkit.InvocationResult;\n"
					//		+ "import gr.iti.wsdl.wsdlToolkit.NativeObject;\n"
					//		+ "import gr.iti.wsdl.wsdlToolkit.ComplexObject;\n"
					//		+ "import gr.iti.wsdl.wsdlToolkit.ParsedWSDLDefinition;\n"
					//		+ "import gr.iti.wsdl.wsdlToolkit.WSOperation;\n"
					//		+ "import gr.iti.wsdl.wsdlToolkit.invocation.Axis2WebServiceInvoker;\n"
					//		+ "import javax.xml.namespace.QName;\n" + 
					"import javax.xml.stream.XMLInputFactory;\n"
							+ "import javax.xml.stream.XMLStreamConstants;\n"
							+ "import javax.xml.stream.XMLStreamException;\n"
							+ "import javax.xml.stream.XMLStreamReader;\n" 
							//+ "import org.w3c.dom.Document;\n"
							//+ "import org.w3c.dom.Element;\n" + "import org.w3c.dom.NodeList;\n"
							+ "import java.io.StringReader;\n" + "import javax.xml.bind.JAXBContext;\n"
							+ "import javax.xml.bind.JAXBElement;\n" + "import javax.xml.bind.JAXBException;\n"
							+ "import javax.xml.bind.Unmarshaller;\n" + "import javax.xml.bind.ValidationEvent;\n"
							+ "import javax.xml.bind.ValidationEventHandler;\n"
							+ "import javax.xml.bind.annotation.XmlAccessType;\n"
							+ "import javax.xml.bind.annotation.XmlAccessorType;\n"
							+ "import javax.xml.soap.MessageFactory;\n" + "import javax.xml.soap.MimeHeaders;\n"
							+ "import javax.xml.soap.SOAPBody;\n" + "import javax.xml.soap.SOAPConnection;\n"
							+ "import javax.xml.soap.SOAPConnectionFactory;\n" + "import javax.xml.soap.SOAPElement;\n"
							+ "import javax.xml.soap.SOAPEnvelope;\n" + "import javax.xml.soap.SOAPMessage;\n"
							+ "import javax.xml.soap.SOAPPart;\n";
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

	static ArrayList<Argument> getSubtypes(ArrayList<Argument> suboutputVariables, Argument sub, boolean noObjects) {
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
					// if (!sub.isArray()) {
					getSubtypes(suboutputVariables, subsub, true);
					// }
				} else {
					getSubtypes(suboutputVariables, subsub, true);
				}
			}
		}
		return suboutputVariables;
	}
}
