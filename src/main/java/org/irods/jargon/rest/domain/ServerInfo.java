/**
 * 
 */
package org.irods.jargon.rest.domain;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.irods.jargon.core.connection.IRODSServerProperties.IcatEnabled;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;

/**
 * Information about the server for representation as XML or JSON
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
@XmlRootElement(name = "serverInfo", namespace = RestConfiguration.NS)
@Mapped(namespaceMap = { @XmlNsMap(namespace = RestConfiguration.NS, jsonName = RestConfiguration.JSON_NAME) })
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

	@XmlElement
	public Date getInitializeDate() {
		return initializeDate;
	}

	public void setInitializeDate(final Date initializeDate) {
		this.initializeDate = initializeDate;
	}

	@XmlElement
	public IcatEnabled getIcatEnabled() {
		return icatEnabled;
	}

	public void setIcatEnabled(final IcatEnabled icatEnabled) {
		this.icatEnabled = icatEnabled;
	}

	@XmlElement
	public int getServerBootTime() {
		return serverBootTime;
	}

	public void setServerBootTime(final int serverBootTime) {
		this.serverBootTime = serverBootTime;
	}

	@XmlElement
	public String getRelVersion() {
		return relVersion;
	}

	public void setRelVersion(final String relVersion) {
		this.relVersion = relVersion;
	}

	@XmlElement
	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(final String apiVersion) {
		this.apiVersion = apiVersion;
	}

	@XmlElement
	public String getRodsZone() {
		return rodsZone;
	}

	public void setRodsZone(final String rodsZone) {
		this.rodsZone = rodsZone;
	}

	@XmlElement
	public long getCurrentServerTime() {
		return currentServerTime;
	}

	public void setCurrentServerTime(final long currentServerTime) {
		this.currentServerTime = currentServerTime;
	}

}
