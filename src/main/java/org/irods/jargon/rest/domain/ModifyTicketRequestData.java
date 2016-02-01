/**
 * 
 */
package org.irods.jargon.rest.domain;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;



/**
 * Value object to hold return value from a GenQuery call.  The request
 * includes an optional count parameter, and optional zone hint, a list 
 * of select fields, a list of conditionals, and a list of order by clauses.
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
