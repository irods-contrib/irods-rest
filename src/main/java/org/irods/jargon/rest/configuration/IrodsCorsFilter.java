/**
 * 
 */
package org.irods.jargon.rest.configuration;

import java.io.IOException;

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

		responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");

		responseContext.getHeaders().add("Access-Control-Allow-Credentials",
				"true");

		responseContext.getHeaders().add("Access-Control-Allow-Methods",
				"GET, POST, DELETE, PUT");

		log.info("set response headers:{}", responseContext.getHeaders());
	}

}
