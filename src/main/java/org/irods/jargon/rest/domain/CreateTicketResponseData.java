/**
 * 
 */
package org.irods.jargon.rest.domain;

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
public class CreateTicketResponseData {

	
	/** The object. */
	private String ticketString = "";
	

	/**
	 * Gets the ticket string.
	 *
	 * @return the ticket string
	 */
	@XmlElement(name = "ticket_string")
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
