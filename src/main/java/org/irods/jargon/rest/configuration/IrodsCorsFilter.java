/**
 * 
 */
package org.irods.jargon.rest.configuration;

import java.io.IOException;
import java.util.Set;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.plugins.interceptors.CorsFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - DICE
 * 
 */
@Provider
@ServerInterceptor
public class IrodsCorsFilter extends CorsFilter {

	private final Logger log = LoggerFactory.getLogger(IrodsCorsFilter.class);

	@Override
	public void filter(ContainerRequestContext requestContext,
			ContainerResponseContext responseContext) throws IOException {

		log.info("cors filter fired");
		super.filter(requestContext, responseContext);
	}

	@Override
	public void filter(ContainerRequestContext requestContext)
			throws IOException {
		log.info("cors filter fired");
		super.filter(requestContext);
	}

	@Override
	public Set<String> getAllowedOrigins() {
		return super.getAllowedOrigins();
	}

}
