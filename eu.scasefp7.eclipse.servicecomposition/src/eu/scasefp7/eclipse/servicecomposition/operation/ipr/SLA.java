package eu.scasefp7.eclipse.servicecomposition.operation.ipr;

import gr.iti.wsdl.wsdlToolkit.WSOperation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class SLA {

	private String SLA_CostCurrency = "";
	private String SLA_CostPaymentChargeType = "";
	private Date SLA_EndDate;
	private Date SLA_StartDate;
	private String hasStatus = "";
	private int durationInUsages;
	private int durationInUsagesSpent;
	private int durationInUsers;
	private int durationInUsersSpent;
	private int vendorWithTotalTimeOfLoyaltyToOwn;
	private float SLA_Cost;
	private float SLA_CostAfterDiscount;
	private String ownerOfAgreement = "";
	private ServiceLicense serviceLicense = new ServiceLicense();
	private String vendorOfAgreement = "";
	private List<String> validForCountries = new ArrayList<String>();
	private List<WSOperation> accompanyingServices = new ArrayList<WSOperation>();
	private List<DiscountSchema> discountSchema = new ArrayList<DiscountSchema>();
	private String ontologyURI = "";

	public String getOntologyURI() {
		return ontologyURI;
	}

	public void setOntologyURI(String ontologyURI) {
		this.ontologyURI = ontologyURI;
	}

	/**
	 * @return the sLA_CostCurrency
	 */
	public String getSLA_CostCurrency() {
		return SLA_CostCurrency;
	}

	/**
	 * @param sLA_CostCurrency
	 *            the sLA_CostCurrency to set
	 */
	public void setSLA_CostCurrency(String sLA_CostCurrency) {
		SLA_CostCurrency = sLA_CostCurrency;
	}

	/**
	 * @return the sLA_CostPaymentChargeType
	 */
	public String getSLA_CostPaymentChargeType() {
		return SLA_CostPaymentChargeType;
	}

	/**
	 * @param sLA_CostPaymentChargeType
	 *            the sLA_CostPaymentChargeType to set
	 */
	public void setSLA_CostPaymentChargeType(String sLA_CostPaymentChargeType) {
		SLA_CostPaymentChargeType = sLA_CostPaymentChargeType;
	}

	/**
	 * @return the sLA_EndDate
	 */
	public Date getSLA_EndDate() {
		return SLA_EndDate;
	}

	/**
	 * @param sLA_EndDate
	 *            the sLA_EndDate to set
	 */
	public void setSLA_EndDate(Date sLA_EndDate) {
		SLA_EndDate = sLA_EndDate;
	}

	/**
	 * @return the sLA_StartDate
	 */
	public Date getSLA_StartDate() {
		return SLA_StartDate;
	}

	/**
	 * @param sLA_StartDate
	 *            the sLA_StartDate to set
	 */
	public void setSLA_StartDate(Date sLA_StartDate) {
		SLA_StartDate = sLA_StartDate;
	}

	/**
	 * @return the hasStatus
	 */
	public String getHasStatus() {
		return hasStatus;
	}

	/**
	 * @param hasStatus
	 *            the hasStatus to set
	 */
	public void setHasStatus(String hasStatus) {
		this.hasStatus = hasStatus;
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
	 * @return the durationInUsers
	 */
	public int getDurationInUsers() {
		return durationInUsers;
	}

	/**
	 * @param durationInUsers
	 *            the durationInUsers to set
	 */
	public void setDurationInUsers(int durationInUsers) {
		this.durationInUsers = durationInUsers;
	}

	/**
	 * @return the durationInUsersSpent
	 */
	public int getDurationInUsersSpent() {
		return durationInUsersSpent;
	}

	/**
	 * @param durationInUsersSpent
	 *            the durationInUsersSpent to set
	 */
	public void setDurationInUsersSpent(int durationInUsersSpent) {
		this.durationInUsersSpent = durationInUsersSpent;
	}

	/**
	 * @return the vendorWithTotalTimeOfLoyaltyToOwn
	 */
	public int getVendorWithTotalTimeOfLoyaltyToOwn() {
		return vendorWithTotalTimeOfLoyaltyToOwn;
	}

	/**
	 * @param vendorWithTotalTimeOfLoyaltyToOwn
	 *            the vendorWithTotalTimeOfLoyaltyToOwn to set
	 */
	public void setVendorWithTotalTimeOfLoyaltyToOwn(
			int vendorWithTotalTimeOfLoyaltyToOwn) {
		this.vendorWithTotalTimeOfLoyaltyToOwn = vendorWithTotalTimeOfLoyaltyToOwn;
	}

	/**
	 * @return the sLA_Cost
	 */
	public float getSLA_Cost() {
		return SLA_Cost;
	}

	/**
	 * @param sLA_Cost
	 *            the sLA_Cost to set
	 */
	public void setSLA_Cost(float sLA_Cost) {
		SLA_Cost = sLA_Cost;
	}

	/**
	 * @return the sLA_CostAfterDiscount
	 */
	public float getSLA_CostAfterDiscount() {
		return SLA_CostAfterDiscount;
	}

	/**
	 * @param sLA_CostAfterDiscount
	 *            the sLA_CostAfterDiscount to set
	 */
	public void setSLA_CostAfterDiscount(float sLA_CostAfterDiscount) {
		SLA_CostAfterDiscount = sLA_CostAfterDiscount;
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

	public List<WSOperation> getAccompanyingServices() {
		return accompanyingServices;
	}

	public void setAccompanyingServices(List<WSOperation> accompanyingServices) {
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

	public String getOwnerOfAgreement() {
		return ownerOfAgreement;
	}

	public void setOwnerOfAgreement(String ownerOfAgreement) {
		this.ownerOfAgreement = ownerOfAgreement;
	}

	public String getVendorOfAgreement() {
		return vendorOfAgreement;
	}

	public void setVendorOfAgreement(String vendorOfAgreement) {
		this.vendorOfAgreement = vendorOfAgreement;
	}

	public SLA clone() {
		SLA sla = new SLA();
		sla.setSLA_CostCurrency(this.getSLA_CostCurrency());
		sla.setSLA_CostPaymentChargeType(this.getSLA_CostPaymentChargeType());
		if (this.getSLA_StartDate() != null)
			sla.setSLA_StartDate((Date) this.getSLA_StartDate().clone());
		if (this.getSLA_EndDate() != null)
			sla.setSLA_EndDate((Date) this.getSLA_EndDate().clone());
		sla.setHasStatus(this.getHasStatus());
		sla.setDurationInUsages(this.getDurationInUsages());
		sla.setDurationInUsagesSpent(this.getDurationInUsagesSpent());
		sla.setDurationInUsers(this.getDurationInUsers());
		sla.setDurationInUsagesSpent(this.getDurationInUsersSpent());
		sla.setVendorWithTotalTimeOfLoyaltyToOwn(this
				.getVendorWithTotalTimeOfLoyaltyToOwn());
		sla.setSLA_Cost(this.getSLA_Cost());
		sla.setSLA_CostAfterDiscount(this.getSLA_CostAfterDiscount());

		sla.setOwnerOfAgreement(this.getOwnerOfAgreement());
		sla.setServiceLicense(this.getServiceLicense().clone());
		sla.setOntologyURI(this.getOntologyURI());
		sla.setVendorOfAgreement(this.getVendorOfAgreement());

		for (int i = 0; i < this.getValidForCountries().size(); i++) {
			sla.getValidForCountries().add(this.getValidForCountries().get(i));
		}

		// TODO accompanyingSolutions are not cloned

		for (int i = 0; i < this.getDiscountSchema().size(); i++) {
			sla.getDiscountSchema()
					.add(this.getDiscountSchema().get(i).clone());
		}

		return sla;
	}

}
