package eu.fp7.scase.servicecomposition.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import eu.fp7.scase.servicecomposition.transformer.Similarity.ComparableName;



/**
 * <h1>PythonImporter</h1>
 * This class can be used to import services from Python files.
 * @author Manios Krasanakis
 */
public class PythonImporter extends Importer{
	
	/**
	 * <h1>PythonOperation</h1>
	 * This class basically implements a different constructor to <code>Operation</code>
	 * that allows variable generation from a Python function body
	 */
	public static class PythonOperation extends Operation{
		/**
		 * Generates an operation from the contents of Python function inside a file.
		 * @param pythonImplementation
		 * @param pythonFileName
		 * @param domainList
		 */
		public PythonOperation(String pythonImplementation, String pythonFileName) {
			super();
			domain = domainList.get(pythonFileName);
			if(!pythonFileName.isEmpty() && domain==null){
				//throw new Exception("Domain "+domainName+" has not been declared");
				domainList.put(pythonFileName, domain = new ApplicationDomain(pythonFileName));
			}
			String retVariable = "";
			for(String line : pythonImplementation.split("\n")){
				if(line.trim().startsWith("def ")){
					name = new ComparableName(line.substring(4, line.indexOf("(")).trim());
					String args = line.substring(line.indexOf("(")+1, line.lastIndexOf(")"));
					for(String arg : args.split(",")){
						if(!arg.trim().isEmpty())
							inputs.add(new Argument(arg.trim(), "", false, false, null));
					}
				}
				int retIndex = line.indexOf("return ");
				if(retIndex!=-1){
					retIndex += 6;
					int end = line.indexOf(";", retIndex);
					if(end==-1)
						retVariable = line.substring(retIndex).trim();
					else
						retVariable = line.substring(retIndex, end).trim();
				}
				int retIndex2 = line.indexOf("cost");
				if(retIndex2!=-1){
					retIndex2 += 8;
					int end = line.length()-2;
					if(end==-1){
						float cost=Float.parseFloat(line.substring(retIndex2).trim());
						accessInfo.getCommercialCostSchema().setCommercialCost(cost);
					}
					else{
						float cost=Float.parseFloat(line.substring(retIndex2, end).trim());
						accessInfo.getCommercialCostSchema().setCommercialCost(cost);
						
					}
				}
				
				int retIndex3 = line.indexOf("charge_type");
				if(retIndex3!=-1){
					retIndex3 += 15;
					int end = line.length()-2;
					if(end==-1){
						String chargeType=line.substring(retIndex3).trim();
						accessInfo.getCommercialCostSchema().setCostPaymentChargeType(chargeType);
					}
					else{
						String chargeType=line.substring(retIndex3, end).trim();
						accessInfo.getCommercialCostSchema().setCostPaymentChargeType(chargeType);
						
					}
				}
				
				int retIndex4 = line.indexOf("currency");
				if(retIndex4!=-1){
					retIndex4 += 12;
					int end = line.length()-2;
					if(end==-1){
						String currency=line.substring(retIndex4).trim();
						accessInfo.getCommercialCostSchema().setCommercialCostCurrency(currency);
					}
					else{
						String currency=line.substring(retIndex4, end).trim();
						accessInfo.getCommercialCostSchema().setCommercialCostCurrency(currency);
						
					}
				}
				
				int retIndex5 = line.indexOf("duration_in_days");
				if(retIndex5!=-1){
					retIndex5 += 20;
					int end = line.length()-2;
					if(end==-1){
						int duration=Integer.parseInt(line.substring(retIndex5).trim());
						accessInfo.getCommercialCostSchema().getTrialSchema().setDurationInDays(duration);
					}
					else{
						int duration=Integer.parseInt(line.substring(retIndex5, end).trim());
						accessInfo.getCommercialCostSchema().getTrialSchema().setDurationInDays(duration);
						
					}
				}
				
				
				int retIndex6 = line.indexOf("duration_in_usages");
				if(retIndex6!=-1){
					retIndex6 += 22;
					int end = line.length()-2;
					if(end==-1){
						int duration=Integer.parseInt(line.substring(retIndex6).trim());
						accessInfo.getCommercialCostSchema().getTrialSchema().setDurationInUsages(duration);
					}
					else{
						int duration=Integer.parseInt(line.substring(retIndex6, end).trim());
						accessInfo.getCommercialCostSchema().getTrialSchema().setDurationInUsages(duration);
						
					}
				}
				
				int retIndex7 = line.indexOf("license_name");
				if(retIndex7!=-1){
					retIndex7 += 16;
					int end = line.length()-2;
					if(end==-1){
						String licence=line.substring(retIndex7).trim();
						accessInfo.getLicense().setLicenseName(licence);
					}
					else{
						String licence=line.substring(retIndex7, end).trim();
						accessInfo.getLicense().setLicenseName(licence);
						
					}
				}
				
			}
			for(String var : retVariable.split(","))
				outputs.add(new Argument(var.trim(), "", false, false, null));
			if(outputs.isEmpty())
				outputs.add(new Argument(name+"Result", "", false, false, null));
		}
	}
	
	/**
	 * <h1>importPath</h1>
	 * Traverses all folder contents (including sub-folders) and uses <code>importPythonAsOwl</code>
	 * to import all Python functions from detected <i>.py</i> files.
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<Operation> importPath(String path) throws Exception{
		Bundle bundle = Platform.getBundle("eu.fp7.scase.serviceComposition");
		URL fileURL = bundle.getEntry(path);
		File file1=new File(FileLocator.resolve(fileURL).toURI());
		File folder = new File(file1.getAbsolutePath());
		if(folder.isFile())
			return importPythonAsOwl(path);
		ArrayList<Operation> operations = new ArrayList<Operation>();
		if(path.isEmpty())
			return operations;
		File[] listOfFiles = folder.listFiles();
		Arrays.sort(listOfFiles);
		for (File file : listOfFiles)
		    if (file.isFile() && (file.getName().contains(".py")))
		    	operations.addAll(importPath(path+file.getName()));
		for (File file : listOfFiles) 
		    if(file.isDirectory() && (file.getName()!=".") && (file.getName()!=".."))
		    	operations.addAll(importPath(path+file.getName()+"/"));
		return operations;
	}
	
	/**
	 * <h1>importPythonAsOwl</h1>
	 * Imports all Python file functions as operations. This function uses the appropriate
	 * <code>Importer.OwlOperation</code> constructor for code and comments detected.
	 * @param pythonPath : the path of the Python file
	 * @return a list of operations that match the functions detected in the file
	 * @throws Exception
	 */
	public static ArrayList<Operation> importPythonAsOwl(String pythonPath) throws Exception{
		ArrayList<Operation> allOperations = new ArrayList<Operation>();
		Bundle bundle = Platform.getBundle("eu.fp7.scase.serviceComposition");
		URL fileURL = bundle.getEntry(pythonPath);
		File file=new File(FileLocator.resolve(fileURL).toURI());
		BufferedReader br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	            if(line.startsWith("def ")){
	    	        String code = sb.toString();
	    	        sb = new StringBuilder();
	    	        if(!code.isEmpty())
	    	        	allOperations.add(new PythonOperation(code, pythonPath));
	            }
	            sb.append(line);
	            sb.append(System.lineSeparator());
	            line = br.readLine();
	        }
	        String code = sb.toString();
	        if(code!=null && code.contains("def "))
	        	allOperations.add(new PythonOperation(code, pythonPath));
	    }
	    finally {
	        br.close();
	    }
		return allOperations;
	}
}
