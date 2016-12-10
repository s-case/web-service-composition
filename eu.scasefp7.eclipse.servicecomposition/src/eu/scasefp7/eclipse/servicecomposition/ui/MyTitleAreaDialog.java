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
	private Text txtdevName;
	private Text txtcompanyName;
	private Text txtdescription;
	private Text txtemail;
	
	private String projectName;
	private String devName;
	private String companyName;
	private String description;
	private String email;
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
		setTitle("Information of the composite web service");
		setMessage("Please enter the following fields for the new web service project", IMessageProvider.INFORMATION);
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

		newShell.setText("Composite Web service project information");
	}

	private void createProjectName(Composite container) {
		// Project Name
		Label lbtFirstName = new Label(container, SWT.NONE);
		lbtFirstName.setText("Project Name");

		GridData dataFirstName = new GridData();
		dataFirstName.grabExcessHorizontalSpace = true;
		dataFirstName.horizontalAlignment = GridData.FILL;

		txtProjectName = new Text(container, SWT.BORDER);
		txtProjectName.setLayoutData(dataFirstName);
		
		//Project Description
		Label lbtDescriptionName = new Label(container, SWT.NONE);
		lbtDescriptionName.setText("Project Description");

		GridData dataDescriptionName = new GridData();
		dataDescriptionName.grabExcessHorizontalSpace = true;
		dataDescriptionName.horizontalAlignment = GridData.FILL;

		txtdescription = new Text(container, SWT.BORDER);
		txtdescription.setLayoutData(dataDescriptionName);
		
		// Developer's Name
		Label lbtDevName = new Label(container, SWT.NONE);
		lbtDevName.setText("Name");

		GridData dataDevName = new GridData();
		dataDevName.grabExcessHorizontalSpace = true;
		dataDevName.horizontalAlignment = GridData.FILL;

		txtdevName = new Text(container, SWT.BORDER);
		txtdevName.setLayoutData(dataDevName);
		
		//email
		Label lbtEmailName = new Label(container, SWT.NONE);
		lbtEmailName.setText("Email");

		GridData dataEmailName = new GridData();
		dataEmailName.grabExcessHorizontalSpace = true;
		dataEmailName.horizontalAlignment = GridData.FILL;

		txtemail = new Text(container, SWT.BORDER);
		txtemail.setLayoutData(dataEmailName);
		
		// Company Name
		Label lbtCompanyName = new Label(container, SWT.NONE);
		lbtCompanyName.setText("Company Name");

		GridData dataCompanyName = new GridData();
		dataCompanyName.grabExcessHorizontalSpace = true;
		dataCompanyName.horizontalAlignment = GridData.FILL;

		txtcompanyName = new Text(container, SWT.BORDER);
		txtcompanyName.setLayoutData(dataCompanyName);

		// Application Domain
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
		setDevName(txtdevName.getText());
		setCompanyName(txtcompanyName.getText());
		setDescription(txtdescription.getText());
		setEmail(txtemail.getText());
		
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

	/**
	 * @return the devName
	 */
	public String getDevName() {
		return devName;
	}

	/**
	 * @param devName the devName to set
	 */
	public void setDevName(String devName) {
		this.devName = devName;
	}

	/**
	 * @return the companyName
	 */
	public String getCompanyName() {
		return companyName;
	}

	/**
	 * @param companyName the companyName to set
	 */
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	
}
