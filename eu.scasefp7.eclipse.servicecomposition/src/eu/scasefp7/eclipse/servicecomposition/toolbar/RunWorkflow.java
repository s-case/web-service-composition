package eu.scasefp7.eclipse.servicecomposition.toolbar;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.GraphNode;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import eu.scasefp7.eclipse.servicecomposition.Activator;
import eu.scasefp7.eclipse.servicecomposition.codeInterpreter.UserInput;
import eu.scasefp7.eclipse.servicecomposition.codeInterpreter.Value;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.RequestHeader;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.operationCaller.RAMLCaller;
import eu.scasefp7.eclipse.servicecomposition.operationCaller.WSDLCaller;
import eu.scasefp7.eclipse.servicecomposition.transformer.PathFinding;
import eu.scasefp7.eclipse.servicecomposition.transformer.Similarity;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.ui.MatchOutputDialog;
import eu.scasefp7.eclipse.servicecomposition.ui.Node;
import eu.scasefp7.eclipse.servicecomposition.views.MyNode;
import eu.scasefp7.eclipse.servicecomposition.views.ServiceCompositionView;
import eu.scasefp7.eclipse.servicecomposition.views.Utils;

public class RunWorkflow {

	// constants
	private static double VARIABLE_NAME_SIMILARITY_THRESHOLD = 0.5;
	private static double MAX_DISTANCE_BETWEEN_SOLUTIONS = 0.1;
	private static double VALUE_SIMILARITY_THRESHOLD = 0;
	// predicates for special types of branches
	private static String[] TRUE_PREDICATES = { "true", "yes" };
	private static String[] FALSE_PREDICATES = { "false", "no" };
	private static String[] ELSE_PREDICATES = { "else", "otherwise" };
	// starting and ending service in the graph
	private OwlService startingService = null;
	private OwlService endingService = null;
	// the selected owlservice
	private OwlService currentService;
	private TreeViewerColumn columnb;
	private TreeViewer treeViewer;
	private Composite authParamsComposite;
	private Composite requestHeaderComposite;
	// eclipse jobs
	private Job runWorkflowJob;

	protected ServiceCompositionView view;
	protected edu.uci.ics.jung.graph.Graph<OwlService, Connector> jungGraph;

	public RunWorkflow(ServiceCompositionView view, TreeViewer treeViewer, TreeViewerColumn columnb,
			Composite authParamsComposite, Composite requestHeaderComposite) {
		this.view = view;
		this.jungGraph = view.getJungGraph();
		this.treeViewer = treeViewer;
		this.columnb = columnb;
		this.authParamsComposite = authParamsComposite;
		this.requestHeaderComposite = requestHeaderComposite;
	}

	public void unhighlightAllNodes() {
		// unhighlight all nodes
		Graph graph = view.getViewer().getGraphControl();
		for (int i = 0; i < graph.getNodes().size(); i++) {
			GraphNode graphNode = (GraphNode) graph.getNodes().get(i);
			graphNode.unhighlight();
		}
	}

	public String runWorkflow() throws Exception {
		final Shell shell = view.getSite().getWorkbenchWindow().getShell();
		final Display disp = shell.getDisplay();
		runWorkflowJob = new Job("Running Workflow") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Workflow execution in progress ...", IProgressMonitor.UNKNOWN);

				try {
					final Graph destGraph = view.getViewer().getGraphControl();
					disp.syncExec(new Runnable() {
						@Override
						public void run() {
							unhighlightAllNodes();
							Utils.cleanOutputs(treeViewer.getTree(), jungGraph);
						}
					});
					Properties prop = new Properties();
					String propFileName = "/matcher.properties";
					// Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
					// URL fileURL = bundle.getEntry(propFileName);
					URL fileURL = new URL("platform:/plugin/" + Activator.PLUGIN_ID + propFileName);

					try {
						InputStream inputStream = fileURL.openConnection().getInputStream();
						prop.load(inputStream);

						// File file = new
						// File(FileLocator.resolve(fileURL).toURI());
						// InputStream inputStream = new FileInputStream(file);
						// prop.load(inputStream);
						VARIABLE_NAME_SIMILARITY_THRESHOLD = Double
								.parseDouble(prop.getProperty("interpreter.VARIABLE_NAME_SIMILARITY_THRESHOLD"));
						MAX_DISTANCE_BETWEEN_SOLUTIONS = Double
								.parseDouble(prop.getProperty("interpreter.MAX_DISTANCE_BETWEEN_SOLUTIONS"));
						VALUE_SIMILARITY_THRESHOLD = Double
								.parseDouble(prop.getProperty("interpreter.VALUE_SIMILARITY_THRESHOLD"));
					} catch (Exception e) {
						Activator.log("Error occured while trying to load matcher settings from " + propFileName, e);
						System.err.println("Error occured while trying to load matcher settings from " + propFileName);
					}
					edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph = new SparseMultigraph<OwlService, Connector>();
					// check if user has cancelled
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
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

								e.printStackTrace();
							}
							// They get the previous call value-empty for the
							// first time.
							OwlService created = new OwlService(service);
							created.getArgument().setOwlService(created);
							Value value = new Value(service.getArgument());
							// value.getElements().clear();
							// // Value value =
							// // (Value.getValue(service.getArgument()));
							// // Clear old values
							// value.setValue("");
							// value.getElements().removeAll(value.getElements());

							// ArrayList<Value> list = value.getElements();
							//
							// for (Iterator<Value> iterator = list.iterator();
							// iterator.hasNext(); ) {
							// Value v = iterator.next();
							// if (v != null) {
							// iterator.remove();
							// }
							// }

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
					// check if user has cancelled
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					// initialize variable lists
					final ArrayList<Value> inputVariables = new ArrayList<Value>();
					final ArrayList<Value> outputVariables = new ArrayList<Value>();
					final ArrayList<Value> suboutputVariables = new ArrayList<Value>();
					final ArrayList<Value> subinputVariables = new ArrayList<Value>();
					final ArrayList<Value> authParamVariables = new ArrayList<Value>();
					final ArrayList<RequestHeader> requestHeaderVariables = new ArrayList<RequestHeader>();
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
								// if (!allVariables.contains(arg))
								allVariables.add((Value) arg);
								// if (!inputVariables.contains(arg))
								inputVariables.add((Value) arg);
								for (Argument sub : arg.getSubtypes()) {
									// if (!allVariables.contains(sub)) {
									allVariables.add((Value) sub);
									// }
									getSubtypes(subinputVariables, sub, false);
								}
							}
							for (Argument arg : op.getOutputs()) {
								// if (!allVariables.contains(arg)) {
								allVariables.add((Value) arg);
								// }
								// if (!outputVariables.contains(arg)) {
								outputVariables.add((Value) arg);
								// }

								System.out.println("OUTPUT DETECTED\t " + arg);
								for (Argument sub : arg.getSubtypes()) {
									if (!allVariables.contains(sub)) {
										allVariables.add((Value) sub);
									}
									getSubtypes(suboutputVariables, sub, false);
								}

							}
							// if (!op.getUriParameters().isEmpty()) {
							// for (Argument arg : op.getUriParameters()) {
							// uriParamVariables.add((Value) arg);
							// }
							// }
							if (!op.getAuthenticationParameters().isEmpty()) {
								for (Argument arg : op.getAuthenticationParameters()) {
									authParamVariables.add((Value) arg);
								}
							}
							if (!op.getRequestHeaders().isEmpty()) {
								for (RequestHeader header : op.getRequestHeaders()) {
									requestHeaderVariables.add(header);
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

							e1.printStackTrace();
						}
					}

					if (endingService == null) {
						try {
							throw new Exception("EndNode was not detected on graph vertices");
						} catch (Exception e1) {

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
					// check if user has cancelled
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					// get input values
					disp.syncExec(new Runnable() {
						@SuppressWarnings("unchecked")
						@Override
						public void run() {
							if (currentService == startingService) {
								// fill inputs from right panel
								for (int i = 0; i < inputVariables.size(); i++) {
									Value var = inputVariables.get(i);
									for (int j = 0; j < ((Vector<Node>) columnb.getViewer().getInput()).size(); j++) {
										if (((Vector<Node>) columnb.getViewer().getInput()).get(j) instanceof Node) {
											Node operation = ((Vector<Node>) columnb.getViewer().getInput()).get(j);
											if (operation.getSubCategories() != null) {
												for (int k = 0; k < operation.getSubCategories().size(); k++) {
													if (operation.getSubCategories().get(k).getOwlService()
															.equals(var.getOwlService())) {
														Node node = operation.getSubCategories().get(k);
														fillInInputValues(var, node);
													}
												}
											}
										}
									}
								}
							}
						}
					});
					// check if user has cancelled
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					// get uri parameters values
					disp.syncExec(new Runnable() {
						@Override
						public void run() {
							if (currentService == startingService) {

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

								// fill request headers from right panel
								for (int i = 0; i < requestHeaderVariables.size(); i++) {
									RequestHeader header = requestHeaderVariables.get(i);

									for (int j = 0; j < requestHeaderComposite.getChildren().length; j++) {
										if (requestHeaderComposite.getChildren()[j] instanceof Label) {
											Label label = (Label) requestHeaderComposite.getChildren()[j];
											int index = label.getText().indexOf("-");
											if (index != -1 && label.getText().substring(index + 1).split("\\*")[0]
													.trim().equals(header.getName())) {
												if (((Text) requestHeaderComposite.getChildren()[j + 1]).isEnabled())
													header.setValue(((Text) requestHeaderComposite.getChildren()[j + 1])
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
									} else if (((OwlService) ((MyNode) graphNode.getData()).getObject())
											.equals(currentService)) {
										destGraph.setSelection(new GraphItem[] { graphNode });
										graphNode.highlight();
									}
								}

							}
						});

						// call the service
						if (currentService.getOperation() != null) {
							try {
								if (currentService.getOperation().getDomain() == null) {
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
								} else if (currentService.getOperation().getType().equalsIgnoreCase("SOAP"))
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
								Activator.log("Error occured while calling "
										+ currentService.getOperation().getName().toString(), e);
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

										ArrayList<Value> temp = new ArrayList<Value>();
										for (Argument arg : serviceOutputs) {
											temp.add((Value) arg);
											for (Argument sub : arg.getSubtypes()) {
												getSubtypes(temp, sub, false);
											}
										}
										OwlService array = null;
										for (Argument arg : temp) {
											for (OwlService service : graph.getVertices()) {
												if (service.equals(((Value) arg).getOwlService())) {
													try {
														service.setContent((Value) arg);
													} catch (Exception e) {
														Activator.log("Error setting outputs content", e);
														e.printStackTrace();
													}
												}
											}
										}

										Vector<Node> matchedOutputNodes = new Vector<Node>();

										for (OwlService service : graph.getVertices()) {
											if (service.getisMatchedIO()
													&& !service.getArgument().getMatchedInputs().isEmpty()
													&& service.getArgument().getBelongsToOperation().getName()
															.toString().equals(currentService.getOperation().getName()
																	.toString())) {

												boolean isMemberOfArray = false;

												for (Object parent : service.getArgument().getParent()) {
													if (parent instanceof Argument)
														if (((Argument) parent).isArray()) {
															isMemberOfArray = true;
														}
												}
												// if predecessor is array
												if (isMemberOfArray || service.getArgument().isArray()) {
													OwlService initialArray = getInitialArray(service, graph, false);
													int subNum = 0;
													for (Argument sub : initialArray.getArgument().getSubtypes()) {
														if (sub.getOwlService().getisMatchedIO())
															subNum++;
													}
													boolean arrayIsMatched = false;

													if (subNum == initialArray.getArgument().getSubtypes().size()) {
														arrayIsMatched = true;

													}
													boolean successorIsMemberOfArray = false;
													for (Argument successor : service.getArgument()
															.getMatchedInputs()) {
														// for each successor
														for (Object parent : successor.getParent()) {
															if (parent instanceof Argument)
																if (((Argument) parent).isArray()) {
																	successorIsMemberOfArray = true;
																}
														}
														OwlService successorMatched = null;
														for (OwlService suc : graph.getSuccessors(service)) {
															successorMatched = suc;
														}
														// if successor is also
														// member of array
														if (successorIsMemberOfArray && arrayIsMatched) {
															OwlService successorInitialArray = getInitialArray(
																	successorMatched, graph, true);
															// clear matched
															// elements

															int i = 0;
															if (arrayIsMatched && array != initialArray) {
																for (Value element : initialArray.getArgument()
																		.getElements()) {
																	Value value = new Value(element);
																	value.setOwlService(successorInitialArray
																			.getArgument().getOwlService());
																	value.setName(successorInitialArray.getArgument()
																			.getType() + "[" + i + "]");
																	successorInitialArray.getArgument().getElements()
																			.add(value);
																	for (Argument input : inputVariables) {
																		if (input.getOwlService()
																				.equals(successorInitialArray)) {
																			ArrayList<Value> list = input.getElements();

																			for (Iterator<Value> iterator = list
																					.iterator(); iterator.hasNext();) {
																				Value v = iterator.next();
																				if (v != null) {
																					iterator.remove();
																				}
																			}
																			input.getElements().add(value);
																			break;
																		}
																	}

																	i++;
																}
																array = initialArray;
															} else {

															}
														} else if (successor.isArray()) {
															// successor is
															// array
															for (Value element : service.getArgument().getElements()) {
																successor.getElements().add(element);
															}
														} else {

															// successor is
															// object or array
															// of objects (but
															// not fully
															// matched)
															OwlService initialInput = getInitialInput(service, graph);
															boolean r = successorIsMemberOfArray;
															// OwlService
															// successorInitialArray
															// = null;
															// if (r){
															// successorInitialArray
															// =
															// getInitialArray(
															// successorMatched,
															// graph, true);
															// }
															ServiceCompositionView.showOutputs(
																	(Value) initialInput.getArgument(), null,
																	matchedOutputNodes, jungGraph);

															disp.syncExec(new Runnable() {
																@Override
																public void run() {
																	MatchOutputDialog dialog = new MatchOutputDialog(
																			shell);
																	dialog.setDisp(disp);
																	dialog.setArrayNodes(matchedOutputNodes);
																	dialog.setName(successor.getName().toString());
																	dialog.setOutputService(service);
																	dialog.create();
																	dialog.setDialogLocation();
																	if (dialog.open() == Window.OK) {
																		String value = dialog.getValue();
																		((Value) successor).setValue(value);
																		boolean found = false;
																		if (r) {
																			OwlService matchedSuccessor = null;
																			for (OwlService suc : graph
																					.getSuccessors(service)) {
																				matchedSuccessor = suc;
																			}
																			OwlService successorInitialArray = getInitialArray(
																					matchedSuccessor, graph, true);
																			for (Argument input : subinputVariables) {
																				if (input.getOwlService().equals(
																						successorInitialArray)) {
																					Argument element0 = input
																							.getElements().get(0);
																					for (Argument arg : element0
																							.getElements()) {
																						if (arg.getOwlService()
																								.equals(successor
																										.getOwlService())) {
																							((Value) arg)
																									.setValue(value);
																							found = true;
																							break;
																						}
																					}
																				}
																			}

																		}
																		if (!found) {
																			for (Argument input : inputVariables) {
																				if (input.getOwlService().equals(
																						successor.getOwlService())) {
																					found = true;
																					((Value) input).setValue(value);
																				}
																			}
																		}
																		if (!found) {
																			for (Argument input : subinputVariables) {

																				if (input.getOwlService().equals(
																						successor.getOwlService())) {
																					found = true;
																					((Value) input).setValue(value);
																				}

																			}
																		}

																	} else {
																		return;
																	}
																}
															});
														}
														break;
													}

													// if predecessor is object
												} else {
													boolean successorIsMemberOfArray = false;
													for (Argument successor : service.getArgument()
															.getMatchedInputs()) {
														// for each successor
														for (Object parent : successor.getParent()) {
															if (parent instanceof Argument)
																if (((Argument) parent).isArray()) {
																	successorIsMemberOfArray = true;
																}
														}

														if (successor.isArray()) {
															Argument in;
															try {
																in = new Argument(
																		successor.getName().toString() + "[0]", "",
																		successor.isTypeOf(), false,
																		successor.isNative(), successor.getSubtypes());
																in.setOwlService(successor.getOwlService());
																Value value = new Value(in);
																value.setValue(
																		((Value) service.getArgument()).getValue());
																successor.getElements().add(0, value);
															} catch (Exception e) {

																// Auto-generated
																// catch block
																e.printStackTrace();
															}
														} else if (successorIsMemberOfArray) {

															// TODO complex
															// array
														} else {
															((Value) successor).setValue(
																	((Value) service.getArgument()).getValue());
															boolean found = false;

															for (Argument input : inputVariables) {
																if (input.getOwlService()
																		.equals(successor.getOwlService())) {
																	found = true;
																	((Value) input).setValue(
																			((Value) service.getArgument()).getValue());
																	break;
																}
															}
															if (!found) {
																for (Argument input : subinputVariables) {

																	if (input.getOwlService()
																			.equals(successor.getOwlService())) {
																		found = true;
																		((Value) input).setValue(
																				((Value) service.getArgument())
																						.getValue());
																		break;
																	}

																}
															}

														}
													}

												}
											}
										}
									}
								});
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
									// if (!output.isArray()) {
									if (output.getSubtypes().isEmpty()) {
										previousServiceOutVariables.add((Value) output);
									}
									for (Argument sub : output.getSubtypes()) {
										// if (!sub.isArray()) {
										getSubtypes(previousServiceOutVariables, sub, true);
										// }

									}
									// }
								}
							}
						}

						currentService = checkCondition(currentService, possibleServices, previousServiceOutVariables,
								graph);
						// Update matched variable values 2
						// if (currentService != null) {
						// if (currentService.getOperation() != null) {
						// for (OwlService service : graph.getVertices()) {
						// boolean isMatched = false;
						// for (int i = 0; i <
						// currentService.getOperation().getInputs().size();
						// i++) {
						// if (service.getArgument() != null) {
						// for (OwlService successor :
						// graph.getSuccessors(service)) {
						// if (successor.getArgument() != null) {
						// if (successor.getisMatchedIO() &&
						// service.getisMatchedIO()) {
						// isMatched = true;
						// }
						// if (successor.getArgument()
						// .getName().toString().equals(currentService.getOperation()
						// .getInputs().get(i).getName().toString())
						// && isMatched) {
						// ((Value)
						// currentService.getOperation().getInputs().get(i))
						// .setValue(((Value)
						// service.getArgument()).getValue());
						// }
						// }
						// }
						//
						// }
						// }
						// }
						// }
						// }

						long stopTime = System.currentTimeMillis();
						long elapsedTime = stopTime - startTime;
						if (elapsedTime < 500) {
							try {
								Thread.sleep(500 - elapsedTime);
							} catch (InterruptedException e) {

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

							}
						});

					}

					monitor.done();
					return Status.OK_STATUS;
				} catch (Exception ex) {
					Activator.log("Error while running the workflow", ex);
					ex.printStackTrace();
					monitor.done();
					return Status.CANCEL_STATUS;
				} finally {
					monitor.done();
				}
			}
		};

		runWorkflowJob.schedule();
		return "";
	}

	public static OwlService getInitialArray(OwlService matchedVar,
			edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph, boolean isSuccessor) {
		OwlService initialArray = matchedVar;
		if (isSuccessor) {
			for (OwlService successor : graph.getSuccessors(matchedVar)) {
				if (successor.getArgument() != null) {
					if (successor.getArgument().isArray()) {
						initialArray = getInitialArray(successor, graph, true);
					} else {
						break;
					}
				} else {
					break;
				}
			}
		} else {
			for (OwlService predecessor : graph.getPredecessors(matchedVar)) {
				if (predecessor.getArgument() != null) {
					if (predecessor.getArgument().isArray()) {
						initialArray = getInitialArray(predecessor, graph, false);
					} else {
						break;
					}
				} else {
					break;
				}
			}
		}
		return initialArray;
	}

	public static OwlService getInitialInput(OwlService matchedVar,
			edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph) {
		OwlService initialInput = matchedVar;

		for (OwlService predecessor : graph.getPredecessors(matchedVar)) {
			if (predecessor.getArgument() != null) {

				initialInput = getInitialInput(predecessor, graph);

			} else {
				break;
			}
		}

		return initialInput;
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
					// if (!sub.isArray()) {
					getSubtypes(suboutputVariables, subsub, noObjects);
					// }
				} else {
					getSubtypes(suboutputVariables, subsub, noObjects);
				}
			}
		}
		return suboutputVariables;
	}

	private void fillInOutputValues(ArrayList<Value> list, Vector<Node> nodes,
			edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph) {

		for (int i = 0; i < list.size(); i++) {
			Value var = list.get(i);
			ServiceCompositionView.showOutputs(var, null, nodes, graph);
		}

	}

	private void fillInInputValues(Value var, Node node) {
		String[] datatypes = new String[] { "string", "long", "int", "float", "double", "dateTime", "boolean" };

		if (((Value) var).isArray() && RAMLCaller.stringIsItemFromList(((Value) var).getType(), datatypes)) {
			for (int k = 0; k < node.getSubCategories().size(); k++) {
				if (node.getSubCategories().get(k) != null) {
					Argument in;
					try {
						in = new Argument(var.getName().toString() + "[" + k + "]", "", var.isTypeOf(), false,
								var.isNative(), var.getSubtypes());
						for (Argument arg : var.getMatchedInputs()) {
							in.getMatchedInputs().add(arg);
						}
						in.setOwlService(var.getOwlService());
						Value value = new Value(in);
						// Value value = Value.getValue(out);
						value.setValue(node.getSubCategories().get(k).getValue());
						var.getElements().add(value);
					} catch (Exception e) {

						e.printStackTrace();
					}
				}

			}
		} else if (((Value) var).isArray() && !RAMLCaller.stringIsItemFromList(((Value) var).getType(), datatypes)) {

			for (int k = 0; k < node.getSubCategories().size(); k++) {
				// create and add site[0]element, device[0]
				Argument in = new Argument(var);
				in.setOwlService(var.getOwlService());
				in.setName(var.getType() + "[" + k + "]");
				Value value = new Value(in);

				for (Argument sub : var.getSubtypes()) {
					if (sub.isArray()) {
						Argument out1 = new Argument(sub);
						out1.setOwlService(sub.getOwlService());
						out1.setName(sub.getName().toString());
						Value value2 = new Value(out1);
						Node object = (Node) node.getSubCategories().get(k);
						for (int i = 0; i < object.getSubCategories().size(); i++) {
							int index = object.getSubCategories().get(i).getName().toString().indexOf('[');
							if (value2.getName().toString().equals(
									object.getSubCategories().get(i).getName().toString().substring(0, index - 1))) {
								fillInInputValues(value2, object.getSubCategories().get(i));
								value.getElements().add(value2);
								break;
							}
						}

					} else {

						// create device
						Argument in1 = new Argument(sub);
						in1.setOwlService(sub.getOwlService());
						in1.setName(sub.getName().toString());
						Value value1 = new Value(in1);
						Node object = (Node) node.getSubCategories().get(k);
						for (int i = 0; i < object.getSubCategories().size(); i++) {
							int index = object.getSubCategories().get(i).getName().toString().indexOf('[');
							if (value1.getName().toString().equals(
									object.getSubCategories().get(i).getName().toString().substring(0, index - 1))) {
								fillInInputValues(value1, (object.getSubCategories().get(i)));
								value.getElements().add(value1);
								break;
							}
						}

					}
				}
				var.getElements().add(value);
			}

		} else if (!((Value) var).isArray() && !RAMLCaller.stringIsItemFromList(((Value) var).getType(), datatypes)) {

			for (int k = 0; k < node.getSubCategories().size(); k++) {
				for (Argument sub : var.getSubtypes()) {
					int index = node.getSubCategories().get(k).getName().toString().indexOf('[');
					if (((Value) sub).getName().toString()
							.equals(node.getSubCategories().get(k).getName().toString().substring(0, index - 1))) {
						fillInInputValues((Value) sub, (Node) node.getSubCategories().get(k));

					}
				}
			}

		} else {
			var.setValue(node.getValue());
		}

	}

	private OwlService checkCondition(OwlService source, HashMap<OwlService, Connector> candidates,
			ArrayList<Value> allVariables, edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph) {
		double prevThreshold = Similarity.levenshteinThreshold;
		Similarity.levenshteinThreshold = 0;
		Iterator<Entry<OwlService, Connector>> it = candidates.entrySet().iterator();
		ArrayList<OwlService> resultCandidates = new ArrayList<OwlService>();
		HashMap<OwlService, String> text = new HashMap<OwlService, String>();
		double bestVal = VALUE_SIMILARITY_THRESHOLD;
		while (it.hasNext()) {
			Map.Entry<OwlService, Connector> pair = (Map.Entry<OwlService, Connector>) it.next();
			double val = checkSingleCondition(source, pair.getValue(), pair.getKey(), allVariables, graph);
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
			OwlService result = checkCondition(source, candidates, allVariables, graph);
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
			ArrayList<Value> allVariables, edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph) {
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
		boolean equality = false;
		if (logicResult) {
			int index;
			if ((index = sourceText.indexOf("==")) != -1) {
				conditionText = sourceText.substring(index + 2);
				sourceText = sourceText.substring(0, index);
				equality = true;
			} else if ((index = sourceText.indexOf("!=")) != -1) {
				conditionText = sourceText.substring(index + 2);
				sourceText = sourceText.substring(0, index);
				negativeLogic = !negativeLogic;
				equality = true;
			} else if ((index = sourceText.indexOf("=")) != -1) {
				conditionText = sourceText.substring(index + 1);
				sourceText = sourceText.substring(0, index);
				equality = true;
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
				// if (val.getValue().replaceAll("[^\\.0123456789]",
				// "").isEmpty() != conditionText
				// .replaceAll("[^\\.0123456789]", "").isEmpty())
				// continue;
				double match = Similarity.similarity(val.getName().toString(), sourceText);
				if (match >= bestMatch) {
					bestMatch = match;
					bestValue = val;
				}
			}
		}
		if (bestValue == null)
			return 1;
		double retValue = 0;
		// if bestValue belongs to an array, check each element and if at least
		// one element fulfils the condition then condition is true
		boolean isMemberOfArray = false;

		for (Object parent : bestValue.getParent()) {
			if (parent instanceof Argument)
				if (((Argument) parent).isArray()) {
					isMemberOfArray = true;
				}
		}
		if (isMemberOfArray) {
			OwlService bestValueService = null;
			for (OwlService serv : graph.getVertices()) {
				if (serv.equals(bestValue.getOwlService())) {
					bestValueService = serv;
					break;
				}
			}
			OwlService initialArray = getInitialArray(bestValueService, graph, false);
			for (Argument element : initialArray.getArgument().getElements()) {
				for (Argument arg : element.getElements()) {
					if (arg.getOwlService() != null) {
						if (arg.getOwlService().equals(bestValue.getOwlService())) {
							if (compareMode == 0) {
								retValue = Similarity.similarity(conditionText.trim(), ((Value) arg).getValue());
							} else {
								double conditionVal = Double
										.parseDouble(conditionText.replaceAll("[^\\.0123456789]", ""));
								double variableValue = Double
										.parseDouble(((Value) arg).getValue().replaceAll("[^\\.0123456789]", ""));
								if (compareMode == 1) {
									if (conditionVal > variableValue) {
										retValue = 1;
										if (negativeLogic)
											retValue = 1 - retValue;
										break;
									}
								} else if (compareMode == -1) {
									if (conditionVal < variableValue) {
										retValue = 1;
										if (negativeLogic)
											retValue = 1 - retValue;
										break;
									}
								}
							}

						}
					}
				}
			}
			return retValue;
		}
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
			// for (Argument uriParam : operation.getUriParameters())
			// getUriParameters().add(new Value(uriParam));
			for (Argument authParam : operation.getAuthenticationParameters())
				getAuthenticationParameters().add(new Value(authParam));
			for (RequestHeader header : operation.getRequestHeaders())
				getRequestHeaders().add(header);
		}
	}

}
