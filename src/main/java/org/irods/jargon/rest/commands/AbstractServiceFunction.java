/**
 * 
 */
package org.irods.jargon.rest.commands;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.rest.configuration.RestConfiguration;

/**
 * Abstract superclass for a service function (business logic module backing an
 * <code>AbstractIrodsService</code> and created by a
 * <code>ServiceFunctionFactory</code>
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public abstract class AbstractServiceFunction {

	private final RestConfiguration restConfiguration;
	private final IRODSAccount irodsAccount;
	private final IRODSAccessObjectFactory irodsAccessObjectFactory;

	/**
	 * @param restConfiguration
	 * @param irodsAccount
	 * @param irodsAccessObjectFactory
	 */
	public AbstractServiceFunction(RestConfiguration restConfiguration,
			IRODSAccount irodsAccount,
			IRODSAccessObjectFactory irodsAccessObjectFactory) {
		super();
		this.restConfiguration = restConfiguration;
		this.irodsAccount = irodsAccount;
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	/**
	 * @return the restConfiguration
	 */
	protected RestConfiguration getRestConfiguration() {
		return restConfiguration;
	}

	/**
	 * @return the irodsAccount
	 */
	protected IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/**
	 * @return the irodsAccessObjectFactory
	 */
	protected IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

}
