package eu.scasefp7.eclipse.servicecomposition.ui;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for renaming an edge coming out of a condition.
 * 
 * @author mkoutli
 *
 */
public class RenameEdgeConditionDialog extends TitleAreaDialog {
	private Text txtConditionName;
	private String conditionName;

	public RenameEdgeConditionDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		setTitle("Name of the condition");
		setMessage("Please enter a name for the condition edge", IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);

		createProjectName(container);

		return area;
	}

	@Override
	public void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText("Condition edge name");
	}

	private void createProjectName(Composite container) {
		Label lbtFirstName = new Label(container, SWT.NONE);
		lbtFirstName.setText("Condition Edge Name");

		GridData dataFirstName = new GridData();
		dataFirstName.grabExcessHorizontalSpace = true;
		dataFirstName.horizontalAlignment = GridData.FILL;

		txtConditionName = new Text(container, SWT.BORDER);
		txtConditionName.setLayoutData(dataFirstName);
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

	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	public String getConditionName() {
		return conditionName;
	}

}
