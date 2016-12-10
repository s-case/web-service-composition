package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

public class CallWSDLServiceCode {

	public static String generateCode(String packageName) {
		String TAB = "    ";
		// package declaration and imports
		String code = "package " + packageName + ";\n\n";
//		code += "import javax.xml.parsers.DocumentBuilder;\n" + "import javax.xml.parsers.DocumentBuilderFactory;\n"
//				+ "import org.xml.sax.InputSource;\n" + "import java.io.StringReader;\n" + "import java.util.HashMap;\n"
//				+ "import org.w3c.dom.Document;\n" + "import gr.iti.wsdl.wsdlToolkit.ParsedWSDLDefinition;\n"
//				+ "import gr.iti.wsdl.wsdlToolkit.WSOperation;\n" + "import gr.iti.wsdl.wsdlToolkit.ITIWSDLParser;\n\n";
		
		code += "import javax.xml.soap.SOAPMessage;\nimport javax.xml.transform.Source;\nimport javax.xml.transform.Transformer;\nimport javax.xml.transform.TransformerFactory;\nimport javax.xml.transform.stream.StreamResult;\nimport java.io.StringWriter;\n";

		code += "public class CallWSDLService{\n";

		// String parsedWSDLMap = "protected HashMap<String,
		// ParsedWSDLDefinition> parsedWSMap = new HashMap<String,
		// ParsedWSDLDefinition> ();\n\n";
		// parsedWSDLMap += TAB + "WSOperation domainOperation(String
		// operationName, String operationURI){\n";
		// parsedWSDLMap += TAB + TAB + "//get domain\n";
		// parsedWSDLMap += TAB + TAB + "ParsedWSDLDefinition parsedWS =
		// parsedWSMap.get(operationURI);\n";
		// parsedWSDLMap += TAB + TAB + "if(parsedWS==null){\n";
		// parsedWSDLMap += TAB + TAB + TAB
		// + "parsedWS = ITIWSDLParser.parseWSDLwithAxis(operationURI, true,
		// true);\n";
		// parsedWSDLMap += TAB + TAB + TAB + "parsedWSMap.put(operationURI,
		// parsedWS);\n";
		// parsedWSDLMap += TAB + TAB + "}\n";
		// parsedWSDLMap += TAB + TAB + "//find WSOperation\n";
		// parsedWSDLMap += TAB + TAB + "WSOperation wsOperation = null;\n";
		// parsedWSDLMap += TAB + TAB + "for(Object op :
		// parsedWS.getWsdlOperations())\n";
		// parsedWSDLMap += TAB + TAB + TAB
		// + "if(((WSOperation)
		// op).getOperationName().equalsIgnoreCase(operationName)){\n";
		// parsedWSDLMap += TAB + TAB + TAB + "wsOperation = (WSOperation)
		// op;\n";
		// parsedWSDLMap += TAB + TAB + TAB + "break;\n";
		// parsedWSDLMap += TAB + TAB + "}\n";
		// parsedWSDLMap += TAB + TAB + "return wsOperation;\n";
		// parsedWSDLMap += TAB + "}\n";
		//
		// CallRestfulServiceCode.applyTab(1,parsedWSDLMap, TAB);
		// code+=parsedWSDLMap;
		//
		// String xmlFromStringMethod = "public static Document
		// loadXMLFromString(String xml) throws Exception{\n";
		// xmlFromStringMethod += TAB + "DocumentBuilderFactory dbf =
		// DocumentBuilderFactory.newInstance();\n" + TAB
		// + "DocumentBuilder db = null;\n" + TAB + "db =
		// dbf.newDocumentBuilder();\n" + TAB
		// + "InputSource is = new InputSource();\n" + TAB +
		// "is.setCharacterStream(new StringReader(xml));\n"
		// + TAB + "Document doc = db.parse(is);\n" + TAB + "return doc;\n";
		//
		// xmlFromStringMethod += "}\n";
		//
		// CallRestfulServiceCode.applyTab(1,xmlFromStringMethod, TAB);
		// code+=xmlFromStringMethod;
		//
		//
		//
		//
		// code+="}\n";

		code += TAB + "/*** Method used to print the SOAP Response*/\n";
		code += TAB + "protected static String printSOAPResponse(SOAPMessage soapResponse) throws Exception {\n"
				+ TAB + TAB + "TransformerFactory transformerFactory = TransformerFactory.newInstance();\n"
				+ TAB + TAB + "Transformer transformer = transformerFactory.newTransformer();\n"
				+ TAB + TAB + "Source sourceContent = soapResponse.getSOAPPart().getContent();\n"
				+ TAB + TAB + "System.out.print(\"Response SOAP Message = \");\n" + TAB + TAB + "StringWriter writer = new StringWriter();\n"
				+ TAB + TAB + "StreamResult result = new StreamResult(writer);\n"
				+ TAB + TAB + "transformer.transform(sourceContent, result);\n" + TAB + TAB + "String output = writer.getBuffer().toString();\n"
				+ TAB + TAB + "System.out.println(output);\n" + TAB + TAB + "return output;\n" + TAB + "}\n" + "}\n";
		return code;
	}
}
