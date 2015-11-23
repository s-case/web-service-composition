package eu.scasefp7.eclipse.servicecomposition.importer;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;

import eu.scasefp7.eclipse.servicecomposition.Activator;
import eu.scasefp7.eclipse.servicecomposition.codeInterpreter.Value;
import eu.scasefp7.eclipse.servicecomposition.operation.ipr.CommercialCostSchema;
import eu.scasefp7.eclipse.servicecomposition.operation.ipr.Country;
import eu.scasefp7.eclipse.servicecomposition.operation.ipr.Description;
import eu.scasefp7.eclipse.servicecomposition.operation.ipr.DiscountSchema;
import eu.scasefp7.eclipse.servicecomposition.operation.ipr.ServiceAccessInfoForUsers;
import eu.scasefp7.eclipse.servicecomposition.operation.ipr.ServiceLicense;
import eu.scasefp7.eclipse.servicecomposition.operation.ipr.ServiceTrialSchema;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.transformer.Similarity.ComparableName;
import gr.iti.wsdl.wsdlToolkit.WSOperation;

/**
 * <h1>Importer</h1> This class declares data structures similar to contents of
 * <i>.owl</i> files.
 * 
 * @author Manios Krasanakis
 */
public abstract class Importer {
	public static String stringType = "String";
	public static String prefix = "http://www.scasefp7.eu/wsOntology.owl#";
	public static String classPrefix = "http://www.scasefp7.eu/wsOntology.owl#";

	protected static ObjectProperty belongsToPrototype;
	protected static DatatypeProperty belongsToURL;
	protected static DatatypeProperty belongsToWSType;
	protected static ObjectProperty hasServiceDomain;
	protected static DatatypeProperty belongsToUser;
	protected static DatatypeProperty isRequired;
	protected static DatatypeProperty hasName;
	protected static ObjectProperty hasInput;
	protected static ObjectProperty hasOutput;
	protected static DatatypeProperty hasURIParameters;
	protected static DatatypeProperty hasCRUDVerb;
	protected static DatatypeProperty hasSecurityScheme;
	protected static ObjectProperty hasType;
	protected static DatatypeProperty isPrototype;
	protected static DatatypeProperty isArray;
	protected static DatatypeProperty hasResourcePath;
	protected static OntModel ontologyModel;

	protected static HashMap<String, ApplicationDomain> domainList = new HashMap<String, ApplicationDomain>();

	/**
	 * <h1>ApplicationDomain</h1> This class contains information about a single
	 * domain.
	 */
	public static class ApplicationDomain {
		private String uri;
		private String name;
		private boolean local;
		private String resourcePath;
		private String crudVerb;
		private String securityScheme;

		ApplicationDomain(Individual ind) {
			this(ind.getURI());
		}

		public ApplicationDomain(String uri) {
			this.uri = uri;
			if (uri.contains("script")) {
				name = uri.substring(uri.lastIndexOf("/") + 1, uri.lastIndexOf("."));
			} else if (uri.contains("localhost") || uri.contains("160.40.50.176")) {
				name = "";
			} else {
				name = uri.substring(uri.lastIndexOf("/") + 1, uri.lastIndexOf("."));
				// name = uri.substring(uri.lastIndexOf("#") + 1);
			}
			Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
			URL fileURL = bundle.getEntry(uri);
			File file;
			try {
				file = new File(FileLocator.resolve(fileURL).toURI());
				local = file.exists();
			} catch (Exception e) {
				local = false;
				// e.printStackTrace();
			}
		}

		public ApplicationDomain(String uri, String resourcePath, String crudVerb, String securityScheme, String type) {
			this.uri = uri;
			if (!resourcePath.isEmpty()) {
				this.resourcePath = resourcePath;
			}
			if (!crudVerb.isEmpty()) {
				this.crudVerb = crudVerb;
			}
			if (!securityScheme.isEmpty()) {
				this.securityScheme = securityScheme;
			}
			if (uri.contains("script")) {
				name = uri.substring(uri.lastIndexOf("/") + 1, uri.lastIndexOf("."));
			} else if (uri.contains("localhost") || uri.contains("160.40.50.176") || type.equalsIgnoreCase("RESTful")
					|| (uri.lastIndexOf("/") + 1 < uri.lastIndexOf("."))) {
				name = "";
			} else {
				name = uri.substring(uri.lastIndexOf("/") + 1, uri.lastIndexOf("."));
				// name = uri.substring(uri.lastIndexOf("#") + 1);
			}
			Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
			URL fileURL = bundle.getEntry(uri);
			File file;
			try {
				file = new File(FileLocator.resolve(fileURL).toURI());
				local = file.exists();
			} catch (Exception e) {
				local = false;
				// e.printStackTrace();
			}
		}

		public String getCrudVerb() {
			return crudVerb;
		}

		public String getSecurityScheme() {
			return securityScheme;
		}

		public String getURI() {
			return uri;
		}

		public String getResourcePath() {
			return resourcePath;
		}

		public String getName() {
			return name;
		}

		/**
		 * <h1>isLocal</h1>
		 * 
		 * @return true if URI corresponds to a local file
		 */
		public boolean isLocal() {
			return local;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * <h1>OwlClass</h1> This class represents an OWL class, containing a
	 * multitude of operations.
	 */
	public static class OwlClass {
		private ComparableName name;
		private OwlClass superClass;
		private ArrayList<Operation> operations = new ArrayList<Operation>();

		/**
		 * <h1>OwlClass</h1> Constructor the OWL class. Operations are loaded
		 * normally, but the super class should be declared externally
		 * 
		 * @param name
		 *            : the name of the class
		 */
		OwlClass(String name) {
			this.name = new ComparableName(name);
			superClass = null;
		}

		public boolean isEmpty() {
			return operations.isEmpty();
		}

		/**
		 * <h1>getName</h1>
		 * 
		 * @return the class' name
		 */
		public ComparableName getName() {
			return name;
		}

		/**
		 * <h1>setSuper</h1> Sets this class' super class
		 * 
		 * @param superClass
		 */
		void setSuper(OwlClass superClass) {
			this.superClass = superClass;
		}

		/**
		 * <h1>getSuper</h1>
		 * 
		 * @return the class' superclass
		 */
		public OwlClass getSuper() {
			return superClass;
		}

		/**
		 * <h1>getOperations</h1>
		 * 
		 * @return an <code>ArrayList</code> with the class's operations
		 *         (editing that list will edit the class too)
		 */
		public ArrayList<Operation> getOperations() {
			return operations;
		}

		/**
		 * <h1>addOperation</h1> Adds an operation to the class.
		 * 
		 * @param op
		 *            : the operation to be added
		 */
		public void addOperation(Operation op) {
			operations.add(op);
		}

		/**
		 * <h1>toString</h1> Represents all class information in a multiline
		 * string.
		 */
		@Override
		public String toString() {
			String ret = name.toString();
			if (superClass != null)
				ret = superClass.getName() + " -> " + ret;
			for (Operation op : operations)
				ret += "\n+ " + op.toString();
			return ret;
		}
	}

	/**
	 * <h1>AccessInfo</h1> This class contains access information about a single
	 * service.
	 */
	public static class AccessInfo {
		private String license = "";
		private double pricePerUse = 0;// Math.random()*10;//in EUR

		public AccessInfo() {
		}

		public String getLicense() {
			return license;
		}

		public double getNextUsagePrice() {
			return pricePerUse;
		}

		@Override
		public String toString() {
			String ret = license;
			if (pricePerUse != 0)
				ret += " " + pricePerUse + " EUR/use";
			return ret;
		}
	}

	/**
	 * <h1>Operation</h1> This class represents an OWL operation, including its
	 * name, domain, all sorts of arguments and its real operations. Real
	 * operations have a redundant copy of the operation's arguments.
	 */
	public static class Operation {
		protected ComparableName name;
		protected ApplicationDomain domain = null;
		protected ArrayList<Argument> outputs = new ArrayList<Argument>();
		protected ArrayList<Argument> inputs = new ArrayList<Argument>();
		protected ArrayList<Argument> uriParameters = new ArrayList<Argument>();
		protected ArrayList<Argument> authenticationParameters = new ArrayList<Argument>();
		protected ArrayList<Operation> realOperations = new ArrayList<Operation>();
		protected ServiceAccessInfoForUsers accessInfo = new ServiceAccessInfoForUsers();
		protected OwlMetadata metadata = null;
		protected boolean isPrototype = false;
		protected String type = "";

		/**
		 * An empty constructor that may be used by derived classes.
		 */
		public Operation() {
		}

		/**
		 * Generates an operation from its Jena Individual (does NOT load real
		 * operations)
		 * 
		 * @param ind
		 * @param domainList
		 *            : a list of domains to search for the domain name
		 * @throws Exception
		 *             if a domain name does not exist in the domainList
		 */
		Operation(Individual ind) throws Exception {
			// load basic properties
			name = new ComparableName(ind.getPropertyValue(hasName).asLiteral().getString());
			
			// SOAP or RESTful
			if (ind.getPropertyValue(belongsToWSType) != null) {
				type = ind.getPropertyValue(belongsToWSType).asLiteral().getString();
			}
			String resourcePath = "";
			if (ind.getPropertyValue(hasResourcePath) != null) {
				resourcePath = ind.getPropertyValue(hasResourcePath).asLiteral().getString();
			}

			String crudVerb = "";
			if (ind.getPropertyValue(hasCRUDVerb) != null) {
				crudVerb = ind.getPropertyValue(hasCRUDVerb).asLiteral().getString();
			}

			String securityScheme = "";
			if (ind.getPropertyValue(hasSecurityScheme) != null) {
				securityScheme = ind.getPropertyValue(hasSecurityScheme).asLiteral().getString();
			}

			if (ind.getPropertyValue(belongsToURL) != null) {
				String domainName = ind.getPropertyValue(belongsToURL).asLiteral().getString();
				domain = domainList.get(domainName);
				if (!domainName.isEmpty() && domain == null) {
					// throw new
					// Exception("Domain "+domainName+" has not been declared");
					domainList.put(domainName,
							domain = new ApplicationDomain(domainName, resourcePath, crudVerb, securityScheme, type));
				}
			} else
				domain = null;

			metadata = new OwlMetadata(ind);

			if (ind.getPropertyValue(Importer.isPrototype) != null)
				this.isPrototype = !ind.getPropertyValue(Importer.isPrototype).asLiteral().getString().equals("false");
			// load inputs
			loadWSIO(ind, hasInput, inputs);
			// load outputs
			loadWSIO(ind, hasOutput, outputs);
			// load uri Parameters
			loadWSURI(ind, hasURIParameters, uriParameters);
			// load authentication Parameters
			loadWSAuth(ind, hasSecurityScheme, authenticationParameters);
			// load Cost, License Parameters
			loadIPR(ind, 1);
			// load implementations
		}

		private void loadIPR(Individual ind, int indicator) {

			// get Solution individual
			try {
				String NS = prefix;
				OntClass cl = ontologyModel.getOntClass(NS + "Solutions");
				ObjectProperty hasServiceAccessInfo = ontologyModel.getObjectProperty(NS + "hasServiceAccessInfo");
				DatatypeProperty hasDescription = ontologyModel.getDatatypeProperty(NS + "hasDescription");

				DatatypeProperty hasURLforAccess = ontologyModel.getDatatypeProperty(NS + "hasURLforAccess");
				ObjectProperty hasLicense = ontologyModel.getObjectProperty(NS + "hasLicense");
				DatatypeProperty hasLicenseDescription = ontologyModel
						.getDatatypeProperty(NS + "hasLicenseDescription");
				DatatypeProperty hasLicenseName = ontologyModel.getDatatypeProperty(NS + "hasLicenseName");
				DatatypeProperty isProprietary = ontologyModel.getDatatypeProperty(NS + "isProprietary");
				ObjectProperty hasCommercialCostSchema = ontologyModel
						.getObjectProperty(NS + "hasCommercialCostSchema");
				DatatypeProperty hasCommercialCost = ontologyModel.getDatatypeProperty(NS + "hasCommecrialCost");
				DatatypeProperty hasCommercialCostCurrency = ontologyModel
						.getDatatypeProperty(NS + "hasCommercialCostCurrency");
				DatatypeProperty hasCostPaymentChargeType = ontologyModel
						.getDatatypeProperty(NS + "hasCostPaymentChargeType");
				ObjectProperty hasTrialSchema = ontologyModel.getObjectProperty(NS + "hasTrialSchema");
				DatatypeProperty hasDurationInDays = ontologyModel.getDatatypeProperty(NS + "hasDurationInDays");
				DatatypeProperty hasLimitedFunctionalityDescription = ontologyModel
						.getDatatypeProperty(NS + "hasLimitedFunctionalityDescription");
				DatatypeProperty hasDurationInUsages = ontologyModel.getDatatypeProperty(NS + "hasDurationInUsages");
				DatatypeProperty offersFullFunctionalityDuringTrial = ontologyModel
						.getDatatypeProperty(NS + "offersFullFunctionalityDuringTrial");
				ObjectProperty hasDiscountIfUsedWithOtherSolution = ontologyModel
						.getObjectProperty(NS + "hasDiscountIfUsedWithOtherSolution");
				DatatypeProperty hasDiscount = ontologyModel.getDatatypeProperty(NS + "hasDiscount");
				DatatypeProperty hasDiscountReason = ontologyModel.getDatatypeProperty(NS + "hasDiscountReason");
				DatatypeProperty hasPairedService = ontologyModel.getDatatypeProperty(NS + "hasPairedService");
				DatatypeProperty isValidForCountries = ontologyModel.getDatatypeProperty(NS + "isValidForCountries");

				if (ind.getPropertyResourceValue(hasServiceAccessInfo) != null) {
					Resource r1 = ind.getPropertyResourceValue(hasServiceAccessInfo);

					Individual hasServiceAccessInfoInd = (Individual) ontologyModel.getIndividual(r1.getURI());

					if (hasServiceAccessInfoInd.getPropertyValue(isValidForCountries) != null) {

						NodeIterator it = hasServiceAccessInfoInd.listPropertyValues(isValidForCountries);

						List<Country> isValidForCountriesList = new ArrayList<Country>();

						while (it.hasNext()) {
							Literal l = it.next().asLiteral();
							String lang = l.getLanguage();
							String country = l.getValue().toString();
							Country isValidForCountriesSchema = new Country(country, lang);
							isValidForCountriesList.add(isValidForCountriesSchema);
						}

						accessInfo.setValidForCountries(isValidForCountriesList);
					}

					if (hasServiceAccessInfoInd.getPropertyValue(hasDescription) != null) {

						NodeIterator it = hasServiceAccessInfoInd.listPropertyValues(hasDescription);

						List<Description> isValidForCountriesList = new ArrayList<Description>();

						while (it.hasNext()) {
							Literal l = it.next().asLiteral();
							String lang = l.getLanguage();
							String description = l.getValue().toString();
							Description isValidForCountriesSchema = new Description(description, lang);
							isValidForCountriesList.add(isValidForCountriesSchema);
						}

						accessInfo.setDescription(isValidForCountriesList);
					}

					// load license

					if (hasServiceAccessInfoInd.getPropertyResourceValue(hasLicense) != null) {
						Resource r2 = hasServiceAccessInfoInd.getPropertyResourceValue(hasLicense);
						Individual licenseInd = (Individual) ontologyModel.getIndividual(r2.getURI());
						ServiceLicense license = new ServiceLicense();

						if (licenseInd.getPropertyValue(hasLicenseDescription) != null) {
							license.setLicenseDescription(
									licenseInd.getPropertyValue(hasLicenseDescription).asLiteral().getString());
						}

						if (licenseInd.getPropertyValue(hasLicenseName) != null) {
							license.setLicenseName(licenseInd.getPropertyValue(hasLicenseName).asLiteral().getString());
						}

						if (licenseInd.getPropertyValue(isProprietary) != null) {
							license.setProprietary(licenseInd.getPropertyValue(isProprietary).asLiteral().getBoolean());
						}
						accessInfo.setLicense(license);

					}

					// load commercial cost schema
					if (hasServiceAccessInfoInd.getPropertyResourceValue(hasCommercialCostSchema) != null) {
						Resource r2 = hasServiceAccessInfoInd.getPropertyResourceValue(hasCommercialCostSchema);
						Individual commercialCostSchemaInd = (Individual) ontologyModel.getIndividual(r2.getURI());
						CommercialCostSchema commercialCostSchema = new CommercialCostSchema();

						if (commercialCostSchemaInd.getPropertyValue(hasCommercialCost) != null) {
							commercialCostSchema.setCommercialCost(
									commercialCostSchemaInd.getPropertyValue(hasCommercialCost).asLiteral().getFloat());
						}

						if (commercialCostSchemaInd.getPropertyValue(hasCommercialCostCurrency) != null) {
							commercialCostSchema.setCommercialCostCurrency(commercialCostSchemaInd
									.getPropertyValue(hasCommercialCostCurrency).asLiteral().getString());
						}

						if (commercialCostSchemaInd.getPropertyValue(hasCostPaymentChargeType) != null) {
							commercialCostSchema.setCostPaymentChargeType(commercialCostSchemaInd
									.getPropertyValue(hasCostPaymentChargeType).asLiteral().getString());
						}
						// load trial schema

						if (commercialCostSchemaInd.getPropertyResourceValue(hasTrialSchema) != null) {
							Resource r3 = commercialCostSchemaInd.getPropertyResourceValue(hasTrialSchema);
							Individual trialSchemaInd = (Individual) ontologyModel.getIndividual(r3.getURI());
							ServiceTrialSchema trialSchema = new ServiceTrialSchema();

							if (trialSchemaInd.getPropertyValue(hasDurationInDays) != null) {
								trialSchema.setDurationInDays(
										trialSchemaInd.getPropertyValue(hasDurationInDays).asLiteral().getInt());
							}

							if (trialSchemaInd.getPropertyValue(hasLimitedFunctionalityDescription) != null) {
								trialSchema.setLimitedFunctionalityDescription(trialSchemaInd
										.getPropertyValue(hasLimitedFunctionalityDescription).asLiteral().getString());
							}

							if (trialSchemaInd.getPropertyValue(hasDurationInUsages) != null) {
								trialSchema.setDurationInUsages(
										trialSchemaInd.getPropertyValue(hasDurationInUsages).asLiteral().getInt());
							}

							if (trialSchemaInd.getPropertyValue(offersFullFunctionalityDuringTrial) != null) {
								try {
									// TODO does not return the right
									// property value
									trialSchema.setOffersFullFunctionalityDuringTrial(
											trialSchemaInd.getPropertyValue(offersFullFunctionalityDuringTrial)
													.asLiteral().getBoolean());
								} catch (Exception ex) {
									// ex.printStackTrace();
								}
							}
							commercialCostSchema.setTrialSchema(trialSchema);
						} else {
							// Set trial period unlimited
							ServiceTrialSchema trialSchema = new ServiceTrialSchema();
							trialSchema.setDurationInDays(Integer.MAX_VALUE);
							trialSchema.setDurationInUsages(Integer.MAX_VALUE);
							commercialCostSchema.setTrialSchema(trialSchema);
						}
						// load discount solutions

						if (commercialCostSchemaInd
								.getPropertyResourceValue(hasDiscountIfUsedWithOtherSolution) != null) {

							NodeIterator it2 = commercialCostSchemaInd
									.listPropertyValues(hasDiscountIfUsedWithOtherSolution);

							List<DiscountSchema> discountSchemaList = new ArrayList<DiscountSchema>();

							while (it2.hasNext()) {
								Resource r = it2.next().asResource();
								// System.out.println(r.getURI());
								Individual discountSchemasInd = (Individual) ontologyModel.getIndividual(r.getURI());
								DiscountSchema discountSchema = new DiscountSchema();

								if (discountSchemasInd.getPropertyValue(hasDiscount) != null) {
									discountSchema.setDiscount(
											discountSchemasInd.getPropertyValue(hasDiscount).asLiteral().getInt());
								}

								if (discountSchemasInd.getPropertyValue(hasDiscountReason) != null) {
									discountSchema.setDiscountReason(discountSchemasInd
											.getPropertyValue(hasDiscountReason).asLiteral().getString());
								}

								if (discountSchemasInd.getPropertyResourceValue(hasPairedService) != null) {
									NodeIterator it3 = discountSchemasInd.listPropertyValues(hasPairedService);

									Resource r3 = it3.next().asResource();

									Individual PairedServiceSchemasInd = (Individual) ontologyModel
											.getIndividual(r3.getURI());
									Operation pairedServiceSchema = new Operation();
									WSOperation test = new WSOperation();

									if (PairedServiceSchemasInd.getPropertyValue(hasName) != null) {
										pairedServiceSchema.name = new ComparableName(PairedServiceSchemasInd
												.getPropertyValue(hasName).asLiteral().getString());

									}
									if (PairedServiceSchemasInd.getPropertyValue(hasInput) != null) {
										pairedServiceSchema.loadWSIO(PairedServiceSchemasInd, hasInput,
												pairedServiceSchema.inputs);
									}
									if (PairedServiceSchemasInd.getPropertyValue(hasURIParameters) != null) {
										pairedServiceSchema.loadWSURI(PairedServiceSchemasInd, hasURIParameters,
												pairedServiceSchema.uriParameters);
									}
									if (PairedServiceSchemasInd.getPropertyValue(hasSecurityScheme) != null) {
										pairedServiceSchema.loadWSAuth(PairedServiceSchemasInd, hasSecurityScheme,
												pairedServiceSchema.authenticationParameters);
									}
									if (PairedServiceSchemasInd.getPropertyValue(hasOutput) != null) {
										pairedServiceSchema.loadWSIO(PairedServiceSchemasInd, hasOutput,
												pairedServiceSchema.outputs);
									}
									if (PairedServiceSchemasInd.getPropertyValue(hasServiceDomain) != null) {
										Resource r4 = PairedServiceSchemasInd
												.getPropertyResourceValue(hasServiceDomain);

										Individual hasServiceDomainInd = (Individual) ontologyModel
												.getIndividual(r4.getURI());
										pairedServiceSchema.domain = new ApplicationDomain(hasServiceDomainInd);
									} else {
										pairedServiceSchema.domain = null;
									}

									pairedServiceSchema.metadata = new OwlMetadata(PairedServiceSchemasInd);

									if (indicator < 2) {
										pairedServiceSchema.loadIPR(PairedServiceSchemasInd, indicator + 1);
									}

									if (PairedServiceSchemasInd.getPropertyValue(Importer.isPrototype) != null)
										pairedServiceSchema.isPrototype = !PairedServiceSchemasInd
												.getPropertyValue(Importer.isPrototype).asLiteral().getString()
												.equals("false");

									discountSchema.setPairedService(pairedServiceSchema);

								}
								discountSchemaList.add(discountSchema);

							}
							commercialCostSchema.setDiscountIfUsedWithOtherService(discountSchemaList);
						}

						accessInfo.setCommercialCostSchema(commercialCostSchema);
					} else {
						// Set trial period unlimited
						ServiceTrialSchema trialSchema = new ServiceTrialSchema();
						trialSchema.setDurationInDays(Integer.MAX_VALUE);
						trialSchema.setDurationInUsages(Integer.MAX_VALUE);
						CommercialCostSchema commercialCostSchema = new CommercialCostSchema();
						commercialCostSchema.setTrialSchema(trialSchema);
						accessInfo.setCommercialCostSchema(commercialCostSchema);
					}

				}

			} catch (Exception ex) {

			}
		}

		/**
		 * Generates an empty operation from its name and domain.
		 * 
		 * @param name
		 *            : the operation's name
		 * @param domain
		 *            : the operation's domain
		 */
		public Operation(String name, ApplicationDomain domain) {
			this.name = new ComparableName(name);
			this.domain = domain;
		}

		public Operation(Operation op) {
			this.accessInfo = op.getAccessInfo();
			this.domain = op.getDomain();
			// this.inputs=op.getInputs();
			this.isPrototype = op.isPrototype();
			this.metadata = op.getMetadata();
			this.name = op.getName();
			// this.outputs=op.getOutputs();
			this.realOperations = op.getRealOperations();
			this.type = op.getType();
		}

		/**
		 * <h1>clear</h1> Clears domain and all inputs, outputs and real
		 * operations.
		 */
		public void clear() {
			domain = null;
			outputs.clear();
			inputs.clear();
			uriParameters.clear();
			authenticationParameters.clear();
			realOperations.clear();
		}

		/**
		 * <h1>assign</h1> Becomes a clone of another Operation.
		 * <emph>ALWAYS</emph> call <code>Operation.clear()</code> first.
		 * 
		 * @param prototype
		 * @throws Exception
		 *             if the operation has outputs, inputs, real operations or
		 *             a domain
		 */
		public void assign(Operation prototype) throws Exception {
			if (!outputs.isEmpty() || !inputs.isEmpty() || !realOperations.isEmpty() || domain != null)
				throw new Exception("Operation.assign(...) may cause loss of information in non-empty operation: "
						+ name + "\n" + "ALWAYS call Operation.clear() first to ensure correct logic");
			name = prototype.name;
			domain = prototype.domain;
			isPrototype = prototype.isPrototype;
			outputs.addAll(prototype.outputs);
			inputs.addAll(prototype.inputs);
			uriParameters.addAll(prototype.uriParameters);
			authenticationParameters.addAll(prototype.authenticationParameters);
			realOperations.addAll(prototype.realOperations);
		}

		/**
		 * <h1>loadWSIO</h1> Loads all appropriate arguments to the appropriate
		 * list.
		 * 
		 * @param operInd
		 * @param prop
		 *            : the arguments to find as a Jena ObjectProperty
		 * @param vec
		 *            : the ArrayList to add the loaded arguments
		 */
		private void loadWSIO(Individual operInd, ObjectProperty prop, ArrayList<Argument> vec) {
			// List<String> primitiveDataTypes=getAllPrimitiveDataTypes();
			if (operInd.getPropertyResourceValue(prop) != null) {
				NodeIterator it = operInd.listPropertyValues(prop);
				while (it.hasNext()) {
					Resource r = it.next().asResource();
					Individual ioInd = (Individual) ontologyModel.getIndividual(r.getURI());
					// check if ioInd is primitive
					if (ioInd.getPropertyResourceValue(hasType) != null) {
						Individual typeInd = (Individual) ontologyModel
								.getIndividual(ioInd.getPropertyResourceValue(hasType).getURI());
						vec.add(new Argument(this, typeInd, ioInd));
					}
				}
			}
		}

		private void loadWSURI(Individual operInd, DatatypeProperty prop, ArrayList<Argument> vec) {
			// List<String> primitiveDataTypes=getAllPrimitiveDataTypes();
			if (operInd.getPropertyValue(prop) != null) {
				NodeIterator it = operInd.listPropertyValues(prop);
				while (it.hasNext()) {
					Literal l = it.next().asLiteral();
					String name = l.getValue().toString();
					String type = "String";
					Argument arg = new Argument(name, type, false, false, null);
					arg.setBelongsToOperation(this);
					vec.add(arg);
				}
			}
		}

		private void loadWSAuth(Individual operInd, DatatypeProperty prop, ArrayList<Argument> vec) {
			String securityScheme = "";
			if (operInd.getPropertyValue(prop) != null) {
				securityScheme = operInd.getPropertyValue(prop).asLiteral().getString();
				getDomain().securityScheme=securityScheme;
			}
			if (securityScheme.equalsIgnoreCase("Basic Authentication")) {
				Argument username = new Argument("Username", "String", false, false, null);
				username.setBelongsToOperation(this);
				vec.add(username);
				Argument password = new Argument("Password", "String", false, false, null);
				password.setBelongsToOperation(this);
				vec.add(password);
			}
		}

		/**
		 * <h1>getName</h1>
		 * 
		 * @return the operation's name
		 */
		public ComparableName getName() {
			return name;
		}

		/**
		 * <h1>getType()</h1>
		 * 
		 * @return the operation's type (SOAP or RESTful)
		 */
		public String getType() {
			return type;
		}

		/**
		 * <h1>getDomain</h1>
		 * 
		 * @return the operation's domain
		 */
		public ApplicationDomain getDomain() {
			return domain;
		}

		/**
		 * <h1>getInputs</h1>
		 * 
		 * @return the operation's inputs
		 */
		public ArrayList<Argument> getInputs() {
			return inputs;
		}

		/**
		 * <h1>getOutputs</h1>
		 * 
		 * @return the operation's outputs
		 */
		public ArrayList<Argument> getOutputs() {
			return outputs;
		}

		public ArrayList<Argument> getUriParameters() {
			return uriParameters;
		}

		public ArrayList<Argument> getAuthenticationParameters() {
			return authenticationParameters;
		}

		/**
		 * <h1>isPrototype</h1>
		 * 
		 * @return true if the operation is a prototype
		 */
		public boolean isPrototype() {
			return isPrototype;
		}

		/**
		 * <h1>getMetadata</h1>
		 * 
		 * @return the OwlMetadata of the operation
		 */
		public OwlMetadata getMetadata() {
			return metadata;
		}

		/**
		 * <h1>addRealOperation</h1> Adds an implementation of this operation.
		 * 
		 * @param op
		 *            : the implementation to be added
		 */
		public void addRealOperation(Operation op) {
			realOperations.add(op);
			if (op.domain == null)
				op.domain = domain;
			if (op.inputs.isEmpty())
				op.inputs.addAll(inputs);
			if (op.uriParameters.isEmpty())
				op.uriParameters.addAll(uriParameters);
			if (op.authenticationParameters.isEmpty())
				op.authenticationParameters.addAll(authenticationParameters);
			if (op.outputs.isEmpty())
				op.outputs.addAll(outputs);
		}

		/**
		 * <h1>getRealOperations</h1>
		 * 
		 * @return a list of all real operations that implement this operation
		 */
		public ArrayList<Operation> getRealOperations() {
			return realOperations;
		}

		/**
		 * <h1>getAccessInfo()</h1>
		 * 
		 * @return accessInfo : all the access information of the service (cost,
		 *         license etc.)
		 */
		public ServiceAccessInfoForUsers getAccessInfo() {
			return accessInfo;
		}

		/**
		 * <h1>toString</h1> Converts the operation and its real operations to a
		 * multiline string.
		 */
		@Override
		public String toString() {
			String ins = "";
			for (Argument arg : inputs) {
				if (!ins.isEmpty())
					ins += ", ";
				ins += arg.toString();
			}
			String uris = "";
			for (Argument arg : uriParameters) {
				if (!uris.isEmpty())
					uris += ", ";
				uris += arg.toString();
			}
			String outs = "";
			for (Argument arg : outputs) {
				if (!outs.isEmpty())
					outs += ", ";
				outs += arg.toString();
			}
			if (outs.isEmpty())
				outs = "void";
			if (outputs.size() > 1)
				outs = "(" + outs + ")";
			String ret = "";
			if (domain != null)
				ret += domain + ": ";
			if (isPrototype)
				ret += "prototype: ";
			ret += outs + " " + name + "(" + ins + ")" + "'";
			if (!accessInfo.getDescription().isEmpty())
				ret += " description: " + accessInfo.getDescription().get(0).getDescription();
			// if (getMetadata() != null && !getMetadata().toString().isEmpty())
			// ret += "\n" + getMetadata().toString();
			for (Operation op : realOperations)
				ret += "\n   - " + op.toString();
			return ret;
		}

		/**
		 * <h1>replaceArgument</h1> Replaces a single input or output argument
		 * (without disrupting the order of the arguments).
		 * 
		 * @param previous
		 * @param next
		 */
		public boolean replaceArgument(Argument previous, Argument next) {
			int i = inputs.indexOf(previous);
			if (i != -1) {
				inputs.remove(previous);
				inputs.add(i, next);
				return true;
			}
			i = outputs.indexOf(previous);
			if (i != -1) {
				outputs.remove(previous);
				outputs.add(i, next);
				return true;
			}
			return false;
		}
	}

	/**
	 * <h1>Argument</h1> This class represents arguments for OWL operations.
	 * Arguments can also be used as inputs or outputs.
	 */
	public static class Argument {
		private ComparableName name = new ComparableName("");
		private String type = "";
		private boolean isArray = false;
		private boolean isNative = true;
		private ArrayList<Argument> subtypes = new ArrayList<Argument>();
		// if Argument isArray it has elements which are filled when the
		// operation is called
		private ArrayList<Value> elements = new ArrayList<Value>();
		private ArrayList<Object> parent = new ArrayList<Object>();
		private OwlService belongsToOwlService = null;
		private Operation belongsToOperation;
		private boolean isRequired = true;

		/**
		 * Generates an argument by its attribute.
		 * 
		 * @param subtypes
		 *            can be null to not declare any subtypes without later
		 *            causing exceptions (not direct assignment)
		 */
		public Argument(String name, String type, boolean isArray, boolean isNative, ArrayList<Argument> subtypes) {
			this.name = new ComparableName(name);
			this.type = type;
			if (this.type.toLowerCase().equals(stringType.toLowerCase()))
				this.type = stringType;
			this.isArray = isArray;
			this.isNative = isNative;
			if (subtypes != null)
				this.subtypes.addAll(subtypes);
			for (Argument sub : this.subtypes)
				sub.parent.add(this);
		}

		/**
		 * Generates an argument for an operation.
		 * 
		 * @param operation
		 *            : the argument's parent operation
		 * @param typeInd
		 * @param ioInd
		 */
		Argument(Operation operation, Individual typeInd, Individual ioInd) {
			if (typeInd.getPropertyResourceValue(hasType) == null) {
				// Native Object
				if (ioInd.getPropertyValue(hasName) != null)
					name = new ComparableName(ioInd.getPropertyValue(hasName).asLiteral().getString());
				type = typeInd.getLocalName();
				if (this.type.toLowerCase().equals(stringType.toLowerCase()))
					this.type = stringType;
				isNative = true;
			} else {
				// Complex Object
				if (ioInd.getPropertyValue(hasName) != null) {
					String foundName = ioInd.getPropertyValue(hasName).asLiteral().getString().trim();
					foundName = foundName.substring(0, 1).toLowerCase() + foundName.substring(1);// convert
																									// first
																									// letter
																									// to
																									// lowercase
					name = new ComparableName(foundName);
					type = ioInd.getPropertyValue(hasName).asLiteral().getString().trim();
				}
				isNative = false;
				
				operation.loadWSIO(ioInd, hasType, subtypes);
			}
			
			belongsToOperation = operation;
			
			if (ioInd.getPropertyValue(Importer.isArray) != null)
				this.isArray = !ioInd.getPropertyValue(Importer.isArray).asLiteral().getString().equals("false");
			if (ioInd.getPropertyValue(Importer.isRequired) != null)
				this.isRequired = ioInd.getPropertyValue(Importer.isRequired).asLiteral().getBoolean();
			for (Argument sub : this.subtypes)
				sub.parent.add(this);
		}

		/**
		 * Generate an argument that is a copy of another argument (another
		 * instance of the sub-type list is created but it refers to the same
		 * sub-type instances).
		 * 
		 * @param prototype
		 */
		public Argument(Argument prototype) {
			name = prototype.name;
			type = prototype.type;
			isArray = prototype.isArray;
			isNative = prototype.isNative;
			isArray = prototype.isArray;
			isRequired = prototype.isRequired;
			for (Argument sub : prototype.getSubtypes()) {
				Argument arg = new Argument(sub);
				subtypes.add(arg);
			}
			// subtypes = prototype.subtypes;
			parent.addAll(prototype.parent);
			belongsToOwlService = prototype.belongsToOwlService;
			belongsToOperation = prototype.belongsToOperation;
		}

		/**
		 * Generates an empty argument (isNative=true, isArray=false, name="",
		 * type="", no sub-types, parent=null)
		 */
		public Argument() {
		}

		/**
		 * <h1>getName</h1>
		 * 
		 * @return the name of the argument (i.e. the variable name)
		 */
		public ComparableName getName() {
			return name;
		}

		/**
		 * <h1>getType</h1>
		 * 
		 * @return the type of the argument (e.g. double)
		 */
		public String getType() {
			return type;
		}

		/**
		 * <h1>isArray</h1>
		 * 
		 * @return true if the argument is declared to be an array
		 */
		public boolean isArray() {
			return isArray;
		}

		/**
		 * <h1>isRequired</h1>
		 * 
		 * @return true if the argument is required for the call
		 */
		public boolean isRequired() {
			return isRequired;
		}
		
		public void setIsRequired(boolean isRequired){
			this.isRequired = isRequired;
		}

		/**
		 * <h1>isNative</h1>
		 * 
		 * @return true if the argument is a native data type
		 */
		public boolean isNative() {
			return isNative;
		}

		/**
		 * <h1>getSubtypes</h1>
		 * 
		 * @return a list of all declared subtypes
		 */
		public ArrayList<Argument> getSubtypes() {
			return subtypes;
		}

		public ArrayList<Value> getElements() {
			return elements;
		}

		public OwlService getOwlService() {
			return belongsToOwlService;
		}
		
		public Operation getBelongsToOperation(){
			return belongsToOperation;
		}
		
		public void setBelongsToOperation(Operation operation){
			this.belongsToOperation= operation;
		}

		public void setOwlService(OwlService service) {
			this.belongsToOwlService = service;
		}

		/**
		 * <h1>getParent</h1>
		 * 
		 * @return returns a list of all parent Arguments and Operations
		 */
		public ArrayList<Object> getParent() {
			return parent;
		}

		public void addParent(Object parent) {
			this.parent.add(parent);
		}

		/**
		 * <h1>toString</h1> Converts the argument to a string in a way a java
		 * variable would be declared.
		 */
		@Override
		public String toString() {
			String ret = type;
			if (isArray)
				ret += " []";
			if (name != null)
				ret += " " + name;
			return ret;
		}

		/**
		 * <h1>replaceSubtype</h1> Replaces a single sub-type (without
		 * disrupting the order of sub-types). If the next sub-type already is a
		 * member, nothing happens (this ensures class integrity).
		 * 
		 * @param previous
		 * @param next
		 * @return true if the replacement took place
		 */
		public boolean replaceSubtype(Argument previous, Argument next) {
			if (subtypes.contains(next))
				return false;
			int i = subtypes.indexOf(previous);
			if (i != -1) {
				subtypes.remove(previous);
				subtypes.add(i, next);
				next.parent.add(this);
				return true;
			}
			return false;
		}

		/**
		 * <h1>assertCorrectSubtypes</h1> This function asserts that duplicate
		 * sub-types don't exist
		 * 
		 * @throws Exception
		 *             if duplicate sub-type found or sub-type has not this as
		 *             parent
		 */
		public void assertCorrectSubtypes() throws Exception {
			/*
			 * for(Argument sub : getSubtypes()){ if(sub.getParent()!=this)
			 * throw new Exception("Subtype "+getName()+"."+sub.getName()+
			 * " has parent " +sub.getParent()); }
			 */
			for (int i = 0; i < subtypes.size(); i++) {
				for (int j = 0; j < subtypes.size(); j++)
					if (i != j && subtypes.get(i) == subtypes.get(j))
						throw new Exception("Dublicate subtype " + getName() + "." + subtypes.get(i));
			}
		}

		/**
		 * <h1>setName</h1>
		 * 
		 * @param name
		 */
		public void setName(String name) {
			this.name = new ComparableName(name);
		}

		/**
		 * <h1>setType</h1>
		 * 
		 * @param type
		 * @param isArray
		 * @param isNative
		 */
		public void setType(String type, boolean isArray, boolean isNative) {
			this.type = type;
			this.isArray = isArray;
			this.isNative = isNative;
		}

		/**
		 * <h1>asList</h1>
		 * 
		 * @return an array list containing the variable and all its sub-types
		 *         recursively
		 */
		public ArrayList<Argument> asList() {
			ArrayList<Argument> list = new ArrayList<Argument>();
			list.add(this);
			for (Argument sub : subtypes)
				list.addAll(sub.asList());
			return list;
		}
	}
}
