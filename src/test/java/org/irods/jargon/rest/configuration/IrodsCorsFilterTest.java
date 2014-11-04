package org.irods.jargon.rest.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import junit.framework.Assert;

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

		IrodsCorsFilter filter = new IrodsCorsFilter();
		filter.setRestConfiguration(config);

		filter.filter(requestContext, responseContext);
		Assert.assertFalse("no headers set", responseContext.getHeaders()
				.isEmpty());
		Assert.assertEquals("did not set default cors origin", "[*]",
				map.get("Access-Control-Allow-Origin").toString());
		Assert.assertEquals("did not set default cors origin",
				"[GET, POST, DELETE, PUT]",
				map.get("Access-Control-Allow-Methods").toString());

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

		IrodsCorsFilter filter = new IrodsCorsFilter();
		filter.setRestConfiguration(config);

		filter.filter(requestContext, responseContext);
		Assert.assertFalse("no headers set", responseContext.getHeaders()
				.isEmpty());
		Assert.assertEquals("did not set default cors origin", "[bob,sally]",
				map.get("Access-Control-Allow-Origin").toString());
		Assert.assertEquals("did not set default cors origin",
				"[GET, POST, DELETE, PUT]",
				map.get("Access-Control-Allow-Methods").toString());

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

	@Test
	public void testFilterWithCorsActionList() throws Exception {
		RestConfiguration config = new RestConfiguration();

		List<String> actions = new ArrayList<String>();
		actions.add("POST");
		actions.add("PUT");

		config.setAllowCors(true);
		config.setCorsAllowCredentials(true);
		config.setCorsMethods(actions);

		ContainerRequestContext requestContext = Mockito
				.mock(ContainerRequestContext.class);
		ContainerResponseContext responseContext = Mockito
				.mock(ContainerResponseContext.class);
		MultivaluedMap<String, Object> map = new MultivaluedHashMap<String, Object>();
		Mockito.when(responseContext.getHeaders()).thenReturn(map);

		IrodsCorsFilter filter = new IrodsCorsFilter();
		filter.setRestConfiguration(config);

		filter.filter(requestContext, responseContext);
		Assert.assertFalse("no headers set", responseContext.getHeaders()
				.isEmpty());
		Assert.assertEquals("did not set methods", "[POST,PUT]",
				map.get("Access-Control-Allow-Methods").toString());

	}

}
