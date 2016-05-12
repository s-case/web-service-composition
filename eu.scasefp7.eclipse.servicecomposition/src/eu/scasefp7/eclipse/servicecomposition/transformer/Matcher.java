package eu.scasefp7.eclipse.servicecomposition.transformer;

import eu.scasefp7.eclipse.servicecomposition.Activator;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Service;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.transformer.Similarity.ComparableName;
import eu.scasefp7.eclipse.servicecomposition.operation.ipr.Description;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * <h1>Matcher</h1> This class contains functions for variable comparison and
 * merging.
 * 
 * @author Manios Krasanakis
 */
public class Matcher {
	public static double NAME_SIMILARITY_WEIGHT = 4; // weight according to
														// action and operation
														// name similarity
	private static double OUTPUT_TO_INPUT_WEIGHT = 1; // weight for outputs that
														// can be used as inputs
														// from nearby
														// operations
	private static double INPUT_TO_INPUT_WEIGHT = 0; // weight for common inputs
														// with nearby
														// operations (should
														// most likely be 0)
	private static double POSSIBLE_INPUT_WEIGHT = 1; // weight for possible
														// inputs (positive
														// values tend to
														// maximize number of
														// possible inputs while
														// minimizing redundant
														// inputs)
	private static double MANDATORY_INPUT_WEIGHT = 1; // weight for mandatory
														// inputs (positive
														// values cause a slight
														// bias towards not
														// wanting redundant
														// inputs)
	private static double BIAS = 0; // biases matcher towards being stricter for
									// negative values
	private static double VARIABLE_SIMILARITY_THRESHOLD = 0.5;
	public static double NAME_SIMILARITY_THRESHOLD = 0;
	private static double DESCRIPTION_WEIGHT = 3;
	private static double VARIABLE_MATCHING_THRESHOLD = 0.5;
	private static ArrayList<String> ignoreNames = new ArrayList<String>();

	public static double getMaxWeight(boolean isRestfulWithDescription) {
		if (isRestfulWithDescription) {
			DESCRIPTION_WEIGHT = 3.0;
			NAME_SIMILARITY_WEIGHT = 2.0;
		} else {
			DESCRIPTION_WEIGHT = 1.0;
			NAME_SIMILARITY_WEIGHT = 4.0;
		}
		return Math.max(NAME_SIMILARITY_WEIGHT, 0) + Math.max(OUTPUT_TO_INPUT_WEIGHT, 0)
				+ Math.max(POSSIBLE_INPUT_WEIGHT, 0) + Math.max(MANDATORY_INPUT_WEIGHT, 0)
				+ Math.max(DESCRIPTION_WEIGHT, 0);
	}

	/**
	 * <h1>loadProperties</h1> Loads matching properties from the file
	 * <i>matcher.properties</i>.
	 */
	public static void loadProperties() {
		Similarity.loadProperties();
		Properties prop = new Properties();
		String propFileName = "matcher.properties";
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		try {
			URL fileURL = bundle.getEntry("matcher.properties");
			// InputStream inputStream = new FileInputStream(new File(new
			// URI(FileLocator.resolve(fileURL).toString().replaceAll(" ",
			// "%20"))));
			URL url = new URL("platform:/plugin/" + Activator.PLUGIN_ID + "/matcher.properties");
			InputStream inputStream = url.openConnection().getInputStream();
			prop.load(inputStream);
			NAME_SIMILARITY_WEIGHT = Double.parseDouble(prop.getProperty("matcher.NAME_SIMILARITY_WEIGHT"));
			OUTPUT_TO_INPUT_WEIGHT = Double.parseDouble(prop.getProperty("matcher.OUTPUT_TO_INPUT_WEIGHT"));
			INPUT_TO_INPUT_WEIGHT = Double.parseDouble(prop.getProperty("matcher.INPUT_TO_INPUT_WEIGHT"));
			POSSIBLE_INPUT_WEIGHT = Double.parseDouble(prop.getProperty("matcher.POSSIBLE_INPUT_WEIGHT"));
			MANDATORY_INPUT_WEIGHT = Double.parseDouble(prop.getProperty("matcher.MANDATORY_INPUT_WEIGHT"));
			BIAS = Double.parseDouble(prop.getProperty("matcher.BIAS"));
			VARIABLE_SIMILARITY_THRESHOLD = Double
					.parseDouble(prop.getProperty("matcher.VARIABLE_SIMILARITY_THRESHOLD"));
			DESCRIPTION_WEIGHT = Double.parseDouble(prop.getProperty("metadata.DESCRIPTION_WEIGHT"));
			ignoreNames.add("a");
			ignoreNames.add("the");
			ignoreNames.add("of");
		} catch (Exception e) {
			System.err.println("Error occured while trying to load matcher settings from " + propFileName);
		}
	}

	/**
	 * <h1>hasSame</h1>
	 * 
	 * @param candidate
	 *            : the candidate variables
	 * @param with
	 *            : the variable to check against
	 * @return true if the candidate variable or one of its children matches
	 *         with the checking variable (according to
	 *         <code>sameVariable</code>)
	 */
	public static boolean hasSame(Importer.Argument candidate, Importer.Argument with) {
		if (sameVariable(candidate, with))
			return true;
		for (Importer.Argument subtype : candidate.getSubtypes())
			if (sameVariable(subtype, with))
				return true;
		return false;
	}

	/**
	 * <h1>match</h1> Returns a double value that describes how much an action
	 * is similar to an operation. Values are not scaled, but greater values
	 * representing a better match.
	 * 
	 * @param action
	 * @param operation
	 * @param mandatoryArguments
	 * @param possibleArguments
	 * @param possibleOutputs
	 * @return the match between an action and an operation, values <=0 are
	 *         returned for non-matching action-operation pairs
	 */
	public static double match(Service action, Importer.Operation operation,
			ArrayList<Importer.Argument> mandatoryArguments, ArrayList<Importer.Argument> possibleArguments,
			ArrayList<Importer.Argument> possibleOutputs, int minInputOutputDiff) {
		for (Importer.Argument mandatoryArgument : mandatoryArguments) {
			boolean found = false;
			for (Importer.Argument arg : operation.getInputs()) {
				if (sameVariable(arg, mandatoryArgument)) {
					found = true;
					break;
				}
			}
			if (!found)
				return 0;
		}
		double nameSimilarity = Similarity.similarity(action.getName(), operation.getName());
		double numberOfWords = action.getName().getComparableForm().split("\\s").length;
		if (nameSimilarity > numberOfWords) {
			nameSimilarity = numberOfWords;
		}
		// double numberOfWords =
		// operation.getName().getComparableForm().split("\\s").length;
		// nameSimilarity = nameSimilarity / numberOfWords;
		double descriptionSimilarity = 0;
		ArrayList<String> description = new ArrayList<String>();
		String noDublicatesDesc = "";
		if (!operation.getDescription().isEmpty()) {
			substring(operation.getDescription(), description);

			for (String desc : description) {
				if (!ignoreNames.contains(desc))
					noDublicatesDesc += desc + " ";
			}
		}

		if (!operation.getDescription().isEmpty()) {
			descriptionSimilarity = Similarity.similarity(new ComparableName(noDublicatesDesc), action.getName());
			// double DescnumberOfWords=((Description)
			// operation.getAccessInfo().getDescription().get(0)).getDescription().split("\\s").length;
			// descriptionSimilarity = descriptionSimilarity / numberOfWords;
		}
		if (descriptionSimilarity > numberOfWords) {
			descriptionSimilarity = numberOfWords;
		}

		if (!action.getName().isEmpty() && nameSimilarity < NAME_SIMILARITY_THRESHOLD)
			return 0;

		double inputSimilarity = 0;
		double mandatoryInputSimilarity = 0;
		double inputToInputSimilarity = 0;
		for (Importer.Argument arg : operation.getInputs()) {
			for (Importer.Argument possibleArgument : possibleArguments) {
				if (hasSame(arg, possibleArgument)) {
					inputSimilarity += 1.0 / possibleArguments.size();
					break;
				}
			}
			for (Importer.Argument possibleArgument : mandatoryArguments) {
				if (hasSame(arg, possibleArgument)) {
					mandatoryInputSimilarity += 1.0 / mandatoryArguments.size();
					break;
				}
			}
			// for (Importer.Argument possibleArgument : possibleOutputs) {
			// if (hasSame(arg, possibleArgument)) {
			// inputToInputSimilarity += 1.0 / operation.getInputs().size();
			// break;
			// }
			// }
		}
		ArrayList<Argument> nativeOutputs = new ArrayList<Argument>();
		for (Argument out : operation.getOutputs()) {
			getNative(out, nativeOutputs);
		}
		double outputSimilarity = 0;
		
			for (Importer.Argument possibleArgument : possibleOutputs) {
				for (Importer.Argument arg : nativeOutputs) {
				if (hasSame(arg, possibleArgument)) {
					// outputSimilarity += 1.0 / operation.getOutputs().size();
					outputSimilarity += 1.0 / possibleOutputs.size();
					break;
				}

			}
		}

		int diff = (int) Math.round(
				inputSimilarity * operation.getInputs().size() - outputSimilarity * operation.getOutputs().size());

		if (minInputOutputDiff != Integer.MIN_VALUE) {
			// if (diff >= minInputOutputDiff)
			// return -diff + minInputOutputDiff + 1;
			// else
			// return 0;
			return inputSimilarity + outputSimilarity;
		}

		double similarity = 0;
		// similarity = BIAS + (operation.getMetadata() != null ?
		// operation.getMetadata().getSimilarity(action) : 0);
		if (operation.getType().equalsIgnoreCase("RESTful") && !operation.getDescription().isEmpty()) {
			DESCRIPTION_WEIGHT = 3.0;
			NAME_SIMILARITY_WEIGHT = 2.0;
		} else {
			NAME_SIMILARITY_WEIGHT = 4.0;
			DESCRIPTION_WEIGHT = 1.0;
		}
		similarity += DESCRIPTION_WEIGHT * descriptionSimilarity;
		similarity += POSSIBLE_INPUT_WEIGHT * inputSimilarity;
		// similarity += INPUT_TO_INPUT_WEIGHT * inputToInputSimilarity;
		similarity += MANDATORY_INPUT_WEIGHT * mandatoryInputSimilarity;
		similarity += OUTPUT_TO_INPUT_WEIGHT * outputSimilarity;
		similarity += NAME_SIMILARITY_WEIGHT * nameSimilarity;

		// System.out.println("\""+action.toString()+"\" compared with
		// \""+operation.toString()+"\" (Weight: "+similarity+")");

		similarity = similarity / (DESCRIPTION_WEIGHT * numberOfWords + POSSIBLE_INPUT_WEIGHT + MANDATORY_INPUT_WEIGHT
				+ OUTPUT_TO_INPUT_WEIGHT + NAME_SIMILARITY_WEIGHT * numberOfWords);
		return similarity;
	}

	/**
	 * Separates a string into different strings every time " " character occurs
	 * and puts the strings in a list.
	 * 
	 * @param s:
	 *            the initial string
	 * @param list:
	 *            the empty list
	 * @return the list with the string values
	 */
	public static ArrayList<String> substring(String s, ArrayList<String> list) {
		int previousOccurance = 0;
		int counter = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == ' ') {
				counter++;
				if (counter > 1 && previousOccurance < i) {
					String part = s.substring(previousOccurance + 1, i);
					if (!list.contains(part)) {
						list.add(part);
					}
				} else if (counter == 1) {
					String part = s.substring(previousOccurance, i);
					if (!list.contains(part)) {
						list.add(part);
					}
				}
				previousOccurance = i;
			}
		}
		String addString = s.substring(previousOccurance + 1, s.length());
		if (!list.contains(addString))
			list.add(addString);

		return list;
	}

	/**
	 * <h1>sameVariable</h1> Compares two Argument instances. <code>match</code>
	 * and <code>getSameVariableInstances</code> depend on this function.
	 * 
	 * @param arg0
	 * @param arg1
	 * @return true if the arguments are considered to be the same variable
	 */
	public static boolean sameVariable(Importer.Argument arg0, Importer.Argument arg1) {
		return (arg0.getType().isEmpty() || arg1.getType().isEmpty()
				|| (arg0.getType().equals(arg1.getType()) && arg0.isArray() == arg1.isArray()))
				&& (arg0.getName().isEmpty() || arg1.getName().isEmpty()
						|| Similarity.similarity(arg0.getName(), arg1.getName())
								/ (Math.max(arg0.getName().getComparableForm().split("\\s").length, arg1.getName()
										.getComparableForm().split("\\s").length)) >= VARIABLE_SIMILARITY_THRESHOLD);
	}

	/**
	 * <h1>common</h1> Generates the intersection of two lists of objects.
	 * 
	 * @param l1
	 *            : first list of objects
	 * @param l2
	 *            : second list of objects
	 * @return l1 intersection l2
	 */
	public static ArrayList<Object> common(ArrayList<Object> l1, ArrayList<Object> l2) {
		ArrayList<Object> c = new ArrayList<Object>();
		for (Object service : l1)
			if (l2.contains(service))
				c.add(service);
		return c;
	}

	/**
	 * <h1>addToArray</h1> Adds to the first list of objects all elements from
	 * the second list of objects, <b>ignoring multiples</b> in the process.
	 * (Thus, <code>addToArray(l1, l2)</code> should be preferred over
	 * <code>l1.addAll(l2)</code> for set logic.)
	 * 
	 * @param l1
	 *            : the first list of objects
	 * @param l2
	 *            : the second list of objects
	 */
	private static void addToArray(ArrayList<Object> l1, ArrayList<Object> l2) {
		for (Object service : l2)
			if (!l1.contains(service))
				l1.add(service);
	}

	/**
	 * <h1>getSameVariableInstances</h1> Returns the output which has the
	 * highest similarity with an input (according to <code>sameVariable</code>
	 * )and is above a similarity threshold.<br/>
	 * If outputs have the same similarity with the input it selects the first
	 * one on the list.<br/>
	 * The inputs and outputs are of type OwlService, in order to be easily
	 * applied on data extracted from a graph.
	 * 
	 * @param input
	 * @param allOutputs
	 * @return theLinkedVariable
	 * @throws Exception
	 */
	public static OwlService getSameVariableInstances(OwlService input, ArrayList<OwlService> allOutputs)
			throws Exception {

		OwlService theLinkedVariable = null;
		Object newParent = new Object();
		ArrayList<Double> variableServiceSimilarities = new ArrayList<Double>();
		// Content of input
		Importer.Argument inputContent = input.getArgument();

		// If var=null throw exception
		if (inputContent == null)
			throw new Exception(input.toString() + " is not a property OwlService");

		// Put name similarities of this input and all the outputs in a list
		for (int i = 0; i < allOutputs.size(); i++) {

			double nameSimilarity = Similarity.similarity(allOutputs.get(i).getName(), input.getName());

			nameSimilarity = nameSimilarity / allOutputs.get(i).getName().getComparableForm().split("\\s").length;

//			nameSimilarity = nameSimilarity
//					/ Math.max(allOutputs.get(i).getName().getComparableForm().split("\\s").length,
//							input.getName().getComparableForm().split("\\s").length);

			variableServiceSimilarities.add(i, nameSimilarity);
		}

		// second pass: add detected to list (generate again instance list to
		double maxWeight = VARIABLE_MATCHING_THRESHOLD;
		OwlService outputMax = null;
		for (int i = 0; i < allOutputs.size(); i++) {
			Importer.Argument output = allOutputs.get(i).getArgument();

			if (output != null && sameVariable(inputContent, output)
					&& (common(inputContent.getParent(), output.getParent()).isEmpty())) {

				if (variableServiceSimilarities.get(i) > maxWeight) {
					maxWeight = variableServiceSimilarities.get(i);
					outputMax = allOutputs.get(i);
					newParent = allOutputs.get(i).getArgument().getParent();

				}

			}
		}

		theLinkedVariable = outputMax;
		inputContent.addParent(newParent);

		return theLinkedVariable;
	}

	/**
	 * <h1>mergeVariables</h1> Combines all variables in the given array of
	 * arguments to produce a more complete variable that is considered the same
	 * with each of them. This function should take as input the list generated
	 * by calling <code>getSameVariableInstances</code>.
	 * 
	 * @param variables
	 * @return the merged variable
	 * @throws Exception
	 *             if variables cannot be marged (this should never occur when
	 *             // * using <code>getSameVariableInstances</code>) //
	 */
	// public static Importer.Argument
	// mergeVariables(ArrayList<Importer.Argument> variables) throws Exception {
	// String name = "";
	// String type = "";
	// boolean isArray = false;
	// boolean isNative = true;
	// ArrayList<Importer.Argument> subtypes = new
	// ArrayList<Importer.Argument>();
	// ArrayList<Object> classInstanceList = new ArrayList<Object>();
	// for (Importer.Argument arg : variables) {
	// if (arg != null) {// && common(classInstanceList,
	// // arg.getParent()).isEmpty()){
	// if (name.isEmpty())
	// name = arg.getName().toString();
	// // else if(!name.equals(arg.getName()))
	// // throw new
	// // Exception("Cannot merge variables due to different non-empty
	// // variable names: "+name+", "+arg.getName());
	// if (type.isEmpty())
	// type = arg.getType();
	// else if (!arg.getType().isEmpty() && !type.equals(arg.getType()))
	// throw new Exception("Cannot merge variables due to different non-empty
	// variable types: " + type
	// + ", " + arg.getType());
	// isArray = isArray || arg.isArray();
	// isNative = isNative && arg.isNative();
	// addToArray(classInstanceList, arg.getParent());
	// for (Argument sub : arg.getSubtypes())
	// if (!subtypes.contains(sub))
	// subtypes.add(sub);
	// }
	// }
	// if (type.isEmpty())
	// type = Importer.stringType;
	// Argument arg = new Importer.Argument(name, type, isArray, isNative,
	// subtypes);
	// addToArray(arg.getParent(), classInstanceList);
	// return arg;
	// }

	/**
	 * <h1>createCommonVariable</h1> Calls <code>createCommonVariable</code> for
	 * the argument contents of all listed OwlServices.
	 * 
	 * @param variableServices
	 * @return an OwlService which contains the merged variable
	 * @throws Exception
	 */
	// public static OwlService createCommonVariable(ArrayList<OwlService>
	// variableServices) throws Exception {
	// ArrayList<Importer.Argument> variables = new
	// ArrayList<Importer.Argument>();
	// for (OwlService variableService : variableServices) {
	// Importer.Argument arg = variableService.getArgument();
	// if (arg != null)
	// variables.add(arg);
	// }
	// return new OwlService(mergeVariables(variables));
	// }

	/**
	 * <h1>mergeVariables</h1> Easier call for <code>mergeVariables</code> for
	 * two arguments (it is common to use it).
	 * 
	 * @param var0
	 * @param var1
	 * @return
	 * @throws Exception
	 */
	// public static Importer.Argument mergeVariables(Importer.Argument var0,
	// Importer.Argument var1) throws Exception {
	// ArrayList<Importer.Argument> variables = new
	// ArrayList<Importer.Argument>();
	// variables.add(var0);
	// variables.add(var1);
	// return mergeVariables(variables);
	// }
	private static void getNative(Argument output, ArrayList<Argument> nativeOutputs) {
		if (output.isNative() && !output.isArray()) {
			nativeOutputs.add(output);
		}
		if (!output.isArray()) {
			for (Argument sub : output.getSubtypes()) {
				getNative(sub, nativeOutputs);
			}
		}
	}
}
