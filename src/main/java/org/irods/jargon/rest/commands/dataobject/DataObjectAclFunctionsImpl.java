/**
 * 
 */
package org.irods.jargon.rest.commands.dataobject;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.DataObjectAO;
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
 * ACL functions services (business processes) for use by the Data Objects REST
 * facade
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DataObjectAclFunctionsImpl extends AbstractServiceFunction
		implements DataObjectAclFunctions {

	private static final Logger log = LoggerFactory
			.getLogger(DataObjectAclFunctionsImpl.class);

	/**
	 * @param restConfiguration
	 * @param irodsAccount
	 * @param irodsAccessObjectFactory
	 */
	public DataObjectAclFunctionsImpl(RestConfiguration restConfiguration,
			IRODSAccount irodsAccount,
			IRODSAccessObjectFactory irodsAccessObjectFactory) {
		super(restConfiguration, irodsAccount, irodsAccessObjectFactory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.rest.commands.dataobject.DataObjectAclFunctions#
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

		log.info("get data object AO and permissions list");
		DataObjectAO dataObjectAO = this.getIrodsAccessObjectFactory()
				.getDataObjectAO(getIrodsAccount());

		log.info("doing a check to see if this exists at all, will throw file not found exception if not");
		dataObjectAO.findByAbsolutePath(absolutePath);

		List<UserFilePermission> permissions = dataObjectAO
				.listPermissionsForDataObject(absolutePath);

		PermissionListing permissionListing = new PermissionListing();
		permissionListing.setAbsolutePathString(absolutePath);
		permissionListing.setInheritance(false);
		permissionListing.setObjectType(ObjectType.DATA_OBJECT);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.rest.commands.dataobject.DataObjectAclFunctions#
	 * addPermission(java.lang.String, java.lang.String,
	 * org.irods.jargon.core.protovalues.FilePermissionEnum)
	 */
	@Override
	public void addPermission(final String absolutePath, final String userName,
			final FilePermissionEnum permission) throws InvalidUserException,
			FileNotFoundException, JargonException {

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

		DataObjectAO dataObjectAO = this.getIrodsAccessObjectFactory()
				.getDataObjectAO(getIrodsAccount());

		String userPartString = MiscIRODSUtils.getUserInUserName(userName);
		String userZoneString = MiscIRODSUtils.getZoneInUserName(userName);

		log.info("setting the permission...");
		dataObjectAO.setAccessPermission(userZoneString, absolutePath,
				userPartString, permission);
		log.info("permission set");

	}

}
