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
public class CreateTicketRequestData {
	
	/** The mode. */
	private String mode = "";
	
	/** The object path. */
	private String objectPath = "";
	
	/** The object. */
	private String ticketString = "";

	/**
	 * @return the mode
	 */
	@XmlElement(name = "mode")
	public String getMode() {
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * @return the objectPath
	 */
	@XmlElement(name = "object_path")
	public String getObjectPath() {
		return objectPath;
	}

	/**
	 * @param objectPath the objectPath to set
	 */
	public void setObjectPath(String objectPath) {
		this.objectPath = objectPath;
	}
	

	/**
	 * Gets the ticket string.
	 *
	 * @return the ticket string
	 */
	@XmlElement(name = "string", required=false)
	public String getTicketString() {
		return ticketString;
	}

	/**
	 * Sets the ticket string.
	 *
	 * @param ticketString the new ticket string
	 */
	public void setTicketString(String ticketString) {
		this.ticketString = ticketString;
	}

}
