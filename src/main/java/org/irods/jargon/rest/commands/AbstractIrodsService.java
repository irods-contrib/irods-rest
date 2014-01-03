package org.irods.jargon.rest.commands;

import javax.inject.Inject;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.rest.auth.RestAuthUtils;
import org.irods.jargon.rest.configuration.RestConfiguration;

/**
 * Base class for an iRODS rest service
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public abstract class AbstractIrodsService {

	@Inject
	IRODSAccessObjectFactory irodsAccessObjectFactory;

	@Inject
	RestConfiguration restConfiguration;

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
	 * Method delegates to utility to turn the authorization header into an
	 * iRODS account
	 * 
	 * @param authorization
	 *            <code>String</code> with the basic auth headers
	 * @return {@link IRODSAccount} that corresponds to the authorization
	 * @throws JargonException
	 */
	protected IRODSAccount retrieveIrodsAccountFromAuthentication(
			final String authorization) throws JargonException {
		return RestAuthUtils.getIRODSAccountFromBasicAuthValues(authorization,
				getRestConfiguration());
	}

	/**
	 * Get the encoding set in the jargon properties
	 * 
	 * @return <code>String</code> with the configured encoding
	 * @throws JargonException
	 */
	protected String retrieveEncoding() throws JargonException {
		return this.getIrodsAccessObjectFactory().getJargonProperties()
				.getEncoding();
	}

}