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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.irods.jargon.core.connection.IRODSAccount;
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
 * REST service for the contents of data objects. This is segmented from the
 * metadata based operations which are found in the {@link DataObjectService}.
 * <p/>
 * The operations here involve upload and download of file data, while the
 * <code>DataObjectService</code> handles catalog operations. In other words, a
 * GET operation on a <code>DataObjectService</code> will return JSON or XML
 * with iRODS system metadata about a file, while the GET operation here will
 * download the file contents.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Named
@Path("/fileContents")
public class FileContentsService extends AbstractIrodsService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

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
		List<InputPart> inputParts = uploadForm.get("uploadFile");

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
			DataObjectData dataObjectData = DataObjectServiceUtils
					.buildDataObjectValuesFromIrodsData(dataObject);

			log.info("got data object data:{}", dataObjectData);

			return dataObjectData;

		} catch (IOException e) {
			log.error("io exception streaming file data", e);
			throw new JargonException("io exception streaming file data", e);
		}
	}

}
