package eu.fp7.scase.servicecomposition.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;



import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import eu.fp7.scase.servicecomposition.transformer.Similarity.ComparableName;

/**
 * <h1>JungXMIImporter</h1>
 * This class imports data from <i>.dbd</i> files in xmi format.
 * @author Manios Krasanakis
 */
public class JungXMIImporter {
	public static String prefix = "auth.storyboards:";
	public static String[] textTypes = {"Note", "Text"};
	public static String[] types = {"storyboardactions", "storyboardproperties", "storyboardstartnode", "storyboardendnode", "storyboardconditions", "storyboardstoryboards"};
	
	/**
	 * <h1>Service</h1>
	 * This class represents a single service. Each service has a name and a type. Properties and text (such as notes)
	 * can also be considered services.
	 */
	public static class Service extends Object{
		protected ComparableName name;
		protected String type;
		/**
		 * Generates a <code>Service</code> from its XMI node.
		 * @param e : an XMI node
		 */
		Service(Element e){
			String foundName = e.getAttribute("name");
			if(foundName==null || foundName.isEmpty())
				foundName = e.getAttribute("description");
			type = e.getAttribute("type");
			if(type==null || type.isEmpty())
				type = e.getAttribute("xmi:type");
			if(type.startsWith(prefix))
				type = type.substring(prefix.length());
			name = new ComparableName(foundName);
		}
		/**
		 * Generates a <code>Service</code> from its characteristics.
		 * @param name
		 * @param type
		 */
		public Service(String name, String type){
			this.name = new ComparableName(name);
			this.type = type;
		}
		/**
		 * <h1>getName</h1>
		 * @return the name of the service as defined in either the <i>name</i> or the <i>description</i> attribute
		 */
		public ComparableName getName(){
			return name;
		}
		/**
		 * <h1>getType</h1>
		 * @return the type of the service as defined in either the <i>type</i> or the <i>xmi:type</i> attribute
		 */
		public String getType(){
			return type;
		}
		/**
		 * <h1>toString</h1>
		 * This function is overrided so that JUNG will display a meaningful string on
		 * the <i>Service</i> when <code>ToStringLabeller</code> is used for labeling vertexes.
		 * @return a string representation of the service
		 */
		@Override
		public String toString(){
			//return type+": "+name;
			if(name.isEmpty())
				return type;
			return name.toString();
		}
		/**
		 * <h1>isText</h1>
		 * @return <code>true</code> if the service's type corresponds to a text type
		 */
		public boolean isText() {
			for(String text : textTypes)
				if(text.compareTo(type)==0)
					return true;
			return false;
		}
	};
	
	/**
	 * <h1>XMIService</h1>
	 * This class is used for storing information concerning interactivity between <code>Service</code>
	 * instances. It is generated as a placeholder between XMI ids and their connections that will help
	 * in mapping the various services.
	 */
	private static class XMIService{
		protected String id = "";
		protected Service service;
		protected ArrayList<String> towards = new ArrayList<String>();
		protected ArrayList<String> towardsCondition = new ArrayList<String>();
		protected ArrayList<String> connections = new ArrayList<String>();
		/**
		 * Generates an <i>XMIService</i> (including most of its connections) from its XMI node. Uses the
		 * same node to generate the base <code>Service</code> instance. For the directed connections generated,
		 * conditions are also added.
		 * <b>Custom connectors for text are not generated here.</b>
		 * @param e : an XMI node
		 */
		public XMIService(Element e){
			service = new Service(e);
			id = e.getAttribute("xmi:id");
			NamedNodeMap propertyNode = e.getAttributes();
			for(int attr=0;attr<propertyNode.getLength();attr++){
				if(propertyNode.item(attr).getNodeName().compareTo("properties")==0)
					connections.add(propertyNode.item(attr).getNodeValue());
				else if(propertyNode.item(attr).getNodeName().compareTo("nextNode")==0
						|| propertyNode.item(attr).getNodeName().compareTo("firstNode")==0){
					towards.add(propertyNode.item(attr).getNodeValue());
					towardsCondition.add("");
				}
			}
			NodeList conditions = e.getElementsByTagName("conditionPaths");
			for(int condition=0;condition<conditions.getLength();condition++){
				Element c = (Element)conditions.item(condition);
				if(c.getParentNode()==e){
					towards.add(c.getAttribute("nextConditionNode"));
					towardsCondition.add(c.getAttribute("name"));
				}
			}
		}
		/**
		 * <h1>getService</h1>
		 * @return the base <code>Service</code> instance
		 */
		public Service getService(){
			return service;
		}
	}
	
	/**
	 * <h1>Connector</h1>
	 * This class represents a connector between two services. It also contains a possible condition
	 * that must hold true for the transition to take place.
	 */
	public static class Connector extends Object{
		protected Service source;
		protected Service target;
		protected String condition;
		public Connector(Service source, Service target, String condition){
			this.source = source;
			this.target = target;
			this.condition = condition;
		}
		
		
		public Connector clone(Service source, Service target, String condition){
			Connector newConnector= new Connector(source, target, condition);
			return newConnector;
		}
		
		/**
		 * <h1>toString</h1>
		 * This function is overrided so that JUNG will display a meaningful string on
		 * the <i>Connector</i> when <code>ToStringLabeller</code> is used for labeling edges.
		 * @return a string representation of the connector
		 */
		@Override
		public String toString(){
			return condition;
		}
		/**
		 * <h1>getSource</h1>
		 * @return the <code>Service</code> the connector starts from
		 */
		public Service getSource(){
			return source;
		}
		/**
		 * <h1>getTarget</h1>
		 * @return the <code>Service</code> the connector ends at
		 */
		public Service getTarget(){
			return target;
		}
		/**
		 * <h1>getCondition</h1>
		 * @return a possible condition in order to move on the connector
		 */
		public String getCondition() {
			return condition;
		}
		
		public void setCondition(String s){
			this.condition=s;
		}
	}
	
	/**
	 * <h1>createGraph</h1>
	 * Generates a <i>Service</i> graph from an XMI file.
	 * @param xmiPath : a path to the XMI file to import
	 * @param includeText : weather to include text (such as notes) as vertexes or ignore it completely
	 * @return a graph with <code>Service</code> instances as vertexes and both directed and undirected connections
	 * between those services (undirected connections exist outside the logic flow and connect services with properties).
	 * If <code>includeText</includeText> is <code>true</code>, the graph is completely directed.
	 * @throws Exception describing any errors that have occurred
	 */
	public static Graph<Service, Connector> createGraph(String xmiPath, boolean includeText) throws Exception{
		Graph<Service, Connector> graph = new SparseMultigraph<Service, Connector>();
		//open file and load document element
		File fXmlFile = new File(xmiPath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		Element documentElement = doc.getDocumentElement();
		
		//import all diagrams (get list from storyboard list)
		NodeList diagrams = documentElement.getElementsByTagName(prefix+"StoryboardDiagram");
		for(int diagram=0;diagram<diagrams.getLength();diagram++){
			//get story id
			String storyId = ((Element)diagrams.item(diagram)).getAttribute("xmi:id");
			//generate a list of all diagram properties
			ArrayList<XMIService> services = new ArrayList<XMIService>();
			HashMap<String, XMIService> serviceId = new HashMap<String, XMIService>();
			for(String type : types){
				NodeList serviceChildren = ((Element)diagrams.item(diagram)).getElementsByTagName(type);
				for(int child=0;child<serviceChildren.getLength();child++){
					Element e = (Element)serviceChildren.item(child);
					if(e.getParentNode()==diagrams.item(diagram)){
						XMIService service = new XMIService(e);
						services.add(service);
						serviceId.put(service.id, service);
					}
				}
			}
			//find corresponding notation:Diagram
			Element notationDiagram = null;
			if(includeText){
				NodeList notationDiagrams = documentElement.getElementsByTagName("notation:Diagram");
				for(int notation=0;notation<notationDiagrams.getLength();notation++){
					if(((Element)notationDiagrams.item(notation)).getAttribute("element").compareTo(storyId)==0){
						notationDiagram = (Element)notationDiagrams.item(notation);
						break;
					}
				}
			}
			//find services in notation:Diagram
			if(notationDiagram!=null){
				NodeList children = notationDiagram.getElementsByTagName("children");
				for(int child=0;child<children.getLength();child++){
					for(String textType : textTypes){
						Element e = (Element)children.item(child);
						if(((Element)children.item(child)).getAttribute("type").compareTo(textType)==0 && e.getParentNode()==notationDiagram){
							XMIService service = new XMIService(e);
							services.add(service);
							serviceId.put(service.id, service);
						}
					}
				}
			}
			//find pins in notation:Diagram and add them to id table
			if(notationDiagram!=null){
				NodeList children = notationDiagram.getElementsByTagName("children");
				for(int child=0;child<children.getLength();child++){
					Element e = (Element)children.item(child);
					if(!e.getAttribute("element").isEmpty() && e.getParentNode()==notationDiagram){
						String element = e.getAttribute("element");
						XMIService service = serviceId.get(element);
						if(service==null)
							throw new Exception("Could not find element service (with id "+element+") for node "+e.getAttribute("xmi:id"));
						serviceId.put(e.getAttribute("xmi:id"), service);
					}
				}
			}
			//add all services to the graph
			for(XMIService service : services){
				graph.addVertex(service.getService());
			}
			//add all directed connections to the graph
			for(XMIService service : services)
				for(int i=0;i<service.towards.size();i++){
					XMIService targetService = serviceId.get(service.towards.get(i));
					if(targetService==null)
						throw new Exception("Could not perform a directed connection from "+service.getService().toString()+" to id "+service.towards.get(i));
					graph.addEdge(new Connector(service.getService(), targetService.getService(), service.towardsCondition.get(i)), service.getService(), targetService.getService(), EdgeType.DIRECTED);
				}
			//add property connections
			for(XMIService service : services)
				for(String str : service.connections){
					XMIService targetService = serviceId.get(str);
					if(serviceId.get(str)==null)
						throw new Exception("Could not perform undirected connection between "+service.getService().toString()+" and id "+str);
					graph.addEdge(new Connector(service.getService(), targetService.getService(), ""), service.getService(), serviceId.get(str).getService(), EdgeType.UNDIRECTED);
				}
			if(notationDiagram!=null){
				//add all connectors to the graph
				NodeList edges = notationDiagram.getElementsByTagName("edges");
				for(int edge=0;edge<edges.getLength();edge++){
					Element e = (Element)edges.item(edge);
					if(e.getParentNode()==notationDiagram){
						String source = e.getAttribute("source");
						String target = e.getAttribute("target");
						XMIService sourceService = serviceId.get(source);
						if(sourceService==null)
							throw new Exception("Could not find source service (with id "+source+") for edge "+e.getAttribute("xmi:id"));
						XMIService targetService = serviceId.get(target);
						if(targetService==null)
							throw new Exception("Could not find target service (with id "+target+") for edge "+e.getAttribute("xmi:id"));
						//ignore already placed connections
						if(targetService.getService().isText() || sourceService.getService().isText())
							graph.addEdge(new Connector(sourceService.getService(), targetService.getService(), ""), sourceService.getService(), targetService.getService(), EdgeType.DIRECTED);
					}
				}
			}
			
		}
		
		return graph;
		
	}
}
