package eu.scasefp7.eclipse.servicecomposition.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import eu.scasefp7.eclipse.servicecomposition.ui.Node;

public class MyLabelProvider extends ColumnLabelProvider implements ILabelProvider {
	public String getText(Object element) {
		return ((Node) element).getName();
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
