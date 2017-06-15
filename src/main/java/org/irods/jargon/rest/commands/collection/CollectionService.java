/**
 * 
 */
package org.irods.jargon.rest.commands.collection;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
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
import org.irods.jargon.rest.domain.MetadataQueryResultEntry;
import org.irods.jargon.rest.domain.PermissionListing;
import org.irods.jargon.rest.utils.DataUtils;
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
	 * @param listingType
	 *            <code>String</code> that should be 'both', 'data',
	 *            'collections', indicating what sort of child data to return
	 * @return {@link CollectionData} marshaled in the appropriate format.
	 * 
	 * @throws JargonException
	 */
	@GET
	@Path("{path:.*}")
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public CollectionData getCollectionData(@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path, @QueryParam("offset") @DefaultValue("0") final int offset,
			@QueryParam("listing") @DefaultValue("false") final boolean isListing,
			@QueryParam("listType") @DefaultValue("both") final String listingType) throws JargonException {

		log.info("getCollectionData()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);
			CollectionAO collectionAO = getIrodsAccessObjectFactory().getCollectionAO(irodsAccount);
			// log.info("looking up collection with URI:{}", uri);

			String decodedPathString = DataUtils.buildDecodedPathFromURLPathInfo(path, retrieveEncoding());
			log.info("decoded path:{}", decodedPathString);
			Collection collection = collectionAO.findByAbsolutePath(decodedPathString);

			log.info("found collection, marshall the data:{}", collection);
			CollectionData collectionData = new CollectionData();
			collectionData.setCollectionId(collection.getCollectionId());
			collectionData.setCollectionInheritance(collection.getCollectionInheritance());
			collectionData.setCollectionMapId(collection.getCollectionMapId());
			collectionData.setCollectionName(collection.getCollectionName());
			collectionData.setCollectionOwnerName(collection.getCollectionOwnerName());
			collectionData.setCollectionOwnerZone(collection.getCollectionOwnerZone());
			collectionData.setCollectionParentName(collection.getCollectionParentName());
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
				List<CollectionAndDataObjectListingEntry> entries;

				if (listingType.equals("both")) {
					log.info("listing colls and data objects");
					entries = collectionAndDataObjectListAndSearchAO
							.listDataObjectsAndCollectionsUnderPath(collection.getAbsolutePath());
				} else if (listingType.equals("data")) {
					log.info("listing data objects");
					entries = collectionAndDataObjectListAndSearchAO
							.listDataObjectsUnderPath(collection.getAbsolutePath(), offset);
				} else if (listingType.equals("collections")) {
					log.info("listing collections");
					entries = collectionAndDataObjectListAndSearchAO
							.listCollectionsUnderPath(collection.getAbsolutePath(), offset);

				} else {
					throw new IllegalArgumentException("invalid listing type, should be both, collections, data");
				}

				for (CollectionAndDataObjectListingEntry entry : entries) {
					log.info("adding entry:{}", entry);
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
					fileListingEntry.setSpecialObjectPath(entry.getSpecialObjectPath());
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
	public CollectionData addCollection(@HeaderParam("Authorization") final String authorization,
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
			CollectionAO collectionAO = getIrodsAccessObjectFactory().getCollectionAO(irodsAccount);

			String decodedPath = DataUtils.buildDecodedPathFromURLPathInfo(path, retrieveEncoding());

			IRODSFile collectionFile = getIrodsAccessObjectFactory().getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(decodedPath);

			log.info("making directory at path:{}", collectionFile.getAbsolutePath());
			collectionFile.mkdirs();

			log.info("dirs created, get data about collection for response...");

			Collection collection = collectionAO.findByAbsolutePath(decodedPath);

			log.info("found collection, marshall the data:{}", collection);
			CollectionData collectionData = new CollectionData();
			collectionData.setCollectionId(collection.getCollectionId());
			collectionData.setCollectionInheritance(collection.getCollectionInheritance());
			collectionData.setCollectionMapId(collection.getCollectionMapId());
			collectionData.setCollectionName(collection.getCollectionName());
			collectionData.setCollectionOwnerName(collection.getCollectionOwnerName());
			collectionData.setCollectionOwnerZone(collection.getCollectionOwnerZone());
			collectionData.setCollectionParentName(collection.getCollectionParentName());
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
	// @Mapped(namespaceMap = { @XmlNsMap(namespace =
	// "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public MetadataListing getCollectionMetadata(@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path) throws JargonException {

		log.info("getCollectionMetadata()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		String decodedPathString = DataUtils.buildDecodedPathFromURLPathInfo(path, retrieveEncoding());

		try {
			log.error("decoded path:{}", decodedPathString);
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);
			CollectionAO collectionAO = getIrodsAccessObjectFactory().getCollectionAO(irodsAccount);

			log.info("listing metadata");
			List<MetadataQueryResultEntry> metadataEntries = new ArrayList<>();
			try {
				List<MetaDataAndDomainData> metadataList = collectionAO
						.findMetadataValuesForCollection(decodedPathString);

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
	 * Do a bulk metadata delete operation for the given collection. This takes
	 * a list of AVU entries in the POST request body, and will attempt to
	 * delete each AVU.
	 * <p/>
	 * A response body will log the disposition of each AVU delete attempt, and
	 * any errors for an individual attempt are noted by the returned status and
	 * message for each entry. This allows partial success.
	 * <p/>
	 * Note that this is an idempotent request, so that deletes of non-existent
	 * AVU data will be gracefully handed.
	 * <p/>
	 * A word of explanation is in order, given that the delete operation is
	 * accomplished with a POST HTTP verb. AVU data is free form and is often
	 * full of delimiters and slash characters, and of arbitrary size, making
	 * them unsuitable for inclusion in a URL, even in encoded form. For this
	 * reason, the operations are expressed by the included request body. HTTP
	 * DELETE verbs are ambiguous, but the consensus seems to be that DELETE
	 * verbs should not include a body, and are sometimes treated as a POST
	 * anyhow. So we had to fudge the 'pure' REST approach to accommodate the
	 * wide range of AVU data that exists.
	 * 
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * @param path
	 *            <code>String</code> with the iRODS absolute path derived from
	 *            the URL extra path information
	 * @param metadataEntries
	 *            <code>List</code> of {@link MetadataQueryResultEntry} that is
	 *            derived from the request body
	 * @return response body derived from a <code>List</code> of
	 *         {@link MetadataOperationResultEntry}
	 * @throws JargonException
	 */
	@POST
	@Path("{path:.*}/metadata")
	@Consumes({ "application/xml", "application/json" })
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public List<MetadataOperationResultEntry> deleteCollectionMetadata(
			@HeaderParam("Authorization") final String authorization, @PathParam("path") final String path,
			final MetadataOperation metadataOperation) throws JargonException {

		log.info("deleteCollectionMetadata()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		if (metadataOperation == null) {
			throw new IllegalArgumentException("null metadataOperation");
		}

		String decodedPathString = DataUtils.buildDecodedPathFromURLPathInfo(path, retrieveEncoding());

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);
			CollectionAO collectionAO = getIrodsAccessObjectFactory().getCollectionAO(irodsAccount);

			log.info("marshalling into AvuData...");
			List<AvuData> avuDatas = new ArrayList<>();
			List<MetadataOperationResultEntry> metadataOperationResultEntries = new ArrayList<>();

			for (MetadataEntry metadataEntry : metadataOperation.getMetadataEntries()) {
				avuDatas.add(AvuData.instance(metadataEntry.getAttribute(), metadataEntry.getValue(),
						metadataEntry.getUnit()));
			}

			log.info("doing bulk delete operation");
			List<BulkAVUOperationResponse> bulkAVUOperationResponses = collectionAO
					.deleteBulkAVUMetadataFromCollection(decodedPathString, avuDatas);
			log.info("responses:{}", bulkAVUOperationResponses);

			log.info("marshalling response into rest domain...");
			MetadataOperationResultEntry resultEntry;
			for (BulkAVUOperationResponse response : bulkAVUOperationResponses) {
				resultEntry = new MetadataOperationResultEntry();
				resultEntry.setAttributeString(response.getAvuData().getAttribute());
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
	 *            <code>List</code> of {@link MetadataQueryResultEntry} that is
	 *            derived from the request body
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
			@HeaderParam("Authorization") final String authorization, @PathParam("path") final String path,
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

		String decodedPathString = DataUtils.buildDecodedPathFromURLPathInfo(path, retrieveEncoding());

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);
			CollectionAO collectionAO = getIrodsAccessObjectFactory().getCollectionAO(irodsAccount);

			log.info("marshalling into AvuData...");
			List<AvuData> avuDatas = new ArrayList<>();
			List<MetadataOperationResultEntry> metadataOperationResultEntries = new ArrayList<>();

			for (MetadataEntry metadataEntry : metadataOperation.getMetadataEntries()) {
				avuDatas.add(AvuData.instance(metadataEntry.getAttribute(), metadataEntry.getValue(),
						metadataEntry.getUnit()));
			}

			log.info("doing bulk add operation");
			List<BulkAVUOperationResponse> bulkAVUOperationResponses = collectionAO
					.addBulkAVUMetadataToCollection(decodedPathString, avuDatas);
			log.info("responses:{}", bulkAVUOperationResponses);

			log.info("marshalling response into rest domain...");
			MetadataOperationResultEntry resultEntry;
			for (BulkAVUOperationResponse response : bulkAVUOperationResponses) {
				resultEntry = new MetadataOperationResultEntry();
				resultEntry.setAttributeString(response.getAvuData().getAttribute());
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

	/**
	 * Delete the given collection. This is equivalent to a rmdir command. A
	 * parameter is available to do the operation with the force option enabled.
	 * <p/>
	 * This is an idempotent method, and if there is not a diretory to delete,
	 * it will silently ignore this.
	 * <p/>
	 * Note that there is no need to return a body, so this method will return
	 * an HTTP 204 with no body information
	 * 
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * @param path
	 *            <code>String</code> with the iRODS absolute path derived from
	 *            the URL extra path information
	 * @param force
	 *            <code>boolean</code> that indicates whether the force option
	 *            is enabled on deletion
	 * @throws JargonException
	 */
	@DELETE
	@Path("{path:.*}")
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public void removeCollection(@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path, @QueryParam("force") @DefaultValue("false") final boolean force)
			throws JargonException {

		log.info("removeCollection()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);

			String decodedPathString = DataUtils.buildDecodedPathFromURLPathInfo(path, retrieveEncoding());

			IRODSFile collectionFile = getIrodsAccessObjectFactory().getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(decodedPathString);

			log.info("removing directory at path:{}", collectionFile.getAbsolutePath());

			if (force) {
				log.info("using force option...");
				collectionFile.deleteWithForceOption();
			} else {
				log.info("not using force option...");
				collectionFile.delete();
			}

			log.info("completed delete operation");

		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * Retrieve a representation of the ACLs associated with a collection
	 * 
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * @param path
	 *            <code>String</code> with the iRODS absolute path derived from
	 *            the URL extra path information
	 * @return
	 * @throws JargonException
	 */
	@GET
	@Path("{path:.*}/acl")
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public PermissionListing getCollectionAcl(@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path) throws FileNotFoundException, JargonException {

		log.info("getCollectionAcl()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		try {
			String decodedPathString = DataUtils.buildDecodedPathFromURLPathInfo(path, retrieveEncoding());
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);

			CollectionAclFunctions collectionAclFunctions = getServiceFunctionFactory()
					.instanceCollectionAclFunctions(irodsAccount);
			return collectionAclFunctions.listPermissions(decodedPathString);

		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * Add a permission for a collection. This is done as a PUT, but this is
	 * idempotent, so it can be invoked against a collection that already has a
	 * permission for a user.
	 * <p/>
	 * Note that this method returns void, there is no response body, and as
	 * such it should return an HTTP 204 code
	 * 
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * @param path
	 *            <code>String</code> with the absolute path to the iRODS
	 *            collection
	 * @param userName
	 *            <code>String</code> with the user name, which can be in
	 *            user,zone format, or can be just the user name. Note the '#'
	 *            character can be mis-interpreted as an anchor in the path, so
	 *            the delimiter between user and zone should be a , (comma)
	 *            character instead of the pound '#' character
	 * @param recursive
	 *            <code>boolean</code> that indicates the permission should be
	 *            set recursively
	 * @param permission
	 *            <code>String</code> of READ, WRITE, OWN, or NONE
	 * @throws InvalidUserException
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	@PUT
	@Path("{path:.*}/acl/{userName}")
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public void addCollectionAcl(@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path, @PathParam("userName") final String userName,
			@QueryParam("recursive") @DefaultValue("false") final boolean recursive,
			@QueryParam("permission") @DefaultValue("READ") final String permission)
			throws InvalidUserException, FileNotFoundException, JargonException {

		log.info("addCollectionAcl()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (permission == null || permission.isEmpty()) {
			throw new IllegalArgumentException("null or empty permission");
		}

		FilePermissionEnum filePermissionEnumTranslationEnum = FilePermissionEnum.NULL;

		if (permission.equals("READ")) {
			filePermissionEnumTranslationEnum = FilePermissionEnum.READ;
		} else if (permission.equals("WRITE")) {
			filePermissionEnumTranslationEnum = FilePermissionEnum.WRITE;
		} else if (permission.equals("OWN")) {
			filePermissionEnumTranslationEnum = FilePermissionEnum.OWN;
		} else if (permission.equals("NONE")) {
			filePermissionEnumTranslationEnum = FilePermissionEnum.NONE;
		} else {
			throw new IllegalArgumentException("unknown permission type:" + permission);
		}

		String myUserNameString = userName.replace(',', '#');

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);

			String decodedPath = DataUtils.buildDecodedPathFromURLPathInfo(path, retrieveEncoding());

			CollectionAclFunctions collectionAclFunctions = getServiceFunctionFactory()
					.instanceCollectionAclFunctions(irodsAccount);
			log.info("adding permission");
			collectionAclFunctions.addPermission(decodedPath, myUserNameString, filePermissionEnumTranslationEnum,
					recursive);
			log.info("done");

		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * Add a permission for a collection. This is done as an HTTP DELETE
	 * <p/>
	 * Note that this method returns void, and as a DELETE there is no response
	 * body. The http invocation will return an HTTP 204 code
	 * 
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * @param path
	 *            <code>String</code> with the absolute path to the iRODS
	 *            collection
	 * @param userName
	 *            <code>String</code> with the user name, which can be in
	 *            user,zone format, or can be just the user name. Note the '#'
	 *            character can be mis-interpreted as an anchor in the path, so
	 *            the delimiter between user and zone should be a , (comma)
	 *            character instead of the pound '#' character
	 * @param recursive
	 *            <code>boolean</code> that indicates the permission should be
	 *            set recursively
	 * @throws InvalidUserException
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	@DELETE
	@Path("{path:.*}/acl/{userName}")
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public void deleteCollectionAcl(@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path, @PathParam("userName") final String userName,
			@QueryParam("recursive") @DefaultValue("false") final boolean recursive)
			throws InvalidUserException, FileNotFoundException, JargonException {

		log.info("deleteCollectionAcl()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		/*
		 * User name comes in delim with , instead of # between user and zone so
		 * as not to be misinterpreted as an anchor
		 */
		String myUserNameString = userName.replace(',', '#');

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);

			String decodedPath = DataUtils.buildDecodedPathFromURLPathInfo(path, retrieveEncoding());

			CollectionAclFunctions collectionAclFunctions = getServiceFunctionFactory()
					.instanceCollectionAclFunctions(irodsAccount);
			log.info("removing permission");
			collectionAclFunctions.deletePermissionForUser(decodedPath, myUserNameString, recursive);
			log.info("done");

		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

}
