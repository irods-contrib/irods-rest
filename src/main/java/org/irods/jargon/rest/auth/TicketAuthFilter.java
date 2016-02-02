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
import org.irods.jargon.ticket.TicketClientSupport;

/**
 * Servlet filter implements basic auth
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Named
public class TicketAuthFilter implements Filter {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Inject
	private RestConfiguration restConfiguration;
	@Inject
	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	/**
	 * 
	 */
	public TicketAuthFilter() {
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

		log.info("JUSTIN: TicketAuthFilter.doFilter()");

		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		String ticketString = httpRequest.getParameter("ticket");

		try {
			if (ticketString != null && !ticketString.isEmpty()) {
				
				log.info("JUSTIN:  Ticket in URL");

				// create a guest session		
				IRODSAccount irodsAccount = RestAuthUtils.instanceForAnonymous(restConfiguration);
				
				log.info("JUSTIN: After instanceForAnonymous");

				// use TicketClientSupport
				TicketClientSupport ticketClientSupport = new TicketClientSupport(
						irodsAccessObjectFactory, irodsAccount);
				ticketClientSupport.initializeSessionWithTicket(ticketString);
				
				log.info("JUSTIN: Before chain.doFilter()");
				
				chain.doFilter(httpRequest, httpResponse);
			} else {
				chain.doFilter(httpRequest, httpResponse);
			}

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
