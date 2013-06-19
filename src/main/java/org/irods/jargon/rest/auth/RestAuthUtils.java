/**
 * 
 */
package org.irods.jargon.rest.auth;

import org.apache.commons.codec.binary.Base64;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class RestAuthUtils {

	private static Logger log = LoggerFactory.getLogger(RestAuthUtils.class);
	
	public static String basicAuthTokenFromIRODSAccount(final IRODSAccount irodsAccount) {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("Basic ");

		
		StringBuilder toEncode = new StringBuilder();
		toEncode.append(irodsAccount.getUserName());
		toEncode.append(":");
		toEncode.append(irodsAccount.getPassword());
		
		sb.append(Base64.encodeBase64String(toEncode.toString().getBytes()));
		return sb.toString();
	}

	/**
	 * Given the raw 'basic' auth header (with the Basic prefix), build an iRODS
	 * account
	 * 
	 * @param basicAuthData
	 * @param restConfiguration
	 * @return
	 * @throws JargonException
	 */
	public static IRODSAccount getIRODSAccountFromBasicAuthValues(
			final String basicAuthData,
			final RestConfiguration restConfiguration) throws JargonException {

		log.info("getIRODSAccountFromBasicAuthValues");

		if (basicAuthData == null || basicAuthData.isEmpty()) {
			throw new IllegalArgumentException("null or empty basicAuthData");
		}

		if (restConfiguration == null) {
			throw new IllegalArgumentException("null restConfiguration");
		}

		final int index = basicAuthData.indexOf(' ');
		log.info("index of end of basic prefix:{}", index);
		String auth = basicAuthData.substring(index);

		String decoded = new String(Base64.decodeBase64(auth));
		log.info("*******decoded:{}", decoded);

		log.info("index of end of basic prefix:{}", index);
		if (decoded.isEmpty()) {
			throw new JargonException("user and password not in credentials");

		}
		final String[] credentials = decoded.split(":");

		log.info("credentials:{}", credentials);

		if (credentials.length != 2) {
			throw new JargonException("user and password not in credentials");
		}

		return IRODSAccount.instance(restConfiguration.getIrodsHost(),
				restConfiguration.getIrodsPort(), credentials[0],
				credentials[1], "", restConfiguration.getIrodsZone(),
				restConfiguration.getDefaultStorageResource());

	}

}
