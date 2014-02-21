package org.irods.jargon.rest.commands.dataobject;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.rest.domain.MetadataListing;

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

}