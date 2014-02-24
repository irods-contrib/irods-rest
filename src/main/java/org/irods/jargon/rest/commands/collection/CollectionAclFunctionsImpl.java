/**
 * 
 */
package org.irods.jargon.rest.commands.collection;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.rest.commands.AbstractServiceFunction;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.irods.jargon.rest.domain.PermissionEntry;
import org.irods.jargon.rest.domain.PermissionListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Backing services for operating on collection ACLs and permissions
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class CollectionAclFunctionsImpl extends AbstractServiceFunction
		implements CollectionAclFunctions {

	private static final Logger log = LoggerFactory
			.getLogger(CollectionAclFunctionsImpl.class);

	public CollectionAclFunctionsImpl(RestConfiguration restConfiguration,
			IRODSAccount irodsAccount,
			IRODSAccessObjectFactory irodsAccessObjectFactory) {
		super(restConfiguration, irodsAccount, irodsAccessObjectFactory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.rest.commands.collection.CollectionAclFunctions#
	 * addPermission(java.lang.String, java.lang.String,
	 * org.irods.jargon.core.protovalues.FilePermissionEnum, boolean)
	 */
	@Override
	public void addPermission(final String absolutePath, final String userName,
			final FilePermissionEnum permission, final boolean recursive)
			throws InvalidUserException, FileNotFoundException, JargonException {

		log.info("addPermission()");

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (permission == null) {
			throw new IllegalArgumentException("null permission");
		}

		log.info("absolutePath:{}", absolutePath);
		log.info("userName:{}", userName);
		log.info("permission:{}", permission);
		log.info("recursive:{}", recursive);

		CollectionAO collectionAO = this.getIrodsAccessObjectFactory()
				.getCollectionAO(getIrodsAccount());

		String userPartString = MiscIRODSUtils.getUserInUserName(userName);
		String userZoneString = MiscIRODSUtils.getZoneInUserName(userName);

		log.info("setting the permission...");
		collectionAO.setAccessPermission(userZoneString, absolutePath,
				userPartString, recursive, permission);
		log.info("permission set");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.rest.commands.collection.CollectionAclFunctions#
	 * listPermissions(java.lang.String)
	 */
	@Override
	public PermissionListing listPermissions(final String absolutePath)
			throws FileNotFoundException, JargonException {

		log.info("listPermissions()");

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		log.info("absolutePath:{}", absolutePath);

		log.info("get collection AO and permissions list");
		CollectionAO collectionAO = this.getIrodsAccessObjectFactory()
				.getCollectionAO(getIrodsAccount());
		List<UserFilePermission> permissions = collectionAO
				.listPermissionsForCollection(absolutePath);
		log.info("get inheritance...");
		boolean inheritance = collectionAO
				.isCollectionSetForPermissionInheritance(absolutePath);
		PermissionListing permissionListing = new PermissionListing();
		permissionListing.setAbsolutePathString(absolutePath);
		permissionListing.setInheritance(inheritance);
		permissionListing.setObjectType(ObjectType.COLLECTION);

		PermissionEntry entry;
		for (UserFilePermission permission : permissions) {
			entry = new PermissionEntry();
			entry.setFilePermissionEnum(permission.getFilePermissionEnum());
			entry.setUserId(permission.getUserId());
			entry.setUserName(permission.getUserName());
			entry.setUserType(permission.getUserType());
			entry.setUserZone(permission.getUserZone());
			permissionListing.getPermissionEntries().add(entry);
		}

		log.info("all data marshaled");
		return permissionListing;
	}

}
