package eu.scasefp7.eclipse.servicecomposition.operation.ipr;

public class ServiceTrialSchema {
	//trial period of the service characteristics
	private String limitedFunctionalityDescription = "";
	private boolean offersFullFunctionalityDuringTrial;
	private int durationInDays;
	private int durationInUsages;

	/**
	 * @return the limitedFunctionalityDescription
	 */
	public String getLimitedFunctionalityDescription() {
		return limitedFunctionalityDescription;
	}

	/**
	 * @param limitedFunctionalityDescription
	 *            the limitedFunctionalityDescription to set
	 */
	public void setLimitedFunctionalityDescription(
			String limitedFunctionalityDescription) {
		this.limitedFunctionalityDescription = limitedFunctionalityDescription;
	}

	/**
	 * @return the offersFullFunctionalityDuringTrial
	 */
	public boolean isOffersFullFunctionalityDuringTrial() {
		return offersFullFunctionalityDuringTrial;
	}

	/**
	 * @param offersFullFunctionalityDuringTrial
	 *            the offersFullFunctionalityDuringTrial to set
	 */
	public void setOffersFullFunctionalityDuringTrial(
			boolean offersFullFunctionalityDuringTrial) {
		this.offersFullFunctionalityDuringTrial = offersFullFunctionalityDuringTrial;
	}

	/**
	 * @return the durationInDays
	 */
	public int getDurationInDays() {
		return durationInDays;
	}

	/**
	 * @param durationInDays
	 *            the durationInDays to set
	 */
	public void setDurationInDays(int durationInDays) {
		this.durationInDays = durationInDays;
	}

	/**
	 * @return the durationInUsages
	 */
	public int getDurationInUsages() {
		return durationInUsages;
	}

	/**
	 * @param durationInUsages
	 *            the durationInUsages to set
	 */
	public void setDurationInUsages(int durationInUsages) {
		this.durationInUsages = durationInUsages;
	}

	public ServiceTrialSchema clone() {
		ServiceTrialSchema schema = new ServiceTrialSchema();
		schema.setDurationInDays(this.getDurationInDays());
		schema.setDurationInUsages(this.getDurationInUsages());
		schema.setLimitedFunctionalityDescription(this
				.getLimitedFunctionalityDescription());
		schema.setOffersFullFunctionalityDuringTrial(this
				.isOffersFullFunctionalityDuringTrial());
		return schema;
	}

}
