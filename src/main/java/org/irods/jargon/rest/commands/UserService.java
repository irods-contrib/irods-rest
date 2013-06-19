/**
 * 
 */
package org.irods.jargon.rest.commands;

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
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.rest.auth.RestAuthUtils;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.irods.jargon.rest.domain.UserData;
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

			return new UserData(user);
		} finally {
			irodsAccessObjectFactory.closeSessionAndEatExceptions();
		}
	}

	@PUT
	@Consumes("application/json")
	public void addUser(
			@HeaderParam("Authorization") final String authorization,
			final UserAddByAdminRequest userAddByAdminRequest)
			throws JargonException {
		log.info("addUser()");
		if (userAddByAdminRequest == null) {
			throw new IllegalArgumentException("null userAddByAdminRequest");
		}
		log.info("userAddByAdminRequest:{}", userAddByAdminRequest);

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
