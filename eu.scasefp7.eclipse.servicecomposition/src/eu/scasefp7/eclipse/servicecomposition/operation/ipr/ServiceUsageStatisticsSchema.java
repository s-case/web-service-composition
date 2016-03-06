package eu.scasefp7.eclipse.servicecomposition.operation.ipr;

import java.util.ArrayList;
import java.util.List;

public class ServiceUsageStatisticsSchema {
	//statistics
	private int totalNrOfSuccessfulUsages;
	private int totalNrOfUnsuccessfulUsages;
	private int totalNrOfUsages;
	private int totalNrOfUsers;
	private float usagePercentageInCategory = 0;
	private ServiceUserFeedback solutionUserFeedback = new ServiceUserFeedback();
	private List<PerCountrySolutionUsage> usagePerCountry = new ArrayList<PerCountrySolutionUsage>();
	private List<PerVendorSolutionUsage> usagePerVendor = new ArrayList<PerVendorSolutionUsage>();
	private List<ServiceUserFeedback> userFeedbackPerCountry = new ArrayList<ServiceUserFeedback>();
	private List<ServiceUserFeedback> userFeedbackPerVendor = new ArrayList<ServiceUserFeedback>();

	/**
	 * @return the totalNrOfSuccessfulUsages
	 */
	public int getTotalNrOfSuccessfulUsages() {
		return totalNrOfSuccessfulUsages;
	}

	/**
	 * @param totalNrOfSuccessfulUsages
	 *            the totalNrOfSuccessfulUsages to set
	 */
	public void setTotalNrOfSuccessfulUsages(int totalNrOfSuccessfulUsages) {
		this.totalNrOfSuccessfulUsages = totalNrOfSuccessfulUsages;
	}

	/**
	 * @return the totalNrOfUnsuccessfulUsages
	 */
	public int getTotalNrOfUnsuccessfulUsages() {
		return totalNrOfUnsuccessfulUsages;
	}

	/**
	 * @param totalNrOfUnsuccessfulUsages
	 *            the totalNrOfUnsuccessfulUsages to set
	 */
	public void setTotalNrOfUnsuccessfulUsages(int totalNrOfUnsuccessfulUsages) {
		this.totalNrOfUnsuccessfulUsages = totalNrOfUnsuccessfulUsages;
	}

	/**
	 * @return the totalNrOfUsages
	 */
	public int getTotalNrOfUsages() {
		return totalNrOfUsages;
	}

	/**
	 * @param totalNrOfUsages
	 *            the totalNrOfUsages to set
	 */
	public void setTotalNrOfUsages(int totalNrOfUsages) {
		this.totalNrOfUsages = totalNrOfUsages;
	}

	/**
	 * @return the totalNrOfUsers
	 */
	public int getTotalNrOfUsers() {
		return totalNrOfUsers;
	}

	/**
	 * @param totalNrOfUsers
	 *            the totalNrOfUsers to set
	 */
	public void setTotalNrOfUsers(int totalNrOfUsers) {
		this.totalNrOfUsers = totalNrOfUsers;
	}

	public float getUsagePercentageInCategory() {
		return usagePercentageInCategory;
	}

	public void setUsagePercentageInCategory(float usagePercentageInCategory) {
		this.usagePercentageInCategory = usagePercentageInCategory;
	}

	/**
	 * @return the solutionUserFeedback
	 */
	public ServiceUserFeedback getSolutionUserFeedback() {
		return solutionUserFeedback;
	}

	/**
	 * @param solutionUserFeedback
	 *            the solutionUserFeedback to set
	 */
	public void setSolutionUserFeedback(
			ServiceUserFeedback solutionUserFeedback) {
		this.solutionUserFeedback = solutionUserFeedback;
	}

	/**
	 * @return the usagePerCountry
	 */
	public List<PerCountrySolutionUsage> getUsagePerCountry() {
		return usagePerCountry;
	}

	/**
	 * @param usagePerCountry
	 *            the usagePerCountry to set
	 */
	public void setUsagePerCountry(List<PerCountrySolutionUsage> usagePerCountry) {
		this.usagePerCountry = usagePerCountry;
	}

	/**
	 * @return the usagePerVendor
	 */
	public List<PerVendorSolutionUsage> getUsagePerVendor() {
		return usagePerVendor;
	}

	/**
	 * @param usagePerVendor
	 *            the usagePerVendor to set
	 */
	public void setUsagePerVendor(List<PerVendorSolutionUsage> usagePerVendor) {
		this.usagePerVendor = usagePerVendor;
	}

	/**
	 * @return the userFeedbackPerCountry
	 */
	public List<ServiceUserFeedback> getUserFeedbackPerCountry() {
		return userFeedbackPerCountry;
	}

	/**
	 * @param userFeedbackPerCountry
	 *            the userFeedbackPerCountry to set
	 */
	public void setUserFeedbackPerCountry(
			List<ServiceUserFeedback> userFeedbackPerCountry) {
		this.userFeedbackPerCountry = userFeedbackPerCountry;
	}

	/**
	 * @return the userFeedbackPerVendor
	 */
	public List<ServiceUserFeedback> getUserFeedbackPerVendor() {
		return userFeedbackPerVendor;
	}

	/**
	 * @param userFeedbackPerVendor
	 *            the userFeedbackPerVendor to set
	 */
	public void setUserFeedbackPerVendor(
			List<ServiceUserFeedback> userFeedbackPerVendor) {
		this.userFeedbackPerVendor = userFeedbackPerVendor;
	}

	public ServiceUsageStatisticsSchema clone() {
		ServiceUsageStatisticsSchema usage = new ServiceUsageStatisticsSchema();
		usage.setTotalNrOfSuccessfulUsages(this.getTotalNrOfSuccessfulUsages());
		usage.setTotalNrOfUnsuccessfulUsages(this
				.getTotalNrOfUnsuccessfulUsages());
		usage.setTotalNrOfUsages(this.getTotalNrOfUsages());
		usage.setTotalNrOfUsers(this.getTotalNrOfUsers());
		usage.setUsagePercentageInCategory(this.getUsagePercentageInCategory());
		usage.setSolutionUserFeedback(this.getSolutionUserFeedback().clone());
		for (int i = 0; i < this.getUsagePerCountry().size(); i++) {
			usage.getUsagePerCountry().add(
					this.getUsagePerCountry().get(i).clone());
		}
		for (int i = 0; i < this.getUsagePerVendor().size(); i++) {
			usage.getUsagePerVendor().add(
					this.getUsagePerVendor().get(i).clone());
		}
		for (int i = 0; i < this.getUserFeedbackPerCountry().size(); i++) {
			usage.getUserFeedbackPerCountry().add(
					this.getUserFeedbackPerCountry().get(i).clone());
		}
		for (int i = 0; i < this.getUserFeedbackPerVendor().size(); i++) {
			usage.getUserFeedbackPerVendor().add(
					this.getUserFeedbackPerVendor().get(i).clone());
		}

		return usage;
	}
}
