/**
 * 
 */
package org.irods.jargon.rest.commands;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class UserAddByAdminRequest {
	private String userName = "";
	private String tempPassword = "";
	private String distinguishedName = "";
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("UserAddByAdminRequest");
		sb.append("\n\t userName:");
		sb.append(userName);
		sb.append("\n\t tempPassword:");
		sb.append(tempPassword);
		sb.append("\n\t distinguishedName:");
		sb.append(distinguishedName);
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
	 * @return the tempPassword
	 */
	public String getTempPassword() {
		return tempPassword;
	}
	/**
	 * @param tempPassword the tempPassword to set
	 */
	public void setTempPassword(String tempPassword) {
		this.tempPassword = tempPassword;
	}
	/**
	 * @return the distinguisedName
	 */
	public String getDistinguishedName() {
		return distinguishedName;
	}
	/**
	 * @param distinguisedName the distinguisedName to set
	 */
	public void setDistinguishedName(String distinguishedName) {
		this.distinguishedName = distinguishedName;
	}
}
