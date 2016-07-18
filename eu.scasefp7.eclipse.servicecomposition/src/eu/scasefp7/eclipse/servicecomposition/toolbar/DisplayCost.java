package eu.scasefp7.eclipse.servicecomposition.toolbar;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.tester.Algorithm.costReport;
import eu.scasefp7.eclipse.servicecomposition.tester.Algorithm.licenseReport;
import eu.scasefp7.eclipse.servicecomposition.tester.Algorithm.trialReport;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.views.ServiceCompositionView;

public class DisplayCost {

	/**
	 * <h1>displayCost</h1>Method for displaying a dialog to the user with
	 * information concerning the total cost, trial period and licenses required
	 * for the workflow.
	 */
	public static void displayCost(ServiceCompositionView view) {
		costReport costPerUsage = new costReport();
		costReport costPerMonth = new costReport();
		costReport costPerYear = new costReport();
		costReport costUnlimited = new costReport();
		trialReport workflowTrialPeriod = new trialReport();
		licenseReport workflowLicense = new licenseReport();

		
		
		for (OwlService service : view.getJungGraph().getVertices()) {
			if (service.getOperation() != null) {

				// Calculate total cost

				String currency = ((Operation) service.getContent()).getAccessInfo().getCommercialCostSchema()
						.getCommercialCostCurrency();
				String chargeType = ((Operation) service.getContent()).getAccessInfo().getCommercialCostSchema()
						.getCostPaymentChargeType();

				switch (chargeType) {
				case "per usage":
					costPerUsage.calculateCost(currency, service);
					break;
				case "per month":
					costPerMonth.calculateCost(currency, service);
					break;
				case "per year":
					costPerYear.calculateCost(currency, service);
					break;
				case "unlimited":
					costUnlimited.calculateCost(currency, service);
					break;
				default:
					break;
				}

				// trial period
				int durationInUsages = ((Operation) service.getContent()).getAccessInfo().getCommercialCostSchema()
						.getTrialSchema().getDurationInUsages();
				int durationInDays = ((Operation) service.getContent()).getAccessInfo().getCommercialCostSchema()
						.getTrialSchema().getDurationInDays();
				workflowTrialPeriod.setTrialReport(durationInDays, durationInUsages);

				// License
				String licenseName = ((Operation) service.getContent()).getAccessInfo().getLicense().getLicenseName();
				workflowLicense.setLicenseReport(licenseName);
			}
		}

		// Calculate and print total cost
		String perUsage = costPerUsage.calculateWorkflowCost(" per usage");
		String perMonth = costPerMonth.calculateWorkflowCost(" per month");
		if (perMonth != "") {
			perMonth = " + " + perMonth;
		}
		String perYear = costPerYear.calculateWorkflowCost(" per year");
		if (perYear != "") {
			perYear = " + " + perYear;
		}
		String unlimited = costUnlimited.calculateWorkflowCost(" unlimited");
		if (unlimited != "") {
			unlimited = " + " + unlimited;
		}
		String ret = "Total workflow cost: " + perUsage + perMonth + perYear + unlimited;
		System.out.println(ret);

		// Calculate and print trial period
		String ret2 = "Total trial period: ";
		int minDays = workflowTrialPeriod.findDurationInDays();
		int minUsages = workflowTrialPeriod.findDurationInUsages();
		if (minDays != Integer.MAX_VALUE && minUsages != Integer.MAX_VALUE) {
			ret2 = ret2 + minDays + " days or " + minUsages + " usages.";
		} else if (minUsages != Integer.MAX_VALUE) {
			ret2 = ret2 + minUsages + " usages.";
		} else if (minDays != Integer.MAX_VALUE) {
			ret2 = ret2 + minDays + " days";
		} else {
			ret2 = ret2 + " unlimited.";
		}
		ret = ret + "\n" + ret2;
		System.out.println(ret2);

		// Total Licenses
		List<String> licenseNames = workflowLicense.getLicenseReport();
		if (!licenseNames.isEmpty()) {
			String ret3 = "Licenses: ";
			for (String licenceName : licenseNames) {
				ret3 = ret3 + licenceName + " ";
			}
			System.out.println(ret3);
			ret = ret + "\n" + ret3;
		}

		final String message = ret;
		final Shell shell = view.getSite().getWorkbenchWindow().getShell();
		final Display disp = shell.getDisplay();
		disp.syncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openInformation(disp.getActiveShell(), "Cost, Trial, License Information.", message);
			}
		});
	}
	
}
