package eu.scasefp7.eclipse.servicecomposition.operation.ipr;

public class PerCountrySolutionUsage {
	//statistics of successful usages of the service per country
	private String forCountry = "";
	private int nrOfSuccessfullUsages;
	private int nrOfUnsuccessfulUsages;
	private int nrOfUsages;
	private int nrOfUsers;

	/**
	 * @return the forCountry
	 */
	public String getForCountry() {
		return forCountry;
	}

	/**
	 * @param forCountry
	 *            the forCountry to set
	 */
	public void setForCountry(String forCountry) {
		this.forCountry = forCountry;
	}

	/**
	 * @return the nrOfSuccessfullUsages
	 */
	public int getNrOfSuccessfullUsages() {
		return nrOfSuccessfullUsages;
	}

	/**
	 * @param nrOfSuccessfullUsages
	 *            the nrOfSuccessfullUsages to set
	 */
	public void setNrOfSuccessfullUsages(int nrOfSuccessfullUsages) {
		this.nrOfSuccessfullUsages = nrOfSuccessfullUsages;
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

	public PerCountrySolutionUsage clone() {
		PerCountrySolutionUsage countrySolutionUsage = new PerCountrySolutionUsage();
		countrySolutionUsage.setForCountry(this.getForCountry());
		countrySolutionUsage.setNrOfSuccessfullUsages(this
				.getNrOfSuccessfullUsages());
		countrySolutionUsage.setNrOfUnsuccessfulUsages(this
				.getNrOfUnsuccessfulUsages());
		countrySolutionUsage.setNrOfUsages(this.getNrOfUsages());
		countrySolutionUsage.setNrOfUsers(this.getNrOfUsers());
		return countrySolutionUsage;
	}

}
