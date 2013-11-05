/**
 * 
 */
package org.irods.jargon.rest.commands.collection;

import java.util.Properties;

import junit.framework.Assert;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.rest.auth.DefaultHttpClientAndContext;
import org.irods.jargon.rest.auth.RestAuthUtils;
import org.irods.jargon.rest.domain.CollectionData;
import org.irods.jargon.rest.utils.RestTestingProperties;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.jboss.resteasy.plugins.spring.SpringBeanProcessor;
import org.jboss.resteasy.plugins.spring.SpringResourceFactory;
import org.jboss.resteasy.spi.ResteasyDeployment;
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

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:jargon-beans.xml",
		"classpath:rest-servlet.xml" })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class })
public class CollectionServiceTest implements ApplicationContextAware {

	private static TJWSEmbeddedJaxrsServer server;

	private static ApplicationContext applicationContext;

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	public static final String IRODS_TEST_SUBDIR_PATH = "RestCollectionServiceTest";

	@Override
	public void setApplicationContext(final ApplicationContext context)
			throws BeansException {
		applicationContext = context;
	}

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
				"collectionService", applicationContext,
				CollectionService.class);
		dispatcher.getRegistry().addResourceFactory(noDefaults);

	}

	@Test
	public void testGetCollectionJsonNoListingNoOffset() throws Exception {
		String testDirName = "findByAbsolutePath";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		IRODSFile collFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		collFile.mkdirs();

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/collection");
		sb.append("?contentType=application/json");
		sb.append("&uri=");
		sb.append(collFile.toURI().toString());
		//sb.append("&listing=false");
		//sb.append("&offset=0");

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
			EntityUtils.consume(entity);
			System.out.println("JSON>>>");
			System.out.println(entityData);
			ObjectMapper objectMapper = new ObjectMapper();
			CollectionData actual = objectMapper.readValue(entityData,
					CollectionData.class);

			Collection collection = collectionAO
					.findByAbsolutePath(targetIrodsCollection);

			Assert.assertEquals(collection.getCollectionId(),
					actual.getCollectionId());
			Assert.assertEquals(collection.getCollectionInheritance(), actual.getCollectionInheritance());
			Assert.assertEquals(collection.getCollectionMapId(), actual.getCollectionMapId());
			Assert.assertEquals(collection.getCollectionName(), actual.getCollectionName());
			Assert.assertEquals(collection.getCollectionOwnerName(), actual.getCollectionOwnerName());
			Assert.assertEquals(collection.getCollectionOwnerZone(), actual.getCollectionOwnerZone());
			Assert.assertEquals(collection.getCollectionParentName(), actual.getCollectionParentName());
			Assert.assertEquals(collection.getComments(), actual.getComments());
			Assert.assertEquals(collection.getInfo1(), actual.getInfo1());
			Assert.assertEquals(collection.getInfo2(), actual.getInfo2());
			Assert.assertEquals(collection.getObjectPath(), actual.getObjectPath());
			Assert.assertEquals(collection.getCreatedAt(), actual.getCreatedAt());
			Assert.assertEquals(collection.getModifiedAt(), actual.getModifiedAt());
			Assert.assertEquals(collection.getSpecColType(), actual.getSpecColType());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}
	
	
	@Test
	public void testGetCollectionJsonWithListing() throws Exception {
		String testDirName = "testGetCollectionJsonWithListing";
		int count = 100;
		String subdirPrefix = "subdir";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);

		IRODSFile collFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		collFile.mkdirs();
		
		String myTarget = "";
		IRODSFile irodsFile;

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i)
					+ subdirPrefix;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(myTarget);
			irodsFile.mkdirs();
			irodsFile.close();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/collection");
		sb.append("?contentType=application/json");
		sb.append("&uri=");
		sb.append(collFile.toURI().toString());
		sb.append("&listing=true");
		sb.append("&offset=0");

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
			EntityUtils.consume(entity);
			System.out.println("JSON>>>");
			System.out.println(entityData);
			ObjectMapper objectMapper = new ObjectMapper();
			CollectionData actual = objectMapper.readValue(entityData,
					CollectionData.class);

			Collection collection = collectionAO
					.findByAbsolutePath(targetIrodsCollection);

			Assert.assertEquals(collection.getCollectionId(),
					actual.getCollectionId());
			Assert.assertEquals(collection.getCollectionInheritance(), actual.getCollectionInheritance());
			Assert.assertEquals(collection.getCollectionMapId(), actual.getCollectionMapId());
			Assert.assertEquals(collection.getCollectionName(), actual.getCollectionName());
			Assert.assertEquals(collection.getCollectionOwnerName(), actual.getCollectionOwnerName());
			Assert.assertEquals(collection.getCollectionOwnerZone(), actual.getCollectionOwnerZone());
			Assert.assertEquals(collection.getCollectionParentName(), actual.getCollectionParentName());
			Assert.assertEquals(collection.getComments(), actual.getComments());
			Assert.assertEquals(collection.getInfo1(), actual.getInfo1());
			Assert.assertEquals(collection.getInfo2(), actual.getInfo2());
			Assert.assertEquals(collection.getObjectPath(), actual.getObjectPath());
			Assert.assertEquals(collection.getCreatedAt(), actual.getCreatedAt());
			Assert.assertEquals(collection.getModifiedAt(), actual.getModifiedAt());
			Assert.assertEquals(collection.getSpecColType(), actual.getSpecColType());
			
			Assert.assertFalse("no children listed", actual.getChildren().isEmpty());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}
	
	@Test
	public void testGetCollectionXmlWithListing() throws Exception {
		String testDirName = "testGetCollectionXmlWithListing";
		int count = 1000;
		String subdirPrefix = "subdir";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile collFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		collFile.mkdirs();
		
		String myTarget = "";
		IRODSFile irodsFile;

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsCollection + "/c" + (10000 + i)
					+ subdirPrefix;
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(myTarget);
			irodsFile.mkdirs();
			irodsFile.close();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/collection");
		sb.append("?contentType=application/xml");
		sb.append("&uri=");
		sb.append(collFile.toURI().toString());
		sb.append("&listing=true");
		sb.append("&offset=0");

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
			EntityUtils.consume(entity);
			System.out.println("XML>>>");
			System.out.println(entityData);
			Assert.assertNotNull("null xml returned", entity);
			Assert.assertTrue("did not get expected xml stuff", entityData.indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:collection xmlns:ns2=\"http://irods.org/irods-rest\" ") > -1);
		
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}


}
