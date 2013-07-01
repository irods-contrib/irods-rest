/**
 * 
 */
package org.irods.jargon.rest.commands.user;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.rest.auth.RestAuthUtils;
import org.irods.jargon.rest.commands.user.UserAddActionResponse.UserAddActionResponseCode;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.irods.jargon.rest.domain.UserData;
import org.irods.jargon.rest.utils.ConfigurationUtils;
import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Restful services for iRODS users
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Named
@Path("/user")
public class UserService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject
	IRODSAccessObjectFactory irodsAccessObjectFactory;

	@Inject
	RestConfiguration restConfiguration;

	/**
	 * @return the irodsAccessObjectFactory
	 */
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/**
	 * @param irodsAccessObjectFactory
	 *            the irodsAccessObjectFactory to set
	 */
	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	@GET
	@Path("/{userName}")
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public UserData getUser(
			@HeaderParam("Authorization") final String authorization,
			@PathParam("userName") final String userName)
			throws JargonException {
		log.info("getUser()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		try {
			IRODSAccount irodsAccount = RestAuthUtils
					.getIRODSAccountFromBasicAuthValues(authorization,
							restConfiguration);
			UserAO userAO = irodsAccessObjectFactory.getUserAO(irodsAccount);
			log.info("looking up user with name:{}", userName);
			User user = userAO.findByName(userName);
			log.info("user found:{}", user);

			return new UserData(user, restConfiguration);
		} finally {
			irodsAccessObjectFactory.closeSessionAndEatExceptions();
		}
	}

	/**
	 * Add a user and set the password based on a {@link UserAddByAdminRequest},
	 * which is expected as a JSON structure in the PUT message body. This
	 * method will return a JSON structure reflecting
	 * {@link UserAddActionResponse} with error or success details.
	 * <p/>
	 * In iRODS, this method will add the user and set a temporary password as
	 * described in the JSON request.
	 * <p/>
	 * Note that any Jargon or other exceptions are trapped and returned to the
	 * caller in the response.
	 * 
	 * @param authorization
	 *            BasicAuth info
	 * @param userAddByAdminRequest
	 *            {@link UserAddByAdminRequest}
	 * @return {@link UserAddActionResponse}
	 * 
	 */
	@PUT
	@Consumes("application/json")
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public UserAddActionResponse addUser(
			@HeaderParam("Authorization") final String authorization,
			final UserAddByAdminRequest userAddByAdminRequest) {
		log.info("addUser()");
		UserAddActionResponse response = new UserAddActionResponse();

		if (userAddByAdminRequest == null) {
			log.error("request not found");
			response.setUserAddActionResponse(UserAddActionResponseCode.ATTRIBUTES_MISSING);
			response.setMessage("null userAddByAdminRequest");
			return response;
		}

		log.info("userAddByAdminRequest:{}", userAddByAdminRequest);

		if (userAddByAdminRequest.getUserName() == null
				|| userAddByAdminRequest.getUserName().isEmpty()) {
			log.error("user name missing");
			response.setMessage("User name is missing in the request");
			response.setUserAddActionResponse(UserAddActionResponseCode.ATTRIBUTES_MISSING);
			return response;
		}

		if (userAddByAdminRequest.getTempPassword() == null
				|| userAddByAdminRequest.getTempPassword().isEmpty()) {
			log.error("temp password is missing");
			response.setMessage("temp password is missing in the request");
			response.setUserName(userAddByAdminRequest.getUserName());
			response.setUserAddActionResponse(UserAddActionResponseCode.ATTRIBUTES_MISSING);
			return response;
		}

		try {
			IRODSAccount irodsAccount = RestAuthUtils
					.getIRODSAccountFromBasicAuthValues(authorization,
							restConfiguration);

			UserAO userAO = irodsAccessObjectFactory.getUserAO(irodsAccount);

			log.info("adding user based on:{}", userAddByAdminRequest);
			User user = new User();
			user.setName(userAddByAdminRequest.getUserName());
			user.setUserDN(userAddByAdminRequest.getDistinguishedName());
			user.setUserType(UserTypeEnum.RODS_USER);
			userAO.addUser(user);
			log.info("user added... set the password");

			userAO.changeAUserPasswordByAnAdmin(user.getName(),
					userAddByAdminRequest.getTempPassword());
			log.info("password was set to requested value");
			response.setMessage("success");
			response.setUserName(userAddByAdminRequest.getUserName());
			response.setUserAddActionResponse(UserAddActionResponseCode.SUCCESS);
			response.setIrodsEnv(ConfigurationUtils
					.buildIrodsEnvForConfigAndUser(restConfiguration,
							user.getNameWithZone()));
			response.setWebAccessURL(restConfiguration.getWebInterfaceURL());
			return response;
		} catch (DuplicateDataException dde) {
			log.error("duplicate data for user add", dde);
			response.setMessage(dde.getMessage());
			response.setUserName(userAddByAdminRequest.getUserName());
			response.setUserAddActionResponse(UserAddActionResponseCode.USER_NAME_IS_TAKEN);
			return response;
		} catch (JargonException je) {
			log.error("Jargon exception in user add", je);
			response.setMessage(je.getMessage());
			response.setUserName(userAddByAdminRequest.getUserName());
			response.setUserAddActionResponse(UserAddActionResponseCode.INTERNAL_ERROR);
			return response;
		} finally {
			irodsAccessObjectFactory.closeSessionAndEatExceptions();
		}
	}

	/**
	 * @return the restConfiguration
	 */
	public RestConfiguration getRestConfiguration() {
		return restConfiguration;
	}

	/**
	 * @param restConfiguration
	 *            the restConfiguration to set
	 */
	public void setRestConfiguration(final RestConfiguration restConfiguration) {
		this.restConfiguration = restConfiguration;
	}

}
