/**
 * 
 */
package org.irods.jargon.rest.commands.dataobject;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.BulkAVUOperationResponse;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.rest.commands.AbstractServiceFunction;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.irods.jargon.rest.domain.MetadataListing;
import org.irods.jargon.rest.domain.MetadataOperationResultEntry;
import org.irods.jargon.rest.domain.MetadataQueryResultEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service functions (business services) behind the Data Object REST service
 * facade.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DataObjectAvuFunctionsImpl extends AbstractServiceFunction
		implements DataObjectAvuFunctions {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * @param restConfiguration
	 * @param irodsAccount
	 * @param irodsAccessObjectFactory
	 */
	public DataObjectAvuFunctionsImpl(RestConfiguration restConfiguration,
			IRODSAccount irodsAccount,
			IRODSAccessObjectFactory irodsAccessObjectFactory) {
		super(restConfiguration, irodsAccount, irodsAccessObjectFactory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.rest.commands.dataobject.DataObjectAvuFunctions#
	 * listDataObjectMetadata(java.lang.String)
	 */
	@Override
	public MetadataListing listDataObjectMetadata(final String absolutePath)
			throws FileNotFoundException, JargonException {

		DataObjectAO dataObectAO = getIrodsAccessObjectFactory()
				.getDataObjectAO(this.getIrodsAccount());

		log.info("listing metadata");
		List<MetadataQueryResultEntry> metadataEntries = new ArrayList<MetadataQueryResultEntry>();
		List<MetaDataAndDomainData> metadataList = dataObectAO
				.findMetadataValuesForDataObject(absolutePath);

		MetadataQueryResultEntry entry;
		for (MetaDataAndDomainData metadata : metadataList) {
			entry = new MetadataQueryResultEntry();
			entry.setCount(metadata.getCount());
			entry.setLastResult(metadata.isLastResult());
			entry.setTotalRecords(metadata.getTotalRecords());
			entry.setAttribute(metadata.getAvuAttribute());
			entry.setValue(metadata.getAvuValue());
			entry.setUnit(metadata.getAvuUnit());
			metadataEntries.add(entry);
		}

		log.info("built response");

		MetadataListing metadataListing = new MetadataListing();
		metadataListing.setMetadataEntries(metadataEntries);
		metadataListing.setObjectType(ObjectType.DATA_OBJECT);
		metadataListing.setUniqueNameString(absolutePath);
		return metadataListing;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.rest.commands.dataobject.DataObjectAvuFunctions#
	 * deleteAvuMetadata(java.lang.String, java.util.List)
	 */
	@Override
	public List<MetadataOperationResultEntry> deleteAvuMetadata(
			final String absolutePath, final List<AvuData> avuData)
			throws FileNotFoundException, JargonException {

		log.info("deleteAvuMetadata()");

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty iRODS absolute path");
		}

		if (avuData == null) {
			throw new IllegalArgumentException(
					"null or empty metadataOperation");
		}

		log.info("absolutePath:{}", absolutePath);

		DataObjectAO dataObjectAO = this.getIrodsAccessObjectFactory()
				.getDataObjectAO(getIrodsAccount());

		List<MetadataOperationResultEntry> metadataOperationResultEntries = new ArrayList<MetadataOperationResultEntry>();

		List<BulkAVUOperationResponse> responses = dataObjectAO
				.deleteBulkAVUMetadataFromDataObject(absolutePath, avuData);

		log.info("responses:{}", responses);

		log.info("marshalling response into rest domain...");
		MetadataOperationResultEntry resultEntry;
		for (BulkAVUOperationResponse response : responses) {
			resultEntry = new MetadataOperationResultEntry();
			resultEntry
					.setAttributeString(response.getAvuData().getAttribute());
			resultEntry.setMessage(response.getMessage());
			resultEntry.setResultStatus(response.getResultStatus());
			resultEntry.setUnit(response.getAvuData().getUnit());
			resultEntry.setValueString(response.getAvuData().getValue());
			metadataOperationResultEntries.add(resultEntry);
			log.info("result entry added:{}", resultEntry);
		}
		log.info("complete...");
		return metadataOperationResultEntries;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.rest.commands.dataobject.DataObjectAvuFunctions#
	 * addAvuMetadata(java.lang.String, java.util.List)
	 */
	@Override
	public List<MetadataOperationResultEntry> addAvuMetadata(
			final String absolutePath, final List<AvuData> avuData)
			throws FileNotFoundException, JargonException {

		log.info("addAvuMetadata()");

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty iRODS absolute path");
		}

		if (avuData == null) {
			throw new IllegalArgumentException(
					"null or empty metadataOperation");
		}

		log.info("absolutePath:{}", absolutePath);

		DataObjectAO dataObjectAO = this.getIrodsAccessObjectFactory()
				.getDataObjectAO(getIrodsAccount());

		List<MetadataOperationResultEntry> metadataOperationResultEntries = new ArrayList<MetadataOperationResultEntry>();

		List<BulkAVUOperationResponse> responses = dataObjectAO
				.addBulkAVUMetadataToDataObject(absolutePath, avuData);

		log.info("responses:{}", responses);

		log.info("marshalling response into rest domain...");
		MetadataOperationResultEntry resultEntry;
		for (BulkAVUOperationResponse response : responses) {
			resultEntry = new MetadataOperationResultEntry();
			resultEntry
					.setAttributeString(response.getAvuData().getAttribute());
			resultEntry.setMessage(response.getMessage());
			resultEntry.setResultStatus(response.getResultStatus());
			resultEntry.setUnit(response.getAvuData().getUnit());
			resultEntry.setValueString(response.getAvuData().getValue());
			metadataOperationResultEntries.add(resultEntry);
			log.info("result entry added:{}", resultEntry);
		}
		log.info("complete...");
		return metadataOperationResultEntries;

	}
}
