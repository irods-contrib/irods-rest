/**
 * 
 */
package org.irods.jargon.rest.commands;

import java.net.URI;
import java.net.URISyntaxException;

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
import org.irods.jargon.core.utils.IRODSUriUtils;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.irods.jargon.rest.domain.UserData;
import org.irods.jargon.rest.utils.RestConstants;
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
	public UserData getUser(@PathParam("userName") final String userName, @HeaderParam(RestConstants.AUTH_RESULT_KEY) final String irodsAccountString)
			throws JargonException {
		log.info("getUser()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}
		
		if (irodsAccountString == null || irodsAccountString.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAccountString");
		}
		
		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		try {
			IRODSAccount irodsAccount = IRODSUriUtils.getIRODSAccountFromURI(new URI(irodsAccountString));
			UserAO userAO = irodsAccessObjectFactory.getUserAO(irodsAccount);
			
			log.info("looking up user with name:{}", userName);

			return new UserData(userAO.findByName(userName));
		} catch (URISyntaxException e) {
			throw new JargonException("invalid iRODS account");
		} finally {
			irodsAccessObjectFactory.closeSessionAndEatExceptions();
		}
	}

	@PUT
	@Consumes("application/json")
	public void addUser(UserAddByAdminRequest userAddByAdminRequest) throws JargonException {
		log.info("addUser()");
		if (userAddByAdminRequest == null) {
			throw new IllegalArgumentException("null userAddByAdminRequest");
		}
		log.info("userAddByAdminRequest:{}", userAddByAdminRequest);
		
		try {
			IRODSAccount irodsAccount = IRODSAccount.instance(restConfiguration.getIrodsHost(), restConfiguration.getIrodsPort(),
					"test1", "test", "", restConfiguration.getIrodsZone(), restConfiguration.getDefaultStorageResource());

			UserAO userAO = irodsAccessObjectFactory.getUserAO(irodsAccount);

			log.info("adding user based on:{}", userAddByAdminRequest);
			User user = new User();
			user.setName(userAddByAdminRequest.getUserName());
			user.setUserDN(userAddByAdminRequest.getDistinguishedName());
			user.setUserType(UserTypeEnum.RODS_USER);
			
			userAO.addUser(user);
			log.info("user added... set the password");
			
			userAO.changeAUserPasswordByAnAdmin(user.getName(), userAddByAdminRequest.getTempPassword());
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
	 * @param restConfiguration the restConfiguration to set
	 */
	public void setRestConfiguration(RestConfiguration restConfiguration) {
		this.restConfiguration = restConfiguration;
	}


}
