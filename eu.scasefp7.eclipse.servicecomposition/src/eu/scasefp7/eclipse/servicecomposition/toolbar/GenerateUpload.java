package eu.scasefp7.eclipse.servicecomposition.toolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.cli.MavenCli;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.eclipse.core.commands.ExecutionException;
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
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jst.j2ee.internal.web.archive.operations.WebFacetProjectCreationDataModelProvider;
import org.eclipse.jst.j2ee.project.facet.IJ2EEModuleFacetInstallDataModelProperties;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties.FacetDataModelMap;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;

import eu.scasefp7.eclipse.core.builder.ProjectUtils;
import eu.scasefp7.eclipse.servicecomposition.Activator;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.CallRestfulServiceCode;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.CallWSDLServiceCode;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.ConnectToMDEOntology;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.NonLinearCodeGenerator;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.RestfulCodeGenerator;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.repository.RepositoryClient;
import eu.scasefp7.eclipse.servicecomposition.repository.WSOntology;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.ui.MyTitleAreaDialog;
import eu.scasefp7.eclipse.servicecomposition.ui.SelectScaseProjectDialog;
import eu.scasefp7.eclipse.servicecomposition.views.ServiceCompositionView;
import eu.scasefp7.eclipse.servicecomposition.views.UploadCompositeService;

public class GenerateUpload {

	private ServiceCompositionView view;
	edu.uci.ics.jung.graph.Graph<OwlService, Connector> jungGraph;
	// generated project name
	private String projectName = "";
	// generated project
	IProject currentProject;
	// the s-case project
	private IProject scaseProject;
	// application domain that a sc belongs to
	private String applicationDomainURI = "";
	NonLinearCodeGenerator gGenerator = new NonLinearCodeGenerator();
	// code generation object
	private NonLinearCodeGenerator generator;
	private String pomPath = "";
	private Job createWarFileJob;
	private Job createProject;

	public GenerateUpload(ServiceCompositionView view) {
		this.view = view;
		this.jungGraph = view.getJungGraph();
		this.scaseProject = view.getScaseProject();
	}

	/**
	 * <h1>generate</h1> Creates a RESTful web service of the workflow, in a
	 * maven-based eclipse project. Method is called by pressing the appropriate
	 * button in the toolbar.
	 * 
	 * @throws Exception
	 */
	public void generate() throws Exception {

		Shell shell = view.getSite().getWorkbenchWindow().getShell();
		MyTitleAreaDialog dialog = new MyTitleAreaDialog(shell);
		dialog.create();
		dialog.setDialogLocation();
		if (dialog.open() == Window.OK) {
			System.out.println(dialog.getProjectName());
			projectName = dialog.getProjectName().trim();
			applicationDomainURI = dialog.getApplicationDomainURI();
		} else {
			return;
		}

		ArrayList<String> projectNames = new ArrayList<String>();
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			projectNames.add(project.getName());
		}
		boolean nameExists = false;
		if ((projectName != null) && (projectName.trim().length() > 0)) {

			for (String name : projectNames) {
				if (name.trim().equalsIgnoreCase(projectName)) {
					nameExists = true;
				}
			}

			IProgressMonitor monitor = new NullProgressMonitor();
			IProject existingProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName.trim());
			if (existingProject.exists() || nameExists) {
				// shell = this.getSite().getWorkbenchWindow().getShell();
				boolean result = MessageDialog.openQuestion(shell, "Project already exists",
						"A project with this name already exists. Would you like to replace it?");

				if (result) {
					// Yes Button selected
					existingProject.delete(true, false, monitor);
				} else {
					// No Button selected
					return;
				}

			}

			createProject = new Job("Creating project..") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Creating the web service project of the workflow...", IProgressMonitor.UNKNOWN);
					// IProject project =
					// ResourcesPlugin.getWorkspace().getRoot().getProject("TestProject");

					// if (!project.exists()) {
					try {

						IProject project = getWebDataModel(projectName);
						currentProject = project;
						// project.create(monitor);
						project.open(monitor);
						// check if user has cancelled
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						// Configure the project to be a Java project and a
						// maven
						// project
						IProjectDescription description = project.getDescription();
						description.setNatureIds(new String[] { JavaCore.NATURE_ID, "org.eclipse.m2e.core.maven2Nature",
								"org.eclipse.jem.workbench.JavaEMFNature", "org.eclipse.wst.jsdt.core.jsNature",
								"org.eclipse.wst.common.modulecore.ModuleCoreNature",
								"org.eclipse.wst.common.project.facet.core.nature" });
						project.setDescription(description, monitor);

						IJavaProject javaProject = JavaCore.create(project);
						// check if user has cancelled
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
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
						IExecutionEnvironment[] executionEnvironments = executionEnvironmentsManager
								.getExecutionEnvironments();
						for (IExecutionEnvironment iExecutionEnvironment : executionEnvironments) {
							// We will look for JavaSE-1.6 as the JRE container
							// to add
							// to our classpath
							if ("JavaSE-1.8".equals(iExecutionEnvironment.getId())) {
								entries.add(JavaCore
										.newContainerEntry(JavaRuntime.newJREContainerPath(iExecutionEnvironment)));
								break;
							}
						}

						boolean hasRest = false;
						boolean hasSoap = false;
						boolean hasPost = false;
						boolean hasForeman = false;
						for (OwlService service : jungGraph.getVertices()) {
							if (service.getOperation() != null) {
								if (service.getOperation().getType().equalsIgnoreCase("Restful")) {
									hasRest = true;
									if (service.getOperation().getDomain().getCrudVerb().equalsIgnoreCase("post")) {
										hasPost = true;
									}
									if (service.getOperation().getDomain().getURI().startsWith("https://foreman.res.eng.it")){
										hasForeman = true;
									}
								}
								if (service.getOperation().getType().equalsIgnoreCase("soap")) {
									hasSoap = true;
								}
							}
						}
						// check if user has cancelled
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						// generate pom.xml
						pomPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/"
								+ javaProject.getElementName();
						RestfulCodeGenerator.writePom(pomPath, projectName, Boolean.toString(hasSoap));

						// Let's add the maven container to our classpath to let
						// the
						// maven plug-in add the dependencies computed from a
						// pom.xml file to our classpath
						IClasspathEntry mavenEntry = JavaCore.newContainerEntry(
								new Path("org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER"), new IAccessRule[0],
								new IClasspathAttribute[] { JavaCore.newClasspathAttribute(
										"org.eclipse.jst.component.dependency", "/WEB-INF/lib") },
								false);
						entries.add(mavenEntry);

						// add libs to project class path
						javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
						// check if user has cancelled
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
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

						// Now let's add our source folder and output folder to
						// our
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
						// check if user has cancelled
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						// generate code of Workflow Class
						generator = new NonLinearCodeGenerator();
						String source = generator.generateCode(jungGraph, "workflow", false, projectName);
						StringBuffer buffer = new StringBuffer();
						buffer.append("package " + pack.getElementName() + ";\n");
						buffer.append("\n");
						// String source="public class TestClass{\n"+
						// " private String name;"+ "\n" + "}";;
						buffer.append(source);
						ICompilationUnit testerClass = pack.createCompilationUnit("WorkflowClass.java",
								buffer.toString(), false, null);
								// IType type =
								// testerClass.getType("TesterClass");

						// type.createField("private String age;", null, true,
						// null);
						// check if user has cancelled
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
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
						String restCode = RestfulCodeGenerator.generateRestfulCode(pack.getElementName(), projectName,
								generator.getInputVariables(), generator.geturiParameters(), generator.getAuthParameters(), generator.getNativeInputsMatchedWithArrays(), generator.getRequestHeaderParameters(), hasPost);
						StringBuffer restBuffer = new StringBuffer();
						restBuffer.append(restCode);
						ICompilationUnit restClass = pack.createCompilationUnit("WebService.java",
								restBuffer.toString(), false, null);
						// IType type2 = restClass.getType("RestCode");

						gGenerator = generator;
						// check if user has cancelled
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						if (hasRest) {
							String code = CallRestfulServiceCode.generateCode(pack.getElementName(), hasForeman);
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
						// check if user has cancelled
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						// edit web.xml
						String path = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/"
								+ javaProject.getElementName() + "/WebContent/WEB-INF/web.xml";
						RestfulCodeGenerator.editWebXML(path, pack.getElementName());

						// refresh project
						IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
						root.refreshLocal(IResource.DEPTH_INFINITE, monitor);

					} catch (Exception e) {
						Activator.log("Error while generating java project", e);
						e.printStackTrace();
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;

				}
			};
			createProject.setUser(true);
			createProject.schedule();

		} else {
			final Display disp = shell.getDisplay();
			disp.syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openInformation(shell, "Error occured", "This is not a valid name.");
				}
			});
		}

		// }

	}

	public static IProject getWebDataModel(String projName) throws ExecutionException {

		IDataModel model = DataModelFactory.createDataModel(new WebFacetProjectCreationDataModelProvider());
		model.setProperty(IFacetDataModelProperties.FACET_PROJECT_NAME, projName);

		FacetDataModelMap map = (FacetDataModelMap) model
				.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
		IDataModel webModel = (IDataModel) map.get(IModuleConstants.JST_WEB_MODULE);
		webModel.setProperty(IJ2EEModuleFacetInstallDataModelProperties.FACET_VERSION_STR, "2.4");

		IStatus st = model.getDefaultOperation().execute(new NullProgressMonitor(), null);
		return st.isOK() ? ResourcesPlugin.getWorkspace().getRoot().getProject(projName) : null;
	}

	/**
	 * Runs maven -clean/install command which builds the project and installs
	 * artifact to the local repository
	 */
	public void install() throws Exception {

		final Shell shell = view.getSite().getWorkbenchWindow().getShell();
		final Display disp = shell.getDisplay();

		createWarFileJob = new Job("Uploading..") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Creating WAR file, uploading on server and connecting to MDE Ontology...",
						IProgressMonitor.UNKNOWN);
				if (projectName.trim().length() > 0 && pomPath.trim().length() > 0) {

					try {

						IStatus s = getXMLPath(shell, disp);
						if (s.equals(Status.CANCEL_STATUS))
							return Status.CANCEL_STATUS;

						if (scaseProject == null) {
							return Status.CANCEL_STATUS;
						}

						org.eclipse.core.resources.IContainer container = ProjectUtils.getProjectCompositionsFolder(scaseProject);
						File basedir = new File(pomPath);
						IProgressMonitor monitor2 = new NullProgressMonitor();
						IMaven maven = MavenPlugin.getMaven();
						MavenExecutionRequest request = createExecutionRequest();
						// MavenExecutionRequest request =
						// createExecutionRequest();
						request.setPom(new File(basedir, "pom.xml"));
						// request.setGoals(Arrays.asList("clean"));
						request.setGoals(Arrays.asList("install"));
						// populator.populateDefaults(request);
						MavenExecutionResult result = maven.execute(request, monitor2);
						// war file is generated with the second execution
						MavenExecutionResult result2 = maven.execute(request, monitor2);
						// boolean exists = webServiceExistsOnServer();
						UploadCompositeService newUpload = new UploadCompositeService();
						boolean exists = newUpload.exists(projectName + "-0.0.1-SNAPSHOT.war");
						if (exists) {
							disp.syncExec(new Runnable() {
								public void run() {
									boolean answer = MessageDialog.openQuestion(shell,
											"Web service already exists on server",
											"A web service with this name already exists. Would you like to update it?");

									if (answer) {
										// Yes Button selected
										try {
											newUpload.upload(
													ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
															+ "/" + projectName + "/target/" + projectName
															+ "-0.0.1-SNAPSHOT.war",
													monitor);
											if (newUpload.getUploadStatus() == 1)
												return;
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
							});

						} else {
							newUpload.upload(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/"
									+ projectName + "/target/" + projectName + "-0.0.1-SNAPSHOT.war", monitor);
							if (newUpload.getUploadStatus() == 1)
								return Status.CANCEL_STATUS;
						}
						// check if user has cancelled
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						if (newUpload.getUploadStatus() == 200) {

							// create .cservice file

							ConnectToMDEOntology.writeToXMLFile(scaseProject, gGenerator.getOperation(), container);

							// user pressed OK
							disp.syncExec(new Runnable() {
								@Override
								public void run() {
									// if (dialog.open() == Window.OK) {
									if (MessageDialog.openQuestion(shell, "Upload is complete!",
											"The web service was deployed successfully on server!\n"
													+ "Base URI: http://109.231.127.61:8080/" + currentProject.getName()
													+ "-0.0.1-SNAPSHOT/\n" + "Resource Path: rest/result/query\n\n"
													+ "Would you like to update YouREST platform with the composite web service?")) {

										ServiceCompositionView.setUpdateYouRest(true);
									} else {
										ServiceCompositionView.setUpdateYouRest(false);
									}
								}
							});

							if (ServiceCompositionView.getUpdateYouRest()) {
								try {

									try {
										WSOntology ws = new WSOntology();
										ws.createNewWSOperation(generator.getOperation().getHasName(),
												generator.getInputsWithoutMatchedVariables(),
												generator.getAuthParameters(), generator.getOutputVariables(), generator.getRepeatedOperations(),
												generator.getOperation().getBelongsToURL(), applicationDomainURI,
												generator.getOperation().getHasCRUDVerb());
										ws.saveToOWL();
										RepositoryClient cl = new RepositoryClient();
										cl.uploadOntology();
									} catch (Exception e) {

										disp.syncExec(new Runnable() {
											@Override
											public void run() {
												MessageDialog.openError(shell, "YouREST could not be updated!",
														"Due to an error, YouREST could not be updated with project "
																+ currentProject.getName());

											}
										});

									}

								} catch (Exception e) {
									e.printStackTrace();
								}
							}

						} else if (newUpload.getUploadStatus() == 0) {
							return Status.CANCEL_STATUS;
						} else {
							disp.syncExec(new Runnable() {
								@Override
								public void run() {
									MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
											"Web service could not be deployed on server. Please contact the server administrator.\nError code: "
													+ newUpload.getUploadStatus());
								}
							});
							return Status.CANCEL_STATUS;
						}

						monitor.done();
						return Status.CANCEL_STATUS;
					} catch (Exception ex) {
						Activator.log("Error while uploading", ex);
						ex.printStackTrace();
						disp.syncExec(new Runnable() {
							@Override
							public void run() {
								MessageDialog.openInformation(disp.getActiveShell(), "Info",
										currentProject.getName()
												+ " could not be uploaded.\nNo connection to the  web server.\nPlease contact the system's administrator!\n"
												+ ex.getStackTrace().toString());
							}
						});
						return Status.CANCEL_STATUS;
					}

				} else {
					disp.syncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog.openInformation(disp.getActiveShell(), "Warning",
									"Create a RESTful project first!");
						}
					});
					return Status.CANCEL_STATUS;
				}

			}

		};
		createWarFileJob.setUser(true);
		createWarFileJob.schedule();

	}

	private IStatus getXMLPath(Shell shell, Display disp) {

		if (scaseProject == null) {
			disp.syncExec(new Runnable() {

				@Override
				public void run() {
					SelectScaseProjectDialog dialog = new SelectScaseProjectDialog(shell, null, true,
							"Select an S-CASE project for the .cservice file:");

					dialog.setTitle("Project Selection");

					dialog.open();
					final Object[] selections = dialog.getResult();
					if (selections == null) {
						disp.syncExec(new Runnable() {

							@Override
							public void run() {
								MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
										"Please select or create an S-CASE project first.");
							}

						});
						return;
					}
					String scaseProjectName = ((Path) dialog.getResult()[0]).segments()[0];
					scaseProject = ResourcesPlugin.getWorkspace().getRoot().getProject(scaseProjectName);
					try {
						if (!scaseProject.getDescription().hasNature("eu.scasefp7.eclipse.core.scaseNature")) {
							disp.syncExec(new Runnable() {

								@Override
								public void run() {
									MessageDialog.openInformation(disp.getActiveShell(), "Error occured",
											"The project you selected is not an S-CASE project! Please try again!");
									scaseProject = null;

								}

							});
							return;
						}

					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			if (scaseProject == null) {
				return Status.CANCEL_STATUS;
			}

		}

		return Status.OK_STATUS;
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

}
