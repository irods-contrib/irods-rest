/**
 * 
 */
package org.irods.jargon.rest.configuration;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.interceptors.CorsFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - DICE
 * 
 */
@Named
@Provider
@PreMatching
public class IrodsCorsFilterold extends CorsFilter {

	private final Logger log = LoggerFactory.getLogger(IrodsCorsFilterold.class);

	@Inject
	private RestConfiguration restConfiguration;

	/**
	 * This is poorly documented in RESTEasy, I'm sure this is not exactly
	 * right, but at least this is workable! Somebody smarter and more patent
	 * than me can make this 'proper'. I decided to fly in info from
	 * RestConfiguration, which in many ways seems tighter.
	 */
	@Override
	public void filter(ContainerRequestContext requestContext,
			ContainerResponseContext responseContext) throws IOException {

		log.info("cors filter fired (request,response)");
		// super.filter(requestContext, responseContext);

		if (restConfiguration == null) {
			throw new IllegalStateException("no RestConfiguration set");
		}

		if (isAllowCors() == false) {
			return;
		} else {
			super.filter(requestContext, responseContext);
		}
	}

	private boolean isAllowCors() {
		log.debug("restConfiguration:{}", restConfiguration);

		if (!restConfiguration.isAllowCors()) {
			return false;
		} else {
			return true;
		}

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
	public void setRestConfiguration(RestConfiguration restConfiguration) {
		this.restConfiguration = restConfiguration;
	}

	@Override
	public String getAllowedMethods() {
		StringBuilder sb = new StringBuilder();
		if (restConfiguration.getCorsMethods().isEmpty()) {
			log.info("no methods specified, add all");
			sb.append("GET, POST, DELETE, PUT");
		} else {
			log.debug("building up cords methods list");

			int ctr = 0;
			for (String method : restConfiguration.getCorsMethods()) {
				if (ctr++ > 0) {
					sb.append(',');
				}
				sb.append(method);
			}

		}

		return sb.toString();
	}

	@Override
	public Set<String> getAllowedOrigins() {

		Set<String> allowedOrigins = new HashSet<String>();
		if (restConfiguration.getCorsOrigins().isEmpty()) {
			log.debug("default to all origins");
			allowedOrigins.add("*");
		} else {
			log.debug("building up origin list");
			for (String origin : restConfiguration.getCorsOrigins()) {
				allowedOrigins.add(origin);
			}
		}

		return allowedOrigins;

	}

	@Override
	public boolean isAllowCredentials() {
		return restConfiguration.isCorsAllowCredentials();
	}

	@Override
	public void filter(ContainerRequestContext requestContext)
			throws IOException {
		if (restConfiguration == null) {
			throw new IllegalStateException("no RestConfiguration set");
		}

		if (isAllowCors() == false) {
			return;
		} else {
			super.filter(requestContext);
		}
	}
}
