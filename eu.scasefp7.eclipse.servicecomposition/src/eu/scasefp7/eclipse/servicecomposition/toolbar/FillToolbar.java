package eu.scasefp7.eclipse.servicecomposition.toolbar;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.osgi.framework.Bundle;

import eu.scasefp7.eclipse.servicecomposition.Activator;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.views.ServiceCompositionView;
import eu.scasefp7.eclipse.servicecomposition.views.Utils;

public class FillToolbar {

	// toolbar actions
	private static Action runWorkflowAction;
	private static Action newWorkflowAction;
	private static Action displayCostAction;
	private static Action generateCodeAction;
	private static Action saveWorkflowAction;
	private static Action openWorkflowAction;
	private static Action reloadWorkflowAction;
	
	static GenerateUpload newProject;

	private static ImageDescriptor getImageDescriptor(String relativePath) {

		try {
			// return
			// ImageDescriptor.createFromURL(FileLocator.find(Platform.getBundle(Activator.PLUGIN_ID),
			// new Path("icons/imageFileName.xxx"),null);
			Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
			URL fullPathString = BundleUtility.find(bundle, relativePath);
			return ImageDescriptor.createFromURL(fullPathString);
		} catch (Exception e) {
			// should not happen
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	/**
	 * <h1>fillToolBar</h1>Add buttons to the toolbar.
	 */
	public static void fillToolBar(ServiceCompositionView view) {
		ZoomContributionViewItem toolbarZoomContributionViewItem = new ZoomContributionViewItem(view);
		IActionBars bars = view.getViewSite().getActionBars();
		final Shell shell = view.getSite().getWorkbenchWindow().getShell();
		final Display disp = shell.getDisplay();
		
		bars.getMenuManager().add(toolbarZoomContributionViewItem);
		runWorkflowAction = new Action("Run workflow") {
			public void run() {

				try {
					// clean outputs
					// cleanOutputs();
					RunWorkflow run = new RunWorkflow(view, view.getTreeViewer(), view.getColumnb(), view.getAuthParamsComposite());
					run.runWorkflow();
				} catch (Exception e) {
					Activator.log("Error while running the workflow", e);
					e.printStackTrace();
				}

			}
		};
		runWorkflowAction.setImageDescriptor(getImageDescriptor("icons/run.png"));

		newWorkflowAction = new Action("Create a new workflow") {
			public void run() {

				try {
					if (view.getSavedWorkflow()) {
						// clean outputs
						Utils.cleanOutputs(view.getOutputsComposite(), view.getJungGraph());
						CreateWorkflow.createNewWorkflow(view);
					} else {
						if (view.jungGraphHasOperations()) {
							MessageDialog dialog = new MessageDialog(shell, "Workflow is not saved", null,
									"This workflow is not saved. Would you like to save it before creating a new one?",
									MessageDialog.QUESTION_WITH_CANCEL, new String[] { "Yes", "No", "Cancel" }, 0);
							int result = dialog.open();
							System.out.println(result);
							if (result == 0) {
								IStatus status = checkGraph(view.getJungGraph(), disp);
								if (status.getMessage().equalsIgnoreCase("OK")) {
									if (view.getWorkflowFilePath().isEmpty()) {
										SaveOpen.saveWorkflow(true, view.getWorkflowFilePath(), view);
									} else {
										SaveOpen.saveWorkflow(false, view.getWorkflowFilePath(), view);
									}
								}
								// clean outputs
								Utils.cleanOutputs(view.getOutputsComposite(), view.getJungGraph());
								view.clearMatchedInputs();
								CreateWorkflow.createNewWorkflow(view);
							} else if (result == 1) {
								// clean outputs
								Utils.cleanOutputs(view.getOutputsComposite(), view.getJungGraph());
								view.clearMatchedInputs();
								CreateWorkflow.createNewWorkflow(view);
							}
						} else {
							CreateWorkflow.createNewWorkflow(view);
						}

					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		newWorkflowAction.setImageDescriptor(getImageDescriptor("icons/New File.png"));

		displayCostAction = new Action("Display total cost, trial period, licenses") {
			public void run() {

				try {
					// clean outputs
					DisplayCost.displayCost(view);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		displayCostAction.setImageDescriptor(getImageDescriptor("icons/copyrights.png"));

		generateCodeAction = new Action("Generate RESTful java project of the workflow.") {
			public void run() {

				try {
					IStatus status = checkGraph(view.getJungGraph(), disp);
					if (status.getMessage().equalsIgnoreCase("OK")) {
						newProject = new GenerateUpload(view);
						newProject.generate();
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		generateCodeAction.setImageDescriptor(getImageDescriptor("icons/java.png"));

		saveWorkflowAction = new Action("Save the workflow.") {
			public void run() {

				try {
					IStatus status = checkGraph(view.getJungGraph(), disp);
					if (status.getMessage().equalsIgnoreCase("OK")) {
						if (view.getWorkflowFilePath().isEmpty()) {
							SaveOpen.saveWorkflow(true, view.getWorkflowFilePath(), view);
						} else {
							SaveOpen.saveWorkflow(false, view.getWorkflowFilePath(), view);
						}
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		saveWorkflowAction.setImageDescriptor(getImageDescriptor("icons/save.png"));

		openWorkflowAction = new Action("Open a workflow file.") {
			public void run() {

				try {
					if (view.getSavedWorkflow()) {
						view.clearMatchedInputs();
						SaveOpen.openWorkflow(view);
					} else {
						if (view.jungGraphHasOperations()) {
							MessageDialog dialog = new MessageDialog(shell, "Workflow is not saved", null,
									"This workflow is not saved. Would you like to save it before creating a new one?",
									MessageDialog.QUESTION_WITH_CANCEL, new String[] { "Yes", "No", "Cancel" }, 0);
							int result = dialog.open();
							System.out.println(result);
							if (result == 0) {
								IStatus status = checkGraph(view.getJungGraph(), disp);
								if (status.getMessage().equalsIgnoreCase("OK")) {
									if (view.getWorkflowFilePath().isEmpty()) {
										SaveOpen.saveWorkflow(true, view.getWorkflowFilePath(), view);
									} else {
										SaveOpen.saveWorkflow(false, view.getWorkflowFilePath(), view);
									}
								}
								view.clearMatchedInputs();
								SaveOpen.openWorkflow(view);
							} else if (result == 1) {
								view.clearMatchedInputs();
								SaveOpen.openWorkflow(view);
							}
						} else {
							SaveOpen.openWorkflow(view);
						}

					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		openWorkflowAction.setImageDescriptor(getImageDescriptor("icons/open.png"));

		reloadWorkflowAction = new Action("Reload storyboard file.") {
			public void run() {

				try {

					ReloadStoryboard.reloadStoryboard(disp, shell, view, view.getStoryboardFile());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		reloadWorkflowAction.setImageDescriptor(getImageDescriptor("icons/reload_storyboard.png"));

		Action uploadOnServerAction = new Action("Upload RESTful web service on server.") {
			public void run() {

				try {

					newProject.install();

				} catch (Exception e) {

					e.printStackTrace();
				}

			}
		};
		uploadOnServerAction.setImageDescriptor(getImageDescriptor("icons/database.png"));

		IToolBarManager mgr = view.getViewSite().getActionBars().getToolBarManager();
		mgr.add(newWorkflowAction);
		mgr.add(openWorkflowAction);
		mgr.add(saveWorkflowAction);
		mgr.add(runWorkflowAction);
		mgr.add(generateCodeAction);
		mgr.add(uploadOnServerAction);
		mgr.add(displayCostAction);
		mgr.add(reloadWorkflowAction);

	}

	public static IStatus checkGraph(edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph, final Display disp)
			throws Exception {

		// Check if every node has a path to StartNode and to
		// EndNode, a condition has two output edges and the graph contains at
		// least one operation in order to allow
		// execution.

		int numberOfActions = 0;
		OwlService startingService = null;
		OwlService endingService = null;

		for (OwlService service : graph.getVertices()) {
			// detect starting service
			if (service.getType().trim().equals("StartNode")) {
				startingService = service;

			}
			// detect ending service
			if (service.getType().trim().equals("EndNode")) {
				endingService = service;

			}
		}

		if (startingService == null) {
			try {
				throw new Exception("Graph should contain a Start Node.");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			disp.syncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
							"Graph should contain a Start Node.");
				}

			});
			return Status.CANCEL_STATUS;
		}
		if (endingService == null) {
			try {
				throw new Exception("Graph should contain an End Node.");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			disp.syncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
							"Graph should contain an End Node.");
				}

			});
			return Status.CANCEL_STATUS;
		}

		for (OwlService service : graph.getVertices()) {
			// check that action has at least one input and one out put edge
			if (service.getType().contains("Action") || service.getType().contains("Condition")) {

				if (service.getType().contains("Action")) {
					numberOfActions++;
					int predecessorCount = 0;
					int successorCount = 0;
					for (OwlService predecessor : graph.getPredecessors(service)) {
						if (predecessor.getType().contains("Action") || predecessor.getType().contains("Condition")
								|| predecessor.getType().contains("StartNode")) {
							predecessorCount++;
						}
					}
					if (predecessorCount == 0) {
						final OwlService unlinked = service;
						try {
							throw new Exception(
									"\"" + unlinked.getName().toString() + "\"" + " has no input connection.");
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						disp.syncExec(new Runnable() {

							@Override
							public void run() {
								MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
										"\"" + unlinked.getName().toString() + "\"" + " has no input connection.");
							}
						});
						return Status.CANCEL_STATUS;
					}

					for (OwlService successor : graph.getSuccessors(service)) {
						if (successor.getType().contains("Action") || successor.getType().contains("Condition")
								|| successor.getType().contains("EndNode")) {
							successorCount++;
						}
					}
					if (successorCount == 0) {
						final OwlService unlinked = service;
						try {
							throw new Exception(
									"\"" + unlinked.getName().toString() + "\"" + " has no output connection.");
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						disp.syncExec(new Runnable() {

							@Override
							public void run() {
								MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
										"\"" + unlinked.getName().toString() + "\"" + " has no output connection.");
							}
						});
						return Status.CANCEL_STATUS;
					}
				}

				// check that end node has one input edge
				if (service.getType().contains("EndNode") && (graph.getInEdges(service).size() == 0)) {

					try {
						throw new Exception("End Node should have an input edge.");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					disp.syncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
									"End Node should have an input edge.");
						}
					});
					return Status.CANCEL_STATUS;
				}
				// check that start node has one output edge
				if (service.getType().contains("StartNode") && (graph.getOutEdges(service).size() == 0)) {

					try {
						throw new Exception("Start Node should have an input edge.");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					disp.syncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
									"Start Node should have an input edge.");
						}
					});
					return Status.CANCEL_STATUS;
				}
				// check that condition has one input edge
				if (service.getType().contains("Condition") && (graph.getInEdges(service).size() == 0)) {
					final OwlService unlinked = service;
					try {
						throw new Exception(
								"\"" + unlinked.getName().toString() + "\"" + " condition should have one input edge.");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					disp.syncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog.openInformation(disp.getActiveShell(), "Error occured", "\""
									+ unlinked.getName().toString() + "\"" + " condition should have an input edge.");
						}
					});
					return Status.CANCEL_STATUS;
				}
				// check that condition has two output edges
				if (service.getType().contains("Condition") && (graph.getOutEdges(service).size() < 2)) {
					final OwlService unlinked = service;
					try {
						throw new Exception("\"" + unlinked.getName().toString() + "\""
								+ " condition should have two output edges.");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					disp.syncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
									"\"" + unlinked.getName().toString() + "\""
											+ " condition should have two output edges.");
						}
					});
					return Status.CANCEL_STATUS;
				}
				// check that condition output edges have text
				if (service.getType().contains("Condition")) {
					final OwlService unlinked = service;
					for (Connector connector : graph.getOutEdges(service)) {
						if (connector.getCondition().isEmpty()) {
							try {
								throw new Exception("\"" + unlinked.getName().toString() + "\""
										+ " condition path should have a name.");
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}

							disp.syncExec(new Runnable() {
								@Override
								public void run() {
									MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
											"\"" + unlinked.getName().toString() + "\""
													+ " condition path should have a name.");
								}
							});
							return Status.CANCEL_STATUS;
						}
					}
				}

			}
		}
		if (numberOfActions == 0) {

			try {
				throw new Exception("Graph should contain at least one Operation.");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			disp.syncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
							"Graph should contain at least one Action.");
				}

			});
			return Status.CANCEL_STATUS;

		}
		return Status.OK_STATUS;
	}


}
