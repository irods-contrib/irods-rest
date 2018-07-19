/**
 * 
 */
package org.irods.jargon.rest.commands.dataobject;

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
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.rest.commands.AbstractIrodsService;
import org.irods.jargon.rest.domain.DataObjectData;
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
 * REST service for iRODS data objects. This is segmented from the file content
 * based operations which are found in the {@link FileContentsService}.
 * <p/>
 * The operations here catalog operations. In other words, a GET operation on a
 * <code>DataObjectService</code> will return JSON or XML with iRODS system
 * metadata about a file, while the GET operation on the
 * <code>FileContentsService</code> will download the file contents.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Named
@Path("/dataObject")
public class DataObjectService extends AbstractIrodsService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Retrieve information about a data object as xml or json.
	 * 
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * @param path
	 *            <code>String</code> with the iRODS absolute path derived from the
	 *            URL extra path information
	 * @return {@link DataObjectData} marshaled in the appropriate format.
	 * @throws JargonException
	 */
	@GET
	@Path("{path:.*}")
	@Produces({ "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public DataObjectData getDataObjectData(@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path) throws JargonException, FileNotFoundException {

		log.info("getDataObjectData()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);
			DataObjectAO dataObjectAO = getIrodsAccessObjectFactory().getDataObjectAO(irodsAccount);

			String decodedPathString = DataUtils.buildDecodedPathFromURLPathInfo(path, retrieveEncoding());
			log.info("decoded path:{}", decodedPathString);
			DataObject dataObject = dataObjectAO.findByAbsolutePath(decodedPathString);

			log.info("found dataObject, marshall the data:{}", dataObject);
			DataObjectData dataObjectData = DataObjectServiceUtils.buildDataObjectValuesFromIrodsData(dataObject);
			log.info("got data object data:{}", dataObjectData);

			return dataObjectData;
		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * Delete a data object (file) with or without force
	 * 
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * @param path
	 *            <code>String</code> with the iRODS absolute path derived from the
	 *            URL extra path information
	 * @param force
	 *            <code>boolean</code> that indicates whether the force option is
	 *            enabled on deletion
	 * @throws JargonException
	 */
	@DELETE
	@Path("{path:.*}")
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public void removeDataObject(@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path, @QueryParam("force") @DefaultValue("false") final boolean force)
			throws JargonException {

		log.info("removeDataObject()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);

			String decodedPathString = DataUtils.buildDecodedPathFromURLPathInfo(path, retrieveEncoding());

			IRODSFile dataFile = getIrodsAccessObjectFactory().getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(decodedPathString);

			log.info("removing directory at path:{}", dataFile.getAbsolutePath());

			if (force) {
				log.info("using force option...");
				dataFile.deleteWithForceOption();
			} else {
				log.info("not using force option...");
				dataFile.delete();
			}

			log.info("completed delete operation");

		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * Retrieve a representation of the ACLs associated with a data object
	 * 
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * @param path
	 *            <code>String</code> with the iRODS absolute path derived from the
	 *            URL extra path information
	 * @return
	 * @throws JargonException
	 */
	@GET
	@Path("{path:.*}/acl")
	@Produces({ "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public PermissionListing getDataObjectAcl(@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path) throws JargonException {

		log.info("getDataObjectAcl()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		try {
			String decodedPathString = DataUtils.buildDecodedPathFromURLPathInfo(path, retrieveEncoding());
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);

			DataObjectAclFunctions dataObjectAclFunctions = getServiceFunctionFactory()
					.instanceDataObjectAclFunctions(irodsAccount);
			return dataObjectAclFunctions.listPermissions(decodedPathString);

		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * @param path
	 *            <code>String</code> with the iRODS absolute path derived from the
	 *            URL extra path information
	 * @return
	 * @throws JargonException
	 */
	@GET
	@Path("{path:.*}/metadata")
	@Produces({ "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public MetadataListing getDataObjectMetadata(@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path) throws JargonException {

		log.info("getDataObjectMetadata()");

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

			log.info("listing metadata");
			DataObjectAvuFunctions dataObjectAvuFunctions = this.getServiceFunctionFactory()
					.instanceDataObjectAvuFunctions(irodsAccount);
			return dataObjectAvuFunctions.listDataObjectMetadata(decodedPathString);

		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * Do a bulk metadata add operation for the given data object. This takes a list
	 * of AVU entries in the PUT request body, and will attempt to add each AVU.
	 * <p/>
	 * A response body will log the disposition of each AVU add attempt, and any
	 * errors for an individual attempt are noted by the returned status and message
	 * for each entry. This allows partial success.
	 * 
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * @param path
	 *            <code>String</code> with the iRODS absolute path derived from the
	 *            URL extra path information
	 * @param metadataEntries
	 *            <code>List</code> of {@link MetadataQueryResultEntry} that is
	 *            derived from the request body
	 * @return response body derived from a <code>List</code> of
	 *         {@link MetadataOperationResultEntry}
	 * @throws JargonException
	 */
	@PUT
	@Path("{path:.*}/metadata")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
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

			log.info("marshalling into AvuData...");
			List<AvuData> avuDatas = new ArrayList<AvuData>();

			for (MetadataEntry metadataEntry : metadataOperation.getMetadataEntries()) {
				avuDatas.add(AvuData.instance(metadataEntry.getAttribute(), metadataEntry.getValue(),
						metadataEntry.getUnit()));
			}
			DataObjectAvuFunctions dataObjectAvuFunctions = getServiceFunctionFactory()
					.instanceDataObjectAvuFunctions(irodsAccount);
			return dataObjectAvuFunctions.addAvuMetadata(decodedPathString, avuDatas);

		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * Do a bulk metadata delete operation for the given data object. This takes a
	 * list of AVU entries in the POST request body, and will attempt to delete each
	 * AVU.
	 * <p/>
	 * A response body will log the disposition of each AVU delete attempt, and any
	 * errors for an individual attempt are noted by the returned status and message
	 * for each entry. This allows partial success.
	 * <p/>
	 * Note that this is an idempotent request, so that deletes of non-existent AVU
	 * data will be gracefully handed.
	 * <p/>
	 * A word of explanation is in order, given that the delete operation is
	 * accomplished with a POST HTTP verb. AVU data is free form and is often full
	 * of delimiters and slash characters, and of arbitrary size, making them
	 * unsuitable for inclusion in a URL, even in encoded form. For this reason, the
	 * operations are expressed by the included request body. HTTP DELETE verbs are
	 * ambiguous, but the consensus seems to be that DELETE verbs should not include
	 * a body, and are sometimes treated as a POST anyhow. So we had to fudge the
	 * 'pure' REST approach to accommodate the wide range of AVU data that exists.
	 * 
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * @param path
	 *            <code>String</code> with the iRODS absolute path derived from the
	 *            URL extra path information
	 * @param metadataEntries
	 *            <code>List</code> of {@link MetadataQueryResultEntry} that is
	 *            derived from the request body
	 * @return response body derived from a <code>List</code> of
	 *         {@link MetadataOperationResultEntry}
	 * @throws JargonException
	 */
	@POST
	@Path("{path:.*}/metadata")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public List<MetadataOperationResultEntry> deleteDataObjectMetadata(
			@HeaderParam("Authorization") final String authorization, @PathParam("path") final String path,
			final MetadataOperation metadataOperation) throws JargonException {

		log.info("deleteDataObjectMetadata()");

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
			DataObjectAO dataObjectAO = this.getIrodsAccessObjectFactory().getDataObjectAO(irodsAccount);

			log.info("marshalling into AvuData...");
			List<AvuData> avuDatas = new ArrayList<AvuData>();
			List<MetadataOperationResultEntry> metadataOperationResultEntries = new ArrayList<MetadataOperationResultEntry>();

			for (MetadataEntry metadataEntry : metadataOperation.getMetadataEntries()) {
				avuDatas.add(AvuData.instance(metadataEntry.getAttribute(), metadataEntry.getValue(),
						metadataEntry.getUnit()));
			}

			log.info("doing bulk delete operation");
			List<BulkAVUOperationResponse> bulkAVUOperationResponses = dataObjectAO
					.deleteBulkAVUMetadataFromDataObject(decodedPathString, avuDatas);
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
	 * Add a permission for a dataObject. This is done as a PUT, but this is
	 * idempotent, so it can be invoked against a data object that already has a
	 * permission for a user, effectively changing the permission.
	 * <p/>
	 * Note that this method returns void, there is no response body, and as such it
	 * should return an HTTP 204 code
	 * 
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * @param path
	 *            <code>String</code> with the absolute path to the iRODS dataObject
	 * @param userName
	 *            <code>String</code> with the user name, which can be in user,zone
	 *            format, or can be just the user name. Note the '#' character can
	 *            be mis-interpreted as an anchor in the path, so the delimiter
	 *            between user and zone should be a , (comma) character instead of
	 *            the pound '#' character
	 * @param permission
	 *            <code>String</code> of READ, WRITE, OWN, or NONE
	 * @throws InvalidUserException
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	@PUT
	@Path("{path:.*}/acl/{userName}")
	@Produces({ "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public void addDataObjectAcl(@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path, @PathParam("userName") final String userName,
			@QueryParam("permission") @DefaultValue("READ") final String permission)
			throws InvalidUserException, FileNotFoundException, JargonException {

		log.info("addDataObjectAcl()");

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

			DataObjectAclFunctions dataObjectAclFunctions = getServiceFunctionFactory()
					.instanceDataObjectAclFunctions(irodsAccount);
			log.info("adding permission");
			dataObjectAclFunctions.addPermission(decodedPath, myUserNameString, filePermissionEnumTranslationEnum);
			log.info("done");

		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * Delete a permission for a dataObject. This is done as an HTTP DELETE. The
	 * method is idempotent and can be invoked multiple times, silently ignoring an
	 * already-deleted permission.
	 * <p/>
	 * Note that this method returns void, there is no response body, and as such it
	 * should return an HTTP 204 code
	 * 
	 * @param authorization
	 *            <code>String</code> with the basic auth header
	 * @param path
	 *            <code>String</code> with the absolute path to the iRODS dataObject
	 * @param userName
	 *            <code>String</code> with the user name, which can be in user,zone
	 *            format, or can be just the user name. Note the '#' character can
	 *            be mis-interpreted as an anchor in the path, so the delimiter
	 *            between user and zone should be a , (comma) character instead of
	 *            the pound '#' character
	 * @throws InvalidUserException
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	@DELETE
	@Path("{path:.*}/acl/{userName}")
	@Produces({ "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public void deleteDataObjectAcl(@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path, @PathParam("userName") final String userName)
			throws InvalidUserException, FileNotFoundException, JargonException {

		log.info("deleteDataObjectAcl()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		String myUserNameString = userName.replace(',', '#');

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);

			String decodedPath = DataUtils.buildDecodedPathFromURLPathInfo(path, retrieveEncoding());

			DataObjectAclFunctions dataObjectAclFunctions = getServiceFunctionFactory()
					.instanceDataObjectAclFunctions(irodsAccount);
			log.info("deleting permission");
			dataObjectAclFunctions.deletePermissionForUser(decodedPath, myUserNameString);
			log.info("done");

		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}
}
