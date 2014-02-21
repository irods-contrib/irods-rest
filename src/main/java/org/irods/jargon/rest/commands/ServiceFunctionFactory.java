package org.irods.jargon.rest.commands;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.rest.commands.collection.CollectionAclFunctions;
import org.irods.jargon.rest.commands.dataobject.DataObjectAclFunctions;
import org.irods.jargon.rest.commands.dataobject.DataObjectAvuFunctions;
import org.irods.jargon.rest.configuration.RestConfiguration;

/**
 * Factory for service functions (business logic behind REST service wrappers)
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface ServiceFunctionFactory {

	/**
	 * @return the restConfiguration
	 */
	public abstract RestConfiguration getRestConfiguration();

	/**
	 * @param restConfiguration
	 *            the restConfiguration to set
	 */
	public abstract void setRestConfiguration(
			RestConfiguration restConfiguration);

	/**
	 * @return the irodsAccessObjectFactory
	 */
	public abstract IRODSAccessObjectFactory getIrodsAccessObjectFactory();

	/**
	 * @param irodsAccessObjectFactory
	 *            the irodsAccessObjectFactory to set
	 */
	public abstract void setIrodsAccessObjectFactory(
			IRODSAccessObjectFactory irodsAccessObjectFactory);

	/**
	 * create an object that manages collection acls and permissions
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} for current connection
	 * @return {@link CollectionAclFunctions}
	 */
	CollectionAclFunctions instanceCollectionAclFunctions(
			final IRODSAccount irodsAccount);

	/**
	 * Create an object that manages data object ACL data
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} for current connection
	 * @return {@link DataObjectAclFunctions}
	 */
	DataObjectAclFunctions instanceDataObjectAclFunctions(
			IRODSAccount irodsAccount);

	/**
	 * Create an object that manages data object AVU data
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} for current connection
	 * @return {@link DataObjectAvuFunctions}
	 */
	DataObjectAvuFunctions instanceDataObjectAvuFunctions(
			IRODSAccount irodsAccount);

}