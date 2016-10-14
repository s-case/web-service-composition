package eu.scasefp7.eclipse.servicecomposition.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import eu.scasefp7.eclipse.servicecomposition.Activator;

/**
 * Provides the default preferences for the service composition plugin.
 * 
 * @author mkoutli
 */
public class PreferencesInitializer extends AbstractPreferenceInitializer {

	/**
	 * Initializes this object.
	 */
	public PreferencesInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault("Operation", "216,228,248");
		store.setDefault("Condition", "0,255,255");
		store.setDefault("Input/Output", "128,0,128");
		store.setDefault("Matched Input/Output", "0,0,128");
		store.setDefault("Start Node", "128,128,128");
		store.setDefault("End Node", "128,128,128");
		store.setDefault("Use PW operations", false); 
		store.setDefault("Use Mashape operations", false); 
		
	}

}
