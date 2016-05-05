/**
 * 
 */
package org.irods.jargon.rest.commands.ticket;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.rest.commands.AbstractIrodsService;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.irods.jargon.rest.domain.CreateTicketRequestData;
import org.irods.jargon.rest.domain.CreateTicketResponseData;
import org.irods.jargon.rest.domain.ModifyTicketRequestData;
import org.irods.jargon.rest.domain.TicketData;
import org.irods.jargon.ticket.Ticket;
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

	public static final SimpleDateFormat EXPIRATION_DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	/** The log. */
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = RestConfiguration.NS, jsonName = RestConfiguration.JSON_NAME) })
	public CreateTicketResponseData createTicket(
			@HeaderParam("Authorization") final String authorization,
			@Mapped(namespaceMap = { @XmlNsMap(namespace = RestConfiguration.NS, jsonName = RestConfiguration.JSON_NAME) }) final CreateTicketRequestData requestData)
			throws JargonException, GenQueryBuilderException,
			JargonQueryException {

		log.info("createTicket()");
		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);

			IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
			IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
					.getIRODSAccessObjectFactory();

			TicketServiceFactoryImpl ticketServiceFactory = new TicketServiceFactoryImpl(
					accessObjectFactory);
			TicketAdminService ticketService = ticketServiceFactory
					.instanceTicketAdminService(irodsAccount);

			log.info("objectPath={}", requestData.getObjectPath());

			IRODSFile file = accessObjectFactory.getIRODSFileFactory(
					irodsAccount)
					.instanceIRODSFile(requestData.getObjectPath());

			String ticketString = requestData.getTicketString();

			String responseTicketString = ticketService.createTicket(
					TicketCreateModeEnum.findTypeByString(requestData.getMode()
							.toLowerCase()), file, ticketString);

			CreateTicketResponseData responseData = new CreateTicketResponseData();
			responseData.setTicketString(responseTicketString);
			return responseData;

		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	@DELETE
	@Path("{ticket:.*}")
	@Mapped(namespaceMap = { @XmlNsMap(namespace = RestConfiguration.NS, jsonName = "irods-rest") })
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
			TicketServiceFactoryImpl ticketServiceFactory = new TicketServiceFactoryImpl(
					accessObjectFactory);
			TicketAdminService ticketService = ticketServiceFactory
					.instanceTicketAdminService(irodsAccount);
			ticketService.deleteTicket(ticketId);
		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	@PUT
	@Path("{ticket:.*}")
	@Consumes({ "application/xml", "application/json" })
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = RestConfiguration.NS, jsonName = "irods-rest") })
	public void updateTicket(
			@HeaderParam("Authorization") final String authorization,
			@PathParam("ticket") final String ticketString,
			final ModifyTicketRequestData requestData) throws JargonException,
			GenQueryBuilderException, JargonQueryException {

		log.info("updateTicket()");
		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);
			IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
			IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
					.getIRODSAccessObjectFactory();
			TicketServiceFactoryImpl ticketServiceFactory = new TicketServiceFactoryImpl(
					accessObjectFactory);
			TicketAdminService ticketService = ticketServiceFactory
					.instanceTicketAdminService(irodsAccount);

			String restrictionType = requestData.getRestrictionType();
			String restrictionValue = requestData.getRestrictionValue();

			switch (restrictionType.toLowerCase()) {
			case "add_host":
				ticketService.addTicketHostRestriction(ticketString,
						restrictionValue);
				break;
			case "remove_host":
				ticketService.removeTicketHostRestriction(ticketString,
						restrictionValue);
				break;
			case "add_group":
				ticketService.addTicketGroupRestriction(ticketString,
						restrictionValue);
				break;
			case "remove_group":
				ticketService.removeTicketGroupRestriction(ticketString,
						restrictionValue);
				break;
			case "add_user":
				ticketService.addTicketUserRestriction(ticketString,
						restrictionValue);
				break;
			case "remove_user":
				ticketService.removeTicketUserRestriction(ticketString,
						restrictionValue);
				break;
			case "byte_write_limit":
				long byteWriteLimit = Long.parseLong(restrictionValue);
				ticketService.setTicketByteWriteLimit(ticketString,
						byteWriteLimit);
				break;
			case "file_write_limit":
				int fileWriteLimit = Integer.parseInt(restrictionValue);
				ticketService.setTicketFileWriteLimit(ticketString,
						fileWriteLimit);
				break;
			case "uses_limit":
				int usesLimit = Integer.parseInt(restrictionValue);
				ticketService.setTicketUsesLimit(ticketString, usesLimit);
				break;
			case "expiration":
				Date expirationTime = EXPIRATION_DATE_FORMAT
						.parse(restrictionValue);
				ticketService.setTicketExpiration(ticketString, expirationTime);
				break;
			default:
				break;
			}

		} catch (NumberFormatException nfe) {
			throw new JargonException(
					"Cannot convert restrictionValue to integer or long", nfe);
		} catch (ParseException pe) {
			throw new JargonException(
					"RestrictionValue is not a valid date in the format yyyy-MM-dd HH:mm:ss",
					pe);
		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	@GET
	@Path("{ticket:.*}")
	@Consumes({ "application/xml", "application/json" })
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = RestConfiguration.NS, jsonName = "irods-rest") })
	public TicketData listTicket(
			@HeaderParam("Authorization") final String authorization,
			@PathParam("ticket") final String ticketString)
			throws JargonException, GenQueryBuilderException,
			JargonQueryException {

		log.info("updateTicket()");
		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);
			IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
			IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
					.getIRODSAccessObjectFactory();
			TicketServiceFactoryImpl ticketServiceFactory = new TicketServiceFactoryImpl(
					accessObjectFactory);
			TicketAdminService ticketService = ticketServiceFactory
					.instanceTicketAdminService(irodsAccount);

			Ticket ticketInfo = ticketService
					.getTicketForSpecifiedTicketString(ticketString);

			TicketData responseData = new TicketData(ticketInfo);

			// retrieve and set the user, group and host restrictions
			responseData.setGroupRestrictions((ArrayList<String>) ticketService
					.listAllGroupRestrictionsForSpecifiedTicket(
							responseData.getTicketString(), 0));
			responseData.setUserRestrictions((ArrayList<String>) ticketService
					.listAllUserRestrictionsForSpecifiedTicket(
							responseData.getTicketString(), 0));
			responseData.setHostRestrictions((ArrayList<String>) ticketService
					.listAllHostRestrictionsForSpecifiedTicket(
							responseData.getTicketString(), 0));

			return responseData;

		} catch (NumberFormatException nfe) {
			throw new JargonException(
					"Cannot convert restrictionValue to integer or long", nfe);
		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

}