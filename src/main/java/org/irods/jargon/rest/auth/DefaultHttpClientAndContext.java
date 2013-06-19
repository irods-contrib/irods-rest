/**
 * 
 */
package org.irods.jargon.rest.auth;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DefaultHttpClientAndContext {
	private DefaultHttpClient httpClient;
	private BasicHttpContext httpContext;
	private String host = "";

	/**
	 * @return the httpClient
	 */
	public DefaultHttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * @param httpClient
	 *            the httpClient to set
	 */
	public void setHttpClient(final DefaultHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * @return the httpContext
	 */
	public BasicHttpContext getHttpContext() {
		return httpContext;
	}

	/**
	 * @param httpContext
	 *            the httpContext to set
	 */
	public void setHttpContext(final BasicHttpContext httpContext) {
		this.httpContext = httpContext;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(final String host) {
		this.host = host;
	}

}
