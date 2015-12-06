package eu.scasefp7.eclipse.servicecomposition.views;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;

//import javax.servlet.http.HttpServletRequest;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
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
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import javax.inject.Inject;

//import org.apache.commons.fileupload.FileItem;
//import org.apache.commons.fileupload.FileUpload;
//import org.apache.commons.fileupload.FileUploadBase.FileUploadIOException;
//import org.apache.commons.fileupload.FileUploadException;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
//import org.apache.commons.fileupload.util.Streams;
//import org.apache.maven.shared.invoker.DefaultInvocationRequest;
//import org.apache.maven.shared.invoker.DefaultInvoker;
//import org.apache.maven.shared.invoker.InvocationOutputHandler;
//import org.apache.maven.shared.invoker.InvocationRequest;
//import org.apache.maven.shared.invoker.InvocationResult;
//import org.apache.maven.shared.invoker.Invoker;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.IFigure;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
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

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import eu.scasefp7.eclipse.servicecomposition.operationCaller.RAMLCaller;
import eu.scasefp7.eclipse.servicecomposition.Activator;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.CallRestfulServiceCode;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.CallWSDLServiceCode;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.ConnectToMDEOntology;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.FunctionCodeNode;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.NonLinearCodeGenerator;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.RestfulCodeGenerator;
import eu.scasefp7.eclipse.servicecomposition.codeInterpreter.UserInput;
import eu.scasefp7.eclipse.servicecomposition.codeInterpreter.Value;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Service;
import eu.scasefp7.eclipse.servicecomposition.operationCaller.WSDLCaller;
import eu.scasefp7.eclipse.servicecomposition.repository.RepositoryClient;
import eu.scasefp7.eclipse.servicecomposition.tester.Algorithm;
import eu.scasefp7.eclipse.servicecomposition.tester.Algorithm.costReport;
import eu.scasefp7.eclipse.servicecomposition.tester.Algorithm.licenseReport;
import eu.scasefp7.eclipse.servicecomposition.tester.Algorithm.trialReport;
import eu.scasefp7.eclipse.servicecomposition.transformer.Matcher;
import eu.scasefp7.eclipse.servicecomposition.transformer.PathFinding;
import eu.scasefp7.eclipse.servicecomposition.transformer.Similarity;
import eu.scasefp7.eclipse.servicecomposition.transformer.Transformer;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;

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

	private int layout = 1;
	private Composite parent;
	private edu.uci.ics.jung.graph.Graph<OwlService, Connector> jungGraph;
	private Action runWorkflowAction;
	private Action newWorkflowAction;
	private Action displayCostAction;
	private Action generateCodeAction;
	private ScrolledComposite sc;
	private Composite rightComposite;
	private SashForm sashForm;
	private Composite inputsComposite;
	private Tree outputsComposite;
	private TreeViewerColumn column1;
	private TreeViewerColumn column2;
	private TreeViewer treeViewer;
	private Composite uriParamsComposite;
	private Composite authParamsComposite;
	private static double VARIABLE_NAME_SIMILARITY_THRESHOLD = 0.5;
	private static double MAX_DISTANCE_BETWEEN_SOLUTIONS = 0.1;
	private static double VALUE_SIMILARITY_THRESHOLD = 0;
	private OwlService currentService;
	private GraphViewer viewer;
	private String projectName = "";
	IProject currentProject;
	NonLinearCodeGenerator gGenerator = new NonLinearCodeGenerator();
	private String pomPath = "";
	public static MavenExecutionRequestPopulator populator;
	public static DefaultPlexusContainer container;
	public static Maven maven;
	static public List<MavenProject> buildedProjects = new ArrayList<MavenProject>();
	static public SettingsBuilder settingsBuilder;
	private MavenExecutionResult installResult = null;
	private IProject scaseProject;

	// predicates for special types of branches
	private static String[] TRUE_PREDICATES = { "true", "yes" };
	private static String[] FALSE_PREDICATES = { "false", "no" };
	private static String[] ELSE_PREDICATES = { "else", "otherwise" };
	private OwlService startingService = null;
	private OwlService endingService = null;
	private Job runWorkflowJob;
	private Job AddNewOperationJob;
	private Job createWarFileJob;
	private Job uploadToServerJob;
	private GraphConnection selectedGraphEdge;
	private GraphNode selectedGraphNode;
	private Point point;

	public void createPartControl(Composite parent) {

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

		RepositoryClient repo = new RepositoryClient();
		String path = repo.downloadOntology("WS");

		final Graph graph = viewer.getGraphControl();
		graph.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				for (int i = 0; i < graph.getNodes().size(); i++) {
					GraphNode graphNode = (GraphNode) graph.getNodes().get(i);
					graphNode.unhighlight();
				}
				graph.setSelection(new GraphItem[] { (GraphNode) e.item });

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

					// if (((OwlService) ((MyNode)
					// source.getData()).getObject()).getType().equals("Property")
					// && ((OwlService) ((MyNode)
					// dest.getData()).getObject()).getType().equals("Property")
					// && dest.getSourceConnections().size() > 0) {
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
					item.addListener(SWT.Selection, new Listener() {

						@Override
						public void handleEvent(Event event) {
							removeEdge(selectedGraphEdge);

						}
					});

					// Rename condition
					item2.addListener(SWT.Selection, new Listener() {

						@Override
						public void handleEvent(Event event) {

							renameCondition(selectedGraphEdge);
						}
					});

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
						if (arg.isNative() && !arg.isArray() && !isMemberOfArray) {
							item.setEnabled(true);
						} else {
							item.setEnabled(false);
						}
						menu.setVisible(true);

						// Match i/o with..
						item.addListener(SWT.Selection, new Listener() {

							@Override
							public void handleEvent(Event event) {
								matchIO(selectedGraphNode);
							}
						});

					} else if (((OwlService) ((MyNode) selectedGraphNode.getData()).getObject()).getType()
							.equals("Condition")) {
						Menu menu = new Menu(getDisplay().getActiveShell(), SWT.POP_UP);
						MenuItem item = new MenuItem(menu, SWT.NONE);
						item.setText("Link this condition to..");
						MenuItem item2 = new MenuItem(menu, SWT.NONE);
						item2.setText("Remove condition");

						if (((MyNode) selectedGraphNode.getData()).getLinkedConnections().size() < 2) {
							item.setEnabled(true);

						} else {
							item.setEnabled(false);
						}

						menu.setVisible(true);

						// Link condition with..
						item.addListener(SWT.Selection, new Listener() {

							@Override
							public void handleEvent(Event event) {
								String mode = "Condition";
								linkOperation(selectedGraphNode, mode);
							}
						});

						// Remove condition
						item2.addListener(SWT.Selection, new Listener() {

							@Override
							public void handleEvent(Event event) {
								String mode = "Condition";
								removeNode(selectedGraphNode, mode);
							}
						});

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
						item.addListener(SWT.Selection, new Listener() {

							@Override
							public void handleEvent(Event event) {
								String mode = "StartNode";
								linkOperation(selectedGraphNode, mode);
							}
						});

					} else if (((OwlService) ((MyNode) selectedGraphNode.getData()).getObject()).getType()
							.equals("Action")) {
						Menu menu = new Menu(getDisplay().getActiveShell(), SWT.POP_UP);
						MenuItem item = new MenuItem(menu, SWT.NONE);
						item.setText("Remove operation "
								+ ((OwlService) ((MyNode) selectedGraphNode.getData()).getObject()).getName());
						item.setEnabled(true);
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
						item.addListener(SWT.Selection, new Listener() {

							@Override
							public void handleEvent(Event event) {
								String mode = "Operation";
								removeNode(selectedGraphNode, mode);
							}
						});

						// Link the operation with..
						item2.addListener(SWT.Selection, new Listener() {

							@Override
							public void handleEvent(Event event) {
								String mode = "Operation";
								linkOperation(selectedGraphNode, mode);
							}
						});

					}

				} else {
					Menu menu = new Menu(getDisplay().getActiveShell(), SWT.POP_UP);
					MenuItem item = new MenuItem(menu, SWT.NONE);
					item.setText("Add operation..");
					MenuItem item2 = new MenuItem(menu, SWT.NONE);
					item2.setText("Add condition..");
					menu.setVisible(true);

					// Add new operation.
					item.addListener(SWT.Selection, new Listener() {

						@Override
						public void handleEvent(Event event) {

							addNewOperation();
						}
					});

					// Add new condition.
					item2.addListener(SWT.Selection, new Listener() {

						@Override
						public void handleEvent(Event event) {

							addNewCondition();
						}
					});
				}

			}
		});

	}

	public class NodeFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {

			if (element instanceof MyNode) {
				MyNode node = (MyNode) element;
				return node.getName().toLowerCase().contains("a");

			}
			return true;
		}

	}

	/**
	 * <h1>matchIO</h1> It is used to match (link variables) outputs with
	 * inputs.
	 * 
	 * @param node
	 *            : The selected graph node.
	 */

	private void matchIO(final GraphNode node) {

		// ArrayList<String> list = new ArrayList<String>();
		ArrayList<OwlService> list = new ArrayList<OwlService>();

		if (node.getTargetConnections().size() == 0) {
			// System.out.println("Node is an input");
			Graph graph = viewer.getGraphControl();
			for (int j = 0; j < graph.getNodes().size(); j++) {
				GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
				if ((graphNode.getSourceConnections().size() == 0)
						&& ((OwlService) ((MyNode) graphNode.getData()).getObject()).getType().contains("Property")
						&& Matcher.common(
								((OwlService) ((MyNode) node.getData()).getObject()).getArgument().getParent(),
								((OwlService) ((MyNode) graphNode.getData()).getObject()).getArgument().getParent())
								.isEmpty()) {
					String outputName = ((OwlService) ((MyNode) graphNode.getData()).getObject()).getName().toString();
					// list.add(outputName);
					boolean isMemberOfArray = false;
					for (int i = 0; i < ((OwlService) ((MyNode) graphNode.getData()).getObject()).getArgument()
							.getParent().size(); i++) {
						if (((OwlService) ((MyNode) graphNode.getData()).getObject()).getArgument().getParent()
								.get(i) instanceof Argument) {
							if (((Argument) ((OwlService) ((MyNode) graphNode.getData()).getObject()).getArgument()
									.getParent().get(i)).isArray()) {
								isMemberOfArray = true;
							}
						}
					}
					if (!((OwlService) ((MyNode) graphNode.getData()).getObject()).getArgument().isArray()
							&& !isMemberOfArray) {
						list.add((OwlService) ((MyNode) graphNode.getData()).getObject());
					}
				}
			}
			// Open selection window
			String option = "matchinput";
			SelectionWindowNode(list, option);

		} else if (node.getSourceConnections().size() == 0) {
			// System.out.println("Node is an output");
			Graph graph = viewer.getGraphControl();
			for (int j = 0; j < graph.getNodes().size(); j++) {
				GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
				if ((graphNode.getTargetConnections().size() == 0)
						&& ((OwlService) ((MyNode) graphNode.getData()).getObject()).getType().contains("Property")
						&& Matcher.common(
								((OwlService) ((MyNode) node.getData()).getObject()).getArgument().getParent(),
								((OwlService) ((MyNode) graphNode.getData()).getObject()).getArgument().getParent())
								.isEmpty()) {
					String inputName = ((OwlService) ((MyNode) graphNode.getData()).getObject()).getName().toString();
					// list.add(inputName);

					list.add((OwlService) ((MyNode) graphNode.getData()).getObject());

				}
			}
			// Open selection window
			String option = "matchoutput";
			SelectionWindowNode(list, option);
		}

	}

	/**
	 * <h1>linkOperation</h1>Link selected node with the node the user chooses
	 * from the selection window(nodes in this case can be anything but IO)
	 * 
	 * @param node
	 * @param mode
	 */
	private void linkOperation(final GraphNode node, final String mode) {

		ArrayList<OwlService> list = new ArrayList<OwlService>();
		// String option = "link";
		Collection<OwlService> services = new ArrayList<OwlService>(jungGraph.getVertices());
		for (OwlService service : services) {
			if (mode == "Operation") {
				if (!(service.getType().contains("Property"))
						&& !((((MyNode) node.getData()).getObject()).equals(service))
						&& !(service.getType().contains("StartNode"))) {

					list.add(service);

				}
			} else if (mode == "StartNode") {
				if (service.getType().contains("Action") || (service.getType().contains("EndNode"))) {

					list.add(service);
				}

			} else if (mode == "Condition") {
				if (!(service.getType().contains("Property"))
						&& !((((MyNode) node.getData()).getObject()).equals(service))) {

					list.add(service);

				}
			}
		}
		// Open selection window
		String option = "link";
		SelectionWindowNode(list, option);

	}

	/**
	 * Dialog for renaming an edge coming out of a condition.
	 * 
	 * @author mkoutli
	 *
	 */
	public class RenameConditionDialog extends Dialog {
		String value;

		/**
		 * @param parent
		 */
		public RenameConditionDialog(Shell parent) {
			super(parent);
		}

		// /**
		// * @param parent
		// * @param style
		// */
		// public RenameConditionDialog(Shell parent, int style) {
		// super(parent, style);
		// }

		/**
		 * Makes the dialog visible.
		 * 
		 * @return
		 */
		public String open() {
			Shell parent = getParent();
			final Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
			shell.setText("Name of the condition");

			shell.setLayout(new GridLayout(2, true));

			Label label = new Label(shell, SWT.NULL);
			label.setText("Please enter the condition");

			final Text text = new Text(shell, SWT.SINGLE | SWT.BORDER);

			final Button buttonOK = new Button(shell, SWT.PUSH);
			buttonOK.setText("Ok");
			buttonOK.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			Button buttonCancel = new Button(shell, SWT.PUSH);
			buttonCancel.setText("Cancel");

			text.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event event) {
					try {
						value = new String(text.getText());
						buttonOK.setEnabled(true);
					} catch (Exception e) {
						buttonOK.setEnabled(false);
					}
				}
			});

			buttonOK.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					shell.dispose();
				}
			});

			buttonCancel.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					value = null;
					shell.dispose();
				}
			});

			shell.addListener(SWT.Traverse, new Listener() {
				public void handleEvent(Event event) {
					if (event.detail == SWT.TRAVERSE_ESCAPE)
						event.doit = false;
				}
			});

			text.setText("");
			shell.pack();
			shell.open();

			Display display = parent.getDisplay();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}

			return value;
		}
	}

	/**
	 * <h1>renameCondition</h1>Rename the selected edge. Edge is coming out of a
	 * condition node.
	 * 
	 * @param edge
	 */
	private void renameCondition(GraphConnection edge) {
		Shell shell = new Shell();
		RenameConditionDialog dialog = new RenameConditionDialog(shell);
		// System.out.println(dialog.open());
		String s = dialog.open();
		boolean exist = false;
		// If a string was returned, say so.
		if ((s != null) && (s.trim().length() > 0)) {

			for (int i = 0; i < jungGraph.getEdges().size(); i++) {
				Connector con = (Connector) jungGraph.getEdges().toArray()[i];
				if (((MyNode) edge.getSource().getData()).getObject().equals(con.getSource())
						&& ((MyNode) edge.getDestination().getData()).getObject().equals(con.getTarget())) {
					OwlService source = (OwlService) con.getSource();
					OwlService target = (OwlService) con.getTarget();
					for (int j = 0; j < jungGraph.getOutEdges(source).size(); j++) {
						if (jungGraph.getOutEdges(source).toArray()[j].toString().equalsIgnoreCase(s)) {

							final Display disp = Display.getCurrent();
							disp.syncExec(new Runnable() {
								@Override
								public void run() {
									MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
											"Name already exists.");
								}
							});
							exist = true;
							break;
						}
					}
					if (!exist) {
						jungGraph.removeEdge(con);
						con.setCondition(s);
						jungGraph.addEdge(con, source, target, EdgeType.DIRECTED);
						edge.setText(s);

						System.out.println("New condition name=" + s);
						break;
					}

				}
			}
		} else {
			final Display disp = Display.getCurrent();
			disp.syncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openInformation(disp.getActiveShell(), "Error occured", "This is not a valid name.");
				}

			});
		}
	}

	/**
	 * <h1>addNewCondition</h1> Add new condition node.
	 */
	private void addNewCondition() {

		if (jungGraph == null) {
			final Display disp = Display.getCurrent();
			disp.syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openInformation(disp.getActiveShell(), "Warning", "Create a new workflow first!");
				}
			});

		} else {

			Shell shell = new Shell();
			RenameConditionDialog dialog = new RenameConditionDialog(shell);

			String s = dialog.open();
			boolean exist = false;
			// If a string was returned, say so.
			if ((s != null) && (s.trim().length() > 0)) {

				Collection<OwlService> services = jungGraph.getVertices();
				for (OwlService service : services) {
					if (service.getType().equalsIgnoreCase("Condition")
							&& service.getName().toString().equalsIgnoreCase(s)) {

						final Display disp = Display.getCurrent();
						disp.syncExec(new Runnable() {

							@Override
							public void run() {
								MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
										"Name already exists.");
							}
						});
						exist = true;
						break;
					}
				}

				if (!exist) {
					Service condition = new Service(s, "Condition");
					OwlService owlService = new OwlService(condition);
					jungGraph.addVertex(owlService);

					MyNode node = new MyNode(owlService.toString(), owlService.toString(), owlService);
					// viewer.addNode(node);
					Graph graph = viewer.getGraphControl();
					GraphNode graphNode = new GraphNode(graph, SWT.NONE, s, node);
					graphNode.setLocation(point.x, point.y);
					ZestLabelProvider labelProvider = new ZestLabelProvider();
					labelProvider.selfStyleNode((MyNode) graphNode.getData(), graphNode);

					System.out.println("New condition " + s);

					this.setJungGraph(jungGraph);
					// this.getViewer().setInput(createGraphNodes(jungGraph));
					this.updateRightComposite(jungGraph);
					this.setFocus();
				}
			} else {
				final Display disp = Display.getCurrent();
				disp.syncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
								"This is not a valid name.");
					}
				});
			}
		}
	}

	@Inject
	UISynchronize sync;

	/**
	 * <h1>addNewOperation</h1> Add a new Operation with its IO. For this, the
	 * method calls <code>SelectionWindowOp</code> method in order to display a
	 * selection window to the user with all the repository operations.
	 */
	private void addNewOperation() {

		if (jungGraph == null) {
			final Display disp = Display.getCurrent();
			disp.syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openInformation(disp.getActiveShell(), "Warning", "Create a new workflow first!");
				}
			});

		} else {

			final Shell shell = new Shell();
			ILabelProvider labelProvider = new LabelProvider() {

				public String getText(Object element) {
					if (element instanceof Operation)
						return ((Operation) element).getName().toString();
					else
						return "";
				}
			};
			final ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, labelProvider);

			final Display disp = Display.getCurrent();
			AddNewOperationJob = new Job("Add new operation") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Loading operations...", IProgressMonitor.UNKNOWN);

					try {

						Algorithm.init();
						// final ArrayList<Operation> operations =
						// Algorithm.importServices(
						// "D:/web-service-composition-Maven-plugin/web-service-composition/eu.scasefp7.eclipse.serviceComposition/data/WS.owl",
						// "data/scripts/");
						final ArrayList<Operation> operations = Algorithm.importServices(
								ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/" + "WS.owl");
						ArrayList<Operation> nonPrototypeOperations = new ArrayList<Operation>();
						// final ArrayList<Operation> operations = Algorithm
						// .importServices("data/WS.owl", "data/scripts/");
						// final ArrayList<Operation> operations = Algorithm
						// .importServices(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
						// +"/"+"WS.owl", "data/scripts/");
						final ArrayList<String> list = new ArrayList<String>();
						// String option = "addnode";
						for (Operation op : operations) {

							if (!op.isPrototype()) {
								String opName = op.getName().toString();
								list.add(opName);
								nonPrototypeOperations.add(op);
							}

						}

						// Open selection window
						disp.syncExec(new Runnable() {
							public void run() {
								SelectionWindowOp(shell, dialog, nonPrototypeOperations, list);
							}
						});

						monitor.done();
						return Status.OK_STATUS;
					} catch (Exception ex) {
						ex.printStackTrace();
						return Status.OK_STATUS;
					}

				}

			};
			AddNewOperationJob.schedule();
		}

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
	 * <h1>SelectionWindowNode</h1>Displays a selection window with a list of
	 * nodes.
	 * 
	 * @param list
	 *            : the nodes to choose from
	 * @param option
	 *            : match IOs or link operation/condition/StartNode/EndNode
	 */
	public void SelectionWindowNode(ArrayList<OwlService> list, String option) {

		Shell shell = new Shell();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new LabelProvider());

		dialog.setElements(list.toArray());

		if (option == "link") {
			dialog.setTitle("Link to..");
			dialog.setMessage("Please choose a node");
		} else if (option == "matchinput") {
			dialog.setTitle("Match this input with..");
			dialog.setMessage("Please select an appropriate output:");
		} else if (option == "matchoutput") {
			dialog.setTitle("Match this output with..");
			dialog.setMessage("Please select an appropriate input:");
		}

		// user pressed OK
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();

			if (option == "link") {
				for (Object selectedItem : result) {
					linkNode(selectedItem);
				}
			} else if (option == "matchinput") {
				for (Object selectedItem : result) {
					addEdge(selectedItem, option);
				}
			} else if (option == "matchoutput") {
				for (Object selectedItem : result) {
					addEdge(selectedItem, option);
				}
			}

		}

	}

	/**
	 * <h1>addEdge</h1>Add an edge in order to create a link between an input
	 * and an output variable. The link is always directed from the output to
	 * the input. Match IOs
	 * 
	 * @param selectedItem
	 * @param option
	 */
	private void addEdge(Object selectedItem, String option) {
		OwlService source = (OwlService) ((MyNode) selectedGraphNode.getData()).getObject();
		Collection<OwlService> services = new ArrayList<OwlService>(jungGraph.getVertices());
		// String targetName = selectedItem.toString();
		// Similarity.ComparableName targetComparableName = new
		// Similarity.ComparableName(targetName);
		for (OwlService service : services) {
			if (service.equals(selectedItem)) {
				if (option == "matchinput") {
					// Add link to Jung Graph (the edge should be from an output
					// to an input)
					jungGraph.addEdge(new Connector(service, source, ""), service, source, EdgeType.DIRECTED);

					Graph graph = viewer.getGraphControl();
					for (int j = 0; j < graph.getNodes().size(); j++) {
						GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
						if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(service)) {

							MyConnection connect = new MyConnection(graphNode.toString() + selectedGraphNode.toString(),
									"", (MyNode) graphNode.getData(), (MyNode) selectedGraphNode.getData());
							((MyNode) graphNode.getData()).getLinkedConnections().add(connect);
							((MyNode) graphNode.getData()).getConnections().add((MyNode) selectedGraphNode.getData());

							// EntityConnectionData connectionData = new
							// EntityConnectionData(
							// (MyNode) graphNode.getData(), (MyNode)
							// selectedGraphNode.getData());
							// viewer.addRelationship(connectionData, (MyNode)
							// graphNode.getData(),
							// (MyNode) selectedGraphNode.getData());

							GraphConnection graphConnection = new GraphConnection(graph, SWT.NONE, graphNode,
									selectedGraphNode);
							// MyNode dest = (MyNode)
							// graphConnection.getDestination().getData();
							// MyNode src = (MyNode)
							// graphConnection.getSource().getData();
							EntityConnectionData connectionData = new EntityConnectionData((MyNode) graphNode.getData(),
									(MyNode) selectedGraphNode.getData());
							graphConnection.setData(connectionData);

							source.setisMatchedIO(true);
							OwlService destination = (OwlService) ((MyNode) graphNode.getData()).getObject();
							destination.setisMatchedIO(true);

							ZestLabelProvider labelProvider = new ZestLabelProvider();
							labelProvider.selfStyleNode((MyNode) graphNode.getData(), graphNode);
							labelProvider.selfStyleNode((MyNode) selectedGraphNode.getData(), selectedGraphNode);

						}

					}

					System.out.println("New link from output: " + service.getName() + " to input: " + source.getName());
				}
				if (option == "matchoutput") {
					// Add link to Jung Graph (the edge should be from an output
					// to an input)
					jungGraph.addEdge(new Connector(source, service, ""), source, service, EdgeType.DIRECTED);

					Graph graph = viewer.getGraphControl();
					for (int j = 0; j < graph.getNodes().size(); j++) {
						GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
						if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(service)) {

							MyConnection connect = new MyConnection(selectedGraphNode.toString() + graphNode.toString(),
									"", (MyNode) selectedGraphNode.getData(), (MyNode) graphNode.getData());
							((MyNode) selectedGraphNode.getData()).getLinkedConnections().add(connect);
							((MyNode) selectedGraphNode.getData()).getConnections().add((MyNode) graphNode.getData());

							// EntityConnectionData connectionData = new
							// EntityConnectionData(
							// (MyNode) selectedGraphNode.getData(), (MyNode)
							// graphNode.getData());
							// viewer.addRelationship(connectionData, (MyNode)
							// selectedGraphNode.getData(),
							// (MyNode) graphNode.getData());

							GraphConnection graphConnection = new GraphConnection(graph, SWT.NONE, selectedGraphNode,
									graphNode);
							// MyNode dest = (MyNode)
							// graphConnection.getDestination().getData();
							// MyNode src = (MyNode)
							// graphConnection.getSource().getData();
							EntityConnectionData connectionData = new EntityConnectionData(
									(MyNode) selectedGraphNode.getData(), (MyNode) graphNode.getData());
							graphConnection.setData(connectionData);

							source.setisMatchedIO(true);
							OwlService destination = (OwlService) ((MyNode) graphNode.getData()).getObject();
							destination.setisMatchedIO(true);

							ZestLabelProvider labelProvider = new ZestLabelProvider();
							labelProvider.selfStyleNode((MyNode) graphNode.getData(), graphNode);
							labelProvider.selfStyleNode((MyNode) selectedGraphNode.getData(), selectedGraphNode);
						}
					}

					System.out.println("New link from output: " + service.getName() + " to input: " + source.getName());
				}

				this.setJungGraph(jungGraph);
				// this.getViewer().setInput(createGraphNodes(jungGraph));
				this.updateRightComposite(jungGraph);
				this.setFocus();
				break;
			}
		}
	}

	/**
	 * <h1>linkNode</h1>Link selected node with another node. It is used for non
	 * IO nodes.
	 * 
	 * @param selectedItem
	 */
	private void linkNode(Object selectedItem) {

		OwlService source = (OwlService) ((MyNode) selectedGraphNode.getData()).getObject();
		// String targetName = selectedItem.toString();
		// Similarity.ComparableName targetComparableName = new
		// Similarity.ComparableName(targetName);
		Collection<OwlService> services = new ArrayList<OwlService>(jungGraph.getVertices());
		// Graph graph = viewer.getGraphControl();

		for (OwlService service : services) {
			if (service.equals(selectedItem)) {
				if (source.getType().contains("Action") || source.getType().contains("StartNode")) {

					// OwlService target = new OwlService(service);
					// String condition = "";

					// Add link to Jung Graph
					jungGraph.addEdge(new Connector(source, service, ""), source, service, EdgeType.DIRECTED);
					// Add link to Zest Graph
					Graph graph = viewer.getGraphControl();
					for (int j = 0; j < graph.getNodes().size(); j++) {
						GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
						if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(service)) {

							MyConnection connect = new MyConnection(selectedGraphNode.toString() + graphNode.toString(),
									"", (MyNode) selectedGraphNode.getData(), (MyNode) graphNode.getData());
							((MyNode) selectedGraphNode.getData()).getLinkedConnections().add(connect);
							((MyNode) selectedGraphNode.getData()).getConnections().add((MyNode) graphNode.getData());

							// EntityConnectionData connectionData = new
							// EntityConnectionData(
							// (MyNode) selectedGraphNode.getData(), (MyNode)
							// graphNode.getData());
							// viewer.addRelationship(connectionData, (MyNode)
							// selectedGraphNode.getData(),
							// (MyNode) graphNode.getData());

							GraphConnection graphConnection = new GraphConnection(graph, SWT.NONE, selectedGraphNode,
									graphNode);
							// MyNode dest = (MyNode)
							// graphConnection.getDestination().getData();
							// MyNode src = (MyNode)
							// graphConnection.getSource().getData();
							EntityConnectionData connectionData = new EntityConnectionData(
									(MyNode) selectedGraphNode.getData(), (MyNode) graphNode.getData());
							graphConnection.setData(connectionData);

						}
					}

				} else if (source.getType().contains("Condition")) {
					Shell shell = new Shell();
					RenameConditionDialog dialog = new RenameConditionDialog(shell);
					// System.out.println(dialog.open());
					String s = dialog.open();
					boolean exist = false;
					// If a string was returned, say so.
					if ((s != null) && (s.trim().length() > 0)) {

						for (int j = 0; j < jungGraph.getOutEdges(source).size(); j++) {
							if (jungGraph.getOutEdges(source).toArray()[j].toString().equalsIgnoreCase(s)) {

								final Display disp = Display.getCurrent();
								disp.syncExec(new Runnable() {
									@Override
									public void run() {
										MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
												"Name already exists.");
									}
								});
								exist = true;
								break;
							}
						}
						if (!exist) {
							// Add link to Jung Graph
							jungGraph.addEdge(new Connector(source, service, s), source, service, EdgeType.DIRECTED);

							// Add link to Zest Graph
							Graph graph = viewer.getGraphControl();
							for (int j = 0; j < graph.getNodes().size(); j++) {
								GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
								if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(service)) {

									MyConnection connect = new MyConnection(
											selectedGraphNode.toString() + graphNode.toString(), s,
											(MyNode) selectedGraphNode.getData(), (MyNode) graphNode.getData());
									((MyNode) selectedGraphNode.getData()).getLinkedConnections().add(connect);
									((MyNode) selectedGraphNode.getData()).getConnections()
											.add((MyNode) graphNode.getData());

									// EntityConnectionData connectionData = new
									// EntityConnectionData(
									// (MyNode) selectedGraphNode.getData(),
									// (MyNode) graphNode.getData());
									// viewer.addRelationship(connectionData,
									// (MyNode) selectedGraphNode.getData(),
									// (MyNode) graphNode.getData());

									GraphConnection graphConnection = new GraphConnection(graph, SWT.NONE,
											selectedGraphNode, graphNode);
									graphConnection.setText(s);
									// MyNode dest = (MyNode)
									// graphConnection.getDestination().getData();
									// MyNode src = (MyNode)
									// graphConnection.getSource().getData();
									EntityConnectionData connectionData = new EntityConnectionData(
											(MyNode) selectedGraphNode.getData(), (MyNode) graphNode.getData());
									graphConnection.setData(connectionData);

								}
							}

							this.setJungGraph(jungGraph);
							// this.getViewer().setInput(createGraphNodes(jungGraph));
							this.updateRightComposite(jungGraph);
							this.setFocus();
						}

					} else {
						final Display disp = Display.getCurrent();
						disp.syncExec(new Runnable() {

							@Override
							public void run() {
								MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
										"This is not a valid name.");
							}

						});
					}
				}
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
	private void addNode(Object selectedItem, ArrayList<Operation> operations) {
		String operationName = selectedItem.toString();
		Similarity.ComparableName operationComparableName = new Similarity.ComparableName(operationName);
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				edu.uci.ics.jung.graph.Graph<OwlService, Connector> tempGraph = new SparseMultigraph<OwlService, Connector>();
				tempGraph.addVertex(owlService);
				Transformer tempTransformer = new Transformer(tempGraph);
				try {
					// Add the io variables of the operation
					tempTransformer.expandOperations(owlService);

				} catch (Exception e) {
					// TODO Auto-generated catch block
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

	/**
	 * Remove the outputs of operation <code>node</node>
	 * 
	 * @param node
	 */
	private void recursivelyRemoveOutputs(MyNode node) {
		if (node.getLinkedConnections().size() != 0) {
			for (int i = 0; i < node.getLinkedConnections().size(); i++) {

				MyConnection con = node.getLinkedConnections().get(i);
				if (con.getSource().equals(node)) {
					MyNode tmpNode = con.getDestination();
					if (((OwlService) tmpNode.getObject()).getType().equals("Property")
							&& !(((OwlService) tmpNode.getObject()).getisMatchedIO()
									&& ((OwlService) node.getObject()).getisMatchedIO())) {

						recursivelyRemoveOutputs(tmpNode);

					}
				}
			}
		}

		if (((OwlService) node.getObject()).getType().equals("Property")) {

			boolean change = false;
			ArrayList<OwlService> changedNodes = new ArrayList<OwlService>();
			// before removing n1 check if it is matched io and set the
			// remaining matched io to false
			if (((OwlService) node.getObject()).getisMatchedIO()) {
				for (int k = jungGraph.getSuccessorCount((OwlService) node.getObject()) - 1; k >= 0; k--) {
					OwlService n2 = (OwlService) jungGraph.getSuccessors((OwlService) node.getObject()).toArray()[k];
					// If node has successors they will be matched variables so
					// it is an unnecessary check.
					if (n2.getisMatchedIO()) {
						n2.setisMatchedIO(false);
						changedNodes.add(n2);
						change = true;

					}
				}

			}

			Graph graph = viewer.getGraphControl();
			for (int j = 0; j < graph.getNodes().size(); j++) {
				GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
				// Check if the properties of the node have changed and update
				// its style.
				for (OwlService changedNode : changedNodes) {
					if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(changedNode)
							&& change == true) {
						// Style Node
						ZestLabelProvider labelProvider = new ZestLabelProvider();
						labelProvider.selfStyleNode((MyNode) graphNode.getData(), graphNode);
					}
				}
				// Check if the node is the one which should be removed.
				if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals((OwlService) node.getObject())) {
					// if (!isNodeMatchedIO(graphNode)) {
					// remove jung node
					jungGraph.removeVertex((OwlService) node.getObject());
					// remove zest node
					graphNode.dispose();

					// } else {
					// ((OwlService) ((MyNode)
					// graphNode.getData()).getObject()).setisMatchedIO(false);

					// }
				}

			}
		}

		// Graph graph = viewer.getGraphControl();
		// for (int j = 0; j < graph.getNodes().size(); j++) {
		// GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
		// if (((OwlService) ((MyNode)
		// graphNode.getData()).getObject()).equals((OwlService)
		// node.getObject())) {
		// if (!isNodeMatchedIO(graphNode)) {
		// // remove jung node
		// jungGraph.removeVertex((OwlService) node.getObject());
		// // remove zest node
		// graphNode.dispose();
		//
		// } else {
		// ((OwlService) ((MyNode)
		// graphNode.getData()).getObject()).setisMatchedIO(false);
		//
		// }
		// }
		// }

		// Graph graph = viewer.getGraphControl();
		// for (int j = 0; j < graph.getNodes().size(); j++) {
		// GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
		// if (((OwlService) ((MyNode)
		// graphNode.getData()).getObject()).equals((OwlService)
		// node.getObject())) {
		// if (!isNodeMatchedIO(graphNode)) {
		// // remove zest node
		// graphNode.dispose();
		// // remove jung node
		// jungGraph.removeVertex((OwlService) node.getObject());
		// } else {
		// ((OwlService) ((MyNode)
		// graphNode.getData()).getObject()).setisMatchedIO(false);
		//
		// }
		// }
		// }

	}

	/**
	 * <h1>isNodeMatchedIO</h1>
	 * 
	 * @param graphNode
	 * @return the boolean value of the <code>isMatchedIO</code> variable of an
	 *         OwlService.
	 */
	private boolean isNodeMatchedIO(GraphNode graphNode) {

		return ((OwlService) ((MyNode) graphNode.getData()).getObject()).getisMatchedIO();

		// if(graphNode.getSourceConnections().size()==0)
		// return false;
		// else
		// if(graphNode.getSourceConnections().size()>0&&
		// !((OwlService)((MyNode)((GraphConnection)graphNode.getSourceConnections().get(0)).getDestination().getData()).getObject()).getType().equals("Action"))
		//
		// return false;
		// else
		// return true;

	}

	/**
	 * <h1>removeNode</h1>Remove operation node and all its IOs.
	 * 
	 * @param node
	 * @param mode
	 */
	private void removeNode(GraphNode node, String mode) {

		if (mode == "Operation") {
			// find and remove all inputs
			if (jungGraph.getPredecessorCount((OwlService) ((MyNode) node.getData()).getObject()) > 0)
				for (int i = jungGraph.getPredecessorCount((OwlService) ((MyNode) node.getData()).getObject())
						- 1; i >= 0; i--) {
					OwlService n1 = (OwlService) jungGraph
							.getPredecessors((OwlService) ((MyNode) node.getData()).getObject()).toArray()[i];

					if (n1.getType().equals("Property")) {

						// before removing n1 check if it is matched input and
						// set
						// the
						// remaining matched output to false (if remaining
						// output is matched with another input keep it as it
						// is)
						// OwlService changedNode = null;
						OwlService previouslyMatchedNode = null;
						boolean keep = false;
						if (n1.getisMatchedIO()) {
							// An input can be link with only one output so
							// PredecessorCount is maximum 1.
							for (int k = jungGraph.getPredecessorCount(n1) - 1; k >= 0; k--) {
								OwlService n2 = (OwlService) jungGraph.getPredecessors(n1).toArray()[k];
								previouslyMatchedNode = n2;
								if (n2.getisMatchedIO()) {
									for (int l = jungGraph.getSuccessorCount(n2) - 1; l >= 0; l--) {
										OwlService n3 = (OwlService) jungGraph.getSuccessors(n2).toArray()[l];
										if (n3.getisMatchedIO() && n3 != n1) {
											keep = true;
										}
									}
									if (!keep) {
										n2.setisMatchedIO(false);
										// changedNode = n2;
									}
								}
							}
						}

						Graph graph = viewer.getGraphControl();
						for (int j = 0; j < graph.getNodes().size(); j++) {
							GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
							if (previouslyMatchedNode != null) {
								if (((OwlService) ((MyNode) graphNode.getData()).getObject())
										.equals(previouslyMatchedNode)) {
									// Remove input node to be deleted from the
									// connections and linked connections of its
									// matched output node.
									for (int k = 0; k < ((MyNode) graphNode.getData()).getConnections().size(); k++) {
										if (((OwlService) ((MyNode) graphNode.getData()).getConnections().get(k)
												.getObject()).equals(n1)) {
											((MyNode) graphNode.getData()).getConnections().remove(k);
										}
									}
									for (int k = 0; k < ((MyNode) graphNode.getData()).getLinkedConnections()
											.size(); k++) {
										if (((OwlService) ((MyNode) graphNode.getData()).getLinkedConnections().get(k)
												.getDestination().getObject()).equals(n1)) {
											((MyNode) graphNode.getData()).getLinkedConnections().remove(k);
										}
									}
									// If properties of the matched output node
									// have changed update its style.
									if (!keep) {
										// Style Node
										ZestLabelProvider labelProvider = new ZestLabelProvider();
										labelProvider.selfStyleNode((MyNode) graphNode.getData(), graphNode);
									}
								}

							}

							// Delete input node.
							if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(n1)) {
								// if (!isNodeMatchedIO(graphNode)) {
								graphNode.dispose();
								jungGraph.removeVertex(n1);

								// } else {
								// ((OwlService) ((MyNode)
								// graphNode.getData()).getObject()).setisMatchedIO(false);

								// }
							}
						}
					}
				}

			// find and remove all outputs
			recursivelyRemoveOutputs((MyNode) node.getData());
		}

		// remove this node from linked connections and connections of the
		// node(s) that it was
		// connected.
		Graph graph = viewer.getGraphControl();
		for (int i = 0; i < graph.getNodes().size(); i++) {
			GraphNode graphNode = (GraphNode) graph.getNodes().get(i);
			for (int j = 0; j < ((MyNode) graphNode.getData()).getLinkedConnections().size(); j++) {
				if (((MyNode) graphNode.getData()).getLinkedConnections().get(j).getDestination()
						.equals(node.getData())) {
					((MyNode) graphNode.getData()).getLinkedConnections().remove(j);
				}
			}
			if (((MyNode) graphNode.getData()).getConnections().contains(node.getData())) {

				((MyNode) graphNode.getData()).getConnections().remove(node.getData());
			}
		}

		// remove selected node
		jungGraph.removeVertex((OwlService) ((MyNode) node.getData()).getObject());
		node.dispose();

		// remove selected node
		// Graph graph = viewer.getGraphControl();
		// MyNode node1=(MyNode) node.getData();
		// for (int j = 0; j < graph.getNodes().size(); j++) {
		// GraphNode graphNode = (GraphNode) graph.getNodes().get(j);
		// if (((OwlService) ((MyNode)
		// graphNode.getData()).getObject()).equals((OwlService)
		// node1.getObject())) {
		// //remove zest node
		// graphNode.dispose();
		// //remove jung node
		// jungGraph.removeVertex((OwlService) node1.getObject());
		// }
		// }
		// createGraphNodes(jungGraph);
		this.setJungGraph(jungGraph);
		// this.getViewer().setInput(createGraphNodes(jungGraph));
		updateRightComposite(jungGraph);
		this.setLayout();
	}

	/**
	 * <h1>removeEdge</h1> Remove selected edge from the graph.
	 * 
	 * @param edge
	 */
	private void removeEdge(GraphConnection edge) {
		// remove edge from jung graph
		for (int i = 0; i < jungGraph.getEdges().size(); i++) {
			Connector con = (Connector) jungGraph.getEdges().toArray()[i];
			if (((MyNode) edge.getSource().getData()).getObject().equals(con.getSource())
					&& ((MyNode) edge.getDestination().getData()).getObject().equals(con.getTarget())) {
				// remove edge from jung graph

				jungGraph.removeEdge(con);
				// if edge connects matched IO set isMatchedIO to false.
				if (isNodeMatchedIO(edge.getSource()) && isNodeMatchedIO(edge.getDestination())) {
					OwlService source = (OwlService) ((MyNode) edge.getSource().getData()).getObject();
					boolean keep = false;
					for (int l = jungGraph.getSuccessorCount(source) - 1; l >= 0; l--) {
						OwlService successor = (OwlService) jungGraph.getSuccessors(source).toArray()[l];
						if (successor.getisMatchedIO()) {
							keep = true;
						}
					}
					if (!keep) {
						((OwlService) ((MyNode) edge.getSource().getData()).getObject()).setisMatchedIO(false);

						// Style Node
						ZestLabelProvider labelProvider = new ZestLabelProvider();
						labelProvider.selfStyleNode((MyNode) edge.getSource().getData(), edge.getSource());
					}
					((OwlService) ((MyNode) edge.getDestination().getData()).getObject()).setisMatchedIO(false);

					// Style Node
					ZestLabelProvider labelProvider = new ZestLabelProvider();
					labelProvider.selfStyleNode((MyNode) edge.getDestination().getData(), edge.getDestination());
				}

				// remove destination node from source node's linked
				// connections and connections(zest).
				for (int j = 0; j < ((MyNode) edge.getSource().getData()).getLinkedConnections().size(); j++) {
					if (((MyNode) edge.getSource().getData()).getLinkedConnections().get(j).getDestination()
							.equals((MyNode) edge.getDestination().getData())) {
						((MyNode) edge.getSource().getData()).getLinkedConnections()
								.remove(((MyNode) edge.getSource().getData()).getLinkedConnections().get(j));
					}
				}
				if (((MyNode) edge.getSource().getData()).getConnections()
						.contains((MyNode) edge.getDestination().getData())) {
					((MyNode) edge.getSource().getData()).getConnections()
							.remove((MyNode) edge.getDestination().getData());
				}
				// remove edge from zest graph

				edge.dispose();
				this.setJungGraph(jungGraph);
				// this.getViewer().setInput(createGraphNodes(jungGraph));
				this.updateRightComposite(jungGraph);
				this.setFocus();

				// viewer.removeRelationship(edge.getData());
				// for (int k=0; k<viewer.getConnectionElements().length; k++){
				// GraphConnection connection = (GraphConnection)
				// viewer.getConnectionElements()[k];
				// if ((connection != null)&&
				// (connection.equals(edge.getData())) ){
				// viewer.connectionsMap;
				// if (!connection.isDisposed()) {
				// connection.dispose();
				// }
				// }
				// }

				// edge.dispose();

				break;
			}
		}

		this.setJungGraph(jungGraph);
		// this.getViewer().setInput(createGraphNodes(jungGraph));
		updateRightComposite(jungGraph);

	}

	public List<MyNode> createGraphNodes(OwlService owlService) {

		List<MyNode> nodes = new ArrayList<MyNode>();
		MyNode operationNode = new MyNode(owlService.toString(), owlService.toString(), owlService);
		nodes.add(operationNode);

		for (OwlService input : jungGraph.getPredecessors(owlService)) {
			MyNode inputNode = new MyNode(input.toString(), input.toString(), input);
			nodes.add(inputNode);

		}

		for (OwlService output : jungGraph.getSuccessors(owlService)) {
			nodes = createOutputNodes(output, nodes);
			// MyNode outputNode = new MyNode(output.toString(),
			// output.toString(), output);
			// nodes.add(outputNode);
			// for (OwlService subOutput : jungGraph.getSuccessors(output)) {
			// MyNode subOutputNode = new MyNode(subOutput.toString(),
			// subOutput.toString(), subOutput);
			// nodes.add(subOutputNode);
			// }

		}

		// create all edges
		List<MyConnection> connections = new ArrayList<MyConnection>();
		Collection<Connector> inEdges = jungGraph.getInEdges(owlService);
		Collection<Connector> outEdges = jungGraph.getOutEdges(owlService);
		Collection<Connector> subEdges = null;

		for (OwlService output : jungGraph.getSuccessors(owlService)) {
			connections = createOutputEdges(output, nodes, connections);

			// if (jungGraph.getOutEdges(output) != null) {
			// subEdges = jungGraph.getOutEdges(output);
			// }
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

		// if (subEdges != null) {
		// for (Connector con : subEdges) {
		// OwlService jungSource = (OwlService) con.getSource();
		// OwlService jungDest = (OwlService) con.getTarget();
		// MyNode source = null;
		// MyNode dest = null;
		//
		// for (int j = 0; j < nodes.size(); j++) {
		// MyNode node = nodes.get(j);
		// if (((OwlService) node.getObject()).equals(jungSource)) {
		// source = node;
		// } else if (((OwlService) node.getObject()).equals(jungDest)) {
		// dest = node;
		// }
		// }
		//
		// MyConnection connect = new MyConnection(source.toString() +
		// dest.toString(), con.getCondition(),
		// source, dest);
		// source.getLinkedConnections().add(connect);
		// connections.add(connect);
		//
		// }
		// }
		for (MyConnection connection : connections) {
			connection.getSource().getConnectedTo().add(connection.getDestination());
		}
		return nodes;
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
		bars.getMenuManager().add(toolbarZoomContributionViewItem);
		runWorkflowAction = new Action("Run workflow") {
			public void run() {

				try {
					// clean outputs
					cleanOutputs();
					runWorkflow();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		runWorkflowAction.setImageDescriptor(getImageDescriptor("icons/run.png"));
		// runWorkflowAction.setEnabled(false);
		Action cancelWorkflowAction = new Action("Stop execution of workflow") {
			public void run() {

				try {
					runWorkflowJob.cancel();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}

			}
		};
		cancelWorkflowAction.setImageDescriptor(getImageDescriptor("icons/stop.png"));

		newWorkflowAction = new Action("Create a new workflow") {
			public void run() {

				try {
					// clean outputs
					cleanOutputs();
					createNewWorkflow();

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
					final Display disp = Display.getCurrent();
					IStatus status = checkGraph(jungGraph, disp);
					if (status.getMessage().equalsIgnoreCase("OK")) {
						generate();
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		generateCodeAction.setImageDescriptor(getImageDescriptor("icons/java.png"));

		Action uploadOnServerAction = new Action("Upload RESTful web service on server.") {
			public void run() {

				try {

					if (projectName.trim().length() > 0 && pomPath.trim().length() > 0) {
						install(pomPath);
						// uploadOnServer();
					} else {
						final Display disp = Display.getCurrent();
						disp.syncExec(new Runnable() {
							@Override
							public void run() {
								MessageDialog.openInformation(disp.getActiveShell(), "Warning",
										"Create a RESTful project first!");
							}
						});

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		uploadOnServerAction.setImageDescriptor(getImageDescriptor("icons/database.png"));

		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		mgr.add(uploadOnServerAction);
		mgr.add(generateCodeAction);
		mgr.add(displayCostAction);
		mgr.add(newWorkflowAction);
		mgr.add(runWorkflowAction);
		mgr.add(cancelWorkflowAction);

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
		final Display disp = Display.getCurrent();
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

	private void cleanOutputs() {
		try {
			for (int j = 0; j < outputsComposite.getChildren().length; j++) {
				if (outputsComposite.getChildren()[j] instanceof Text) {
					Text text = (Text) outputsComposite.getChildren()[j];
					text.setText("");
				}
			}
		} catch (Exception ex) {

		}
		for (OwlService service : jungGraph.getVertices()) {
			if (service.getOperation() != null) {
				for (Argument output : service.getOperation().getOutputs()) {
					int size = output.getElements().size();
					cleanElements(output, size, outputsComposite);
					int size2 = output.getSubtypes().size();
					cleanElements(output, size2, outputsComposite);
				}
			}
		}

	}

	private void cleanElements(Argument output, int size, Object obj) {

		// for (int i=0; i<output.getSubtypes().size(); i++) {
		// int sizeb = output.getSubtypes().get(i).getElements().size();
		// TreeItem item ;
		// if (obj instanceof Tree) {
		// item = (TreeItem) ((Tree) obj).getItem(i);
		// } else {
		// item = (TreeItem) ((TreeItem) obj).getItem(i);
		// }
		// cleanElements(output.getSubtypes().get(i), sizeb, item);
		// }
		if (obj instanceof Tree) {
			for (int j = 0; j < ((Tree) obj).getItemCount(); j++) {
				if (((Tree) obj).getItem(j) instanceof TreeItem) {
					TreeItem item = (TreeItem) ((Tree) obj).getItem(j);
					if (item.getText().split("\\[")[0].trim().equals(output.getName().toString())) {
						if (output.isArray()) {
							disposeElements(item, item.getItemCount());

						} else {
							if (!output.getSubtypes().isEmpty()) {
								disposeElements(item, item.getItemCount());
							}
						}
					}
				}
			}
		} else {
			for (int j = 0; j < ((TreeItem) obj).getItemCount(); j++) {
				if (((TreeItem) obj).getItem(j) instanceof TreeItem) {
					TreeItem item = (TreeItem) ((TreeItem) obj).getItem(j);
					if (item.getText().split("\\[")[0].trim().equals(output.getName().toString())) {
						if (output.isArray()) {
							disposeElements(item, item.getItemCount());
						} else {
							if (!output.getSubtypes().isEmpty()) {
								disposeElements(item, item.getItemCount());
							}
						}
					}
				}
			}
		}
		// if (output.isArray()) {
		// for (int i = 0; i < size; i++) {
		// output.getElements().remove(i);
		// }
		// }

	}

	private void disposeElements(TreeItem item, int size) {
		for (int k = 0; k < size; k++) {
			TreeItem item2 = (TreeItem) item.getItem(0);
			item2.dispose();
		}
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
		this.setFocus();

	}

	public void updateRightComposite(edu.uci.ics.jung.graph.Graph jungGraph) {

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

		// create inputs composite

		rightComposite.setLayout(new GridLayout());
		Composite inputsLabelComposite = new Composite(rightComposite, SWT.FILL);

		inputsLabelComposite.setLayout(new GridLayout());
		Label label = new Label(inputsLabelComposite, SWT.FILL);
		label.setText("Workflow Inputs:");
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setFont(JFaceResources.getFontRegistry().getBold(""));
		inputsComposite = new Composite(rightComposite, SWT.FILL);
		inputsComposite.setLayout(new GridLayout(2, false));
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

			if (node.getType().contains("Property")) {
				if (jungGraph.getInEdges(node).size() == 0) {

					showInputs(node, matchedNodes, graph);

				} else if ((jungGraph.getInEdges(node).size() == 1
						&& ((OwlService) jungGraph.getPredecessors(node).toArray()[0]).getisMatchedIO())) {
					showInputs(node, matchedNodes, graph);
				}
			}
		}

		// create UriParams composite

		rightComposite.setLayout(new GridLayout());
		Composite urisLabelComposite = new Composite(rightComposite, SWT.FILL);

		urisLabelComposite.setLayout(new GridLayout());
		Label label3 = new Label(urisLabelComposite, SWT.FILL);
		label3.setText("Workflow URI Parameters:");
		label3.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label3.setFont(JFaceResources.getFontRegistry().getBold(""));
		uriParamsComposite = new Composite(rightComposite, SWT.FILL);
		uriParamsComposite.setLayout(new GridLayout(2, false));

		// get all uriParams

		for (int i = 0; i < vertices.length; i++) {
			final OwlService node = (OwlService) vertices[i];

			if (node.getType().contains("Action")) {
				if (!node.getOperation().getUriParameters().isEmpty()) {
					for (Argument arg : node.getOperation().getUriParameters()) {
						showUriParams(arg);
					}
				}
			}
		}
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

		for (int i = 0; i < vertices.length; i++) {
			final OwlService node = (OwlService) vertices[i];

			if (node.getType().contains("Action")&& node.getOperation().getDomain()!=null) {
				if (node.getOperation().getDomain().getSecurityScheme() != null) {
					if (node.getOperation().getDomain().getSecurityScheme().equalsIgnoreCase("Basic Authentication")) {
						showBasicAuthenticationParams();
					}
				}
			}
		}

		// create outputs composite

		Composite outputsLabelComposite = new Composite(rightComposite, SWT.FILL);
		outputsLabelComposite.setLayout(new GridLayout(1, false));
		Label label2 = new Label(outputsLabelComposite, SWT.NONE);
		label2.setText("Workflow Outputs:");
		label2.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label2.setFont(JFaceResources.getFontRegistry().getBold(""));
		outputsComposite = new Tree(rightComposite, SWT.FILL | SWT.MULTI);
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
						showOutputs(successor, null, nodes, column2, jungGraph);

					}
				}
			}
		}

		treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		outputsComposite.setSize(300, 200);
		treeViewer.setContentProvider(new MyTreeContentProvider());

		column1.setLabelProvider(new MyLabelProvider());
		treeViewer.setInput(nodes);
		// outputsComposite.setSize(300, nodes.size() * 10);
		treeViewer.expandAll();

		Listener listener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				TreeItem treeItem = (TreeItem) event.item;
				final TreeColumn[] treeColumns = treeItem.getParent().getColumns();
				final Display display = Display.getCurrent();
				display.asyncExec(new Runnable() {

					@Override
					public void run() {
						for (TreeColumn treeColumn : treeColumns)
							treeColumn.pack();
					}
				});
			}
		};

		outputsComposite.addListener(SWT.Expand, listener);

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

		sc.update();
		sc.redraw();
		this.rightComposite.update();
		this.rightComposite.redraw();

		sashForm.update();
		sashForm.redraw();
		sashForm.layout(true);

		this.showBusy(false);
	}

	class MyLabelProvider extends ColumnLabelProvider implements ILabelProvider {
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

	class MyTreeContentProvider implements ITreeContentProvider {
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

	private ColumnLabelProvider createColumnLabelProvider() {
		return new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return ((Node) element).getValue();
			}

		};
	}

	class Node {
		private String name;
		private String value;
		private OwlService service;
		private Value argument;
		private Vector<Node> subCategories;
		private Node parent;

		public Node(String name, Node parent, OwlService service, Value argument) {
			if (service != null) {
				this.name = name + " [" + service.getArgument().getType() + "]:";
			} else if (argument != null) {
				this.name = name + " [" + argument.getType() + "]:";
			}
			this.parent = parent;
			this.service = service;
			this.argument = argument;
			if (argument != null) {
				this.value = argument.getValue();
			} else if (service != null) {
				this.value = "";
			}

			if (parent != null)
				parent.addSubCategory(this);
		}

		public Vector<Node> getSubCategories() {
			return subCategories;
		}

		private void addSubCategory(Node subcategory) {
			if (subCategories == null)
				subCategories = new Vector<Node>();
			if (!subCategories.contains(subcategory))
				subCategories.add(subcategory);
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		public OwlService getOwlService() {
			return service;
		}

		public Value getArgument() {
			return argument;
		}

		public Node getParent() {
			return parent;
		}
	}

	public void showInputs(final OwlService arg, ArrayList<OwlService> matchedNodes, final Graph graph) {

		Label firstLabel = new Label(inputsComposite, SWT.NONE);
		if (arg.getArgument().isRequired()) {
			firstLabel.setText(arg.getName() + " [" + ((Argument) arg.getContent()).getType() + "]*" + ":");
		} else {
			firstLabel.setText(arg.getName() + " [" + ((Argument) arg.getContent()).getType() + "]" + ":");
		}
		firstLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		Text firstText = new Text(inputsComposite, SWT.BORDER);
		firstText.setText("");
		firstText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		for (OwlService matched : matchedNodes) {
			if (arg.equals(matched)) {
				firstText.setEditable(false);
			}
		}

		GridData data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;
		firstText.setLayoutData(data);
		// listener for highlighting nodes in the graph
		FocusListener focusListener = new FocusListener() {
			public void focusGained(FocusEvent e) {
				Text t = (Text) e.widget;
				t.selectAll();

				// highlight node in graph
				for (int i = 0; i < graph.getNodes().size(); i++) {
					GraphNode graphNode = (GraphNode) graph.getNodes().get(i);
					// System.out.println(graphNode);
					if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(arg)) {
						System.out.println("Selection={"
								+ ((OwlService) ((MyNode) graphNode.getData()).getObject()).getName().toString() + "}");
						graphNode.highlight();
					} else
						graphNode.unhighlight();
				}
			}

			public void focusLost(FocusEvent e) {
				for (int i = 0; i < graph.getNodes().size(); i++) {
					GraphNode graphNode = (GraphNode) graph.getNodes().get(i);
					// System.out.println(graphNode);
					if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(arg)) {
						graphNode.unhighlight();
						break;
					}
				}
			}
		};
		firstText.addFocusListener(focusListener);
		firstText.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				// check if all inputs are filled in
				boolean allFilledIn = true;
				for (int i = 0; i < inputsComposite.getChildren().length; i++) {
					if (inputsComposite.getChildren()[i] instanceof Text)
						if (((Text) inputsComposite.getChildren()[i]).getText().trim().equals(""))
							allFilledIn = false;
				}
				// if (allFilledIn)
				// runWorkflowAction.setEnabled(true);
				// else
				// runWorkflowAction.setEnabled(false);

			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});

	}

	public void showOutputs(final Object arg, Node parent, Vector<Node> nodes, TreeViewerColumn column,
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			// array of primitive
			if (((Value) arg).isArray() && RAMLCaller.stringIsItemFromList(((Value) arg).getType(), datatypes)) {
				for (Argument element : ((Value) arg).getElements()) {
					Node e = new Node(element.getName().toString(), n, n.getOwlService(), ((Value) element));
					column.setLabelProvider(createColumnLabelProvider());
					// TreeItem newItem = new TreeItem(item, SWT.NONE);
					// newItem.setText(1, ((Value) element).getValue());
				}
				// array of array or array of objects
			} else
				if (((Value) arg).isArray() && !RAMLCaller.stringIsItemFromList(((Value) arg).getType(), datatypes)) {
				for (Argument element : ((Value) arg).getElements()) {
					Node e = new Node(element.getName().toString(), n, ((Value) arg).getOwlService(),
							((Value) element));
					column.setLabelProvider(createColumnLabelProvider());
					// TreeItem newItem = new TreeItem(item, SWT.NONE);
					// newItem.setText(0, element.getName().toString());
					if (element.isArray()) {
						for (Argument el : ((Value) element).getElements()) {
							showOutputs(el, e, nodes, column, graph);
						}
						if (element.getElements().isEmpty()) {
							for (Argument sub : element.getSubtypes())
								showOutputs(sub, e, nodes, column, graph);
						}
					} else {
						for (Argument sub : element.getSubtypes())
							showOutputs(sub, e, nodes, column, graph);
					}

				}
				if (((Value) arg).getElements().isEmpty()) {
					for (Argument sub : ((Value) arg).getSubtypes())
						showOutputs(sub, n, nodes, column, graph);
				}
			} else if (!((Value) arg).isArray()
					&& !RAMLCaller.stringIsItemFromList(((Value) arg).getType(), datatypes)) {
				for (Argument sub : ((Value) arg).getSubtypes()) {
					// TreeItem newItem = new TreeItem(item, SWT.NONE);
					// newItem.setText(0, sub.getName().toString() + " [" +
					// sub.getType() +
					// "]:");
					showOutputs(sub, n, nodes, column, graph);
				}
			}

		} else {
			n = new Node(((OwlService) arg).getName().toString(), parent, (OwlService) arg, null);
			column.setLabelProvider(

			createColumnLabelProvider());

			if (!((OwlService) arg).getArgument().getSubtypes().isEmpty()) {
				Collection<OwlService> successors = (Collection<OwlService>) jungGraph.getSuccessors((OwlService) arg);
				for (OwlService successor : successors) {
					showOutputs(successor, n, nodes, column, graph);
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

	public void showUriParams(final Argument arg) {

		Label firstLabel = new Label(uriParamsComposite, SWT.NONE);
		firstLabel.setText(arg.getName() + " [" + arg.getType() + "]*" + ":");
		firstLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		Text firstText = new Text(uriParamsComposite, SWT.BORDER);
		firstText.setText("");
		firstText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		firstText.setEditable(true);

	}

	public void unhighlightAllNodes() {
		// unhighlight all nodes
		Graph graph = viewer.getGraphControl();
		for (int i = 0; i < graph.getNodes().size(); i++) {
			GraphNode graphNode = (GraphNode) graph.getNodes().get(i);
			graphNode.unhighlight();
		}
	}

	// @Inject
	// UISynchronize sync;

	public String runWorkflow() throws Exception {
		final Display disp = Display.getCurrent();
		runWorkflowJob = new Job("Running Workflow") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Workflow execution in progress ...", IProgressMonitor.UNKNOWN);

				try {
					final Graph destGraph = viewer.getGraphControl();
					disp.syncExec(new Runnable() {
						@Override
						public void run() {
							unhighlightAllNodes();
						}
					});
					Properties prop = new Properties();
					String propFileName = "matcher.properties";
					Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
					URL fileURL = bundle.getEntry(propFileName);

					try {
						File file = new File(FileLocator.resolve(fileURL).toURI());
						InputStream inputStream = new FileInputStream(file);
						prop.load(inputStream);
						VARIABLE_NAME_SIMILARITY_THRESHOLD = Double
								.parseDouble(prop.getProperty("interpreter.VARIABLE_NAME_SIMILARITY_THRESHOLD"));
						MAX_DISTANCE_BETWEEN_SOLUTIONS = Double
								.parseDouble(prop.getProperty("interpreter.MAX_DISTANCE_BETWEEN_SOLUTIONS"));
						VALUE_SIMILARITY_THRESHOLD = Double
								.parseDouble(prop.getProperty("interpreter.VALUE_SIMILARITY_THRESHOLD"));
					} catch (Exception e) {
						System.err.println("Error occured while trying to load matcher settings from " + propFileName);
					}
					edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph = new SparseMultigraph<OwlService, Connector>();

					// replace all services in the graph with implementations
					HashMap<OwlService, OwlService> implementations = new HashMap<OwlService, OwlService>();
					for (OwlService service : jungGraph.getVertices()) {
						if (service.getOperation() != null) {
							// System.out.println("replacing:
							// "+service.getOperation());
							OwlService created = new OwlService(service);
							Implementation test = new Implementation(service.getOperation());
							created.editContent(test);
							implementations.put(service, created);
							graph.addVertex(created);
							// System.out.println("with:
							// "+created.getOperation());
						} else if (service.getArgument() != null) {
							try {
								service.getArgument().assertCorrectSubtypes();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// They get the previous call value-empty for the
							// first time.
							OwlService created = new OwlService(service);
							created.getArgument().setOwlService(created);
							Value value = new Value(service.getArgument());
							// Value value =
							// (Value.getValue(service.getArgument()));
							// Clear old values
							value.setValue("");
							value.getElements().removeAll(value.getElements());
							created.editContent(value);

							// if (service.getisMatchedIO()) {
							// created.setisMatchedIO(true);
							// }
							implementations.put(service, created);
							graph.addVertex(created);
						} else {
							implementations.put(service, service);
							graph.addVertex(service);
						}
					}
					for (Connector connector : jungGraph.getEdges(EdgeType.DIRECTED)) {
						OwlService source = implementations.get(connector.getSource());
						OwlService target = implementations.get(connector.getTarget());
						if (graph.findEdge(source, target) == null)
							graph.addEdge(new Connector(source, target, connector.getCondition()), source, target,
									EdgeType.DIRECTED);
					}
					for (Connector connector : jungGraph.getEdges(EdgeType.UNDIRECTED)) {
						OwlService source = implementations.get(connector.getSource());
						OwlService target = implementations.get(connector.getTarget());
						if (graph.findEdge(target, source) == null)
							graph.addEdge(new Connector(target, source, connector.getCondition()), target, source,
									EdgeType.UNDIRECTED);
					}

					// initialize variable lists
					final ArrayList<Value> inputVariables = new ArrayList<Value>();
					final ArrayList<Value> outputVariables = new ArrayList<Value>();
					final ArrayList<Value> suboutputVariables = new ArrayList<Value>();
					final ArrayList<Value> uriParamVariables = new ArrayList<Value>();
					final ArrayList<Value> authParamVariables = new ArrayList<Value>();
					Vector<Node> nodes = new Vector<Node>();
					// ArrayList<Value> directAssignVariables = new
					// ArrayList<Value>();
					// detect all operations
					ArrayList<OwlService> remainingOperations = new ArrayList<OwlService>();
					// detect all variables(input and output variables could be
					// also detected here)
					ArrayList<Value> allVariables = new ArrayList<Value>();
					for (OwlService service : graph.getVertices()) {
						Operation op = service.getOperation();
						if (op != null) {
							remainingOperations.add(service);
							for (Argument arg : op.getInputs()) {
								if (!allVariables.contains(arg))
									allVariables.add((Value) arg);
								if (!inputVariables.contains(arg))
									inputVariables.add((Value) arg);
							}
							for (Argument arg : op.getOutputs()) {
								if (!allVariables.contains(arg)) {
									allVariables.add((Value) arg);
								}
								if (!outputVariables.contains(arg)) {
									outputVariables.add((Value) arg);
								}

								System.out.println("OUTPUT DETECTED\t " + arg);
								for (Argument sub : arg.getSubtypes()) {
									if (!allVariables.contains(sub)) {
										allVariables.add((Value) sub);
									}
									getSubtypes(suboutputVariables, sub, false);
								}

							}
							if (!op.getUriParameters().isEmpty()) {
								for (Argument arg : op.getUriParameters()) {
									uriParamVariables.add((Value) arg);
								}
							}
							if (!op.getAuthenticationParameters().isEmpty()) {
								for (Argument arg : op.getAuthenticationParameters()) {
									authParamVariables.add((Value) arg);
								}
							}
						}
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
							throw new Exception("StartNode was not detected on graph vertices");

						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

					if (endingService == null) {
						try {
							throw new Exception("EndNode was not detected on graph vertices");
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

					// Check if every node has a path to StartNode and to
					// EndNode and also a condition has two output edges in
					// order to allow execution.
					ArrayList<OwlService> previousList = new ArrayList<OwlService>();
					ArrayList<OwlService> nextList = new ArrayList<OwlService>();
					for (OwlService service : graph.getVertices()) {

						if (service.getType().contains("Action") || service.getType().contains("Condition")) {

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

							previousList = (new PathFinding(graph).findOperationPath(startingService, service));
							if (previousList.get(0) != startingService) {
								final OwlService unlinked = previousList.get(0);
								try {
									throw new Exception("\"" + unlinked.getName().toString() + "\""
											+ " has no path to StartNode. Check for unlinked operations.");
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								disp.syncExec(new Runnable() {
									@Override
									public void run() {
										MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
												"\"" + unlinked.getName().toString() + "\""
														+ " has no path to StartNode. Check for unlinked operations.");
									}
								});
								return Status.CANCEL_STATUS;
							}
							nextList = (new PathFinding(graph).findOperationPath(service, endingService));
							if (nextList.get(0) != service) {
								final OwlService unlinked = service;
								try {
									throw new Exception("\"" + unlinked.getName().toString() + "\""
											+ " has no path to EndNode. Check for unlinked operations.");
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}

								disp.syncExec(new Runnable() {
									@Override
									public void run() {
										MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
												"\"" + unlinked.getName().toString() + "\""
														+ " has no path to EndNode. Check for unlinked operations.");
									}
								});
								return Status.CANCEL_STATUS;

							}

						}
					}

					currentService = startingService;

					// get input values
					disp.syncExec(new Runnable() {
						@Override
						public void run() {
							if (currentService == startingService) {
								// fill inputs from right panel
								for (int i = 0; i < inputVariables.size(); i++) {
									Value var = inputVariables.get(i);

									for (int j = 0; j < inputsComposite.getChildren().length; j++) {
										if (inputsComposite.getChildren()[j] instanceof Label) {
											Label label = (Label) inputsComposite.getChildren()[j];
											if (label.getText().split("\\[")[0].trim()
													.equals(var.getName().toString())) {
												if (((Text) inputsComposite.getChildren()[j + 1]).isEnabled())
													var.setValue(
															((Text) inputsComposite.getChildren()[j + 1]).getText());
											}
										}
									}
								}
							}
						}
					});

					// get uri parameters values
					disp.syncExec(new Runnable() {
						@Override
						public void run() {
							if (currentService == startingService) {
								// fill URI params from right panel
								for (int i = 0; i < uriParamVariables.size(); i++) {
									Value var = uriParamVariables.get(i);

									for (int j = 0; j < uriParamsComposite.getChildren().length; j++) {
										if (uriParamsComposite.getChildren()[j] instanceof Label) {
											Label label = (Label) uriParamsComposite.getChildren()[j];
											if (label.getText().split("\\[")[0].trim()
													.equals(var.getName().toString())) {
												if (((Text) uriParamsComposite.getChildren()[j + 1]).isEnabled())
													var.setValue(
															((Text) uriParamsComposite.getChildren()[j + 1]).getText());
											}
										}
									}
								}

								// fill Authentication params from right panel
								for (int i = 0; i < authParamVariables.size(); i++) {
									Value var = authParamVariables.get(i);

									for (int j = 0; j < authParamsComposite.getChildren().length; j++) {
										if (authParamsComposite.getChildren()[j] instanceof Label) {
											Label label = (Label) authParamsComposite.getChildren()[j];
											if (label.getText().split("\\*")[0].trim()
													.equals(var.getName().toString())) {
												if (((Text) authParamsComposite.getChildren()[j + 1]).isEnabled())
													var.setValue(((Text) authParamsComposite.getChildren()[j + 1])
															.getText());
											}
										}
									}
								}
							}
						}
					});

					WSDLCaller callWSDL = new WSDLCaller();
					// PythonCaller callPython = new PythonCaller();
					RAMLCaller callRAML = new RAMLCaller();
					OwlService previousNode = null;
					// highlight nodes

					while (currentService != null) {
						long startTime = System.currentTimeMillis();

						if (monitor.isCanceled()) {
							disp.syncExec(new Runnable() {
								@Override
								public void run() {
									unhighlightAllNodes();
								}
							});
							return Status.CANCEL_STATUS;
						}
						// Highlight Nodes
						disp.syncExec(new Runnable() {
							@Override
							public void run() {
								unhighlightAllNodes();
								for (int i = 0; i < destGraph.getNodes().size(); i++) {
									GraphNode graphNode = (GraphNode) destGraph.getNodes().get(i);
									if (currentService.toString().equals("StartNode")) {
										if (((OwlService) ((MyNode) graphNode.getData()).getObject()).toString()
												.equals("StartNode")) {
											destGraph.setSelection(new GraphItem[] { graphNode });
											graphNode.highlight();
										}
									} else if (currentService.toString().equals("EndNode")) {
										if (((OwlService) ((MyNode) graphNode.getData()).getObject()).toString()
												.equals("EndNode")) {
											destGraph.setSelection(new GraphItem[] { graphNode });
											graphNode.highlight();
										}
									} else if (((OwlService) ((MyNode) graphNode.getData()).getObject()).equals(currentService)) {
										destGraph.setSelection(new GraphItem[] { graphNode });
										graphNode.highlight();
									}
								}

							}
						});

						// call the service
						if (currentService.getOperation() != null) {
							try {
								if (currentService.getOperation().getDomain() == null){
									disp.syncExec(new Runnable() {
										@Override
										public void run() {
											MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
													"Action \"" + currentService.getOperation().getName()
													+ "\" was not replaced. Please replace it manually with one of the available operations or modify and re-import the storyboard diagram before executing the workflow.");
										}
									});
									throw new Exception("Service " + currentService.getOperation().getName()
											+ " is missing its domain");
								}
								else if (currentService.getOperation().getType().equalsIgnoreCase("SOAP"))
									// else if
									// (!currentService.getOperation().getDomain().isLocal())
									callWSDL.callOperation(currentService.getOperation());
								else if (currentService.getOperation().getType().equalsIgnoreCase("RESTful")) {
									callRAML.callRESTfulOperation(currentService.getOperation());
								}
								/*
								 * else callPython.callOperation(currentService.
								 * getOperation());
								 */
							} catch (Exception e) {

								e.printStackTrace();
								disp.syncExec(new Runnable() {
									@Override
									public void run() {
										MessageDialog.openInformation(disp.getActiveShell(), "Invocation error occured",
												"An error occured during invocation of \"" + currentService.getName()
														+ "\"");
									}
								});
								return Status.CANCEL_STATUS;
							}

							ArrayList<Value> serviceOutputs = new ArrayList<Value>();
							for (Argument arg : currentService.getOperation().getOutputs()) {
								serviceOutputs.add((Value) arg);
							}

							if (!serviceOutputs.isEmpty()) {
								disp.syncExec(new Runnable() {
									@Override
									public void run() {
										fillInOutputValues(serviceOutputs, nodes, graph);
									}
								});
							}

							// Update matched variable values
							for (OwlService service : graph.getVertices()) {
								if (service.getisMatchedIO()) {
									if (graph.getSuccessorCount(service) > 0) {
										for (OwlService successor : graph.getSuccessors(service)) {
											if (successor.getisMatchedIO()) {

												((Value) successor.getArgument())
														.setValue(((Value) service.getArgument()).getValue());

											}
										}

									}
								}
							}

						}

						// select next service
						ArrayList<Value> previousServiceOutVariables = new ArrayList<Value>();
						HashMap<OwlService, Connector> possibleServices = new HashMap<OwlService, Connector>();
						for (OwlService next : graph.getSuccessors(currentService))
							if (next.getArgument() == null)
								possibleServices.put(next, graph.findEdge(currentService, next));
						previousNode = currentService;
						for (OwlService previousOperation : graph.getPredecessors(currentService)) {
							if (previousOperation.getOperation() != null) {
								for (Argument output : previousOperation.getOperation().getOutputs()) {
									if (!output.isArray()) {
										if (output.getSubtypes().isEmpty()) {
											previousServiceOutVariables.add((Value) output);
										}
										for (Argument sub : output.getSubtypes()) {
											if (!sub.isArray()) {
												getSubtypes(previousServiceOutVariables, sub, true);
											}

										}
									}
								}
							}
						}

						currentService = checkCondition(currentService, possibleServices, previousServiceOutVariables);
						// Update matched variable values 2
						if (currentService != null) {
							if (currentService.getOperation() != null) {
								for (OwlService service : graph.getVertices()) {
									boolean isMatched = false;
									for (int i = 0; i < currentService.getOperation().getInputs().size(); i++) {
										if (service.getArgument() != null) {
											for (OwlService successor : graph.getSuccessors(service)) {
												if (successor.getArgument() != null) {
													if (successor.getisMatchedIO() && service.getisMatchedIO()) {
														isMatched = true;
													}
													if (successor.getArgument()
															.getName().toString().equals(currentService.getOperation()
																	.getInputs().get(i).getName().toString())
															&& isMatched) {
														((Value) currentService.getOperation().getInputs().get(i))
																.setValue(((Value) service.getArgument()).getValue());
													}
												}
											}

										}
									}
								}
							}
						}

						long stopTime = System.currentTimeMillis();
						long elapsedTime = stopTime - startTime;
						if (elapsedTime < 500) {
							try {
								Thread.sleep(500 - elapsedTime);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}

					if (!outputVariables.isEmpty()) {
						disp.syncExec(new Runnable() {
							@Override
							public void run() {
								// fillInOutputValues(outputVariables);
								treeViewer.setInput(nodes);
								treeViewer.expandAll();
								// rightComposite.update();
								// rightComposite.redraw();
								// destGraph.redraw();
								// destGraph.update();
								// destGraph.layout(true);
								// sashForm.update();
								// sashForm.redraw();
								// sashForm.layout(true);
							}
						});

					}

					monitor.done();
					return Status.OK_STATUS;
				} catch (Exception ex) {
					ex.printStackTrace();
					return Status.OK_STATUS;
				}
			}
		};

		runWorkflowJob.schedule();
		return "";
	}

	private ArrayList<Value> getSubtypes(ArrayList<Value> suboutputVariables, Argument sub, boolean noObjects) {
		if (!suboutputVariables.contains(sub)) {
			if (noObjects) {
				if (sub.getSubtypes().isEmpty()) {
					suboutputVariables.add((Value) sub);
				}
			} else {
				suboutputVariables.add((Value) sub);
			}
		}
		if (!sub.getSubtypes().isEmpty()) {
			for (Argument subsub : sub.getSubtypes()) {
				if (noObjects) {
					if (!sub.isArray()) {
						getSubtypes(suboutputVariables, subsub, true);
					}
				} else {
					getSubtypes(suboutputVariables, subsub, true);
				}
			}
		}
		return suboutputVariables;
	}

	private void fillInOutputValues(ArrayList<Value> list, Vector<Node> nodes,
			edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph) {

		for (int i = 0; i < list.size(); i++) {
			Value var = list.get(i);
			showOutputs(var, null, nodes, column2, graph);
		}

		// outputsComposite.setSize(300, list.size() * 50);

		// String[] datatypes = new String[] { "string", "long", "int", "float",
		// "double", "dateTime", "boolean" };
		//
		// for (int i = 0; i < list.size(); i++) {
		// Value var = (Value) list.get(i);
		// if (obj instanceof Tree) {
		// for (int j = 0; j < ((Tree) obj).getItemCount(); j++) {
		// if (((Tree) obj).getItem(j) instanceof TreeItem) {
		// TreeItem item = (TreeItem) ((Tree) obj).getItem(j);
		// if (item.getText().trim().equals(var.getName().toString())
		// ||
		// item.getText().split("\\[")[0].trim().equals(var.getName().toString()))
		// {
		// if (var.isArray() && RAMLCaller.stringIsItemFromList(var.getType(),
		// datatypes)) {
		// for (Argument element : var.getElements()) {
		// TreeItem newItem = new TreeItem(item, SWT.NONE);
		// newItem.setText(1, ((Value) element).getValue());
		// }
		// } else if (var.isArray() &&
		// !RAMLCaller.stringIsItemFromList(var.getType(), datatypes)) {
		// for (Argument element : var.getElements()) {
		// TreeItem newItem = new TreeItem(item, SWT.NONE);
		// newItem.setText(0, element.getName().toString());
		// fillInOutputValues(var.getElements(), newItem);
		// }
		// if (var.getElements().isEmpty()) {
		// for (Argument sub : var.getSubtypes()) {
		// TreeItem newItem = new TreeItem(item, SWT.NONE);
		// newItem.setText(0, sub.getName().toString());
		// fillInOutputValues(var.getSubtypes(), newItem);
		// }
		// }
		// } else if (!var.isArray() &&
		// !RAMLCaller.stringIsItemFromList(var.getType(), datatypes)) {
		// for (Argument sub : var.getSubtypes()) {
		// TreeItem newItem = new TreeItem(item, SWT.NONE);
		// newItem.setText(0, sub.getName().toString() + " [" + sub.getType() +
		// "]:");
		// fillInOutputValues(var.getSubtypes(), newItem);
		// }
		// } else {
		// item.setText(1, ((Value) var).getValue());
		// fillInOutputValues(var.getSubtypes(), item);
		// }
		// }
		// }
		// }
		// } else if (obj instanceof TreeItem) {
		// for (int j = 0; j < ((TreeItem) obj).getItemCount(); j++) {
		// if (((TreeItem) obj).getItem(j) instanceof TreeItem) {
		// TreeItem item = (TreeItem) ((TreeItem) obj).getItem(j);
		// if (item.getText().trim().equals(var.getName().toString())
		// ||
		// item.getText().split("\\[")[0].trim().equals(var.getName().toString()))
		// {
		// if (var.isArray() && RAMLCaller.stringIsItemFromList(var.getType(),
		// datatypes)) {
		// for (Argument element : var.getElements()) {
		// TreeItem newItem = new TreeItem(item, SWT.NONE);
		// newItem.setText(1, ((Value) element).getValue());
		// }
		// } else if (var.isArray() &&
		// !RAMLCaller.stringIsItemFromList(var.getType(), datatypes)) {
		// for (Argument element : var.getElements()) {
		// TreeItem newItem = new TreeItem(item, SWT.NONE);
		// newItem.setText(0, element.getName().toString());
		// fillInOutputValues(var.getElements(), newItem);
		// }
		// if (var.getElements().isEmpty()) {
		// for (Argument sub : var.getSubtypes()) {
		// TreeItem newItem = new TreeItem(item, SWT.NONE);
		// newItem.setText(0, sub.getName().toString());
		// fillInOutputValues(var.getSubtypes(), newItem);
		// }
		// }
		// } else if (!var.isArray() &&
		// !RAMLCaller.stringIsItemFromList(var.getType(), datatypes)) {
		// for (Argument sub : var.getSubtypes()) {
		// TreeItem newItem = new TreeItem(item, SWT.NONE);
		// newItem.setText(0, sub.getName().toString() + " [" + sub.getType() +
		// "]:");
		// fillInOutputValues(var.getSubtypes(), newItem);
		// }
		// } else {
		// item.setText(1, ((Value) var).getValue());
		// fillInOutputValues(var.getSubtypes(), item);
		// }
		// }
		// }
		// }
		// if (((TreeItem) obj).getItemCount() == 0) {
		// TreeItem item = (TreeItem) ((TreeItem) obj);
		// if (item.getText().trim().equals(var.getName().toString())
		// ||
		// item.getText().split("\\[")[0].trim().equals(var.getName().toString()))
		// {
		// if (var.isArray() && RAMLCaller.stringIsItemFromList(var.getType(),
		// datatypes)) {
		// for (Argument element : var.getElements()) {
		// TreeItem newItem = new TreeItem(item, SWT.NONE);
		// newItem.setText(1, ((Value) element).getValue());
		// }
		// } else if (var.isArray() &&
		// !RAMLCaller.stringIsItemFromList(var.getType(), datatypes)) {
		// for (Argument element : var.getElements()) {
		// TreeItem newItem = new TreeItem(item, SWT.NONE);
		// newItem.setText(0, element.getName().toString());
		// fillInOutputValues(var.getElements(), newItem);
		// }
		// if (var.getElements().isEmpty()) {
		// for (Argument sub : var.getSubtypes()) {
		// TreeItem newItem = new TreeItem(item, SWT.NONE);
		// newItem.setText(0, sub.getName().toString());
		// fillInOutputValues(var.getSubtypes(), newItem);
		// }
		// }
		// } else if (!var.isArray() &&
		// !RAMLCaller.stringIsItemFromList(var.getType(), datatypes)) {
		// for (Argument sub : var.getSubtypes()) {
		// TreeItem newItem = new TreeItem(item, SWT.NONE);
		// newItem.setText(0, sub.getName().toString() + " [" + sub.getType() +
		// "]:");
		// fillInOutputValues(var.getSubtypes(), newItem);
		// }
		// } else {
		// item.setText(1, ((Value) var).getValue());
		// fillInOutputValues(var.getSubtypes(), item);
		// }
		// }
		// }
		// }
		//
		// // fillInOutputValues(var.getSubtypes());
		//
		// }

		// for (int i = 0; i < list.size(); i++) {
		// Value var = (Value) list.get(i);
		// for (int j = 0; j < outputsComposite.getChildren().length; j++) {
		// if (outputsComposite.getChildren()[j] instanceof Label) {
		// Label label = (Label) outputsComposite.getChildren()[j];
		// if
		// (label.getText().split("\\[")[0].trim().equals(var.getName().toString()))
		// {
		// ((Text) outputsComposite.getChildren()[j +
		// 1]).setText(var.getValue());
		// }
		// }
		// }
		//
		// fillInOutputValues(var.getSubtypes());
		//
		// }

	}

	private OwlService checkCondition(OwlService source, HashMap<OwlService, Connector> candidates,
			ArrayList<Value> allVariables) {
		double prevThreshold = Similarity.levenshteinThreshold;
		Similarity.levenshteinThreshold = 0;
		Iterator<Entry<OwlService, Connector>> it = candidates.entrySet().iterator();
		ArrayList<OwlService> resultCandidates = new ArrayList<OwlService>();
		HashMap<OwlService, String> text = new HashMap<OwlService, String>();
		double bestVal = VALUE_SIMILARITY_THRESHOLD;
		while (it.hasNext()) {
			Map.Entry<OwlService, Connector> pair = (Map.Entry<OwlService, Connector>) it.next();
			double val = checkSingleCondition(source, pair.getValue(), pair.getKey(), allVariables);
			if (val >= bestVal) {
				if (val >= bestVal + MAX_DISTANCE_BETWEEN_SOLUTIONS) {
					resultCandidates.clear();
					bestVal = val;
				}
				resultCandidates.add(pair.getKey());
				text.put(pair.getKey(), pair.getValue().toString());
			}
		}
		if ((resultCandidates.isEmpty() || resultCandidates.size() > 1) && !candidates.isEmpty()) {
			ArrayList<OwlService> tempCandidates = new ArrayList<OwlService>();
			it = candidates.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<OwlService, Connector> pair = (Map.Entry<OwlService, Connector>) it.next();
				for (String predicate : ELSE_PREDICATES)
					if (Similarity.comparePredicates(pair.getValue().getCondition(), predicate)) {
						tempCandidates.add(pair.getKey());
						break;
					}
			}
			if (tempCandidates.size() == 1)
				resultCandidates = tempCandidates;
		}
		Similarity.levenshteinThreshold = prevThreshold;
		if (resultCandidates.isEmpty() && !candidates.isEmpty() && VALUE_SIMILARITY_THRESHOLD != 0) {
			System.out.println("TOO STRICT CONDITIONS\t trying again with VALUE_SIMILARITY_THRESHOLD to zero");
			double temp = VALUE_SIMILARITY_THRESHOLD;
			VALUE_SIMILARITY_THRESHOLD = 0;
			OwlService result = checkCondition(source, candidates, allVariables);
			VALUE_SIMILARITY_THRESHOLD = temp;
			return result;
		}
		Similarity.levenshteinThreshold = prevThreshold;
		if (resultCandidates.size() == 1)
			return resultCandidates.get(0);
		return (OwlService) UserInput.getSelection(source.toString(), resultCandidates, text);
	}

	/**
	 * <h1>checkSingleCondition</h1> This function returns a weight in range
	 * [0,1] that represents the match of the given expression to the given
	 * condition.<br/>
	 * This function also tries to extract numeric data in order to compare
	 * expressions in case it yield better results.
	 * 
	 * @param source
	 *            : an OwlService that represents the expression to be evaluated
	 * @param condition
	 *            : a Connector that represents the condition to be checked
	 * @param candidate
	 *            : the next OwlService to be reached if the given Connector is
	 *            selected
	 * @param allVariables
	 *            : a list of all possible variables
	 * @return a value in range [0,1]
	 */
	private double checkSingleCondition(OwlService source, Connector condition, OwlService candidate,
			ArrayList<Value> allVariables) {
		if (condition.toString().isEmpty())
			return 1;
		String sourceText = source.toString();
		String conditionText = condition.getCondition();
		boolean negativeLogic = false;
		boolean logicResult = false;

		for (String predicate : FALSE_PREDICATES)
			if (Similarity.comparePredicates(conditionText, predicate)) {
				negativeLogic = true;
				logicResult = true;
				break;
			}
		for (String predicate : TRUE_PREDICATES)
			if (Similarity.comparePredicates(conditionText, predicate)) {
				logicResult = true;
				break;
			}
		double compareMode = 0;
		boolean equality= false;
		if (logicResult) {
			int index;
			if ((index = sourceText.indexOf("==")) != -1) {
				conditionText = sourceText.substring(index + 2);
				sourceText = sourceText.substring(0, index);
				equality=true;
			} else if ((index = sourceText.indexOf("!=")) != -1) {
				conditionText = sourceText.substring(index + 2);
				sourceText = sourceText.substring(0, index);
				negativeLogic = !negativeLogic;
			} else if ((index = sourceText.indexOf("=")) != -1) {
				conditionText = sourceText.substring(index + 1);
				sourceText = sourceText.substring(0, index);
				equality=true;
			} else if ((index = sourceText.indexOf("<")) != -1) {
				conditionText = sourceText.substring(index + 1);
				sourceText = sourceText.substring(0, index);
				compareMode = 1;
			} else if ((index = sourceText.indexOf(">")) != -1) {
				conditionText = sourceText.substring(index + 1);
				sourceText = sourceText.substring(0, index);
				compareMode = -1;
			}
		}
		String replacableCondition = conditionText.replaceAll("[^\\.0123456789]", "");
		if (replacableCondition.isEmpty()) {
			replacableCondition = sourceText.replaceAll("[^\\.0123456789]", "");
			if (!replacableCondition.isEmpty()) {
				String temp = sourceText;
				sourceText = conditionText;
				conditionText = temp;
				compareMode = -compareMode;
			}
		}

		Value bestValue = null;
		double bestMatch = VARIABLE_NAME_SIMILARITY_THRESHOLD;
		for (Value val : allVariables) {
			if (val.getValue() != null) {
				if (val.getValue().replaceAll("[^\\.0123456789]", "").isEmpty() != conditionText
						.replaceAll("[^\\.0123456789]", "").isEmpty())
					continue;
				double match = Similarity.similarity(val.getName().toString(), sourceText);
				if (match >= bestMatch) {
					bestMatch = match;
					bestValue = val;
				}
			}
		}
		if (bestValue == null)
			return 1;
		boolean success = true;
		double conditionValue = Double.POSITIVE_INFINITY - 1;
		double variableValue = 0;
		try {
			conditionValue = Double.parseDouble(conditionText.replaceAll("[^\\.0123456789]", ""));// careful
																									// to
																									// have
																									// this
																									// first,
																									// so
																									// that
																									// if
																									// it
																									// fails
																									// POSITIVE_INFINITY
																									// calculations
																									// work
																									// as
																									// intended
			variableValue = Double.parseDouble(bestValue.getValue());
		} catch (Exception e) {
			success = false;
		}
		double retValue = 0;
		if (success) {
			retValue = (conditionValue == variableValue) ? 1
					: (Math.abs(conditionValue - variableValue)
							/ (Math.abs(conditionValue) + Math.abs(variableValue) + 1));
			if (compareMode > 0)
				retValue = conditionValue > variableValue ? 1 : 0;
			else if (compareMode < 0)
				retValue = conditionValue < variableValue ? 1 : 0;
			if (negativeLogic)
				retValue = 1 - retValue;
		}
		double retValue2 = Similarity.similarity(conditionText, bestValue.getValue());
		// / (conditionText.length() * bestValue.getValue().length() + 1);
		if (negativeLogic && equality)
			retValue2 = 1 - retValue2;
		// System.out.println(sourceText+" vs "+bestValue.getName()+" =
		// "+Math.max(retValue,
		// retValue2));
		return Math.max(retValue, retValue2);
	}

	public class Implementation extends Operation {
		/**
		 * Generates an implementation out of an operation by converting all
		 * variables into <code>Value</code> instances using
		 * <code>Value.getValue</code>.
		 */
		public Implementation(Operation operation) {
			// super(operation.getName().toString(), operation.getDomain());
			super(operation);
			for (Argument input : operation.getInputs())
				getInputs().add(new Value(input));
			for (Argument output : operation.getOutputs())
				getOutputs().add(new Value(output));
			for (Argument uriParam : operation.getUriParameters())
				getUriParameters().add(new Value(uriParam));
			for (Argument authParam : operation.getAuthenticationParameters())
				getAuthenticationParameters().add(new Value(authParam));
		}
	}

	public edu.uci.ics.jung.graph.Graph<OwlService, Connector> getJungGraph() {
		return jungGraph;
	}

	public void setJungGraph(edu.uci.ics.jung.graph.Graph<OwlService, Connector> jungGraph) {
		this.jungGraph = jungGraph;
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
		// //TEST
		// for (int j = 0; j < zestGraph.getConnections().size(); j++) {
		// GraphConnection
		// graphConnection=(GraphConnection)zestGraph.getConnections().get(j);
		// graphConnection.setData(Boolean.TRUE);
		// }

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
							// EntityConnectionData connectionData = new
							// EntityConnectionData(node, destination);
							// viewer.addRelationship(connectionData, node,
							// destination);
							GraphConnection graphConnection = new GraphConnection(zestGraph, SWT.NONE, graphNode, find);
							// MyNode dest = (MyNode)
							// graphConnection.getDestination().getData();
							// MyNode src = (MyNode)
							// graphConnection.getSource().getData();
							EntityConnectionData connectionData = new EntityConnectionData(node, destination);
							graphConnection.setData(connectionData);
						}
					}
				}
			}
		}

		// LayoutAlgorithm layout = setLayout();
		// Filter filter = new Filter() {
		// public boolean isObjectFiltered(LayoutItem item) {
		//
		// // Get the "Connection" from the Layout Item
		// // and use this connection to get the "Graph Data"
		// Object object = item.getGraphData();
		// if (object instanceof GraphConnection ) {
		// GraphConnection connection = (GraphConnection) object;
		// if ( connection.getData() != null && connection.getData() instanceof
		// Boolean ) {
		// // If the data is false, don't filter, otherwise, filter.
		// return ((Boolean)connection.getData()).booleanValue();
		// }
		// return true;
		// }
		// return false;
		// }
		//
		// };
		// layout.setFilter(filter);
		// viewer.setLayoutAlgorithm(layout, true);
		// ViewerFilter[] filters = new ViewerFilter[1];
		// filters[0]= filter;
		// viewer.applyLayout();
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
							// EntityConnectionData connectionData = new
							// EntityConnectionData(node, destination);
							// viewer.addRelationship(connectionData, node,
							// destination);
							GraphConnection graphConnection = new GraphConnection(zestGraph, SWT.NONE, graphNode, find);
							graphConnection.setText(condition);
							// MyNode dest = (MyNode)
							// graphConnection.getDestination().getData();
							// MyNode src = (MyNode)
							// graphConnection.getSource().getData();
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

	public class ProjectNameDialog extends Dialog {
		String value;

		/**
		 * @param parent
		 */
		public ProjectNameDialog(Shell parent) {
			super(parent);
		}

		/**
		 * Makes the dialog visible.
		 * 
		 * @return
		 */
		public String open() {
			Shell parent = getParent();
			final Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
			shell.setText("Name of the project");

			shell.setLayout(new GridLayout(2, true));

			Label label = new Label(shell, SWT.NULL);
			label.setText("Please enter a name for the project");

			final Text text = new Text(shell, SWT.SINGLE | SWT.BORDER);

			final Button buttonOK = new Button(shell, SWT.PUSH);
			buttonOK.setText("Ok");
			buttonOK.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			Button buttonCancel = new Button(shell, SWT.PUSH);
			buttonCancel.setText("Cancel");

			text.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event event) {
					try {
						value = new String(text.getText());
						buttonOK.setEnabled(true);
					} catch (Exception e) {
						buttonOK.setEnabled(false);
					}
				}
			});

			buttonOK.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					shell.dispose();
				}
			});

			buttonCancel.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					value = null;
					shell.dispose();
				}
			});

			shell.addListener(SWT.Traverse, new Listener() {
				public void handleEvent(Event event) {
					if (event.detail == SWT.TRAVERSE_ESCAPE)
						event.doit = false;
				}
			});

			text.setText("");
			shell.pack();
			shell.open();

			Display display = parent.getDisplay();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}

			return value;
		}
	}

	public String askProjectName() {

		Shell shell = new Shell();
		ProjectNameDialog dialog = new ProjectNameDialog(shell);

		String s = dialog.open();
		return s;

	}

	/**
	 * <h1>generate</h1> Creates a RESTful web service of the workflow, in a
	 * maven-based eclipse project. Method is called by pressing the appropriate
	 * button in the toolbar.
	 * 
	 * @throws Exception
	 */
	public void generate() throws Exception {

		projectName = askProjectName();
		if ((projectName != null) && (projectName.trim().length() > 0)) {

			IProgressMonitor monitor = new NullProgressMonitor();
			IProject existingProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (existingProject.exists()) {
				Shell shell = new Shell();
				boolean result = MessageDialog.openConfirm(shell, "Project already exists",
						"A project with this name already exists. Would you like to replace it?");

				if (result) {
					// OK Button selected
					existingProject.delete(true, false, monitor);
				} else {
					// Cancel Button selected
					return;
				}

			}
			// IProject project =
			// ResourcesPlugin.getWorkspace().getRoot().getProject("TestProject");
			IProject project = getWebDataModel(projectName);
			currentProject = project;
			// if (!project.exists()) {
			try {

				// project.create(monitor);
				project.open(monitor);

				// Configure the project to be a Java project and a maven
				// project
				IProjectDescription description = project.getDescription();
				description.setNatureIds(new String[] { JavaCore.NATURE_ID, "org.eclipse.m2e.core.maven2Nature",
						"org.eclipse.jem.workbench.JavaEMFNature", "org.eclipse.wst.jsdt.core.jsNature",
						"org.eclipse.wst.common.modulecore.ModuleCoreNature",
						"org.eclipse.wst.common.project.facet.core.nature" });
				project.setDescription(description, monitor);

				IJavaProject javaProject = JavaCore.create(project);

				// src

				IFolder src = project.getFolder("src");
				if (!src.exists()) {
					src.create(true, true, monitor);
				}
				// bin
				IFolder binFolder = project.getFolder("bin");
				if (!binFolder.exists()) {
					binFolder.create(false, true, monitor);
				}
				javaProject.setOutputLocation(binFolder.getFullPath(), monitor);
				System.out.println(binFolder.getFullPath());

				// Let's add JavaSE-1.6 to our classpath
				List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
				IExecutionEnvironmentsManager executionEnvironmentsManager = JavaRuntime
						.getExecutionEnvironmentsManager();
				IExecutionEnvironment[] executionEnvironments = executionEnvironmentsManager.getExecutionEnvironments();
				for (IExecutionEnvironment iExecutionEnvironment : executionEnvironments) {
					// We will look for JavaSE-1.6 as the JRE container to add
					// to our classpath
					if ("JavaSE-1.7".equals(iExecutionEnvironment.getId())) {
						entries.add(JavaCore.newContainerEntry(JavaRuntime.newJREContainerPath(iExecutionEnvironment)));
						break;
					}
				}
				// IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
				// LibraryLocation[] locations =
				// JavaRuntime.getLibraryLocations(vmInstall);
				// for (LibraryLocation element : locations) {
				// entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(),
				// null, null));
				// }

				// generate pom.xml
				pomPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/"
						+ javaProject.getElementName();
				RestfulCodeGenerator.writePom(pomPath, projectName);

				// Let's add the maven container to our classpath to let the
				// maven plug-in add the dependencies computed from a
				// pom.xml file to our classpath
				IClasspathEntry mavenEntry = JavaCore.newContainerEntry(
						new Path("org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER"), new IAccessRule[0],
						new IClasspathAttribute[] { JavaCore
								.newClasspathAttribute("org.eclipse.jst.component.dependency", "/WEB-INF/lib") },
						false);
				entries.add(mavenEntry);

				// add libs to project class path
				javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);

				// Let's create our target/classes output folder

				IFolder target = project.getFolder("target");
				if (!target.exists()) {
					target.create(true, true, monitor);
				}

				IFolder build = project.getFolder("build");
				if (!build.exists()) {
					build.create(true, true, monitor);
				}

				IFolder classes = target.getFolder("classes");
				if (!classes.exists()) {
					classes.create(true, true, monitor);
				}

				// Let's add target/classes as our output folder for
				// compiled ".class"
				javaProject.setOutputLocation(classes.getFullPath(), monitor);

				// Now let's add our source folder and output folder to our
				// classpath
				IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
				// +1 for our src entry
				IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
				System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);

				IPackageFragmentRoot packageRoot = javaProject.getPackageFragmentRoot(src);
				newEntries[oldEntries.length] = JavaCore.newSourceEntry(packageRoot.getPath(), new Path[] {},
						new Path[] {}, classes.getFullPath());

				javaProject.setRawClasspath(newEntries, null);

				IPackageFragment pack = javaProject.getPackageFragmentRoot(src)
						.createPackageFragment("eu.scasefp7.services.composite", false, null);

				// generate code of Workflow Class
				NonLinearCodeGenerator generator = new NonLinearCodeGenerator();
				String source = generator.generateCode(jungGraph, "workflow", false, projectName);
				StringBuffer buffer = new StringBuffer();
				buffer.append("package " + pack.getElementName() + ";\n");
				buffer.append("\n");
				// String source="public class TestClass{\n"+
				// " private String name;"+ "\n" + "}";;
				buffer.append(source);
				ICompilationUnit testerClass = pack.createCompilationUnit("WorkflowClass.java", buffer.toString(),
						false, null);
						// IType type = testerClass.getType("TesterClass");

				// type.createField("private String age;", null, true, null);

				// generate code of REST
				// detect input variables
				ArrayList<OwlService> inputVariables = new ArrayList<OwlService>();
				for (OwlService service : jungGraph.getVertices()) {
					if (service.getArgument() != null) {
						if (jungGraph.getInEdges(service).size() == 0) {
							if (!service.getisMatchedIO() && !inputVariables.contains(service)) {
								inputVariables.add(service);
							}
						}
					}
				}
				String restCode = RestfulCodeGenerator.generateRestfulCode(pack.getElementName(),
						generator.getInputVariables(), generator.geturiParameters());
				StringBuffer restBuffer = new StringBuffer();
				restBuffer.append(restCode);
				ICompilationUnit restClass = pack.createCompilationUnit("WebService.java", restBuffer.toString(), false,
						null);
				// IType type2 = restClass.getType("RestCode");

				boolean hasRest = false;
				boolean hasSoap = false;
				for (OwlService service : jungGraph.getVertices()) {
					if (service.getOperation() != null) {
						if (service.getOperation().getType().equalsIgnoreCase("Restful")) {
							hasRest = true;
						}
						if (service.getOperation().getType().equalsIgnoreCase("soap")) {
							hasSoap = true;
						}
					}
				}

				gGenerator = generator;

				if (hasRest) {
					String code = CallRestfulServiceCode.generateCode(pack.getElementName());
					StringBuffer codeBuffer = new StringBuffer();
					codeBuffer.append(code);
					ICompilationUnit callRestClass = pack.createCompilationUnit("CallRESTfulService.java",
							codeBuffer.toString(), false, null);
				}

				if (hasSoap) {
					String code = CallWSDLServiceCode.generateCode(pack.getElementName());
					StringBuffer codeBuffer = new StringBuffer();
					codeBuffer.append(code);
					ICompilationUnit callWSDLClass = pack.createCompilationUnit("CallWSDLService.java",
							codeBuffer.toString(), false, null);
				}

				// edit web.xml
				String path = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/"
						+ javaProject.getElementName() + "/WebContent/WEB-INF/web.xml";
				RestfulCodeGenerator.editWebXML(path, pack.getElementName());

				// refresh project
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				root.refreshLocal(IResource.DEPTH_INFINITE, monitor);

			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			final Display disp = Display.getCurrent();
			disp.syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openInformation(disp.getActiveShell(), "Error occured", "This is not a valid name.");
				}
			});
		}

		// }

	}

	public static IProject getWebDataModel(String projName) throws ExecutionException {
		// IDataModel dm = DataModelFactory.createDataModel(new
		// SimpleWebFacetProjectCreationDataModelProvider());
		// dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME,
		// projName);
		//
		// FacetDataModelMap facetMap = (FacetDataModelMap) dm
		// .getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
		// IDataModel facetModel = (IDataModel)
		// facetMap.get(IModuleConstants.WST_WEB_MODULE);
		// facetModel.setProperty(IJ2EEModuleFacetInstallDataModelProperties.FACET_VERSION_STR,
		// "2.4");
		// facetModel.setBooleanProperty(IJ2EEFacetInstallDataModelProperties.GENERATE_DD,true);

		IDataModel model = DataModelFactory.createDataModel(new WebFacetProjectCreationDataModelProvider());
		model.setProperty(IFacetDataModelProperties.FACET_PROJECT_NAME, projName);

		FacetDataModelMap map = (FacetDataModelMap) model
				.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
		IDataModel webModel = (IDataModel) map.get(IModuleConstants.JST_WEB_MODULE);
		webModel.setProperty(IJ2EEModuleFacetInstallDataModelProperties.FACET_VERSION_STR, "2.4");
		// webModel.setBooleanProperty(IJ2EEModuleFacetInstallDataModelProperties.ADD_TO_EAR,
		// true);

		IStatus st = model.getDefaultOperation().execute(new NullProgressMonitor(), null);
		return st.isOK() ? ResourcesPlugin.getWorkspace().getRoot().getProject(projName) : null;
	}

	private IStatus checkGraph(edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph, final Display disp)
			throws Exception {

		// Check if every node has a path to StartNode and to
		// EndNode, a condition has two output edges and the graph contains at
		// least one operation in order to allow
		// execution.
		ArrayList<OwlService> previousList = new ArrayList<OwlService>();
		ArrayList<OwlService> nextList = new ArrayList<OwlService>();
		int numberOfActions = 0;

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

		for (OwlService service : graph.getVertices()) {

			if (service.getType().contains("Action") || service.getType().contains("Condition")) {

				if (service.getType().contains("Action")) {
					numberOfActions++;
				}
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

				previousList = (new PathFinding(graph).findOperationPath(startingService, service));
				if (previousList.get(0) != startingService) {
					final OwlService unlinked = previousList.get(0);
					try {
						throw new Exception("\"" + unlinked.getName().toString() + "\""
								+ " has no path to StartNode. Check for unlinked operations.");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					disp.syncExec(new Runnable() {

						@Override
						public void run() {
							MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
									"\"" + unlinked.getName().toString() + "\""
											+ " has no path to StartNode. Check for unlinked operations.");
						}
					});
					return Status.CANCEL_STATUS;
				}
				nextList = (new PathFinding(graph).findOperationPath(service, endingService));
				if (nextList.get(0) != service) {
					final OwlService unlinked = service;
					try {
						throw new Exception("\"" + unlinked.getName().toString() + "\""
								+ " has no path to EndNode. Check for unlinked operations.");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					disp.syncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
									"\"" + unlinked.getName().toString() + "\""
											+ " has no path to EndNode. Check for unlinked operations.");
						}
					});
					return Status.CANCEL_STATUS;

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
							"Graph should contain at least one Operation.");
				}

			});
			return Status.CANCEL_STATUS;

		}
		return Status.OK_STATUS;
	}

	private void uploadOnServer() throws Exception {

		String SFTPHOST = "109.231.126.106";
		int SFTPPORT = 22;
		String SFTPUSER = "root";
		String SFTPPASS = "loubas";
		String SFTPWORKINGDIR = "/home/ubuntu/apache-tomcat-8.0.23/webapps/";
		Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;
		try {
			JSch jsch = new JSch();
			session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
			session.setPassword(SFTPPASS);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp) channel;
			channelSftp.cd("..");
			channelSftp.cd(SFTPWORKINGDIR);
			File f = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/" + projectName
					+ "/target/" + projectName + "-0.0.1-SNAPSHOT.war");
			channelSftp.put(new FileInputStream(f), f.getName());
		} catch (Exception ex) {
			ex.printStackTrace();

		}

	}

	/**
	 * Sets up Maven embedder for execution.
	 */
	// protected void setUpMavenBuild() {
	// try {
	// container = new DefaultPlexusContainer();
	// maven = container.lookup(Maven.class);
	// populator = container.lookup(MavenExecutionRequestPopulator.class);
	// settingsBuilder = container.lookup(SettingsBuilder.class);
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	// }
	//
	/**
	 * Runs maven -clean/install command which builds the project and installs
	 * artifact to the local repository
	 */
	protected void install(String path) throws Exception {

		final Shell shell = new Shell();
		final Display disp = Display.getCurrent();
		createWarFileJob = new Job("Uploading..") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Creating WAR file, uploading on server and connecting to MDE Ontology...",
						IProgressMonitor.UNKNOWN);

				try {

					File basedir = new File(pomPath);
					IProgressMonitor monitor2 = new NullProgressMonitor();
					IMaven maven = MavenPlugin.getMaven();
					MavenExecutionRequest request = createExecutionRequest();
					// MavenExecutionRequest request = createExecutionRequest();
					request.setPom(new File(basedir, "pom.xml"));
					// request.setGoals(Arrays.asList("clean"));
					request.setGoals(Arrays.asList("install"));
					// populator.populateDefaults(request);
					MavenExecutionResult result = maven.execute(request, monitor2);
					// war file is generated with the second execution
					MavenExecutionResult result2 = maven.execute(request, monitor2);
					boolean exists = webServiceExistsOnServer();
					if (exists) {
						disp.syncExec(new Runnable() {
							public void run() {
								boolean answer = MessageDialog.openConfirm(shell,
										"Web service already exists on server",
										"A web service with this name already exists. Would you like to update it?");

								if (answer) {
									// OK Button selected
									try {
										uploadOnServer();
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						});

					} else {
						uploadOnServer();
					}
					if (webServiceExistsOnServer()) {
						disp.syncExec(new Runnable() {
							public void run() {
								boolean answer = MessageDialog.openConfirm(shell, "Upload is complete!",
										"The web service was uploaded successfully!\n"
												+ "Base URI: http://109.231.126.106:8080/" + currentProject.getName()
												+ "-0.0.1-SNAPSHOT/\n" + "Resource Path: rest/result/query\n\n"
												+ "Would you like to also connect the web service to the MDE ontology?");

								if (answer) {
									// OK Button selected
									try {
										ConnectToMDEOntology.writeToOntology(scaseProject, gGenerator.getOperation());
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									final IFile file = ResourcesPlugin.getWorkspace().getRoot()
											.getFileForLocation(Path.fromOSString(
													ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
															+ "/" + scaseProject.getName() + "/LinkedOntology.owl"));
									if (file != null) {
										disp.syncExec(new Runnable() {
											@Override
											public void run() {
												MessageDialog.openInformation(disp.getActiveShell(), "Info",
														"LinkedOntology.owl file is created under the project "
																+ scaseProject.getName());
											}
										});

									}
								}
							}
						});

					} else {
						disp.syncExec(new Runnable() {
							@Override
							public void run() {
								MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
										"Web service could not be deployed on server. Please contact the server administrator.");
							}
						});
					}

					monitor.done();
					return Status.OK_STATUS;
				} catch (Exception ex) {
					return Status.OK_STATUS;
				}

			}

		};

		createWarFileJob.schedule();

	}

	/**
	 * Maven Excecution request.
	 */
	public MavenExecutionRequest createExecutionRequest() throws Exception {
		SettingsBuildingRequest settingsRequest = new DefaultSettingsBuildingRequest();
		settingsRequest.setUserSettingsFile(MavenCli.DEFAULT_USER_SETTINGS_FILE);
		settingsRequest.setGlobalSettingsFile(MavenCli.DEFAULT_GLOBAL_SETTINGS_FILE);
		MavenExecutionRequest request = new DefaultMavenExecutionRequest();
		request.setUserSettingsFile(settingsRequest.getUserSettingsFile());
		request.setGlobalSettingsFile(settingsRequest.getGlobalSettingsFile());
		request.setSystemProperties(System.getProperties());
		// populator.populateFromSettings(request,
		// settingsBuilder.build(settingsRequest).getEffectiveSettings());
		return request;
	}

	private boolean webServiceExistsOnServer() throws Exception {
		boolean exists = false;

		String username = "admin";
		String password = "loubas";
		URL url = new URL("http://109.231.126.106:8080/manager/text/list");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		try {

			con.setRequestMethod("GET");
			Authenticator.setDefault(new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("admin", "loubas".toCharArray());
				}
			});

			// con.addRequestProperty("Authorization",
			// "Basic " + username + ":" + password);
			// OutputStream out = con.getOutputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			try {

				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				System.out.println(response.toString());
				if (response.toString().contains("/" + projectName + "-0.0.1-SNAPSHOT")) {
					exists = true;
				}
			} finally {
				in.close();
			}
			int code = con.getResponseCode();
			if (code != HttpURLConnection.HTTP_OK) {
				String msg = con.getResponseMessage();
				throw new IOException("HTTP Error " + code + ": " + msg);
			}
		} finally {
			con.disconnect();
		}
		return exists;
	}
	
	public void setScaseProject(IProject project){
		scaseProject=project;
	}

}