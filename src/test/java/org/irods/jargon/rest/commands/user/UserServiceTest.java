package org.irods.jargon.rest.commands.user;

import java.util.Properties;

import junit.framework.Assert;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.rest.auth.DefaultHttpClientAndContext;
import org.irods.jargon.rest.auth.RestAuthUtils;
import org.irods.jargon.rest.utils.RestTestingProperties;
import org.irods.jargon.testutils.TestingPropertiesHelper;
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

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpGet httpget = new HttpGet(sb.toString());
			httpget.addHeader("accept", "application/json");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpget, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);
			String entityData = EntityUtils.toString(entity);
			Assert.assertFalse(
					"did not get json with user name",
					entityData.indexOf("\"name\":\""
							+ testingProperties
									.get(TestingPropertiesHelper.IRODS_USER_KEY)
							+ "\"") == -1);

			EntityUtils.consume(entity);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

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

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpGet httpget = new HttpGet(sb.toString());
			httpget.addHeader("accept", "application/xml");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpget, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);
			String entityData = EntityUtils.toString(entity);
			Assert.assertFalse(
					"did not get json with user name",
					entityData.indexOf("name=\""
							+ testingProperties
									.get(TestingPropertiesHelper.IRODS_USER_KEY)
							+ "\"") == -1);

			EntityUtils.consume(entity);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Test
	public void testGetUserNull() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user/");

		// contentType doesn't really work in test container, set in the header
		sb.append("?contentType=application/xml");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpGet httpget = new HttpGet(sb.toString());
			httpget.addHeader("accept", "application/xml");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpget, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(405, response.getStatusLine().getStatusCode());
			EntityUtils.consume(entity);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testGetUserInvalid() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user/iamaninvaliduser");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpGet httpget = new HttpGet(sb.toString());
			httpget.addHeader("accept", "application/xml");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpget, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(500, response.getStatusLine().getStatusCode());
			EntityUtils.consume(entity);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Test
	public void testAddUserByAdmin() throws Exception {

		String testUser = "testAddUserByAdmin";
		String testPassword = "test123";
		String testDn = "testDNForaddubyAdmin";
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

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPut httpPut = new HttpPut(sb.toString());
			httpPut.addHeader("accept", "application/json");
			httpPut.addHeader("Content-Type", "application/json");

			ObjectMapper mapper = new ObjectMapper();
			UserAddByAdminRequest addRequest = new UserAddByAdminRequest();
			addRequest.setDistinguishedName(testDn);
			addRequest.setTempPassword(testPassword);
			addRequest.setUserName(testUser);
			String body = mapper.writeValueAsString(addRequest);

			System.out.println(body);

			httpPut.setEntity(new StringEntity(body));

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPut, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);

			System.out.println(entityData);

			UserAddActionResponse actual = mapper.readValue(entityData,
					UserAddActionResponse.class);
			Assert.assertEquals(testUser, actual.getUserName());
			Assert.assertEquals(
					UserAddActionResponse.UserAddActionResponseCode.SUCCESS,
					actual.getUserAddActionResponse());
			Assert.assertEquals(
					UserAddActionResponse.UserAddActionResponseCode.SUCCESS
							.ordinal(), actual
							.getUserAddActionResponseNumericCode());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

		User user = userAO.findByName(testUser);
		Assert.assertNotNull("user not added", user);
		Assert.assertEquals("dn not set", testDn, user.getUserDN());

	}

	@Test
	public void testAddUserDuplicateByAdmin() throws Exception {

		String testUser = "testAddUserDuplicateByAdmin";
		String testPassword = "test123";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		UserAO userAO = accessObjectFactory.getUserAO(irodsAccount);
		userAO.deleteUser(testUser);
		User user = new User();
		user.setName(testUser);
		user.setUserType(UserTypeEnum.RODS_USER);
		userAO.addUser(user);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user/");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPut httpPut = new HttpPut(sb.toString());
			httpPut.addHeader("accept", "application/json");
			httpPut.addHeader("Content-Type", "application/json");

			ObjectMapper mapper = new ObjectMapper();
			UserAddByAdminRequest addRequest = new UserAddByAdminRequest();
			addRequest.setDistinguishedName("dn here");
			addRequest.setTempPassword(testPassword);
			addRequest.setUserName(testUser);
			String body = mapper.writeValueAsString(addRequest);

			System.out.println(body);

			httpPut.setEntity(new StringEntity(body));

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPut, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);

			System.out.println(entityData);

			UserAddActionResponse actual = mapper.readValue(entityData,
					UserAddActionResponse.class);
			Assert.assertEquals(testUser, actual.getUserName());
			Assert.assertEquals(
					UserAddActionResponse.UserAddActionResponseCode.USER_NAME_IS_TAKEN,
					actual.getUserAddActionResponse());
			Assert.assertEquals(
					UserAddActionResponse.UserAddActionResponseCode.USER_NAME_IS_TAKEN
							.ordinal(), actual
							.getUserAddActionResponseNumericCode());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testAddUserByAdminInvalidMessage() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user/");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPut httpPut = new HttpPut(sb.toString());
			httpPut.addHeader("accept", "application/json");
			httpPut.addHeader("Content-Type", "application/json");
			String body = "I am not valid json";
			httpPut.setEntity(new StringEntity(body));

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPut, clientAndContext.getHttpContext());
			Assert.assertEquals(400, response.getStatusLine().getStatusCode());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testAddUserByAdminBlankUserName() throws Exception {

		String testPassword = "test123";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user/");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPut httpPut = new HttpPut(sb.toString());
			httpPut.addHeader("accept", "application/json");
			httpPut.addHeader("Content-Type", "application/json");

			ObjectMapper mapper = new ObjectMapper();
			UserAddByAdminRequest addRequest = new UserAddByAdminRequest();
			addRequest.setDistinguishedName("dn here");
			addRequest.setTempPassword(testPassword);
			addRequest.setUserName("");
			String body = mapper.writeValueAsString(addRequest);
			httpPut.setEntity(new StringEntity(body));

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPut, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);
			UserAddActionResponse actual = mapper.readValue(entityData,
					UserAddActionResponse.class);
			Assert.assertEquals(
					UserAddActionResponse.UserAddActionResponseCode.ATTRIBUTES_MISSING,
					actual.getUserAddActionResponse());
			Assert.assertEquals(
					UserAddActionResponse.UserAddActionResponseCode.ATTRIBUTES_MISSING
							.ordinal(), actual
							.getUserAddActionResponseNumericCode());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testAddUserByAdminBlankPassword() throws Exception {

		String testUserName = "test123";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user/");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPut httpPut = new HttpPut(sb.toString());
			httpPut.addHeader("accept", "application/json");
			httpPut.addHeader("Content-Type", "application/json");

			ObjectMapper mapper = new ObjectMapper();
			UserAddByAdminRequest addRequest = new UserAddByAdminRequest();
			addRequest.setDistinguishedName("dn here");
			addRequest.setTempPassword("");
			addRequest.setUserName(testUserName);
			String body = mapper.writeValueAsString(addRequest);
			httpPut.setEntity(new StringEntity(body));

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPut, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);
			UserAddActionResponse actual = mapper.readValue(entityData,
					UserAddActionResponse.class);
			Assert.assertEquals(
					UserAddActionResponse.UserAddActionResponseCode.ATTRIBUTES_MISSING,
					actual.getUserAddActionResponse());
			Assert.assertEquals(
					UserAddActionResponse.UserAddActionResponseCode.ATTRIBUTES_MISSING
							.ordinal(), actual
							.getUserAddActionResponseNumericCode());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Override
	public void setApplicationContext(final ApplicationContext context)
			throws BeansException {
		applicationContext = context;
	}

}
