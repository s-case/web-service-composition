package eu.scasefp7.eclipse.servicecomposition.operation.ipr;

import java.util.ArrayList;
import java.util.List;

public class CommercialCostSchema {
	private String commercialCostCurrency = "";
	private String costPaymentChargeType = "";
	private float commercialCost;
	private ServiceTrialSchema trialSchema = new ServiceTrialSchema();
	private List<DiscountSchema> discountIfUsedWithOtherService = new ArrayList<DiscountSchema>();

	/**
	 * @return the commercialCostCurrency
	 */
	public String getCommercialCostCurrency() {
		return commercialCostCurrency;
	}

	/**
	 * @param commercialCostCurrency
	 *            the commercialCostCurrency to set
	 */
	public void setCommercialCostCurrency(String commercialCostCurrency) {
		this.commercialCostCurrency = commercialCostCurrency;
	}

	/**
	 * @return the costPaymentChargeType
	 */
	public String getCostPaymentChargeType() {
		return costPaymentChargeType;
	}

	/**
	 * @param costPaymentChargeType
	 *            the costPaymentChargeType to set
	 */
	public void setCostPaymentChargeType(String costPaymentChargeType) {
		this.costPaymentChargeType = costPaymentChargeType;
	}

	/**
	 * @return the commercialCost
	 */
	public float getCommercialCost() {
		return commercialCost;
	}

	/**
	 * @param commercialCost
	 *            the commercialCost to set
	 */
	public void setCommercialCost(float commercialCost) {
		this.commercialCost = commercialCost;
	}

	/**
	 * @return the trialSchema
	 */
	public ServiceTrialSchema getTrialSchema() {
		return trialSchema;
	}

	/**
	 * @param trialSchema
	 *            the trialSchema to set
	 */
	public void setTrialSchema(ServiceTrialSchema trialSchema) {
		this.trialSchema = trialSchema;
	}

	

	public List<DiscountSchema> getDiscountIfUsedWithOtherService() {
		return discountIfUsedWithOtherService;
	}

	public void setDiscountIfUsedWithOtherService(
			List<DiscountSchema> discountIfUsedWithOtherService) {
		this.discountIfUsedWithOtherService = discountIfUsedWithOtherService;
	}

	public CommercialCostSchema clone() {
		CommercialCostSchema commercialCostSchema = new CommercialCostSchema();
		commercialCostSchema.setCommercialCost(this.getCommercialCost());
		commercialCostSchema.setCommercialCostCurrency(this
				.getCommercialCostCurrency());
		commercialCostSchema.setCostPaymentChargeType(this
				.getCostPaymentChargeType());
		commercialCostSchema.setTrialSchema(this.getTrialSchema().clone());
		List<DiscountSchema> list = new ArrayList<DiscountSchema>();
		for (int i = 0; i < this.getDiscountIfUsedWithOtherService().size(); i++) {
			list.add(this.getDiscountIfUsedWithOtherService().get(i).clone());
		}
		commercialCostSchema.setDiscountIfUsedWithOtherService(list);
		return commercialCostSchema;
	}

}
