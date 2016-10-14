package eu.scasefp7.eclipse.servicecomposition.handlers;

import eu.scasefp7.eclipse.servicecomposition.Activator;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.repository.RepositoryClient;
import eu.scasefp7.eclipse.servicecomposition.tester.Algorithm;
import eu.scasefp7.eclipse.servicecomposition.tester.Algorithm.WeightReport;
import eu.scasefp7.eclipse.servicecomposition.toolbar.FillToolbar;
import eu.scasefp7.eclipse.servicecomposition.toolbar.SaveOpen;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.ui.ResourceFileSelectionDialog;
import eu.scasefp7.eclipse.servicecomposition.views.MyConnection;
import eu.scasefp7.eclipse.servicecomposition.views.MyNode;
import eu.scasefp7.eclipse.servicecomposition.views.ServiceCompositionView;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import edu.uci.ics.jung.graph.Graph;

public class ImportHandler extends AbstractHandler {
	/**
	 * the workflow
	 */
	Graph<OwlService, Connector> graph;
	/**
	 * s-case project
	 */
	IProject existingProject;
	/**
	 * the ontology operations
	 */
	private static ArrayList<Operation> operations;
	private static ArrayList<Operation> PWoperations;
	private static ArrayList<Operation> MashapeOperations;
	private static boolean updateOntology = false;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ServiceCompositionView.ID);
		} catch (PartInitException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		ServiceCompositionView view = (ServiceCompositionView) getView(ServiceCompositionView.ID);
		final Shell shell = view.getSite().getWorkbenchWindow().getShell();
		final Display disp = shell.getDisplay();

		Graph<OwlService, Connector> previousGraph = view.getJungGraph();
		if (previousGraph != null) {
			if (view.jungGraphHasOperations() && !view.getSavedWorkflow()) {
				MessageDialog saveDialog = new MessageDialog(shell, "Workflow is not saved", null,
						"This workflow is not saved. Would you like to save it before creating a new one?",
						MessageDialog.QUESTION_WITH_CANCEL, new String[] { "Yes", "No", "Cancel" }, 0);
				int result = saveDialog.open();
				System.out.println(result);
				if (result == 0) {
					IStatus status;
					try {
						status = FillToolbar.checkGraph(previousGraph, disp);

						if (status.getMessage().equalsIgnoreCase("OK")) {
							if (view.getWorkflowFilePath().isEmpty()) {
								SaveOpen.saveWorkflow(true, view.getWorkflowFilePath(), view);
							} else {
								SaveOpen.saveWorkflow(false, view.getWorkflowFilePath(), view);
							}
						}
						view.clearMatchedInputs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (result == 1) {
					view.clearMatchedInputs();
				} else if (result == 2) {
					return null;
				}
			}
		}
		importStoryboard(disp, shell, view);
		return null;

	}

	private void importStoryboard(Display disp, Shell shell, ServiceCompositionView view) {
		ResourceFileSelectionDialog dialog = new ResourceFileSelectionDialog("Select an .scd file", "",
				new String[] { "scd" });
		dialog.open();
		final Object[] selections = dialog.getResult();
		if (selections == null)
			return;

		try {

			// Runnable myRunnable = new Runnable() {

			Job ImportSBD = new Job("Import StoryBoard Creator file") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Transforming storyboard creator diagram to workflow of web services...",

					IProgressMonitor.UNKNOWN);
					PWoperations = ServiceCompositionView.getPWOperations();
					MashapeOperations = ServiceCompositionView.getMashapeOperations();
					if (PWoperations != null && MashapeOperations != null) {
						try {
							graph = null;
							File file = (File) selections[0];
							// check if ontology file exists in .metadata
							// plug-in's
							// folder
							

							final String pathToSBDFile = ResourcesPlugin.getWorkspace().getRoot().getLocation()
									.toString() + file.getFullPath().toOSString();
							// check if the user has cancelled the job before
							// transforming
							if (monitor.isCanceled())
								return Status.CANCEL_STATUS;
							boolean usePWOperations = false;
							boolean useMashapeOperations = false;
							if (Activator.getDefault() != null) {
								usePWOperations = Activator.getDefault().getPreferenceStore()
										.getBoolean("Use PW operations");
								useMashapeOperations = Activator.getDefault().getPreferenceStore()
										.getBoolean("Use Mashape operations");
							}
							if (usePWOperations) {
								graph = Algorithm.transformationAlgorithm(pathToSBDFile, PWoperations, disp, shell);
							} else if (useMashapeOperations){
								graph = Algorithm.transformationAlgorithm(pathToSBDFile, MashapeOperations, disp, shell);
							} else {
								view.loadOperations(disp, shell);
								graph = Algorithm.transformationAlgorithm(pathToSBDFile, operations, disp, shell);
								
							}

							if (graph != null) {
								// SHOW REPLACEMENT REPORT
								System.out.println();
								for (WeightReport report : Algorithm.getStepReports()) {
									report.getReplaceInformation().reEvaluateWeight(graph);
									report.updateWeight();
									System.out.println(report.toString());
								}

								// If the action was replaced with an operation
								// remove
								// any
								// properties left from initial xmi.
								Collection<OwlService> services = new ArrayList<OwlService>(graph.getVertices());
								boolean propertyExists = false;
								for (OwlService property : services) {
									if (property.getArgument() != null) {
										if (property.getArgument().getBelongsToOperation() == null) {
											propertyExists = true;
											for (OwlService operation : graph.getSuccessors(property)) {
												if (operation.getOperation() != null) {
													if (operation.getOperation().getDomain() != null)
														graph.removeVertex(property);
												}
											}
										}

									}
								}
								// check if user cancelled before showing the
								// view
								if (monitor.isCanceled())
									return Status.CANCEL_STATUS;
								disp.syncExec(new Runnable() {
									@Override
									public void run() {

										try {
											PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
													.showView(ServiceCompositionView.ID);
											ServiceCompositionView view = (ServiceCompositionView) getView(
													ServiceCompositionView.ID);
											File file = (File) selections[0];
											existingProject = file.getProject();
											if (!existingProject.exists()) {
											} else {
												view.setScaseProject(existingProject);
											}
											final IFile inputFile = ResourcesPlugin.getWorkspace().getRoot()
													.getFileForLocation(Path.fromOSString(
															ResourcesPlugin.getWorkspace().getRoot().getLocation()
																	.toString() + file.getFullPath().toOSString()));
											view.setStoryboardFile(inputFile);
											if (inputFile != null) {
												IWorkbenchPage page = PlatformUI.getWorkbench()
														.getActiveWorkbenchWindow().getActivePage();

												IEditorPart openEditor = IDE.openEditor(page, inputFile);

											}

											view.setJungGraph(graph);
											view.addGraphInZest(graph, Algorithm.getStepReports());
											view.updateRightComposite(graph);
											view.setSavedWorkflow(false);
											view.setWorkflowFilePath("");
											// view.setDirty(true);
											view.setFocus();

										} catch (Exception e) {
											// TODO Auto-generated catch block
											Activator.log("Error while opening the service composition view", e);
											e.printStackTrace();
										}

									}
								});
								// Check if there are still unreplaced actions
								// in
								// the
								// graph
								boolean serviceHasOperations = false;
								// view.getViewer().setInput(createGraphNodes(graph));
								for (OwlService service : graph.getVertices()) {
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

			};
			ImportSBD.setUser(true);
			ImportSBD.schedule();
			// Trace user action
			Activator.TRACE.trace("/debug/executeCommand", "Storyboard is imported.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Activator.log("Error while importing the .scd file", e);
			e.printStackTrace();
		}
	}

	public List<MyNode> createGraphNodes(Graph<OwlService, Connector> graph) {
		List<MyNode> nodes = new ArrayList<MyNode>();
		Object[] vertices = (Object[]) graph.getVertices().toArray();
		// OwlService endNode = findEndNode(vertices);
		// vertices[0] instanceof OWLService
		// show all vertices
		for (int i = 0; i < vertices.length; i++) {
			OwlService owlNode = (OwlService) vertices[i];
			MyNode node = new MyNode(owlNode.toString(), owlNode.toString(), owlNode);
			nodes.add(node);
		}
		// create all edges
		List<MyConnection> connections = new ArrayList<MyConnection>();
		Object[] edges = graph.getEdges().toArray();
		for (int i = 0; i < edges.length; i++) {
			Connector con = (Connector) edges[i];
			OwlService jungSource = (OwlService) con.getSource();
			OwlService jungDest = (OwlService) con.getTarget();
			MyNode source = null;
			MyNode dest = null;

			for (int j = 0; j < nodes.size(); j++) {
				MyNode node = nodes.get(j);
				if (node.getObject().equals(jungSource)
						&& ((OwlService) node.getObject()).getId() == jungSource.getId()) {
					source = node;
				} else if (node.getObject().equals(jungDest)
						&& ((OwlService) node.getObject()).getId() == jungDest.getId()) {
					dest = node;
				}
			}

			if ((source != null) && (dest != null)) {

				MyConnection connect = new MyConnection(source.toString() + dest.toString(), con.getCondition(), source,
						dest);
				source.getLinkedConnections().add(connect);
				connections.add(connect);

			}
		}
		for (MyConnection connection : connections) {
			connection.getSource().getConnectedTo().add(connection.getDestination());
		}
		return nodes;
	}

	public IViewPart getView(String id) {
		IViewReference viewReferences[] = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getViewReferences();
		for (int i = 0; i < viewReferences.length; i++) {
			if (id.equals(viewReferences[i].getId())) {
				return viewReferences[i].getView(false);
			}
		}
		return null;
	}

	public static void ontologyCheck(Shell shell, Display disp) throws IOException {
		RepositoryClient repo = new RepositoryClient();
		repo.copyOntologyToWorkspace();

		// check if a newer ontology version exists

		String serverVersion = repo.getLatestSubmissionId("WS");
		BufferedReader reader = new BufferedReader(
				new FileReader(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
						+ "/.metadata/.plugins/eu.scasefp7.servicecomposition/ontology/version.txt"));
		String localVersion = reader.readLine().replaceAll("\\D+", "");
		if (!serverVersion.toString().isEmpty() && !localVersion.toString().isEmpty()) {
			if (Integer.parseInt(serverVersion) > Integer.parseInt(localVersion)) {
				// ask user if he would like to download the new
				// version

				disp.syncExec(new Runnable() {
					public void run() {
						boolean answer = MessageDialog.openConfirm(shell,
								"A newer version of the ontology exists on server",
								"A newer version of the web services ontology exists on server. Would you like to download it?");

						if (answer) {
							setUpdateOntology(true);
						} else {
							setUpdateOntology(false);
						}
					}
				});

				if (getUpdateOntology()) {
					// OK Button selected
					try {
						String path = repo.downloadOntology("WS", serverVersion, disp);
						ServiceCompositionView.setUpdateOperations(true);
					} catch (Exception e) {
						Activator.log("Error occured while downloading the ontology", e);
						e.printStackTrace();
					}
				} else {
					ServiceCompositionView.setUpdateOperations(false);
				}

			} else {
				ServiceCompositionView.setUpdateOperations(false);
			}
		}
	}

	/**
	 * <h1>setOperations</h1>
	 * 
	 * @param operations:
	 *            the list of ontology operations
	 */
	public static void setOperations(ArrayList<Operation> operationsList) {
		operations = operationsList;
	}

	public static void setUpdateOntology(boolean update) {
		updateOntology = update;
	}

	public static boolean getUpdateOntology() {
		return updateOntology;
	}

}
