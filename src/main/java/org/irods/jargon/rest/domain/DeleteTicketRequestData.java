/**
 * 
 */
package org.irods.jargon.rest.domain;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <pre>
 * Value object to hold request ticket information.  
 * 
 * The XML representation of this request follows.
 * 
 * {@code
 * <ticket> 
 *   <ticket_string>META_DATA_ATTR_NAME</ticket_string> 
 * </ticket>
 * }
 * </pre>
 * 
 * @author jjames
 * 
 */
@XmlRootElement(name = "deleteTicketRequest")
public class DeleteTicketRequestData {

	/** The object. */
	private String ticketString = "";

	/**
	 * Gets the ticket string.
	 *
	 * @return the ticket string
	 */
	@XmlElement(name = "ticket_string", required = true)
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
