package eu.fp7.scase.servicecomposition.transformer;

import java.util.ArrayList;

import eu.fp7.scase.servicecomposition.importer.JungXMIImporter.Connector;
import eu.fp7.scase.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import edu.uci.ics.jung.graph.Graph;

/**
 * <h1>PathFindins</h1>
 * This class contains implementations for path-finding algorithms.
 * @author Manios Krasanakis
 */
public class PathFinding {
	private Graph<OwlService, Connector> graph;
	/**
	 * Initializes a path-finding instance for a particular graph.
	 * @param graph
	 */
	public PathFinding(Graph<OwlService, Connector> graph){
		this.graph = graph;
	}
	/**
	 * <h1>findOperationPath</h1>
	 * Detects the minimum directed path from a node to another (without taking arguments into account).
	 * @param from
	 * @param to
	 * @return an array list containing all nodes starting with <code>from</code> and ending at <code>to</code>
	 * @throws Exception if graph does not contain both edges of the path
	 */
	public ArrayList<OwlService> findOperationPath(OwlService from, OwlService to) throws Exception{
		if(!graph.containsVertex(from) || !graph.containsVertex(to))
			throw new Exception("Graph does not contain path edges");
		return findOperationPath(from, to, new ArrayList<OwlService>());
	}
	private ArrayList<OwlService> findOperationPath(OwlService from, OwlService to, ArrayList<OwlService> ignore){
		ArrayList<OwlService> path = new ArrayList<OwlService>();
		if(from!=to){
			ignore.add(to);
			int minLength = Integer.MAX_VALUE;
			ArrayList<OwlService> minPath = null;
			for(OwlService neighbor : graph.getPredecessors(to))
			if(neighbor.getArgument()==null && !ignore.contains(neighbor)){
				ArrayList<OwlService> testPath = findOperationPath(from, neighbor, new ArrayList<OwlService>(ignore));
				if(testPath.size()<minLength){
					minLength = testPath.size();
					minPath = testPath;
				}
			}
			if(minPath!=null)
				path.addAll(minPath);
		}
		path.add(to);
		return path;
	}
}
