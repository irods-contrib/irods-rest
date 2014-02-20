package org.irods.jargon.rest.commands.collection;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.rest.domain.PermissionListing;

/**
 * Interface for collection operations dealing with ACLs and permissions
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface CollectionAclFunctions {

	/**
	 * Retrieve a listing of permissions on a collection. This is a
	 * course-grained operation that consolidates inheritance, user, and group
	 * permissions
	 * 
	 * @param IORDSAccount
	 *            {@link IRODSAccount} for current connection
	 * @param absolutepath
	 *            <code>String</code> with the iRODS absolute path to a
	 *            collection
	 */
	PermissionListing listPermissions(IRODSAccount irodsAccount,
			String absolutePath) throws DataNotFoundException, JargonException;

}