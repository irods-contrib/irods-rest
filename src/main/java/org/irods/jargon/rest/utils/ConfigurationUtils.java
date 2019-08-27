/**
 * 
 */
package org.irods.jargon.rest.utils;

import org.irods.jargon.rest.configuration.RestConfiguration;

/**
 * Various utils for handling REST configuration information
 * 
 * @author Mike Conway - DICE (www.irods.org) see http://code.renci.org for
 *         trackers, access info, and documentation
 * 
 */
public class ConfigurationUtils {

	/**
	 * Build an exemplar .irodsEnv file based on the configuration for a given
	 * user name
	 * 
	 * @param restConfiguration
	 * @param userName
	 * @return
	 */
	public static String buildIrodsEnvForConfigAndUser(
			final RestConfiguration restConfiguration, final String userName) {

		if (restConfiguration == null) {
			throw new IllegalArgumentException("null restConfiguration");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		StringBuilder sb = new StringBuilder();

		sb.append("irodsHost=");
		sb.append(restConfiguration.getIrodsHost());
		sb.append("\n");
		sb.append("irodsPort=");
		sb.append(restConfiguration.getIrodsPort());
		sb.append("\n");
		sb.append("irodsDefResource=");
		sb.append(restConfiguration.getDefaultStorageResource());
		sb.append("\n");
		sb.append("irodsHome=/");
		sb.append(restConfiguration.getIrodsZone());
		sb.append("/home/");
		sb.append(userName);
		sb.append("\n");
		sb.append("irodsCwd=");
		sb.append("/");
		sb.append(restConfiguration.getIrodsZone());
		sb.append("/home/");
		sb.append(userName);
		sb.append("\n");
		sb.append("irodsUserName=");
		sb.append(userName);
		sb.append("\n");
		sb.append("irodsZone=");
		sb.append(restConfiguration.getIrodsZone());

		return sb.toString();

	}

}
