/**
 * 
 */
package org.irods.jargon.rest.commands.dataobject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileOutputStream;
import org.irods.jargon.rest.commands.AbstractIrodsService;
import org.irods.jargon.rest.domain.DataObjectData;
import org.irods.jargon.rest.utils.DataUtils;
import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST service for managing iRODS data objects
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Named
@Path("/data")
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
					.buildDecodedPathFromURLPathInfo(path,
							this.retrieveEncoding());
			log.info("decoded path:{}", decodedPathString);
			DataObject dataObject = dataObjectAO
					.findByAbsolutePath(decodedPathString);

			log.info("found dataObject, marshall the data:{}", dataObject);
			DataObjectData dataObjectData = buildDataObjectValuesFromIrodsData(dataObject);

			log.info("got data object data:{}", dataObjectData);

			return dataObjectData;
		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * @param dataObject
	 * @return
	 */
	private DataObjectData buildDataObjectValuesFromIrodsData(
			DataObject dataObject) {
		DataObjectData dataObjectData = new DataObjectData();

		dataObjectData.setChecksum(dataObject.getChecksum());
		dataObjectData.setCollectionId(dataObject.getCollectionId());
		dataObjectData.setCollectionName(dataObject.getCollectionName());
		dataObjectData.setComments(dataObject.getComments());
		dataObjectData.setCreatedAt(dataObject.getCreatedAt());
		dataObjectData.setDataMapId(dataObject.getDataMapId());
		dataObjectData.setDataName(dataObject.getDataName());
		dataObjectData.setDataOwnerName(dataObject.getDataOwnerName());
		dataObjectData.setDataOwnerZone(dataObject.getDataOwnerZone());
		dataObjectData.setDataPath(dataObject.getDataPath());
		dataObjectData.setDataReplicationNumber(dataObject
				.getDataReplicationNumber());
		dataObjectData.setDataSize(dataObject.getDataSize());
		dataObjectData.setDataStatus(dataObject.getDataStatus());
		dataObjectData.setDataTypeName(dataObject.getDataTypeName());
		dataObjectData.setDataVersion(dataObject.getDataVersion());
		dataObjectData.setExpiry(dataObject.getExpiry());
		dataObjectData.setId(dataObject.getId());
		dataObjectData.setObjectPath(dataObject.getObjectPath());
		dataObjectData.setReplicationStatus(dataObject.getReplicationStatus());
		dataObjectData.setResourceGroupName(dataObject.getResourceGroupName());
		dataObjectData.setResourceName(dataObject.getResourceName());
		dataObjectData.setSpecColType(dataObject.getSpecColType());
		dataObjectData.setUpdatedAt(dataObject.getUpdatedAt());
		return dataObjectData;
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
					.buildDecodedPathFromURLPathInfo(path,
							this.retrieveEncoding());

			IRODSFile dataFile = this.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(decodedPathString);

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

	@POST
	@Path("{path:.*}")
	@Consumes("multipart/form-data")
	@Produces({ "application/xml", "application/json" })
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public DataObjectData uploadFile(
			@HeaderParam("Authorization") final String authorization,
			@PathParam("path") final String path, MultipartFormDataInput input)
			throws JargonException {

		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		List<InputPart> inputParts = uploadForm.get("uploadedFile");

		if (inputParts == null) {
			throw new IllegalArgumentException(
					"uploadedFile missing in form data");
		}

		if (inputParts.isEmpty()) {
			throw new IllegalArgumentException("empty inputParts");
		}

		if (inputParts.size() > 1) {
			throw new JargonException("only 1 file upload per invocation");
		}

		IRODSAccount irodsAccount = retrieveIrodsAccountFromAuthentication(authorization);

		/*
		 * The path param in the URL gives the target file in iRODS, there is
		 * only one
		 */

		String decodedPathString = DataUtils.buildDecodedPathFromURLPathInfo(
				path, this.retrieveEncoding());

		IRODSFile dataFile = this.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(decodedPathString);

		InputPart inputPart = inputParts.get(0);
		Stream2StreamAO stream2StreamAO = this.getIrodsAccessObjectFactory()
				.getStream2StreamAO(irodsAccount);
		DataObjectAO dataObjectAO = this.getIrodsAccessObjectFactory()
				.getDataObjectAO(irodsAccount);
		log.info("creating target output stream to irods..");
		IRODSFileOutputStream outputStream = this.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFileOutputStream(dataFile);

		try {

			log.info("getting input stream for file...");
			// convert the uploaded file to inputstream
			InputStream inputStream = inputPart
					.getBody(InputStream.class, null);
			log.info("started stream copy...");
			stream2StreamAO.streamToStreamCopyUsingStandardIO(inputStream,
					outputStream);
			log.info("stream copy completed...look up resulting iRODS data object to prepare response");
			DataObject dataObject = dataObjectAO
					.findByAbsolutePath(decodedPathString);

			log.info("found dataObject, marshall the data:{}", dataObject);
			DataObjectData dataObjectData = buildDataObjectValuesFromIrodsData(dataObject);

			log.info("got data object data:{}", dataObjectData);

			return dataObjectData;

		} catch (IOException e) {
			log.error("io exception streaming file data", e);
			throw new JargonException("io exception streaming file data", e);
		}
	}
}
