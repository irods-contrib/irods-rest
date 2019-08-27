package org.irods.jargon.rest.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <pre>
 * Holds a list of tickets.
 * 
 * The following is a sample XML representation:
 * 
 * {@code
 *   <ns2:tickets xmlns:ns2="http://irods.org/irods-rest">
 *   <ticket>
 *       <expire_time></expire_time> 
 *       <irods_path></irods_path>
 *       ...
 *   </ticket>
 *   <ticket>
 *       ...
 *   </ticket>
 *   ...
 *          
 * }
 * </pre>
 * 
 * @author jjames
 */
@XmlRootElement(name = "tickets")
public class ListTicketResponseData {

	/** The column list. */
	private ArrayList<TicketData> tickets = new ArrayList<TicketData>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Begin Ticket Data");
		for (TicketData ticket : tickets) {
			sb.append(ticket);
			sb.append(System.lineSeparator());
		}
		sb.append(System.lineSeparator());
		sb.append("End Ticket Data");
		return sb.toString();
	}

	/**
	 * Gets the column list.
	 *
	 * @return the column list
	 */
	@XmlElement(name = "ticket")
	public List<TicketData> getTickets() {
		return tickets;
	}

	/**
	 * Sets the column list.
	 *
	 * @param columnList
	 *            the new column list
	 */
	public void setTickets(ArrayList<TicketData> tickets) {
		this.tickets = tickets;
	}

}
