/**
 * 
 */
package org.irods.jargon.rest.commands.user;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.rest.auth.RestAuthUtils;
import org.irods.jargon.rest.commands.AbstractIrodsService;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.irods.jargon.rest.exception.InvalidRequestDataException;
import org.irods.jargon.rest.exception.IrodsRestException;
import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to administer (obtain) temporary iRODS passwords, as a user, or as an
 * admin, as a sub-resource of a user e.g. /user/username/temppassword
 * 
 * @author Mike Conway - DICE
 * 
 */
@Named
@Path("/user")
public class TemporaryPasswordService extends AbstractIrodsService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * 
	 * In iRODS, this method will generate a temporary password on an existing
	 * user as noted in the resource URI, and return that password in an entity.
	 * <p/>
	 * Note that any Jargon or other exceptions are trapped and returned to the
	 * caller in the response.
	 * 
	 * @param authorization
	 *            BasicAuth info
	 * @param userName
	 *            <code>String</code> with the user name for which a temporary
	 *            password will be requested
	 * @return {@link TemporaryPasswordResponse} with the temporary password
	 *         data
	 * @throws IrodsRestException
	 * 
	 */
	@PUT
	@Path("/{userName}/temppassword")
	@Consumes("application/json")
	@Mapped(namespaceMap = { @XmlNsMap(namespace = RestConfiguration.NS, jsonName = RestConfiguration.JSON_NAME) })
	public TemporaryPasswordResponse obtainTemporaryPasswordForUser(
			@HeaderParam("Authorization") final String authorization,
			@PathParam("userName") final String userName,
			@QueryParam("admin") @DefaultValue("false") final boolean isAdmin)
			throws IrodsRestException {
		log.info("obtainTemporaryPasswordForUser()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info("get temp password for user:{}", userName);
		log.info("in admin mode?:{}", isAdmin);

		try {
			IRODSAccount irodsAccount = RestAuthUtils
					.getIRODSAccountFromBasicAuthValues(authorization,
							getRestConfiguration());

			UserAO userAO = getIrodsAccessObjectFactory().getUserAO(
					irodsAccount);
			String tempPassword = null;
			if (isAdmin) {
				log.info("admin mode");
				tempPassword = userAO
						.getTemporaryPasswordForASpecifiedUser(userName);
			} else {

				if (!userName.equals(irodsAccount.getUserName())) {
					log.error("invalid request, not adding flags for admin mode, user name in resource must be the same as the logged in user");
					throw new InvalidRequestDataException(
							"cannot obtain a temporary password as another user when not requesting in admin mode");
				} else {
					tempPassword = userAO
							.getTemporaryPasswordForConnectedUser();

				}
			}

			log.info("obtained temp password");
			TemporaryPasswordResponse temporaryPasswordResponse = new TemporaryPasswordResponse();
			temporaryPasswordResponse.setUserName(userName);
			temporaryPasswordResponse.setPassword(tempPassword);
			return temporaryPasswordResponse;

		} catch (JargonException je) {
			log.error("Jargon exception in user add", je);
			throw new IrodsRestException(
					"jargon exception creating temporary password", je);
		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}
}
