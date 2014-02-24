package org.irods.jargon.rest.commands.dataobject;

import java.util.List;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.rest.domain.MetadataListing;
import org.irods.jargon.rest.domain.MetadataOperationResultEntry;

/**
 * Describes business methods behind the data object REST service wrapper
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface DataObjectAvuFunctions {

	/**
	 * Generate a metadata listing for the given data object
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to a data object
	 * @return {@link MetadataListing} which is a representation of data object
	 *         AVU data
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	MetadataListing listDataObjectMetadata(String absolutePath)
			throws FileNotFoundException, JargonException;

	/**
	 * Given a list of desired AVUs, associate them with the data object by
	 * adding. Note that each AVU is added separately, and an entry will be
	 * added in the response for success or failure. In this way, duplicates do
	 * not throw and exception, rather, they are returned with an invalid
	 * status.
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to a data object
	 * @param avuData
	 *            <code>List</code> of {@link AvuData} to be added to the data
	 *            object
	 * @return <code>List</code> of {@link MetadataOperationResultEntry} with
	 *         the outcome of each AVU add
	 * @throws FileNotFoundException
	 *             if the data object is missing
	 * @throws JargonException
	 */
	List<MetadataOperationResultEntry> addAvuMetadata(
			final String absolutePath, final List<AvuData> avuData)
			throws FileNotFoundException, JargonException;

	/**
	 * Given a list of desired AVUs, delete them from the data object. Note that
	 * each AVU is deleted separately, and an entry will be added in the
	 * response for success or failure. An individual AVU that is missing during
	 * delete will be silently ignored.
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the absolute path to a data object
	 * @param avuData
	 *            <code>List</code> of {@link AvuData} to be deleted from the
	 *            data object
	 * @return <code>List</code> of {@link MetadataOperationResultEntry} with
	 *         the outcome of each AVU delete
	 * @throws FileNotFoundException
	 *             if the data object is missing
	 * @throws JargonException
	 */
	List<MetadataOperationResultEntry> deleteAvuMetadata(
			final String absolutePath, final List<AvuData> avuData)
			throws FileNotFoundException, JargonException;

}