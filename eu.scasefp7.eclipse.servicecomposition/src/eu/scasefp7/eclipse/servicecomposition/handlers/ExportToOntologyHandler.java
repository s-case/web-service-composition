package eu.scasefp7.eclipse.servicecomposition.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.uci.ics.jung.graph.Graph;
import eu.scasefp7.eclipse.servicecomposition.Activator;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.ConnectToMDEOntology;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.MDEOperation;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.NonLinearCodeGenerator;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.tester.Algorithm;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.views.ServiceCompositionView;

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
		try {
			Algorithm.init();
			ArrayList<Operation> operations = Algorithm.importServices(ResourcesPlugin.getWorkspace().getRoot()
					.getLocation().toString()
					+ "/.metadata/.plugins/eu.scasefp7.servicecomposition/ontology/WS.owl");
			Graph<OwlService, Connector> jungGraph = ServiceCompositionView.readFile(file, operations);
			NonLinearCodeGenerator gGenerator = new NonLinearCodeGenerator();
			gGenerator.generateCode(jungGraph, "workflow", false, file.getProject().getName());
			MDEOperation operation = gGenerator.getOperation();
			ConnectToMDEOntology.writeToOntology(file.getProject(), operation);
		} catch (Exception e) {
			Activator.log("Error when instantiating the linked ontology using sc file", e);
		}
	}

}
