package eu.scasefp7.eclipse.servicecomposition.operation.ipr;

public class PerVendorSolutionUsage {
	//statistics of successful usages of the service per vendor
	private int nrOfSuccessfulUsages;
	private int nrOfUnsuccessfulUsages;
	private int nrOfUsages;
	private int nrOfUsers;
	private int forVendor;

	/**
	 * @return the nrOfSuccessfulUsages
	 */
	public int getNrOfSuccessfulUsages() {
		return nrOfSuccessfulUsages;
	}

	/**
	 * @param nrOfSuccessfulUsages
	 *            the nrOfSuccessfulUsages to set
	 */
	public void setNrOfSuccessfulUsages(int nrOfSuccessfulUsages) {
		this.nrOfSuccessfulUsages = nrOfSuccessfulUsages;
	}

	/**
	 * @return the nrOfUnsuccessfulUsages
	 */
	public int getNrOfUnsuccessfulUsages() {
		return nrOfUnsuccessfulUsages;
	}

	/**
	 * @param nrOfUnsuccessfulUsages
	 *            the nrOfUnsuccessfulUsages to set
	 */
	public void setNrOfUnsuccessfulUsages(int nrOfUnsuccessfulUsages) {
		this.nrOfUnsuccessfulUsages = nrOfUnsuccessfulUsages;
	}

	/**
	 * @return the nrOfUsages
	 */
	public int getNrOfUsages() {
		return nrOfUsages;
	}

	/**
	 * @param nrOfUsages
	 *            the nrOfUsages to set
	 */
	public void setNrOfUsages(int nrOfUsages) {
		this.nrOfUsages = nrOfUsages;
	}

	/**
	 * @return the nrOfUsers
	 */
	public int getNrOfUsers() {
		return nrOfUsers;
	}

	/**
	 * @param nrOfUsers
	 *            the nrOfUsers to set
	 */
	public void setNrOfUsers(int nrOfUsers) {
		this.nrOfUsers = nrOfUsers;
	}

	/**
	 * @return the forVendor
	 */
	public int getForVendor() {
		return forVendor;
	}

	/**
	 * @param forVendor
	 *            the forVendor to set
	 */
	public void setForVendor(int forVendor) {
		this.forVendor = forVendor;
	}

	public PerVendorSolutionUsage clone() {
		PerVendorSolutionUsage perVendorSolutionUsage = new PerVendorSolutionUsage();
		perVendorSolutionUsage.setForVendor(this.getForVendor());
		perVendorSolutionUsage.setNrOfSuccessfulUsages(this
				.getNrOfSuccessfulUsages());
		perVendorSolutionUsage.setNrOfUnsuccessfulUsages(this
				.getNrOfUnsuccessfulUsages());
		perVendorSolutionUsage.setNrOfUsages(this.getNrOfUsages());
		perVendorSolutionUsage.setNrOfUsers(this.getNrOfUsers());
		return perVendorSolutionUsage;
	}

}
