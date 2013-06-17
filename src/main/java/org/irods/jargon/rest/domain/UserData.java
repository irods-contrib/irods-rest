/**
 * 
 */
package org.irods.jargon.rest.domain;

import java.util.Date;

import org.irods.jargon.core.protovalues.UserTypeEnum;

/**
 * Value object wrapping a jargon <code>User</code> object for representation as XML or JSON
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class UserData {
	

	private String name = "";
	private String id = "";
	private String zone = "";
	private String info = "";
	private String comment = "";
	private Date createTime = null;
	private Date modifyTime = null;
	private UserTypeEnum userType = UserTypeEnum.RODS_UNKNOWN;
	private String userDN = "";

	/**
	 * 
	 */
	public UserData() {
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("UserData:\n");
		stringBuilder.append("  id:");
		stringBuilder.append(id);
		stringBuilder.append("  name:");
		stringBuilder.append(name);
		stringBuilder.append('\n');
		stringBuilder.append("  userType:");
		stringBuilder.append(userType.getTextValue());
		stringBuilder.append('\n');
		stringBuilder.append("  userDn:");
		stringBuilder.append(userDN);
		stringBuilder.append('\n');
		stringBuilder.append("  zone:");
		stringBuilder.append(zone);
		stringBuilder.append('\n');
		stringBuilder.append("  info:");
		stringBuilder.append(info);
		stringBuilder.append('\n');
		stringBuilder.append("  comment:");
		stringBuilder.append(comment);
		return stringBuilder.toString();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
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

	/**
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(String info) {
		this.info = info;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the createTime
	 */
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * @param createTime the createTime to set
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * @return the modifyTime
	 */
	public Date getModifyTime() {
		return modifyTime;
	}

	/**
	 * @param modifyTime the modifyTime to set
	 */
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	/**
	 * @return the userType
	 */
	public UserTypeEnum getUserType() {
		return userType;
	}

	/**
	 * @param userType the userType to set
	 */
	public void setUserType(UserTypeEnum userType) {
		this.userType = userType;
	}

	/**
	 * @return the userDN
	 */
	public String getUserDN() {
		return userDN;
	}

	/**
	 * @param userDN the userDN to set
	 */
	public void setUserDN(String userDN) {
		this.userDN = userDN;
	}

}
