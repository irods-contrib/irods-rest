/**
 * 
 */
package org.irods.jargon.rest.commands.ticket;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.rest.commands.AbstractIrodsService;
import org.irods.jargon.rest.domain.CreateTicketRequestData;
import org.irods.jargon.rest.domain.CreateTicketResponseData;
import org.irods.jargon.ticket.TicketAdminService;
import org.irods.jargon.ticket.TicketServiceFactoryImpl;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;
import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class CreateTicketService.
 * 
 * @author jjames
 */
@Named
@Path("/ticket")
public class TicketService extends AbstractIrodsService {


	/** The log. */
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@POST
	@Consumes({ "application/xml", "application/json" })
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public CreateTicketResponseData createTicket(
			@HeaderParam("Authorization") final String authorization,
			final CreateTicketRequestData requestData) throws JargonException,
			GenQueryBuilderException, JargonQueryException {

		log.info("createTicket()");
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
			
			log.info("objectPath={}", requestData.getObjectPath());
			
			IRODSFile file = accessObjectFactory.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
							requestData.getObjectPath());
			
			String ticketString = requestData.getTicketString();
			
			String responseTicketString = ticketService.createTicket(TicketCreateModeEnum.findTypeByString(requestData.getMode().toLowerCase()), file, ticketString);

			CreateTicketResponseData responseData = new CreateTicketResponseData();
			responseData.setTicketString(responseTicketString);
			return responseData;
			
		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}
	
	@DELETE
	@Path("{ticket:.*}")
	@Consumes({ "application/xml", "application/json" })
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public void deleteTicket(
			@HeaderParam("Authorization") final String authorization,
			@PathParam("ticket") final String ticketId) throws JargonException,
			GenQueryBuilderException, JargonQueryException {

		log.info("deleteTicket()");
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
			ticketService.deleteTicket(ticketId);			
		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}


}