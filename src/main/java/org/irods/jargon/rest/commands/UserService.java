/**
 * 
 */
package org.irods.jargon.rest.commands;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
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

	@PUT
	@Path("/user/{userName}")
	public void addUser() throws JargonException {
		log.info("addUser()");
	}

}
