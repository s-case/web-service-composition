package eu.fp7.scase.operation.ipr;

import gr.iti.wsdl.wsdlToolkit.WSOperation;

import java.util.ArrayList;
import java.util.List;



public class Vendor {

	private String vendorName = "";
	private ReputationSchema reputation = new ReputationSchema();
	private String contactDetails = "";
	private String country = "";
	private String description = "";
	private List<EULA> eulas = new ArrayList<EULA>();
	private List<SLA> slasAsOwner = new ArrayList<SLA>();
	private List<SLA> slasAsVendor = new ArrayList<SLA>();
	private List<WSOperation> vendorOf = new ArrayList<WSOperation>();
	private String ontologyURI = "";

	/**
	 * @return the vendorName
	 */
	public String getVendorName() {
		return vendorName;
	}

	/**
	 * @param vendorName
	 *            the vendorName to set
	 */
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	/**
	 * @return the reputation
	 */
	public ReputationSchema getReputation() {
		return reputation;
	}

	/**
	 * @param reputation
	 *            the reputation to set
	 */
	public void setReputation(ReputationSchema reputation) {
		this.reputation = reputation;
	}

	/**
	 * @return the contactDetails
	 */
	public String getContactDetails() {
		return contactDetails;
	}

	/**
	 * @param contactDetails
	 *            the contactDetails to set
	 */
	public void setContactDetails(String contactDetails) {
		this.contactDetails = contactDetails;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @param country
	 *            the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the eulas
	 */
	public List<EULA> getEulas() {
		return eulas;
	}

	/**
	 * @param eulas
	 *            the eulas to set
	 */
	public void setEulas(List<EULA> eulas) {
		this.eulas = eulas;
	}

	/**
	 * @return the slasAsOwner
	 */
	public List<SLA> getSlasAsOwner() {
		return slasAsOwner;
	}

	/**
	 * @param slasAsOwner
	 *            the slasAsOwner to set
	 */
	public void setSlasAsOwner(List<SLA> slasAsOwner) {
		this.slasAsOwner = slasAsOwner;
	}

	/**
	 * @return the slasAsVendor
	 */
	public List<SLA> getSlasAsVendor() {
		return slasAsVendor;
	}

	/**
	 * @param slasAsVendor
	 *            the slasAsVendor to set
	 */
	public void setSlasAsVendor(List<SLA> slasAsVendor) {
		this.slasAsVendor = slasAsVendor;
	}

	/**
	 * @return the vendorOf
	 */
	public List<WSOperation> getVendorOf() {
		return vendorOf;
	}

	/**
	 * @param vendorOf
	 *            the vendorOf to set
	 */
	public void setVendorOf(List<WSOperation> vendorOf) {
		this.vendorOf = vendorOf;
	}

	public String getOntologyURI() {
		return ontologyURI;
	}

	public void setOntologyURI(String ontologyURI) {
		this.ontologyURI = ontologyURI;
	}

	public Vendor clone() {
		Vendor ven = new Vendor();
		ven.setContactDetails(this.getContactDetails());
		ven.setCountry(this.getCountry());
		ven.setDescription(this.getDescription());
		ven.setOntologyURI(this.getOntologyURI());
		ven.setReputation(this.getReputation().clone());
		// TODO clone eulas, slas,solutions

		return ven;
	}

}
