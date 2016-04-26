package eu.scasefp7.eclipse.servicecomposition.ui;

import org.eclipse.core.resources.IContainer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class SelectScaseProjectDialog extends ContainerSelectionDialog{

	public SelectScaseProjectDialog(Shell parentShell, IContainer initialRoot, boolean allowNewContainerName,
			String message) {
		super(parentShell, initialRoot, allowNewContainerName, message);
		// TODO Auto-generated constructor stub
	}

	

}
