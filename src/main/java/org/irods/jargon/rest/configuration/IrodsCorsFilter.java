package org.irods.jargon.rest.configuration;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.CorsHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles CORS requests both preflight and simple CORS requests.
 */
@PreMatching
public class IrodsCorsFilter implements ContainerRequestFilter,
		ContainerResponseFilter {

	private final Logger log = LoggerFactory.getLogger(IrodsCorsFilter.class);

	@Inject
	private RestConfiguration restConfiguration;

	private boolean allowCredentials = true;
	private String allowedMethods;
	private String allowedHeaders;
	private String exposedHeaders;
	private int corsMaxAge = -1;
	private Set<String> allowedOrigins = new HashSet<String>();

	/**
	 * Put "*" if you want to accept all origins
	 *
	 * @return
	 */
	public synchronized Set<String> getAllowedOrigins() {
		return allowedOrigins;
	}

	/**
	 * Defaults to true
	 *
	 * @return
	 */
	public synchronized boolean isAllowCredentials() {
		return allowCredentials;
	}

	public synchronized void setAllowCredentials(boolean allowCredentials) {
		this.allowCredentials = allowCredentials;
	}

	/**
	 * Will allow all by default
	 *
	 * @return
	 */
	public synchronized String getAllowedMethods() {
		return allowedMethods;
	}

	/**
	 * Will allow all by default comma delimited string for
	 * Access-Control-Allow-Methods
	 *
	 * @param allowedMethods
	 */
	public synchronized void setAllowedMethods(String allowedMethods) {
		this.allowedMethods = allowedMethods;
	}

	public synchronized String getAllowedHeaders() {
		return allowedHeaders;
	}

	/**
	 * Will allow all by default comma delimited string for
	 * Access-Control-Allow-Headers
	 *
	 * @param allowedHeaders
	 */
	public synchronized void setAllowedHeaders(String allowedHeaders) {
		this.allowedHeaders = allowedHeaders;
	}

	public synchronized int getCorsMaxAge() {
		return corsMaxAge;
	}

	public synchronized void setCorsMaxAge(int corsMaxAge) {
		this.corsMaxAge = corsMaxAge;
	}

	public synchronized String getExposedHeaders() {
		return exposedHeaders;
	}

	/**
	 * comma delimited list
	 *
	 * @param exposedHeaders
	 */
	public synchronized void setExposedHeaders(String exposedHeaders) {
		this.exposedHeaders = exposedHeaders;
	}

	@Override
	public void filter(ContainerRequestContext requestContext)
			throws IOException {

		log.debug("filter(requestContext)");

		if (!isAllowCors()) {
			log.debug("no CORS");
			return;
		}

		String origin = requestContext.getHeaderString(CorsHeaders.ORIGIN);
		if (origin == null) {
			log.debug("no origin in header, ignore");
			return;
		}
		if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
			preflight(origin, requestContext);
		} else {
			checkOrigin(requestContext, origin);
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext,
			ContainerResponseContext responseContext) throws IOException {

		log.debug("filter(reqeustContext,responseContext");

		if (!isAllowCors()) {
			log.debug("no CORS");
			return;
		}

		String origin = requestContext.getHeaderString(CorsHeaders.ORIGIN);
		if (origin == null
				|| requestContext.getMethod().equalsIgnoreCase("OPTIONS")
				|| requestContext.getProperty("cors.failure") != null) {
			// don't do anything if origin is null, its an OPTIONS request, or
			// cors.failure is set
			log.debug("no origin, not options, or cords.failure is set..ignore");

			return;
		}
		responseContext.getHeaders().putSingle(
				CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
		if (allowCredentials)
			responseContext.getHeaders().putSingle(
					CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

		if (exposedHeaders != null) {
			responseContext.getHeaders().putSingle(
					CorsHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, exposedHeaders);
		}
	}

	protected void preflight(String origin,
			ContainerRequestContext requestContext) throws IOException {

		log.debug("preflight");

		if (!isAllowCors()) {
			log.debug("no CORS");
			return;
		}

		log.info("check origin");
		checkOrigin(requestContext, origin);

		Response.ResponseBuilder builder = Response.ok();
		builder.header(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
		if (allowCredentials)
			builder.header(CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
		String requestMethods = requestContext
				.getHeaderString(CorsHeaders.ACCESS_CONTROL_REQUEST_METHOD);
		if (requestMethods != null) {
			if (allowedMethods != null) {
				requestMethods = this.allowedMethods;
			}
			builder.header(CorsHeaders.ACCESS_CONTROL_ALLOW_METHODS,
					requestMethods);
		}
		String allowHeaders = requestContext
				.getHeaderString(CorsHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
		if (allowHeaders != null) {
			if (allowedHeaders != null) {
				allowHeaders = this.allowedHeaders;
			}
			builder.header(CorsHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
					allowHeaders);
		}
		if (corsMaxAge > -1) {
			builder.header(CorsHeaders.ACCESS_CONTROL_MAX_AGE, corsMaxAge);
		}
		requestContext.abortWith(builder.build());

	}

	protected void checkOrigin(ContainerRequestContext requestContext,
			String origin) {
		if (!allowedOrigins.contains("*") && !allowedOrigins.contains(origin)) {
			requestContext.setProperty("cors.failure", true);
			throw new ForbiddenException("Origin not allowed: " + origin);
		}
	}

	public synchronized RestConfiguration getRestConfiguration() {
		return restConfiguration;
	}

	public synchronized void setRestConfiguration(
			RestConfiguration restConfiguration) {

		log.info("rest configuration set:{}", restConfiguration);

		this.restConfiguration = restConfiguration;

		log.info(
				"setting up cors filter parameters from new rest configuration:{}",
				restConfiguration);
		parseAllowedMethodsFromConfiguration();
		parseAllowedOriginsFromConfiguration();
		parseAllowCredentialsFromConfiguration();

	}

	private synchronized boolean isAllowCors() {
		log.debug("restConfiguration:{}", restConfiguration);

		if (!restConfiguration.isAllowCors()) {
			return false;
		} else {
			return true;
		}

	}

	private void parseAllowedMethodsFromConfiguration() {
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

		this.setAllowedMethods(sb.toString());
	}

	public void parseAllowedOriginsFromConfiguration() {

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

		this.allowedOrigins = allowedOrigins;

	}

	public void parseAllowCredentialsFromConfiguration() {
		this.allowCredentials = restConfiguration.isCorsAllowCredentials();
	}

}