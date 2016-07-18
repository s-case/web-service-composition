package eu.scasefp7.eclipse.servicecomposition.toolbar;

import java.util.List;

import edu.uci.ics.jung.graph.SparseMultigraph;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Service;
import eu.scasefp7.eclipse.servicecomposition.transformer.Similarity;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.views.MyNode;
import eu.scasefp7.eclipse.servicecomposition.views.ServiceCompositionView;

public class CreateWorkflow {

	/**
	 * <h1>createNewWorkflow</h1> Create a workflow with only Start and End
	 * nodes.
	 */
	public static void createNewWorkflow(ServiceCompositionView view) {

		edu.uci.ics.jung.graph.Graph<OwlService, Connector> jungGraph = new SparseMultigraph<OwlService, Connector>();
		Similarity.loadProperties();
		Service startNode = new Service("", "StartNode");
		Service endNode = new Service("", "EndNode");

		OwlService start = new OwlService(startNode);
		OwlService end = new OwlService(endNode);

		jungGraph.addVertex(start);
		jungGraph.addVertex(end);
		// jungGraph.addEdge(new Connector(start, end, ""), start, end);

		
		view.setJungGraph(jungGraph);
		view.addGraphInZest(jungGraph);
		
		view.updateRightComposite(jungGraph);
		view.setSavedWorkflow(false);
		view.setWorkflowFilePath("");
		view.setStoryboardFile(null);
		view.setFocus();

	}
	
}
