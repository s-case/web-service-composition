package eu.scasefp7.eclipse.servicecomposition.repository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.jena.riot.RDFDataMgr;
import org.eclipse.core.resources.ResourcesPlugin;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.ontology.impl.IndividualImpl;

import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;

public class WSOntology {
	// uris of the ontology file
	private String SOURCE = "http://www.scasefp7.eu/wsOntology.owl";
	private String NS = SOURCE + "#";

	private OntModel ontologyModel;
	public Set<String> uris = new HashSet<String>();

	public WSOntology() {

		try {
			String path = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
					+ "/.metadata/.plugins/eu.scasefp7.servicecomposition/ontology";
			Model base = RDFDataMgr.loadModel(path + "/WS.owl");
			ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, base);

			calculateUris();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void calculateUris() {
		// load uris
		for (StmtIterator i = ontologyModel.listStatements(); i.hasNext();) {
			Statement s = i.next();
			if (!s.getSubject().isAnon()) {
				uris.add(s.getSubject().getURI().toLowerCase());

			}
			uris.add(s.getPredicate().getURI().toLowerCase());
			if (s.getObject().isResource() && !s.getResource().isAnon()) {
				uris.add(s.getResource().getURI().toLowerCase());

			}
		}
	}

	public List<ApplicationDomain> getAllDomainForMenu() {
		List<ApplicationDomain> res = new ArrayList<ApplicationDomain>();
		ApplicationDomain dom;
		OntClass cl = ontologyModel.getOntClass(NS + "ApplicationDomains");
		List list = cl.listInstances().toList();
		for (int i = 0; i < list.size(); i++) {
			IndividualImpl ind = (IndividualImpl) list.get(i);
			dom = new ApplicationDomain();
			dom.setUri(ind.getURI());
			dom.setName(getOperationNameFromURI(ind.getURI()));
			res.add(dom);
		}
		return res;
	}

	public String getOperationNameFromURI(String uri) {
		IndividualImpl ind = (IndividualImpl) ontologyModel.getIndividual(uri);
		DatatypeProperty prop = ontologyModel.getDatatypeProperty(NS + "hasName");
		if (ind.getPropertyValue(prop) != null) {
			return ind.getPropertyValue(prop).asLiteral().getString();
		}
		return "";
	}

	public void createNewWSOperation(String projectName, ArrayList<OwlService> inputs,
			ArrayList<Argument> uriParameters, ArrayList<OwlService> resultVariables, String serviceURL,
			String applicationDomainURI, String crudVerb) {
		String str = "";

		str = projectName.replace(" ", "_").replaceAll("[^\\p{L}\\p{Nd}]", "");
		if (Character.isDigit(str.charAt(0))) {
			str = "operation";
		}
		str = changeUri(str);
		OntClass className = ontologyModel.getOntClass(NS + "operation");
		IndividualImpl operInd = (IndividualImpl) ontologyModel.createIndividual(NS + str, className);
		uris.add(operInd.getURI().toLowerCase());
		// add application domain
		if (applicationDomainURI != null && !applicationDomainURI.equals("")) {
			ObjectProperty hasServiceDomain = ontologyModel.getObjectProperty(NS + "hasServiceDomain");
			operInd.addProperty(hasServiceDomain, ontologyModel.getIndividual(applicationDomainURI));
		}
		// add hasName
		DatatypeProperty hasName = ontologyModel.getDatatypeProperty(NS + "hasName");
		operInd.addProperty(hasName, projectName);
		// add belongsToURL
		DatatypeProperty belongsToURL = ontologyModel.getDatatypeProperty(NS + "belongsToURL");
		operInd.addProperty(belongsToURL, serviceURL);
		// add resource path
		DatatypeProperty hasResourcePath = ontologyModel.getDatatypeProperty(NS + "hasResourcePath");
		operInd.addProperty(hasResourcePath, "rest/result/query");
		// add crud operation
		DatatypeProperty hasCRUDVerb = ontologyModel.getDatatypeProperty(NS + "hasCRUDVerb");
		operInd.addProperty(hasCRUDVerb, crudVerb);
		DatatypeProperty belongsToWSType = ontologyModel.getDatatypeProperty(NS + "belongsToWSType");
		operInd.addProperty(belongsToWSType, "RESTful");
		DatatypeProperty hasResponseType = ontologyModel.getDatatypeProperty(NS + "hasResponseType");
		operInd.addProperty(hasResponseType, "json");
		operInd.addProperty(hasResponseType, "xml");
		// add service access info
		ObjectProperty hasServiceAccessInfo = ontologyModel.getObjectProperty(NS + "hasServiceAccessInfo");
		OntClass serviceAccessInfoClass = ontologyModel.getOntClass(NS + "ServiceAccessInfo");
		String accessInfoname = "ServiceAccessInfo";
		accessInfoname = changeUri(accessInfoname);
		IndividualImpl accessInd = (IndividualImpl) ontologyModel.createIndividual(NS + accessInfoname,
				serviceAccessInfoClass);
		uris.add(accessInd.getURI().toLowerCase());
		DatatypeProperty hasDescription = ontologyModel.getDatatypeProperty(NS + "hasDescription");
		String description = "";
		for (String s : projectName.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
			description = description + " " + s;
		}
		accessInd.addProperty(hasDescription, description);
		operInd.addProperty(hasServiceAccessInfo, accessInd);

		// create isPrototype prop
		DatatypeProperty isPrototype = ontologyModel.getDatatypeProperty(NS + "isPrototype");
		operInd.addProperty(isPrototype, "false");
		// create belongsToUser prop
		// TODO it should not be hardcoded
		DatatypeProperty belongsToUser = ontologyModel.getDatatypeProperty(NS + "belongsToUser");
		operInd.addProperty(belongsToUser, "kgiannou@iti.gr");
		// create inputs
		ObjectProperty hasInput = ontologyModel.getObjectProperty(NS + "hasInput");
		OntClass conceptClass = ontologyModel.getOntClass(NS + "Concept");
		OntClass datatypeClass = ontologyModel.getOntClass(NS + "Datatype");
		DatatypeProperty isTypeOf = ontologyModel.getDatatypeProperty(NS + "isTypeOf");
		DatatypeProperty isRequired = ontologyModel.getDatatypeProperty(NS + "isRequired");
		DatatypeProperty isArray = ontologyModel.getDatatypeProperty(NS + "isArray");

		createOutputs(hasInput, operInd, inputs, "input");

		// for (int i = 0; i < inputs.size(); i++) {
		// if (!inputs.get(i).getisMatchedIO()) {
		// String name = inputs.get(i).getArgument().getName().toString();
		// name = changeUri(name);
		// // check if concept already exists
		// IndividualImpl nativeObjectInd = null;
		// IndividualImpl noInd = (IndividualImpl)
		// ontologyModel.createIndividual(NS + name, conceptClass);
		// uris.add(noInd.getURI().toLowerCase());
		// // create has name prop
		// noInd.addProperty(hasName,
		// inputs.get(i).getArgument().getName().getContent());
		// // create is Required prop
		// noInd.addProperty(isRequired,
		// String.valueOf(inputs.get(i).getArgument().isRequired()));
		// // create has isTypeOf prop
		// if (inputs.get(i).getArgument().isTypeOf().equals("BodyParameter")) {
		// noInd.addProperty(isTypeOf, "BodyParameter");
		// } else {
		// noInd.addProperty(isTypeOf, "QueryParameter");
		// }
		// // create has type prop
		// String type = inputs.get(i).getArgument().getType();
		// Iterator it = datatypeClass.listInstances();
		// String uri = "";
		// IndividualImpl datatypeInd = null;
		// while (it.hasNext()) {
		// datatypeInd = (IndividualImpl) it.next();
		// if (datatypeInd.getLocalName().equalsIgnoreCase(type)) {
		// uri = datatypeInd.getURI();
		// break;
		// }
		// }
		// if (datatypeInd != null) {
		// ObjectProperty hasType = ontologyModel.getObjectProperty(NS +
		// "hasType");
		// noInd.addProperty(hasType, datatypeInd);
		// }
		// operInd.addProperty(hasInput, noInd);
		// }
		// }
		// commented since uri params are considered as inputs now
		// for (int i = 0; i < uriParameters.size(); i++) {
		// String name = uriParameters.get(i).getName().toString();
		// name = changeUri(name);
		// // check if concept already exists
		// IndividualImpl nativeObjectInd = null;
		// IndividualImpl noInd = (IndividualImpl)
		// ontologyModel.createIndividual(NS + name, conceptClass);
		// uris.add(noInd.getURI().toLowerCase());
		// // create has name prop
		// noInd.addProperty(hasName,
		// uriParameters.get(i).getName().getContent());
		// // create is Required prop
		// noInd.addProperty(isRequired,
		// String.valueOf(uriParameters.get(i).isRequired()));
		// // create has type prop
		// String type = uriParameters.get(i).getType();
		// Iterator it = datatypeClass.listInstances();
		// String uri = "";
		// IndividualImpl datatypeInd = null;
		// while (it.hasNext()) {
		// datatypeInd = (IndividualImpl) it.next();
		// if (datatypeInd.getLocalName().equalsIgnoreCase(type)) {
		// uri = datatypeInd.getURI();
		// break;
		// }
		// }
		// if (datatypeInd != null) {
		// ObjectProperty hasType = ontologyModel.getObjectProperty(NS +
		// "hasType");
		// noInd.addProperty(hasType, datatypeInd);
		// }
		// operInd.addProperty(hasInput, noInd);
		// }
		ObjectProperty hasOutput = ontologyModel.getObjectProperty(NS + "hasOutput");
		createOutputs(hasOutput, operInd, resultVariables, "output");
	}

	private void createOutputs(ObjectProperty property, IndividualImpl operInd, ArrayList<OwlService> resultVariables,
			String IOtype) {
		OntClass conceptClass = ontologyModel.getOntClass(NS + "Concept");
		OntClass datatypeClass = ontologyModel.getOntClass(NS + "Datatype");
		DatatypeProperty hasName = ontologyModel.getDatatypeProperty(NS + "hasName");
		DatatypeProperty isRequired = ontologyModel.getDatatypeProperty(NS + "isRequired");
		DatatypeProperty isArray = ontologyModel.getDatatypeProperty(NS + "isArray");
		DatatypeProperty isTypeOf = ontologyModel.getDatatypeProperty(NS + "isTypeOf");
		for (int i = 0; i < resultVariables.size(); i++) {
			OwlService s = resultVariables.get(i);
			Argument arg = s.getArgument();
			if ((IOtype.equals("input") && !s.getisMatchedIO()) || (IOtype.equals("output"))) {
				if (arg.getSubtypes().size() == 0) {
					// it is native object

					String name = arg.getName().toString();
					name = changeUri(name);
					IndividualImpl noInd = (IndividualImpl) ontologyModel.createIndividual(NS + name, conceptClass);
					uris.add(noInd.getURI().toLowerCase());
					// create has name prop
					noInd.addProperty(hasName, arg.getName().toString());
					// create is Required prop
					noInd.addProperty(isRequired, String.valueOf(arg.isRequired()));
					// create has isTypeOf prop (only for inputs)
					if (arg.isTypeOf().equals("BodyParameter")) {
						noInd.addProperty(isTypeOf, "BodyParameter");
					} else if (arg.isTypeOf().equals("QueryParameter") || arg.isTypeOf().equals("URIParameter")) {
						noInd.addProperty(isTypeOf, "QueryParameter");
					}
					// create is array prop
					if (arg.isArray()) {
						noInd.addProperty(isArray, String.valueOf(arg.isArray()));
					}
					// create has type prop
					String type = arg.getType();
					Iterator it = datatypeClass.listInstances();
					String uri = "";
					IndividualImpl datatypeInd = null;
					while (it.hasNext()) {
						datatypeInd = (IndividualImpl) it.next();
						if (datatypeInd.getLocalName().equalsIgnoreCase(type)) {
							uri = datatypeInd.getURI();
							break;
						}
					}
					if (datatypeInd != null) {
						ObjectProperty hasType = ontologyModel.getObjectProperty(NS + "hasType");

						noInd.addProperty(hasType, datatypeInd);

					}
					operInd.addProperty(property, noInd);
				} else {
					// it is complex object
					String name = arg.getName().toString();
					name = changeUri(name);
					IndividualImpl coInd = (IndividualImpl) ontologyModel.createIndividual(NS + name, conceptClass);
					uris.add(coInd.getURI().toLowerCase());
					// create has name prop
					coInd.addProperty(hasName, arg.getName().toString());
					// create is required prop
					if (arg.isRequired()) {
						coInd.addProperty(isRequired, String.valueOf(arg.isRequired()));
					}
					// create is array prop
					if (arg.isArray()) {
						coInd.addProperty(isArray, String.valueOf(arg.isArray()));
					}
					// create has isTypeOf prop (only for inputs)
					if (arg.isTypeOf().equals("BodyParameter")) {
						coInd.addProperty(isTypeOf, "BodyParameter");
					} else if (arg.isTypeOf().equals("QueryParameter") || arg.isTypeOf().equals("URIParameter")) {
						coInd.addProperty(isTypeOf, "QueryParameter");
					}
					// add children
					ObjectProperty hasType = ontologyModel.getObjectProperty(NS + "hasType");

					createIOrecursively(hasType, coInd, arg.getSubtypes());
					// ObjectProperty hasOutput =
					// ontologyModel.getObjectProperty(NS + "hasOutput");
					operInd.addProperty(property, coInd);
				}
			}
		}

	}

	private void createIOrecursively(ObjectProperty property, IndividualImpl operInd,
			ArrayList<Argument> resultVariables) {
		OntClass conceptClass = ontologyModel.getOntClass(NS + "Concept");
		OntClass datatypeClass = ontologyModel.getOntClass(NS + "Datatype");
		DatatypeProperty hasName = ontologyModel.getDatatypeProperty(NS + "hasName");
		DatatypeProperty isRequired = ontologyModel.getDatatypeProperty(NS + "isRequired");
		DatatypeProperty isArray = ontologyModel.getDatatypeProperty(NS + "isArray");
		DatatypeProperty isTypeOf = ontologyModel.getDatatypeProperty(NS + "isTypeOf");
		for (int i = 0; i < resultVariables.size(); i++) {

			Argument arg = resultVariables.get(i);
			if (arg.getSubtypes().size() == 0) {
				// it is native object

				String name = arg.getName().toString();
				name = changeUri(name);
				IndividualImpl noInd = (IndividualImpl) ontologyModel.createIndividual(NS + name, conceptClass);
				uris.add(noInd.getURI().toLowerCase());
				// create has name prop
				noInd.addProperty(hasName, arg.getName().toString());
				// create is Required prop
				noInd.addProperty(isRequired, String.valueOf(arg.isRequired()));
				// create has isTypeOf prop (only for inputs)
				if (arg.isTypeOf().equals("BodyParameter")) {
					noInd.addProperty(isTypeOf, "BodyParameter");
				} else if (arg.isTypeOf().equals("QueryParameter") || arg.isTypeOf().equals("URIParameter")) {
					noInd.addProperty(isTypeOf, "QueryParameter");
				}
				// create is array prop
				if (arg.isArray()) {
					noInd.addProperty(isArray, String.valueOf(arg.isArray()));
				}
				// create has type prop
				String type = arg.getType();
				Iterator it = datatypeClass.listInstances();
				String uri = "";
				IndividualImpl datatypeInd = null;
				while (it.hasNext()) {
					datatypeInd = (IndividualImpl) it.next();
					if (datatypeInd.getLocalName().equalsIgnoreCase(type)) {
						uri = datatypeInd.getURI();
						break;
					}
				}
				if (datatypeInd != null) {
					ObjectProperty hasType = ontologyModel.getObjectProperty(NS + "hasType");

					noInd.addProperty(hasType, datatypeInd);

				}
				operInd.addProperty(property, noInd);
			} else {
				// it is complex object
				String name = arg.getName().toString();
				name = changeUri(name);
				IndividualImpl coInd = (IndividualImpl) ontologyModel.createIndividual(NS + name, conceptClass);
				uris.add(coInd.getURI().toLowerCase());
				// create has name prop
				coInd.addProperty(hasName, arg.getName().toString());
				// create is required prop
				if (arg.isRequired()) {
					coInd.addProperty(isRequired, String.valueOf(arg.isRequired()));
				}
				// create has isTypeOf prop (only for inputs)
				if (arg.isTypeOf().equals("BodyParameter")) {
					coInd.addProperty(isTypeOf, "BodyParameter");
				} else if (arg.isTypeOf().equals("QueryParameter") || arg.isTypeOf().equals("URIParameter")) {
					coInd.addProperty(isTypeOf, "QueryParameter");
				}
				// create is array prop
				if (arg.isArray()) {
					coInd.addProperty(isArray, String.valueOf(arg.isArray()));
				}
				// add children
				ObjectProperty hasType = ontologyModel.getObjectProperty(NS + "hasType");

				createIOrecursively(hasType, coInd, arg.getSubtypes());
				operInd.addProperty(hasType, coInd);
			}
		}

	}

	public String changeUri(String str) {
		String res = str;
		Random randomGenerator = new Random();
		boolean found = true;
		while (found) {
			if (uriExists(res)) {
				res = str + "_" + randomGenerator.nextInt(10000);
				found = true;
			} else {
				found = false;
			}
		}
		return res;
	}

	public boolean uriExists(String text) {
		if (text.contains(NS)) {
			if (uris.contains(text.toLowerCase())) {
				return true;
			} else {
				return false;
			}
		} else if (uris.contains(NS.toLowerCase() + text.toLowerCase())) {
			return true;
		} else {
			return false;
		}

	}

	public void saveToOWL() {
		OutputStream out = null;
		try {
			System.out.println("saveToOWL is called..");
			System.out.println(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
					+ "/.metadata/.plugins/eu.scasefp7.servicecomposition/ontology" + "/WS.owl");
			out = new FileOutputStream(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
					+ "/.metadata/.plugins/eu.scasefp7.servicecomposition/ontology" + "/WS.owl");
			if (out != null && !out.toString().isEmpty()) {
				ontologyModel.write(out, "RDF/XML-ABBREV"); // readable rdf/xml
			}
			// out.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			if (out != null) {
				try {

					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}
}
