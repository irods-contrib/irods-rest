/**
 * 
 */
package org.irods.jargon.rest.commands.user;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Response object for a temporary password request
 * 
 * @author Mike Conway - DICE
 * 
 */
@XmlRootElement(name = "temporaryPassword")
public class TemporaryPasswordResponse {

	/**
	 * User name for request
	 */
	private String userName = "";
	/**
	 * Temporary password value
	 */
	private String password = "";

	/**
	 * 
	 */
	public TemporaryPasswordResponse() {
	}

	/**
	 * @return the userName
	 */
	@XmlElement
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
	 * @return the password
	 */
	@XmlElement
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

}
