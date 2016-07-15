package eu.scasefp7.eclipse.servicecomposition.views;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;


import org.apache.maven.Maven;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.project.MavenProject;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.codehaus.plexus.DefaultPlexusContainer;



import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.jst.j2ee.internal.web.archive.operations.WebComponentExportDataModelProvider;
import org.eclipse.jst.j2ee.internal.web.archive.operations.WebFacetProjectCreationDataModelProvider;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetInstallDataModelProperties;
import org.eclipse.jst.j2ee.project.facet.IJ2EEModuleFacetInstallDataModelProperties;
import org.eclipse.jst.j2ee.web.datamodel.properties.IWebComponentExportDataModelProperties;
import org.eclipse.jst.j2ee.web.project.facet.IWebFacetInstallDataModelProperties;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.core.viewers.internal.IStylingGraphModelFactory;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.IContainer;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.Filter;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutItem;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.HorizontalTreeLayoutAlgorithm;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.osgi.framework.Bundle;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties.FacetDataModelMap;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.project.facet.SimpleWebFacetProjectCreationDataModelProvider;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.GraphMLWriter;
import edu.uci.ics.jung.io.graphml.EdgeMetadata;
import edu.uci.ics.jung.io.graphml.GraphMLReader2;
import edu.uci.ics.jung.io.graphml.GraphMetadata;
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata;
import edu.uci.ics.jung.io.graphml.NodeMetadata;
import eu.scasefp7.eclipse.servicecomposition.operationCaller.RAMLCaller;
import eu.scasefp7.eclipse.core.ontology.LinkedOntologyAPI;
import eu.scasefp7.eclipse.servicecomposition.Activator;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.CallRestfulServiceCode;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.CallWSDLServiceCode;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.ConnectToMDEOntology;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.FunctionCodeNode;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.MDERepresentation;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.NonLinearCodeGenerator;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.RestfulCodeGenerator;
import eu.scasefp7.eclipse.servicecomposition.codeInterpreter.UserInput;
import eu.scasefp7.eclipse.servicecomposition.codeInterpreter.Value;
import eu.scasefp7.eclipse.servicecomposition.handlers.ImportHandler;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Service;
import eu.scasefp7.eclipse.servicecomposition.operationCaller.WSDLCaller;
import eu.scasefp7.eclipse.servicecomposition.repository.ApplicationDomain;
import eu.scasefp7.eclipse.servicecomposition.repository.RepositoryClient;
import eu.scasefp7.eclipse.servicecomposition.repository.WSOntology;
import eu.scasefp7.eclipse.servicecomposition.tester.Algorithm;
import eu.scasefp7.eclipse.servicecomposition.tester.Algorithm.WeightReport;
import eu.scasefp7.eclipse.servicecomposition.tester.Algorithm.costReport;
import eu.scasefp7.eclipse.servicecomposition.tester.Algorithm.licenseReport;
import eu.scasefp7.eclipse.servicecomposition.tester.Algorithm.trialReport;
import eu.scasefp7.eclipse.servicecomposition.toolbar.GenerateUpload;
import eu.scasefp7.eclipse.servicecomposition.toolbar.RunWorkflow;
import eu.scasefp7.eclipse.servicecomposition.toolbar.SaveOpen;
import eu.scasefp7.eclipse.servicecomposition.transformer.Matcher;
import eu.scasefp7.eclipse.servicecomposition.transformer.PathFinding;
import eu.scasefp7.eclipse.servicecomposition.transformer.Similarity;
import eu.scasefp7.eclipse.servicecomposition.transformer.Transformer;
import eu.scasefp7.eclipse.servicecomposition.transformer.Transformer.ReplaceInformation;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.ui.MatchOutputDialog;
import eu.scasefp7.eclipse.servicecomposition.ui.MyTextCellEditor;
import eu.scasefp7.eclipse.servicecomposition.ui.MyTitleAreaDialog;
import eu.scasefp7.eclipse.servicecomposition.ui.Node;
import eu.scasefp7.eclipse.servicecomposition.ui.RenameConditionDialog;
import eu.scasefp7.eclipse.servicecomposition.ui.RenameEdgeConditionDialog;
import eu.scasefp7.eclipse.servicecomposition.ui.ResourceFileSelectionDialog;
import eu.scasefp7.eclipse.servicecomposition.ui.SafeSaveDialog;
import eu.scasefp7.eclipse.servicecomposition.ui.SelectScaseProjectDialog;
import eu.scasefp7.eclipse.servicecomposition.ui.TreeDialog;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class ServiceCompositionView extends ViewPart implements IZoomableWorkbenchPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "eu.scasefp7.eclipse.servicecomposition.views.ServiceCompositionView";

	
	// the graph that appears in the view
	private edu.uci.ics.jung.graph.Graph<OwlService, Connector> jungGraph;
	
	// the storyboard file
	private IFile storyboardFile;
	// toolbar actions
	private Action runWorkflowAction;
	private Action newWorkflowAction;
	private Action displayCostAction;
	private Action generateCodeAction;
	private Action saveWorkflowAction;
	private Action openWorkflowAction;
	private Action reloadWorkflowAction;
	// composite in ui
	private ScrolledComposite sc;
	private Composite rightComposite;
	private SashForm sashForm;
	private Tree inputsComposite;
	private Tree outputsComposite;
	private TreeViewerColumn column1;
	private TreeViewerColumn column2;
	private TreeViewerColumn columna;
	private TreeViewerColumn columnb;
	private TreeViewer treeViewer;
	private TreeViewer inputsTreeViewer;
	private Composite authParamsComposite;
	private GraphViewer viewer;
	
	// maven build
	public static MavenExecutionRequestPopulator populator;
	public static DefaultPlexusContainer container;
	public static Maven maven;
	static public List<MavenProject> buildedProjects = new ArrayList<MavenProject>();
	static public SettingsBuilder settingsBuilder;
	
	// the s-case project
	private IProject scaseProject;
	// if the workflow is saved
	private boolean savedWorkflow = false;
	GenerateUpload newProject;
	
	// zest graph connection and node
	private GraphConnection selectedGraphEdge;
	private GraphNode selectedGraphNode;
	private Point point;
	// input composite selection
	private ISelection inputSelection;
	private String workflowFilePath = "";
	// property to denote file is not saved
	private boolean isDirty = false;
	
	// the imported operations from the ontology
	private static ArrayList<Operation> operations;
	// flag for updating operations
	private static boolean updateOperations = true;
	private static boolean updateYouRest = false;

	public void createPartControl(Composite parent) {

		ServiceCompositionView view = this;
		sashForm = new SashForm(parent, SWT.HORIZONTAL);
		viewer = new GraphViewer(sashForm, SWT.BORDER);

		rightComposite = new Composite(sashForm, SWT.NONE);

		sashForm.setWeights(new int[] { 4, 1 });
		viewer.setContentProvider(new ZestNodeContentProvider());
		viewer.setLabelProvider(new ZestLabelProvider());
		viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
		NodeModelContentProvider model = new NodeModelContentProvider();
		viewer.setInput(model.getNodes());
		LayoutAlgorithm layout = setLayout();

		viewer.setLayoutAlgorithm(layout, true);
		viewer.applyLayout();
		fillToolBar();

		createNewWorkflow();
		setSavedWorkflow(false);

		// RepositoryClient repo = new RepositoryClient();
		// String path = repo.downloadOntology("WS");

		final Graph graph = viewer.getGraphControl();
		graph.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				for (int i = 0; i < graph.getNodes().size(); i++) {
					GraphNode graphNode = (GraphNode) graph.getNodes().get(i);
					graphNode.unhighlight();
				}
				if (e.item instanceof GraphNode) {
					graph.setSelection(new GraphItem[] { (GraphNode) e.item });
				} else {
					graph.setSelection(new GraphItem[] { (GraphConnection) e.item });
				}

			}

		});
		// add context menu on nodes
		graph.addMenuDetectListener(new MenuDetectListener() {

			@Override
			public void menuDetected(MenuDetectEvent e) {
				selectedGraphEdge = null;
				point = graph.toControl(e.x, e.y);
				IFigure fig = graph.getViewport().findFigureAt(point.x, point.y);
				selectedGraphNode = null;
				for (int i = 0; i < graph.getNodes().size(); i++) {
					GraphNode graphNode = (GraphNode) graph.getNodes().get(i);
					if (graphNode.isSelected()) {
						selectedGraphNode = graphNode;
						break;
					}
				}

				// check if edge is selected

				for (int i = 0; i < graph.getConnections().size(); i++) {
					GraphConnection graphConnection = (GraphConnection) graph.getConnections().get(i);
					if (graphConnection.isHighlighted()) {
						selectedGraphEdge = graphConnection;
						break;
					}
				}
				if (fig != null && selectedGraphEdge != null) {
					GraphNode source = selectedGraphEdge.getSource();
					GraphNode dest = selectedGraphEdge.getDestination();
					Menu menu = new Menu(getDisplay().getActiveShell(), SWT.POP_UP);
					MenuItem item = new MenuItem(menu, SWT.NONE);
					item.setText("Remove this connection");
					MenuItem item2 = new MenuItem(menu, SWT.NONE);
					item2.setText("Rename this connection");

					// Check the kind of the nodes that the edge is connecting.
					// Remove is applied to any edge that connects nodes that
					// none of them is a property.

					
					if ((((OwlService) ((MyNode) source.getData()).getObject()).getArgument() == null
							&& ((OwlService) ((MyNode) dest.getData()).getObject()).getArgument() == null)
							|| (((OwlService) ((MyNode) source.getData()).getObject()).getisMatchedIO()
									&& ((OwlService) ((MyNode) dest.getData()).getObject()).getisMatchedIO())) {
						item.setEnabled(true);
					} else
						item.setEnabled(false);

					if (((OwlService) ((MyNode) source.getData()).getObject()).getType().equals("Condition")) {
						item2.setEnabled(true);
					} else
						item2.setEnabled(false);

					menu.setVisible(true);

					// Remove selected edge.
					item.addListener(SWT.Selection, new Listeners(selectedGraphEdge, selectedGraphNode, view, "removeEdge"));

					// Rename condition
					item2.addListener(SWT.Selection, new Listeners(selectedGraphEdge, selectedGraphNode, view, "renameConditionEdge"));

				} else if (fig != null && selectedGraphNode != null) {
					if (((OwlService) ((MyNode) selectedGraphNode.getData()).getObject()).getType()
							.equals("Property")) {
						Menu menu = new Menu(getDisplay().getActiveShell(), SWT.POP_UP);
						MenuItem item = new MenuItem(menu, SWT.NONE);
						item.setText("Match this i/o with.. ");
						Argument arg = (Argument) ((OwlService) ((MyNode) selectedGraphNode.getData()).getObject())
								.getContent();
						boolean isMemberOfArray = false;
						for (int i = 0; i < arg.getParent().size(); i++) {
							if (arg.getParent().get(i) instanceof Argument) {
								if (((Argument) arg.getParent().get(i)).isArray()) {
									isMemberOfArray = true;
								}
							}
						}
						if (arg.isNative()) {
							// if (arg.isNative() && !arg.isArray() &&
							// !isMemberOfArray) {
							item.setEnabled(true);
						} else {
							item.setEnabled(false);
						}
						menu.setVisible(true);

						// Match i/o with..
						item.addListener(SWT.Selection, new Listeners(selectedGraphEdge, selectedGraphNode, view, "matchIO"));


					} else if (((OwlService) ((MyNode) selectedGraphNode.getData()).getObject()).getType()
							.equals("Condition")) {
						Menu menu = new Menu(getDisplay().getActiveShell(), SWT.POP_UP);
						MenuItem item = new MenuItem(menu, SWT.NONE);
						item.setText("Link this condition to..");
						MenuItem item2 = new MenuItem(menu, SWT.NONE);
						item2.setText("Remove condition");
						MenuItem item3 = new MenuItem(menu, SWT.NONE);
						item3.setText("Rename condition");

						if (((MyNode) selectedGraphNode.getData()).getLinkedConnections().size() < 2) {
							item.setEnabled(true);

						} else {
							item.setEnabled(false);
						}

						menu.setVisible(true);

						// Link condition with..
						item.addListener(SWT.Selection, new Listeners(selectedGraphEdge, selectedGraphNode, view, "linkCondition"));

						// Remove condition
						item2.addListener(SWT.Selection, new Listeners(selectedGraphEdge, selectedGraphNode, view, "removeConditionNode"));
						// rename condition
						item3.addListener(SWT.Selection, new Listeners(selectedGraphEdge, selectedGraphNode, view, "renameConditionNode"));

					} else if (((OwlService) ((MyNode) selectedGraphNode.getData()).getObject()).getType()
							.equals("StartNode")) {
						Menu menu = new Menu(getDisplay().getActiveShell(), SWT.POP_UP);
						MenuItem item = new MenuItem(menu, SWT.NONE);
						item.setText("Link StartNode with.. ");
						if (((MyNode) selectedGraphNode.getData()).getLinkedConnections().size() == 0) {
							item.setEnabled(true);

						} else {
							item.setEnabled(false);
						}
						menu.setVisible(true);

						// Link Start Node
						item.addListener(SWT.Selection, new Listeners(selectedGraphEdge, selectedGraphNode, view, "linkStartNode"));

					} else if (((OwlService) ((MyNode) selectedGraphNode.getData()).getObject()).getType()
							.equals("Action")) {
						Menu menu = new Menu(getDisplay().getActiveShell(), SWT.POP_UP);
						MenuItem item = new MenuItem(menu, SWT.NONE);
						item.setText("Remove operation "
								+ ((OwlService) ((MyNode) selectedGraphNode.getData()).getObject()).getName());
						item.setEnabled(true);
						MenuItem item1 = new MenuItem(menu, SWT.NONE);
						item1.setText("See alternative operations");
						item1.setEnabled(true);
						MenuItem item2 = new MenuItem(menu, SWT.NONE);
						item2.setText("Link this operation to..");

						int sizeOfProperties = 0;
						if (((MyNode) selectedGraphNode.getData()).getLinkedConnections().size() != 0) {
							for (int i = 0; i < ((MyNode) selectedGraphNode.getData()).getLinkedConnections()
									.size(); i++) {
								MyConnection con = ((MyNode) selectedGraphNode.getData()).getLinkedConnections().get(i);
								MyNode tmpNode = con.getDestination();
								if (((OwlService) tmpNode.getObject()).getType().equals("Property")) {
									sizeOfProperties = sizeOfProperties + 1;
								}
							}
							if (((MyNode) selectedGraphNode.getData()).getLinkedConnections()
									.size() == sizeOfProperties) {

								item2.setEnabled(true);
							} else {
								item2.setEnabled(false);
							}
						} else {
							item2.setEnabled(true);
						}

						// item2.setEnabled(true);
						menu.setVisible(true);

						// Remove selected edge.
						item.addListener(SWT.Selection, new Listeners(selectedGraphEdge, selectedGraphNode, view, "removeOperationNode"));
						// See alternative operations
						item1.addListener(SWT.Selection, new Listeners(selectedGraphEdge, selectedGraphNode, view, "showAlternatives"));


						// Link the operation with..
						item2.addListener(SWT.Selection, new Listeners(selectedGraphEdge, selectedGraphNode, view, "linkOperation"));


					}

				} else {
					Menu menu = new Menu(getDisplay().getActiveShell(), SWT.POP_UP);
					MenuItem item = new MenuItem(menu, SWT.NONE);
					item.setText("Add operation..");
					MenuItem item2 = new MenuItem(menu, SWT.NONE);
					item2.setText("Add condition..");
					menu.setVisible(true);

					// Add new operation.
					item.addListener(SWT.Selection, new Listeners(selectedGraphEdge, selectedGraphNode, view, "addNewOperation"));


					// Add new condition.
					item2.addListener(SWT.Selection, new Listeners(selectedGraphEdge, selectedGraphNode, view, "addNewCondition"));
				}

			}
		});
	}


	

	/**
	 * <h1>SelectionWindowOp</h1>Displays the selection window with the list of
	 * the repository operations.
	 * 
	 * @param shell
	 * @param dialog
	 * @param operations
	 * @param list
	 */
	public void SelectionWindowOp(Shell shell, ElementListSelectionDialog dialog, final ArrayList<Operation> operations,
			ArrayList<String> list) {

		dialog.setElements(operations.toArray());

		dialog.setTitle("S-CASE Operations");
		dialog.setMessage("Please choose one operation");

		// user pressed OK
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();

			for (Object selectedItem : result) {
				addNode(selectedItem, operations);
			}

		}

	}

	
	/**
	 * <h1>addNode</h1>Add the selected operation (chosen from the selection
	 * window) to the graph with its IOs.
	 * 
	 * @param selectedItem
	 * @param operations
	 */
	protected void addNode(Object selectedItem, ArrayList<Operation> operations) {
		String operationName = selectedItem.toString();
		// Similarity.ComparableName operationComparableName = new
		// Similarity.ComparableName(operationName);
		for (Operation op : operations) {
			if (((Operation) selectedItem).getName().equals(op.getName())
					&& ((Operation) selectedItem).getInputs().equals(op.getInputs())
					&& ((Operation) selectedItem).getOutputs().equals(op.getOutputs())) {

				String operationType = "Action";
				JungXMIImporter.Service service = new JungXMIImporter.Service(operationName, operationType);
				OwlService owlService;
				owlService = new OwlService(op);

				Collection<OwlService> services = new ArrayList<OwlService>(jungGraph.getVertices());
				for (OwlService s : services) {
					if (owlService.getName().getContent().equals(s.getName().getContent())) {
						if (owlService.getId() <= s.getId()) {
							owlService.setId(s.getId() + 1);
						}
					}

				}

				HashMap<Service, OwlService> map = new HashMap<Service, OwlService>();
				map.put(service, owlService);
				jungGraph.addVertex(owlService);

				System.out.println("New operation: " + owlService + " is added to the graph");
				Transformer transformer = new Transformer(jungGraph);
				try {
					// Add the io variables of the operation
					transformer.expandOperations(owlService);
					// transformer.createLinkedVariableGraph();
				} catch (Exception e) {
					Activator.log("Error while expanding new operation", e);
					e.printStackTrace();
				}
				edu.uci.ics.jung.graph.Graph<OwlService, Connector> tempGraph = new SparseMultigraph<OwlService, Connector>();
				tempGraph.addVertex(owlService);
				Transformer tempTransformer = new Transformer(tempGraph);
				try {
					// Add the io variables of the operation
					tempTransformer.expandOperations(owlService);

				} catch (Exception e) {
					Activator.log("Error while expanding new operation", e);
					e.printStackTrace();
				}
				addOperationInZest(owlService, tempGraph);
			}

		}

		this.setJungGraph(jungGraph);
		// this.getViewer().setInput(createGraphNodes(jungGraph));
		this.updateRightComposite(jungGraph);
		this.setLayout();
		this.setFocus();

	}


	

	public List<MyNode> createGraphNodes(edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph) {
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

			MyConnection connect = new MyConnection(source.toString() + dest.toString(), con.getCondition(), source,
					dest);
			source.getLinkedConnections().add(connect);
			connections.add(connect);

		}
		for (MyConnection connection : connections) {
			connection.getSource().getConnectedTo().add(connection.getDestination());
		}
		return nodes;
	}

	public static Display getDisplay() {
		Display display = Display.getCurrent();
		// may be null if outside the UI thread
		if (display == null)
			display = Display.getDefault();
		return display;
	}

	@Override
	public AbstractZoomableViewer getZoomableViewer() {
		return viewer;
	}

	private LayoutAlgorithm setLayout() {
		LayoutAlgorithm layout;
		// layout = new
		// SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		// layout = new
		// TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		// layout = new
		// GridLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		layout = new HorizontalTreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		// layout = new
		// RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		layout = new MyCustomLayout(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		return layout;

	}

	private ImageDescriptor getImageDescriptor(String relativePath) {

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
	private void fillToolBar() {
		ZoomContributionViewItem toolbarZoomContributionViewItem = new ZoomContributionViewItem(this);
		IActionBars bars = getViewSite().getActionBars();
		final Shell shell = this.getSite().getWorkbenchWindow().getShell();
		final Display disp = shell.getDisplay();
		final ServiceCompositionView SCview = this;
		bars.getMenuManager().add(toolbarZoomContributionViewItem);
		runWorkflowAction = new Action("Run workflow") {
			public void run() {

				try {
					// clean outputs
					// cleanOutputs();
					RunWorkflow run = new RunWorkflow(SCview, treeViewer, columnb, authParamsComposite);
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
					if (getSavedWorkflow()) {
						// clean outputs
						Utils.cleanOutputs(outputsComposite, jungGraph);
						createNewWorkflow();
					} else {
						if (jungGraphHasOperations()) {
							MessageDialog dialog = new MessageDialog(shell, "Workflow is not saved", null,
									"This workflow is not saved. Would you like to save it before creating a new one?",
									MessageDialog.QUESTION_WITH_CANCEL, new String[] { "Yes", "No", "Cancel" }, 0);
							int result = dialog.open();
							System.out.println(result);
							if (result == 0) {
								IStatus status = checkGraph(jungGraph, disp);
								if (status.getMessage().equalsIgnoreCase("OK")) {
									if (workflowFilePath.isEmpty()) {
										SaveOpen.saveWorkflow(true, workflowFilePath, SCview);
									} else {
										SaveOpen.saveWorkflow(false, workflowFilePath, SCview);
									}
								}
								// clean outputs
								Utils.cleanOutputs(outputsComposite, jungGraph);
								clearMatchedInputs();
								createNewWorkflow();
							} else if (result == 1) {
								// clean outputs
								Utils.cleanOutputs(outputsComposite, jungGraph);
								clearMatchedInputs();
								createNewWorkflow();
							}
						} else {
							createNewWorkflow();
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
					displayCost();

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
					IStatus status = checkGraph(jungGraph, disp);
					if (status.getMessage().equalsIgnoreCase("OK")) {
						newProject = new GenerateUpload(SCview);
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
					IStatus status = checkGraph(jungGraph, disp);
					if (status.getMessage().equalsIgnoreCase("OK")) {
						if (workflowFilePath.isEmpty()) {
							SaveOpen.saveWorkflow(true, workflowFilePath, SCview);
						} else {
							SaveOpen.saveWorkflow(false, workflowFilePath, SCview);
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
					if (getSavedWorkflow()) {
						SaveOpen.openWorkflow(SCview);
					} else {
						if (jungGraphHasOperations()) {
							MessageDialog dialog = new MessageDialog(shell, "Workflow is not saved", null,
									"This workflow is not saved. Would you like to save it before creating a new one?",
									MessageDialog.QUESTION_WITH_CANCEL, new String[] { "Yes", "No", "Cancel" }, 0);
							int result = dialog.open();
							System.out.println(result);
							if (result == 0) {
								IStatus status = checkGraph(jungGraph, disp);
								if (status.getMessage().equalsIgnoreCase("OK")) {
									if (workflowFilePath.isEmpty()) {
										SaveOpen.saveWorkflow(true, workflowFilePath, SCview);
									} else {
										SaveOpen.saveWorkflow(false, workflowFilePath, SCview);
									}
								}
								clearMatchedInputs();
								SaveOpen.openWorkflow(SCview);
							} else if (result == 1) {
								clearMatchedInputs();
								SaveOpen.openWorkflow(SCview);
							}
						} else {
							SaveOpen.openWorkflow(SCview);
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

					reloadStoryboard(disp, shell);
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

		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		mgr.add(newWorkflowAction);
		mgr.add(openWorkflowAction);
		mgr.add(saveWorkflowAction);
		mgr.add(runWorkflowAction);
		// mgr.add(cancelWorkflowAction);
		mgr.add(generateCodeAction);
		mgr.add(uploadOnServerAction);
		mgr.add(displayCostAction);
		mgr.add(reloadWorkflowAction);
		// mgr.add(DownloadOntologyAction);

	}

	public void clearMatchedInputs() {
		ArrayList<Argument> outputs = new ArrayList<Argument>();
		for (OwlService op : jungGraph.getVertices()) {
			if (op.getOperation() != null) {
				for (Argument arg : op.getOperation().getOutputs()) {
					Utils.addArguments(arg, outputs);
				}
			}
		}

		for (Argument out : outputs) {
			ArrayList<Argument> list = out.getMatchedInputs();
			for (Iterator<Argument> iterator = list.iterator(); iterator.hasNext();) {
				Argument v = iterator.next();
				if (v != null) {
					iterator.remove();
				}
			}
		}

	}

	public boolean jungGraphHasOperations() {
		boolean hasOperations = false;
		for (OwlService service : jungGraph.getVertices()) {
			if (service.getType().equals("Action") || service.getType().equals("Condition")) {
				hasOperations = true;
				break;
			}
		}
		return hasOperations;
	}

	/**
	 * <h1>displayCost</h1>Method for displaying a dialog to the user with
	 * information concerning the total cost, trial period and licenses required
	 * for the workflow.
	 */
	private void displayCost() {
		costReport costPerUsage = new costReport();
		costReport costPerMonth = new costReport();
		costReport costPerYear = new costReport();
		costReport costUnlimited = new costReport();
		trialReport workflowTrialPeriod = new trialReport();
		licenseReport workflowLicense = new licenseReport();

		for (OwlService service : jungGraph.getVertices()) {
			if (service.getOperation() != null) {

				// Calculate total cost

				String currency = ((Operation) service.getContent()).getAccessInfo().getCommercialCostSchema()
						.getCommercialCostCurrency();
				String chargeType = ((Operation) service.getContent()).getAccessInfo().getCommercialCostSchema()
						.getCostPaymentChargeType();

				switch (chargeType) {
				case "per usage":
					costPerUsage.calculateCost(currency, service);
					break;
				case "per month":
					costPerMonth.calculateCost(currency, service);
					break;
				case "per year":
					costPerYear.calculateCost(currency, service);
					break;
				case "unlimited":
					costUnlimited.calculateCost(currency, service);
					break;
				default:
					break;
				}

				// trial period
				int durationInUsages = ((Operation) service.getContent()).getAccessInfo().getCommercialCostSchema()
						.getTrialSchema().getDurationInUsages();
				int durationInDays = ((Operation) service.getContent()).getAccessInfo().getCommercialCostSchema()
						.getTrialSchema().getDurationInDays();
				workflowTrialPeriod.setTrialReport(durationInDays, durationInUsages);

				// License
				String licenseName = ((Operation) service.getContent()).getAccessInfo().getLicense().getLicenseName();
				workflowLicense.setLicenseReport(licenseName);
			}
		}

		// Calculate and print total cost
		String perUsage = costPerUsage.calculateWorkflowCost(" per usage");
		String perMonth = costPerMonth.calculateWorkflowCost(" per month");
		if (perMonth != "") {
			perMonth = " + " + perMonth;
		}
		String perYear = costPerYear.calculateWorkflowCost(" per year");
		if (perYear != "") {
			perYear = " + " + perYear;
		}
		String unlimited = costUnlimited.calculateWorkflowCost(" unlimited");
		if (unlimited != "") {
			unlimited = " + " + unlimited;
		}
		String ret = "Total workflow cost: " + perUsage + perMonth + perYear + unlimited;
		System.out.println(ret);

		// Calculate and print trial period
		String ret2 = "Total trial period: ";
		int minDays = workflowTrialPeriod.findDurationInDays();
		int minUsages = workflowTrialPeriod.findDurationInUsages();
		if (minDays != Integer.MAX_VALUE && minUsages != Integer.MAX_VALUE) {
			ret2 = ret2 + minDays + " days or " + minUsages + " usages.";
		} else if (minUsages != Integer.MAX_VALUE) {
			ret2 = ret2 + minUsages + " usages.";
		} else if (minDays != Integer.MAX_VALUE) {
			ret2 = ret2 + minDays + " days";
		} else {
			ret2 = ret2 + " unlimited.";
		}
		ret = ret + "\n" + ret2;
		System.out.println(ret2);

		// Total Licenses
		List<String> licenseNames = workflowLicense.getLicenseReport();
		if (!licenseNames.isEmpty()) {
			String ret3 = "Licenses: ";
			for (String licenceName : licenseNames) {
				ret3 = ret3 + licenceName + " ";
			}
			System.out.println(ret3);
			ret = ret + "\n" + ret3;
		}

		final String message = ret;
		final Shell shell = this.getSite().getWorkbenchWindow().getShell();
		final Display disp = shell.getDisplay();
		disp.syncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openInformation(disp.getActiveShell(), "Cost, Trial, License Information.", message);
			}
		});
	}

	@Override
	public void setFocus() {

		viewer.getControl().setFocus();

	}

	public GraphViewer getViewer() {
		return viewer;
	}

	public void setViewer(GraphViewer viewer) {
		this.viewer = viewer;
	}

	
	/**
	 * <h1>createNewWorkflow</h1> Create a workflow with only Start and End
	 * nodes.
	 */
	private void createNewWorkflow() {

		jungGraph = new SparseMultigraph<OwlService, Connector>();
		Similarity.loadProperties();
		Service startNode = new Service("", "StartNode");
		Service endNode = new Service("", "EndNode");

		OwlService start = new OwlService(startNode);
		OwlService end = new OwlService(endNode);

		jungGraph.addVertex(start);
		jungGraph.addVertex(end);
		// jungGraph.addEdge(new Connector(start, end, ""), start, end);

		List<MyNode> nodes = createGraphNodes(jungGraph);
		this.setJungGraph(jungGraph);
		this.addGraphInZest(jungGraph);
		// this.getViewer().setInput(createGraphNodes(jungGraph));
		this.updateRightComposite(jungGraph);
		this.setSavedWorkflow(false);
		this.setWorkflowFilePath("");
		this.setStoryboardFile(null);
		this.setFocus();

	}

	public void updateRightComposite(edu.uci.ics.jung.graph.Graph jungGraph) {

		final Display display = Display.getCurrent();
		final Graph graph = viewer.getGraphControl();
		if (sc != null) {
			sc.dispose();
		}
		// Create the ScrolledComposite to scroll horizontally and vertically
		sc = new ScrolledComposite(sashForm, SWT.H_SCROLL | SWT.V_SCROLL);

		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		rightComposite.dispose();

		rightComposite = new Composite(sc, SWT.FILL);

		sc.setContent(rightComposite);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(300, 600);

		// runWorkflowAction.setEnabled(false);

		Listener inputListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				TreeItem treeItem = (TreeItem) event.item;
				final TreeColumn[] treeColumns = treeItem.getParent().getColumns();
				// final Display display = Display.getCurrent();
				display.asyncExec(new Runnable() {

					@Override
					public void run() {
						for (TreeColumn treeColumn : treeColumns) {
							treeColumn.pack();
						}
					}
				});

			}
		};
		// create inputs composite

		rightComposite.setLayout(new GridLayout());

		Composite inputsLabelComposite = new Composite(rightComposite, SWT.FILL);
		inputsLabelComposite.setLayout(new GridLayout(1, false));
		Label label1 = new Label(inputsLabelComposite, SWT.NONE);
		label1.setText("Workflow Inputs:");
		label1.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label1.setFont(JFaceResources.getFontRegistry().getBold(""));
		inputsComposite = new Tree(rightComposite, SWT.BORDER | SWT.FILL | SWT.MULTI);
		inputsComposite.setLayout(new GridLayout(2, false));
		inputsTreeViewer = new TreeViewer(inputsComposite);
		TreeViewerEditor.create(inputsTreeViewer, new ColumnViewerEditorActivationStrategy(inputsTreeViewer) {
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION;
			}
		}, ColumnViewerEditor.TABBING_HORIZONTAL);

		columna = new TreeViewerColumn(inputsTreeViewer, SWT.NONE);
		columna.getColumn().setWidth(200);
		columna.getColumn().setText("Columna");
		columna.getColumn().setResizable(true);
		// column1.getColumn().pack();
		// columnz = new TreeViewerColumn(inputsTreeViewer, SWT.NONE);
		// columnz.getColumn().setWidth(10);
		// columnz.getColumn().setText("Columnz");
		// columnz.getColumn().setResizable(true);
		columnb = new TreeViewerColumn(inputsTreeViewer, SWT.NONE);
		columnb.getColumn().setText("Columnb");
		columnb.getColumn().setWidth(300);
		columnb.getColumn().setResizable(true);

		Vector<Node> InputNodes = new Vector<Node>();
		// column2.getColumn().pack();

		// get matched io
		Object[] vertices1 = (Object[]) jungGraph.getVertices().toArray();
		ArrayList<OwlService> matchedNodes = new ArrayList<OwlService>();
		for (int i = 0; i < vertices1.length; i++) {
			final OwlService node = (OwlService) vertices1[i];

			if (node.getisMatchedIO()) {

				matchedNodes.add(node);
			}
		}

		// get all inputs

		Object[] vertices = (Object[]) jungGraph.getVertices().toArray();
		for (int i = 0; i < vertices.length; i++) {
			final OwlService node = (OwlService) vertices[i];
			if (node.getType().contains("Action")) {
				Node n = new Node(node.getName().toString(), null, node, null);
				Collection<OwlService> predecessors = (Collection<OwlService>) jungGraph.getPredecessors(node);
				for (OwlService predecessor : predecessors) {
					if (predecessor.getType().contains("Property")) {
						showInputs(predecessor, n, InputNodes, jungGraph, matchedNodes);

					}
				}
				InputNodes.add(n);
			}
		}

		// columnz.setLabelProvider(createColumnZLabelProvider(InputNodes));

		// inputsTreeViewer.getTree().setLayoutData(new
		// GridData(GridData.FILL_BOTH));
		inputsComposite.setSize(300, 200);
		inputsTreeViewer.setContentProvider(new MyTreeContentProvider());

		columna.setLabelProvider(new MyLabelProvider());
		columnb.setLabelProvider(createColumnLabelProvider());

		final TextCellEditor cellEditor = new MyTextCellEditor(inputsTreeViewer.getTree());
		columnb.setEditingSupport(new EditingSupport(inputsTreeViewer) {

			@Override
			protected void setValue(Object element, Object value) {
				if (((Node) element).getOwlService().getArgument() != null
						&& ((Node) element).getOwlService().getArgument().getSubtypes().isEmpty())
					((Node) element).setValue(value.toString());

				getViewer().update(element, null);
			}

			@Override
			protected Object getValue(Object element) {
				return ((Node) element).getValue();
			}

			@Override
			protected TextCellEditor getCellEditor(Object element) {
				return cellEditor;
			}

			@Override
			protected boolean canEdit(Object element) {
				if (((Node) element).getOwlService().getArgument() != null
						&& ((Node) element).getOwlService().getArgument().getSubtypes().isEmpty()
						&& !((Node) element).getOwlService().getisMatchedIO())
					return true;
				else
					return false;
			}
		});

		inputsTreeViewer.setInput(InputNodes);
		inputsTreeViewer.expandToLevel(2);

		final Action a = new Action("Add new element") {
			public void run() {

				try {
					int length = ((Node) ((TreeSelection) inputSelection).getFirstElement()).getSubCategories().size();
					addTreeNode(((Node) ((TreeSelection) inputSelection).getFirstElement()).getOwlService(),
							((Node) ((TreeSelection) inputSelection).getFirstElement()), length);
					// Updating the display in the view
					// inputsTreeViewer.setInput(InputNodes);
					inputsTreeViewer.refresh();
				} catch (Exception e) {
					Activator.log("Error while running the workflow", e);
					e.printStackTrace();
				}

			}
		};

		final Action b = new Action("Remove element") {
			public void run() {

				try {
					Node n = ((Node) ((TreeSelection) inputSelection).getFirstElement()).getParent();
					n.getSubCategories().remove(((Node) ((TreeSelection) inputSelection).getFirstElement()));

					// Updating the display in the view
					// inputsTreeViewer.setInput(InputNodes);
					inputsTreeViewer.refresh();
				} catch (Exception e) {
					Activator.log("Error while running the workflow", e);
					e.printStackTrace();
				}

			}
		};
		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);

		mgr.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				inputSelection = inputsTreeViewer.getSelection();

				Node n = ((Node) ((TreeSelection) inputSelection).getFirstElement());
				if (!inputSelection.isEmpty()) {
					if (n.getOwlService().getArgument() != null && n.getOwlService().getArgument().isArray()
							&& n.getName().toString().replaceAll("[^\\d.]", "").isEmpty()) {
						boolean notMatched = true;
						// check if the array of primitive or array of objects
						// is matched
						if (n.getSubCategories().get(0).getValue().equals("matched")
								|| (!n.getOwlService().getArgument().getSubtypes().isEmpty()
										&& (n.getOwlService().getArgument().getSubtypes().get(0)).getOwlService()
												.getisMatchedIO())) {
							notMatched = false;
						}
						if (notMatched) {
							a.setText("Add new element for "
									+ ((Node) ((TreeSelection) inputSelection).getFirstElement()).getName().toString());
							a.setToolTipText("Right click to add new element");
							a.setEnabled(true);

							mgr.add(a);
						}

					}
					if (n.getOwlService().getArgument() != null && n.getOwlService().getArgument().isArray()
							&& !n.getName().toString().replaceAll("[^\\d.]", "").isEmpty()) {

						int nodeNum = Integer.parseInt(n.getName().toString().replaceAll("[^\\d.]", ""));
						Node parent = ((Node) ((TreeSelection) inputSelection).getFirstElement()).getParent();

						if (nodeNum == parent.getSubCategories().size() - 1 && nodeNum != 0) {

							b.setText("Remove element "
									+ ((Node) ((TreeSelection) inputSelection).getFirstElement()).getName().toString());
							b.setToolTipText("Right click to remove element");
							b.setEnabled(true);

							mgr.add(b);
						}
					}
				}
			}

		});
		inputsTreeViewer.getControl().setMenu(mgr.createContextMenu(inputsTreeViewer.getControl()));

		inputsComposite.addListener(SWT.Collapse, inputListener);
		inputsComposite.addListener(SWT.Expand, inputListener);

		inputsTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					if (selection.getFirstElement() != null) {
						for (int i = 0; i < graph.getNodes().size(); i++) {
							GraphNode graphNode = (GraphNode) graph.getNodes().get(i);
							if (((OwlService) ((MyNode) graphNode.getData()).getObject())
									.equals(((Node) selection.getFirstElement()).getOwlService())) {
								graphNode.highlight();
							} else
								graphNode.unhighlight();

						}
					}

				}
			}
		});

		// for (int i = 0; i < vertices.length; i++) {
		// final OwlService node = (OwlService) vertices[i];
		//
		// if (node.getType().contains("Property")) {
		// if (jungGraph.getInEdges(node).size() == 0) {
		//
		// showInputs(node, matchedNodes, graph);
		//
		// } else if ((jungGraph.getInEdges(node).size() == 1
		// && ((OwlService)
		// jungGraph.getPredecessors(node).toArray()[0]).getisMatchedIO())) {
		// showInputs(node, matchedNodes, graph);
		// }
		// }
		// }

		// create UriParams composite

		// rightComposite.setLayout(new GridLayout());
		// Composite urisLabelComposite = new Composite(rightComposite,
		// SWT.FILL);
		//
		// urisLabelComposite.setLayout(new GridLayout());
		// Label label3 = new Label(urisLabelComposite, SWT.FILL);
		// label3.setText("Workflow URI Parameters:");
		// label3.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
		// false));
		// label3.setFont(JFaceResources.getFontRegistry().getBold(""));
		// uriParamsComposite = new Composite(rightComposite, SWT.FILL);
		// uriParamsComposite.setLayout(new GridLayout(2, false));

		// get all uriParams

		// for (int i = 0; i < vertices.length; i++) {
		// final OwlService node = (OwlService) vertices[i];
		//
		// if (node.getType().contains("Action")) {
		// if (!node.getOperation().getUriParameters().isEmpty()) {
		// for (Argument arg : node.getOperation().getUriParameters()) {
		// showUriParams(arg);
		// }
		// }
		// }
		// }
		// create authentication Params composite

		rightComposite.setLayout(new GridLayout());
		Composite authenticationLabelComposite = new Composite(rightComposite, SWT.FILL);

		authenticationLabelComposite.setLayout(new GridLayout());
		Label label4 = new Label(authenticationLabelComposite, SWT.FILL);
		label4.setText("Workflow Authentication Parameters:");
		label4.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label4.setFont(JFaceResources.getFontRegistry().getBold(""));
		authParamsComposite = new Composite(rightComposite, SWT.FILL);
		authParamsComposite.setLayout(new GridLayout(2, false));

		// get all authParams
		ArrayList<String> baseURIs = new ArrayList<String>();
		for (int i = 0; i < vertices.length; i++) {
			final OwlService node = (OwlService) vertices[i];

			if (node.getType().contains("Action") && node.getOperation().getDomain() != null) {
				if (node.getOperation().getDomain().getSecurityScheme() != null) {
					if (node.getOperation().getDomain().getSecurityScheme().equalsIgnoreCase("Basic Authentication")
							&& !baseURIs.contains(node.getOperation().getDomain().getURI())) {
						showBasicAuthenticationParams();
						baseURIs.add(node.getOperation().getDomain().getURI());
					}
				}
			}
		}

		Listener outputListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				TreeItem treeItem = (TreeItem) event.item;
				final TreeColumn[] treeColumns = treeItem.getParent().getColumns();

				display.asyncExec(new Runnable() {

					@Override
					public void run() {
						for (TreeColumn treeColumn : treeColumns)
							treeColumn.pack();
					}
				});
			}
		};

		// create outputs composite

		Composite outputsLabelComposite = new Composite(rightComposite, SWT.FILL);
		outputsLabelComposite.setLayout(new GridLayout(1, false));
		Label label2 = new Label(outputsLabelComposite, SWT.NONE);
		label2.setText("Workflow Outputs:");
		label2.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label2.setFont(JFaceResources.getFontRegistry().getBold(""));
		outputsComposite = new Tree(rightComposite, SWT.BORDER | SWT.FILL | SWT.MULTI);
		outputsComposite.setLayout(new GridLayout(2, false));
		treeViewer = new TreeViewer(outputsComposite);
		column1 = new TreeViewerColumn(treeViewer, SWT.NONE);
		column1.getColumn().setWidth(300);
		column1.getColumn().setText("Column1");
		column1.getColumn().setResizable(true);
		// column1.getColumn().pack();
		column2 = new TreeViewerColumn(treeViewer, SWT.NONE);
		column2.getColumn().setText("Column2");
		column2.getColumn().setWidth(300);
		column2.getColumn().setResizable(true);
		// column2.getColumn().pack();
		// get all outputs
		Vector<Node> nodes = new Vector<Node>();
		for (int i = 0; i < vertices.length; i++) {
			final OwlService node = (OwlService) vertices[i];

			if (node.getType().contains("Action")) {
				Collection<OwlService> successors = (Collection<OwlService>) jungGraph.getSuccessors(node);
				for (OwlService successor : successors) {
					if (successor.getType().contains("Property")) {
						showOutputs(successor, null, nodes, jungGraph);

					}
				}
			}
		}

		treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		outputsComposite.setSize(300, 200);
		treeViewer.setContentProvider(new MyTreeContentProvider());

		// sort alphabetically based on operation name
		treeViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				Node t1 = (Node) e1;
				Node t2 = (Node) e2;
				int order = ((t1.getOwlService().getArgument().getBelongsToOperation().getName().toString())
						.compareTo(t2.getOwlService().getArgument().getBelongsToOperation().getName().toString()));
				return order;
			};
		});

		column1.setLabelProvider(new MyLabelProvider());
		column2.setLabelProvider(createColumnLabelProvider());
		treeViewer.setInput(nodes);
		// // outputsComposite.setSize(300, nodes.size() * 10);
		// treeViewer.expandAll();
		//

		outputsComposite.addListener(SWT.Collapse, outputListener);
		outputsComposite.addListener(SWT.Expand, outputListener);

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					if (selection.getFirstElement() != null) {
						for (int i = 0; i < graph.getNodes().size(); i++) {
							GraphNode graphNode = (GraphNode) graph.getNodes().get(i);
							if (((OwlService) ((MyNode) graphNode.getData()).getObject())
									.equals(((Node) selection.getFirstElement()).getOwlService())) {
								graphNode.highlight();
							} else
								graphNode.unhighlight();

						}
					}

				}
			}
		});

		graph.update();
		graph.redraw();

		inputsLabelComposite.redraw();
		outputsLabelComposite.redraw();
		treeViewer.refresh();
		inputsTreeViewer.refresh();
		outputsComposite.redraw();
		inputsComposite.redraw();
		rightComposite.layout();
		rightComposite.update();
		rightComposite.redraw();
		sc.update();
		sc.redraw();

		sashForm.update();
		sashForm.redraw();
		sashForm.layout(true);
		this.showBusy(false);
	}

	private static ColumnLabelProvider createColumnLabelProvider() {
		return new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return ((Node) element).getValue();
			}

		};
	}

	// private ColumnLabelProvider createColumnZLabelProvider(Vector<Node>
	// InputNodes) {
	// return new ColumnLabelProvider() {
	// // make sure you dispose these buttons when viewer input changes
	// // Map<Object, Button> buttons = new HashMap<Object, Button>();
	//
	// @Override
	// public void update(ViewerCell cell) {
	//
	// TreeItem item = (TreeItem) cell.getItem();
	// if (((Node) item.getData()).getOwlService().getArgument().isArray()
	// && ((Node) item.getData()).getName().toString().replaceAll("[^\\d.]",
	// "").isEmpty()) {
	// // Button button;
	// // if (buttons.containsKey(cell.getElement())) {
	// // button = buttons.get(cell.getElement());
	// // } else {
	// Button button = new Button((Composite) cell.getViewerRow().getControl(),
	// SWT.PUSH);
	// button.addSelectionListener(new SelectionAdapter() {
	// @Override
	// public void widgetSelected(SelectionEvent e) {
	// int length = ((Node) item.getData()).getSubCategories().size();
	// // Node n= new Node(((Node)
	// // item.getData()).getOwlService().getName().toString()+
	// // "[" + length + "]", ((Node) item.getData()),
	// // ((Node) item.getData()).getOwlService(),
	// // null);
	// addTreeNode(((Node) item.getData()).getOwlService(), (Node)
	// item.getData(), length);
	// // Updating the display in the view
	// // disposeButtons();
	// inputsTreeViewer.setInput(InputNodes);
	// columnz.getColumn().getData();
	// columnz.setLabelProvider(createColumnZLabelProvider(InputNodes));
	// columnz.getViewer().refresh();
	// }
	// // public void disposeButtons(){
	// // buttons.clear();
	// // }
	// });
	// // button.setText("+");
	// // buttons.put(cell.getElement(), button);
	// // }
	// TreeEditor editor = new TreeEditor(item.getParent());
	// editor.grabHorizontal = true;
	// editor.grabVertical = true;
	// editor.setEditor(button, item, cell.getColumnIndex());
	// editor.layout();
	// }
	// }
	//
	// };
	// }

	public void addTreeNode(final Object arg, Node parent, int length) {
		Node n;
		if (length != 0) {
			n = parent;
		} else {
			n = new Node(((OwlService) arg).getName().toString(), parent, (OwlService) arg, null);
		}

		if (((OwlService) arg).getArgument().isArray()) {
			Node subnode = new Node(((OwlService) arg).getName().toString() + "[" + length + "]", n, (OwlService) arg,
					null);
			// columnb.setLabelProvider(createColumnLabelProvider());
			if (!((OwlService) arg).getArgument().getSubtypes().isEmpty()) {
				Collection<OwlService> predecessors = (Collection<OwlService>) jungGraph
						.getPredecessors((OwlService) arg);
				for (OwlService predecessor : predecessors) {
					if (predecessor.getType().contains("Property")) {
						addTreeNode(predecessor, subnode, 0);

					}
				}
			} 
		} else {

			if (!((OwlService) arg).getArgument().getSubtypes().isEmpty()) {
				Collection<OwlService> predecessors = (Collection<OwlService>) jungGraph
						.getPredecessors((OwlService) arg);
				for (OwlService predecessor : predecessors) {
					if (predecessor.getType().contains("Property")) {
						addTreeNode(predecessor, n, 0);

					}
				}
			} else {
				n.setValue("enter value");
			}
		}
	}

	public void showInputs(final Object arg, Node parent, Vector<Node> nodes,
			edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph, ArrayList<OwlService> matchedNodes) {

		String[] datatypes = new String[] { "string", "long", "int", "float", "double", "dateTime", "boolean" };
		Node n = new Node(((OwlService) arg).getName().toString(), parent, (OwlService) arg, null);
		// if it is native argument
		if (arg instanceof Value) {
			if (RAMLCaller.stringIsItemFromList(((OwlService) arg).getArgument().getType(), datatypes)) {
				n.setValue("enter value");
			}
		} else {

			if (((OwlService) arg).getArgument().isArray()) {
				Node subnode = new Node(((OwlService) arg).getName().toString() + "[0]", n, (OwlService) arg, null);

				if (!((OwlService) arg).getArgument().getSubtypes().isEmpty()) {
					subnode.setValue("");
					Collection<OwlService> predecessors = (Collection<OwlService>) graph
							.getPredecessors((OwlService) arg);
					for (OwlService predecessor : predecessors) {
						if (predecessor.getType().contains("Property")) {
							showInputs(predecessor, subnode, nodes, graph, matchedNodes);

						}
					}
				} else {
					if (matchedNodes.contains(arg)) {
						subnode.setValue("matched");
					} else {
						subnode.setValue("enter value");
					}
				}
			} else {
				if (!((OwlService) arg).getArgument().getSubtypes().isEmpty()) {
					Collection<OwlService> predecessors = (Collection<OwlService>) graph
							.getPredecessors((OwlService) arg);
					for (OwlService predecessor : predecessors) {
						if (predecessor.getType().contains("Property")) {
							showInputs(predecessor, n, nodes, graph, matchedNodes);

						}
					}
				} else {
					if (matchedNodes.contains(arg)) {
						n.setValue("matched");
					} else {
						n.setValue("enter value");
					}
				}
			}

		}

		if (parent == null) {
			nodes.add(n);

		}

	}

	public static void showOutputs(final Object arg, Node parent, Vector<Node> nodes,
			edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph) {

		String[] datatypes = new String[] { "string", "long", "int", "float", "double", "dateTime", "boolean" };
		Node n;
		if (arg instanceof Value) {
			n = new Node(((Value) arg).getName().toString(), parent, ((Value) arg).getOwlService(), (Value) arg);
			for (OwlService service : graph.getVertices()) {

				if (service.equals(((Value) arg).getOwlService())
						&& RAMLCaller.stringIsItemFromList(((Value) arg).getType(), datatypes)) {
					try {
						service.setContent((Value) arg);
					} catch (Exception e) {
						Activator.log("Error setting outputs content", e);
						e.printStackTrace();
					}
				}
			}
			// array of primitive
			if (((Value) arg).isArray() && RAMLCaller.stringIsItemFromList(((Value) arg).getType(), datatypes)) {
				for (Argument element : ((Value) arg).getElements()) {
					Node e = new Node(element.getName().toString(), n, n.getOwlService(), ((Value) element));
					// column.setLabelProvider(createColumnLabelProvider());
					// TreeItem newItem = new TreeItem(item, SWT.NONE);
					// newItem.setText(1, ((Value) element).getValue());
				}
				// array of array or array of objects
			} else
				if (((Value) arg).isArray() && !RAMLCaller.stringIsItemFromList(((Value) arg).getType(), datatypes)) {
				for (Argument element : ((Value) arg).getElements()) {
					Node e = new Node(element.getName().toString(), n, ((Value) arg).getOwlService(),
							((Value) element));
					// column.setLabelProvider(createColumnLabelProvider());
					// TreeItem newItem = new TreeItem(item, SWT.NONE);
					// newItem.setText(0, element.getName().toString());
					if (element.isArray()) {
						for (Argument el : ((Value) element).getElements()) {
							showOutputs(el, e, nodes, graph);
						}
						if (element.getElements().isEmpty()) {
							for (Argument sub : element.getSubtypes())
								showOutputs(sub, e, nodes, graph);
						}
					} else {
						for (Argument sub : element.getSubtypes())
							showOutputs(sub, e, nodes, graph);
					}

				}
				if (((Value) arg).getElements().isEmpty()) {
					for (Argument sub : ((Value) arg).getSubtypes())
						showOutputs(sub, n, nodes, graph);
				}
			} else if (!((Value) arg).isArray()
					&& !RAMLCaller.stringIsItemFromList(((Value) arg).getType(), datatypes)) {
				for (Argument sub : ((Value) arg).getSubtypes()) {
					// TreeItem newItem = new TreeItem(item, SWT.NONE);
					// newItem.setText(0, sub.getName().toString() + " [" +
					// sub.getType() +
					// "]:");
					showOutputs(sub, n, nodes, graph);
				}
			}

		} else {
			n = new Node(((OwlService) arg).getName().toString(), parent, (OwlService) arg, null);
			// column.setLabelProvider(
			//
			// createColumnLabelProvider());

			if (!((OwlService) arg).getArgument().getSubtypes().isEmpty()) {
				Collection<OwlService> successors = (Collection<OwlService>) graph.getSuccessors((OwlService) arg);
				for (OwlService successor : successors) {
					showOutputs(successor, n, nodes, graph);
				}
			}
		}

		if (parent == null) {
			nodes.add(n);

		}

	}

	public void showBasicAuthenticationParams() {

		// username
		Label firstLabel = new Label(authParamsComposite, SWT.NONE);
		firstLabel.setText("Username*" + ":");
		firstLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		Text firstText = new Text(authParamsComposite, SWT.BORDER);
		firstText.setText("");
		firstText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		firstText.setEditable(true);

		// password
		Label secondLabel = new Label(authParamsComposite, SWT.NONE);
		secondLabel.setText("Password*" + ":");
		secondLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		Text secondText = new Text(authParamsComposite, SWT.BORDER);
		secondText.setText("");
		secondText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		secondText.setEditable(true);

	}

	// public void showUriParams(final Argument arg) {
	//
	// Label firstLabel = new Label(uriParamsComposite, SWT.NONE);
	// firstLabel.setText(arg.getName() + " [" + arg.getType() + "]*" + ":");
	// firstLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
	// false));
	//
	// Text firstText = new Text(uriParamsComposite, SWT.BORDER);
	// firstText.setText("");
	// firstText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	// firstText.setEditable(true);
	//
	// }

	
	public edu.uci.ics.jung.graph.Graph<OwlService, Connector> getJungGraph() {
		return jungGraph;
	}

	public void setJungGraph(edu.uci.ics.jung.graph.Graph<OwlService, Connector> jungGraph) {
		this.jungGraph = jungGraph;
	}

	public void setSavedWorkflow(boolean saved) {
		this.savedWorkflow = saved;
	}

	public boolean getSavedWorkflow() {
		return savedWorkflow;
	}

	public void setWorkflowFilePath(String path) {
		this.workflowFilePath = path;
	}

	public String getWorkflowFilePath() {
		return workflowFilePath;
	}

	public void setStoryboardFile(IFile file) {
		this.storyboardFile = file;
	}

	/**
	 * <h1>addOperationInZest</h1> It is called by <code>addNode</code> in order
	 * to add the new nodes and connections of a single operation in the zest
	 * graph.
	 * 
	 * @param owlService
	 */
	public void addOperationInZest(OwlService owlService,
			edu.uci.ics.jung.graph.Graph<OwlService, Connector> tempGraph) {

		List<MyNode> nodes = createGraphNodes(owlService);
		Graph zestGraph = viewer.getGraphControl();

		int count1 = 0;
		int count2 = 0;
		int count3 = 0;
		int constant = 50;
		int inputx = 0;
		int outputx = 0;
		int suboutputx = 0;
		int x = viewer.getGraphControl().getSize().x;
		int y = viewer.getGraphControl().getSize().y;
		for (MyNode node : nodes) {
			// viewer.addNode(node);
			// create graph node
			GraphNode graphNode = new GraphNode(zestGraph, SWT.NONE, node.getName(), node);
			OwlService service = (OwlService) node.getObject();

			// set its location
			if (service.getType().contains("Action")) {
				graphNode.setLocation(point.x, point.y);
			}
			if (service.getType().contains("Property")) {

				if (jungGraph.getInEdges(service).size() == 0) {
					if ((count1 % 2) == 0) {
						graphNode.setLocation(point.x + inputx, point.y - constant);
						inputx = inputx + graphNode.getSize().width + constant;
					} else {
						graphNode.setLocation(point.x - inputx, point.y - constant);
					}
					count1++;
				}
				if (jungGraph.getOutEdges(service).size() == 0) {
					if ((count2 % 2) == 0) {
						graphNode.setLocation(point.x + suboutputx, point.y + 2 * constant);
						suboutputx = suboutputx + graphNode.getSize().width + constant;
					} else {
						graphNode.setLocation(point.x - suboutputx, point.y + 2 * constant);
					}
					count2++;
				}
				if (jungGraph.getInEdges(service).size() > 0 && jungGraph.getOutEdges(service).size() > 0) {
					if ((count3 % 2) == 0) {
						graphNode.setLocation(point.x + outputx, point.y + constant);
						outputx = outputx + graphNode.getSize().width + constant;
					} else {
						graphNode.setLocation(point.x - outputx, point.y + constant);
					}
					count3++;
				}
			}

		}
		

		for (int j = 0; j < zestGraph.getNodes().size(); j++) {
			GraphNode graphNode = (GraphNode) zestGraph.getNodes().get(j);
			for (MyNode node : nodes) {
				if (((MyNode) graphNode.getData()).getObject().equals(node.getObject())) {

					ZestLabelProvider labelProvider = new ZestLabelProvider();
					labelProvider.selfStyleNode((MyNode) graphNode.getData(), graphNode);

					for (int i = 0; i < ((MyNode) graphNode.getData()).getLinkedConnections().size(); i++) {
						MyNode destination = ((MyNode) graphNode.getData()).getLinkedConnections().get(i)
								.getDestination();
						GraphNode find = find(destination, zestGraph);
						if (find != null) {
							
							GraphConnection graphConnection = new GraphConnection(zestGraph, SWT.NONE, graphNode, find);
							
							EntityConnectionData connectionData = new EntityConnectionData(node, destination);
							graphConnection.setData(connectionData);
						}
					}
				}
			}
		}

		
	}
	
	
	private List<MyNode> createInputNodes(OwlService input, List<MyNode> nodes) {
		MyNode inputNode = new MyNode(input.toString(), input.toString(), input);
		nodes.add(inputNode);
		for (OwlService subinput : jungGraph.getPredecessors(input)) {
			createInputNodes(subinput, nodes);
		}
		return nodes;
	}

	private List<MyConnection> createInputEdges(OwlService input, List<MyNode> nodes, List<MyConnection> connections) {
		Collection<Connector> subEdges = null;
		if (jungGraph.getInEdges(input) != null && jungGraph.getInEdges(input).size() != 0) {
			subEdges = jungGraph.getInEdges(input);

		}
		if (subEdges != null) {
			for (Connector con : subEdges) {
				OwlService jungSource = (OwlService) con.getSource();
				OwlService jungDest = (OwlService) con.getTarget();
				MyNode source = null;
				MyNode dest = null;

				for (int j = 0; j < nodes.size(); j++) {
					MyNode node = nodes.get(j);
					if (((OwlService) node.getObject()).equals(jungSource)) {
						source = node;
					} else if (((OwlService) node.getObject()).equals(jungDest)) {
						dest = node;
					}
				}

				MyConnection connect = new MyConnection(source.toString() + dest.toString(), con.getCondition(), source,
						dest);
				source.getLinkedConnections().add(connect);
				connections.add(connect);

			}
		}

		for (OwlService subInput : jungGraph.getPredecessors(input)) {
			createInputEdges(subInput, nodes, connections);
		}

		return connections;
	}

	private List<MyNode> createOutputNodes(OwlService output, List<MyNode> nodes) {
		MyNode outputNode = new MyNode(output.toString(), output.toString(), output);
		nodes.add(outputNode);
		for (OwlService subOutput : jungGraph.getSuccessors(output)) {
			createOutputNodes(subOutput, nodes);
		}
		return nodes;
	}

	private List<MyConnection> createOutputEdges(OwlService output, List<MyNode> nodes,
			List<MyConnection> connections) {
		Collection<Connector> subEdges = null;
		if (jungGraph.getOutEdges(output) != null && jungGraph.getOutEdges(output).size() != 0) {
			subEdges = jungGraph.getOutEdges(output);

		}
		if (subEdges != null) {
			for (Connector con : subEdges) {
				OwlService jungSource = (OwlService) con.getSource();
				OwlService jungDest = (OwlService) con.getTarget();
				MyNode source = null;
				MyNode dest = null;

				for (int j = 0; j < nodes.size(); j++) {
					MyNode node = nodes.get(j);
					if (((OwlService) node.getObject()).equals(jungSource)) {
						source = node;
					} else if (((OwlService) node.getObject()).equals(jungDest)) {
						dest = node;
					}
				}

				MyConnection connect = new MyConnection(source.toString() + dest.toString(), con.getCondition(), source,
						dest);
				source.getLinkedConnections().add(connect);
				connections.add(connect);

			}
		}

		for (OwlService subOutput : jungGraph.getSuccessors(output)) {
			createOutputEdges(subOutput, nodes, connections);
		}

		return connections;
	}
	
	public List<MyNode> createGraphNodes(OwlService owlService) {

		List<MyNode> nodes = new ArrayList<MyNode>();
		MyNode operationNode = new MyNode(owlService.toString(), owlService.toString(), owlService);
		nodes.add(operationNode);

		for (OwlService input : jungGraph.getPredecessors(owlService)) {
			nodes = createInputNodes(input, nodes);

		}

		for (OwlService output : jungGraph.getSuccessors(owlService)) {
			nodes = createOutputNodes(output, nodes);

		}

		// create all edges
		List<MyConnection> connections = new ArrayList<MyConnection>();
		Collection<Connector> inEdges = jungGraph.getInEdges(owlService);
		Collection<Connector> outEdges = jungGraph.getOutEdges(owlService);
		// Collection<Connector> subEdges = null;

		for (OwlService input : jungGraph.getPredecessors(owlService)) {
			connections = createInputEdges(input, nodes, connections);
		}

		for (OwlService output : jungGraph.getSuccessors(owlService)) {
			connections = createOutputEdges(output, nodes, connections);
		}

		for (Connector con : inEdges) {
			OwlService jungSource = (OwlService) con.getSource();
			OwlService jungDest = (OwlService) con.getTarget();
			MyNode source = null;
			MyNode dest = null;

			for (int j = 0; j < nodes.size(); j++) {
				MyNode node = nodes.get(j);
				if (((OwlService) node.getObject()).equals(jungSource)) {
					source = node;
				} else if (((OwlService) node.getObject()).equals(jungDest)) {
					dest = node;
				}
			}

			MyConnection connect = new MyConnection(source.toString() + dest.toString(), con.getCondition(), source,
					dest);
			source.getLinkedConnections().add(connect);
			connections.add(connect);

		}

		for (Connector con : outEdges) {
			OwlService jungSource = (OwlService) con.getSource();
			OwlService jungDest = (OwlService) con.getTarget();
			MyNode source = null;
			MyNode dest = null;

			for (int j = 0; j < nodes.size(); j++) {
				MyNode node = nodes.get(j);
				if (((OwlService) node.getObject()).equals(jungSource)) {
					source = node;
				} else if (((OwlService) node.getObject()).equals(jungDest)) {
					dest = node;
				}
			}

			MyConnection connect = new MyConnection(source.toString() + dest.toString(), con.getCondition(), source,
					dest);
			source.getLinkedConnections().add(connect);
			connections.add(connect);

		}

		
		for (MyConnection connection : connections) {
			connection.getSource().getConnectedTo().add(connection.getDestination());
		}
		return nodes;
	}

	/**
	 * <h1>find</h1> If there is a GraphNode in Zest graph which has the same
	 * MyNode data as the <code>node</code> find it.
	 * 
	 * @param node
	 * @param graph
	 * @return
	 */
	private GraphNode find(MyNode node, Graph graph) {
		for (Object o : graph.getNodes()) {
			GraphNode n = (GraphNode) o;
			if (node != null && node.getObject().equals(((MyNode) n.getData()).getObject())) {
				return n;
			}
		}
		return null;
	}

	/**
	 * <h1>addGraphInZest</h1> It is called in order to create a zest graph
	 * according to <code>graph</code>
	 * 
	 * @param graph
	 *            : the jung graph
	 */
	public void addGraphInZest(edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph) {

		List<MyNode> nodes = createGraphNodes(graph);
		addZestNodes(nodes);

	}

	/**
	 * <h1>addGraphInZest</h1> It is called in order to create a zest graph
	 * according to <code>graph</code>
	 * 
	 * @param graph
	 *            : the jung graph
	 */
	public void addGraphInZest(edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph,
			ArrayList<WeightReport> reports) {

		List<MyNode> nodes = createGraphNodes(graph);
		for (WeightReport report : reports) {
			for (MyNode node : nodes) {
				if (((OwlService) node.getObject()).getOperation() != null) {
					if (((OwlService) node.getObject()).equals(report.getReplaceInformation().getTargetService())) {
						node.setAlternativeOperations(report.getReplaceInformation().getAlternativeOperations());
						break;
					}
				}
			}
		}
		addZestNodes(nodes);

	}

	private void addZestNodes(List<MyNode> nodes) {
		Graph zestGraph = viewer.getGraphControl();
		int graphNodeSize = zestGraph.getNodes().size();
		deleteAllNodes(zestGraph, graphNodeSize);

		// viewer.refresh();

		for (MyNode node : nodes) {
			// viewer.addNode(node);
			GraphNode graphNode = new GraphNode(zestGraph, SWT.NONE, node.getName(), node);

		}

		for (int j = 0; j < zestGraph.getNodes().size(); j++) {
			GraphNode graphNode = (GraphNode) zestGraph.getNodes().get(j);
			for (MyNode node : nodes) {
				if (((MyNode) graphNode.getData()).getObject().equals(node.getObject())) {

					ZestLabelProvider labelProvider = new ZestLabelProvider();
					labelProvider.selfStyleNode((MyNode) graphNode.getData(), graphNode);

					for (int i = 0; i < ((MyNode) graphNode.getData()).getLinkedConnections().size(); i++) {
						MyNode destination = ((MyNode) graphNode.getData()).getLinkedConnections().get(i)
								.getDestination();
						String condition = ((MyConnection) ((MyNode) graphNode.getData()).getLinkedConnections().get(i))
								.getLabel();
						GraphNode find = find(destination, zestGraph);
						if (find != null) {
							
							GraphConnection graphConnection = new GraphConnection(zestGraph, SWT.NONE, graphNode, find);
							graphConnection.setText(condition);
							
							EntityConnectionData connectionData = new EntityConnectionData(node, destination);
							graphConnection.setData(connectionData);
						}
					}
				}
			}
		}
		viewer.applyLayout();
	}

	
	/**
	 * <h1>deleteAllNodes</h1>Delete all nodes from the zest graph.
	 * 
	 * @param zestGraph
	 * @param size
	 *            : the number of nodes
	 */
	public void deleteAllNodes(org.eclipse.zest.core.widgets.Graph zestGraph, int size) {
		for (int j = 0; j < size; j++) {
			GraphNode graphNode = (GraphNode) zestGraph.getNodes().get(0);
			graphNode.dispose();
		}
	}

	

	private void reloadStoryboard(Display disp, Shell shell) {
		if (this.storyboardFile != null) {
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
						final ArrayList<Operation> operations = Algorithm
								.importServices(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
										+ "/.metadata/.plugins/eu.scasefp7.servicecomposition/ontology/WS.owl");
						
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

										setJungGraph(jungGraph);
										addGraphInZest(jungGraph);
										updateRightComposite(jungGraph);
										setSavedWorkflow(false);
										// view.setDirty(true);
										setFocus();

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

	

	
	public IStatus checkGraph(edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph, final Display disp)
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



	
	

	public void loadOperations(Display disp, Shell shell) {

		try {
			ImportHandler.ontologyCheck(shell, disp);
			// check if user has cancelled
			// if (monitor.isCanceled())
			// return Status.CANCEL_STATUS;
			if (operations == null || getUpdateOperations()) {
				Algorithm.init();
				operations = Algorithm.importServices(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
						+ "/.metadata/.plugins/eu.scasefp7.servicecomposition/ontology/WS.owl");
				// set the operations
				setOperations(operations);
				setUpdateOperations(false);
				ImportHandler.setOperations(operations);
			}

			// monitor.done();
			// return Status.OK_STATUS;
		} catch (Exception ex) {
			Activator.log("Error while loading the operations from the ontology", ex);
			ex.printStackTrace();
			// return Status.CANCEL_STATUS;
		} finally {
			// monitor.done();
		}
		// }
		// };
		// loadOperationJob.schedule();
	}

	public void setScaseProject(IProject project) {
		scaseProject = project;
	}

	public static void setOperations(ArrayList<Operation> operationsList) {
		operations = operationsList;
	}

	public static ArrayList<Operation> getOperations() {
		return operations;
	}

	public static void setUpdateYouRest(boolean update) {
		updateYouRest = update;
	}

	public static boolean getUpdateYouRest() {
		return updateYouRest;
	}

	public static void setUpdateOperations(boolean update) {
		updateOperations = update;
	}

	public static boolean getUpdateOperations() {
		return updateOperations;
	}

	public IProject getScaseProject() {
		return scaseProject;
	}

//	public NonLinearCodeGenerator getGenerator() {
//		return gGenerator;
//	}
	
	public Point getPoint() {
		return point;
	}

}