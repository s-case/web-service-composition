package eu.scasefp7.eclipse.servicecomposition.views;

import java.util.Vector;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import eu.scasefp7.eclipse.servicecomposition.ui.Node;

public class MyTreeContentProvider implements ITreeContentProvider {
	public Object[] getChildren(Object parentElement) {
		Vector<Node> subcats = ((Node) parentElement).getSubCategories();
		return subcats == null ? new Object[0] : subcats.toArray();
	}

	public Object getParent(Object element) {
		return ((Node) element).getParent();
	}

	public boolean hasChildren(Object element) {
		return ((Node) element).getSubCategories() != null;
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement != null && inputElement instanceof Vector) {
			return ((Vector<Node>) inputElement).toArray();
		}
		return new Object[0];
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
