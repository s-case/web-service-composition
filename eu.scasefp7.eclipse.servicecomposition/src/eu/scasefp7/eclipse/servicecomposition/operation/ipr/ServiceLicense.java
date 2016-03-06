package eu.scasefp7.eclipse.servicecomposition.operation.ipr;

public class ServiceLicense {
	//service license
	private String licenseDescription = "";
	private String licenseName = "";
	private boolean proprietary = false;

	/**
	 * @return the licenseDescription
	 */
	public String getLicenseDescription() {
		return licenseDescription;
	}

	/**
	 * @param licenseDescription
	 *            the licenseDescription to set
	 */
	public void setLicenseDescription(String licenseDescription) {
		this.licenseDescription = licenseDescription;
	}

	/**
	 * @return the licenseName
	 */
	public String getLicenseName() {
		return licenseName;
	}

	/**
	 * @param licenseName
	 *            the licenseName to set
	 */
	public void setLicenseName(String licenseName) {
		this.licenseName = licenseName;
	}

	/**
	 * @return the proprietary
	 */
	public boolean isProprietary() {
		return proprietary;
	}

	/**
	 * @param proprietary
	 *            the proprietary to set
	 */
	public void setProprietary(boolean proprietary) {
		this.proprietary = proprietary;
	}

	public ServiceLicense clone() {
		ServiceLicense license = new ServiceLicense();
		license.setLicenseDescription(this.getLicenseDescription());
		license.setLicenseName(this.getLicenseName());
		license.setProprietary(this.isProprietary());
		return license;
	}

}
