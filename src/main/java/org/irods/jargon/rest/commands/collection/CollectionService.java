/**
 * 
 */
package org.irods.jargon.rest.commands.collection;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.BulkAVUOperationResponse;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.rest.commands.AbstractIrodsService;
import org.irods.jargon.rest.domain.CollectionData;
import org.irods.jargon.rest.domain.FileListingEntry;
import org.irods.jargon.rest.domain.MetadataEntry;
import org.irods.jargon.rest.domain.MetadataListing;
import org.irods.jargon.rest.domain.MetadataOperation;
import org.irods.jargon.rest.domain.MetadataOperationResultEntry;
import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Services for accessing iRODS Collections
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Named
@Path("/collection")
public class CollectionService extends AbstractIrodsService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Retrieve information about a collection, and optionally return a listing
	 * of data within the collection as xml or json.
	 * 
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * @param path
	 *            <code>String</code> with the iRODS absolute path derived from
	 *            the URL extra path information
	 * @param offset
	 *            <code>int</code> with an optional (default = 0) offset for any
	 *            listing
	 * @param isListing
	 *            <code>boolean</code> with an optional (default=false)
	 *            parameter that will cause a listing of collection children
	 * @return {@link CollectionData} marshaled in the appropriate format.
	 * @throws JargonException
	 */
	@GET
	@Path("{path:.*}")
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public CollectionData getCollectionData(
			@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path,
			@QueryParam("offset") @DefaultValue("0") final int offset,
			@QueryParam("listing") @DefaultValue("false") final boolean isListing)
			throws JargonException {

		log.info("getCollectionData()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);
			CollectionAO collectionAO = getIrodsAccessObjectFactory()
					.getCollectionAO(irodsAccount);
			// log.info("looking up collection with URI:{}", uri);

			StringBuilder sBuilder = new StringBuilder();
			sBuilder.append('/');
			sBuilder.append(path);

			Collection collection = collectionAO.findByAbsolutePath(sBuilder
					.toString());

			log.info("found collection, marshall the data:{}", collection);
			CollectionData collectionData = new CollectionData();
			collectionData.setCollectionId(collection.getCollectionId());
			collectionData.setCollectionInheritance(collection
					.getCollectionInheritance());
			collectionData.setCollectionMapId(collection.getCollectionMapId());
			collectionData.setCollectionName(collection.getCollectionName());
			collectionData.setCollectionOwnerName(collection
					.getCollectionOwnerName());
			collectionData.setCollectionOwnerZone(collection
					.getCollectionOwnerZone());
			collectionData.setCollectionParentName(collection
					.getCollectionParentName());
			collectionData.setComments(collection.getComments());
			collectionData.setCreatedAt(collection.getCreatedAt());
			collectionData.setInfo1(collection.getInfo1());
			collectionData.setInfo2(collection.getInfo2());
			collectionData.setObjectPath(collection.getObjectPath());
			collectionData.setModifiedAt(collection.getModifiedAt());
			collectionData.setSpecColType(collection.getSpecColType());
			log.info("collectionData:{}", collectionData);

			// if listing, then get children based on given offset
			if (isListing) {
				log.info("add listing with offset at:{}", offset);
				CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = getIrodsAccessObjectFactory()
						.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
				FileListingEntry fileListingEntry;

				for (CollectionAndDataObjectListingEntry entry : collectionAndDataObjectListAndSearchAO
						.listCollectionsUnderPath(collection.getAbsolutePath(),
								offset)) {
					fileListingEntry = new FileListingEntry();
					fileListingEntry.setCount(entry.getCount());
					fileListingEntry.setCreatedAt(entry.getCreatedAt());
					fileListingEntry.setDataSize(entry.getDataSize());
					fileListingEntry.setId(entry.getId());
					fileListingEntry.setLastResult(entry.isLastResult());
					fileListingEntry.setModifiedAt(entry.getModifiedAt());
					fileListingEntry.setObjectType(entry.getObjectType());
					fileListingEntry.setOwnerName(entry.getOwnerName());
					fileListingEntry.setOwnerZone(entry.getOwnerZone());
					fileListingEntry.setParentPath(entry.getParentPath());
					fileListingEntry.setPathOrName(entry.getPathOrName());
					fileListingEntry.setSpecColType(entry.getSpecColType());
					fileListingEntry.setSpecialObjectPath(entry
							.getSpecialObjectPath());
					fileListingEntry.setTotalRecords(entry.getTotalRecords());
					collectionData.getChildren().add(fileListingEntry);

				}
				log.info("listing added...");
			}
			return collectionData;
		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * Add a new collection (mkdir), based on an HTTP PUT operation using the
	 * provided path information.
	 * <p/>
	 * Note that this operation is idempotent and can be invoked more than once.
	 * If a directory already exists, it will be silently ignored.
	 * <p/>
	 * This method will return basic metadata about the collection that was to
	 * be created, as in the <code>getCollectionData</code> method.
	 * 
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * @param path
	 *            <code>String</code> with the iRODS absolute path derived from
	 *            the URL extra path information
	 * @return {@link CollectionData} marshaled in the appropriate format.
	 * @throws JargonException
	 */
	@PUT
	@Path("{path:.*}")
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public CollectionData addCollection(
			@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path) throws JargonException {

		log.info("addCollection()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);
			CollectionAO collectionAO = getIrodsAccessObjectFactory()
					.getCollectionAO(irodsAccount);

			StringBuilder sBuilder = new StringBuilder();
			sBuilder.append('/');
			sBuilder.append(path);

			IRODSFile collectionFile = this.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(sBuilder.toString());

			log.info("making directory at path:{}",
					collectionFile.getAbsolutePath());
			collectionFile.mkdirs();

			log.info("dirs created, get data about collection for response...");

			Collection collection = collectionAO.findByAbsolutePath(sBuilder
					.toString());

			log.info("found collection, marshall the data:{}", collection);
			CollectionData collectionData = new CollectionData();
			collectionData.setCollectionId(collection.getCollectionId());
			collectionData.setCollectionInheritance(collection
					.getCollectionInheritance());
			collectionData.setCollectionMapId(collection.getCollectionMapId());
			collectionData.setCollectionName(collection.getCollectionName());
			collectionData.setCollectionOwnerName(collection
					.getCollectionOwnerName());
			collectionData.setCollectionOwnerZone(collection
					.getCollectionOwnerZone());
			collectionData.setCollectionParentName(collection
					.getCollectionParentName());
			collectionData.setComments(collection.getComments());
			collectionData.setCreatedAt(collection.getCreatedAt());
			collectionData.setInfo1(collection.getInfo1());
			collectionData.setInfo2(collection.getInfo2());
			collectionData.setObjectPath(collection.getObjectPath());
			collectionData.setModifiedAt(collection.getModifiedAt());
			collectionData.setSpecColType(collection.getSpecColType());
			log.info("collectionData:{}", collectionData);

			return collectionData;
		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * @param path
	 *            <code>String</code> with the iRODS absolute path derived from
	 *            the URL extra path information
	 * @return
	 * @throws JargonException
	 */
	@GET
	@Path("{path:.*}/metadata")
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public MetadataListing getCollectionMetadata(
			@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path) throws JargonException {

		log.info("getCollectionMetadata()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		StringBuilder sb = new StringBuilder();
		sb.append('/');
		sb.append(path);

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);
			CollectionAO collectionAO = getIrodsAccessObjectFactory()
					.getCollectionAO(irodsAccount);

			log.info("listing metadata");
			List<MetadataEntry> metadataEntries = new ArrayList<MetadataEntry>();
			try {
				List<MetaDataAndDomainData> metadataList = collectionAO
						.findMetadataValuesForCollection(sb.toString());

				MetadataEntry entry;
				for (MetaDataAndDomainData metadata : metadataList) {
					entry = new MetadataEntry();
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
				metadataListing.setObjectType(ObjectType.COLLECTION);
				metadataListing.setUniqueNameString(path);
				return metadataListing;

			} catch (JargonQueryException e) {
				throw new JargonException("cannot query metadata", e);
			}

		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * Do a bulk metadata add operation for the given collection. This takes a
	 * list of AVU entries in the PUT request body, and will attempt to add each
	 * AVU.
	 * <p/>
	 * A response body will log the disposition of each AVU add attempt, and any
	 * errors for an individual attempt are noted by the returned status and
	 * message for each entry. This allows partial success.
	 * 
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * @param path
	 *            <code>String</code> with the iRODS absolute path derived from
	 *            the URL extra path information
	 * @param metadataEntries
	 *            <code>List</code> of {@link MetadataEntry} that is derived
	 *            from the request body
	 * @return response body derived from a <code>List</code> of
	 *         {@link MetadataOperationResultEntry}
	 * @throws JargonException
	 */
	@PUT
	@Path("{path:.*}/metadata")
	@Consumes({ "application/xml", "application/json" })
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public List<MetadataOperationResultEntry> addCollectionMetadata(
			@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path,
			final MetadataOperation metadataOperation) throws JargonException {

		log.info("addCollectionMetadata()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		if (metadataOperation == null) {
			throw new IllegalArgumentException("null metadataOperation");
		}

		StringBuilder sb = new StringBuilder();
		sb.append('/');
		sb.append(path);

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);
			CollectionAO collectionAO = getIrodsAccessObjectFactory()
					.getCollectionAO(irodsAccount);

			log.info("marshalling into AvuData...");
			List<AvuData> avuDatas = new ArrayList<AvuData>();
			List<MetadataOperationResultEntry> metadataOperationResultEntries = new ArrayList<MetadataOperationResultEntry>();

			for (MetadataEntry metadataEntry : metadataOperation
					.getMetadataEntries()) {
				avuDatas.add(AvuData.instance(metadataEntry.getAttribute(),
						metadataEntry.getValue(), metadataEntry.getUnit()));
			}

			log.info("doing bulk add operation");
			List<BulkAVUOperationResponse> bulkAVUOperationResponses = collectionAO
					.addBulkAVUMetadataToCollection(sb.toString(), avuDatas);
			log.info("responses:{}", bulkAVUOperationResponses);

			log.info("marshalling response into rest domain...");
			MetadataOperationResultEntry resultEntry;
			for (BulkAVUOperationResponse response : bulkAVUOperationResponses) {
				resultEntry = new MetadataOperationResultEntry();
				resultEntry.setAttributeString(response.getAvuData()
						.getAttribute());
				resultEntry.setMessage(response.getMessage());
				resultEntry.setResultStatus(response.getResultStatus());
				resultEntry.setUnit(response.getAvuData().getUnit());
				resultEntry.setValueString(response.getAvuData().getValue());
				metadataOperationResultEntries.add(resultEntry);
				log.info("result entry added:{}", resultEntry);
			}
			log.info("complete...");
			return metadataOperationResultEntries;

		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}
}
