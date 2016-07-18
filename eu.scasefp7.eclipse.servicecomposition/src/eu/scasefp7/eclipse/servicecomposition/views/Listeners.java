package eu.scasefp7.eclipse.servicecomposition.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import eu.scasefp7.eclipse.servicecomposition.Activator;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Service;
import eu.scasefp7.eclipse.servicecomposition.transformer.Matcher;
import eu.scasefp7.eclipse.servicecomposition.transformer.Transformer;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.transformer.Transformer.ReplaceInformation;
import eu.scasefp7.eclipse.servicecomposition.ui.RenameConditionDialog;
import eu.scasefp7.eclipse.servicecomposition.ui.RenameEdgeConditionDialog;
import eu.scasefp7.eclipse.servicecomposition.ui.TreeDialog;

public class Listeners implements Listener {

	protected GraphConnection selectedGraphEdge;
	protected GraphNode selectedGraphNode;
	protected ServiceCompositionView view;
	protected edu.uci.ics.jung.graph.Graph<OwlService, Connector> jungGraph;
	protected String eventType;
	
	private Job AddNewOperationJob;

	Listeners(GraphConnection edge, GraphNode graphNode, ServiceCompositionView SCview, String type) {
		selectedGraphEdge = edge;
		selectedGraphNode = graphNode;
		view = SCview;
		jungGraph = view.getJungGraph();
		eventType = type;
	}

	@Override
	public void handleEvent(Event event) {
		if (eventType.equals("removeEdge")){
			removeEdge(selectedGraphEdge);
		}else if (eventType.equals("renameConditionEdge")){
			renameConditionEdge(selectedGraphEdge);
		}else if (eventType.equals("matchIO")){
			matchIO(selectedGraphNode);		
		}else if (eventType.equals("linkStartNode")){
			String mode = "StartNode";
			linkOperation(selectedGraphNode, mode);
		}else if (eventType.equals("linkCondition")){
			String mode = "Condition";
			linkOperation(selectedGraphNode, mode);
		}else if (eventType.equals("linkOperation")){
			String mode = "Operation";
			linkOperation(selectedGraphNode, mode);
		}else if (eventType.equals("removeConditionNode")){
			String mode = "Condition";
			removeNode(selectedGraphNode, mode);
		}else if (eventType.equals("renameConditionNode")){
			renameConditionNode(selectedGraphNode);
		}else if (eventType.equals("showAlternatives")){
			showAlternatives(selectedGraphNode);
		}else if (eventType.equals("addNewOperation")){
			addNewOperation();
		}else if (eventType.equals("addNewCondition")){
			addNewCondition();
		}else if (eventType.equals("removeOperationNode")){
			String mode = "Operation";
			removeNode(selectedGraphNode, mode);
		}

	}

	/**
	 * <h1>removeEdge</h1> Remove selected edge from the graph.
	 * 
	 * @param edge
	 */
	private void removeEdge(GraphConnection edge) {
		// remove edge from jung graph
		for (int i = 0; i < jungGraph.getEdges().size(); i++) {
			Connector con = (Connector) jungGraph.getEdges().toArray()[i];
			if (((MyNode) edge.getSource().getData()).getObject().equals(con.getSource())
					&& ((MyNode) edge.getDestination().getData()).getObject().equals(con.getTarget())) {
				// remove edge from jung graph

				jungGraph.removeEdge(con);
				// if edge connects matched IO set isMatchedIO to false.
				if (isNodeMatchedIO(edge.getSource()) && isNodeMatchedIO(edge.getDestination())) {
					OwlService source = (OwlService) ((MyNode) edge.getSource().getData()).getObject();
					boolean keep = false;
					for (int l = jungGraph.getSuccessorCount(source) - 1; l >= 0; l--) {
						OwlService successor = (OwlService) jungGraph.getSuccessors(source).toArray()[l];
						if (successor.getisMatchedIO()) {
							keep = true;
						}
					}
					if (!keep) {
						((OwlService) ((MyNode) edge.getSource().getData()).getObject()).setisMatchedIO(false);

						// Style Node
						ZestLabelProvider labelProvider = new ZestLabelProvider();
						labelProvider.selfStyleNode((MyNode) edge.getSource().getData(), edge.getSource());
					}
					((OwlService) ((MyNode) edge.getSource().getData()).getObject()).getArgument().getMatchedInputs()
							.remove(((OwlService) ((MyNode) edge.getDestination().getData()).getObject())
									.getArgument());
					((OwlService) ((MyNode) edge.getDestination().getData()).getObject()).setisMatchedIO(false);

					// Style Node
					ZestLabelProvider labelProvider = new ZestLabelProvider();
					labelProvider.selfStyleNode((MyNode) edge.getDestination().getData(), edge.getDestination());
				}

				// remove destination node from source node's linked
				// connections and connections(zest).
				for (int j = 0; j < ((MyNode) edge.getSource().getData()).getLinkedConnections().size(); j++) {
					if (((MyNode) edge.getSource().getData()).getLinkedConnections().get(j).getDestination()
							.equals((MyNode) edge.getDestination().getData())) {
						((MyNode) edge.getSource().getData()).getLinkedConnections()
								.remove(((MyNode) edge.getSource().getData()).getLinkedConnections().get(j));
					}
				}
				if (((MyNode) edge.getSource().getData()).getConnections()
						.contains((MyNode) edge.getDestination().getData())) {
					((MyNode) edge.getSource().getData()).getConnections()
							.remove((MyNode) edge.getDestination().getData());
				}
				// remove edge from zest graph

				edge.dispose();
				

				break;
			}
		}

		view.setJungGraph(jungGraph);
		view.setSavedWorkflow(false);
		view.updateRightComposite(jungGraph);
		view.setFocus();
	}
	
	
	/**
	 * <h1>renameCondition</h1>Rename the selected edge. Edge is coming out of a
	 * condition node.
	 * 
	 * @param edge
	 */
	private void renameConditionEdge(GraphConnection edge) {
		Shell shell = view.getSite().getWorkbenchWindow().getShell();
		RenameEdgeConditionDialog dialog = new RenameEdgeConditionDialog(shell);
		String s = "";
		dialog.create();
		dialog.setDialogLocation();
		if (dialog.open() == Window.OK) {
			s = dialog.getConditionName().trim();
			System.out.println(s);
		} else {
			return;
		}
		boolean exist = false;
		// If a string was returned, say so.
		if ((s != null) && (s.trim().length() > 0)) {

			for (int i = 0; i < jungGraph.getEdges().size(); i++) {
				Connector con = (Connector) jungGraph.getEdges().toArray()[i];
				if (((MyNode) edge.getSource().getData()).getObject().equals(con.getSource())
						&& ((MyNode) edge.getDestination().getData()).getObject().equals(con.getTarget())) {
					OwlService source = (OwlService) con.getSource();
					OwlService target = (OwlService) con.getTarget();
					for (int j = 0; j < jungGraph.getOutEdges(source).size(); j++) {
						if (jungGraph.getOutEdges(source).toArray()[j].toString().equalsIgnoreCase(s)) {

							final Display disp = shell.getDisplay();
							disp.syncExec(new Runnable() {
								@Override
								public void run() {
									MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
											"Name already exists.");
								}
							});
							exist = true;
							break;
						}
					}
					if (!exist) {
						jungGraph.removeEdge(con);
						con.setCondition(s);
						jungGraph.addEdge(con, source, target, EdgeType.DIRECTED);
						edge.setText(s);

						System.out.println("New condition name=" + s);
						break;
					}

				}
			}
		} else {
			final Display disp = shell.getDisplay();
			disp.syncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openInformation(disp.getActiveShell(), "Error occured", "This is not a valid name.");
				}

			});
		}

		view.setJungGraph(jungGraph);
		view.setSavedWorkflow(false);
		view.updateRightComposite(jungGraph);
		view.setFocus();
	}
	
	/**
	 * <h1>matchIO</h1> It is used to match (link variables) outputs with
	 * inputs.
	 * 
	 * @param node
	 *            : The selected graph node.
	 */

	private void matchIO(final GraphNode node) {

		// ArrayList<String> list = new ArrayList<String>();
		ArrayList<Object> list = new ArrayList<Object>();

		if (node.getTargetConnections().size() == 0) {
			// System.out.println("Node is an input");
			Graph graph = view.getViewer().getGraphControl();
			for (int j = 0; j < graph.getNodes().size(); j++) {
				GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
				if ((graphNode.getSourceConnections().size() == 0)
						&& ((OwlService) ((MyNode) graphNode.getData()).getObject()).getType().contains("Property")
						&& Matcher.common(
								((OwlService) ((MyNode) node.getData()).getObject()).getArgument().getParent(),
								((OwlService) ((MyNode) graphNode.getData()).getObject()).getArgument().getParent())
								.isEmpty()) {
					
					if (((OwlService) ((MyNode) graphNode.getData()).getObject()).getArgument().getSubtypes().isEmpty()
					// && !isMemberOfArray
					) {
						list.add((OwlService) ((MyNode) graphNode.getData()).getObject());
					}
				}
			}
			// Open selection window
			String option = "matchinput";
			SelectionWindowNode(list, option);

		} else
			if (node.getSourceConnections().size() == 0
					|| (node.getSourceConnections().size() != 0
							&& ((OwlService) ((MyNode) node.getData()).getObject())
									.getisMatchedIO()
					&& ((OwlService) ((MyNode) ((GraphNode) ((GraphConnection) node.getSourceConnections().get(0))
							.getDestination()).getData()).getObject()).getisMatchedIO())) {
			// System.out.println("Node is an output");
			Graph graph = view.getViewer().getGraphControl();
			for (int j = 0; j < graph.getNodes().size(); j++) {
				GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
				if ((graphNode.getTargetConnections().size() == 0)
						&& ((OwlService) ((MyNode) graphNode.getData()).getObject()).getType().contains("Property")
						&& Matcher.common(
								((OwlService) ((MyNode) node.getData()).getObject()).getArgument().getParent(),
								((OwlService) ((MyNode) graphNode.getData()).getObject()).getArgument().getParent())
								.isEmpty()) {
					
					if (((OwlService) ((MyNode) graphNode.getData()).getObject()).getArgument().getSubtypes().isEmpty()
					// && !isMemberOfArray
					) {
						list.add((OwlService) ((MyNode) graphNode.getData()).getObject());
					}
				}
			}
			// Open selection window
			String option = "matchoutput";
			SelectionWindowNode(list, option);
		}

		view.setJungGraph(jungGraph);
		view.setSavedWorkflow(false);
		view.updateRightComposite(jungGraph);
		view.setFocus();

	}
	
	/**
	 * <h1>linkOperation</h1>Link selected node with the node the user chooses
	 * from the selection window(nodes in this case can be anything but IO)
	 * 
	 * @param node
	 * @param mode
	 */
	private void linkOperation(final GraphNode node, final String mode) {

		ArrayList<Object> list = new ArrayList<Object>();
		// String option = "link";
		Collection<OwlService> services = new ArrayList<OwlService>(jungGraph.getVertices());
		for (OwlService service : services) {
			if (mode == "Operation") {
				if (!(service.getType().contains("Property"))
						&& !((((MyNode) node.getData()).getObject()).equals(service))
						&& !(service.getType().contains("StartNode"))) {

					list.add(service);

				}
			} else if (mode == "StartNode") {
				if (service.getType().contains("Action") || (service.getType().contains("EndNode"))) {

					list.add(service);
				}

			} else if (mode == "Condition") {
				if (!(service.getType().contains("Property"))
						&& !((((MyNode) node.getData()).getObject()).equals(service))) {

					list.add(service);

				}
			}
		}
		// Open selection window
		String option = "link";
		SelectionWindowNode(list, option);

		view.setJungGraph(jungGraph);
		view.setSavedWorkflow(false);
		view.updateRightComposite(jungGraph);
		view.setFocus();
	}
	
	
	/**
	 * <h1>removeNode</h1>Remove operation node and all its IOs.
	 * 
	 * @param node
	 * @param mode
	 */
	private void removeNode(GraphNode node, String mode) {

		if (mode == "Operation") {
			// find and remove all inputs
			if (jungGraph.getPredecessorCount((OwlService) ((MyNode) node.getData()).getObject()) > 0)
				for (int i = jungGraph.getPredecessorCount((OwlService) ((MyNode) node.getData()).getObject())
						- 1; i >= 0; i--) {
					OwlService n1 = (OwlService) jungGraph
							.getPredecessors((OwlService) ((MyNode) node.getData()).getObject()).toArray()[i];

					if (n1.getType().equals("Property")) {

						// before removing n1 check if it is matched input and
						// set
						// the
						// remaining matched output to false (if remaining
						// output is matched with another input keep it as it
						// is)
						// OwlService changedNode = null;
						OwlService previouslyMatchedNode = null;
						boolean keep = false;
						if (n1.getisMatchedIO()) {
							// An input can be link with only one output so
							// PredecessorCount is maximum 1.
							for (int k = jungGraph.getPredecessorCount(n1) - 1; k >= 0; k--) {
								OwlService n2 = (OwlService) jungGraph.getPredecessors(n1).toArray()[k];
								previouslyMatchedNode = n2;
								if (n2.getisMatchedIO()) {
									for (int l = jungGraph.getSuccessorCount(n2) - 1; l >= 0; l--) {
										OwlService n3 = (OwlService) jungGraph.getSuccessors(n2).toArray()[l];
										if (n3.getisMatchedIO() && n3 != n1) {
											keep = true;
										}
									}
									if (!keep) {
										n2.setisMatchedIO(false);
										// changedNode = n2;
									}
								}
							}
						}

						Graph graph = view.getViewer().getGraphControl();
						for (int j = 0; j < graph.getNodes().size(); j++) {
							GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
							if (previouslyMatchedNode != null) {
								if (((OwlService) ((MyNode) graphNode.getData()).getObject())
										.equals(previouslyMatchedNode)) {
									// Remove input node to be deleted from the
									// connections and linked connections of its
									// matched output node.
									for (int k = 0; k < ((MyNode) graphNode.getData()).getConnections().size(); k++) {
										if (((OwlService) ((MyNode) graphNode.getData()).getConnections().get(k)
												.getObject()).equals(n1)) {
											((MyNode) graphNode.getData()).getConnections().remove(k);
										}
									}
									for (int k = 0; k < ((MyNode) graphNode.getData()).getLinkedConnections()
											.size(); k++) {
										if (((OwlService) ((MyNode) graphNode.getData()).getLinkedConnections().get(k)
												.getDestination().getObject()).equals(n1)) {
											((MyNode) graphNode.getData()).getLinkedConnections().remove(k);
										}
									}
									// If properties of the matched output node
									// have changed update its style.
									if (!keep) {
										// Style Node
										ZestLabelProvider labelProvider = new ZestLabelProvider();
										labelProvider.selfStyleNode((MyNode) graphNode.getData(), graphNode);
									}
								}

							}

							// Delete input node.
							if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(n1)) {
								// if (!isNodeMatchedIO(graphNode)) {
								graphNode.dispose();
								jungGraph.removeVertex(n1);

								// } else {
								// ((OwlService) ((MyNode)
								// graphNode.getData()).getObject()).setisMatchedIO(false);

								// }
							}
						}
					}
				}

			// find and remove all outputs
			recursivelyRemoveOutputs((MyNode) node.getData());
		}

		// remove this node from linked connections and connections of the
		// node(s) that it was
		// connected.
		Graph graph = view.getViewer().getGraphControl();
		for (int i = 0; i < graph.getNodes().size(); i++) {
			GraphNode graphNode = (GraphNode) graph.getNodes().get(i);
			for (int j = 0; j < ((MyNode) graphNode.getData()).getLinkedConnections().size(); j++) {
				if (((MyNode) graphNode.getData()).getLinkedConnections().get(j).getDestination()
						.equals(node.getData())) {
					((MyNode) graphNode.getData()).getLinkedConnections().remove(j);
				}
			}
			if (((MyNode) graphNode.getData()).getConnections().contains(node.getData())) {

				((MyNode) graphNode.getData()).getConnections().remove(node.getData());
			}
		}

		// remove selected node
		jungGraph.removeVertex((OwlService) ((MyNode) node.getData()).getObject());
		node.dispose();

		
		view.setJungGraph(jungGraph);
		view.setSavedWorkflow(false);
		view.updateRightComposite(jungGraph);
		view.setFocus();
	}
	
	
	/**
	 * Remove the outputs of operation <code>node</node>
	 * 
	 * @param node
	 */
	private void recursivelyRemoveOutputs(MyNode node) {
		if (node.getLinkedConnections().size() != 0) {
			for (int i = 0; i < node.getLinkedConnections().size(); i++) {

				MyConnection con = node.getLinkedConnections().get(i);
				if (con.getSource().equals(node)) {
					MyNode tmpNode = con.getDestination();
					if (((OwlService) tmpNode.getObject()).getType().equals("Property")
							&& !(((OwlService) tmpNode.getObject()).getisMatchedIO()
									&& ((OwlService) node.getObject()).getisMatchedIO())) {

						recursivelyRemoveOutputs(tmpNode);

					}
				}
			}
		}

		if (((OwlService) node.getObject()).getType().equals("Property")) {

			boolean change = false;
			ArrayList<OwlService> changedNodes = new ArrayList<OwlService>();
			// before removing n1 check if it is matched io and set the
			// remaining matched io to false
			if (((OwlService) node.getObject()).getisMatchedIO()) {
				for (int k = jungGraph.getSuccessorCount((OwlService) node.getObject()) - 1; k >= 0; k--) {
					OwlService n2 = (OwlService) jungGraph.getSuccessors((OwlService) node.getObject()).toArray()[k];
					// If node has successors they will be matched variables so
					// it is an unnecessary check.
					if (n2.getisMatchedIO()) {
						n2.setisMatchedIO(false);
						changedNodes.add(n2);
						change = true;

					}
				}

			}

			Graph graph = view.getViewer().getGraphControl();
			for (int j = 0; j < graph.getNodes().size(); j++) {
				GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
				// Check if the properties of the node have changed and update
				// its style.
				for (OwlService changedNode : changedNodes) {
					if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(changedNode)
							&& change == true) {
						// Style Node
						ZestLabelProvider labelProvider = new ZestLabelProvider();
						labelProvider.selfStyleNode((MyNode) graphNode.getData(), graphNode);
					}
				}
				// Check if the node is the one which should be removed.
				if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals((OwlService) node.getObject())) {
					
					// remove jung node
					jungGraph.removeVertex((OwlService) node.getObject());
					// remove zest node
					graphNode.dispose();

					
				}

			}
		}

		

	}


	
	/**
	 * <h1>replaceOperation</h1> Replace an operation with the alternative
	 * operation that the user has chosen.
	 * 
	 * @param selectedItem:
	 *            the alternative operation that the user has chosen
	 */
	private void replaceOperation(Object selectedItem) {

		// find previous and next services
		MyNode previousOperation = null;
		GraphNode previousGraphNode = null;
		MyNode nextOperation = null;
		GraphNode nextGraphNode = null;
		String edgeText = "";

		Graph graph = view.getViewer().getGraphControl();
		for (int i = 0; i < graph.getNodes().size(); i++) {
			GraphNode graphNode = (GraphNode) graph.getNodes().get(i);
			for (int j = 0; j < ((MyNode) graphNode.getData()).getLinkedConnections().size(); j++) {
				if (((MyNode) graphNode.getData()).getLinkedConnections().get(j).getDestination()
						.equals(selectedGraphNode.getData())
						&& (((OwlService) ((MyNode) graphNode.getData()).getObject()).getOperation() != null
								|| ((OwlService) ((MyNode) graphNode.getData()).getObject()).getType()
										.equals("StartNode")
								|| ((OwlService) ((MyNode) graphNode.getData()).getObject()).getType()
										.equals("Condition"))) {
					previousOperation = ((MyNode) graphNode.getData());
					previousGraphNode = graphNode;
					edgeText = ((MyNode) graphNode.getData()).getLinkedConnections().get(j).getLabel();
					break;
				}
			}
			for (int k = 0; k < graphNode.getTargetConnections().size(); k++) {
				if (((MyNode) ((GraphConnection) graphNode.getTargetConnections().get(k)).getSource().getData())
						.equals(selectedGraphNode.getData())
						&& (((OwlService) ((MyNode) graphNode.getData()).getObject()).getOperation() != null
								|| ((OwlService) ((MyNode) graphNode.getData()).getObject()).getType().equals("EndNode")
								|| ((OwlService) ((MyNode) graphNode.getData()).getObject()).getType()
										.equals("Condition"))) {
					nextOperation = ((MyNode) graphNode.getData());
					nextGraphNode = graphNode;
					break;
				}
			}
		}
		// get replaceinfo
		List<ReplaceInformation> alternativeOperations = ((MyNode) selectedGraphNode.getData())
				.getAlternativeOperations();

		// remove selected node
		String mode = "Operation";
		removeNode(selectedGraphNode, mode);

		// add new node
		Collection<OwlService> services = new ArrayList<OwlService>(jungGraph.getVertices());
		
		OwlService owlService = new OwlService(((ReplaceInformation) selectedItem).getOperationToReplace());
		for (OwlService s : services) {
			if (owlService.getName().getContent().equals(s.getName().getContent())) {
				if (owlService.getId() <= s.getId()) {
					owlService.setId(s.getId() + 1);
				}
			}

		}

		jungGraph.addVertex(owlService);

		System.out.println("New operation: " + owlService + " is added to the graph");
		Transformer transformer = new Transformer(jungGraph);
		try {
			// Add the io variables of the operation
			transformer.expandOperations(owlService);
			// transformer.createLinkedVariableGraph();
		} catch (Exception e) {
			Activator.log("Error while expanding new operation", e);
			e.printStackTrace();
		}
		edu.uci.ics.jung.graph.Graph<OwlService, Connector> tempGraph = new SparseMultigraph<OwlService, Connector>();
		tempGraph.addVertex(owlService);
		Transformer tempTransformer = new Transformer(tempGraph);
		try {
			// Add the io variables of the operation
			tempTransformer.expandOperations(owlService);

		} catch (Exception e) {
			Activator.log("Error while expanding new operation", e);
			e.printStackTrace();
		}
		view.addOperationInZest(owlService, tempGraph);

		// add alternatives

		for (int j = 0; j < graph.getNodes().size(); j++) {
			GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
			if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(owlService)) {
				((MyNode) graphNode.getData()).setAlternativeOperations(alternativeOperations);
			}
		}
		// add previous connection
		// Add link to Jung Graph
		jungGraph.addEdge(new Connector((OwlService) previousOperation.getObject(), owlService, edgeText),
				(OwlService) previousOperation.getObject(), owlService, EdgeType.DIRECTED);
		// Add link to Zest Graph

		for (int j = 0; j < graph.getNodes().size(); j++) {
			GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
			if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(owlService)) {

				MyConnection connect = new MyConnection(previousGraphNode.toString() + graphNode.toString(), edgeText,
						(MyNode) previousGraphNode.getData(), (MyNode) graphNode.getData());
				((MyNode) previousGraphNode.getData()).getLinkedConnections().add(connect);
				((MyNode) previousGraphNode.getData()).getConnections().add((MyNode) graphNode.getData());

				GraphConnection graphConnection = new GraphConnection(graph, SWT.NONE, previousGraphNode, graphNode);
				graphConnection.setText(edgeText);
				EntityConnectionData connectionData = new EntityConnectionData((MyNode) previousGraphNode.getData(),
						(MyNode) graphNode.getData());
				graphConnection.setData(connectionData);

			}
		}

		// add next connection

		// Add link to Jung Graph
		jungGraph.addEdge(new Connector(owlService, (OwlService) nextOperation.getObject(), ""), owlService,
				(OwlService) nextOperation.getObject(), EdgeType.DIRECTED);
		// Add link to Zest Graph

		for (int j = 0; j < graph.getNodes().size(); j++) {
			GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
			if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(owlService)) {

				MyConnection connect = new MyConnection(graphNode.toString() + nextGraphNode.toString(), "",
						(MyNode) graphNode.getData(), (MyNode) nextGraphNode.getData());
				((MyNode) graphNode.getData()).getLinkedConnections().add(connect);
				((MyNode) graphNode.getData()).getConnections().add((MyNode) nextGraphNode.getData());

				GraphConnection graphConnection = new GraphConnection(graph, SWT.NONE, graphNode, nextGraphNode);
				EntityConnectionData connectionData = new EntityConnectionData((MyNode) graphNode.getData(),
						(MyNode) nextGraphNode.getData());
				graphConnection.setData(connectionData);

			}
		}

		view.setJungGraph(jungGraph);
		view.setSavedWorkflow(false);
		view.updateRightComposite(jungGraph);
		view.setFocus();

	}

	
	
	private void renameConditionNode(GraphNode node) {
		Shell shell = view.getSite().getWorkbenchWindow().getShell();
		RenameConditionDialog dialog = new RenameConditionDialog(shell);

		String s = "";
		dialog.create();
		dialog.setDialogLocation();
		if (dialog.open() == Window.OK) {
			if (dialog.getConditionValue().trim().isEmpty()) {
				s = dialog.getConditionName().trim();
			} else {
				s = dialog.getConditionName().trim() + dialog.getConditionSymbol() + dialog.getConditionValue().trim();
			}
			System.out.println(s);
		} else {
			return;
		}

		// If a string was returned, say so.
		if ((s != null) && (s.trim().length() > 0)) {

			((OwlService) ((MyNode) node.getData()).getObject()).setName(s);
			System.out.println("New condition name " + s);
			((MyNode) node.getData()).setName(s);
			node.setText(s);
			
			view.setJungGraph(jungGraph);
			view.setSavedWorkflow(false);
			view.updateRightComposite(jungGraph);
			view.setFocus();
		}
	}

	
	private void showAlternatives(GraphNode node) {
		ArrayList<Object> list = new ArrayList<Object>();
		for (ReplaceInformation replacement : ((MyNode) node.getData()).getAlternativeOperations()) {
			if (!((OwlService) ((MyNode) selectedGraphNode.getData()).getObject()).getOperation()
					.equals(replacement.getOperationToReplace()))
				list.add(replacement);
		}
		SelectionWindowNode(list, "alternativeOperations");
	}


	
	
	
	
	
	/**
	 * <h1>addNewOperation</h1> Add a new Operation with its IO. For this, the
	 * method calls <code>SelectionWindowOp</code> method in order to display a
	 * selection window to the user with all the repository operations.
	 */
	private void addNewOperation() {
		if (jungGraph == null) {
			final Shell shell = view.getSite().getWorkbenchWindow().getShell();
			final Display disp = shell.getDisplay();
			disp.syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openInformation(disp.getActiveShell(), "Warning", "Create a new workflow first!");
				}
			});

		} else {

			final Shell shell = view.getSite().getWorkbenchWindow().getShell();
			final Display disp = shell.getDisplay();
			

			AddNewOperationJob = new Job("Add new operation") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Loading operations...", IProgressMonitor.UNKNOWN);

					try {
						view.loadOperations(disp, shell);
						ArrayList<Operation> nonPrototypeOperations = new ArrayList<Operation>();

						final ArrayList<String> list = new ArrayList<String>();
						// String option = "addnode";
						for (Operation op : ServiceCompositionView.getOperations()) {

							if (!op.isPrototype()) {
								String opName = op.getName().toString();
								list.add(opName);
								nonPrototypeOperations.add(op);
							}

						}
						// check if user has cancelled
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						// Open selection window
						disp.syncExec(new Runnable() {
							public void run() {

								TreeDialog dialog = new TreeDialog(shell, "S-CASE Operations");
								dialog.setDisp(disp);
								dialog.setOperations(nonPrototypeOperations);
								dialog.create();
								dialog.setDialogLocation();
								if (dialog.open() == Window.OK) {
									Operation selectedItem = dialog.getOperation();
									if (selectedItem != null) {
										addNode(selectedItem,  ServiceCompositionView.getOperations());
									}

								} else {
									return;
								}
								// SelectionWindowOp(shell, dialog,
								// nonPrototypeOperations, list);
							}
						});

						monitor.done();
						return Status.OK_STATUS;
					} catch (Exception ex) {
						Activator.log("Error while loading the operations from the ontology", ex);
						ex.printStackTrace();
						return Status.CANCEL_STATUS;
					} finally {
						monitor.done();
					}

				}

			};
			AddNewOperationJob.schedule();
		}
		view.setJungGraph(jungGraph);
		view.setSavedWorkflow(false);
		view.updateRightComposite(jungGraph);
		view.setFocus();

	}
	
	
	/**
	 * <h1>addNewCondition</h1> Add new condition node.
	 */
	private void addNewCondition() {

		if (jungGraph == null) {
			final Shell shell = view.getSite().getWorkbenchWindow().getShell();
			final Display disp = shell.getDisplay();
			disp.syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openInformation(disp.getActiveShell(), "Warning", "Create a new workflow first!");
				}
			});

		} else {

			Shell shell = view.getSite().getWorkbenchWindow().getShell();
			RenameConditionDialog dialog = new RenameConditionDialog(shell);

			String s = "";
			dialog.create();
			dialog.setDialogLocation();
			if (dialog.open() == Window.OK) {
				if (dialog.getConditionValue().trim().isEmpty()) {
					s = dialog.getConditionName().trim();
				} else {
					s = dialog.getConditionName().trim() + dialog.getConditionSymbol()
							+ dialog.getConditionValue().trim();
				}
				System.out.println(s);
			} else {
				return;
			}
			boolean exist = false;
			// If a string was returned, say so.
			if ((s != null) && (s.trim().length() > 0)) {

				Collection<OwlService> services = jungGraph.getVertices();
				for (OwlService service : services) {
					if (service.getType().equalsIgnoreCase("Condition")
							&& service.getName().toString().equalsIgnoreCase(s)) {

						final Display disp = shell.getDisplay();
						disp.syncExec(new Runnable() {

							@Override
							public void run() {
								MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
										"Name already exists.");
							}
						});
						exist = true;
						break;
					}
				}

				if (!exist) {
					Service condition = new Service(s, "Condition");
					OwlService owlService = new OwlService(condition);
					jungGraph.addVertex(owlService);

					MyNode node = new MyNode(owlService.toString(), owlService.toString(), owlService);
					// viewer.addNode(node);
					Graph graph = view.getViewer().getGraphControl();
					GraphNode graphNode = new GraphNode(graph, SWT.NONE, s, node);
					graphNode.setLocation(view.getPoint().x, view.getPoint().y);
					ZestLabelProvider labelProvider = new ZestLabelProvider();
					labelProvider.selfStyleNode((MyNode) graphNode.getData(), graphNode);

					System.out.println("New condition " + s);

				}
			} else {
				final Display disp = shell.getDisplay();
				disp.syncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
								"This is not a valid name.");
					}
				});
			}
		}

		view.setJungGraph(jungGraph);
		view.setSavedWorkflow(false);
		view.updateRightComposite(jungGraph);
		view.setFocus();

	}
	
	
	/**
	 * <h1>linkNode</h1>Link selected node with another node. It is used for non
	 * IO nodes.
	 * 
	 * @param selectedItem
	 */
	private void linkNode(Object selectedItem) {

		OwlService source = (OwlService) ((MyNode) selectedGraphNode.getData()).getObject();

		Collection<OwlService> services = new ArrayList<OwlService>(jungGraph.getVertices());

		for (OwlService service : services) {
			if (service.equals(selectedItem)) {
				if (source.getType().contains("Action") || source.getType().contains("StartNode")) {

					// Add link to Jung Graph
					jungGraph.addEdge(new Connector(source, service, ""), source, service, EdgeType.DIRECTED);
					// Add link to Zest Graph
					Graph graph = view.getViewer().getGraphControl();
					for (int j = 0; j < graph.getNodes().size(); j++) {
						GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
						if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(service)) {

							MyConnection connect = new MyConnection(selectedGraphNode.toString() + graphNode.toString(),
									"", (MyNode) selectedGraphNode.getData(), (MyNode) graphNode.getData());
							((MyNode) selectedGraphNode.getData()).getLinkedConnections().add(connect);
							((MyNode) selectedGraphNode.getData()).getConnections().add((MyNode) graphNode.getData());

							GraphConnection graphConnection = new GraphConnection(graph, SWT.NONE, selectedGraphNode,
									graphNode);
							EntityConnectionData connectionData = new EntityConnectionData(
									(MyNode) selectedGraphNode.getData(), (MyNode) graphNode.getData());
							graphConnection.setData(connectionData);

						}
					}

				} else if (source.getType().contains("Condition")) {
					Shell shell = view.getSite().getWorkbenchWindow().getShell();
					RenameEdgeConditionDialog dialog = new RenameEdgeConditionDialog(shell);
					// System.out.println(dialog.open());
					String s = "";
					dialog.create();
					dialog.setDialogLocation();
					if (dialog.open() == Window.OK) {
						s = dialog.getConditionName().trim();
						System.out.println(s);
					} else {
						return;
					}
					boolean exist = false;
					// If a string was returned, say so.
					if ((s != null) && (s.trim().length() > 0)) {

						for (int j = 0; j < jungGraph.getOutEdges(source).size(); j++) {
							if (jungGraph.getOutEdges(source).toArray()[j].toString().equalsIgnoreCase(s)) {

								final Display disp = shell.getDisplay();
								disp.syncExec(new Runnable() {
									@Override
									public void run() {
										MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
												"Name already exists.");
									}
								});
								exist = true;
								break;
							}
						}
						if (!exist) {
							// Add link to Jung Graph
							jungGraph.addEdge(new Connector(source, service, s), source, service, EdgeType.DIRECTED);

							// Add link to Zest Graph
							Graph graph = view.getViewer().getGraphControl();
							for (int j = 0; j < graph.getNodes().size(); j++) {
								GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
								if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(service)) {

									MyConnection connect = new MyConnection(
											selectedGraphNode.toString() + graphNode.toString(), s,
											(MyNode) selectedGraphNode.getData(), (MyNode) graphNode.getData());
									((MyNode) selectedGraphNode.getData()).getLinkedConnections().add(connect);
									((MyNode) selectedGraphNode.getData()).getConnections()
											.add((MyNode) graphNode.getData());

									GraphConnection graphConnection = new GraphConnection(graph, SWT.NONE,
											selectedGraphNode, graphNode);
									graphConnection.setText(s);
									EntityConnectionData connectionData = new EntityConnectionData(
											(MyNode) selectedGraphNode.getData(), (MyNode) graphNode.getData());
									graphConnection.setData(connectionData);

								}
							}

							view.setJungGraph(jungGraph);
							view.setSavedWorkflow(false);
							view.updateRightComposite(jungGraph);
							view.setFocus();
						}

					} else {
						final Display disp = shell.getDisplay();
						disp.syncExec(new Runnable() {

							@Override
							public void run() {
								MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
										"This is not a valid name.");
							}

						});
					}
				}
			}
		}

	}

	
	/**
	 * <h1>addEdge</h1>Add an edge in order to create a link between an input
	 * and an output variable. The link is always directed from the output to
	 * the input. Match IOs
	 * 
	 * @param selectedItem
	 * @param option
	 */
	private void addEdge(Object selectedItem, String option) {
		OwlService source = (OwlService) ((MyNode) selectedGraphNode.getData()).getObject();
		Collection<OwlService> services = new ArrayList<OwlService>(jungGraph.getVertices());
		// String targetName = selectedItem.toString();
		// Similarity.ComparableName targetComparableName = new
		// Similarity.ComparableName(targetName);
		for (OwlService service : services) {
			if (service.equals(selectedItem)) {
				if (option == "matchinput") {
					// Add link to Jung Graph (the edge should be from an output
					// to an input)

					jungGraph.addEdge(new Connector(service, source, ""), service, source, EdgeType.DIRECTED);

					Graph graph = view.getViewer().getGraphControl();
					for (int j = 0; j < graph.getNodes().size(); j++) {
						GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
						if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(service)) {

							MyConnection connect = new MyConnection(graphNode.toString() + selectedGraphNode.toString(),
									"", (MyNode) graphNode.getData(), (MyNode) selectedGraphNode.getData());
							((MyNode) graphNode.getData()).getLinkedConnections().add(connect);
							((MyNode) graphNode.getData()).getConnections().add((MyNode) selectedGraphNode.getData());

							GraphConnection graphConnection = new GraphConnection(graph, SWT.NONE, graphNode,
									selectedGraphNode);

							EntityConnectionData connectionData = new EntityConnectionData((MyNode) graphNode.getData(),
									(MyNode) selectedGraphNode.getData());
							graphConnection.setData(connectionData);

							source.setisMatchedIO(true);

							OwlService destination = (OwlService) ((MyNode) graphNode.getData()).getObject();
							destination.setisMatchedIO(true);
							service.getArgument().addMatchedInputs(source.getArgument());

							ZestLabelProvider labelProvider = new ZestLabelProvider();
							labelProvider.selfStyleNode((MyNode) graphNode.getData(), graphNode);
							labelProvider.selfStyleNode((MyNode) selectedGraphNode.getData(), selectedGraphNode);

						}

					}

					System.out.println("New link from output: " + service.getName() + " to input: " + source.getName());
				}
				if (option == "matchoutput") {
					// Add link to Jung Graph (the edge should be from an output
					// to an input)

					jungGraph.addEdge(new Connector(source, service, ""), source, service, EdgeType.DIRECTED);

					Graph graph = view.getViewer().getGraphControl();
					for (int j = 0; j < graph.getNodes().size(); j++) {
						GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
						if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(service)) {

							MyConnection connect = new MyConnection(selectedGraphNode.toString() + graphNode.toString(),
									"", (MyNode) selectedGraphNode.getData(), (MyNode) graphNode.getData());
							((MyNode) selectedGraphNode.getData()).getLinkedConnections().add(connect);
							((MyNode) selectedGraphNode.getData()).getConnections().add((MyNode) graphNode.getData());

							GraphConnection graphConnection = new GraphConnection(graph, SWT.NONE, selectedGraphNode,
									graphNode);

							EntityConnectionData connectionData = new EntityConnectionData(
									(MyNode) selectedGraphNode.getData(), (MyNode) graphNode.getData());
							graphConnection.setData(connectionData);

							source.setisMatchedIO(true);

							OwlService destination = (OwlService) ((MyNode) graphNode.getData()).getObject();
							destination.setisMatchedIO(true);
							source.getArgument().addMatchedInputs(service.getArgument());

							ZestLabelProvider labelProvider = new ZestLabelProvider();
							labelProvider.selfStyleNode((MyNode) graphNode.getData(), graphNode);
							labelProvider.selfStyleNode((MyNode) selectedGraphNode.getData(), selectedGraphNode);
						}
					}

					System.out.println("New link from output: " + service.getName() + " to input: " + source.getName());
				}

				view.setJungGraph(jungGraph);
				view.setSavedWorkflow(false);
				view.updateRightComposite(jungGraph);
				view.setFocus();
				break;
			}
		}
	}
	
	
	/**
	 * <h1>addNode</h1>Add the selected operation (chosen from the selection
	 * window) to the graph with its IOs.
	 * 
	 * @param selectedItem
	 * @param operations
	 */
	protected void addNode(Object selectedItem, ArrayList<Operation> operations) {
		String operationName = selectedItem.toString();
		// Similarity.ComparableName operationComparableName = new
		// Similarity.ComparableName(operationName);
		for (Operation op : operations) {
			if (((Operation) selectedItem).getName().equals(op.getName())
					&& ((Operation) selectedItem).getInputs().equals(op.getInputs())
					&& ((Operation) selectedItem).getOutputs().equals(op.getOutputs())) {

				String operationType = "Action";
				JungXMIImporter.Service service = new JungXMIImporter.Service(operationName, operationType);
				OwlService owlService;
				owlService = new OwlService(op);

				Collection<OwlService> services = new ArrayList<OwlService>(jungGraph.getVertices());
				for (OwlService s : services) {
					if (owlService.getName().getContent().equals(s.getName().getContent())) {
						if (owlService.getId() <= s.getId()) {
							owlService.setId(s.getId() + 1);
						}
					}

				}

				HashMap<Service, OwlService> map = new HashMap<Service, OwlService>();
				map.put(service, owlService);
				jungGraph.addVertex(owlService);

				System.out.println("New operation: " + owlService + " is added to the graph");
				Transformer transformer = new Transformer(jungGraph);
				try {
					// Add the io variables of the operation
					transformer.expandOperations(owlService);
					// transformer.createLinkedVariableGraph();
				} catch (Exception e) {
					Activator.log("Error while expanding new operation", e);
					e.printStackTrace();
				}
				edu.uci.ics.jung.graph.Graph<OwlService, Connector> tempGraph = new SparseMultigraph<OwlService, Connector>();
				tempGraph.addVertex(owlService);
				Transformer tempTransformer = new Transformer(tempGraph);
				try {
					// Add the io variables of the operation
					tempTransformer.expandOperations(owlService);

				} catch (Exception e) {
					Activator.log("Error while expanding new operation", e);
					e.printStackTrace();
				}
				view.addOperationInZest(owlService, tempGraph);
			}

		}

		view.setJungGraph(jungGraph);
		// this.getViewer().setInput(createGraphNodes(jungGraph));
		view.updateRightComposite(jungGraph);
		view.setLayout();
		view.setFocus();

	}
	
	class ArgumentsLabelProvider implements ILabelProvider {
		public String getText(Object element) {
			String parents = "";
			String name = ((OwlService) element).getName().toString();
			for (Object obj : ((OwlService) element).getArgument().getParent()) {
				if (obj instanceof Argument) {
					if (!parents.isEmpty()) {
						parents += ", ";
					}
					parents += ((Argument) obj).getName().toString();
				}
			}
			if (!parents.isEmpty()) {
				name += " (" + parents + ")";
			}
			return name;
		}

		public Image getImage(Object arg0) {
			return null;
		}

		public void addListener(ILabelProviderListener arg0) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object arg0, String arg1) {
			return false;
		}

		public void removeListener(ILabelProviderListener arg0) {
		}
	}

	
	/**
	 * <h1>SelectionWindowNode</h1>Displays a selection window with a list of
	 * nodes.
	 * 
	 * @param list
	 *            : the nodes to choose from
	 * @param option
	 *            : match IOs or link operation/condition/StartNode/EndNode or
	 *            show alternative operations
	 */
	public void SelectionWindowNode(ArrayList<Object> list, String option) {

		Shell shell = view.getSite().getWorkbenchWindow().getShell();
		ElementListSelectionDialog dialog;
		if ((option == "matchinput") || (option == "matchoutput")) {
			dialog = new ElementListSelectionDialog(shell, new ArgumentsLabelProvider());
		} else {
			dialog = new ElementListSelectionDialog(shell, new LabelProvider());
		}

		dialog.setElements(list.toArray());

		if (option == "link") {
			dialog.setTitle("Link to..");
			dialog.setMessage("Please choose a node");
		} else if (option == "matchinput") {
			dialog.setTitle("Match this input with..");
			dialog.setMessage("Please select an appropriate output:");
		} else if (option == "matchoutput") {
			dialog.setTitle("Match this output with..");
			dialog.setMessage("Please select an appropriate input:");
		} else if (option == "alternativeOperations") {
			dialog.setTitle("Select an alternative operation..");
			dialog.setMessage("Please select an alternative operation for replacement:");
		}

		// user pressed OK
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();

			if (option == "link") {
				for (Object selectedItem : result) {
					linkNode(selectedItem);
				}
			} else if (option == "matchinput") {
				for (Object selectedItem : result) {
					addEdge(selectedItem, option);
				}
			} else if (option == "matchoutput") {
				for (Object selectedItem : result) {
					addEdge(selectedItem, option);
				}
			} else if (option == "alternativeOperations") {
				for (Object selectedItem : result) {
					replaceOperation(selectedItem);
				}
			}

		}

	}

	/**
	 * <h1>isNodeMatchedIO</h1>
	 * 
	 * @param graphNode
	 * @return the boolean value of the <code>isMatchedIO</code> variable of an
	 *         OwlService.
	 */
	private boolean isNodeMatchedIO(GraphNode graphNode) {

		return ((OwlService) ((MyNode) graphNode.getData()).getObject()).getisMatchedIO();

	}
}
