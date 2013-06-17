/**
 * 
 */
package org.irods.jargon.rest.commands;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.UserAO;
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
	public UserData getUser(@PathParam("userName") final String userName)
			throws JargonException {
		log.info("getUser()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}
		
		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		try {
			IRODSAccount irodsAccount = IRODSAccount.instance("fedZone1", 1247,
					"test1", "test", "", "fedZone1", "test1-resc");

			UserAO userAO = irodsAccessObjectFactory.getUserAO(irodsAccount);
			
			log.info("looking up user with name:{}", userName);

			return new UserData(userAO.findByName(userName));
		} finally {
			irodsAccessObjectFactory.closeSessionAndEatExceptions();
		}
	}

	@PUT
	@Path("/{userName}")
	public void addUser() throws JargonException {
		log.info("addUser()");
	}


}
