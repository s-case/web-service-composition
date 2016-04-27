package eu.scasefp7.eclipse.servicecomposition.ui;

import java.util.List;

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

import eu.scasefp7.eclipse.servicecomposition.repository.ApplicationDomain;
import eu.scasefp7.eclipse.servicecomposition.repository.WSOntology;

public class MyTitleAreaDialog extends TitleAreaDialog {

	private Text txtProjectName;
	private String projectName;
	// the application domain of a sc
	private String applicationDomainURI = "";
	private Combo doms;
	List<ApplicationDomain> domains;

	public MyTitleAreaDialog(Shell parentShell) {
		super(parentShell);
		WSOntology ws = new WSOntology();
		domains = ws.getAllDomainForMenu();
	}

	@Override
	public void create() {
		super.create();
		setTitle("Name and application domain of the composite web service");
		setMessage("Please enter a name and select an application domain for the new web service project", IMessageProvider.INFORMATION);
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

		newShell.setText("Web service project name and application domain");
	}

	private void createProjectName(Composite container) {
		Label lbtFirstName = new Label(container, SWT.NONE);
		lbtFirstName.setText("Project Name");

		GridData dataFirstName = new GridData();
		dataFirstName.grabExcessHorizontalSpace = true;
		dataFirstName.horizontalAlignment = GridData.FILL;

		txtProjectName = new Text(container, SWT.BORDER);
		txtProjectName.setLayoutData(dataFirstName);

		Label applicationDomainLabel = new Label(container, SWT.NONE);
		applicationDomainLabel.setText("Applcation Domain");

		doms = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.LEFT);
		
		for (int i = 0; i < domains.size(); i++)
			doms.add(domains.get(i).toString());
		doms.select(0);
		
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
		projectName = txtProjectName.getText();
		applicationDomainURI = domains.get(doms.getSelectionIndex()).getUri();

	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	public String getProjectName() {
		return projectName;
	}

	public String getApplicationDomainURI() {
		return applicationDomainURI;
	}

	
}
