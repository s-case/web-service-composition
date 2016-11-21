package eu.scasefp7.eclipse.servicecomposition.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import eu.scasefp7.eclipse.servicecomposition.Activator;

public class ColorPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

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
		CheckboxFieldEditor PWoperations = new CheckboxFieldEditor("Use PW operations",
				"Use ProgrammableWeb operations", getFieldEditorParent());
		addField(PWoperations);
		CheckboxFieldEditor mashapeOperations = new CheckboxFieldEditor("Use Mashape operations",
				"Use Mashape operations", getFieldEditorParent());
		addField(mashapeOperations);

		/*
		 * This does not work for regular field editors since there can be only
		 * one property change listener and we get overriden by the FieldParent
		 */
//		PWoperations.setExtraPropertyChangeListener(new IPropertyChangeListener() {
//			@Override
//			public void propertyChange(PropertyChangeEvent event) {
//				Object val = event.getNewValue();
//				Object valOld = event.getOldValue();
//
//				if (val instanceof Boolean && valOld instanceof Boolean && val != valOld) {
//					if (((Boolean) val)) {
//						getPreferenceStore().setValue("Use Mashape operations", !((Boolean) val));
//					}
//					mashapeOperations.setEnabled(!((Boolean) val), getFieldEditorParent());
//				}
//			}
//		});
//
//		mashapeOperations.setExtraPropertyChangeListener(new IPropertyChangeListener() {
//			@Override
//			public void propertyChange(PropertyChangeEvent event) {
//				Object val = event.getNewValue();
//				Object valOld = event.getOldValue();
//
//				
//				if (val instanceof Boolean && valOld instanceof Boolean && val != valOld) {
//					if (((Boolean) val)) {
//						getPreferenceStore().setValue("Use PW operations", !((Boolean) val));
//					}
//					PWoperations.setEnabled(!((Boolean) val), getFieldEditorParent());
//				}
//			}
//		});

	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Set the preferences for Web Service Composition plugin.");

	}

}
