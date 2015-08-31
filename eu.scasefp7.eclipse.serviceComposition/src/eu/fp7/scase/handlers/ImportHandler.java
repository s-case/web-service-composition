package eu.fp7.scase.handlers;

import eu.fp7.scase.servicecomposition.importer.JungXMIImporter.Connector;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;


import eu.fp7.scase.servicecomposition.tester.Algorithm.WeightReport;
import eu.fp7.scase.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import edu.uci.ics.jung.graph.Graph;
import eu.fp7.scase.servicecomposition.importer.Importer.Operation;
import eu.fp7.scase.servicecomposition.tester.Algorithm;
import eu.fp7.scase.servicecomposition.ui.ResourceFileSelectionDialog;
import eu.fp7.scase.servicecomposition.views.MyConnection;
import eu.fp7.scase.servicecomposition.views.MyNode;
import eu.fp7.scase.servicecomposition.views.ServiceCompositionView;


public class ImportHandler extends AbstractHandler {
	Graph<OwlService, Connector> graph;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ResourceFileSelectionDialog dialog = new ResourceFileSelectionDialog("Select a .sbd file", "",
				new String[] { "sbd" });
		dialog.open();
		final Object[] selections = dialog.getResult();
		if (selections == null)
			return null;
		
		try {

			graph = null;

			Runnable myRunnable = new Runnable() {

				public void run() {
					try {
						File file = (File) selections[0];
						Algorithm.init();
						final ArrayList<Operation> operations = Algorithm
								.importServices(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
										+"/"+"WS.owl");
//						final ArrayList<Operation> operations = Algorithm
//							.importServices("data/WS.owl", "data/scripts/");
						//final ArrayList<Operation> operations = Algorithm.importServices("", "data/testing_scripts/");
						final String pathToSBDFile = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
								+ file.getFullPath().toOSString();
						graph = Algorithm.transformationAlgorithm(pathToSBDFile, operations);
						
						
						
						
						
					} catch (Exception ex) {
						ex.printStackTrace();
						
					}
					
				}
			};

			
			Thread thread = new Thread(myRunnable);
			thread.run();
			BusyIndicator.showWhile(Display.getCurrent(), myRunnable);
			
			
			//SHOW REPLACEMENT REPORT
			System.out.println();
			for(WeightReport report : Algorithm.getStepReports()){
				report.getReplaceInformation().reEvaluateWeight(graph);
				report.updateWeight();
				System.out.println(report.toString());
			}
			System.out.println(Algorithm.getFinalReport(-1).toString());
			
			
			
			// open view
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ServiceCompositionView.ID);
			ServiceCompositionView view = (ServiceCompositionView) getView(ServiceCompositionView.ID);
			// view.updateGraph(graph);
			// open file with storyboard creator
			File file = (File) selections[0];
			final IFile inputFile = ResourcesPlugin
					.getWorkspace()
					.getRoot()
					.getFileForLocation(
							Path.fromOSString(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
									+ file.getFullPath().toOSString()));
			if (inputFile != null) {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IEditorPart openEditor = IDE.openEditor(page, inputFile);
			}
			view.setJungGraph(graph);
			view.addGraphInZest(graph);
			//view.getViewer().setInput(createGraphNodes(graph));
			view.updateRightComposite(graph);
			view.setFocus();
		} catch (Exception e) {

			e.printStackTrace();
			
		}
		
		return null;
	}

	public List<MyNode> createGraphNodes(Graph<OwlService, Connector> graph) {
		List<MyNode> nodes = new ArrayList<MyNode>();
		Object[] vertices = (Object[]) graph.getVertices().toArray();
		// OwlService endNode = findEndNode(vertices);
		// vertices[0] instanceof OWLService
		// show all vertices
		for (int i = 0; i < vertices.length; i++) {
			OwlService owlNode = (OwlService) vertices[i];
			MyNode node = new MyNode(owlNode.toString(), owlNode.toString(), owlNode);
			nodes.add(node);
		}
		// create all edges
		List<MyConnection> connections = new ArrayList<MyConnection>();
		Object[] edges = graph.getEdges().toArray();
		for (int i = 0; i < edges.length; i++) {
			Connector con = (Connector) edges[i];
			OwlService jungSource = (OwlService) con.getSource();
			OwlService jungDest = (OwlService) con.getTarget();
			MyNode source = null;
			MyNode dest = null;

			for (int j = 0; j < nodes.size(); j++) {
				MyNode node = nodes.get(j);
				if (node.getObject().equals(jungSource)&& ((OwlService)node.getObject()).getId()==jungSource.getId()) {
					source = node;
				} else if (node.getObject().equals(jungDest)&& ((OwlService)node.getObject()).getId()==jungDest.getId()) {
					dest = node;
				}
			}

			if ((source!=null)&& (dest!=null)){
			
			MyConnection connect = new MyConnection(source.toString() + dest.toString(), con.getCondition(), source,
					dest);
			source.getLinkedConnections().add(connect);
			connections.add(connect);

			}
		}
		for (MyConnection connection : connections) {
			connection.getSource().getConnectedTo().add(connection.getDestination());
		}
		return nodes;
	}

	public IViewPart getView(String id) {
		IViewReference viewReferences[] = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getViewReferences();
		for (int i = 0; i < viewReferences.length; i++) {
			if (id.equals(viewReferences[i].getId())) {
				return viewReferences[i].getView(false);
			}
		}
		return null;
	}
}
