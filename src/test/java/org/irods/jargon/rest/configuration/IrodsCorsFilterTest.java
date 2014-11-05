package org.irods.jargon.rest.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import junit.framework.Assert;

import org.jboss.resteasy.spi.CorsHeaders;
import org.junit.Test;
import org.mockito.Mockito;

public class IrodsCorsFilterTest {

	@Test
	public void testFilterWithCors() throws Exception {
		RestConfiguration config = new RestConfiguration();
		config.setAllowCors(true);
		config.setCorsAllowCredentials(true);

		ContainerRequestContext requestContext = Mockito
				.mock(ContainerRequestContext.class);
		ContainerResponseContext responseContext = Mockito
				.mock(ContainerResponseContext.class);
		MultivaluedMap<String, Object> map = new MultivaluedHashMap<String, Object>();
		Mockito.when(responseContext.getHeaders()).thenReturn(map);

		Mockito.when(requestContext.getHeaderString(CorsHeaders.ORIGIN))
				.thenReturn("blah");

		Mockito.when(requestContext.getMethod()).thenReturn("PUT");

		IrodsCorsFilter filter = new IrodsCorsFilter();
		filter.setRestConfiguration(config);

		filter.filter(requestContext, responseContext);
		Assert.assertFalse("no headers set", responseContext.getHeaders()
				.isEmpty());
		Assert.assertEquals("did not set default cors origin", "[blah]", map
				.get("Access-Control-Allow-Origin").toString());

	}

	@Test(expected = ForbiddenException.class)
	public void testFilterWithCorsOriginListBadOriginOPreFlight()
			throws Exception {
		RestConfiguration config = new RestConfiguration();

		List<String> origins = new ArrayList<String>();
		origins.add("bob");
		origins.add("sally");

		config.setAllowCors(true);
		config.setCorsAllowCredentials(true);
		config.setCorsOrigins(origins);

		ContainerRequestContext requestContext = Mockito
				.mock(ContainerRequestContext.class);

		Mockito.when(requestContext.getHeaderString(CorsHeaders.ORIGIN))
				.thenReturn("steve");

		Mockito.when(requestContext.getMethod()).thenReturn("OPTIONS");
		IrodsCorsFilter filter = new IrodsCorsFilter();
		filter.setRestConfiguration(config);

		filter.filter(requestContext);

	}

	@Test
	public void testFilterWithCorsOriginListBadOriginOPreFlightCorsOff()
			throws Exception {
		RestConfiguration config = new RestConfiguration();

		List<String> origins = new ArrayList<String>();
		origins.add("bob");
		origins.add("sally");

		config.setAllowCors(false);
		config.setCorsAllowCredentials(true);
		config.setCorsOrigins(origins);

		ContainerRequestContext requestContext = Mockito
				.mock(ContainerRequestContext.class);

		Mockito.when(requestContext.getHeaderString(CorsHeaders.ORIGIN))
				.thenReturn("steve");

		Mockito.when(requestContext.getMethod()).thenReturn("OPTIONS");
		IrodsCorsFilter filter = new IrodsCorsFilter();
		filter.setRestConfiguration(config);

		filter.filter(requestContext);

	}

	@Test
	public void testFilterWithCorsOriginList() throws Exception {
		RestConfiguration config = new RestConfiguration();

		List<String> origins = new ArrayList<String>();
		origins.add("bob");
		origins.add("sally");

		config.setAllowCors(true);
		config.setCorsAllowCredentials(true);
		config.setCorsOrigins(origins);

		ContainerRequestContext requestContext = Mockito
				.mock(ContainerRequestContext.class);
		ContainerResponseContext responseContext = Mockito
				.mock(ContainerResponseContext.class);
		MultivaluedMap<String, Object> map = new MultivaluedHashMap<String, Object>();
		Mockito.when(responseContext.getHeaders()).thenReturn(map);

		Mockito.when(requestContext.getHeaderString(CorsHeaders.ORIGIN))
				.thenReturn("bob");

		Mockito.when(requestContext.getMethod()).thenReturn("PUT");
		IrodsCorsFilter filter = new IrodsCorsFilter();
		filter.setRestConfiguration(config);

		filter.filter(requestContext, responseContext);
		Assert.assertFalse("no headers set", responseContext.getHeaders()
				.isEmpty());
		Assert.assertEquals("did not set default cors origin", "[bob]", map
				.get("Access-Control-Allow-Origin").toString());
		/*
		 * Assert.assertEquals("did not set default cors origin",
		 * "[GET, POST, DELETE, PUT]",
		 * map.get("Access-Control-Allow-Methods").toString());
		 */

	}

	@Test
	public void testFilterWithNoCors() throws Exception {
		RestConfiguration config = new RestConfiguration();
		config.setAllowCors(false);
		config.setCorsAllowCredentials(true);

		ContainerRequestContext requestContext = Mockito
				.mock(ContainerRequestContext.class);
		ContainerResponseContext responseContext = Mockito
				.mock(ContainerResponseContext.class);
		MultivaluedMap<String, Object> map = new MultivaluedHashMap<String, Object>();
		Mockito.when(responseContext.getHeaders()).thenReturn(map);

		IrodsCorsFilter filter = new IrodsCorsFilter();
		filter.setRestConfiguration(config);

		filter.filter(requestContext, responseContext);
		Assert.assertTrue("headers set", responseContext.getHeaders().isEmpty());

	}

}
