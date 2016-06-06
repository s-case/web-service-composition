package eu.scasefp7.eclipse.servicecomposition.operationCaller;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.namespace.QName;








import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;



//import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



import org.xml.sax.InputSource;

import eu.scasefp7.eclipse.servicecomposition.codeInterpreter.Value;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import gr.iti.wsdl.wsdlToolkit.ComplexObject;
import gr.iti.wsdl.wsdlToolkit.ITIWSDLParser;
import gr.iti.wsdl.wsdlToolkit.InvocationResult;
import gr.iti.wsdl.wsdlToolkit.NativeObject;
import gr.iti.wsdl.wsdlToolkit.ParsedWSDLDefinition;
import gr.iti.wsdl.wsdlToolkit.WSOperation;
import gr.iti.wsdl.wsdlToolkit.invocation.Axis2WebServiceInvoker;


/**
 * <h1>WSDLCaller</h1>
 * This class is used as an interface with <code>gr.iti.wsdl.wsdlToolkit</code>.
 * @author Manios Krasanakis
 */
public class WSDLCaller extends OperationCaller{
	//parsed WSDL (so as not to perform unnecessary loading)
	private HashMap<String, ParsedWSDLDefinition> parsedWSMap = new HashMap<String, ParsedWSDLDefinition> ();
	@Override
    public void callOperation(Operation operation) throws Exception{
    	if(operation.getDomain()==null)
    		throw new Exception("WSOperation "+operation.getName()+" does not have a corresponding domain");
    	PrintStream original = System.out;
    	ParsedWSDLDefinition parsedWS = parsedWSMap.get(operation.getDomain().getURI());
    	if(parsedWS==null){
    		//"http://www.webservicex.net/CurrencyConvertor.asmx?WSDL"
    		System.out.println("IMPORTING DOMAIN\t"+operation.getDomain().toString()+" ("+operation.getDomain().getURI()+")");
    		System.setOut(new PrintStream(new FileOutputStream("WSDL_log.txt")));
    		parsedWS = ITIWSDLParser.parseWSDLwithAxis(operation.getDomain().getURI(), true, true);
    		parsedWSMap.put(operation.getDomain().getURI(), parsedWS);
    		System.setOut(original);
    	}
    	
		System.out.println("CALLING SERVICE\t "+operation.toString());
    	
		//find WSOperation
		WSOperation wsOperation = null;
        for(Object op : parsedWS.getWsdlOperations())
            if(((WSOperation) op).getOperationName().equalsIgnoreCase(operation.getName().toString())){
            	wsOperation = (WSOperation) op;
                break;
            }
        if(wsOperation==null)
        	throw new Exception("WSOperation "+operation.getName()+" not found");
        //assign native inputs
        for(Object input : wsOperation.getHasInput().getHasNativeOrComplexObjects()) {
            //note that if the input is of type ComplexObject you have to recursively fill in the values
            if(input instanceof NativeObject){
                NativeObject no = (NativeObject)input;
                Value val = null;
                for(Argument arg : operation.getInputs())
                	if(no.getObjectName().getLocalPart().equalsIgnoreCase(arg.getName().toString()) && arg instanceof Value)
                		val = (Value)arg;
                if(val==null)
                	throw new Exception("Operation "+operation.getName()+" missing input "+no.getObjectName().getLocalPart());
                no.setHasValue(val.getValue());
            }
            else if(input instanceof ComplexObject){
            	ComplexObject no = (ComplexObject)input;
                Value val = null;
            	for(Argument arg : operation.getInputs())
                	if(no.getObjectName().getLocalPart().equalsIgnoreCase(arg.getName().toString()) && arg instanceof Value)
                		val = (Value)arg;
                if(val==null)
                	throw new Exception("Operation "+operation.getName()+" missing input "+no.getObjectName().getLocalPart());
            	assign(input, val, false);
            }
        }
        //invoke the web service
        System.setOut(new PrintStream(new FileOutputStream("error.txt")));
		 InvocationResult result=Axis2WebServiceInvoker.invokeWebService(
        			parsedWS.getWsdlURL(),
        		    new QName(parsedWS.getTargetNamespaceURI(), wsOperation.getOperationName()),
                    wsOperation.getHasInput(),
                    wsOperation,
                    parsedWS);    
		 System.setOut(original);
 		
        //System.out.println(result.getHasResponseInString());
		for(Argument output : operation.getOutputs()){
			for(Argument member : output.getSubtypes()){
				((Value)member).setValue("<failed assignment>");
			}
		}
		
		if (result.getResponseHasNativeOrComplexObjects().get(0) instanceof NativeObject && operation.getOutputs().get(0).getSubtypes().size()>0){
			assignNative(result, operation.getOutputs().get(0));
		}else{
        assign(result, operation.getOutputs().get(0), true);}
		System.out.println("FINISHED");
	}
	
	/**
	 * <h1>assign</h1>
	 * Performs assignment between a given argument and a WSDL data structure 
	 * @param data : a WSDL data structure 
	 * @param argument : the given argument
	 * @param getData : true to assign to the given argument, false to assign to the WSDL data structure
	 * @throws Exception
	 */
	protected void assign(Object data, Argument argument, boolean getData)throws Exception{
		if(data instanceof NativeObject){
			if(!(argument instanceof Value))
				throw new Exception(argument.getName()+" should be a Value");
			if(getData==false)
				((NativeObject) data).setHasValue(((Value) argument).getValue());
			else
				((Value) argument).setValue(((NativeObject) data).getHasValue());
			return;
		}
		if(data instanceof ComplexObject){
			int i = 0;
			for(Object dat : ((ComplexObject) data).getHasNativeObjects()){
				int retIndex = ((NativeObject) dat).getObjectName().toString().indexOf("}");
				for (Argument arg:argument.getSubtypes()){
					if (arg.getName().getContent().toString().equalsIgnoreCase(((NativeObject) dat).getObjectName().toString().substring(retIndex+1))){
				assign(dat, arg, getData);
					}
				i++;
				}
			}
			return;
		}
		if(!(data instanceof InvocationResult))
			throw new Exception("Neither native object, nor complex object, nor invocation result: "+data.toString());
		if(argument.getSubtypes().size()>1 && ((InvocationResult) data).getResponseHasNativeOrComplexObjects().size()==1){
			assign(((InvocationResult) data).getResponseHasNativeOrComplexObjects().get(0), argument, getData);
			return;
		}
		if(argument.isNative()){
			assign(((InvocationResult) data).getResponseHasNativeOrComplexObjects().get(0), argument, getData);
			return;
		}
		if(argument.getSubtypes().size()!=((InvocationResult) data).getResponseHasNativeOrComplexObjects().size())
			throw new Exception("Different number of members between class "+argument.getName()+"("+argument.getSubtypes().size()
					+" members) and invocation result "+((InvocationResult) data).toString()
					+" ("+((InvocationResult) data).getResponseHasNativeOrComplexObjects().size()+" members)");
		int i = 0;
		for(Object dat : ((InvocationResult) data).getResponseHasNativeOrComplexObjects()){
			assign(dat, argument.getSubtypes().get(i), getData);
			i++;
		}
	}
	
	protected void assignNative(Object data, Argument argument) throws Exception{
		String xmlString=((NativeObject)((InvocationResult)data).getResponseHasNativeOrComplexObjects().get(0)).getHasValue();
		Document doc=loadXMLFromString(xmlString);
		//NodeList nList = doc.getElementsByTagName("CurrentWeather");
		//Node nNode = nList.item(0);
		//Element eElement = (Element) nNode;
		Element element = doc.getDocumentElement();

        // get all child nodes
        NodeList nodes = element.getChildNodes();
        for (Argument arg:argument.getSubtypes()){
		 for (int i = 0; i < nodes.getLength(); i++) {
			 if (nodes.item(i).getNodeName()!=null){
			 if (nodes.item(i).getNodeName().equalsIgnoreCase(arg.getName().getContent().toString())){
		((Value)arg).setValue(nodes.item(i).getTextContent());
		break;
			 }
		 }
		 }
}
		//((Value)argument.getSubtypes().get(0)).setValue(eElement.getAttribute("Location"));
	}
	
	public static Document loadXMLFromString(String xml) throws Exception
	{
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		db = dbf.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xml));
		Document doc = db.parse(is);
		return doc;
		
//	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//	    DocumentBuilder builder = factory.newDocumentBuilder();
//	    InputSource is = new InputSource(new StringReader(xml));
//	    return builder.parse(is);
	}

}
