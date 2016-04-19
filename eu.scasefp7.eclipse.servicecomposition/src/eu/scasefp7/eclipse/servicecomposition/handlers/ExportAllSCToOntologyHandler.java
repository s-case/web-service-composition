package eu.scasefp7.eclipse.servicecomposition.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * A command handler for exporting all the instances of all files to the linked ontology.
 * 
 * @author themis
 */
public class ExportAllSCToOntologyHandler extends ExportToOntologyHandler {

	/**
	 * This function is called when the user selects the menu item. It reads the selected resource(s) and populates the
	 * linked ontology.
	 * 
	 * @param event the event containing the information about which file was selected.
	 * @return the result of the execution which must be {@code null}.
	 */
	@SuppressWarnings("unchecked")
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			List<Object> selectionList = structuredSelection.toList();
			IProject project = getProjectOfSelectionList(selectionList);
			ArrayList<IFile> files = getFilesOfProject(project, "sc");
			// Iterate over the selected files
			for (IFile file : files) {
				if (file != null) {
					instantiateOntology(file);
				}
			}
		}
		return null;
	}

}
