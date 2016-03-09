package eu.scasefp7.eclipse.servicecomposition.views;

import java.util.ArrayList;
import java.util.List;

import eu.scasefp7.eclipse.servicecomposition.transformer.Transformer.ReplaceInformation;

/**
 * @author mkoutli Represents the node of the workflow (zest diagram)
 */
public class MyNode {
	// node attributes
	private final String id;
	private String name;
	private Object object;
	private List<MyNode> connections;
	private List<MyConnection> linkedConnections;
	private List<ReplaceInformation> alternativeOperations;

	public MyNode(String id, String name, Object obj) {
		this.id = id;
		this.name = name;
		this.object = obj;
		this.connections = new ArrayList<MyNode>();
		this.linkedConnections = new ArrayList<MyConnection>();
		this.alternativeOperations = new ArrayList<ReplaceInformation>();
	}

	public List<ReplaceInformation> getAlternativeOperations() {
		return alternativeOperations;
	}

	public void setAlternativeOperations(List<ReplaceInformation> alternativeOperations) {
		this.alternativeOperations = alternativeOperations;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<MyNode> getConnectedTo() {
		return connections;
	}

	public List<MyNode> getConnections() {
		return connections;
	}

	public void setObject(Object object) {
		this.object = object;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setConnections(List<MyNode> connections) {
		this.connections = connections;
	}

	public Object getObject() {
		return object;
	}

	public List<MyConnection> getLinkedConnections() {
		return linkedConnections;
	}

	public void setLinkedConnections(List<MyConnection> linkedConnections) {
		this.linkedConnections = linkedConnections;
	}

}
