package eu.scasefp7.eclipse.servicecomposition.ui;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for renaming condition node
 * 
 * @author mkoutli
 *
 */
public class RenameConditionDialog extends TitleAreaDialog {
	private Text txtConditionName;
	private String conditionName;
	private Text txtConditionValue;
	private String conditionValue;
	private String conditionSymbol;
	private int index;
	private Combo lineHeight;

	public RenameConditionDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		setTitle("Create condition");
		setMessage(
				"Please enter a variable's name (e.g the name of a previous output), a value for this variable and a comparison symbol.",
				IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);

		createConditionName(container);
		createConditionValue(container);
		createConditionSymbol(container);
		return area;
	}

	@Override
	public void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText("Condition node");
	}

	private void createConditionName(Composite container) {
		Label lbtFirstName = new Label(container, SWT.NONE);
		lbtFirstName.setText("Variable name");

		GridData dataFirstName = new GridData();
		dataFirstName.grabExcessHorizontalSpace = true;
		dataFirstName.horizontalAlignment = GridData.FILL;

		txtConditionName = new Text(container, SWT.BORDER);
		txtConditionName.setLayoutData(dataFirstName);
	}

	private void createConditionValue(Composite container) {
		Label lbtFirstName = new Label(container, SWT.NONE);
		lbtFirstName.setText("Value");

		GridData dataFirstName = new GridData();
		dataFirstName.grabExcessHorizontalSpace = true;
		dataFirstName.horizontalAlignment = GridData.FILL;

		txtConditionValue = new Text(container, SWT.BORDER);
		txtConditionValue.setLayoutData(dataFirstName);
	}

	private void createConditionSymbol(Composite container) {

		lineHeight = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
		lineHeight.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false));
		String[] choices = { "==", "!=", ">", "<" };
		lineHeight.setItems(choices);
		lineHeight.select(0);

	}

	public void setDialogLocation() {
		Rectangle monitorArea = getShell().getDisplay().getPrimaryMonitor().getBounds();
		Rectangle shellArea = getShell().getBounds();
		int x = monitorArea.x + (monitorArea.width - shellArea.width) / 2;
		int y = monitorArea.y + (monitorArea.height - shellArea.height) / 2;
		getShell().setLocation(x, y);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	// save content of the Text field because it gets disposed
	// as soon as the Dialog closes
	private void saveInput() {
		conditionName = txtConditionName.getText();
		conditionValue = txtConditionValue.getText();
		index = lineHeight.getSelectionIndex();
		if (index == 0) {
			conditionSymbol = "==";
		} else if (index == 1) {
			conditionSymbol = "!=";
		} else if (index == 2) {
			conditionSymbol = ">";
		} else if (index == 3) {
			conditionSymbol = "<";
		}
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	public String getConditionName() {
		return conditionName;
	}

	public String getConditionValue() {
		return conditionValue;
	}

	public String getConditionSymbol() {
		return conditionSymbol;
	}

}
