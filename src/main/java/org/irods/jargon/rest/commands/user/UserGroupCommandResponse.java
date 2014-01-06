/**
 * 
 */
package org.irods.jargon.rest.commands.user;

import org.irods.jargon.rest.commands.GenericCommandResponse;

/**
 * @author Mike Conway - DICE (www.irods.org) see http://code.renci.org for
 *         trackers, access info, and documentation
 * 
 */
public class UserGroupCommandResponse extends GenericCommandResponse {

	public enum UserGroupCommandStatus {
		OK, DUPLICATE_GROUP, DUPLICATE_USER, INVALID_USER, INVALID_GROUP
	}

	private UserGroupCommandStatus userGroupCommandStatus = UserGroupCommandStatus.OK;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("UserGroupCommandStatus");
		sb.append("\n\t");
		sb.append(super.toString());
		sb.append("\n\tuserGroupCommandStatus:");
		sb.append(userGroupCommandStatus);
		return sb.toString();
	}

	/**
	 * 
	 */
	public UserGroupCommandResponse() {
	}

	public UserGroupCommandStatus getUserGroupCommandStatus() {
		return userGroupCommandStatus;
	}

	public void setUserGroupCommandStatus(
			final UserGroupCommandStatus userGroupCommandStatus) {
		this.userGroupCommandStatus = userGroupCommandStatus;
	}

}
