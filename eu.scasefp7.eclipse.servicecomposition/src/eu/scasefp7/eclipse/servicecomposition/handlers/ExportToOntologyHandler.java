package eu.scasefp7.eclipse.servicecomposition.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import eu.scasefp7.eclipse.servicecomposition.codeGenerator.ConnectToMDEOntology;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.MDEOperation;

/**
 * A command handler for exporting all the instances to the linked ontology.
 * 
 * @author themis
 */
public class ExportToOntologyHandler extends ProjectAwareHandler {

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
			// Iterate over the selected files
			for (Object object : selectionList) {
				IFile file = (IFile) Platform.getAdapterManager().getAdapter(object, IFile.class);
				if (file == null) {
					if (object instanceof IAdaptable) {
						file = (IFile) ((IAdaptable) object).getAdapter(IFile.class);
					}
				}
				if (file != null) {
					instantiateOntology(file);
				}
			}
		}
		return null;
	}

	protected void instantiateOntology(IFile file) {
		// TODO: Write code to open the file and get its MDEOperation
		// Note that the file has the format org.eclipse.core.resources.IFile
		// To open it in the java.io.File format, you can call this:
		// File jFile = new File(file.getRawLocation().toPortableString());
		MDEOperation operation = null;

		// Write to the ontology
		ConnectToMDEOntology.writeToOntology(file.getProject(), operation);
	}

}
