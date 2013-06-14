/**
 * 
 */
package org.irods.jargon.rest.commands;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Restful services for iRODS users
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Path("/user")
public class UserService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
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
	@Path("{userName}")
	public User getUser(@PathParam("userName") final String userName) throws JargonException {
		log.info("getUser()");
		
		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}
		
		return new User();
	}

	@PUT
	@Path("{userName}")
	public void addUser() throws JargonException {
		log.info("addUser()");
	}

}
