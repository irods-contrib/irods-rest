/**
 * 
 */
package org.irods.jargon.rest.commands.user;

/**
 * Request to add a user to the given user group
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class UserAddToGroupRequest {

	
	private String userName = "";
	private String userGroup = "";
	private String zone = "";
	
	/**
	 * 
	 */
	public UserAddToGroupRequest() {
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("userAddToGroupRequest");
		sb.append("\n\tuserName:");
		sb.append(userName);
		sb.append("\n\tuserGroup:");
		sb.append(userGroup);
		sb.append("\n\tzone:");
		sb.append(zone);
		return sb.toString();
	}
	
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the userGroup
	 */
	public String getUserGroup() {
		return userGroup;
	}

	/**
	 * @param userGroup the userGroup to set
	 */
	public void setUserGroup(String userGroup) {
		this.userGroup = userGroup;
	}

	/**
	 * @return the zone
	 */
	public String getZone() {
		return zone;
	}

	/**
	 * @param zone the zone to set
	 */
	public void setZone(String zone) {
		this.zone = zone;
	}

}
