package eu.scasefp7.eclipse.servicecomposition.transformer;

import eu.scasefp7.eclipse.servicecomposition.Activator;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.ApplicationDomain;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.operation.ipr.ServiceLicense;
import eu.scasefp7.eclipse.servicecomposition.operation.ipr.ServiceTrialSchema;
import eu.scasefp7.eclipse.servicecomposition.transformer.Matcher;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class Transformer {

	/**
	 * <h1>ReplaceInformation</h1> This class is used to transfer replacement
	 * information about an operation
	 */
	public static class ReplaceInformation {
		/**
		 * the replaced owl service
		 */
		private OwlService owlService;
		/**
		 * the replaced operation
		 */
		private Operation operation;
		/**
		 * similarity weight
		 */
		private double weight;
		/**
		 * original action
		 */
		private Operation originalServiceOperation;
		/**
		 * alternative operations for replacement
		 */
		private ArrayList<ReplaceInformation> alternativeOperations = new ArrayList<ReplaceInformation>();

		public ReplaceInformation(OwlService owlService, Operation operation, double weight) {
			this.owlService = owlService;
			this.operation = operation;
			this.weight = weight;
			this.originalServiceOperation = owlService.getOperation();
		}

		@Override
		public String toString() {
			return "Replace '" + owlService.toString() + "' with '" + operation.toString();
		}

		/**
		 * <h1>reEvaluateWeight</h1> This function evaluates from scratch
		 * according to the final graph. Mandatory arguments are NOT taken into
		 * account.<br/>
		 * <b>Only use this function if you want to evaluate weights AFTER
		 * changes.</b>
		 */
		public void reEvaluateWeight(Graph<OwlService, Connector> graph) {
			// detect previous arguments (possibly adding items within loops)
			ArrayList<OwlService> perviousServices = getSources(owlService, graph);
			ArrayList<Argument> possibleArguments = new ArrayList<Argument>();
			for (OwlService perviousService : perviousServices) {
				Operation previousOperation = perviousService.getOperation();
				if (previousOperation != null)
					possibleArguments.addAll(previousOperation.getOutputs());
			}
			// detect next arguments (possibly adding items within loops)
			ArrayList<OwlService> nextServices = getDerived(owlService, graph);
			ArrayList<Argument> possibleOutputs = new ArrayList<Argument>();
			for (OwlService nextService : nextServices) {
				Operation nextOperation = nextService.getOperation();
				if (nextOperation != null)
					possibleOutputs.addAll(nextOperation.getInputs());
			}
			if (!IGNORE_ASSIGNED_POSSIBLE_OUTPUTS)
				for (int i = possibleOutputs.size() - 1; i >= 0; i--)
					for (OwlService outputService : graph.getVertices())
						if (outputService.getArgument() == possibleOutputs.get(i)
								&& graph.getPredecessorCount(outputService) > 0) {
							possibleOutputs.remove(i);
							break;
						}
			// detect mandatory arguments
			ArrayList<Argument> mandatoryArguments = new ArrayList<Argument>();
			weight = Matcher.match(new OwlService(originalServiceOperation), operation, mandatoryArguments,
					possibleArguments, possibleOutputs, Integer.MIN_VALUE);
		}

		/**
		 * <h1>getTargetSercice</h1>
		 * 
		 * @return the service in which the operation should be replaced
		 */
		public OwlService getTargetService() {
			return owlService;
		}

		/**
		 * <h1>getOperationToReplace</h1>
		 * 
		 * @return the operation that needs to replace the owlService
		 */
		public Operation getOperationToReplace() {
			return operation;
		}

		public Operation getOriginalServiceOperation() {
			return originalServiceOperation;
		}

		/**
		 * <h1>getWeight</h1>
		 * 
		 * @return the weight of the replacement (0 if no weight has been set)
		 */
		public double getWeight() {
			return weight;
		}
		/**
		 * <h1>setAlternativeOperations</h1> Add alternative operations for replacement to the list
		 * @param replace
		 */
		public void setAlternativeOperations(ReplaceInformation replace){
			this.alternativeOperations.add(replace);
		}
		/**
		 * <h1>getAlternativeOperations</h1>
		 * @return alternative operations list
		 */
		public ArrayList<ReplaceInformation> getAlternativeOperations(){
			return alternativeOperations;
		}

		/**
		 * <h1>isValid</h1> Should always initially return true for instances
		 * generated by <code>Transformer</code> functions.
		 * 
		 * @return false if the target service is or has been locked
		 */
		public boolean isValid() {
			return owlService.isUnlocked();
		}

		/**
		 * <h1>performReplacement</h1> Replaces the operation in the service and
		 * locks the service. (Afterwards <code>isValid</code> will return
		 * <code>false</code>.)
		 * 
		 * @throws Exception
		 *             if the replacement is not valid (i.e. because it has
		 *             already been performed or the service has somehow already
		 *             been locked.
		 */
		public void performReplacement(Graph<OwlService, Connector> graph) throws Exception {
			if (!isValid()) {
				throw new Exception();
			}
			System.out.println("\"" + owlService.getName().toString() + "\" replaced with \""
					+ operation.getName().toString() + "\" (Weight: " + weight + ")");

			owlService.setContent(operation);

			Collection<OwlService> services = new ArrayList<OwlService>(graph.getVertices());
			for (OwlService owlService : services) {
				if (owlService.getName().getContent().equals(this.owlService.getName().getContent())) {
					if (owlService.getId() >= this.owlService.getId()) {
						this.owlService.setId(owlService.getId() + 1);
					}
				}

			}

			owlService.lock();
		}
	}

	public static double REPLACE_NAME_SIMILARITY_THRESHOLD = 0;
	private static boolean IGNORE_ASSIGNED_POSSIBLE_OUTPUTS = false;

	/**
	 * <h1>loadProperties</h1> Loads matching properties from the file
	 * <i>matcher.properties</i>.
	 */
	public static void loadProperties() {
		Properties prop = new Properties();
		String propFileName = "matcher.properties";
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		try {
			URL fileURL = bundle.getEntry(propFileName);
			// InputStream inputStream = new FileInputStream(new File(new
			// URI(FileLocator.resolve(fileURL).toString().replaceAll(" ",
			// "%20"))));
			URL url = new URL("platform:/plugin/" + Activator.PLUGIN_ID + "/matcher.properties");
			InputStream inputStream = url.openConnection().getInputStream();
			prop.load(inputStream);
			REPLACE_NAME_SIMILARITY_THRESHOLD = Double
					.parseDouble(prop.getProperty("transformer.REPLACE_NAME_SIMILARITY_THRESHOLD"));
			IGNORE_ASSIGNED_POSSIBLE_OUTPUTS = !prop.getProperty("transformer.IGNORE_ASSIGNED_POSSIBLE_OUTPUTS").trim()
					.equalsIgnoreCase("false");
		} catch (Exception e) {
			System.err.println("Error occured while trying to load transformer settings from " + propFileName);
		}
	}

	private Graph<OwlService, Connector> graph;

	/**
	 * Generates an instance that allows transformations for the given graph.
	 * Transformer DOES NOT NECESSARILY APPLY TRANSFORMATIONS TO THE INITIAL
	 * GRAPH REFERENCE. Use <code>getGraph</code> to get the correct transformed
	 * graph instance.
	 * 
	 * @param graph
	 */
	public Transformer(Graph<OwlService, Connector> graph) {
		this.graph = graph;
	}

	/**
	 * <h1>getGraph</h1>
	 * 
	 * @return the OWL graph on which transformations are applied
	 */
	public Graph<OwlService, Connector> getGraph() {
		return graph;
	}

	/**
	 * <h1>expandOperations</h1> Adds all operation inputs as nodes to the
	 * graph. Uses <code>Matcher.sameVariable</code> to compare with already
	 * existing nodes and <code>Matcher.mergeVariables</code> to merge existing
	 * and new variables together.
	 * 
	 * @throws Exception
	 */
	public void expandOperations(OwlService service) throws Exception {

		Collection<OwlService> services = new ArrayList<OwlService>(graph.getVertices());

		Operation op = service.getOperation();
		if (op != null) {

			for (Argument arg : op.getInputs()) {

				OwlService argument = new OwlService(arg);
				argument.getArgument().setOwlService(argument);
				for (OwlService owlService : services) {
					if (owlService.getName().getContent().equals(argument.getName().getContent())) {
						if (owlService.getId() >= argument.getId()) {
							argument.setId(owlService.getId() + 1);
						}
					}

				}
				graph.addVertex(argument);
				graph.addEdge(new Connector(argument, service, ""), argument, service, EdgeType.DIRECTED);
				arg.getParent().remove(op);
				arg.getParent().add(op);

				for (Argument sub : arg.getSubtypes()) {
					OwlService subtype = new OwlService(sub);

					for (OwlService owlService : services) {
						if (owlService.getName().getContent().equals(subtype.getName().getContent())) {
							if (owlService.getId() >= subtype.getId()) {
								subtype.setId(owlService.getId() + 1);
							}
						}

					}

					graph.addVertex(subtype);
					graph.addEdge(new Connector(subtype, argument, ""), subtype, argument, EdgeType.DIRECTED);
					sub.getParent().remove(op);
					sub.getParent().add(op);

				}

			}
			for (Argument arg : op.getOutputs()) {
				addOutputs(arg, service, op);
			}

		}

	}

	/**
	 * <h1>addOutputs</h1>
	 * 
	 * @param arg
	 *            : node of level x
	 * @param service
	 *            : node of level x+1
	 * @param services
	 *            : all nodes of the graph
	 * @param op
	 *            : operation of node (applies only to services of type
	 *            "Action")
	 */
	private void addOutputs(Argument arg, OwlService service, Operation op) {
		OwlService argument = new OwlService(arg);
		argument.getArgument().setOwlService(argument);
		for (OwlService owlService : graph.getVertices()) {
			if (owlService.getName().getContent().equals(argument.getName().getContent())) {
				if (owlService.getId() >= argument.getId()) {
					argument.setId(owlService.getId() + 1);
				}
			}

		}

		graph.addVertex(argument);
		graph.addEdge(new Connector(service, argument, ""), service, argument, EdgeType.DIRECTED);
		if (!arg.getParent().contains(op)) {
			arg.getParent().add(op);
		}
		if (service.getArgument() != null) {
			for (int i = 0; i < service.getArgument().getParent().size(); i++) {
				if (!arg.getParent().contains(service.getArgument().getParent().get(i)))
					arg.getParent().add(service.getArgument().getParent().get(i));
			}
		}

		if (arg.getSubtypes().size() > 0) {
			for (Argument sub : arg.getSubtypes()) {
				addOutputs(sub, argument, op);
			}
		}
	}

	/**
	 * <h1>addInputs</h1>
	 * 
	 * @param arg:
	 *            node of level x
	 * @param service
	 *            : node of level x+1
	 * @param services
	 *            : all nodes of the graph
	 * @param op
	 *            : operation of node (applies only to services of type
	 *            "Action")
	 */
	private void addInputs(Argument arg, OwlService service, Operation op) {
		OwlService argument = new OwlService(arg);
		argument.getArgument().setOwlService(argument);
		for (OwlService owlService : graph.getVertices()) {
			if (owlService.getName().getContent().equals(argument.getName().getContent())) {
				if (owlService.getId() >= argument.getId()) {
					argument.setId(owlService.getId() + 1);
				}
			}

		}

		graph.addVertex(argument);
		graph.addEdge(new Connector(argument, service, ""), argument, service, EdgeType.DIRECTED);
		arg.getParent().remove(op);
		arg.getParent().add(op);

		if (arg.getSubtypes().size() > 0) {
			for (Argument sub : arg.getSubtypes()) {
				addInputs(sub, argument, op);
			}
		}
	}

	/**
	 * <h1>removeSimpleVariables</h1> Removes all graph Argument services that
	 * are direct inputs or outputs without any other complexity.
	 * 
	 * @throws Exception
	 */
	public void removeSimpleVariables() throws Exception {
		Collection<OwlService> services = new ArrayList<OwlService>(graph.getVertices());
		for (OwlService service : services) {
			if (service.getArgument() != null && graph.getNeighborCount(service) <= 1)
				graph.removeVertex(service);
		}
	}

	/**
	 * <h1>expandClasses</h1> Expand all class instances by adding their members
	 * to the graph. This may generate a ridiculously complex graph. Use
	 * <code>removeSimpleVariables</code> to make that graph comprehensive.
	 * 
	 * @throws Exception
	 */
	public void expandClasses() throws Exception {
		// create a copy of the original service list to iterate through
		Collection<OwlService> services = new ArrayList<OwlService>(graph.getVertices());
		// create variable properties for all services
		for (OwlService service : services) {
			Argument baseClassInstance = service.getArgument();
			if (baseClassInstance != null) {
				// Collection<OwlService> neighbors =
				// graph.getNeighbors(service);
				for (Argument arg : baseClassInstance.getSubtypes()) {
					OwlService neighbourExists = null;
					for (OwlService neighbor : graph.getNeighbors(service)) {
						Argument neighbourAsArgument = neighbor.getArgument();
						if (neighbourAsArgument == arg) {
							neighbourExists = neighbor;
							break;
						}
					}
					if (neighbourExists == null) {
						OwlService argument = new OwlService(arg);
						graph.addVertex(argument);
						graph.addEdge(new Connector(service, argument, ""), service, argument, EdgeType.UNDIRECTED);
					}
				}
			}
		}
	}

	/**
	 * <h1>createLinkedVariableGraph</h1> This function generates a new graph in
	 * which any input of an operation in the prototype is linked with an output
	 * of another operation if they are similar.<br/>
	 * The function <code>Matcher.getSameVariableInstances</code> that is used
	 * should be careful to not link variables with the same parent.
	 * 
	 * @param prototype
	 *            : the prototype of the graph
	 * @throws Exception
	 */
	public void createLinkedVariableGraph() throws Exception {
		Graph<OwlService, Connector> prototype = this.graph;
		Graph<OwlService, Connector> graph = new SparseMultigraph<OwlService, Connector>();
		// HashMap<OwlService, OwlService> map = new HashMap<OwlService,
		// OwlService>();
		graph = prototype;

		ArrayList<OwlService> allServices = new ArrayList<OwlService>(prototype.getVertices());
		ArrayList<OwlService> Inputs = new ArrayList<OwlService>();
		ArrayList<OwlService> Outputs = new ArrayList<OwlService>();
		for (OwlService service : allServices) {
			if (service.getType().contains("Property")) {
				if (graph.getInEdges(service).toArray().length == 0) {
					Inputs.add(service);
				} else if (graph.getOutEdges(service).toArray().length == 0) {
					Outputs.add(service);
				}
			}

		}

		// For each input find only one output that can be linked to it.

		for (OwlService in : Inputs) {
			OwlService sameVariable = Matcher.getSameVariableInstances(in, Outputs);

			if (sameVariable != null) {
				OwlService source = sameVariable;
				OwlService target = in;

				if ((graph.findEdge(source, target) == null)) {
					source.setisMatchedIO(true);
					target.setisMatchedIO(true);
					graph.addEdge(new Connector(source, target, ""), source, target, EdgeType.DIRECTED);

				}

			}
		}

		this.graph = graph;
	}

	/**
	 * <h1>getSources</h1> Finds all OwlServices whose output may affect a given
	 * service outside of loops.
	 * 
	 * @param service
	 *            : the given service
	 * @param graph
	 *            : the graph with all OwlServices
	 * @return an ArrayList containing all OwlServices that affect the given
	 *         service
	 */
	private static ArrayList<OwlService> getSources(OwlService service, Graph<OwlService, Connector> graph) {
		ArrayList<OwlService> sources = new ArrayList<OwlService>();
		sources.add(service);// prevents most loops from being falsely detected
		int addCount = 1;
		while (addCount != 0) {
			addCount = 0;
			int n = sources.size();
			for (int i = 0; i < n; i++) {
				if (graph.getPredecessors(sources.get(i)) != null) {
					for (OwlService preService : graph.getPredecessors(sources.get(i)))
						if (!sources.contains(preService))
							sources.add(preService);
				}
			}
		}
		sources.remove(service);
		return sources;
	}

	/**
	 * <h1>getDerived</h1> Finds all OwlServices that are affected by a given
	 * service. Uses only directed edges.
	 * 
	 * @param service
	 *            : the given service
	 * @param graph
	 *            : the graph with all OwlServices
	 * @return an ArrayList containing all OwlServices that affect the given
	 *         service
	 */
	private static ArrayList<OwlService> getDerived(OwlService service, Graph<OwlService, Connector> graph) {
		ArrayList<OwlService> derived = new ArrayList<OwlService>();
		derived.add(service);
		int addCount = 1;
		while (addCount != 0) {
			addCount = 0;
			int n = derived.size();
			for (int i = 0; i < n; i++) {
				if (graph.getSuccessors(derived.get(i)) != null) {
					for (OwlService preService : graph.getSuccessors(derived.get(i)))
						if (!derived.contains(preService))
							derived.add(preService);
				}
			}
		}
		derived.remove(service);
		return derived;
	}

	/**
	 * <h1>getReplaceInformation</h1> Finds the best matching choices between a
	 * the graph's OWL services and the given operations using the private
	 * overload <code>getReplaceInformation</code>.
	 * 
	 * @param operations
	 *            : the list of operations to be checked for matching
	 * @param maxMemory
	 *            : the size of the returned list (keeping only the replacements
	 *            with more weight - smaller values allow for optimizations)
	 * @return a list of at most <code>maxMemory</code> elements of valid
	 *         <code>ReplaceInformation</code> instances
	 * @throws Exception
	 */
	public ArrayList<ReplaceInformation> getReplaceInformation(ArrayList<Operation> operations, int maxMemory)
			throws Exception {
		return getReplaceInformation(operations, graph.getVertices(), maxMemory);
	}

	/**
	 * <h1>getReplaceInformation</h1> Finds the best matching choices between a
	 * given OWL service in the graph and the given operations using the private
	 * overload <code>getReplaceInformation</code>.
	 * 
	 * @param operations
	 *            : the list of operations to be checked for matching
	 * @param service
	 *            : the service to replace
	 * @param maxMemory
	 *            : the size of the returned list (keeping only the replacements
	 *            with more weight - smaller values allow for optimizations)
	 * @return a list of at most <code>maxMemory</code> elements of valid
	 *         <code>ReplaceInformation</code> instances
	 * @throws Exception
	 */
	public ArrayList<ReplaceInformation> getReplaceInformation(ArrayList<Operation> operations, OwlService service,
			int maxMemory) throws Exception {
		ArrayList<OwlService> serviceList = new ArrayList<OwlService>();
		serviceList.add(service);
		return getReplaceInformation(operations, serviceList, maxMemory);
	}

	/**
	 * <h1>getReplaceInformation</h1> Finds the best matching choices between a
	 * list of OWL services and the given operations.
	 * 
	 * @param operations
	 *            : the list of operations to be checked for matching
	 * @param maxMemory
	 *            : the size of the returned list (keeping only the replacements
	 *            with more weight - smaller values allow for optimizations)
	 * @return a list of at most <code>maxMemory</code> elements of valid
	 *         <code>ReplaceInformation</code> instances
	 * @throws Exception
	 */
	private ArrayList<ReplaceInformation> getReplaceInformation(ArrayList<Operation> operations,
			Collection<OwlService> services, int maxMemory) throws Exception {
		ArrayList<ReplaceInformation> replacements = new ArrayList<ReplaceInformation>();
		double minPlacement = Double.NEGATIVE_INFINITY;
		for (OwlService service : services)
			if (service.isUnlocked()) {
				Operation op = service.getOperation();
				if (op != null) {
					// detect previous arguments (possibly adding items within
					// loops)
					ArrayList<OwlService> perviousServices = getSources(service, graph);
					ArrayList<Argument> possibleArguments = new ArrayList<Argument>();
					for (OwlService perviousService : perviousServices) {
						Operation previousOperation = perviousService.getOperation();
						if (previousOperation != null)
							possibleArguments.addAll(previousOperation.getOutputs());
					}
					// detect next arguments (possibly adding items within
					// loops)
					ArrayList<OwlService> nextServices = getDerived(service, graph);
					ArrayList<Argument> possibleOutputs = new ArrayList<Argument>();
					for (OwlService nextService : nextServices) {
						Operation nextOperation = nextService.getOperation();
						if (nextOperation != null)
							possibleOutputs.addAll(nextOperation.getInputs());
					}
					if (!IGNORE_ASSIGNED_POSSIBLE_OUTPUTS)
						for (int i = possibleOutputs.size() - 1; i >= 0; i--)
							for (OwlService outputService : graph.getVertices())
								if (outputService.getArgument() == possibleOutputs.get(i)
										&& graph.getPredecessorCount(outputService) > 0) {
									possibleOutputs.remove(i);
									break;
								}
					// detect mandatory arguments
					ArrayList<Argument> mandatoryArguments = new ArrayList<Argument>();
					for (OwlService connectedService : graph.getNeighbors(service)) {
						Argument arg = connectedService.getArgument();
						if (arg != null)
							mandatoryArguments.add(arg);
					}
					// detect matching operation
					for (Operation operation : operations) {
						double match = Matcher.match(service, operation, mandatoryArguments, possibleArguments,
								possibleOutputs, Integer.MIN_VALUE);
						if (match >= minPlacement && match > REPLACE_NAME_SIMILARITY_THRESHOLD) {
							int id = 0;
							for (int i = 1; i < replacements.size(); i++)
								if (match < replacements.get(i - 1).getWeight()
										&& match >= replacements.get(i).getWeight()) {
									id = i;
									break;
								}
							replacements.add(id, new ReplaceInformation(service, operation, match));
							if (replacements.size() > maxMemory && maxMemory > 0)
								replacements.remove(replacements.size() - 1);
							minPlacement = replacements.get(replacements.size() - 1).getWeight();
						}
					}
				}
			}
		return replacements;
	}

	/**
	 * <h1>replaceSingleLinkingOperation</h1> Replaces the contents of a
	 * <b>single</b> unlocked OwlService and then locks it.<br/>
	 * The replacement is selected so that the <code>Matcher.match</code> metric
	 * is maximized.
	 * 
	 * @param operations
	 *            : the list of operations
	 * @return the operation that was inserted into the graph
	 * @throws Exception
	 */
	public OwlService placeSingleLinkingOperation(ArrayList<Operation> operations) throws Exception {
		OwlService dummyService = new OwlService(new Operation("", (ApplicationDomain) null));
		Collection<Connector> connectors = graph.getEdges();
		double bestMatch = 0;
		Operation targetOperation = null;
		Connector targetConnector = null;
		boolean found=false;
		for (Connector connector : connectors) {
			Operation source = ((OwlService) connector.getSource()).getOperation();
			Operation target = ((OwlService) connector.getTarget()).getOperation();
			if ((source != null || ((OwlService) connector.getSource()).getArgument() == null)
					&& (target != null)) {
				// detect previous arguments (possibly adding items within
				// loops)
				ArrayList<OwlService> perviousServices = getSources((OwlService) connector.getSource(), graph);
				perviousServices.add((OwlService) connector.getSource());
				ArrayList<Argument> possibleArguments = new ArrayList<Argument>();
				for (OwlService perviousService : perviousServices) {
					Operation previousOperation = perviousService.getOperation();
					if (previousOperation != null) {
						// possibleArguments.addAll(previousOperation.getOutputs());
						
						
								for (Argument out : previousOperation.getOutputs()) {
									getNative(out, possibleArguments);
								}
							
							
					}
				}
				// detect next arguments (possibly adding items within loops)
				ArrayList<OwlService> nextServices = getDerived((OwlService) connector.getTarget(), graph);
				nextServices.add((OwlService) connector.getTarget());
				ArrayList<Argument> possibleOutputs = new ArrayList<Argument>();
				for (OwlService nextService : nextServices) {
					Operation nextOperation = nextService.getOperation();
					if (nextOperation != null)
						possibleOutputs.addAll(nextOperation.getInputs());
				}
				for (int i = possibleOutputs.size() - 1; i >= 0; i--)
					for (OwlService service : graph.getVertices())
						if (service.getArgument() == possibleOutputs.get(i) && graph.getPredecessorCount(service) > 0) {
							possibleOutputs.remove(i);
							break;
						}
				// detect mandatory arguments
				ArrayList<Argument> mandatoryArguments = new ArrayList<Argument>();
				
				// detect matching operation
				for (Operation operation : operations) {
					//detect if all target inputs are matched
					int matchedTargetInputs=0;
					if (target != null){
						
						for (Argument in: target.getInputs()){
							if (in.getOwlService().getisMatchedIO()){
								matchedTargetInputs++;
							}
						}
						if (target.getInputs().size()==matchedTargetInputs){
							break;
						}
					}
//					if (operation.getName().toString().equals("ResolveIP")){
//						int a=1;
//					}
					double match = Matcher.match(dummyService, operation, mandatoryArguments, possibleArguments,
							possibleOutputs, -1);
//					if (match > 0)
//						match += nextServices.size();
					boolean contains=false;
					for (OwlService pre :perviousServices){
						if (pre.getOperation()!=null){
							if (pre.getOperation().equals(operation)){
								contains=true;
							}
						}
					}
					for (OwlService next :nextServices){
						if (next.getOperation()!=null){
							if (next.getOperation().equals(operation)){
								contains=true;
							}
						}
					}
					if (match > bestMatch && !contains) {
						bestMatch = match;
						targetOperation = operation;
						targetConnector = connector;
					}
				}
				ArrayList<Argument> nativeOutputs = new ArrayList<Argument>();
				ArrayList<Operation> previousOperations = new ArrayList<Operation>();
//				for (OwlService service:perviousServices){
//					if (service.getOperation()!=null){
//						previousOperations.add(service.getOperation());
//					}
//				}
//				if (previousOperations!=null){
//				for (Operation op: previousOperations){
//					for (Argument out : op.getOutputs()) {
//						getNative(out, nativeOutputs);
//					}
//				}
//				}
				if (targetOperation != null){
					for (Argument out : targetOperation.getOutputs()) {
						getNative(out, nativeOutputs);
					}
				}
				
				if (targetOperation != null && ((OwlService) targetConnector.getTarget()).getOperation() != null) {
					int matched = 0;
					for (Argument in : ((OwlService) targetConnector.getTarget()).getOperation().getInputs()) {
						for (Argument out : nativeOutputs) {

							if (Matcher.hasSame(out, in)) {
								matched++;
								break;
							}
						}
					}
					if (targetConnector != null && (targetOperation.getInputs().size() <= matched)) {
						found=true;
						break;
					}
				}
			}
		}
		
		
		if (targetOperation != null && ((OwlService) targetConnector.getTarget()).getOperation() != null) {
//			int matched = 0;
//			for (Argument in : ((OwlService) targetConnector.getTarget()).getOperation().getInputs()) {
//				for (Argument out : nativeOutputs) {
//
//					if (Matcher.hasSame(out, in)) {
//						matched++;
//						break;
//					}
//				}
//			}
			if (found) {
				graph.removeEdge(targetConnector);
				dummyService.setContent(targetOperation);
				dummyService.lock();
				graph.addEdge(new Connector((OwlService) targetConnector.getSource(), dummyService, targetConnector.getCondition()),
						(OwlService) targetConnector.getSource(), dummyService, EdgeType.DIRECTED);
				graph.addEdge(new Connector(dummyService, (OwlService) targetConnector.getTarget(), ""), dummyService,
						(OwlService) targetConnector.getTarget(), EdgeType.DIRECTED);
				return dummyService;
			}
		}
		
		return null;
	}

	private void getNative(Argument output, ArrayList<Argument> nativeOutputs) {
		if (output.isNative() && !output.isArray()) {
			nativeOutputs.add(output);
		}
		if (!output.isArray()) {
			for (Argument sub : output.getSubtypes()) {
				getNative(sub, nativeOutputs);
			}
		}
	}
}
