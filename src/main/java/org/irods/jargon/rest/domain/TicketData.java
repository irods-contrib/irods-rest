/**
 * 
 */
package org.irods.jargon.rest.domain;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.irods.jargon.rest.commands.ticket.TicketService;
import org.irods.jargon.ticket.Ticket;
import org.irods.jargon.ticket.Ticket.TicketObjectType;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;



/**
 * <pre>
 * Holds return data for iRODS ticket information.
 * 
 * The following is an example response.
 * 
 * {@code
 *    <ns2:ticket xmlns:ns2="http://irods.org/irods-rest">
 *      <expire_time>2016-02-28 12:00:00</expire_time>
 *      <irods_path>/tempZone/home/rods</irods_path>
 *      <object_type>collection</object_type>
 *      <owner_name>rods</owner_name>
 *      <owner_zone>tempZone</owner_zone> 
 *      <ticket_id>12493</ticket_id> 
 *      <string>abcdefghijklmno</string>
 *      <ticket_type>write</ticket_type>
 *      <uses_count>0</uses_count>
 *      <uses_limit>19</uses_limit>
 *      <write_byte_count>0</write_byte_count>
 *      <byte_limit>0</byte_limit>
 *      <write_file_count>0</write_file_count>
 *      <write_file_limit>10</write_file_limit>
 *    </ns2:ticket>
 * }
 * 
 * </pre>
 * @author jjames
 * 
 */
@XmlRootElement(name = "ticket")
public class TicketData {

	private Ticket ticket;
	
	private ArrayList<String> hostRestrictions = new ArrayList<String>();
	private ArrayList<String> userRestrictions = new ArrayList<String>();
	private ArrayList<String> groupRestrictions = new ArrayList<String>();
	
	public TicketData() {
	}
	
	public TicketData(Ticket ticket) {
		this.ticket = ticket;
	}
	
	/**
	 * @return the ticketId
	 */
	@XmlElement(name = "ticket_id")
	public String getTicketId() {
		return ticket.getTicketId();
	}

	/**
	 * @param ticketId the ticketId to set
	 */
	public void setTicketId(String ticketId) {
		ticket.setTicketId(ticketId);
	}

	/**
	 * @return the type
	 */
	@XmlElement(name = "ticket_type")
	public String getType() {
		return ticket.getType().name().toLowerCase();
	}

	/**
	 * @param type the type to set
	 */
	public void setType(TicketCreateModeEnum type) {
		ticket.setType(type);
	}

	/**
	 * @return the objectType
	 */
	@XmlElement(name = "object_type")
	public String getObjectType() {
		return ticket.getObjectType().name().toLowerCase();
	}

	/**
	 * @param objectType the objectType to set
	 */
	public void setObjectType(TicketObjectType objectType) {
		ticket.setObjectType(objectType);
	}

	/**
	 * @return the ownerName
	 */
	@XmlElement(name = "owner_name")
	public String getOwnerName() {
		return ticket.getOwnerName();
	}

	/**
	 * @param ownerName the ownerName to set
	 */
	public void setOwnerName(String ownerName) {
		ticket.setOwnerName(ownerName);
	}

	/**
	 * @return the ownerZone
	 */
	@XmlElement(name = "owner_zone")
	public String getOwnerZone() {
		return ticket.getOwnerZone();
	}

	/**
	 * @param ownerZone the ownerZone to set
	 */
	public void setOwnerZone(String ownerZone) {
		ticket.setOwnerZone(ownerZone);
	}

	/**
	 * @return the usesCount
	 */
	@XmlElement(name = "uses_count")
	public int getUsesCount() {
		return ticket.getUsesCount();
	}

	/**
	 * @param usesCount the usesCount to set
	 */
	public void setUsesCount(int usesCount) {
		ticket.setUsesCount(usesCount);
	}

	/**
	 * @return the usesLimit
	 */
	@XmlElement(name = "uses_limit")
	public int getUsesLimit() {
		return ticket.getUsesLimit();
	}

	/**
	 * @param usesLimit the usesLimit to set
	 */
	public void setUsesLimit(int usesLimit) {
		ticket.setUsesLimit(usesLimit);
	}

	/**
	 * @return the writeFileCount
	 */
	@XmlElement(name = "write_file_count")
	public int getWriteFileCount() {
		return ticket.getWriteFileCount();
	}

	/**
	 * @param writeFileCount the writeFileCount to set
	 */
	public void setWriteFileCount(int writeFileCount) {
		this.setWriteFileCount(writeFileCount);
	}

	/**
	 * @return the writeFileLimit
	 */
	@XmlElement(name = "write_file_limit")
	public int getWriteFileLimit() {
		return ticket.getWriteFileLimit();
	}

	/**
	 * @param writeFileLimit the writeFileLimit to set
	 */
	public void setWriteFileLimit(int writeFileLimit) {
		ticket.setWriteFileLimit(writeFileLimit);
	}

	/**
	 * @return the writeByteCount
	 */
	@XmlElement(name = "write_byte_count")
	public long getWriteByteCount() {
		return ticket.getWriteByteCount();
	}

	/**
	 * @param writeByteCount the writeByteCount to set
	 */
	public void setWriteByteCount(long writeByteCount) {
		ticket.setWriteByteCount(writeByteCount);
	}

	/**
	 * @return the writeByteLimit
	 */
	@XmlElement(name = "byte_limit")
	public long getWriteByteLimit() {
		return ticket.getWriteByteLimit();
	}

	/**
	 * @param writeByteLimit the writeByteLimit to set
	 */
	public void setWriteByteLimit(long writeByteLimit) {
		ticket.setWriteByteLimit(writeByteLimit);
	}

	/**
	 * @return the expireTime
	 */
	@XmlElement(name = "expire_time")
	public String getExpireTime() {
		Date expireTime = ticket.getExpireTime();
		if (expireTime == null) {
			return "";
		} else {
			return TicketService.EXPIRATION_DATE_FORMAT.format(expireTime);
		}
	}

	/**
	 * @param expireTime the expireTime to set
	 * @throws ParseException 
	 */
	public void setExpireTime(String expirationDateStr) throws ParseException {
		ticket.setExpireTime(TicketService.EXPIRATION_DATE_FORMAT.parse(expirationDateStr));
	}

	/**
	 * @return the irodsAbsolutePath
	 */
	@XmlElement(name = "irods_path")
	public String getIrodsAbsolutePath() {
		return ticket.getIrodsAbsolutePath();
	}

	/**
	 * @param irodsAbsolutePath the irodsAbsolutePath to set
	 */
	public void setIrodsAbsolutePath(String irodsAbsolutePath) {
		ticket.setIrodsAbsolutePath(irodsAbsolutePath);
	}
	

	/**
	 * Gets the ticket string.
	 *
	 * @return the ticket string
	 */
	@XmlElement(name = "ticket_string")
	public String getTicketString() {
		return ticket.getTicketString();
	}

	/**
	 * Sets the ticket string.
	 *
	 * @param ticketString the new ticket string
	 */
	public void setTicketString(String ticketString) {
		ticket.setTicketString(ticketString);
	}

	/**
	 * @return the hostRestrictions
	 */
	@XmlElement(name = "host_restrictions")
	public List<String> getHostRestrictions() {
		return hostRestrictions;
	}

	/**
	 * @param hostRestrictions the hostRestrictions to set
	 */
	public void setHostRestrictions(ArrayList<String> hostRestrictions) {
		this.hostRestrictions = hostRestrictions;
	}

	/**
	 * @return the userRestrictions
	 */
	@XmlElement(name = "user_restrictions")
	public List<String> getUserRestrictions() {
		return userRestrictions;
	}

	/**
	 * @param userRestrictions the userRestrictions to set
	 */
	public void setUserRestrictions(ArrayList<String> userRestrictions) {
		this.userRestrictions = userRestrictions;
	}

	/**
	 * @return the groupRestrictions
	 */
	@XmlElement(name = "group_restrictions")
	public List<String> getGroupRestrictions() {
		return groupRestrictions;
	}

	/**
	 * @param groupRestrictions the groupRestrictions to set
	 */
	public void setGroupRestrictions(ArrayList<String> groupRestrictions) {
		this.groupRestrictions = groupRestrictions;
	}

}
