/**
 * 
 */
package org.irods.jargon.rest.domain;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An instance of iRODS AVU metadata
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@XmlRootElement(name = "metadataEntry")
public class MetadataQueryResultEntry extends MetadataEntry {

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
	 * 
	 */
	public MetadataQueryResultEntry() {
	}

}
