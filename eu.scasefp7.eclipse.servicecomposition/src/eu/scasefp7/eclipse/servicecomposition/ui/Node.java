package eu.scasefp7.eclipse.servicecomposition.ui;

import java.util.Vector;

import eu.scasefp7.eclipse.servicecomposition.codeInterpreter.Value;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;

public class Node {
	private String name;
	private String value;
	private OwlService service;
	private Value argument;
	private Vector<Node> subCategories;
	private Node parent;

	public Node(String name, Node parent, OwlService service, Value argument) {
		if (service != null) {
			this.name = name + " [" + service.getArgument().getType() + "]:";
			if (service.getArgument().isRequired()){
				this.name += "*";
			}
		} else if (argument != null) {
			this.name = name + " [" + argument.getType() + "]:";
			if (argument.isRequired()){
				this.name += "*";
			}
		}
		this.parent = parent;
		this.service = service;
		this.argument = argument;
		if (argument != null) {
			this.value = argument.getValue();
		} else if (service != null) {
			this.value = "";
		}

		if (parent != null)
			parent.addSubCategory(this);
	}

	public Vector<Node> getSubCategories() {
		return subCategories;
	}

	private void addSubCategory(Node subcategory) {
		if (subCategories == null)
			subCategories = new Vector<Node>();
		if (!subCategories.contains(subcategory))
			subCategories.add(subcategory);
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public OwlService getOwlService() {
		return service;
	}

	public Value getArgument() {
		return argument;
	}

	public Node getParent() {
		return parent;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
