/**
 * 
 */
package org.irods.jargon.rest.commands.user;

/**
 * Request to add a user group
 * 
 * @author Mike Conway - DICE (www.irods.org) see http://code.renci.org for
 *         trackers, access info, and documentation
 * 
 */
public class UserGroupRequest {

	private String userGroupName = "";
	private String zone = "";

	public UserGroupRequest() {
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("UserGroupRequest");
		sb.append("\n\tuserGroupName:");
		sb.append(userGroupName);
		sb.append("\n\tzone");
		sb.append(zone);
		return sb.toString();
	}

	public String getUserGroupName() {
		return userGroupName;
	}

	public void setUserGroupName(String userGroupName) {
		this.userGroupName = userGroupName;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

}
