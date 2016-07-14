package eu.scasefp7.eclipse.servicecomposition.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import eu.scasefp7.eclipse.servicecomposition.Activator;

public class ColorPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{

	
	/**
	 * Initializes this object.
	 */
	public ColorPreferencesPage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		addField(new ColorFieldEditor("Operation", "Operation Node", getFieldEditorParent()));
		addField(new ColorFieldEditor("Condition", "Condition Node", getFieldEditorParent()));
		addField(new ColorFieldEditor("Input/Output", "Input/Output Node", getFieldEditorParent()));
		addField(new ColorFieldEditor("Matched Input/Output", "Matched Input/Output Node", getFieldEditorParent()));
		addField(new ColorFieldEditor("Start Node", "Start Node", getFieldEditorParent()));
		addField(new ColorFieldEditor("End Node", "End Node", getFieldEditorParent()));
	}
	
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Set the colors of Web Service Composition Workflow.");
	}
	
}
