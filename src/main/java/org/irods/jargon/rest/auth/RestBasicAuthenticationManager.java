package org.irods.jargon.rest.auth;

import java.net.UnknownHostException;

import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;


/**
 * Implementation of a Spring Security <code>AuthenticationManager</code>
 * interface. This manager will authenticate a user to an IRODS zone with a
 * given set of credentials via basic authentication, and uses a configuration
 * object to pont to the correct zone and host that will be used for
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class RestBasicAuthenticationManager implements AuthenticationManager {

	private Logger log = LoggerFactory.getLogger(this.getClass());   

	/**
	 * Dependency on {@link IRODSAccessObjectFactory}
	 */
	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	/**
	 * Dependency on {@link RestConfiguration} which provides the iRODS host
	 * info to go with the incoming credentials
	 */
	private RestConfiguration restConfiguration;
 
	public RestBasicAuthenticationManager() {
	}
 
	/*
	 * (non-Javadoc)   
	 * 
	 * @seeorg.springframework.security.authentication.AuthenticationManager#
	 * authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	public Authentication authenticate(final Authentication authentication)
			throws AuthenticationException {

		log.info("authenticating:{}", authentication);

		if (authentication == null) {
			log.error("the authentication passed to the method is null");
			throw new BadCredentialsException("null authentication");
		}

		checkDependencies();

		try {
			// irodsAccessObjectFactory
			// .getUserAO(irodsAuthToken.getIrodsAccount());
		} catch (Exception e) {
			log.error("unable to authenticate, JargonException", e);
			e.printStackTrace();

			if (e.getCause() == null) {
				if (e.getMessage().indexOf("-826000") > -1) {
					log.warn("invalid user/password");

					throw new BadCredentialsException(
							"Unknown user id/password", e);
				} else {
					log.error("authentication service exception", e);

					throw new AuthenticationServiceException(
							"unable to authenticate", e);
				}
			} else if (e.getCause() instanceof UnknownHostException) {
				log.warn("cause is invalid host");

				throw new BadCredentialsException("The host is unknown", e);
			} else if (e.getCause().getMessage().indexOf("refused") > -1) {
				log.error("cause is refused or invalid port");

				throw new BadCredentialsException(
						"The host/port is unknown or refusing connection", e);
			} else {
				log.error("authentication service exception", e);

				throw new AuthenticationServiceException(
						"unable to authenticate", e);
			}
		}

		log.info("authenticated");
		authentication.setAuthenticated(true);

		return authentication;

	}

	private void checkDependencies() {
		if (irodsAccessObjectFactory == null) {
			throw new AuthenticationServiceException(
					"irods access object factory is null");
		}

		if (restConfiguration == null) {
			throw new AuthenticationServiceException(
					"restConfiguration is null");
		}
	}

	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/**
	 * Factory to create access objects used to obtain necessary IRODS data used
	 * in authentication and authorization.
	 * 
	 * @param irodsAccessObjectFactory
	 */
	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
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

}
