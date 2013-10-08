package org.irods.jargon.rest.commands.user;

import java.net.URLEncoder;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.UserGroupAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.rest.auth.DefaultHttpClientAndContext;
import org.irods.jargon.rest.auth.RestAuthUtils;
import org.irods.jargon.rest.commands.GenericCommandResponse;
import org.irods.jargon.rest.commands.user.UserGroupCommandResponse.UserGroupCommandStatus;
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
public class UserGroupServiceTest implements ApplicationContextAware {

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
				"userGroupService", applicationContext, UserGroupService.class);
		dispatcher.getRegistry().addResourceFactory(noDefaults);

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddUserToGroup() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user_group/user");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);

		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		String userGroupName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY);
		userGroupAO.removeUserFromGroup(userGroupName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
				irodsAccount.getZone());

		try {

			HttpPut httpPut = new HttpPut(sb.toString());
			httpPut.addHeader("accept", "application/json");
			httpPut.addHeader("Content-Type", "application/json");

			ObjectMapper mapper = new ObjectMapper();
			UserGroupMembershipRequest userAddToGroupRequest = new UserGroupMembershipRequest();
			userAddToGroupRequest.setUserGroup(testingProperties
					.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY));
			userAddToGroupRequest.setZone(irodsAccount.getZone());
			userAddToGroupRequest
					.setUserName(testingProperties
							.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

			String body = mapper.writeValueAsString(userAddToGroupRequest);

			System.out.println(body);

			httpPut.setEntity(new StringEntity(body));

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPut, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);

			System.out.println(entityData);

			UserGroupCommandResponse actual = mapper.readValue(entityData,
					UserGroupCommandResponse.class);
			Assert.assertEquals(GenericCommandResponse.Status.OK,
					actual.getStatus());
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Test
	public void testAddUserToGroupDuplicateUser() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user_group/user");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);

		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		String userGroupName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY);
		userGroupAO.removeUserFromGroup(userGroupName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
				irodsAccount.getZone());
		userGroupAO.addUserToGroup(userGroupName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
				irodsAccount.getZone());

		try {

			HttpPut httpPut = new HttpPut(sb.toString());
			httpPut.addHeader("accept", "application/json");
			httpPut.addHeader("Content-Type", "application/json");

			ObjectMapper mapper = new ObjectMapper();
			UserGroupMembershipRequest userAddToGroupRequest = new UserGroupMembershipRequest();
			userAddToGroupRequest.setUserGroup(testingProperties
					.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY));
			userAddToGroupRequest.setZone(irodsAccount.getZone());
			userAddToGroupRequest
					.setUserName(testingProperties
							.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

			String body = mapper.writeValueAsString(userAddToGroupRequest);

			System.out.println(body);

			httpPut.setEntity(new StringEntity(body));

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPut, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);

			System.out.println(entityData);

			UserGroupCommandResponse actual = mapper.readValue(entityData,
					UserGroupCommandResponse.class);
			Assert.assertEquals(GenericCommandResponse.Status.ERROR,
					actual.getStatus());

			Assert.assertEquals(
					UserGroupCommandResponse.UserGroupCommandStatus.DUPLICATE_USER,
					actual.getUserGroupCommandStatus());

		} finally {

			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Test
	public void testAddUserToGroupBogusUser() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user_group/user");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);

		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		String userGroupName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY);
		userGroupAO.removeUserFromGroup(userGroupName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
				irodsAccount.getZone());

		try {

			HttpPut httpPut = new HttpPut(sb.toString());
			httpPut.addHeader("accept", "application/json");
			httpPut.addHeader("Content-Type", "application/json");

			ObjectMapper mapper = new ObjectMapper();
			UserGroupMembershipRequest userAddToGroupRequest = new UserGroupMembershipRequest();
			userAddToGroupRequest.setUserGroup(testingProperties
					.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY));
			userAddToGroupRequest.setZone(irodsAccount.getZone());
			userAddToGroupRequest.setUserName("bogususer");

			String body = mapper.writeValueAsString(userAddToGroupRequest);

			System.out.println(body);

			httpPut.setEntity(new StringEntity(body));

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPut, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);

			System.out.println(entityData);

			UserGroupCommandResponse actual = mapper.readValue(entityData,
					UserGroupCommandResponse.class);
			Assert.assertEquals(GenericCommandResponse.Status.ERROR,
					actual.getStatus());
			Assert.assertEquals(
					UserGroupCommandResponse.UserGroupCommandStatus.INVALID_USER,
					actual.getUserGroupCommandStatus());
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Test
	public void testAddUserToGroupBogusGroup() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user_group/user");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);

		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		String userGroupName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY);
		userGroupAO.removeUserFromGroup(userGroupName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
				irodsAccount.getZone());

		try {

			HttpPut httpPut = new HttpPut(sb.toString());
			httpPut.addHeader("accept", "application/json");
			httpPut.addHeader("Content-Type", "application/json");

			ObjectMapper mapper = new ObjectMapper();
			UserGroupMembershipRequest userAddToGroupRequest = new UserGroupMembershipRequest();
			userAddToGroupRequest.setUserGroup("bogusGroup");
			userAddToGroupRequest.setZone(irodsAccount.getZone());
			userAddToGroupRequest
					.setUserName(testingProperties
							.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

			String body = mapper.writeValueAsString(userAddToGroupRequest);

			System.out.println(body);

			httpPut.setEntity(new StringEntity(body));

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPut, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);

			System.out.println(entityData);

			UserGroupCommandResponse actual = mapper.readValue(entityData,
					UserGroupCommandResponse.class);
			Assert.assertEquals(GenericCommandResponse.Status.ERROR,
					actual.getStatus());
			Assert.assertEquals(
					UserGroupCommandResponse.UserGroupCommandStatus.INVALID_GROUP,
					actual.getUserGroupCommandStatus());
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Test
	public void testRemoveUserFromGroup() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);

		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		String userGroupName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY);
		userGroupAO.removeUserFromGroup(userGroupName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
				irodsAccount.getZone());

		userGroupAO.addUserToGroup(userGroupName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
				irodsAccount.getZone());

		String userGroup = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY);
		irodsAccount.getZone();
		String userName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user_group/");
		sb.append(userGroup);
		sb.append("/user/");
		sb.append(userName);

		try {

			HttpDelete httpDelete = new HttpDelete(sb.toString());
			httpDelete.addHeader("accept", "application/json");
			httpDelete.addHeader("Content-Type", "application/json");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpDelete, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);

			System.out.println(entityData);

			ObjectMapper mapper = new ObjectMapper();

			UserGroupCommandResponse actual = mapper.readValue(entityData,
					UserGroupCommandResponse.class);
			Assert.assertEquals(GenericCommandResponse.Status.OK,
					actual.getStatus());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}
	
	
	@Test
	public void testRemoveUserFromGroupNotInGroup() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);

		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		String userGroupName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY);
		userGroupAO.removeUserFromGroup(userGroupName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
				irodsAccount.getZone());

		String userGroup = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY);
		irodsAccount.getZone();
		String userName = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user_group/");
		sb.append(userGroup);
		sb.append("/user/");
		sb.append(userName);

		try {

			HttpDelete httpDelete = new HttpDelete(sb.toString());
			httpDelete.addHeader("accept", "application/json");
			httpDelete.addHeader("Content-Type", "application/json");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpDelete, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);

			System.out.println(entityData);

			ObjectMapper mapper = new ObjectMapper();

			UserGroupCommandResponse actual = mapper.readValue(entityData,
					UserGroupCommandResponse.class);
			Assert.assertEquals(GenericCommandResponse.Status.OK,
					actual.getStatus());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}
	
	@Test
	public void testRemoveUserFromGroupNotInGroupAndNotExists() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);

		
		String userGroup = testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY);
		irodsAccount.getZone();
		String userName = "iambogus";

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user_group/");
		sb.append(userGroup);
		sb.append("/user/");
		sb.append(userName);

		try {

			HttpDelete httpDelete = new HttpDelete(sb.toString());
			httpDelete.addHeader("accept", "application/json");
			httpDelete.addHeader("Content-Type", "application/json");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpDelete, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);

			System.out.println(entityData);

			ObjectMapper mapper = new ObjectMapper();

			UserGroupCommandResponse actual = mapper.readValue(entityData,
					UserGroupCommandResponse.class);
			Assert.assertEquals(GenericCommandResponse.Status.ERROR,
					actual.getStatus());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}


	@Test
	public void testRemoveUserFromGroupHyphenated() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);

		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);

		String testUserGroupName = "geni-ahtest2";
		String testUserName = "geni-ahelsing";

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroupName);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(testUserGroupName);
		userGroupAO.addUserGroup(userGroup);

		User testUser = new User();
		testUser.setName(testUserName);
		testUser.setUserType(UserTypeEnum.RODS_USER);

		UserAO userAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getUserAO(irodsAccount);
		userAO.deleteUser(testUserName);
		userAO.addUser(testUser);

		userGroupAO.removeUserFromGroup(testUserGroupName, testUserName,
				irodsAccount.getZone());

		userGroupAO.addUserToGroup(testUserGroupName, testUserName,
				irodsAccount.getZone());

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user_group/");
		sb.append(URLEncoder.encode(testUserGroupName));
		sb.append("/user/");
		sb.append(URLEncoder.encode(testUserName));

		System.out.println("request url:" + sb.toString());

		try {

			HttpDelete httpDelete = new HttpDelete(sb.toString());
			httpDelete.addHeader("accept", "application/json");
			// httpDelete.addHeader("Content-Type", "application/json");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpDelete, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);

			System.out.println(entityData);

			ObjectMapper mapper = new ObjectMapper();

			UserGroupCommandResponse actual = mapper.readValue(entityData,
					UserGroupCommandResponse.class);
			Assert.assertEquals(GenericCommandResponse.Status.OK,
					actual.getStatus());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Test
	public void testAddUserGroup() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user_group");

		String testUserGroup = "testAddUserGroup";

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);

		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);

		userGroupAO.removeUserGroup(testUserGroup);

		try {

			HttpPut httpPut = new HttpPut(sb.toString());
			httpPut.addHeader("accept", "application/json");
			httpPut.addHeader("Content-Type", "application/json");

			ObjectMapper mapper = new ObjectMapper();
			UserGroupRequest userGroupRequest = new UserGroupRequest();
			userGroupRequest.setUserGroupName(testUserGroup);
			userGroupRequest.setZone(irodsAccount.getZone());

			String body = mapper.writeValueAsString(userGroupRequest);
			System.out.println(body);
			httpPut.setEntity(new StringEntity(body));

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPut, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);

			System.out.println(entityData);

			UserGroupCommandResponse actual = mapper.readValue(entityData,
					UserGroupCommandResponse.class);
			Assert.assertEquals(GenericCommandResponse.Status.OK,
					actual.getStatus());

			userGroupAO.removeUserGroup(testUserGroup);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Test
	public void testAddDuplicateUserGroup() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user_group");

		String testUserGroup = "testAddUserGroup";

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);

		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);

		userGroupAO.removeUserGroup(testUserGroup);
		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(testUserGroup);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.addUserGroup(userGroup);

		try {

			HttpPut httpPut = new HttpPut(sb.toString());
			httpPut.addHeader("accept", "application/json");
			httpPut.addHeader("Content-Type", "application/json");

			ObjectMapper mapper = new ObjectMapper();
			UserGroupRequest userGroupRequest = new UserGroupRequest();
			userGroupRequest.setUserGroupName(testUserGroup);
			userGroupRequest.setZone(irodsAccount.getZone());

			String body = mapper.writeValueAsString(userGroupRequest);
			System.out.println(body);
			httpPut.setEntity(new StringEntity(body));

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPut, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			String entityData = EntityUtils.toString(entity);

			System.out.println(entityData);
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());

			UserGroupCommandResponse actual = mapper.readValue(entityData,
					UserGroupCommandResponse.class);
			Assert.assertEquals(GenericCommandResponse.Status.ERROR,
					actual.getStatus());
			Assert.assertEquals(UserGroupCommandStatus.DUPLICATE_GROUP,
					actual.getUserGroupCommandStatus());

			userGroupAO.removeUserGroup(testUserGroup);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Test
	public void testRemoveUserGroup() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);

		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		String userGroupName = "testRemoveUserGroup";

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(userGroupName);
		userGroup.setZone(irodsAccount.getZone());

		userGroupAO.removeUserGroup(userGroupName);
		userGroupAO.addUserGroup(userGroup);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user_group/");
		sb.append(userGroupName);

		try {

			HttpDelete httpDelete = new HttpDelete(sb.toString());
			httpDelete.addHeader("accept", "application/json");
			httpDelete.addHeader("Content-Type", "application/json");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpDelete, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);

			System.out.println(entityData);

			ObjectMapper mapper = new ObjectMapper();

			UserGroupCommandResponse actual = mapper.readValue(entityData,
					UserGroupCommandResponse.class);
			Assert.assertEquals(GenericCommandResponse.Status.OK,
					actual.getStatus());

			UserGroup actualUserGroup = userGroupAO.findByName(userGroupName);
			Assert.assertNull("did not remove user group", actualUserGroup);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Test
	public void testRemoveUserGroupNotExists() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);

		UserGroupAO userGroupAO = accessObjectFactory
				.getUserGroupAO(irodsAccount);
		String userGroupName = "testRemoveUserGroupNotExists";

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(userGroupName);
		userGroup.setZone(irodsAccount.getZone());

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/user_group/");
		sb.append(userGroupName);

		try {

			HttpDelete httpDelete = new HttpDelete(sb.toString());
			httpDelete.addHeader("accept", "application/json");
			httpDelete.addHeader("Content-Type", "application/json");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpDelete, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);

			System.out.println(entityData);

			ObjectMapper mapper = new ObjectMapper();

			UserGroupCommandResponse actual = mapper.readValue(entityData,
					UserGroupCommandResponse.class);
			Assert.assertEquals(GenericCommandResponse.Status.OK,
					actual.getStatus());

			UserGroup actualUserGroup = userGroupAO.findByName(userGroupName);
			Assert.assertNull("did not remove user group", actualUserGroup);

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
