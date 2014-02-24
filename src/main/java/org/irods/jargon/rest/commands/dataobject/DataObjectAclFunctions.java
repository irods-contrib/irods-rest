package org.irods.jargon.rest.commands.dataobject;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
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

	/**
	 * Set a permission for the given user on the given iRODS collection
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the iRODS absolute path to a
	 *            collection
	 * @param userName
	 *            <code>String</code> with a user name, which can be in
	 *            user#zone format
	 * @param permission
	 *            {@link FilePermissionEnum} value for the permission to set
	 * @throws InvalidUserException
	 *             if the user is not available
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	void addPermission(String absolutePath, String userName,
			FilePermissionEnum permission) throws InvalidUserException,
			FileNotFoundException, JargonException;

}