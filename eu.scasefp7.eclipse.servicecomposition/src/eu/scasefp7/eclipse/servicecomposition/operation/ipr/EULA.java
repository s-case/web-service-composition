package eu.scasefp7.eclipse.servicecomposition.operation.ipr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class EULA {
	//cost, usage duration and discount aspects of the service
	private String EULA_costCurrency = "";
	private String EULA_costPaymentChargeType = "";
	private Date EULA_EndDate;
	private Date EULA_StartDate;
	private String status = "";
	private int durationInUsages;
	private int durationInUsagesSpent;
	private int userWithTotalTimeOfLoyaltyToVendorInDays;
	private float EULA_cost;
	private float EULA_costAfterDiscount;
	private ServiceLicense serviceLicense = new ServiceLicense();
	private Vendor vendorOfAgreement;
	private List<String> validForCountries = new ArrayList<String>();
	private List<String> accompanyingServices = new ArrayList<String>();
	private List<DiscountSchema> discountSchema = new ArrayList<DiscountSchema>();
	private String ontologyURI = "";
	private String user = "";
	private String refersToSolution = "";

	/**
	 * @return the eULA_costCurrency
	 */
	public String getEULA_costCurrency() {
		return EULA_costCurrency;
	}

	/**
	 * @param eULA_costCurrency
	 *            the eULA_costCurrency to set
	 */
	public void setEULA_costCurrency(String eULA_costCurrency) {
		EULA_costCurrency = eULA_costCurrency;
	}

	/**
	 * @return the eULA_costPaymentChargeType
	 */
	public String getEULA_costPaymentChargeType() {
		return EULA_costPaymentChargeType;
	}

	/**
	 * @param eULA_costPaymentChargeType
	 *            the eULA_costPaymentChargeType to set
	 */
	public void setEULA_costPaymentChargeType(String eULA_costPaymentChargeType) {
		EULA_costPaymentChargeType = eULA_costPaymentChargeType;
	}

	/**
	 * @return the eULA_EndDate
	 */
	public Date getEULA_EndDate() {
		return EULA_EndDate;
	}

	/**
	 * @param eULA_EndDate
	 *            the eULA_EndDate to set
	 */
	public void setEULA_EndDate(Date eULA_EndDate) {
		EULA_EndDate = eULA_EndDate;
	}

	/**
	 * @return the eULA_StartDate
	 */
	public Date getEULA_StartDate() {
		return EULA_StartDate;
	}

	/**
	 * @param eULA_StartDate
	 *            the eULA_StartDate to set
	 */
	public void setEULA_StartDate(Date eULA_StartDate) {
		EULA_StartDate = eULA_StartDate;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
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

	/**
	 * @return the durationInUsagesSpent
	 */
	public int getDurationInUsagesSpent() {
		return durationInUsagesSpent;
	}

	/**
	 * @param durationInUsagesSpent
	 *            the durationInUsagesSpent to set
	 */
	public void setDurationInUsagesSpent(int durationInUsagesSpent) {
		this.durationInUsagesSpent = durationInUsagesSpent;
	}

	/**
	 * @return the userWithTotalTimeOfLoyaltyToVendorInDays
	 */
	public int getUserWithTotalTimeOfLoyaltyToVendorInDays() {
		return userWithTotalTimeOfLoyaltyToVendorInDays;
	}

	/**
	 * @param userWithTotalTimeOfLoyaltyToVendorInDays
	 *            the userWithTotalTimeOfLoyaltyToVendorInDays to set
	 */
	public void setUserWithTotalTimeOfLoyaltyToVendorInDays(
			int userWithTotalTimeOfLoyaltyToVendorInDays) {
		this.userWithTotalTimeOfLoyaltyToVendorInDays = userWithTotalTimeOfLoyaltyToVendorInDays;
	}

	/**
	 * @return the eULA_cost
	 */
	public float getEULA_cost() {
		return EULA_cost;
	}

	/**
	 * @param eULA_cost
	 *            the eULA_cost to set
	 */
	public void setEULA_cost(float eULA_cost) {
		EULA_cost = eULA_cost;
	}

	/**
	 * @return the eULA_costAfterDiscount
	 */
	public float getEULA_costAfterDiscount() {
		return EULA_costAfterDiscount;
	}

	/**
	 * @param eULA_costAfterDiscount
	 *            the eULA_costAfterDiscount to set
	 */
	public void setEULA_costAfterDiscount(float eULA_costAfterDiscount) {
		EULA_costAfterDiscount = eULA_costAfterDiscount;
	}

	/**
	 * @return the vendorOfAgreement
	 */
	public Vendor getVendorOfAgreement() {
		return vendorOfAgreement;
	}

	/**
	 * @param vendorOfAgreement
	 *            the vendorOfAgreement to set
	 */
	public void setVendorOfAgreement(Vendor vendorOfAgreement) {
		this.vendorOfAgreement = vendorOfAgreement;
	}

	/**
	 * @return the validForCountries
	 */
	public List<String> getValidForCountries() {
		return validForCountries;
	}

	/**
	 * @param validForCountries
	 *            the validForCountries to set
	 */
	public void setValidForCountries(List<String> validForCountries) {
		this.validForCountries = validForCountries;
	}

	

	public ServiceLicense getServiceLicense() {
		return serviceLicense;
	}

	public void setServiceLicense(ServiceLicense serviceLicense) {
		this.serviceLicense = serviceLicense;
	}

	public List<String> getAccompanyingServices() {
		return accompanyingServices;
	}

	public void setAccompanyingServices(List<String> accompanyingServices) {
		this.accompanyingServices = accompanyingServices;
	}

	/**
	 * @return the discountSchema
	 */
	public List<DiscountSchema> getDiscountSchema() {
		return discountSchema;
	}

	/**
	 * @param discountSchema
	 *            the discountSchema to set
	 */
	public void setDiscountSchema(List<DiscountSchema> discountSchema) {
		this.discountSchema = discountSchema;
	}

	public EULA clone() {
		EULA eula = new EULA();
		eula.setEULA_costCurrency(this.getEULA_costCurrency());
		eula.setEULA_costPaymentChargeType(this.getEULA_costPaymentChargeType());
		eula.setEULA_StartDate(this.getEULA_StartDate());
		eula.setStatus(this.getStatus());
		eula.setDurationInUsages(this.getDurationInUsages());
		eula.setDurationInUsagesSpent(this.getDurationInUsagesSpent());
		eula.setUserWithTotalTimeOfLoyaltyToVendorInDays(this
				.getUserWithTotalTimeOfLoyaltyToVendorInDays());
		eula.setEULA_cost(this.getEULA_cost());
		eula.setEULA_costAfterDiscount(this.getEULA_costAfterDiscount());
		eula.setServiceLicense(this.getServiceLicense().clone());
		eula.setOntologyURI(this.getOntologyURI());
		eula.setRefersToSolution(this.getRefersToSolution());
		eula.setUser(this.getUser());
		// TODO clone vendor of agreement
		for (int i = 0; i < this.getValidForCountries().size(); i++) {
			eula.getValidForCountries().add(this.getValidForCountries().get(i));
		}

		for (int i = 0; i < this.getAccompanyingServices().size(); i++) {
			eula.getAccompanyingServices().add(
					this.getAccompanyingServices().get(i));
		}
		for (int i = 0; i < this.getDiscountSchema().size(); i++) {
			eula.getDiscountSchema().add(this.getDiscountSchema().get(i));
		}

		return eula;

	}

	public String getOntologyURI() {
		return ontologyURI;
	}

	public void setOntologyURI(String ontologyURI) {
		this.ontologyURI = ontologyURI;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getRefersToSolution() {
		return refersToSolution;
	}

	public void setRefersToSolution(String refersToSolution) {
		this.refersToSolution = refersToSolution;
	}

}
