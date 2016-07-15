package eu.scasefp7.eclipse.servicecomposition.transformer;

import eu.scasefp7.eclipse.servicecomposition.codeInterpreter.Value;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.ApplicationDomain;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Service;
import eu.scasefp7.eclipse.servicecomposition.transformer.Similarity.ComparableName;


import java.util.HashMap;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * <h1>JungXMItoOwlTransform</h1> This class is used to transform an XMI to an
 * OwlService graph.
 * 
 * @author Manios Krasanakis
 */
public class JungXMItoOwlTransform {
	/**
	 * <h1>OwlService</h1> Represents a single OWL service, domain or property.
	 * The service can be locked in order to not be able to change.
	 */
	public static class OwlService extends Service {
		/**
		 * the operation or the argument that the node represents
		 */
		private Object content;
		/**
		 * if content can be modified
		 */
		private boolean editable = true;
		/**
		 * if it matches another variable
		 */
		private boolean isMatchedIO = false;
		/**
		 * the id of the node
		 */
		private int id=0;

		@Override
		public boolean equals(Object obj) {
			OwlService owlService=(OwlService)obj;
			if(owlService.getName().getContent().equals(this.getName().getContent())&& owlService.getType().equals(this.getType()) && owlService.id==this.id)
				return true;
				else return false;
		}
		
		
		public OwlService clone(Object service, int id, boolean isMatchedIO){
			if (service instanceof Operation){
			OwlService newService=new OwlService((Operation)service);
			newService.setId(id);
			newService.setisMatchedIO(isMatchedIO);
			return newService;
			}else if (service instanceof Argument){
				OwlService newService=new OwlService((Argument)service);
				newService.setId(id);
				newService.setisMatchedIO(isMatchedIO);
				return newService;
			}else if (service instanceof Service){
				OwlService newService=new OwlService((Service)service);
				newService.setId(id);
				newService.setisMatchedIO(isMatchedIO);
				return newService;
			}
			return null;
		}

		/**
		 * Generates an unlocked OWLService containing an operation.
		 * 
		 * @param operation
		 */
		public OwlService(Operation operation) {
			super(operation.getName().toString(), "Action");
			this.content = operation;
		}

		/**
		 * Generates an OWLService containing a domain.
		 * 
		 * @param domain
		 */
		public OwlService(ApplicationDomain domain) {
			super(domain.getName(), "Domain");
			this.content = domain;
		}

		/**
		 * Generates an OWLService containing a property.
		 * 
		 * @param argument
		 */
		public OwlService(Argument argument) {
			super(argument.getName().toString(), "Property");
			this.content = argument;
		}

		/**
		 * Generates an OWLService according to a Service prototype. The
		 * generated instance will have no content, unless the source service is
		 * also an OwlService.
		 * 
		 * @param service
		 */
		public OwlService(Service service) {
			super(service.getName().toString(), service.getType());
			content = null;
			if (service instanceof OwlService){
				content = ((OwlService) service).content;
				editable=((OwlService) service).editable;
				isMatchedIO=((OwlService) service).isMatchedIO;
				id=((OwlService) service).id;
				
			}
			
		}

		/**
		 * <h1>getOperation</h1>
		 * 
		 * @return the operation content of this instance, null if content is
		 *         not an operation
		 */
		public Operation getOperation() {
			return (content instanceof Operation) ? (Operation) content : null;
		}

		/**
		 * <h1>getApplicationDomain</h1>
		 * 
		 * @return the domain content of this instance, null if content is not a
		 *         domain
		 */
		public ApplicationDomain getApplicationDomain() {
			return (content instanceof ApplicationDomain) ? (ApplicationDomain) content : null;
		}

		/**
		 * <h1>getArgument</h1>
		 * 
		 * @return the property content of this instance, null if content is not
		 *         a property
		 */
		public Argument getArgument() {
			return (content instanceof Argument) ? (Argument) content : null;
		}

		/**
		 * <h1>autoConvert</h1> Automatically generates suitable content for
		 * this instance if no content is present. (i.e. when a Service was
		 * given to the constructor)
		 * 
		 * @throws Exception
		 *             if the service has been locked
		 */
		public void autoConvert() throws Exception {
			if (!editable)
				throw new Exception("Owl service is currently not editable: " + toString());
			if (content == null) {
				if (type.equals("Property"))
					this.content = new Argument(getName().toString(), "", "", false, true, null);
				else if (type.equals("Action"))
					this.content = new Operation(getName().toString(), (ApplicationDomain) null);
				else if (type.equals("Domain"))
					this.content = new ApplicationDomain(getName().toString());
			}
		}

		/**
		 * <h1>setContent</h1>
		 * 
		 * @param content
		 *            : the new content for the OwlService
		 * @throws Exception
		 *             if the service has been locked or the content type is not
		 *             supported
		 */
		public void setContent(Object content) throws Exception {
			if (!editable){
				System.out.println("Got an IOException: ");
				throw new Exception("Owl service is currently not editable: " + toString());}
				
			if (content instanceof Operation) {
				this.content = content;
				type = "Action";
				name = ((Operation) content).getName();
			} else if (content instanceof Argument) {
				this.content = content;
				type = "Property";
				name = ((Argument) content).getName();
			} else if (content instanceof Value) {
				this.content = content;
				type = "Property";
				name = ((Argument) content).getName();
			} else if (content instanceof ApplicationDomain) {
				this.content = content;
				type = "Domain";
				name = new ComparableName(((ApplicationDomain) content).getName());
			} else
				throw new Exception("Content " + content.toString() + " is invalid for OwlService."
						+ " (Only Operation, Argument or ApplicationDomain are instances are valid.");
		}
		
		
		/**
		 * <h1>editContent</h1>
		 * @param content
		 * @throws Exception
		 */
		public void editContent(Object content) throws Exception {
				
			if (content instanceof Operation) {
				this.content = content;
				type = "Action";
				name = ((Operation) content).getName();
			} else if (content instanceof Argument) {
				this.content = content;
				type = "Property";
				name = ((Argument) content).getName();
			} else if (content instanceof ApplicationDomain) {
				this.content = content;
				type = "Domain";
				name = new ComparableName(((ApplicationDomain) content).getName());
			} else
				throw new Exception("Content " + content.toString() + " is invalid for OwlService."
						+ " (Only Operation, Argument or ApplicationDomain are instances are valid.");
		}
		
		public void setName(String name){
			this.name = new ComparableName(name);
		}
		
		/**
		 * <h1>setisMatchedIO</h1> In order to set isMatchedIO variable of OwlService when it is a merged with another IO variable
		 * @param isMatchedIO
		 * */ 
		
		public void setisMatchedIO(boolean isMatchedIO){
			this.isMatchedIO=isMatchedIO;
		}
		/** 
		 * <h1>getisMatchedIO</h1> True if the IO is matched with another IO
		 * @return isMatchedIO
		 * */
		public boolean getisMatchedIO(){
			return isMatchedIO;
		}

		/**
		 * <h1>getId</h1>
		 * @return id
		 */
		public int getId(){
			return id;
		}
		
		/**
		 * <h1>setId</h1>
		 * @param id
		 */
		public void setId(int id){
			this.id=id;
		}
		
		/**
		 * <h1>lock</h1> Locks the service so that it can not be edited in any
		 * way. Locked services <b>cannot be unlocked</b>. Consider creating a
		 * new service in case you need an unlocked instance (with new
		 * OwlService(service)). Their content can be changed normally though.
		 */
		public void lock() {
			editable = false;
		}

		/**
		 * <h1>isUnlocked</h1>
		 * 
		 * @return true if the service can be edited
		 */
		public boolean isUnlocked() {
			return editable;
		}

		public Object getContent() {
			return content;
		}

	}

	/**
	 * <h1>createOwlGraph</h1> Generates an OWL JUNG graph from its
	 * corresponding XMI JUNG graph.<br/>
	 * This function can also convert the graph to a purely directed
	 * representation by assuming an inverse dataflow in undirected connections.
	 * 
	 * @param xmiGraph
	 *            : an XMI graph
	 * @param changeToDirected
	 *            : converts the graph into a directed graph
	 * @return the generated OWL graph
	 * @throws Exception
	 */
	public static Graph<OwlService, Connector> createOwlGraph(Graph<Service, Connector> xmiGraph,
			boolean changeToDirected) throws Exception {
		Graph<OwlService, Connector> graph = new SparseMultigraph<OwlService, Connector>();
		HashMap<Service, OwlService> map = new HashMap<Service, OwlService>();
		for (Service service : xmiGraph.getVertices()) {
			OwlService owlService;
			owlService = new OwlService(service);
			owlService.autoConvert();// also convert the XMI using classes from
										// OwlImporter, so that calculation are
										// consistent
			
			for (OwlService condition:graph.getVertices()){
				if (owlService.getName().getContent().equals(condition.getName().getContent())&& condition.getType().equalsIgnoreCase("Condition")){
					if (owlService.getId() >= condition.getId()) {
						condition.setId(owlService.getId() + 1);
					}
				}
			}
			map.put(service, owlService);
			graph.addVertex(owlService);
		}
		for (Connector connector : xmiGraph.getEdges(EdgeType.DIRECTED)) {
			OwlService source = map.get(connector.getSource());
			OwlService target = map.get(connector.getTarget());
			if (graph.findEdge(source, target) == null)
				graph.addEdge(new Connector(source, target, connector.getCondition()), source, target,
						EdgeType.DIRECTED);
		}
		for (Connector connector : xmiGraph.getEdges(EdgeType.UNDIRECTED)) {
			OwlService source = map.get(connector.getSource());
			OwlService target = map.get(connector.getTarget());
			if (graph.findEdge(target, source) == null)
				graph.addEdge(new Connector(target, source, connector.getCondition()), target, source,
						changeToDirected ? EdgeType.DIRECTED : EdgeType.UNDIRECTED);
		}
		return graph;
	}
	
	
}
