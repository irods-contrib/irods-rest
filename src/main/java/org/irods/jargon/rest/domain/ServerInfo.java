/**
 * 
 */
package org.irods.jargon.rest.domain;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import org.irods.jargon.core.connection.IRODSServerProperties.IcatEnabled;
import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;

/**
 * Information about the server for representation as XML or JSON
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
@XmlRootElement(name = "serverInfo")
@BadgerFish
public class ServerInfo {

	private Date initializeDate = new Date();
	private IcatEnabled icatEnabled = IcatEnabled.ICAT_ENABLED;
	private int serverBootTime = 0;
	private String relVersion = "";
	private String apiVersion = "";
	private String rodsZone = "";
	/**
	 * 
	 */
	private long currentServerTime = 0;

	public Date getInitializeDate() {
		return initializeDate;
	}

	public void setInitializeDate(Date initializeDate) {
		this.initializeDate = initializeDate;
	}

	public IcatEnabled getIcatEnabled() {
		return icatEnabled;
	}

	public void setIcatEnabled(IcatEnabled icatEnabled) {
		this.icatEnabled = icatEnabled;
	}

	public int getServerBootTime() {
		return serverBootTime;
	}

	public void setServerBootTime(int serverBootTime) {
		this.serverBootTime = serverBootTime;
	}

	public String getRelVersion() {
		return relVersion;
	}

	public void setRelVersion(String relVersion) {
		this.relVersion = relVersion;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getRodsZone() {
		return rodsZone;
	}

	public void setRodsZone(String rodsZone) {
		this.rodsZone = rodsZone;
	}

	public long getCurrentServerTime() {
		return currentServerTime;
	}

	public void setCurrentServerTime(long currentServerTime) {
		this.currentServerTime = currentServerTime;
	}

}
