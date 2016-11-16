package eu.scasefp7.eclipse.servicecomposition.codeGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;

public class RestfulCodeGenerator {

	public static String generateRestfulCode(String packageName, ArrayList<OwlService> inputs,
			ArrayList<Argument> uriParameters, ArrayList<Argument> authParameters, ArrayList<OwlService> nativeInputsMatchedWithArrays, boolean containsPost) {

		boolean hasBodyInput = false;
		for (OwlService input : inputs) {
			if (input.getArgument().isTypeOf().equals("BodyParameter")) {
				hasBodyInput = true;
			}
		}
		String TAB = "    ";
		String code = "package " + packageName + ";\n" + "import " + packageName + ".WorkflowClass;\n" + "import "
				+ packageName + ".WorkflowClass.Response;\n";
		if (hasBodyInput)
			code += "import " + packageName + ".WorkflowClass.Request;\n";

		if (containsPost) {
			code += "import javax.ws.rs.Consumes;\nimport javax.ws.rs.POST;\n";
		}else{
			code += "import javax.ws.rs.GET;\n";
		}
		code += "import javax.ws.rs.Path;\nimport javax.ws.rs.Produces;\nimport javax.ws.rs.QueryParam;\nimport javax.ws.rs.core.MediaType;\n";
		code += "@Path(\"/result\")\n public class WebService {\n";
		if (containsPost) {
			code += TAB + "@POST\n" + TAB + "@Path(\"/query\")\n";
			code += TAB + "@Consumes(MediaType.APPLICATION_JSON)\n";
		} else {

			code += TAB + "@GET\n" + TAB + "@Path(\"/query\")\n";
		}
		code += TAB + "@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})\n" + TAB
				+ "public Response generateResponse(";

		String inputList = "";

		for (OwlService input : inputs) {
			if ((input.getArgument().isTypeOf().equals("QueryParameter")
					|| input.getArgument().isTypeOf().equals("URIParameter") || input.getArgument().isTypeOf().equals(""))&& !input.getisMatchedIO()) {
				if (!inputList.isEmpty())
					inputList += ", ";
				if (input.getArgument().getType().equals("String")) {
					inputList += "@QueryParam(\"" + input.getName().getJavaValidContent() + "\") "
							+ input.getArgument().getType() + " "
							+ input.getName().getJavaValidContent();
				} else if (input.getArgument().getType().equals("int")) {
					inputList += "@QueryParam(\"" + input.getName().getJavaValidContent()
							+ "\") Integer " + input.getName().getJavaValidContent();
				} else {
					String type = input.getArgument().getType();
					inputList += "@QueryParam(\"" + input.getName().getJavaValidContent() + "\") "
							+ type.substring(0, 1).toUpperCase() + type.substring(1) + " "
							+ input.getName().getJavaValidContent();
				}
			}
			
			

			// for (Argument param : uriParameters) {
			// if (!inputList.isEmpty())
			// inputList += ", ";
			// inputList += "@QueryParam(\"" +
			// param.getName().getJavaValidContent().replaceAll("[0123456789]", "") +
			// "\") String "
			// + param.getName().getJavaValidContent().replaceAll("[0123456789]", "");
			// }
		}
		for (Argument auth :authParameters){
			if (!inputList.isEmpty())
				inputList += ", ";
			inputList += "@QueryParam(\"" + auth.getName().getJavaValidContent().toLowerCase() + "\") "
						+ auth.getType() + " "
						+ auth.getName().getJavaValidContent().toLowerCase();
		}
		for (OwlService matchedInput :nativeInputsMatchedWithArrays){
			if (!inputList.isEmpty())
				inputList += ", ";
			inputList += "@QueryParam(\"" + matchedInput.getName().getJavaValidContent() + "_num"
					+ "\") Integer " + matchedInput.getName().getJavaValidContent() + "_num";
		}
		if (containsPost) {
			if (!inputList.isEmpty()) {
				inputList += ", ";
			}
			inputList += "Request request";
		}
		code += inputList;
		code += ") throws Exception {\n";
		code += TAB + TAB + "WorkflowClass newClass = new WorkflowClass();\n" + TAB + TAB
				+ "Response response = newClass.parseResponse(";
		inputList = "";

		for (OwlService input : inputs) {
			if ((input.getArgument().isTypeOf().equals("QueryParameter")
					|| input.getArgument().isTypeOf().equals("URIParameter") || input.getArgument().isTypeOf().equals("")) && !input.getisMatchedIO()) {
				if (!inputList.isEmpty())
					inputList += ", ";
				inputList += input.getName().getJavaValidContent();
			}
		}
		for (Argument auth :authParameters){
			if (!inputList.isEmpty())
				inputList += ", ";
			inputList += auth.getName().getJavaValidContent().toLowerCase();
		}
		for (OwlService matchedInput :nativeInputsMatchedWithArrays){
			if (!inputList.isEmpty())
				inputList += ", ";
			inputList += matchedInput.getName().getJavaValidContent() + "_num";
		}
		if (containsPost) {
			if (!inputList.isEmpty())
				inputList += ", ";
			inputList += "request";
		}

		// for (Argument param : uriParameters) {
		// if (!inputList.isEmpty())
		// inputList += ", ";
		// inputList += param.getName().getJavaValidContent().replaceAll("[0123456789]",
		// "");
		// }
		code += inputList;
		code += ");\n";
		code += TAB + TAB + "return response;\n";
		code += TAB + "}\n" + "}\n";

		return code;

	}

	public static void editWebXML(String path, String packageName) throws Exception {

		try {
			String filepath = path;
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(filepath);

			// Get the root element
			Node webApp = doc.getFirstChild();

			// append a new node to staff
			Element servlet = doc.createElement("servlet");
			webApp.appendChild(servlet);

			Element servletName = doc.createElement("servlet-name");
			servletName.appendChild(doc.createTextNode("TestProject-servlet"));
			servlet.appendChild(servletName);

			Element servletClass = doc.createElement("servlet-class");
			servletClass.appendChild(doc.createTextNode("com.sun.jersey.spi.container.servlet.ServletContainer"));
			servlet.appendChild(servletClass);

			Element initParam = doc.createElement("init-param");
			servlet.appendChild(initParam);
			Element paramName = doc.createElement("param-name");
			paramName.appendChild(doc.createTextNode("com.sun.jersey.config.property.packages"));
			initParam.appendChild(paramName);

			Element paramValue = doc.createElement("param-value");
			paramValue.appendChild(doc.createTextNode(packageName));
			initParam.appendChild(paramValue);

			Element loadOnStartUp = doc.createElement("load-on-startup");
			loadOnStartUp.appendChild(doc.createTextNode("1"));
			servlet.appendChild(loadOnStartUp);

			Element servletMapping = doc.createElement("servlet-mapping");
			webApp.appendChild(servletMapping);

			Element servletName2 = doc.createElement("servlet-name");
			servletName2.appendChild(doc.createTextNode("TestProject-servlet"));
			servletMapping.appendChild(servletName2);

			Element urlPattern = doc.createElement("url-pattern");
			urlPattern.appendChild(doc.createTextNode("/rest/*"));
			servletMapping.appendChild(urlPattern);

			// loop the staff child node
			NodeList list = webApp.getChildNodes();

			for (int i = 0; i < list.getLength(); i++) {

				Node node = list.item(i);

				// remove welcome-file-list
				if ("welcome-file-list".equals(node.getNodeName())) {
					webApp.removeChild(node);
				}

			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(filepath));
			transformer.transform(source, result);

			System.out.println("web.xml edited");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		}

	}

	public static void writePom(String path, String projectName, String hasSoap) {

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("project");
			doc.appendChild(rootElement);

			// set attributes to project element
			Attr xmlns = doc.createAttribute("xmlns");
			xmlns.setValue("http://maven.apache.org/POM/4.0.0");
			rootElement.setAttributeNode(xmlns);

			Attr xmlnsxsi = doc.createAttribute("xmlns:xsi");
			xmlnsxsi.setValue("http://www.w3.org/2001/XMLSchema-instance");
			rootElement.setAttributeNode(xmlnsxsi);

			Attr xsischemaLocation = doc.createAttribute("xsi:schemaLocation");
			xsischemaLocation.setValue("http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd");
			rootElement.setAttributeNode(xsischemaLocation);

			Element modelVersion = doc.createElement("modelVersion");
			modelVersion.appendChild(doc.createTextNode("4.0.0"));
			rootElement.appendChild(modelVersion);

			Element groupId = doc.createElement("groupId");
			groupId.appendChild(doc.createTextNode("eu.scasefp7.services.composite"));
			rootElement.appendChild(groupId);

			Element artifactId = doc.createElement("artifactId");
			artifactId.appendChild(doc.createTextNode(projectName));
			rootElement.appendChild(artifactId);

			Element version = doc.createElement("version");
			version.appendChild(doc.createTextNode("0.0.1-SNAPSHOT"));
			rootElement.appendChild(version);

			Element packaging = doc.createElement("packaging");
			packaging.appendChild(doc.createTextNode("war"));
			rootElement.appendChild(packaging);

			// plugins
			Element build = doc.createElement("build");
			rootElement.appendChild(build);

			Element sourceDirectory = doc.createElement("sourceDirectory");
			sourceDirectory.appendChild(doc.createTextNode("src"));
			build.appendChild(sourceDirectory);

			Element plugins = doc.createElement("plugins");
			build.appendChild(plugins);

			Element plugin = doc.createElement("plugin");
			plugins.appendChild(plugin);

			Element pluginArtifactId = doc.createElement("artifactId");
			pluginArtifactId.appendChild(doc.createTextNode("maven-compiler-plugin"));
			plugin.appendChild(pluginArtifactId);

			Element pluginVersion = doc.createElement("version");
			pluginVersion.appendChild(doc.createTextNode("3.1"));
			plugin.appendChild(pluginVersion);

			Element pluginConfiguration = doc.createElement("configuration");
			plugin.appendChild(pluginConfiguration);

			Element Confsource = doc.createElement("source");
			Confsource.appendChild(doc.createTextNode("1.8"));
			pluginConfiguration.appendChild(Confsource);

			Element Conftarget = doc.createElement("target");
			Conftarget.appendChild(doc.createTextNode("1.8"));
			pluginConfiguration.appendChild(Conftarget);

			Element plugin2 = doc.createElement("plugin");
			plugins.appendChild(plugin2);

			Element plugin2ArtifactId = doc.createElement("artifactId");
			plugin2ArtifactId.appendChild(doc.createTextNode("maven-war-plugin"));
			plugin2.appendChild(plugin2ArtifactId);

			Element plugin2Version = doc.createElement("version");
			plugin2Version.appendChild(doc.createTextNode("2.4"));
			plugin2.appendChild(plugin2Version);

			Element pluginConfiguration2 = doc.createElement("configuration");
			plugin2.appendChild(pluginConfiguration2);

			Element warSourceDirectory = doc.createElement("warSourceDirectory");
			warSourceDirectory.appendChild(doc.createTextNode("WebContent"));
			pluginConfiguration2.appendChild(warSourceDirectory);

			Element failOnMissingWebXml = doc.createElement("failOnMissingWebXml");
			failOnMissingWebXml.appendChild(doc.createTextNode("false"));
			pluginConfiguration2.appendChild(failOnMissingWebXml);

			// properties
			Element properties = doc.createElement("properties");
			rootElement.appendChild(properties);

			Element project_build_sourceEncoding = doc.createElement("project.build.sourceEncoding");
			project_build_sourceEncoding.appendChild(doc.createTextNode("UTF-8"));
			properties.appendChild(project_build_sourceEncoding);

			// repositories
			Element repositories = doc.createElement("repositories");
			rootElement.appendChild(repositories);

			Element repository = doc.createElement("repository");
			repositories.appendChild(repository);

			Element id = doc.createElement("id");
			id.appendChild(doc.createTextNode("maven2-repository.java.net"));
			repository.appendChild(id);

			Element name = doc.createElement("name");
			name.appendChild(doc.createTextNode("Java.net Repository for Maven"));
			repository.appendChild(name);

			Element url = doc.createElement("url");
			url.appendChild(doc.createTextNode("http://download.java.net/maven/2/"));
			repository.appendChild(url);

			Element layout = doc.createElement("layout");
			layout.appendChild(doc.createTextNode("default"));
			repository.appendChild(layout);

			Element repository2 = doc.createElement("repository");
			repositories.appendChild(repository2);

			Element id2 = doc.createElement("id");
			id2.appendChild(doc.createTextNode("OSSRH snapshots"));
			repository2.appendChild(id2);

			Element url2 = doc.createElement("url");
			url2.appendChild(doc.createTextNode("https://oss.sonatype.org/content/repositories/snapshots"));
			repository2.appendChild(url2);

			Element snapshots2 = doc.createElement("snapshots");
			repository2.appendChild(snapshots2);

			Element enabled2 = doc.createElement("enabled");
			enabled2.appendChild(doc.createTextNode("true"));
			snapshots2.appendChild(enabled2);

			Element updatePolicy2 = doc.createElement("updatePolicy");
			updatePolicy2.appendChild(doc.createTextNode("always"));
			snapshots2.appendChild(updatePolicy2);

			// dependencies
			Element dependencies = doc.createElement("dependencies");
			rootElement.appendChild(dependencies);

			Element dependency1 = doc.createElement("dependency");
			dependencies.appendChild(dependency1);

			Element groupId1 = doc.createElement("groupId");
			groupId1.appendChild(doc.createTextNode("com.sun.jersey"));
			dependency1.appendChild(groupId1);

			Element artifactId1 = doc.createElement("artifactId");
			artifactId1.appendChild(doc.createTextNode("jersey-server"));
			dependency1.appendChild(artifactId1);

			Element version1 = doc.createElement("version");
			version1.appendChild(doc.createTextNode("1.9"));
			dependency1.appendChild(version1);

			Element dependency2 = doc.createElement("dependency");
			dependencies.appendChild(dependency2);

			Element groupId2 = doc.createElement("groupId");
			groupId2.appendChild(doc.createTextNode("com.sun.jersey"));
			dependency2.appendChild(groupId2);

			Element artifactId2 = doc.createElement("artifactId");
			artifactId2.appendChild(doc.createTextNode("jersey-client"));
			dependency2.appendChild(artifactId2);

			Element version2 = doc.createElement("version");
			version2.appendChild(doc.createTextNode("1.9"));
			dependency2.appendChild(version2);

			Element dependency3 = doc.createElement("dependency");
			dependencies.appendChild(dependency3);

			Element groupId3 = doc.createElement("groupId");
			groupId3.appendChild(doc.createTextNode("com.googlecode.json-simple"));
			dependency3.appendChild(groupId3);

			Element artifactId3 = doc.createElement("artifactId");
			artifactId3.appendChild(doc.createTextNode("json-simple"));
			dependency3.appendChild(artifactId3);

			Element version3 = doc.createElement("version");
			version3.appendChild(doc.createTextNode("1.1"));
			dependency3.appendChild(version3);

			Element dependency4 = doc.createElement("dependency");
			dependencies.appendChild(dependency4);

			Element groupId4 = doc.createElement("groupId");
			groupId4.appendChild(doc.createTextNode("com.owlike"));
			dependency4.appendChild(groupId4);

			Element artifactId4 = doc.createElement("artifactId");
			artifactId4.appendChild(doc.createTextNode("genson"));
			dependency4.appendChild(artifactId4);

			Element version4 = doc.createElement("version");
			version4.appendChild(doc.createTextNode("1.3"));
			dependency4.appendChild(version4);

			Element dependency5 = doc.createElement("dependency");
			dependencies.appendChild(dependency5);

			Element groupId5 = doc.createElement("groupId");
			groupId5.appendChild(doc.createTextNode("com.google.code.gson"));
			dependency5.appendChild(groupId5);

			Element artifactId5 = doc.createElement("artifactId");
			artifactId5.appendChild(doc.createTextNode("gson"));
			dependency5.appendChild(artifactId5);

			Element version5 = doc.createElement("version");
			version5.appendChild(doc.createTextNode("2.3.1"));
			dependency5.appendChild(version5);

			if (hasSoap.equals("true")) {
				Element dependency6 = doc.createElement("dependency");
				dependencies.appendChild(dependency6);

				Element groupId6 = doc.createElement("groupId");
				groupId6.appendChild(doc.createTextNode("eu.scasefp7"));
				dependency6.appendChild(groupId6);

				Element artifactId6 = doc.createElement("artifactId");
				artifactId6.appendChild(doc.createTextNode("scase-wsparser"));
				dependency6.appendChild(artifactId6);

				Element version6 = doc.createElement("version");
				version6.appendChild(doc.createTextNode("1.0.1-SNAPSHOT"));
				dependency6.appendChild(version6);

				Element classifier6 = doc.createElement("classifier");
				classifier6.appendChild(doc.createTextNode("jar-with-dependencies"));
				dependency6.appendChild(classifier6);
			} else {
				Element dependency6 = doc.createElement("dependency");
				dependencies.appendChild(dependency6);

				Element groupId6 = doc.createElement("groupId");
				groupId6.appendChild(doc.createTextNode("org.apache.httpcomponents"));
				dependency6.appendChild(groupId6);

				Element artifactId6 = doc.createElement("artifactId");
				artifactId6.appendChild(doc.createTextNode("httpmime"));
				dependency6.appendChild(artifactId6);

				Element version6 = doc.createElement("version");
				version6.appendChild(doc.createTextNode("4.4.1"));
				dependency6.appendChild(version6);

				Element dependency7 = doc.createElement("dependency");
				dependencies.appendChild(dependency6);

				Element groupId7 = doc.createElement("groupId");
				groupId7.appendChild(doc.createTextNode("org.apache.httpcomponents"));
				dependency7.appendChild(groupId7);

				Element artifactId7 = doc.createElement("artifactId");
				artifactId7.appendChild(doc.createTextNode("httpcore"));
				dependency7.appendChild(artifactId7);

				Element version7 = doc.createElement("version");
				version7.appendChild(doc.createTextNode("4.4.1"));
				dependency7.appendChild(version7);
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(path + "/pom.xml"));

			// Output to console for testing
			// StreamResult result2 = new StreamResult(System.out);

			transformer.transform(source, result);

			System.out.println("pom.xml generated!");

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}

	}

	// private void addDependency(Document doc,String id, String groupIdText,
	// String artifactIdText, String versionText){
	//
	// Element dependency = doc.createElement("dependency");
	// dependencies.appendChild(dependency);
	//
	// Element groupId = doc.createElement("groupId");
	// groupId.appendChild(doc.createTextNode("org.apache.axis2"));
	// dependency.appendChild(groupId);
	//
	// Element artifactId = doc.createElement("artifactId");
	// artifactId.appendChild(doc.createTextNode("org.apache.axis2.osgi"));
	// dependency.appendChild(artifactId);
	//
	// Element version = doc.createElement("version");
	// version.appendChild(doc.createTextNode("1.5.2"));
	// dependency.appendChild(version);
	// }

}
