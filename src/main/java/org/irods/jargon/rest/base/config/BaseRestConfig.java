package org.irods.jargon.rest.base.config;


import org.irods.jargon.core.connection.AuthScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author conwaymc
 *
 */
@Configuration
@PropertySource(value = { "file:///etc/irods-ext/irods-rest.properties" })

public class BaseRestConfig {
	
	/**
	 * FQDN of the iRODS server
	 */
	@Value( "${irods.host}" )
	private String irodsHost = "";
	
	/**
	 * iRODS main comm port
	 */
	@Value( "${irods.port}" )
	private int irodsPort = 1247;
	
	/**
	 * iRODS Zone name
	 */
	@Value("${irods.zone}")
	private String irodsZone = "";
	
	/**
	 * Authentication type (standard, PAM, etc)
	 */
	@Value("${auth.type}")
	private String authType = AuthScheme.STANDARD.getTextValue();
	
	/**
	 * Stream buffering optimizations for put/get
	 */
	@Value("${utilize.packing.streams}")
	private boolean utilizePackingStreams = true;
	
	/**
	 * Optional default storage resource
	 */
	@Value("${default.storage.resource}")
	private String defaultStorageResource = "";
	
	/**
	 * Client SSL negotiation behavior
	 */
	@Value("${ssl.negotiation.policy}")
	private String sslNegotiationPolicy = "CS_NEG_DONT_CARE";
	
	/**
	 * Compute checksum on uploaded files
	 */
	@Value("${compute.checksum}")
	private boolean computeChecksum = false;
	
	/**
	 * Optional interface to the standard web URL reference for MetaLnx
	 */
	@Value("${web.interface.url}")
	private String webInterfaceUrl = "";
	
	/**
	 * Option to use connection pooling/caching versus one connection per request
	 */
	@Value("${utilize.connection.pooling}")
	private boolean utilizeConnectionPooling = false;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BaseRestConfig [irodsHost=");
		builder.append(irodsHost);
		builder.append(", irodsPort=");
		builder.append(irodsPort);
		builder.append(", irodsZone=");
		builder.append(irodsZone);
		builder.append(", authType=");
		builder.append(authType);
		builder.append(", utilizePackingStreams=");
		builder.append(utilizePackingStreams);
		builder.append(", defaultStorageResource=");
		builder.append(defaultStorageResource);
		builder.append(", sslNegotiationPolicy=");
		builder.append(sslNegotiationPolicy);
		builder.append(", computeChecksum=");
		builder.append(computeChecksum);
		builder.append(", webInterfaceUrl=");
		builder.append(webInterfaceUrl);
		builder.append(", utilizeConnectionPooling=");
		builder.append(utilizeConnectionPooling);
		builder.append("]");
		return builder.toString();
	}

	public String getIrodsHost() {
		return irodsHost;
	}

	public void setIrodsHost(String irodsHost) {
		this.irodsHost = irodsHost;
	}

	public int getIrodsPort() {
		return irodsPort;
	}

	public void setIrodsPort(int irodsPort) {
		this.irodsPort = irodsPort;
	}

	public String getIrodsZone() {
		return irodsZone;
	}

	public void setIrodsZone(String irodsZone) {
		this.irodsZone = irodsZone;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public boolean isUtilizePackingStreams() {
		return utilizePackingStreams;
	}

	public void setUtilizePackingStreams(boolean utilizePackingStreams) {
		this.utilizePackingStreams = utilizePackingStreams;
	}

	public String getDefaultStorageResource() {
		return defaultStorageResource;
	}

	public void setDefaultStorageResource(String defaultStorageResource) {
		this.defaultStorageResource = defaultStorageResource;
	}

	public String getSslNegotiationPolicy() {
		return sslNegotiationPolicy;
	}

	public void setSslNegotiationPolicy(String sslNegotiationPolicy) {
		this.sslNegotiationPolicy = sslNegotiationPolicy;
	}

	public boolean isComputeChecksum() {
		return computeChecksum;
	}

	public void setComputeChecksum(boolean computeChecksum) {
		this.computeChecksum = computeChecksum;
	}

	public String getWebInterfaceUrl() {
		return webInterfaceUrl;
	}

	public void setWebInterfaceUrl(String webInterfaceUrl) {
		this.webInterfaceUrl = webInterfaceUrl;
	}
	
	

}
