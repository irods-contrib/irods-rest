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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
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
	 *            <code>String</code> with the iRODS absolute path derived from
	 *            the URL extra path information
	 * @return {@link DataObjectData} marshaled in the appropriate format.
	 * @throws JargonException
	 */
	@GET
	@Path("{path:.*}")
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public DataObjectData getDataObjectData(
			@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path) throws JargonException,
			FileNotFoundException {

		log.info("getDataObjectData()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);
			DataObjectAO dataObjectAO = getIrodsAccessObjectFactory()
					.getDataObjectAO(irodsAccount);

			String decodedPathString = DataUtils
					.buildDecodedPathFromURLPathInfo(path, retrieveEncoding());
			log.info("decoded path:{}", decodedPathString);
			DataObject dataObject = dataObjectAO
					.findByAbsolutePath(decodedPathString);

			log.info("found dataObject, marshall the data:{}", dataObject);
			DataObjectData dataObjectData = DataObjectServiceUtils
					.buildDataObjectValuesFromIrodsData(dataObject);
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
	public void removeDataObject(
			@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path,
			@QueryParam("force") @DefaultValue("false") final boolean force)
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

			String decodedPathString = DataUtils
					.buildDecodedPathFromURLPathInfo(path, retrieveEncoding());

			IRODSFile dataFile = getIrodsAccessObjectFactory()
					.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
							decodedPathString);

			log.info("removing directory at path:{}",
					dataFile.getAbsolutePath());

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
	 *            <code>String</code> with the iRODS absolute path derived from
	 *            the URL extra path information
	 * @return
	 * @throws JargonException
	 */
	@GET
	@Path("{path:.*}/acl")
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public PermissionListing getDataObjectAcl(
			@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path) throws JargonException {

		log.info("getDataObjectAcl()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		try {
			String decodedPathString = DataUtils
					.buildDecodedPathFromURLPathInfo(path, retrieveEncoding());
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
	 *            <code>String</code> with the iRODS absolute path derived from
	 *            the URL extra path information
	 * @return
	 * @throws JargonException
	 */
	@GET
	@Path("{path:.*}/metadata")
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public MetadataListing getDataObjectMetadata(
			@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path) throws JargonException {

		log.info("getDataObjectMetadata()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		String decodedPathString = DataUtils.buildDecodedPathFromURLPathInfo(
				path, retrieveEncoding());

		try {
			log.error("decoded path:{}", decodedPathString);
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);

			log.info("listing metadata");
			DataObjectAvuFunctions dataObjectAvuFunctions = this
					.getServiceFunctionFactory()
					.instanceDataObjectAvuFunctions(irodsAccount);
			return dataObjectAvuFunctions
					.listDataObjectMetadata(decodedPathString);

		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * Do a bulk metadata add operation for the given data object. This takes a
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

		String decodedPathString = DataUtils.buildDecodedPathFromURLPathInfo(
				path, retrieveEncoding());

		try {
			IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);

			log.info("marshalling into AvuData...");
			List<AvuData> avuDatas = new ArrayList<AvuData>();

			for (MetadataEntry metadataEntry : metadataOperation
					.getMetadataEntries()) {
				avuDatas.add(AvuData.instance(metadataEntry.getAttribute(),
						metadataEntry.getValue(), metadataEntry.getUnit()));
			}
			DataObjectAvuFunctions dataObjectAvuFunctions = getServiceFunctionFactory()
					.instanceDataObjectAvuFunctions(irodsAccount);
			return dataObjectAvuFunctions.addAvuMetadata(decodedPathString,
					avuDatas);

		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

}
