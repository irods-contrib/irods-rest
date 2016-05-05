/**
 * 
 */
package org.irods.jargon.rest.domain;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.irods.jargon.rest.configuration.RestConfiguration;

/**
 * Value object to hold request data for ticket creation. The
 * 
 * @author jjames
 * 
 */
@XmlType(name = "createTicketRequestData", namespace = RestConfiguration.NS)
@XmlRootElement(name = "createTicketRequestData", namespace = RestConfiguration.NS)
public class CreateTicketRequestData {

	/** The mode. */
	private String mode = "";

	/** The object path. */
	private String objectPath = "";

	/** The object. */
	private String ticketString = "";

	public CreateTicketRequestData() {

	}

	/**
	 * @return the mode
	 */
	@XmlElement(name = "mode", required = true, namespace = RestConfiguration.NS)
	public String getMode() {
		return mode;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * @return the objectPath
	 */
	@XmlElement(name = "object_path", required = true, namespace = RestConfiguration.NS)
	public String getObjectPath() {
		return objectPath;
	}

	/**
	 * @param objectPath
	 *            the objectPath to set
	 */
	public void setObjectPath(String objectPath) {
		this.objectPath = objectPath;
	}

	/**
	 * Gets the ticket string.
	 *
	 * @return the ticket string
	 */
	@XmlElement(name = "ticket_string", required = false, namespace = RestConfiguration.NS)
	public String getTicketString() {
		return ticketString;
	}

	/**
	 * Sets the ticket string.
	 *
	 * @param ticketString
	 *            the new ticket string
	 */
	public void setTicketString(String ticketString) {
		this.ticketString = ticketString;
	}

}
