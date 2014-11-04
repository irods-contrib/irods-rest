/**
 * 
 */
package org.irods.jargon.rest.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Pojo containing configuration information
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class RestConfiguration {

	private String irodsHost = "";
	private int irodsPort = 1247;
	private String irodsZone = "";
	private String defaultStorageResource = "";
	private String realm = "irods-rest";
	private boolean smimeEncryptAdminFunctions = false;
	private String privateCertAbsPath = "";
	private String publicKeyAbsPath = "";
	/**
	 * Add CORS headers
	 */
	private boolean allowCors = false;
	/**
	 * Allowed CORS origins
	 */
	private List<String> corsOrigins = new ArrayList<String>();
	/**
	 * Allowed CORS methods
	 */
	private List<String> corsMethods = new ArrayList<String>();
	/**
	 * Allow CORDS credentials
	 */
	private boolean corsAllowCredentials = false;

	/**
	 * Optional URL for a web interface to access grid data (typically an
	 * idrop-web installation pointing to the same grid)
	 */
	private String webInterfaceURL = "";

	/**
	 * @return the irodsHost
	 */
	public String getIrodsHost() {
		return irodsHost;
	}

	/**
	 * @param irodsHost
	 *            the irodsHost to set
	 */
	public void setIrodsHost(final String irodsHost) {
		this.irodsHost = irodsHost;
	}

	/**
	 * @return the irodsPort
	 */
	public int getIrodsPort() {
		return irodsPort;
	}

	/**
	 * @param irodsPort
	 *            the irodsPort to set
	 */
	public void setIrodsPort(final int irodsPort) {
		this.irodsPort = irodsPort;
	}

	/**
	 * @return the irodsZone
	 */
	public String getIrodsZone() {
		return irodsZone;
	}

	/**
	 * @param irodsZone
	 *            the irodsZone to set
	 */
	public void setIrodsZone(final String irodsZone) {
		this.irodsZone = irodsZone;
	}

	/**
	 * @return the defaultStorageResource
	 */
	public String getDefaultStorageResource() {
		return defaultStorageResource;
	}

	/**
	 * @param defaultStorageResource
	 *            the defaultStorageResource to set
	 */
	public void setDefaultStorageResource(final String defaultStorageResource) {
		this.defaultStorageResource = defaultStorageResource;
	}

	/**
	 * @return the realm
	 */
	public String getRealm() {
		return realm;
	}

	/**
	 * @param realm
	 *            the realm to set
	 */
	public void setRealm(final String realm) {
		this.realm = realm;
	}

	/**
	 * @return the smimeEncryptAdminFunctions
	 */
	public boolean isSmimeEncryptAdminFunctions() {
		return smimeEncryptAdminFunctions;
	}

	/**
	 * @param smimeEncryptAdminFunctions
	 *            the smimeEncryptAdminFunctions to set
	 */
	public void setSmimeEncryptAdminFunctions(
			final boolean smimeEncryptAdminFunctions) {
		this.smimeEncryptAdminFunctions = smimeEncryptAdminFunctions;
	}

	/**
	 * @return the privateCertAbsPath
	 */
	public String getPrivateCertAbsPath() {
		return privateCertAbsPath;
	}

	/**
	 * @param privateCertAbsPath
	 *            the privateCertAbsPath to set
	 */
	public void setPrivateCertAbsPath(final String privateCertAbsPath) {
		this.privateCertAbsPath = privateCertAbsPath;
	}

	/**
	 * @return the publicKeyAbsPath
	 */
	public String getPublicKeyAbsPath() {
		return publicKeyAbsPath;
	}

	/**
	 * @param publicKeyAbsPath
	 *            the publicKeyAbsPath to set
	 */
	public void setPublicKeyAbsPath(final String publicKeyAbsPath) {
		this.publicKeyAbsPath = publicKeyAbsPath;
	}

	public String getWebInterfaceURL() {
		return webInterfaceURL;
	}

	public void setWebInterfaceURL(final String webInterfaceURL) {
		this.webInterfaceURL = webInterfaceURL;
	}

	/**
	 * @return the allowCors
	 */
	public boolean isAllowCors() {
		return allowCors;
	}

	/**
	 * @param allowCors
	 *            the allowCors to set
	 */
	public void setAllowCors(boolean allowCors) {
		this.allowCors = allowCors;
	}

	/**
	 * @return the corsOrigins
	 */
	public List<String> getCorsOrigins() {
		return corsOrigins;
	}

	/**
	 * @param corsOrigins
	 *            the corsOrigins to set
	 */
	public void setCorsOrigins(List<String> corsOrigins) {
		this.corsOrigins = corsOrigins;
	}

	/**
	 * @return the corsMethods
	 */
	public List<String> getCorsMethods() {
		return corsMethods;
	}

	/**
	 * @param corsMethods
	 *            the corsMethods to set
	 */
	public void setCorsMethods(List<String> corsMethods) {
		this.corsMethods = corsMethods;
	}

	/**
	 * @return the corsAllowCredentials
	 */
	public boolean isCorsAllowCredentials() {
		return corsAllowCredentials;
	}

	/**
	 * @param corsAllowCredentials
	 *            the corsAllowCredentials to set
	 */
	public void setCorsAllowCredentials(boolean corsAllowCredentials) {
		this.corsAllowCredentials = corsAllowCredentials;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("RestConfiguration [");
		if (irodsHost != null) {
			builder.append("irodsHost=");
			builder.append(irodsHost);
			builder.append(", ");
		}
		builder.append("irodsPort=");
		builder.append(irodsPort);
		builder.append(", ");
		if (irodsZone != null) {
			builder.append("irodsZone=");
			builder.append(irodsZone);
			builder.append(", ");
		}
		if (defaultStorageResource != null) {
			builder.append("defaultStorageResource=");
			builder.append(defaultStorageResource);
			builder.append(", ");
		}
		if (realm != null) {
			builder.append("realm=");
			builder.append(realm);
			builder.append(", ");
		}
		builder.append("smimeEncryptAdminFunctions=");
		builder.append(smimeEncryptAdminFunctions);
		builder.append(", ");
		if (privateCertAbsPath != null) {
			builder.append("privateCertAbsPath=");
			builder.append(privateCertAbsPath);
			builder.append(", ");
		}
		if (publicKeyAbsPath != null) {
			builder.append("publicKeyAbsPath=");
			builder.append(publicKeyAbsPath);
			builder.append(", ");
		}
		builder.append("allowCors=");
		builder.append(allowCors);
		builder.append(", ");
		if (corsOrigins != null) {
			builder.append("corsOrigins=");
			builder.append(corsOrigins.subList(0,
					Math.min(corsOrigins.size(), maxLen)));
			builder.append(", ");
		}
		if (corsMethods != null) {
			builder.append("corsMethods=");
			builder.append(corsMethods.subList(0,
					Math.min(corsMethods.size(), maxLen)));
			builder.append(", ");
		}
		builder.append("corsAllowCredentials=");
		builder.append(corsAllowCredentials);
		builder.append(", ");
		if (webInterfaceURL != null) {
			builder.append("webInterfaceURL=");
			builder.append(webInterfaceURL);
		}
		builder.append("]");
		return builder.toString();
	}

}
