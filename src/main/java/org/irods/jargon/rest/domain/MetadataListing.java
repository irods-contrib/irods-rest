/**
 * 
 */
package org.irods.jargon.rest.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;

/**
 * Listing of metadata associated with an iRODS domain object
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@XmlRootElement(name = "metadataListing")
public class MetadataListing {

	private List<MetadataQueryResultEntry> metadataEntries = new ArrayList<MetadataQueryResultEntry>();

	/**
	 * Type of iRODS domain object (collection, data object, etc) that this
	 * metadata belongs to
	 */
	private CollectionAndDataObjectListingEntry.ObjectType objectType = ObjectType.UNKNOWN;

	/**
	 * Unique name that identifies the given object, for example, for a
	 * collection, this is the iRODS absolute path
	 */
	private String uniqueNameString = "";

	public MetadataListing() {
	}

	/**
	 * @return the metadataEntries
	 */
	public List<MetadataQueryResultEntry> getMetadataEntries() {
		return metadataEntries;
	}

	/**
	 * @param metadataEntries
	 *            the metadataEntries to set
	 */
	public void setMetadataEntries(
			final List<MetadataQueryResultEntry> metadataEntries) {
		this.metadataEntries = metadataEntries;
	}

	/**
	 * @return the objectType
	 */
	@XmlAttribute
	public CollectionAndDataObjectListingEntry.ObjectType getObjectType() {
		return objectType;
	}

	/**
	 * @param objectType
	 *            the objectType to set
	 */
	public void setObjectType(
			final CollectionAndDataObjectListingEntry.ObjectType objectType) {
		this.objectType = objectType;
	}

	/**
	 * @return the uniqueNameString
	 */
	@XmlElement
	public String getUniqueNameString() {
		return uniqueNameString;
	}

	/**
	 * @param uniqueNameString
	 *            the uniqueNameString to set
	 */
	public void setUniqueNameString(final String uniqueNameString) {
		this.uniqueNameString = uniqueNameString;
	}

}
