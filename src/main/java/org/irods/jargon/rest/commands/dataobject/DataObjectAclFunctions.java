package org.irods.jargon.rest.commands.dataobject;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.rest.domain.PermissionListing;

public interface DataObjectAclFunctions {

	/**
	 * Return a list of permissions for a given data object
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to the collection
	 * @return {@link PermissionListing} object that can be marshaled to XML or
	 *         JSON
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	PermissionListing listPermissions(String absolutePath)
			throws FileNotFoundException, JargonException;

}