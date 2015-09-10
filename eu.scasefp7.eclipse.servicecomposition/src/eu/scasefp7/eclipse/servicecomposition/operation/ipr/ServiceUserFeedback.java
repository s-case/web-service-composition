package eu.scasefp7.eclipse.servicecomposition.operation.ipr;

public class ServiceUserFeedback {

	private String forCountry = "";
	private int nrOfNegativeRatings;
	private int nrOfPositiveRatings;
	private int totalNumberOfRatings;
	private float averageRating = 0;
	private Vendor vendor;

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
	 * @return the nrOfNegativeRatings
	 */
	public int getNrOfNegativeRatings() {
		return nrOfNegativeRatings;
	}

	/**
	 * @param nrOfNegativeRatings
	 *            the nrOfNegativeRatings to set
	 */
	public void setNrOfNegativeRatings(int nrOfNegativeRatings) {
		this.nrOfNegativeRatings = nrOfNegativeRatings;
	}

	/**
	 * @return the nrOfPositiveRatings
	 */
	public int getNrOfPositiveRatings() {
		return nrOfPositiveRatings;
	}

	/**
	 * @param nrOfPositiveRatings
	 *            the nrOfPositiveRatings to set
	 */
	public void setNrOfPositiveRatings(int nrOfPositiveRatings) {
		this.nrOfPositiveRatings = nrOfPositiveRatings;
	}

	/**
	 * @return the totalNumberOfRatings
	 */
	public int getTotalNumberOfRatings() {
		return totalNumberOfRatings;
	}

	/**
	 * @param totalNumberOfRatings
	 *            the totalNumberOfRatings to set
	 */
	public void setTotalNumberOfRatings(int totalNumberOfRatings) {
		this.totalNumberOfRatings = totalNumberOfRatings;
	}

	/**
	 * @return the averageRating
	 */
	public float getAverageRating() {
		return averageRating;
	}

	/**
	 * @param averageRating
	 *            the averageRating to set
	 */
	public void setAverageRating(float averageRating) {
		this.averageRating = averageRating;
	}

	/**
	 * @return the vendor
	 */
	public Vendor getVendor() {
		return vendor;
	}

	/**
	 * @param vendor
	 *            the vendor to set
	 */
	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public ServiceUserFeedback clone() {
		ServiceUserFeedback userFeedback = new ServiceUserFeedback();
		userFeedback.setAverageRating(this.getAverageRating());
		userFeedback.setForCountry(this.getForCountry());
		userFeedback.setNrOfNegativeRatings(this.getNrOfNegativeRatings());
		userFeedback.setNrOfPositiveRatings(this.getNrOfPositiveRatings());
		userFeedback.setTotalNumberOfRatings(this.getTotalNumberOfRatings());
		// TODO clone vendor
		// userFeedback.setVendor(this.getVendor().clone());
		return userFeedback;
	}

}
