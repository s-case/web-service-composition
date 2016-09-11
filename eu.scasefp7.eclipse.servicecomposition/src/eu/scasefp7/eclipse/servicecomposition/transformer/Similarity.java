package eu.scasefp7.eclipse.servicecomposition.transformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import eu.scasefp7.eclipse.servicecomposition.Activator;
import eu.scasefp7.eclipse.servicecomposition.operationCaller.RAMLCaller;

/**
 * <h1>Similarity</h1> This class contains functions for comparing strings.
 */
public class Similarity {

	private static String[] invalidNames = new String[] { "string", "long", "int", "float", "double", "boolean", "class", "enum", "static", "private", "package", "super" };
	public static class ComparableName {
		/**
		 * the original name
		 */
		protected String content;
		/**
		 * name after camel case separation
		 */
		protected String comparable;

		public ComparableName(String name) {
			name = name.trim();
			this.content = name;
//			comparable = name.replaceAll("(.)([A-Z])([a-z])", "$1 $2$3").toLowerCase();
			comparable = name.replaceAll(
				      String.format("%s|%s|%s",
				    	         "(?<=[A-Z])(?=[A-Z][a-z])",
				    	         "(?<=[^A-Z])(?=[A-Z])",
				    	         "(?<=[A-Za-z])(?=[^A-Za-z])"
				    	      ),
				    	      " "
				    	   ).toLowerCase();
			comparable = comparable.replaceAll("\\_", " ").replaceAll("\\-", " ").replaceAll("\\s+", " ").trim();
			String[] bag = comparable.split("\\s");
			String comparable = "";
			for (int i = 0; i < bag.length; i++)
				comparable = " " + comparable + transformPredicate(bag[i]);
			comparable = comparable.trim();
		}

		public boolean isEmpty() {
			return comparable.isEmpty();
		}

		@Override
		public String toString() {
			return content;
		}

		public String getComparableForm() {
			return comparable;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}
		
		public String getJavaValidContent() {
			String name = content;
			name = name.replaceAll("[^A-Za-z0-9()_\\[\\]]", "");
			if (RAMLCaller.stringIsItemFromList(name, invalidNames)){
				name += "_";
			}
			return name;
		}

	}

	private static String[] ignorable;
	private static String[] endings;
	public static double levenshteinThreshold = 0;

	public static void loadProperties() {
		Properties prop = new Properties();
		String propFileName = "matcher.properties";
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		try {
			URL fileURL = bundle.getEntry("matcher.properties");
			//InputStream inputStream = new FileInputStream(new File(new URI(FileLocator.resolve(fileURL).toString().replaceAll(" ", "%20"))));
			URL url = new URL("platform:/plugin/" +Activator.PLUGIN_ID+"/matcher.properties");
			InputStream inputStream = url.openConnection().getInputStream();
			prop.load(inputStream);
			levenshteinThreshold = Double.parseDouble(prop.getProperty("similarity.LEVENSHTEIN_THRESHOLD"));
			endings = prop.getProperty("similarity.ENDINGS").trim().split("\\s*\\,\\s*");
			ignorable = prop.getProperty("similarity.IGNORE_PREDICATES").trim().split("\\s*\\,\\s*");
		} catch (Exception e) {
			System.err.println("Error occured while trying to load similarity settings from " + propFileName);
		}
	}

	public static boolean comparePredicates(String predicate1, String predicate2) {
		return transformPredicate(predicate1).equalsIgnoreCase(transformPredicate(predicate2));
	}

	private static String transformPredicate(String w) {
			for (String ending : endings) {
				if (w.endsWith(ending)) {
					w = w.substring(0, w.length() - ending.length());
					break;
				}
			}
		for (String predicate : ignorable)
			if (predicate.equalsIgnoreCase(w))
				return "";
		return w;
	}

	public static double similarity(ComparableName s1, ComparableName s2) {
		return similarity(s1.getComparableForm(), s2.getComparableForm());
	}

	public static double similarity(String s1, String s2) {
		String[] bag1 = s1.split("\\s+");
		String[] bag2 = s2.split("\\s+");
		double ret = 0;
		for (String word1 : bag1)
			for (String word2 : bag2)
				ret += continuesSimilarity(word1, word2);
		// ret /= bag1.length*bag2.length;
		return ret;
	}

	/**
	 * <h1>continuesSimilarity</h1> This function compares two strings by
	 * viewing them as a "bag of characters" and normalizing their Levenshtein
	 * distance. Values less than a certain threshold are snapped to
	 * <code>0</code>.
	 * 
	 * @param s1
	 *            : the first String argument
	 * @param s2
	 *            : the second String argument
	 * @return similarity in range [0, 1] between its arguments
	 */
	public static double continuesSimilarity(String s1, String s2) {
		// determine longer and shorter string
		String longer = s1;
		String shorter = s2;
		if (s1.length() < s2.length()) {
			longer = s2;
			shorter = s1;
		}
		// empty strings are similar
		int longerLength = longer.length();
		if (longerLength == 0)
			return 1.0;
		// use complementary of the Levenshtein distance percentage of length
		double similarity = 1.0 - calculateLevenshteinDistance(longer, shorter) / (double) longerLength;
		if (similarity < levenshteinThreshold)
			return 0;
		return similarity;
	}

	/**
	 * <h1>calculateLevenshteinDistance</h1> Calculates the Levenshtein distance
	 * between two strings. Levenshtein distance is the number of operations
	 * needed to transform the second string into the first. Those operations
	 * can only be insertions, deletions and substitutions. (For more
	 * information see
	 * <url>http://en.wikipedia.org/wiki/Levenshtein_distance</url>)<br/>
	 * This function uses a bottom-up implementation.
	 * 
	 * @param s1
	 * @param s2
	 * @return the Levenshtein distance between the two strings
	 */
	public static int calculateLevenshteinDistance(String s1, String s2) {
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();
		int[] costs = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0)
					costs[j] = j;
				else {
					if (j > 0) {
						int newValue = costs[j - 1];
						if (s1.charAt(i - 1) != s2.charAt(j - 1))
							newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
						costs[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}
			if (i > 0)
				costs[s2.length()] = lastValue;
		}
		return costs[s2.length()];
	}
}
