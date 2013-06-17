package org.irods.jargon.rest.commands;

import java.util.Properties;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.rest.utils.RestTestingProperties;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
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

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user/");
		sb.append(testingProperties.get(TestingPropertiesHelper.IRODS_USER_KEY));
		// contentType doesn't really work in test container, set in the header
		sb.append("?contentType=application/json");

		final ClientRequest clientCreateRequest = new ClientRequest(
				sb.toString());
		clientCreateRequest.accept(MediaType.APPLICATION_JSON);

		final ClientResponse<String> clientCreateResponse = clientCreateRequest
				.get(String.class);
		Assert.assertEquals(200, clientCreateResponse.getStatus());
		String entity = clientCreateResponse.getEntity();
		Assert.assertNotNull(entity);
		System.out.println(">>>>>" + entity);
		Assert.assertFalse("did not get json with user name", entity.indexOf("\"name\":\"" + testingProperties.get(TestingPropertiesHelper.IRODS_USER_KEY) + "\"") == -1);
	}
	
	@Test
	public void testGetUserXML() throws Exception {

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
		clientCreateRequest.accept(MediaType.APPLICATION_XML);

		final ClientResponse<String> clientCreateResponse = clientCreateRequest
				.get(String.class);
		Assert.assertEquals(200, clientCreateResponse.getStatus());
		String entity = clientCreateResponse.getEntity();
		Assert.assertNotNull(entity);
		Assert.assertFalse("did not get json with user name", entity.indexOf("name=\""+ testingProperties.get(TestingPropertiesHelper.IRODS_USER_KEY) + "\"") == -1);
	}
	
	@Test
	public void testGetUserNull() throws Exception {

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user");
		// contentType doesn't really work in test container, set in the header
		sb.append("?contentType=application/xml");

		final ClientRequest clientCreateRequest = new ClientRequest(
				sb.toString());
		clientCreateRequest.accept(MediaType.APPLICATION_XML);

		final ClientResponse<String> clientCreateResponse = clientCreateRequest
				.get(String.class);
		Assert.assertEquals(405, clientCreateResponse.getStatus());
		
	}
	
	@Test
	public void testGetUserInvalid() throws Exception {

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

		final ClientResponse<String> clientCreateResponse = clientCreateRequest
				.get(String.class);
		Assert.assertEquals(500, clientCreateResponse.getStatus());
		String entity = clientCreateResponse.getEntity();
		Assert.assertNotNull(entity);
		Assert.assertFalse("did not get data not found exception", entity.indexOf("DataNotFoundExeption") != -1);

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
		UserAddByAdminRequest addRequest = new UserAddByAdminRequest();
		addRequest.setDistinguishedName("dn here");
		addRequest.setTempPassword(testPassword);
		addRequest.setUserName(testUser);
		clientCreateRequest.body(MediaType.APPLICATION_JSON, addRequest);

		final ClientResponse<String> clientCreateResponse = clientCreateRequest
				.put(String.class);
		
		User user = userAO.findByName(testUser);
		Assert.assertNotNull("user not added", user);
		
		
	}

	@Override
	public void setApplicationContext(final ApplicationContext context)
			throws BeansException {
		applicationContext = context;
	}

}
