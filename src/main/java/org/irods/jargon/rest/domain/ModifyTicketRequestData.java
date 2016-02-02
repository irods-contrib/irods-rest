/**
 * 
 */
package org.irods.jargon.rest.domain;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;



/**
 * 
 * <pre>
 * Value object to hold request data for iRODS ticket modification.  The request
 * includes the restriction_type and restriction_value.  See documentation for 
 * valid restriction types.
 * 
 * </pre>
 * 
 * @author jjames
 * 
 */
@XmlRootElement(name = "ticket")
public class ModifyTicketRequestData {
	
	/** The mode. */
	private String restrictionType = "";
	
	/** The object path. */
	private String restrictionValue = "";

	/**
	 * @return the restrictionType
	 */
	@XmlElement(name = "restriction_type")
	public String getRestrictionType() {
		return restrictionType;
	}

	/**
	 * @param restrictionType the restrictionType to set
	 */
	public void setRestrictionType(String restrictionType) {
		this.restrictionType = restrictionType;
	}

	/**
	 * @return the restrictionValue
	 */
	@XmlElement(name = "restriction_value")
	public String getRestrictionValue() {
		return restrictionValue;
	}

	/**
	 * @param restrictionValue the restrictionValue to set
	 */
	public void setRestrictionValue(String restrictionValue) {
		this.restrictionValue = restrictionValue;
	}


}
