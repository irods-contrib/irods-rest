/**
 * 
 */
package org.irods.jargon.rest.commands.collection;

import java.net.URI;

import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.rest.auth.RestAuthUtils;
import org.irods.jargon.rest.commands.AbstractIrodsService;
import org.irods.jargon.rest.domain.UserData;
import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Services for accessing iRODS Collections
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
@Named
@Path("/collection")
public class CollectionService extends AbstractIrodsService  {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@GET
	@Produces({ "application/xml", "application/json"})
	@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://irods.org/irods-rest", jsonName = "irods-rest") })
	public CollectionData getCollectionData(
			@HeaderParam("Authorization") final String authorization,
			@QueryParam("uri") final URI uri, @QueryParam("offset")  final int offset)
			throws JargonException {
		log.info("getUser()");

		if (authorization == null || authorization.isEmpty()) {
			throw new IllegalArgumentException("null or empty authorization");
		}

		if (uri == null) {
			throw new IllegalArgumentException("null uri");
		}

		try {
			IRODSAccount irodsAccount = RestAuthUtils
					.getIRODSAccountFromBasicAuthValues(authorization,
							getRestConfiguration());
			UserAO userAO = getIrodsAccessObjectFactory().getUserAO(
					irodsAccount);
			log.info("looking up user with name:{}", userName);
			User user = userAO.findByName(userName);
			log.info("user found:{}", user);

			return new UserData(user, getRestConfiguration());
		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}
	

}
