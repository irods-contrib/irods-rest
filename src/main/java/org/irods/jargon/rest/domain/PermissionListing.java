/**
 * 
 */
package org.irods.jargon.rest.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;

/**
 * Represents a listing of permissions (ACLs) for an iRODS object
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@XmlRootElement(name = "permissionListing")
public class PermissionListing {

	private List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>();

	/**
	 * Type of iRODS domain object (collection, data object, etc) that this
	 * metadata belongs to
	 */
	private CollectionAndDataObjectListingEntry.ObjectType objectType = ObjectType.UNKNOWN;

	private String absolutePathString = "";

	private boolean inheritance = false;

	/**
	 * @return the permissionEntries
	 */
	public List<PermissionEntry> getPermissionEntries() {
		return permissionEntries;
	}

	/**
	 * @param permissionEntries
	 *            the permissionEntries to set
	 */
	public void setPermissionEntries(List<PermissionEntry> permissionEntries) {
		this.permissionEntries = permissionEntries;
	}

	/**
	 * @return the objectType
	 */
	@XmlElement
	public CollectionAndDataObjectListingEntry.ObjectType getObjectType() {
		return objectType;
	}

	/**
	 * @param objectType
	 *            the objectType to set
	 */
	public void setObjectType(
			CollectionAndDataObjectListingEntry.ObjectType objectType) {
		this.objectType = objectType;
	}

	/**
	 * @return the absolutePathString
	 */
	@XmlElement
	public String getAbsolutePathString() {
		return absolutePathString;
	}

	/**
	 * @param absolutePathString
	 *            the absolutePathString to set
	 */
	public void setAbsolutePathString(String absolutePathString) {
		this.absolutePathString = absolutePathString;
	}

	/**
	 * @return the inheritance
	 */
	@XmlElement
	public boolean isInheritance() {
		return inheritance;
	}

	/**
	 * @param inheritance
	 *            the inheritance to set
	 */
	public void setInheritance(boolean inheritance) {
		this.inheritance = inheritance;
	}

}
