package org.irods.jargon.rest.commands.collection;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
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
	 * @param absolutepath
	 *            <code>String</code> with the iRODS absolute path to a
	 *            collection
	 */
	PermissionListing listPermissions(String absolutePath)
			throws DataNotFoundException, JargonException;

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
	 * @param recursive
	 *            <code>boolean</code> if the permission should be recursively
	 *            set
	 * @throws InvalidUserException
	 *             if the user is not available
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	void addPermission(final String absolutePath, final String userName,
			final FilePermissionEnum permission, final boolean recursive)
			throws InvalidUserException, FileNotFoundException, JargonException;

	/**
	 * Delete a permission for the given user on the given iRODS collection
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the iRODS absolute path to a
	 *            collection
	 * @param userName
	 *            <code>String</code> with a user name, which can be in
	 * @param recursive
	 *            <code>boolean</code> if the permission should be recursively
	 *            set
	 * @throws InvalidUserException
	 *             if the user is not available
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	void deletePermissionForUser(final String absolutePath,
			final String userName, final boolean recursive)
			throws InvalidUserException, FileNotFoundException, JargonException;

}