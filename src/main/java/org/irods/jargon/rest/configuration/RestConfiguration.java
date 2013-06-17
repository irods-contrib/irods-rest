/**
 * 
 */
package org.irods.jargon.rest.configuration;

/**
 * Pojo containing configuration information
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class RestConfiguration {
	
	private String irodsHost = "";
	private int irodsPort = 1247;
	private String irodsZone = "";
	private String defaultStorageResource = "";
	
	/**
	 * @return the irodsHost
	 */
	public String getIrodsHost() {
		return irodsHost;
	}
	/**
	 * @param irodsHost the irodsHost to set
	 */
	public void setIrodsHost(String irodsHost) {
		this.irodsHost = irodsHost;
	}
	/**
	 * @return the irodsPort
	 */
	public int getIrodsPort() {
		return irodsPort;
	}
	/**
	 * @param irodsPort the irodsPort to set
	 */
	public void setIrodsPort(int irodsPort) {
		this.irodsPort = irodsPort;
	}
	/**
	 * @return the irodsZone
	 */
	public String getIrodsZone() {
		return irodsZone;
	}
	/**
	 * @param irodsZone the irodsZone to set
	 */
	public void setIrodsZone(String irodsZone) {
		this.irodsZone = irodsZone;
	}
	/**
	 * @return the defaultStorageResource
	 */
	public String getDefaultStorageResource() {
		return defaultStorageResource;
	}
	/**
	 * @param defaultStorageResource the defaultStorageResource to set
	 */
	public void setDefaultStorageResource(String defaultStorageResource) {
		this.defaultStorageResource = defaultStorageResource;
	}
	
}
