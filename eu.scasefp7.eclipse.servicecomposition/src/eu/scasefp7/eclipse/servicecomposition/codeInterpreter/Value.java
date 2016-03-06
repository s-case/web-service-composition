package eu.scasefp7.eclipse.servicecomposition.codeInterpreter;



import java.util.HashMap;

import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;

/**
 * <h1>Value</h1>
 * This class extends the basic Argument class by adding a specific value.
 * That value is represented as a String. Class members are automatically
 * converted to instances of Value too.<br/>
 * Only use the <code>Value.getValue</code> function to ensure that same
 * Arguments are converted to same Values. This allows, for example, a
 * variable that is contained inside a class to be set externally and then
 * used by the class with no further complications.
 * 
 * @author Manios Krasanakis
 */
public class Value extends Argument{
	private static HashMap<Argument, Value> enumeration = new HashMap<Argument, Value>();
	/**
	 * value of the variable
	 */
	private String value = "";
	/**
	 * Generates the value from a given variable. Always use <code>getValue</code> instead of
	 * this constructor, so that each variable corresponds to a single Value.<br/>
	 * This constructor also takes into account sub-type structure for variables.
	 * @param variable
	 * @throws Exception 
	 */
	public Value(Argument variable){
		super(variable.getName().toString(), variable.getType(), variable.isArray(), variable.isNative(), null);
		setIsRequired(variable.isRequired());
		enumeration.put(variable, this);
		if(variable instanceof Value)
			value = ((Value)variable).value;
		for(Argument arg : variable.getSubtypes()){
			getSubtypes().add(getValue(arg));
		}
		for(Object arg : variable.getParent()){
			getParent().add(arg);
		}
		setOwlService(variable.getOwlService());
	}
	/**
	 * <h1>getValue</h1>
	 * @param arg : a variable Argument
	 * @return returns a Value that corresponds to the given variable
	 * @throws Exception 
	 */
	public static Value getValue(Argument arg){
		if(arg instanceof Value)
			return (Value)arg;
		Value val = enumeration.get(arg);
		if(val==null)
			try {
				val = new Value(arg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return val;
	}
	/**
	 * <h1>getValue<h1>
	 * @return the value stored inside
	 */
	public String getValue(){
		return value;
	}
	/**
	 * <h1>getCompleteValue<h1>
	 * @return the value stored inside
	 */
	public String getCompleteValue(){
		String ret = value;
		for(Argument arg : getSubtypes())
			ret += "\n"+arg.toString();
		return ret;
	}
	/**
	 * <h1>setValue<h1>
	 * @param value
	 * changes value stored inside
	 */
	public void setValue(String value){
		this.value = value;
	}
	@Override
	public String toString(){
		if(value.isEmpty())
			return super.toString();
		return super.toString()+" = "+value;
	}
}
