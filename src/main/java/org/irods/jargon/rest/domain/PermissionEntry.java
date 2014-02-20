/**
 * 
 */
package org.irods.jargon.rest.domain;

import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.protovalues.UserTypeEnum;

/**
 * Represents an individual user permission
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class PermissionEntry {

	private String userName = "";
	private String userZone = "";
	private String userId = "";
	private UserTypeEnum userType = UserTypeEnum.RODS_UNKNOWN;
	private FilePermissionEnum filePermissionEnum;

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the userZone
	 */
	public String getUserZone() {
		return userZone;
	}

	/**
	 * @param userZone
	 *            the userZone to set
	 */
	public void setUserZone(String userZone) {
		this.userZone = userZone;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the userType
	 */
	public UserTypeEnum getUserType() {
		return userType;
	}

	/**
	 * @param userType
	 *            the userType to set
	 */
	public void setUserType(UserTypeEnum userType) {
		this.userType = userType;
	}

	/**
	 * @return the filePermissionEnum
	 */
	public FilePermissionEnum getFilePermissionEnum() {
		return filePermissionEnum;
	}

	/**
	 * @param filePermissionEnum
	 *            the filePermissionEnum to set
	 */
	public void setFilePermissionEnum(FilePermissionEnum filePermissionEnum) {
		this.filePermissionEnum = filePermissionEnum;
	}

}
