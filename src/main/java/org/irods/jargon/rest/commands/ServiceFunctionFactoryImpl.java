/**
 * 
 */
package org.irods.jargon.rest.commands;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.rest.commands.collection.CollectionAclFunctions;
import org.irods.jargon.rest.commands.collection.CollectionAclFunctionsImpl;
import org.irods.jargon.rest.commands.dataobject.DataObjectAclFunctions;
import org.irods.jargon.rest.commands.dataobject.DataObjectAclFunctionsImpl;
import org.irods.jargon.rest.configuration.RestConfiguration;

/**
 * Factory that produces service functions (business logic behind REST service
 * wrappers)
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ServiceFunctionFactoryImpl implements ServiceFunctionFactory {

	/**
	 * Required dependency on configuration
	 */
	private RestConfiguration restConfiguration;

	/**
	 * Required dependency
	 */
	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.rest.commands.ServiceFunctionFactory#
	 * instanceCollectionAclFunctions
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public CollectionAclFunctions instanceCollectionAclFunctions(
			final IRODSAccount irodsAccount) {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		return new CollectionAclFunctionsImpl(restConfiguration, irodsAccount,
				irodsAccessObjectFactory);
	}

	@Override
	public DataObjectAclFunctions instanceDataObjectAclFunctions(
			final IRODSAccount irodsAccount) {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		return new DataObjectAclFunctionsImpl(restConfiguration, irodsAccount,
				irodsAccessObjectFactory);
	}

	/**
	 * 
	 */
	public ServiceFunctionFactoryImpl() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.rest.commands.ServiceFunctionFactory#getRestConfiguration
	 * ()
	 */
	@Override
	public RestConfiguration getRestConfiguration() {
		return restConfiguration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.rest.commands.ServiceFunctionFactory#setRestConfiguration
	 * (org.irods.jargon.rest.configuration.RestConfiguration)
	 */
	@Override
	public void setRestConfiguration(RestConfiguration restConfiguration) {
		this.restConfiguration = restConfiguration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.rest.commands.ServiceFunctionFactory#
	 * getIrodsAccessObjectFactory()
	 */
	@Override
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.rest.commands.ServiceFunctionFactory#
	 * setIrodsAccessObjectFactory
	 * (org.irods.jargon.core.pub.IRODSAccessObjectFactory)
	 */
	@Override
	public void setIrodsAccessObjectFactory(
			IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

}
