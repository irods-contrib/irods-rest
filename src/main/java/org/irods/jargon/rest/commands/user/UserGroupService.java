/**
 * 
 */
package org.irods.jargon.rest.commands.user;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.InvalidGroupException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.UserGroupAO;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.rest.auth.RestAuthUtils;
import org.irods.jargon.rest.commands.AbstractIrodsService;
import org.irods.jargon.rest.commands.GenericCommandResponse;
import org.irods.jargon.rest.commands.GenericCommandResponse.Status;
import org.irods.jargon.rest.commands.user.UserGroupCommandResponse.UserGroupCommandStatus;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.irods.jargon.rest.exception.InvalidRequestDataException;
import org.irods.jargon.rest.exception.IrodsRestException;
import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Restful services for iRODS user groups
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Named
@Path("/user_group")
public class UserGroupService extends AbstractIrodsService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Add a user to a given user group.
	 * 
	 * @param authorization
	 *            BasicAuth info
	 * @param UserGroupMembershipRequest
	 *            {@link UserGroupMembershipRequest}
	 * @return {@link GenericCommandResponse}
	 * 
	 */
	@PUT
	@Path("/user")
	@Consumes("application/json")
	@Mapped(namespaceMap = { @XmlNsMap(namespace = RestConfiguration.NS, jsonName = RestConfiguration.JSON_NAME) })
	public UserGroupCommandResponse addUserToGroup(
			@HeaderParam("Authorization") final String authorization,
			final UserGroupMembershipRequest userAddToGroupRequest)
			throws InvalidRequestDataException, IrodsRestException {
		log.info("addUserToGroup()");
		UserGroupCommandResponse response = new UserGroupCommandResponse();

		if (userAddToGroupRequest == null) {
			log.error("request not found");
			throw new InvalidRequestDataException(
					"missing userAddToGroupRequest");
		}

		log.info("userAddToGroupRequest:{}", userAddToGroupRequest);

		if (userAddToGroupRequest.getUserName() == null
				|| userAddToGroupRequest.getUserName().isEmpty()) {
			log.error("user name missing");
			throw new InvalidRequestDataException("missing userName");
		}

		if (userAddToGroupRequest.getUserGroup() == null
				|| userAddToGroupRequest.getUserGroup().isEmpty()) {
			throw new InvalidRequestDataException("missing userGroup");
		}

		if (userAddToGroupRequest.getZone() == null
				|| userAddToGroupRequest.getZone().isEmpty()) {
			throw new InvalidRequestDataException("missing zone");
		}

		try {
			IRODSAccount irodsAccount = RestAuthUtils
					.getIRODSAccountFromBasicAuthValues(authorization,
							getRestConfiguration());

			UserGroupAO userGroupAO = getIrodsAccessObjectFactory()
					.getUserGroupAO(irodsAccount);

			try {
				userGroupAO.addUserToGroup(
						userAddToGroupRequest.getUserGroup(),
						userAddToGroupRequest.getUserName(),
						userAddToGroupRequest.getZone());
			} catch (InvalidUserException iue) {
				log.info("invalid user will be reflected in response");
				response.setStatus(Status.ERROR);
				response.setMessage(iue.getMessage());
				response.setUserGroupCommandStatus(UserGroupCommandStatus.INVALID_USER);
			} catch (DuplicateDataException dde) {
				log.info("duplicate user will be reflected in response");
				response.setStatus(Status.ERROR);
				response.setMessage(dde.getMessage());
				response.setUserGroupCommandStatus(UserGroupCommandStatus.DUPLICATE_USER);
			} catch (InvalidGroupException ige) {
				log.info("invalid group will be reflected in response");
				response.setStatus(Status.ERROR);
				response.setMessage(ige.getMessage());
				response.setUserGroupCommandStatus(UserGroupCommandStatus.INVALID_GROUP);
			}

			return response;

		} catch (JargonException je) {
			log.error("Jargon exception in user add", je);
			throw new IrodsRestException(je);
		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * Delete a user from a given user group.
	 * 
	 * @param authorization
	 *            BasicAuth info
	 * @param UserGroupMembershipRequest
	 *            {@link UserGroupMembershipRequest}
	 * @return {@link GenericCommandResponse}
	 * 
	 */
	@DELETE
	@Path("/{userGroup}")
	@Consumes("application/json")
	@Mapped(namespaceMap = { @XmlNsMap(namespace = RestConfiguration.NS, jsonName = RestConfiguration.JSON_NAME) })
	public UserGroupCommandResponse deleteUserGroup(
			@HeaderParam("Authorization") final String authorization,
			@PathParam("userGroup") final String userGroup)
			throws InvalidRequestDataException, IrodsRestException {
		log.info("deleteUserGroup()");
		UserGroupCommandResponse response = new UserGroupCommandResponse();

		if (userGroup == null || userGroup.isEmpty()) {
			throw new InvalidRequestDataException("missing userGroup");
		}

		try {
			IRODSAccount irodsAccount = RestAuthUtils
					.getIRODSAccountFromBasicAuthValues(authorization,
							getRestConfiguration());

			UserGroupAO userGroupAO = getIrodsAccessObjectFactory()
					.getUserGroupAO(irodsAccount);

			userGroupAO.removeUserGroup(userGroup);
			return response;

		} catch (JargonException je) {
			log.error("Jargon exception", je);
			throw new IrodsRestException(je);
		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * Delete a user from a given user group.
	 * 
	 * @param authorization
	 *            BasicAuth info
	 * @param UserGroupMembershipRequest
	 *            {@link UserGroupMembershipRequest}
	 * @return {@link GenericCommandResponse}
	 * 
	 */
	@DELETE
	@Path("/{userGroup}/user/{userName}")
	// @Consumes("application/json")
	@Mapped(namespaceMap = { @XmlNsMap(namespace = RestConfiguration.NS, jsonName = RestConfiguration.JSON_NAME) })
	public UserGroupCommandResponse deleteUserFromGroup(
			@HeaderParam("Authorization") final String authorization,
			@PathParam("userGroup") final String userGroup,
			@PathParam("userName") final String userName)
			throws InvalidRequestDataException, IrodsRestException {
		log.info("deleteUserFromGroup()");
		UserGroupCommandResponse response = new UserGroupCommandResponse();

		if (userGroup == null || userGroup.isEmpty()) {
			throw new InvalidRequestDataException("missing userGroup");
		}

		if (userName == null || userName.isEmpty()) {
			throw new InvalidRequestDataException("missing userName");
		}

		try {
			IRODSAccount irodsAccount = RestAuthUtils
					.getIRODSAccountFromBasicAuthValues(authorization,
							getRestConfiguration());

			UserGroupAO userGroupAO = getIrodsAccessObjectFactory()
					.getUserGroupAO(irodsAccount);

			userGroupAO.removeUserFromGroup(userGroup, userName,
					irodsAccount.getZone());

			return response;

		} catch (InvalidUserException iue) {
			log.error("Invalid user exception", iue);
			response.setStatus(Status.ERROR);
			response.setMessage(iue.getMessage());
			return response;
		} catch (JargonException je) {
			log.error("Jargon exception", je);
			throw new IrodsRestException(je);
		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

	/**
	 * Add a new user group
	 * 
	 * @param authorization
	 * @param userGroupAddRequest
	 * @return
	 * @throws InvalidRequestDataException
	 * @throws IrodsRestException
	 */
	@PUT
	@Consumes("application/json")
	@Mapped(namespaceMap = { @XmlNsMap(namespace = RestConfiguration.NS, jsonName = RestConfiguration.JSON_NAME) })
	public UserGroupCommandResponse addUserGroup(
			@HeaderParam("Authorization") final String authorization,
			final UserGroupRequest userGroupAddRequest)
			throws InvalidRequestDataException, IrodsRestException {
		log.info("addUserGroup()");
		UserGroupCommandResponse response = new UserGroupCommandResponse();

		if (userGroupAddRequest == null) {
			log.error("request not found");
			throw new InvalidRequestDataException("missing userGroupAddRequest");
		}

		log.info("userGroupAddRequest:{}", userGroupAddRequest);

		if (userGroupAddRequest.getUserGroupName() == null
				|| userGroupAddRequest.getUserGroupName().isEmpty()) {
			log.error("user groupName missing");
			throw new InvalidRequestDataException("missing userName");
		}

		if (userGroupAddRequest.getZone() == null
				|| userGroupAddRequest.getZone().isEmpty()) {
			throw new InvalidRequestDataException("missing zone");
		}

		try {
			IRODSAccount irodsAccount = RestAuthUtils
					.getIRODSAccountFromBasicAuthValues(authorization,
							getRestConfiguration());

			UserGroupAO userGroupAO = getIrodsAccessObjectFactory()
					.getUserGroupAO(irodsAccount);

			UserGroup userGroup = new UserGroup();
			userGroup.setUserGroupName(userGroupAddRequest.getUserGroupName());
			userGroup.setZone(userGroupAddRequest.getZone());

			try {
				userGroupAO.addUserGroup(userGroup);
			} catch (DuplicateDataException dde) {
				log.info("duplicate data exception will be reflected in response");
				response.setStatus(Status.ERROR);
				response.setMessage(dde.getMessage());
				response.setUserGroupCommandStatus(UserGroupCommandStatus.DUPLICATE_GROUP);
			}

			return response;

		} catch (JargonException je) {
			log.error("Jargon exception in user add", je);
			throw new IrodsRestException(je);
		} finally {
			getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
		}
	}

}
