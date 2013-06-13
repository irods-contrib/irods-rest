/**
 * 
 */
package org.irods.jargon.rest.commands;

import javax.ws.rs.Path;

import org.irods.jargon.core.pub.IRODSAccessObjectFactory;

/**
 * Restful services for iRODS users
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
@Path("/user")
public class UserService {
	
	IRODSAccessObjectFactory irodsAccessObjectFactory;

	/**
	 * @return the irodsAccessObjectFactory
	 */
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/**
	 * @param irodsAccessObjectFactory the irodsAccessObjectFactory to set
	 */
	public void setIrodsAccessObjectFactory(
			IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

}
