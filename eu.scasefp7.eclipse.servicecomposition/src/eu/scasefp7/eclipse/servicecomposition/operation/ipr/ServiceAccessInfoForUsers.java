package eu.scasefp7.eclipse.servicecomposition.operation.ipr;

import java.util.ArrayList;
import java.util.List;

public class ServiceAccessInfoForUsers {
	//all non-functional characteristics of the service
	private List<Description> description = new ArrayList<Description>();
	private String URLForAccess = "";
	private float maxDiscount=0;
	private CommercialCostSchema commercialCostSchema = new CommercialCostSchema();
	private ServiceLicense license = new ServiceLicense();
	private Vendor serviceVendor = new Vendor();
	private List<Country> validForCountries = new ArrayList<Country>();

	
	
	@Override
	public String toString() {
		String ret = "";
		if (!commercialCostSchema.getCommercialCostCurrency().isEmpty()){
		ret="(cost: "+commercialCostSchema.getCommercialCost()+ " "+commercialCostSchema.getCommercialCostCurrency()+ " " + commercialCostSchema.getCostPaymentChargeType()+")";
		}else if (commercialCostSchema.getCommercialCost()==0.0 && commercialCostSchema.getCommercialCostCurrency().isEmpty()){
			ret="(cost: "+commercialCostSchema.getCommercialCost()+"EUR)";
		}
		return ret;
		
	}
	
	/**
	 * @return the description
	 */
	public List<Description> getDescription() {
		return description;
	}

	/**
	 * @param isValidForCountriesList
	 *            the description to set
	 */
	public void setDescription(List<Description> isValidForCountriesList) {
		this.description = isValidForCountriesList;
	}

	/**
	 * @return the uRLForAccess
	 */
	public String getURLForAccess() {
		return URLForAccess;
	}

	/**
	 * @param uRLForAccess
	 *            the uRLForAccess to set
	 */
	public void setURLForAccess(String uRLForAccess) {
		URLForAccess = uRLForAccess;
	}

	/**
	 * @return the maxDiscount
	 */
	public float getMaxDiscount() {
		return maxDiscount;
	}

	/**
	 * @param maxDiscount
	 *            the maxDiscount to set
	 */
	public void setMaxDiscount(float maxDiscount) {
		this.maxDiscount = maxDiscount;
	}

	/**
	 * @return the commercialCostSchema
	 */
	public CommercialCostSchema getCommercialCostSchema() {
		return commercialCostSchema;
	}

	/**
	 * @param commercialCostSchema
	 *            the commercialCostSchema to set
	 */
	public void setCommercialCostSchema(
			CommercialCostSchema commercialCostSchema) {
		this.commercialCostSchema = commercialCostSchema;
	}

	/**
	 * @return the license
	 */
	public ServiceLicense getLicense() {
		return license;
	}

	/**
	 * @param license
	 *            the license to set
	 */
	public void setLicense(ServiceLicense license) {
		this.license = license;
	}

	

	public Vendor getServiceVendor() {
		return serviceVendor;
	}

	public void setServiceVendor(Vendor serviceVendor) {
		this.serviceVendor = serviceVendor;
	}

	/**
	 * @return the validForCountries
	 */
	public List<Country> getValidForCountries() {
		return validForCountries;
	}

	/**
	 * @param validForCountries
	 *            the validForCountries to set
	 */
	public void setValidForCountries(List<Country> validForCountries) {
		this.validForCountries = validForCountries;
	}

	public ServiceAccessInfoForUsers clone() {
		ServiceAccessInfoForUsers access = new ServiceAccessInfoForUsers();
		access.setDescription(this.getDescription());
		access.setMaxDiscount(this.getMaxDiscount());
		access.setURLForAccess(this.getURLForAccess());
		access.setCommercialCostSchema(this.getCommercialCostSchema().clone());
		access.setLicense(this.getLicense().clone());
		for (int i = 0; i < this.getValidForCountries().size(); i++) {
			access.getValidForCountries().add(
					this.getValidForCountries().get(i));
		}

		// TODO vendor is missing
		return access;
	}

}
