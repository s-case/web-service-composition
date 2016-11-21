package eu.scasefp7.eclipse.servicecomposition.toolbar;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import eu.scasefp7.eclipse.servicecomposition.Activator;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.tester.Algorithm;
import eu.scasefp7.eclipse.servicecomposition.tester.Algorithm.WeightReport;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.views.ServiceCompositionView;

public class ReloadStoryboard {
	static edu.uci.ics.jung.graph.Graph<OwlService, Connector> jungGraph;
	private static ArrayList<Operation> SCASEoperations;
	private static ArrayList<Operation> PWoperations;
	private static ArrayList<Operation> MashapeOperations;
	
	public static void reloadStoryboard(Display disp, Shell shell, ServiceCompositionView view,
	 IFile storyboardFile) {
		if (storyboardFile != null) {
			
			Job reloadSBD = new Job("Reload StoryBoard Creator file") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Transforming storyboard creator diagram to workflow of web services...",
							IProgressMonitor.UNKNOWN);

					try {
						
						jungGraph = null;
						IFile file = storyboardFile;
						// // check if ontology file exists in .metadata
						// plug-in's
						// // folder
						// ontologyCheck(shell, disp);
						Algorithm.init();
						ArrayList<Operation> operations = new ArrayList<Operation>();
						PWoperations = ServiceCompositionView.getPWOperations();
						MashapeOperations = ServiceCompositionView.getMashapeOperations();
						SCASEoperations = ServiceCompositionView.getOperations();
						view.loadOperations(disp, shell, false);
						for (Operation op : SCASEoperations){
							operations.add(op);
						}
						//Check if Mashape and PW ontologies should be loaded
						boolean usePWOperations = false;
						boolean useMashapeOperations = false;
						if (Activator.getDefault() != null) {
							usePWOperations = Activator.getDefault().getPreferenceStore()
									.getBoolean("Use PW operations");
							useMashapeOperations = Activator.getDefault().getPreferenceStore()
									.getBoolean("Use Mashape operations");
						}
						
						if (usePWOperations && !useMashapeOperations) {
							if (PWoperations == null){
								view.loadPWOperations(disp, shell, monitor);
								PWoperations = ServiceCompositionView.getPWOperations();
							}
							for (Operation op : PWoperations) {
								operations.add(op);
							}
							 
						} else if (useMashapeOperations && !usePWOperations) {
							if (MashapeOperations == null){
								view.loadMashapeOperations(disp, shell, monitor);
								MashapeOperations = ServiceCompositionView.getMashapeOperations();
							}
							for (Operation op : MashapeOperations) {
								operations.add(op);
							}

						} else if (useMashapeOperations && usePWOperations) {
							if (PWoperations == null){
								view.loadPWOperations(disp, shell, monitor);
								PWoperations = ServiceCompositionView.getPWOperations();
							}
							if (MashapeOperations == null){
								view.loadMashapeOperations(disp, shell, monitor);
								MashapeOperations = ServiceCompositionView.getMashapeOperations();
							}
							if (PWoperations != null && MashapeOperations != null) {
								for (Operation op : PWoperations) {
									operations.add(op);
								}
								for (Operation op : MashapeOperations) {
									operations.add(op);
								}
							} else {
								disp.syncExec(new Runnable() {
									public void run() {
										MessageDialog.openInformation(disp.getActiveShell(), "Try again later",
												"Please wait until loading of ProgrammableWeb and Mashape operations is finished!");
									}
								});
								monitor.done();
								return Status.CANCEL_STATUS;
							}
						}
						final String pathToSBDFile = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
								+ file.getFullPath().toOSString();
						// check if user has cancelled
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						// transform graph
						jungGraph = Algorithm.transformationAlgorithm(pathToSBDFile, operations, disp, shell);

						if (jungGraph != null) {
							// SHOW REPLACEMENT REPORT
							System.out.println();
							for (WeightReport report : Algorithm.getStepReports()) {
								report.getReplaceInformation().reEvaluateWeight(jungGraph);
								report.updateWeight();
								System.out.println(report.toString());
							}

							// If the action was replaced with an operation
							// remove
							// any
							// properties left from initial xmi.
							Collection<OwlService> services = new ArrayList<OwlService>(jungGraph.getVertices());
							boolean propertyExists = false;
							for (OwlService property : services) {
								if (property.getArgument() != null) {
									if (property.getArgument().getParent().isEmpty()) {
										propertyExists = true;
										for (OwlService operation : jungGraph.getSuccessors(property)) {
											if (operation.getOperation() != null) {
												if (operation.getOperation().getDomain() != null)
													jungGraph.removeVertex(property);
											}
										}
									}

								}
							}
							// check if user has cancelled
							if (monitor.isCanceled())
								return Status.CANCEL_STATUS;
							disp.syncExec(new Runnable() {
								@Override
								public void run() {

									try {
										IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
												.getActivePage();

										IEditorPart openEditor = IDE.openEditor(page, storyboardFile);

										view.setJungGraph(jungGraph);
										view.addGraphInZest(jungGraph);
										view.updateRightComposite(jungGraph);
										view.setSavedWorkflow(false);
										// view.setDirty(true);
										view.setFocus();

									} catch (Exception e) {
										// TODO Auto-generated catch block
										Activator.log("Error while opening the service composition view", e);
										e.printStackTrace();
									}

								}
							});
							// Check if there are still unreplaced actions in
							// the
							// graph
							boolean serviceHasOperations = false;
							// view.getViewer().setInput(createGraphNodes(graph));
							for (OwlService service : jungGraph.getVertices()) {
								if (service.getOperation() != null) {
									if (service.getOperation().getDomain() != null) {
										serviceHasOperations = true;
									} else {
										disp.syncExec(new Runnable() {
											@Override
											public void run() {
												MessageDialog.openInformation(disp.getActiveShell(), "Info",
														"No matching operation was found for action \""
																+ service.getOperation().getName()
																+ "\". Please modify the storyboard diagram or manually add an operation.");
											}
										});
									}
								}
							}

							monitor.done();
							return Status.OK_STATUS;
						} else {
							try {
								throw new Exception("Graph can not be null");
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								Activator.log("Graph is null", e1);
								e1.printStackTrace();
							}
							return Status.CANCEL_STATUS;
						}
					} catch (Exception ex) {
						Activator.log("Error while importing the .scd file", ex);
						ex.printStackTrace();
						return Status.CANCEL_STATUS;

					} finally {
						monitor.done();
					}
				}

			};
			reloadSBD.setUser(true);
			reloadSBD.schedule();
		} else {
			disp.syncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openInformation(disp.getActiveShell(), "Info",
							"Nothing to reload! You should import a storyboard file first!");
				}

			});
		}
	}

	
	
}
