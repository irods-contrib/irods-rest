package org.irods.jargon.rest.domain;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;

/**
 * Value object that holds information on data objects and collections. This
 * object includes info to distinguish between data object and collection, to
 * identify it by path, and also information that can be used for paging.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@XmlRootElement(name = "fileListingEntry")
public class FileListingEntry {

	private String parentPath = "";
	private String pathOrName = "";
	private String specialObjectPath = "";
	private ObjectType objectType = null;
	private Date createdAt = null;
	private Date modifiedAt = null;
	private long dataSize = 0L;
	private String ownerName = "";
	private String ownerZone = "";
	private int id;
	private ObjStat.SpecColType specColType = SpecColType.NORMAL;

	/***
	 * Sequence number for this records based on query result
	 */
	private int count = 0;

	/**
	 * Is this the last result from the query or set
	 */
	private boolean lastResult = false;

	/**
	 * Total number of records for the given query. Note that this is not always
	 * available, depending on the iCAT database
	 */
	private int totalRecords = 0;

	/**
	 * Indicates whether this is the last result based on the query or listing
	 * operation. Many operations in Jargon produce a pageable result set, and
	 * methods are available to requery at an offset or contine paging results.
	 * 
	 * @return <code>boolean</code> that will be <code>true</code> if no more
	 *         results are available.
	 */
	@XmlAttribute
	public boolean isLastResult() {
		return lastResult;
	}

	/**
	 * Sets whether this is the last result from a query or listing operation
	 * 
	 * @param lastResult
	 *            <code>boolean</code> that indicates that this is the last
	 *            result for an operation
	 */
	public void setLastResult(final boolean lastResult) {
		this.lastResult = lastResult;
	}

	/**
	 * Get the sequence number in a set of results for this object.
	 * 
	 * @return <code>int</code> with a record sequence number that can be used
	 *         for setting offsets on subsequent queries.
	 */
	@XmlAttribute
	public int getCount() {
		return count;
	}

	/**
	 * Set a sequence number in a list of results for this object. This is used
	 * to handle paging when results are continued when listing or querying
	 * iRODS information.
	 * 
	 * @param count
	 *            <code>int</code> with a sequence number for this result within
	 *            a listing.
	 */
	public void setCount(final int count) {
		this.count = count;
	}

	/**
	 * Total number of records for the given query. Note that this is not always
	 * available, depending on the iCAT database
	 * 
	 * @return <code>int</code> with the total number of records that match this
	 *         query, not always available and otherwise zero
	 */
	@XmlAttribute
	public int getTotalRecords() {
		return totalRecords;
	}

	/**
	 * Total number of records for the given query. Note that this is not always
	 * available, depending on the iCAT database
	 * 
	 * @param totalRecords
	 *            <code>int</code> with the total number of records that match
	 *            this query, not always available and otherwise zero
	 */
	public void setTotalRecords(final int totalRecords) {
		this.totalRecords = totalRecords;
	}

	/**
	 * Return the absolute path the the parent of the file or collection.
	 * 
	 * @return <code>String</code> with the absolute path to the parent of the
	 *         file or collection.
	 */
	@XmlElement
	public String getParentPath() {
		return parentPath;
	}

	public void setParentPath(final String parentPath) {
		this.parentPath = parentPath;
	}

	/**
	 * Return the absolute path of the file or collection under the parent
	 * 
	 * @return <code>String</code> with the absolute path to the file or
	 *         collection under the parent.
	 */
	@XmlElement
	public String getPathOrName() {
		return pathOrName;
	}

	public void setPathOrName(final String pathOrName) {
		this.pathOrName = pathOrName;
	}

	/**
	 * Return an enum that differentiates between collection and data object
	 * 
	 * @return <code>ObjectType</code> enum value
	 */
	@XmlElement
	public ObjectType getObjectType() {
		return objectType;
	}

	public void setObjectType(final ObjectType objectType) {
		this.objectType = objectType;
	}

	@XmlElement
	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Date createdAt) {
		this.createdAt = createdAt;
	}

	@XmlElement
	public Date getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(final Date modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	@XmlElement
	public long getDataSize() {
		return dataSize;
	}

	public void setDataSize(final long dataSize) {
		this.dataSize = dataSize;
	}

	@XmlAttribute
	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof FileListingEntry)) {
			return false;
		}
		FileListingEntry otherEntry = (FileListingEntry) obj;
		return (otherEntry.parentPath.equals(parentPath) && otherEntry.pathOrName
				.equals(pathOrName));
	}

	@Override
	public int hashCode() {
		return parentPath.hashCode() + pathOrName.hashCode();
	}

	@Override
	public String toString() {
		String thisPath = pathOrName.substring(pathOrName.lastIndexOf('/') + 1);

		if (thisPath.isEmpty()) {
			thisPath = "/";
		}

		return thisPath;
	}

	@XmlElement
	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(final String ownerName) {
		this.ownerName = ownerName;
	}

	/**
	 * @return the ownerZone
	 */
	@XmlElement
	public String getOwnerZone() {
		return ownerZone;
	}

	/**
	 * @param ownerZone
	 *            the ownerZone to set
	 */
	public void setOwnerZone(final String ownerZone) {
		this.ownerZone = ownerZone;
	}

	/**
	 * @return the specColType {@link ObjStat.SpecColType} enum value that
	 *         indicates if this is some type of special collection
	 */
	@XmlAttribute
	public ObjStat.SpecColType getSpecColType() {
		return specColType;
	}

	/**
	 * @param specColType
	 *            the specColType to set {@link ObjStat.SpecColType} enum value
	 *            that indicates if this is some type of special collection
	 */
	public void setSpecColType(final ObjStat.SpecColType specColType) {
		this.specColType = specColType;
	}

	/**
	 * @return the specialObjectPath <code>String</code> with the underlyng
	 *         special object path. If this is a soft link, this reflects the
	 *         canonical iRODS path.
	 */
	@XmlElement
	public String getSpecialObjectPath() {
		return specialObjectPath;
	}

	/**
	 * @param specialObjectPath
	 *            the specialObjectPath to set <code>String</code> with the
	 *            underlyng special object path.
	 */
	public void setSpecialObjectPath(final String specialObjectPath) {
		this.specialObjectPath = specialObjectPath;
	}
}
