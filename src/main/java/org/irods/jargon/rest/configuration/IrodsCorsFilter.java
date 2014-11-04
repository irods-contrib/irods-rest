/**
 * 
 */
package org.irods.jargon.rest.configuration;

import java.io.IOException;

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
public class IrodsCorsFilter extends CorsFilter {

	private final Logger log = LoggerFactory.getLogger(IrodsCorsFilter.class);

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

		log.debug("restConfiguration:{}", restConfiguration);

		if (!restConfiguration.isAllowCors()) {
			log.debug("no CORS processing");
			return;
		}

		log.debug("restConfiguration:{}", restConfiguration);

		if (restConfiguration.getCorsOrigins().isEmpty()) {
			log.debug("default to all origins");
			responseContext.getHeaders()
					.add("Access-Control-Allow-Origin", "*");
		} else {
			log.debug("building up origin list");
			StringBuilder sb = new StringBuilder();
			int ctr = 0;
			for (String origin : restConfiguration.getCorsOrigins()) {
				if (ctr++ > 0) {
					sb.append(',');
				}
				sb.append(origin);
			}
			responseContext.getHeaders().add("Access-Control-Allow-Origin",
					sb.toString());

		}

		if (restConfiguration.isCorsAllowCredentials()) {
			log.debug("allow credentials");
			responseContext.getHeaders().add(
					"Access-Control-Allow-Credentials", "true");
		}

		if (restConfiguration.getCorsMethods().isEmpty()) {
			log.info("no methods specified, add all");
			responseContext.getHeaders().add("Access-Control-Allow-Methods",
					"GET, POST, DELETE, PUT");
		} else {
			log.debug("building up cords methods list");
			StringBuilder sb = new StringBuilder();
			int ctr = 0;
			for (String method : restConfiguration.getCorsMethods()) {
				if (ctr++ > 0) {
					sb.append(',');
				}
				sb.append(method);
			}
			responseContext.getHeaders().add("Access-Control-Allow-Methods",
					sb.toString());

		}

		log.info("set response headers:{}", responseContext.getHeaders());
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

}
