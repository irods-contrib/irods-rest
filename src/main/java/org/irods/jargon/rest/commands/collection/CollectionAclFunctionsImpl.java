/**
 * 
 */
package org.irods.jargon.rest.commands.collection;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.rest.commands.AbstractServiceFunction;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.irods.jargon.rest.domain.PermissionListing;

/**
 * Backing services for operating on collection ACLs and permissions
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class CollectionAclFunctionsImpl extends AbstractServiceFunction
		implements CollectionAclFunctions {

	public CollectionAclFunctionsImpl(RestConfiguration restConfiguration,
			IRODSAccount irodsAccount,
			IRODSAccessObjectFactory irodsAccessObjectFactory) {
		super(restConfiguration, irodsAccount, irodsAccessObjectFactory);
	}

	@Override
	public PermissionListing listPermissions(final IRODSAccount irodsAccount,
			final String absolutePath) throws DataNotFoundException,
			JargonException {
		return null;
	}

}
