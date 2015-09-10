package eu.scasefp7.eclipse.servicecomposition.operationCaller;

import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;



/**
 * <h1>OperationCaller</h1>
 * This class is used as an interface for calling various operations.
 * @author Manios Krasanakis
 */
public abstract class OperationCaller {
	/**
	 * <h1>callOperation</h1>
	 * This function calls a given operation.
	 * @param operation : the given operation to be called
	 * @throws Exception describes encountered errors
	 */
	public abstract void callOperation(Operation operation) throws Exception;
}
