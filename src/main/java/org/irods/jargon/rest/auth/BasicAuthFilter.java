/**
 * 
 */
package org.irods.jargon.rest.auth;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet filter implements basic auth
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Named
public class BasicAuthFilter implements Filter {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	@Inject
	private RestConfiguration restConfiguration;
	@Inject
	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	/**
	 * 
	 */
	public BasicAuthFilter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(final ServletRequest request,
			final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {

		log.info("doFilter()");

		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		/*
		 * If options request do not quthenticate
		 */

		if (isPreflight((HttpServletRequest) request)) {
			log.debug("preflight, no auth");
			chain.doFilter(httpRequest, httpResponse);
			return;

		}

		String auth = httpRequest.getHeader("Authorization");

		if (auth == null || auth.isEmpty()) {
			log.error("auth null or empty");
			sendAuthError(httpResponse);
			return;
		}

		AuthResponse authResponse = null;
		try {
			IRODSAccount irodsAccount = RestAuthUtils
					.getIRODSAccountFromBasicAuthValues(auth, restConfiguration);

			log.info("irods account for auth:{}", irodsAccount);

			authResponse = irodsAccessObjectFactory
					.authenticateIRODSAccount(irodsAccount);

			log.info("authResponse:{}", authResponse);
			log.info("success!");
			/*
			 * HttpServletRequestWrapper wrapper = new
			 * HttpServletRequestWrapper(httpRequest) {
			 * 
			 * @Override public String getHeader(String name) {
			 * log.info("getting header from:{}", name); final String value =
			 * (String) super.getAttribute(name);
			 * log.info("value form attrib is:{}", value); if (value != null) {
			 * return value; } return super.getHeader(name); }
			 * 
			 * 
			 * @SuppressWarnings("rawtypes")
			 * 
			 * @Override public Enumeration getHeaders(String name) {
			 * log.info("getting headers from:{}", name); final String value =
			 * (String) request.getAttribute(name); if (value != null) {
			 * log.info("value from attrib is:{}", value); Set<String> mySet =
			 * new HashSet<String>(); mySet.add(value); return
			 * Collections.enumeration(mySet); } return super.getHeaders(name);
			 * } }; wrapper.setAttribute(RestConstants.AUTH_RESULT_KEY,
			 * irodsAccount.toURI(true).toString());
			 */
			chain.doFilter(httpRequest, httpResponse);
			return;

		} catch (JargonException e) {
			log.warn("auth exception", e);
			sendAuthError(httpResponse);
			return;
		} finally {
			irodsAccessObjectFactory.closeSessionAndEatExceptions();
		}

	}

	private void sendAuthError(final HttpServletResponse httpResponse)
			throws IOException {
		httpResponse.setHeader("WWW-Authenticate", "Basic realm=\""
				+ restConfiguration.getRealm() + "\"");
		httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/**
	 * @return the restConfiguration
	 */
	public RestConfiguration getRestConfiguration() {
		return restConfiguration;
	}

	/**
	 * @param restConfiguration
	 *            the restConfiguration to set
	 */
	public void setRestConfiguration(final RestConfiguration restConfiguration) {
		this.restConfiguration = restConfiguration;
	}

	/**
	 * @return the irodsAccessObjectFactory
	 */
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/**
	 * @param irodsAccessObjectFactory
	 *            the irodsAccessObjectFactory to set
	 */
	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	/**
	 * Checks if this is a X-domain pre-flight request.
	 * 
	 * @param request
	 * @return
	 */
	private boolean isPreflight(HttpServletRequest request) {
		return "OPTIONS".equals(request.getMethod());
	}

}
