package eu.scasefp7.eclipse.servicecomposition.views;

import java.util.ArrayList;
import java.util.List;

public class MyNode {
	private final String id;
	private String name;
	private final Object object;
	private List<MyNode> connections;
	private List<MyConnection> linkedConnections;

	public MyNode(String id, String name, Object obj) {
		this.id = id;
		this.name = name;
		this.object = obj;
		this.connections = new ArrayList<MyNode>();
		linkedConnections=new ArrayList<MyConnection>();
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

	public void setName(String name){
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
