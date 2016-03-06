package eu.scasefp7.eclipse.servicecomposition.codeInterpreter;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * <h1>UserInput</h1>
 * This class contains modal user interfaces that can be called to get data inside the program flow.
 * @author Manios Krasanakis
 */
public class UserInput {
	/**
	 * user selection
	 */
	private static Object selectedObject;
	/**
	 * <h1>getSelection</h1>
	 * Selects a single one among a multitude of candidates (returns directly if not candidate given).
	 * @param name : the dialog name
	 * @param candidates
	 * @param text : should map candidates to their appropriate text
	 * @return the selected candidate
	 */
	public static Object getSelection(String name, final ArrayList<?> candidates, HashMap<?, String> text){
		if(candidates.size()==0)
			return null;
		selectedObject = null;
		final JDialog frame = new JDialog();
		frame.setTitle(name);
		frame.setLayout(new GridLayout(0, 1));
		final HashMap<Object, JRadioButton> options = new HashMap<Object, JRadioButton>();
		for(Object obj : candidates){
			JRadioButton option = new JRadioButton((text==null || text.get(obj)==null)?(" "+obj.toString()+" "):text.get(obj), false);
			frame.add(option);
			options.put(obj, option);
		}
		JButton apply = new JButton("Select");
		frame.add(apply);
		apply.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				for(Object obj : candidates){
					if(options.get(obj).isSelected())
						selectedObject = obj;
				}
				if(selectedObject!=null)
					frame.setVisible(false);
				else
					JOptionPane.showMessageDialog(null, "Please select something");
			}
		});
		frame.pack();
		frame.setModal(true);
		frame.setVisible(true);
		if(selectedObject==null)
			System.err.println("Failed to get user input");
		return selectedObject;
	}
	
	/**
	 * <h1>inputVariables</h1>
	 * This function asks the user for input to be assigned to a number of variables.
	 * @param name : the dialog name
	 * @param variables
	 */
	public static void inputVariables(String name, final ArrayList<Value> variables){
		if(variables.isEmpty())
			return;
		final JDialog frame = new JDialog();
		frame.setTitle(name);
		frame.setLayout(new GridLayout(0, 2));
		final HashMap<Value, JTextField> fields = new HashMap<Value, JTextField>();
		for(Value var : variables){
			frame.add(new JLabel(" "+var.getName()+" ("+var.getType()+") "));
			JTextField field = new JTextField(var.getValue());
			frame.add(field);
			fields.put(var, field);
		}
		JButton apply = new JButton("Done");
		frame.add(apply);
		apply.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				String err = "";
				for(Value var : variables){
					if(fields.get(var).getText().isEmpty())
						err += "- "+var.getName()+"\n";
					else
						var.setValue(fields.get(var).getText());
				}
				if(err.isEmpty())
					frame.setVisible(false);
				else
					JOptionPane.showMessageDialog(null, "Missing values for:\n"+err);
			}
		});
		frame.pack();
		frame.setModal(true);
		frame.setVisible(true);
	}
}
