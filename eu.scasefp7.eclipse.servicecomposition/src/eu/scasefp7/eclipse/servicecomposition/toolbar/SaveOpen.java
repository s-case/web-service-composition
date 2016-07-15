package eu.scasefp7.eclipse.servicecomposition.toolbar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.io.GraphMLWriter;
import edu.uci.ics.jung.io.graphml.EdgeMetadata;
import edu.uci.ics.jung.io.graphml.GraphMLReader2;
import edu.uci.ics.jung.io.graphml.GraphMetadata;
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata;
import edu.uci.ics.jung.io.graphml.NodeMetadata;
import eu.scasefp7.eclipse.servicecomposition.Activator;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Service;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.ui.SafeSaveDialog;
import eu.scasefp7.eclipse.servicecomposition.views.ServiceCompositionView;
import eu.scasefp7.eclipse.servicecomposition.views.Utils;

public class SaveOpen {

	
	// the saved workflow file
	private static File workflowFile;
	private static String workflowFilePath = "";
	
	
	
	/**
	 * <h1>openWorkflow</h1> Opens a workflow file. Method is called by pressing
	 * the appropriate button in the toolbar.
	 */
	public static void openWorkflow(ServiceCompositionView view) {
		FileDialog fileDialog = new FileDialog(new Shell());
		// Set the text
		fileDialog.setText("Select Workflow File..");
		// Set filter on .txt files
		fileDialog.setFilterExtensions(new String[] { "*.sc" });
		if (view.getScaseProject() != null) {
			fileDialog.setFilterPath(
					ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/" + view.getScaseProject().getName());
		} else {
			fileDialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());
		}
		// Open Dialog and save result of selection
		String selected = fileDialog.open();
		if (selected != null) {
			workflowFilePath = selected;
			view.setWorkflowFilePath(workflowFilePath);
			File file = new File(selected);
			openWorkflowJob(file, view);
		}
	}

	public static void openWorkflowJob(File file, ServiceCompositionView view) {
		final Shell shell = view.getSite().getWorkbenchWindow().getShell();
		final Display disp = shell.getDisplay();
		Job OpenWorkflowJob = new Job("Open workflow..") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Loading operations...", IProgressMonitor.UNKNOWN);

				try {
					view.loadOperations(disp, shell);
					edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph = loadWorkflowFile(file);
					if (graph != null) {
						disp.syncExec(new Runnable() {
							public void run() {
								view.setJungGraph(graph);
								view.addGraphInZest(graph);
								view.updateRightComposite(graph);
							}
						});
					}
					view.setSavedWorkflow(true);
					view.setStoryboardFile(null);
					monitor.done();
					return Status.OK_STATUS;
				} catch (Exception ex) {
					ex.printStackTrace();
					return Status.CANCEL_STATUS;
				} finally {
					monitor.done();
				}
			}
		};
		OpenWorkflowJob.schedule();
	}

	/**
	 * <h1>saveWorkflow</h1> Saves the current workflow.Method is called by
	 * pressing the appropriate button in the toolbar.
	 */
	public static void saveWorkflow(boolean doSaveAs, String workflowFilePath, ServiceCompositionView view) {
		final Shell shell = view.getSite().getWorkbenchWindow().getShell();
		final Display disp = shell.getDisplay();
		String path = workflowFilePath;
		if (doSaveAs) {
			SafeSaveDialog dialog = new SafeSaveDialog(shell);
			dialog.setFilterExtensions(new String[] { "*.sc" });
			dialog.setText("Save workflow..");
			if (view.getScaseProject() != null) {
				dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/"
						+ view.getScaseProject().getName());
			} else {
				dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());
			}

			dialog.setFileName("workflow.sc");
			path = dialog.open();
		}

		GraphMLWriter<OwlService, Connector> graphWriter = new GraphMLWriter<OwlService, Connector>();

		PrintWriter out = null;
		// String path =
		// ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		try {
			workflowFile = new File(path);
			if (!workflowFile.exists()) {
				workflowFile.getParentFile().mkdirs();
				workflowFile.createNewFile();
			}
			workflowFilePath = workflowFile.getAbsolutePath();
			view.setWorkflowFilePath(workflowFilePath);
			out = new PrintWriter(new BufferedWriter(new FileWriter(workflowFile, false)));

			ArrayList<Argument> possibleInputs = new ArrayList<Argument>();
			ArrayList<Argument> possibleOutputs = new ArrayList<Argument>();
			for (OwlService op : view.getJungGraph().getVertices()) {
				if (op.getOperation() != null) {
					for (Argument arg : op.getOperation().getInputs()) {
						Utils.addArguments(arg, possibleInputs);
					}
					for (Argument arg : op.getOperation().getOutputs()) {
						Utils.addArguments(arg, possibleOutputs);
					}
				}
			}

			graphWriter.addVertexData("type", "The type of the vertex", "Action",
					new org.apache.commons.collections15.Transformer<OwlService, String>() {
						public String transform(OwlService v) {
							return (v.getType());
						}
					});

			graphWriter.addVertexData("matchedIO", "Vertex is matched IO", "false",
					new org.apache.commons.collections15.Transformer<OwlService, String>() {
						public String transform(OwlService v) {
							return Boolean.toString(v.getisMatchedIO());
						}
					});

			graphWriter.addVertexData("name", "Vertex name", "",
					new org.apache.commons.collections15.Transformer<OwlService, String>() {
						public String transform(OwlService v) {

							return (v.getName().toString());
						}
					});

			graphWriter.addVertexData("url", "Vertex url", "",
					new org.apache.commons.collections15.Transformer<OwlService, String>() {
						public String transform(OwlService v) {
							String url = "";
							if (v.getOperation() != null) {
								url = v.getOperation().getDomain().getURI();
							}
							return url;
						}
					});

			

			graphWriter.addVertexData("operationName", "Vertex operation name", "",
					new org.apache.commons.collections15.Transformer<OwlService, String>() {
						public String transform(OwlService v) {
							String operationName = "";
							if (v.getOperation() != null) {
								operationName = v.getOperation().getName().toString();
							} else if (v.getArgument() != null) {
								operationName = v.getArgument().getBelongsToOperation().getName().toString();
							}
							return operationName;
						}
					});

			graphWriter.addVertexData("IOType", "IO Argument type", "",
					new org.apache.commons.collections15.Transformer<OwlService, String>() {
						public String transform(OwlService v) {
							String type = "";
							if (v.getArgument() != null) {
								type = v.getArgument().getType();
							}
							return type;
						}
					});

			graphWriter.addVertexData("IOisNative", "IO Argument isNative value", "",
					new org.apache.commons.collections15.Transformer<OwlService, String>() {
						public String transform(OwlService v) {
							String isNative = "";
							if (v.getArgument() != null) {
								isNative = Boolean.toString(v.getArgument().isNative());
							}
							return isNative;
						}
					});
			graphWriter.addVertexData("IOisArray", "IO Argument isArray value", "",
					new org.apache.commons.collections15.Transformer<OwlService, String>() {
						public String transform(OwlService v) {
							String isArray = "";
							if (v.getArgument() != null) {
								isArray = Boolean.toString(v.getArgument().isArray());
							}
							return isArray;
						}
					});

			graphWriter.addVertexData("IOisRequired", "IO Argument isRequired value", "",
					new org.apache.commons.collections15.Transformer<OwlService, String>() {
						public String transform(OwlService v) {
							String isRequired = "";
							if (v.getArgument() != null) {
								isRequired = Boolean.toString(v.getArgument().isRequired());
							}
							return isRequired;
						}
					});

			graphWriter.addVertexData("IO", "Argument is input or output", "",
					new org.apache.commons.collections15.Transformer<OwlService, String>() {
						public String transform(OwlService v) {
							String is = "";
							if (v.getArgument() != null) {
								if (possibleInputs.contains(v.getArgument())
										&& !possibleOutputs.contains(v.getArgument())) {
									is = "input";
								} else if (possibleOutputs.contains(v.getArgument())
										&& !possibleInputs.contains(v.getArgument())) {
									is = "output";
								} else {
									is = "not defined";
								}
							}
							return is;
						}
					});

			graphWriter.addVertexData("IOSubtypes", "IO Argument Subtypes", "",
					new org.apache.commons.collections15.Transformer<OwlService, String>() {
						public String transform(OwlService v) {
							String subtypes = "";
							if (v.getArgument() != null) {
								for (Argument sub : v.getArgument().getSubtypes()) {
									subtypes += sub.getName().toString() + ",";
								}
							}
							return subtypes;
						}
					});


			graphWriter.addEdgeData("condition", "Edge condition name", "",
					new org.apache.commons.collections15.Transformer<Connector, String>() {
						public String transform(Connector v) {

							return v.getCondition();
						}
					});

			graphWriter.setVertexIDs(new org.apache.commons.collections15.Transformer<OwlService, String>() {
				public String transform(OwlService v) {
					if (v.getName().toString().isEmpty()) {
						return v.getType() + Integer.toString(v.getId());
					}
					return v.getName().toString() + Integer.toString(v.getId());
				}
			});

			graphWriter.save(view.getJungGraph(), out);

		} catch (IOException e) {
			Activator.log("Error while saving workflow", e);
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}

		view.setSavedWorkflow(true);
		// setDirty(false);

		// Trace user action
		Activator.TRACE.trace("/debug/executeCommand", "Workflow is saved.");

		disp.syncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialog.openInformation(disp.getActiveShell(), "Info", "Workflow is saved.");
			}

		});

	}
	
	public static edu.uci.ics.jung.graph.Graph<OwlService, Connector> loadWorkflowFile(File file) {

		edu.uci.ics.jung.graph.Graph<OwlService, Connector> g = null;

		try {
			
			g = readFile(file, ServiceCompositionView.getOperations());

		} catch (Exception ex) {
			Activator.log("Error loading workflow file", ex);
			ex.printStackTrace();

		}

		return g;

	}

	private static edu.uci.ics.jung.graph.Graph<OwlService, Connector> readFile(File file, ArrayList<Operation> operations) {
		edu.uci.ics.jung.graph.Graph<OwlService, Connector> g = null;
		HashMap<String, OwlService> nodes = new HashMap<String, OwlService>();
		HashMap<String, String> ids = new HashMap<String, String>();
		HashMap<String, String> matchedIO = new HashMap<String, String>();
		ArrayList<OwlService> owlInputs = new ArrayList<OwlService>();
		ArrayList<OwlService> owlOutputs = new ArrayList<OwlService>();
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(file.getAbsolutePath()));

			/* Create the Graph Transformer */
			org.apache.commons.collections15.Transformer<GraphMetadata, edu.uci.ics.jung.graph.Graph<OwlService, Connector>> graphTransformer = new org.apache.commons.collections15.Transformer<GraphMetadata, edu.uci.ics.jung.graph.Graph<OwlService, Connector>>() {

				public edu.uci.ics.jung.graph.Graph<OwlService, Connector> transform(GraphMetadata metadata) {
					if (metadata.getEdgeDefault().equals(metadata.getEdgeDefault().DIRECTED)) {
						return new DirectedSparseGraph<OwlService, Connector>();
					} else {
						return new UndirectedSparseGraph<OwlService, Connector>();
					}
				}
			};

			/* Create the Vertex Transformer */

			org.apache.commons.collections15.Transformer<NodeMetadata, OwlService> vertexTransformer = new org.apache.commons.collections15.Transformer<NodeMetadata, OwlService>() {
				public OwlService transform(NodeMetadata metadata) {

					OwlService v = null;
					if (metadata.getProperty("type").equals("Action")) {
						for (Operation op : operations) {
							if (metadata.getProperty("name").equals(op.getName().toString())
									&& metadata.getProperty("url").equals(op.getDomain().getURI())) {
								v = new OwlService(op);
								v.setId(Integer.parseInt(metadata.getId().replaceAll("\\D+", "")));
								nodes.put(metadata.getId(), v);
							}
						}
					} else if (metadata.getProperty("type").equals("Condition")) {
						v = new OwlService(new Service(metadata.getProperty("name"), metadata.getProperty("type")));
						nodes.put(metadata.getId(), v);
					} else if (metadata.getProperty("type").equals("Property")) {
						ArrayList<Argument> possibleArguments = new ArrayList<Argument>();
						ArrayList<Argument> possibleOutputs = new ArrayList<Argument>();
						for (Operation op : operations) {
							for (Argument arg : op.getInputs()) {
								Utils.addArguments(arg, possibleArguments);
							}
							for (Argument arg : op.getOutputs()) {
								Utils.addArguments(arg, possibleOutputs);
							}
						}
						for (Argument arg : possibleArguments) {
							if (arg.getName().toString().equals(metadata.getProperty("name"))
									&& arg.getType().equals(metadata.getProperty("IOType"))
									&& Boolean.toString(arg.isNative()).equals(metadata.getProperty("IOisNative"))
									&& Boolean.toString(arg.isArray()).equals(metadata.getProperty("IOisArray"))
									&& Boolean.toString(arg.isRequired()).equals(metadata.getProperty("IOisRequired"))
									&& arg.getBelongsToOperation().getName().toString()
											.equals(metadata.getProperty("operationName"))
									&& metadata.getProperty("IO").equals("input")) {
								Argument argument = new Argument(arg);
								v = new OwlService(argument);
								// v.getArgument().setOwlService(v);
							}
						}

						for (Argument arg : possibleOutputs) {
							if (arg.getName().toString().equals(metadata.getProperty("name"))
									&& arg.getType().equals(metadata.getProperty("IOType"))
									&& Boolean.toString(arg.isNative()).equals(metadata.getProperty("IOisNative"))
									&& Boolean.toString(arg.isArray()).equals(metadata.getProperty("IOisArray"))
									&& Boolean.toString(arg.isRequired()).equals(metadata.getProperty("IOisRequired"))
									&& arg.getBelongsToOperation().getName().toString()
											.equals(metadata.getProperty("operationName"))
									&& metadata.getProperty("IO").equals("output")) {
								Argument argument = new Argument(arg);
								v = new OwlService(argument);
								// v.getArgument().setOwlService(v);
							}
						}
						ids.put(metadata.getId(), metadata.getId().replaceAll("\\D+", ""));
						matchedIO.put(metadata.getId(), metadata.getProperty("matchedIO"));

						v.setId(Integer.parseInt(metadata.getId().replaceAll("\\D+", "")));
						v.setisMatchedIO(Boolean.parseBoolean(metadata.getProperty("matchedIO")));

						if (metadata.getProperty("IO").equals("input")) {
							owlInputs.add(v);
						} else {
							owlOutputs.add(v);
						}

						nodes.put(metadata.getId(), v);
					} else {
						v = new OwlService(new Service("", metadata.getProperty("type")));
						nodes.put(metadata.getId(), v);
					}

					return v;
				}
			};

			/* Create the Edge Transformer */
			org.apache.commons.collections15.Transformer<EdgeMetadata, Connector> edgeTransformer = new org.apache.commons.collections15.Transformer<EdgeMetadata, Connector>() {
				public Connector transform(EdgeMetadata metadata) {

					Connector e = new Connector(nodes.get(metadata.getSource()), nodes.get(metadata.getTarget()), "");
					if (metadata.getProperty("condition") != null) {
						e.setCondition(metadata.getProperty("condition"));
					}
					return e;
				}
			};

			/* Create the Hyperedge Transformer */
			org.apache.commons.collections15.Transformer<HyperEdgeMetadata, Connector> hyperEdgeTransformer = new org.apache.commons.collections15.Transformer<HyperEdgeMetadata, Connector>() {
				public Connector transform(HyperEdgeMetadata metadata) {
					Connector e = new Connector();
					return e;
				}
			};

			/* Create the graphMLReader2 */
			GraphMLReader2<edu.uci.ics.jung.graph.Graph<OwlService, Connector>, OwlService, Connector> graphReader = new GraphMLReader2<edu.uci.ics.jung.graph.Graph<OwlService, Connector>, OwlService, Connector>(
					fileReader, graphTransformer, vertexTransformer, edgeTransformer, hyperEdgeTransformer);

			/* Get the new graph object from the GraphML file */
			g = graphReader.readGraph();

			// connect arguments with operations
			ArrayList<Operation> graphOperations = new ArrayList<Operation>();
			for (OwlService op : g.getVertices()) {
				if (op.getOperation() != null) {
					graphOperations.add(op.getOperation());
				}
			}
			ArrayList<Argument> inputs = new ArrayList<Argument>();
			ArrayList<Argument> outputs = new ArrayList<Argument>();
			for (Operation op : graphOperations) {
				for (Argument arg : op.getInputs()) {
					Utils.addArguments(arg, inputs);
				}
				for (Argument arg : op.getOutputs()) {
					Utils.addArguments(arg, outputs);
				}
			}
			for (OwlService arg : owlInputs) {
				if (arg.getArgument() != null) {
					for (Argument argument : inputs) {
						if (argument.getName().toString().equals(arg.getArgument().getName().toString())
								&& argument.getBelongsToOperation().getName().toString()
										.equals(arg.getArgument().getBelongsToOperation().getName().toString())
								&& Boolean.toString(argument.isArray())
										.equals(Boolean.toString(arg.getArgument().isArray()))
								&& Boolean.toString(argument.isNative())
										.equals(Boolean.toString(arg.getArgument().isNative()))
								&& Boolean.toString(argument.isRequired())
										.equals(Boolean.toString(arg.getArgument().isRequired()))) {

							arg.setContent(argument);
							arg.getArgument().setOwlService(arg);
							break;
						}
					}
				}
			}

			for (OwlService arg : owlOutputs) {
				if (arg.getArgument() != null) {
					for (Argument argument : outputs) {
						if (argument.getName().toString().equals(arg.getArgument().getName().toString())
								&& argument.getBelongsToOperation().getName().toString()
										.equals(arg.getArgument().getBelongsToOperation().getName().toString())
								&& Boolean.toString(argument.isArray())
										.equals(Boolean.toString(arg.getArgument().isArray()))
								&& Boolean.toString(argument.isNative())
										.equals(Boolean.toString(arg.getArgument().isNative()))
								&& Boolean.toString(argument.isRequired())
										.equals(Boolean.toString(arg.getArgument().isRequired()))) {

							arg.setContent(argument);
							arg.getArgument().setOwlService(arg);
							break;
						}
					}
				}
			}

			// add matchedInputs
			for (OwlService arg : g.getVertices()) {
				if (arg.getisMatchedIO()) {
					for (OwlService next : g.getSuccessors(arg)) {
						if (next.getisMatchedIO()) {
							arg.getArgument().addMatchedInputs(next.getArgument());
						}
					}
				}
			}

		} catch (Exception ex) {
			Activator.log("Error while reading graph from workflow file", ex);
			ex.printStackTrace();

		}
		return g;
	}

	
	
}
