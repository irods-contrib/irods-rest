package org.irods.jargon.rest.commands;

import java.net.URI;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.rest.utils.RestConstants;
import org.irods.jargon.rest.utils.RestTestingProperties;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.jboss.resteasy.plugins.spring.SpringBeanProcessor;
import org.jboss.resteasy.plugins.spring.SpringResourceFactory;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:jargon-beans.xml",
		"classpath:rest-servlet.xml" })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class })
public class UserServiceTest implements ApplicationContextAware {

	private static TJWSEmbeddedJaxrsServer server;

	private static ApplicationContext applicationContext;

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (server != null) {
			server.stop();
		}
		irodsFileSystem.closeAndEatExceptions();
	}

	@Before
	public void setUp() throws Exception {

		if (server != null) {
			return;
		}

		int port = testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY);
		server = new TJWSEmbeddedJaxrsServer();
		server.setPort(port);
		ResteasyDeployment deployment = server.getDeployment();
	
		server.start();
		Dispatcher dispatcher = deployment.getDispatcher();
		SpringBeanProcessor processor = new SpringBeanProcessor(dispatcher,
				deployment.getRegistry(), deployment.getProviderFactory());
		((ConfigurableApplicationContext) applicationContext)
				.addBeanFactoryPostProcessor(processor);

		SpringResourceFactory noDefaults = new SpringResourceFactory(
				"userService", applicationContext, UserService.class);
		dispatcher.getRegistry().addResourceFactory(noDefaults);

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetUserJSON() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user/");
		sb.append(testingProperties.get(TestingPropertiesHelper.IRODS_USER_KEY));
		// contentType doesn't really work in test container, set in the header
		sb.append("?contentType=application/json");

		Credentials credentials = new UsernamePasswordCredentials(
				irodsAccount.getUserName(), irodsAccount.getPassword());
		DefaultHttpClient httpClient = new DefaultHttpClient();
		CredentialsProvider provider = new BasicCredentialsProvider();
		provider.setCredentials(AuthScope.ANY, credentials);
		httpClient.setCredentialsProvider(provider);
		// Create AuthCache instance
		AuthCache authCache = new BasicAuthCache();
		// Generate BASIC scheme object and add it to the local
		// auth cache
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(new HttpHost("localhost"), basicAuth);

		// Add AuthCache to the execution context
		BasicHttpContext localcontext = new BasicHttpContext();
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
		ClientExecutor clientExecutor = new ApacheHttpClient4Executor(
				httpClient);

		URI uri = new URI(sb.toString());
		ClientRequestFactory fac = new ClientRequestFactory(clientExecutor, uri);

		ClientRequest clientCreateRequest = fac.createRequest(sb.toString());

		clientCreateRequest.accept(MediaType.APPLICATION_JSON);

		final ClientResponse<String> clientCreateResponse = clientCreateRequest
				.get(String.class);
		Assert.assertEquals(200, clientCreateResponse.getStatus());
		String entity = clientCreateResponse.getEntity();
		Assert.assertNotNull(entity);
		System.out.println(">>>>>" + entity);
		Assert.assertFalse("did not get json with user name", entity
				.indexOf("\"name\":\""
						+ testingProperties
								.get(TestingPropertiesHelper.IRODS_USER_KEY)
						+ "\"") == -1);
	}
	
	@Test
	public void testGetUserJSONBlah() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user/");
		sb.append(testingProperties.get(TestingPropertiesHelper.IRODS_USER_KEY));
		// contentType doesn't really work in test container, set in the header
		sb.append("?contentType=application/json");

		HttpHost targetHost = new HttpHost("localhost", testingPropertiesHelper.getPropertyValueAsInt(testingProperties, RestTestingProperties.REST_PORT_PROPERTY), "http");

        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            httpclient.getCredentialsProvider().setCredentials(
                    new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                    new UsernamePasswordCredentials(irodsAccount.getUserName(), irodsAccount.getPassword()));
            // Create AuthCache instance
            AuthCache authCache = new BasicAuthCache();
            // Generate BASIC scheme object and add it to the local
            // auth cache
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(targetHost, basicAuth);

            // Add AuthCache to the execution context
            BasicHttpContext localcontext = new BasicHttpContext();
            localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

            HttpGet httpget = new HttpGet(sb.toString());
            httpget.addHeader("accept", "application/json");

            System.out.println("executing request: " + httpget.getRequestLine());
            System.out.println("to target: " + targetHost);

            for (int i = 0; i < 3; i++) {
                HttpResponse response = httpclient.execute(targetHost, httpget, localcontext);
                HttpEntity entity = response.getEntity();
        		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        		Assert.assertNotNull(entity);
        		System.out.println(">>>>>" + entity);
        		String entityData = EntityUtils.toString(entity);
        		Assert.assertFalse("did not get json with user name", entityData
        				.indexOf("\"name\":\""
        						+ testingProperties
        								.get(TestingPropertiesHelper.IRODS_USER_KEY)
        						+ "\"") == -1);

                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                if (entity != null) {
                    System.out.println("Response content length: " + entity.getContentLength());
                }
                EntityUtils.consume(entity);
            }

        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
		
	}

	@Test
	public void testGetUserXML() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user/");
		sb.append(testingProperties.get(TestingPropertiesHelper.IRODS_USER_KEY));
		// contentType doesn't really work in test container, set in the header
		sb.append("?contentType=application/xml");

		final ClientRequest clientCreateRequest = new ClientRequest(
				sb.toString());
		clientCreateRequest.header(RestConstants.AUTH_RESULT_KEY, irodsAccount
				.toURI(true).toString());

		clientCreateRequest.accept(MediaType.APPLICATION_XML);

		final ClientResponse<String> clientCreateResponse = clientCreateRequest
				.get(String.class);
		Assert.assertEquals(200, clientCreateResponse.getStatus());
		String entity = clientCreateResponse.getEntity();
		Assert.assertNotNull(entity);
		Assert.assertFalse("did not get json with user name", entity
				.indexOf("name=\""
						+ testingProperties
								.get(TestingPropertiesHelper.IRODS_USER_KEY)
						+ "\"") == -1);
	}

	@Test
	public void testGetUserNull() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user");
		// contentType doesn't really work in test container, set in the header
		sb.append("?contentType=application/xml");

		final ClientRequest clientCreateRequest = new ClientRequest(
				sb.toString());
		clientCreateRequest.header(RestConstants.AUTH_RESULT_KEY, irodsAccount
				.toURI(true).toString());

		clientCreateRequest.accept(MediaType.APPLICATION_XML);

		final ClientResponse<String> clientCreateResponse = clientCreateRequest
				.get(String.class);
		Assert.assertEquals(405, clientCreateResponse.getStatus());

	}

	@Test
	public void testGetUserInvalid() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user/");
		sb.append("iamaninvaliduser");
		// contentType doesn't really work in test container, set in the header
		sb.append("?contentType=application/xml");

		final ClientRequest clientCreateRequest = new ClientRequest(
				sb.toString());
		clientCreateRequest.accept(MediaType.APPLICATION_XML);
		clientCreateRequest.header(RestConstants.AUTH_RESULT_KEY, irodsAccount
				.toURI(true).toString());

		final ClientResponse<String> clientCreateResponse = clientCreateRequest
				.get(String.class);
		Assert.assertEquals(500, clientCreateResponse.getStatus());
		String entity = clientCreateResponse.getEntity();
		Assert.assertNotNull(entity);
		Assert.assertFalse("did not get data not found exception",
				entity.indexOf("DataNotFoundExeption") != -1);

	}

	@Test
	public void testAddUserByAdmin() throws Exception {

		String testUser = "testAddUserByAdmin";
		String testPassword = "test123";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		userAO.deleteUser(testUser);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user/");
		final ClientRequest clientCreateRequest = new ClientRequest(
				sb.toString());
		clientCreateRequest.accept(MediaType.APPLICATION_JSON);
		clientCreateRequest.header(RestConstants.AUTH_RESULT_KEY, irodsAccount
				.toURI(true).toString());

		UserAddByAdminRequest addRequest = new UserAddByAdminRequest();
		addRequest.setDistinguishedName("dn here");
		addRequest.setTempPassword(testPassword);
		addRequest.setUserName(testUser);
		clientCreateRequest.body(MediaType.APPLICATION_JSON, addRequest);

		clientCreateRequest.put(String.class);

		User user = userAO.findByName(testUser);
		Assert.assertNotNull("user not added", user);

	}

	@Override
	public void setApplicationContext(final ApplicationContext context)
			throws BeansException {
		applicationContext = context;
	}

}
