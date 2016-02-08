/**
 * 
 */
package org.irods.jargon.rest.commands.ticket;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.rest.commands.AbstractIrodsService;
import org.irods.jargon.rest.domain.ListTicketResponseData;
import org.irods.jargon.rest.domain.TicketData;
import org.irods.jargon.ticket.Ticket;
import org.irods.jargon.ticket.TicketAdminService;
import org.irods.jargon.ticket.TicketServiceFactoryImpl;
import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ListAllTicketsService.
 * 
 * @author jjames
 */
@Named
@Path("/listAllTickets")
public class ListAllTicketsService extends AbstractIrodsService {
	
	/** The log. */
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	
	@GET
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public ListTicketResponseData listTicket(
			@HeaderParam("Authorization") final String authorization) throws JargonException,
			GenQueryBuilderException, JargonQueryException {

		System.out.println("List All Tickets Ran!!!");
		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);
			IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
			IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
					.getIRODSAccessObjectFactory();
			TicketServiceFactoryImpl ticketServiceFactory = new TicketServiceFactoryImpl(accessObjectFactory);
			TicketAdminService ticketService = ticketServiceFactory.instanceTicketAdminService(irodsAccount);

			ListTicketResponseData responseData = new ListTicketResponseData();
			
			List<Ticket> ticketList = ticketService.listAllTickets(0);
			ArrayList<TicketData> ticketDataList = new ArrayList<TicketData>();
			for (Ticket ticket : ticketList) {
				
				TicketData currentTicket = new TicketData(ticket);
				
				// retrieve and set the user, group and host restrictions
				currentTicket.setGroupRestrictions((ArrayList<String>) ticketService
						.listAllGroupRestrictionsForSpecifiedTicket(
								currentTicket.getTicketString(), 0));
				currentTicket.setUserRestrictions((ArrayList<String>) ticketService
						.listAllUserRestrictionsForSpecifiedTicket(
								currentTicket.getTicketString(), 0));
				currentTicket.setHostRestrictions((ArrayList<String>) ticketService
						.listAllHostRestrictionsForSpecifiedTicket(
								currentTicket.getTicketString(), 0));
				
				// Because of a Jargon issue 172, the irodsAbsolutePath is not being set.  Do another query for this.
				currentTicket.setIrodsAbsolutePath(ticketService
						.getTicketForSpecifiedTicketString(
								currentTicket.getTicketString())
						.getIrodsAbsolutePath());
				
				ticketDataList.add(currentTicket);
			}
			responseData.setTickets(ticketDataList);
			
			return responseData;
			
		} catch (NumberFormatException nfe) {
			throw new JargonException("Cannot convert restrictionValue to integer or long", nfe);
		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}



}