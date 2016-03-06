package eu.scasefp7.eclipse.servicecomposition.operation.ipr;


import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Operation;
import gr.iti.wsdl.wsdlToolkit.WSOperation;

import java.util.ArrayList;
import java.util.List;






public class DiscountSchema {
	//discount when used together with another service in the workflow
	private int discount = 0;
	private Operation pairedService = new Operation();
	
	private String discountReason = "";
	

	

	public DiscountSchema() {

	}



	/**
	 * @return the discount
	 */
	public int getDiscount() {
		return discount;
	}

	/**
	 * @param discount
	 *            the discount to set
	 */
	public void setDiscount(int discount) {
		this.discount = discount;
	}

	

	/**
	 * @return the discountReason
	 */
	public String getDiscountReason() {
		return discountReason;
	}

	/**
	 * @param discountReason
	 *            the discountReason to set
	 */
	public void setDiscountReason(String discountReason) {
		this.discountReason = discountReason;
	}

	
	public DiscountSchema clone() {
		DiscountSchema discountSchema = new DiscountSchema();
		discountSchema.setDiscount(this.getDiscount());
		discountSchema.setDiscountReason(this.getDiscountReason());
		// we do not clone solution because it will cause an infinite loop
		// discountSchema.setPairedSolution(pairedSolution)
		return discountSchema;

	}

	public Operation getPairedService() {
		return pairedService;
	}

	public void setPairedService(Operation pairedService) {
		this.pairedService = pairedService;
	}

	
	
	



}
