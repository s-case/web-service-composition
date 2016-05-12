package eu.scasefp7.eclipse.servicecomposition.tester;

import eu.scasefp7.eclipse.servicecomposition.codeGenerator.FunctionCodeNode;
import eu.scasefp7.eclipse.servicecomposition.codeGenerator.NonLinearCodeGenerator;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter;
import eu.scasefp7.eclipse.servicecomposition.importer.OwlImporter;
import eu.scasefp7.eclipse.servicecomposition.importer.PythonImporter;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Service;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform;
import eu.scasefp7.eclipse.servicecomposition.transformer.Matcher;
import eu.scasefp7.eclipse.servicecomposition.transformer.PathFinding;
import eu.scasefp7.eclipse.servicecomposition.transformer.Similarity;
import eu.scasefp7.eclipse.servicecomposition.transformer.Similarity.ComparableName;
import eu.scasefp7.eclipse.servicecomposition.transformer.Transformer;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.transformer.Transformer.ReplaceInformation;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import edu.uci.ics.jung.graph.Graph;

/**
 * <h1>Algorithm</h1> This class combines all necessary functions needed for
 * service composition. Its functions combine all components developed for this
 * project into a coherent flow.
 * 
 * @author Manios Krasanakis
 */
public class Algorithm {

	/**
	 * store replacement strategy evaluation (obtained with getter)
	 */
	private static ArrayList<WeightReport> correctStepProbability = new ArrayList<WeightReport>();

	// private static boolean expandOperationStrategy = false;

	public static class WeightReport {
		/**
		 * total weight of the service
		 */
		private double weight;
		/**
		 * worst probability depending on the maximum number of words of either
		 * the action or the service name
		 */
		private double worstCaseProbability;
		/**
		 * probability of successful replacement
		 */
		private double effectiveProbability;
		/**
		 * description of the service
		 */
		private String description;
		/**
		 * similarity between action and service names
		 */
		private double nameSimilarity;
		/**
		 * 
		 */
		private double numberOfWords;
		/**
		 * the replacement selection
		 */
		private ReplaceInformation selection = null;
		/**
		 * alternative operations for replacement
		 */
		private ArrayList<ReplaceInformation> alternativeOperations = new ArrayList<ReplaceInformation>();

		public WeightReport(ReplaceInformation selection, ArrayList<ReplaceInformation> replaceInformations) {
			this.selection = selection;
			description = selection.toString();
			double maxWeight = selection.getWeight();
			//set alternative operations
			for (Transformer.ReplaceInformation replace : replaceInformations)
				if (replace.getWeight() <= maxWeight) {
					if (replace.getOriginalServiceOperation().equals(selection.getOriginalServiceOperation()))
						alternativeOperations.add(replace);
				}
			int alternativeOperationsMaxSize = 3;
			int alternativeOperationsSize = alternativeOperations.size();
			if (alternativeOperationsSize > alternativeOperationsMaxSize) {
				alternativeOperationsSize = alternativeOperationsMaxSize;
			}
			for (int i=0;i<alternativeOperationsSize;i++) {
				selection.setAlternativeOperations(alternativeOperations.get(i));
			}
			updateWeight();
		}

		public ReplaceInformation getReplaceInformation() {
			return selection;
		}

		public void updateWeight() {
			if (selection == null)
				return;
			weight = selection.getWeight();
			// double nameSimilarity =
			// selection.getOperationToReplace().getName().getComparableForm().split("\\s").length;
			double descriptionSimilarity = 0;
			boolean isRestfulWithDescription = false;
			this.numberOfWords = selection.getOriginalServiceOperation().getName().getComparableForm()
					.split("\\s").length;
			this.nameSimilarity = Similarity.similarity(selection.getOperationToReplace().getName(),
					selection.getOriginalServiceOperation().getName());
			if (!selection.getOperationToReplace().getDescription().isEmpty()) {
				// descriptionSimilarity =
				// selection.getOperationToReplace().getAccessInfo().getDescription().get(0)
				// .getDescription().split("\\s").length;
				if (selection.getOperationToReplace().getType().equalsIgnoreCase("Restful")) {
					isRestfulWithDescription = true;
					// this.numberOfWords=selection.getOperationToReplace().getAccessInfo().getDescription().get(0)
					// .getDescription().split("\\s").length;
					this.nameSimilarity = Similarity
							.similarity(
									new ComparableName(selection.getOperationToReplace()
											.getDescription()),
							selection.getOriginalServiceOperation().getName());
				}

			}

			effectiveProbability = selection.getWeight();
			// nameSimilarity = Math.max(nameSimilarity,
			// selection.getTargetService().getOperation().getName().getComparableForm().split("\\s").length);
			// worstCaseProbability = selection.getWeight() /
			// Matcher.getMaxWeight(isRestfulWithDescription);

		}

		public WeightReport(ArrayList<WeightReport> correctStepProbability) {
			description = "QoS";
			weight = 0;
			double effectiveMaxWeight = 0;
			double worstCaseMaxWeight = 0;
			for (WeightReport report : correctStepProbability) {
				weight += report.weight;
				nameSimilarity += report.nameSimilarity;
				effectiveMaxWeight += report.weight / report.effectiveProbability;
				worstCaseMaxWeight += report.weight / report.worstCaseProbability;
			}
			effectiveProbability = effectiveMaxWeight == 0 ? 0 : weight / effectiveMaxWeight;
			worstCaseProbability = worstCaseMaxWeight == 0 ? 0 : weight / worstCaseMaxWeight;

		}

		public WeightReport(ArrayList<WeightReport> correctStepProbability, double minimumTrust) {
			description = "QoS - probabilistic estimation";
			weight = 0;
			effectiveProbability = 1;
			worstCaseProbability = 1;
			for (WeightReport report : correctStepProbability) {
				weight += report.weight;
				nameSimilarity += report.nameSimilarity;
				effectiveProbability *= report.effectiveProbability > minimumTrust ? 1 : report.effectiveProbability;
				worstCaseProbability *= report.worstCaseProbability;
			}
			effectiveProbability = Math.pow(effectiveProbability, 1.0 / correctStepProbability.size());
			worstCaseProbability = Math.pow(worstCaseProbability, 1.0 / correctStepProbability.size());
		}

		@Override
		public String toString() {
			String ret = "REPLACEMENT REPORT\n";
			ret += "\tAction     : " + description + "\n";
			ret += "\tBase URI	: " + this.selection.getOperationToReplace().getDomain().getURI() + "\n";
			ret += "\tName Similarity     : " + this.nameSimilarity + "/" + this.numberOfWords + "\n";
			ret += "\tProbability: " + Math.round(effectiveProbability * 1000) / 10.0 + "%\n";
			// ret += "\tWorst Probability: " + Math.round(worstCaseProbability
			// * 1000) / 10.0 + "%\n";
			return ret;
		}
	}

	/**
	 * <h1>costReport</h1>Class for the calculation of the total cost of the
	 * workflow.
	 * 
	 * @author mkoutli
	 *
	 */
	public static class costReport {
		/**
		 * total cost in euro
		 */
		protected float totalCostEUR = 0;
		/**
		 * total cost in usd
		 */
		protected float totalCostUSD = 0;
		/**
		 * total cost in gbp
		 */
		protected float totalCostGBP = 0;
		/**
		 * total cost
		 */
		protected double totalCost = 0;
		/**
		 * all currencies in the workflow
		 */
		protected String totalCurrency = "";

		public costReport() {
			this.totalCostEUR = 0;
			this.totalCostUSD = 0;
			this.totalCostGBP = 0;
			this.totalCost = 0;
			this.totalCurrency = "";
		}

		/**
		 * <h1>calculateWorkflowCost</h1> Calculate the total cost of the
		 * workflow by adding the per usage/month/year/unlimited costs
		 * calculated by the <code>calculateCost</code> and save it in a string
		 * to print it.
		 * 
		 * @param ret
		 * @return the string for print
		 */
		public String calculateWorkflowCost(String ret) {

			if (this.totalCostEUR != 0 && this.totalCostUSD == 0 && this.totalCostGBP == 0) {
				this.totalCost = this.totalCostEUR;
				this.totalCurrency = "EUR";
			} else if (this.totalCostEUR == 0 && this.totalCostUSD != 0 && this.totalCostGBP == 0) {
				this.totalCost = totalCostUSD;
				this.totalCurrency = "USD";
			} else if (this.totalCostEUR == 0 && this.totalCostUSD == 0 && this.totalCostGBP != 0) {
				this.totalCost = totalCostGBP;
				this.totalCurrency = "GBP";
			} else {
				ret = " (" + this.totalCostEUR + " " + "EUR" + ", " + this.totalCostUSD + " " + "USD" + ", "
						+ this.totalCostGBP + " " + "GBP)" + ret;
				double rateUSD = 0.89;
				double rateGBP = 1.41;
				this.totalCost = this.totalCostEUR + this.totalCostUSD * rateUSD + this.totalCostGBP * rateGBP;
				this.totalCost = round(this.totalCost, 2);
				this.totalCurrency = "EUR";
			}
			if (this.totalCost != 0) {
				ret = this.totalCost + " " + this.totalCurrency + ret;
				return ret;
				// System.out.println(this.totalCost + " " +
				// this.totalCurrency+ret);
			}
			return "";
		}

		public static double round(double value, int places) {
			if (places < 0)
				throw new IllegalArgumentException();

			BigDecimal bd = new BigDecimal(value);
			bd = bd.setScale(places, RoundingMode.HALF_UP);
			return bd.doubleValue();
		}

		/**
		 * <h1>calculateCost</h1>Calculate the total cost of a charge type(e.g.
		 * per usage) by adding the costs of the services in EUR, USD, GBP.
		 * 
		 * @param currency
		 * @param service
		 */
		public void calculateCost(String currency, OwlService service) {
			switch (currency) {
			case "EUR":
				totalCostEUR = totalCostEUR + ((Operation) service.getContent()).getAccessInfo()
						.getCommercialCostSchema().getCommercialCost();
				break;
			case "USD":
				totalCostUSD = totalCostUSD + ((Operation) service.getContent()).getAccessInfo()
						.getCommercialCostSchema().getCommercialCost();
				break;
			case "GBP":
				totalCostGBP = totalCostGBP + ((Operation) service.getContent()).getAccessInfo()
						.getCommercialCostSchema().getCommercialCost();
				break;
			default:
				break;
			}
		}

	}

	/**
	 * <h1>trialReport</h1>Class for calculating the total(minimum) trial period
	 * of the workflow.
	 * 
	 * @author mkoutli
	 *
	 */

	public static class trialReport {

		/**
		 * trial duration in days
		 */
		private List<Integer> durationInDays = new ArrayList<Integer>();
		/**
		 * trial duration in usages
		 */
		private List<Integer> durationInUsages = new ArrayList<Integer>();

		public trialReport() {
			this.durationInDays.add(Integer.MAX_VALUE);
			this.durationInUsages.add(Integer.MAX_VALUE);
		}

		public void setTrialReport(int durationInDays, int durationInUsages) {
			this.durationInDays.add(durationInDays);
			this.durationInUsages.add(durationInUsages);
		}

		public int findDurationInDays() {
			return Collections.min(this.durationInDays);
		}

		public int findDurationInUsages() {
			return Collections.min(this.durationInUsages);
		}
	}

	/**
	 * <h1>licenseReport</h1>Class for gathering the total licenses of the
	 * workflow.
	 * 
	 * @author mkoutli
	 *
	 */

	public static class licenseReport {
		/**
		 * the licenses of the service
		 */
		private List<String> licenseNames = new ArrayList<String>();

		public licenseReport() {
			this.licenseNames.add("");
		}

		public void setLicenseReport(String licenseName) {
			this.licenseNames.add(licenseName);
		}

		public List<String> getLicenseReport() {
			return licenseNames;
		}
	}

	/**
	 * <h1>init</h1> Initializes all necessary properties.
	 */
	public static void init() {
		Matcher.loadProperties();
		Transformer.loadProperties();
	}

	/**
	 * <h1>importServices</h1> Imports both OWL and Python services.
	 * 
	 * @param owlPath
	 *            : path to the OWL file
	 * @param pythonPath
	 *            : path to a folder which contains all Python scripts to detect
	 *            functions as services from
	 * @return a list of all detected services as Operations
	 * @throws Exception
	 */
	public static ArrayList<Importer.Operation> importServices(final String owlPath, final String pythonPath)
			throws Exception {

		System.out.print("Importing operations... ");
		ArrayList<Importer.Operation> operations = new ArrayList<Importer.Operation>();
		if (!owlPath.isEmpty())
			operations.addAll(OwlImporter.importOwl(owlPath, true, false));
		System.out.print("Done (" + operations.size() + " operations)\nImporting python operations... ");
		ArrayList<Importer.Operation> pythonOperations = PythonImporter.importPath(pythonPath);
		System.out.println("Done (" + pythonOperations.size() + " operations)");
		operations.addAll(pythonOperations);
		// for(Object op : operations)
		// System.out.println(op.toString());
		return operations;

	}

	/**
	 * <h1>importServices</h1> Imports both OWL and Python services.
	 * 
	 * @param owlPath
	 *            : path to the OWL file
	 * @param pythonPath
	 *            : path to a folder which contains all Python scripts to detect
	 *            functions as services from
	 * @return a list of all detected services as Operations
	 * @throws Exception
	 */
	public static ArrayList<Importer.Operation> importServices(final String owlPath) throws Exception {

		System.out.print("Importing operations... ");
		ArrayList<Importer.Operation> operations = new ArrayList<Importer.Operation>();
		if (!owlPath.isEmpty())
			operations.addAll(OwlImporter.importOwl(owlPath, true, false));
		System.out.print("Done (" + operations.size() + " operations)\nImporting python operations... ");

		return operations;

	}

	/**
	 * <h1>transformationAlgorithm</h1> Implements a transformation algorithm on
	 * the workflow that resides in an XMI file.
	 * 
	 * @param path
	 *            : the path of the XMI file to transform
	 * @param operations
	 *            : the list of operations loaded with
	 *            <code>importServices</code>
	 * @return an OWL graph that contains the composition of services
	 * @throws Exception
	 */
	public static Graph<OwlService, Connector> transformationAlgorithm(String path,
			ArrayList<Importer.Operation> operations, Display disp) throws Exception {

		costReport costPerUsage = new costReport();
		costReport costPerMonth = new costReport();
		costReport costPerYear = new costReport();
		costReport costUnlimited = new costReport();
		trialReport workflowTrialPeriod = new trialReport();
		licenseReport workflowLicense = new licenseReport();
		ArrayList<Importer.Operation> candidateOperations = new ArrayList<Importer.Operation>(operations);
		// load the XMI graph
		Graph<Service, Connector> xmiGraph = JungXMIImporter.createGraph(path, false);

		// check graph
		IStatus status = checkGraph(JungXMItoOwlTransform.createOwlGraph(xmiGraph, true), disp);
		if (status.getMessage().equalsIgnoreCase("OK")) {
			// transform XMI to OWL graph and generate a transformer to
			// manipulate
			// the graph
			Transformer transformer = new Transformer(JungXMItoOwlTransform.createOwlGraph(xmiGraph, true));
			correctStepProbability.clear();
			int numOfActions = 0;
			for (OwlService service : transformer.getGraph().getVertices()){
				if (service.getOperation()!= null){
					numOfActions++;
				}
			}
			boolean noReplacements = false;
			// run the algorithm
			while (true) {

				// merge common variables
				// transformer.createLinkedVariableGraph();
				// select best operation to replace
				ArrayList<Transformer.ReplaceInformation> replaceInformations = transformer
						.getReplaceInformation(operations, 10);
				// if (replaceInformations.isEmpty()) {
				// noReplacements = true;
				// }
				double maxWeight = Double.NEGATIVE_INFINITY;
				Transformer.ReplaceInformation selection = null;
				for (Transformer.ReplaceInformation replace : replaceInformations)
					if (replace.getWeight() > maxWeight) {
						selection = replace;
						maxWeight = replace.getWeight();
					}
				if (selection == null)
					break;

				correctStepProbability.add(new WeightReport(selection, replaceInformations));
				// if an ideal operation was selected, try to replace with one
				// of
				// the implementations instead
				if (!selection.getOperationToReplace().getRealOperations().isEmpty()) {
					replaceInformations = transformer.getReplaceInformation(
							selection.getOperationToReplace().getRealOperations(), selection.getTargetService(), 1);
					maxWeight = Double.NEGATIVE_INFINITY;
					for (Transformer.ReplaceInformation replace : replaceInformations)
						if (replace.getWeight() > maxWeight) {
							selection = replace;
							maxWeight = replace.getWeight();
						}
				}
				// perform the replacement
				selection.performReplacement(transformer.getGraph());
				// candidateOperations.remove(selection.getOperationToReplace());
				OwlService service = selection.getTargetService();
				// expand the workflow as much as possible
				transformer.expandOperations(service);

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
			for (OwlService service : transformer.getGraph().getVertices()) {
				if (service.getOperation() != null) {
					if (service.getOperation().getDomain() == null) {
						noReplacements = true;
					}
				}
			}
			if (!noReplacements) {
				transformer.createLinkedVariableGraph();

				int counter = 0;
				while (true) {

					OwlService replacedOperation = transformer.placeSingleLinkingOperation(candidateOperations);
					// added interlock to avoid infinite loops (counter)
					if (replacedOperation == null || counter>numOfActions*3)
						break;

					// Moved down in order to be executed only if op is not
					// null.
					transformer.expandOperations(replacedOperation);

					// Calculate total cost

					String currency = ((Operation) replacedOperation.getContent()).getAccessInfo()
							.getCommercialCostSchema().getCommercialCostCurrency();
					String chargeType = ((Operation) replacedOperation.getContent()).getAccessInfo()
							.getCommercialCostSchema().getCostPaymentChargeType();
					switch (chargeType) {
					case "per usage":
						costPerUsage.calculateCost(currency, replacedOperation);
						break;
					case "per month":
						costPerMonth.calculateCost(currency, replacedOperation);
						break;
					case "per  year":
						costPerYear.calculateCost(currency, replacedOperation);
						break;
					case "unlimited":
						costUnlimited.calculateCost(currency, replacedOperation);
						break;
					default:
						break;
					}

					// trial period
					int durationInUsages = ((Operation) replacedOperation.getContent()).getAccessInfo()
							.getCommercialCostSchema().getTrialSchema().getDurationInUsages();
					int durationInDays = ((Operation) replacedOperation.getContent()).getAccessInfo()
							.getCommercialCostSchema().getTrialSchema().getDurationInDays();
					workflowTrialPeriod.setTrialReport(durationInDays, durationInUsages);

					// License
					String licenseName = ((Operation) replacedOperation.getContent()).getAccessInfo().getLicense()
							.getLicenseName();
					workflowLicense.setLicenseReport(licenseName);

					transformer.createLinkedVariableGraph();
					// candidateOperations.remove(replacedOperation.getOperation());
				}

				//transformer.createLinkedVariableGraph();

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

			}
			return transformer.getGraph();
		} else {
			return null;
		}

	}

	/**
	 * <h1>getStepReports</h1> Returns the report on the correctness of each
	 * step of <code>transformationAlgorithm</code>.
	 * 
	 * @return a list of weight reports
	 */
	public static ArrayList<WeightReport> getStepReports() {
		return correctStepProbability;
	}

	/**
	 * <h1>getFinalReport</h1>
	 * 
	 * @param minimumTrust
	 *            (-1 uses weight-based calculations instead of probabilistic
	 *            ones)
	 * @return the final report for algorithm correctness
	 */
	public static WeightReport getFinalReport(double minimumTrust) {
		if (minimumTrust == -1)
			return new WeightReport(correctStepProbability);
		return new WeightReport(correctStepProbability, minimumTrust);
	}

	private static IStatus checkGraph(edu.uci.ics.jung.graph.Graph<OwlService, Connector> graph, final Display disp)
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

	/**
	 * <h1>codeGeneration</h1> Saves an OWL graph as Java code. This function
	 * serves mostly demonstrative purposes for the usage and capabilities of
	 * the <code>codeGenerator</code> package.
	 * 
	 * @param path
	 *            : the path of the file to save the code into
	 * @param graph
	 *            : the OWL graph
	 * @throws Exception
	 */
	// public static void codeGeneration(String path, Graph<OwlService,
	// Connector> graph) throws Exception {
	// String codeGenerated = "";
	// try {
	// System.out.print("Generating linear code... ");
	// codeGenerated = (new
	// eu.scasefp7.eclipse.servicecomposition.codeGenerator.LinearCodeGenerator()).generateCode(
	// graph, "tester", true);
	// } catch (Exception e) {
	// System.out.print("Failed\nGenerating class code... ");
	// codeGenerated = (new
	// eu.scasefp7.eclipse.servicecomposition.codeGenerator.NonLinearCodeGenerator()).generateCode(
	// graph, "tester", true);
	// }
	// System.out.println("Done");
	// // save code to file
	// if (!codeGenerated.isEmpty()) {
	// PrintWriter out = new PrintWriter(path);
	// out.println(codeGenerated);
	// out.close();
	// }
	// }

}
