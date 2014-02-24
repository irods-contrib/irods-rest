/**
 * 
 */
package org.irods.jargon.rest.commands.collection;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import junit.framework.Assert;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.rest.auth.DefaultHttpClientAndContext;
import org.irods.jargon.rest.auth.RestAuthUtils;
import org.irods.jargon.rest.domain.CollectionData;
import org.irods.jargon.rest.domain.MetadataEntry;
import org.irods.jargon.rest.domain.MetadataListing;
import org.irods.jargon.rest.domain.MetadataOperation;
import org.irods.jargon.rest.domain.MetadataOperationResultEntry;
import org.irods.jargon.rest.domain.MetadataQueryResultEntry;
import org.irods.jargon.rest.domain.PermissionListing;
import org.irods.jargon.rest.utils.DataUtils;
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
		sb.append("/collection/");
		sb.append(DataUtils.encodeIrodsAbsolutePath(collFile.getAbsolutePath(),
				accessObjectFactory.getJargonProperties().getEncoding()));

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
			Assert.assertEquals(collection.getCollectionInheritance(),
					actual.getCollectionInheritance());
			Assert.assertEquals(collection.getCollectionMapId(),
					actual.getCollectionMapId());
			Assert.assertEquals(collection.getCollectionName(),
					actual.getCollectionName());
			Assert.assertEquals(collection.getCollectionOwnerName(),
					actual.getCollectionOwnerName());
			Assert.assertEquals(collection.getCollectionOwnerZone(),
					actual.getCollectionOwnerZone());
			Assert.assertEquals(collection.getCollectionParentName(),
					actual.getCollectionParentName());
			Assert.assertEquals(collection.getComments(), actual.getComments());
			Assert.assertEquals(collection.getInfo1(), actual.getInfo1());
			Assert.assertEquals(collection.getInfo2(), actual.getInfo2());
			Assert.assertEquals(collection.getObjectPath(),
					actual.getObjectPath());
			Assert.assertEquals(collection.getCreatedAt(),
					actual.getCreatedAt());
			Assert.assertEquals(collection.getModifiedAt(),
					actual.getModifiedAt());
			Assert.assertEquals(collection.getSpecColType(),
					actual.getSpecColType());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testAddNewCollection() throws Exception {
		String testDirName = "testAddNewCollection";

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
		sb.append("/collection/");
		sb.append(DataUtils.encodeIrodsAbsolutePath(collFile.getAbsolutePath(),
				accessObjectFactory.getJargonProperties().getEncoding()));

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPut httpPut = new HttpPut(sb.toString());
			httpPut.addHeader("accept", "application/json");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPut, clientAndContext.getHttpContext());

			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);
			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);

			// test a second put for idempotency

			response = clientAndContext.getHttpClient().execute(httpPut,
					clientAndContext.getHttpContext());

			entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);
			entityData = EntityUtils.toString(entity);
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
			Assert.assertEquals(collection.getCollectionInheritance(),
					actual.getCollectionInheritance());
			Assert.assertEquals(collection.getCollectionMapId(),
					actual.getCollectionMapId());
			Assert.assertEquals(collection.getCollectionName(),
					actual.getCollectionName());
			Assert.assertEquals(collection.getCollectionOwnerName(),
					actual.getCollectionOwnerName());
			Assert.assertEquals(collection.getCollectionOwnerZone(),
					actual.getCollectionOwnerZone());
			Assert.assertEquals(collection.getCollectionParentName(),
					actual.getCollectionParentName());
			Assert.assertEquals(collection.getComments(), actual.getComments());
			Assert.assertEquals(collection.getInfo1(), actual.getInfo1());
			Assert.assertEquals(collection.getInfo2(), actual.getInfo2());
			Assert.assertEquals(collection.getObjectPath(),
					actual.getObjectPath());
			Assert.assertEquals(collection.getCreatedAt(),
					actual.getCreatedAt());
			Assert.assertEquals(collection.getModifiedAt(),
					actual.getModifiedAt());
			Assert.assertEquals(collection.getSpecColType(),
					actual.getSpecColType());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testGetCollectionJsonNoListingQMarkInNameAndSpaces()
			throws Exception {
		String testDirName = "how about this!!!!(/$that/&=testGetCollectionJsonNoListingQMarkInNameAndSpaces";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		// targetIrodsCollection = URLEncoder.encode(targetIrodsCollection);

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
		sb.append("/collection/");
		sb.append(DataUtils.encodeIrodsAbsolutePath(collFile.getAbsolutePath(),
				accessObjectFactory.getJargonProperties().getEncoding()));

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
			Assert.assertEquals(collection.getCollectionInheritance(),
					actual.getCollectionInheritance());
			Assert.assertEquals(collection.getCollectionMapId(),
					actual.getCollectionMapId());
			Assert.assertEquals(collection.getCollectionName(),
					actual.getCollectionName());
			Assert.assertEquals(collection.getCollectionOwnerName(),
					actual.getCollectionOwnerName());
			Assert.assertEquals(collection.getCollectionOwnerZone(),
					actual.getCollectionOwnerZone());
			Assert.assertEquals(collection.getCollectionParentName(),
					actual.getCollectionParentName());
			Assert.assertEquals(collection.getComments(), actual.getComments());
			Assert.assertEquals(collection.getInfo1(), actual.getInfo1());
			Assert.assertEquals(collection.getInfo2(), actual.getInfo2());
			Assert.assertEquals(collection.getObjectPath(),
					actual.getObjectPath());
			Assert.assertEquals(collection.getCreatedAt(),
					actual.getCreatedAt());
			Assert.assertEquals(collection.getModifiedAt(),
					actual.getModifiedAt());
			Assert.assertEquals(collection.getSpecColType(),
					actual.getSpecColType());

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
		int count = 10;
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
		sb.append("/collection/");
		sb.append(collFile.getAbsolutePath());
		sb.append("?listing=true");
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
			Assert.assertEquals(collection.getCollectionInheritance(),
					actual.getCollectionInheritance());
			Assert.assertEquals(collection.getCollectionMapId(),
					actual.getCollectionMapId());
			Assert.assertEquals(collection.getCollectionName(),
					actual.getCollectionName());
			Assert.assertEquals(collection.getCollectionOwnerName(),
					actual.getCollectionOwnerName());
			Assert.assertEquals(collection.getCollectionOwnerZone(),
					actual.getCollectionOwnerZone());
			Assert.assertEquals(collection.getCollectionParentName(),
					actual.getCollectionParentName());
			Assert.assertEquals(collection.getComments(), actual.getComments());
			Assert.assertEquals(collection.getInfo1(), actual.getInfo1());
			Assert.assertEquals(collection.getInfo2(), actual.getInfo2());
			Assert.assertEquals(collection.getObjectPath(),
					actual.getObjectPath());
			Assert.assertEquals(collection.getCreatedAt(),
					actual.getCreatedAt());
			Assert.assertEquals(collection.getModifiedAt(),
					actual.getModifiedAt());
			Assert.assertEquals(collection.getSpecColType(),
					actual.getSpecColType());

			Assert.assertFalse("no children listed", actual.getChildren()
					.isEmpty());

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
		sb.append("/collection/");
		sb.append(DataUtils.encodeIrodsAbsolutePath(collFile.getAbsolutePath(),
				accessObjectFactory.getJargonProperties().getEncoding()));
		sb.append("?contentType=application/xml");
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
			Assert.assertTrue(
					"did not get expected xml stuff",
					entityData
							.indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:collection xmlns:ns2=\"http://irods.org/irods-rest\" ") > -1);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Test
	public void testGetCollectionMetadataListingJson() throws Exception {
		String testDirName = "testGetCollectionMetadataListingJson";

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

		// initialize the AVU data
		String expectedAttribName = "testmdattrib1".toUpperCase();
		String expectedAttribValue = "testmdvalue1";
		String expectedAttribUnits = "test1mdunits";
		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);
		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);

		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/collection");
		sb.append(collFile.getAbsolutePath());
		sb.append("/metadata");
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
			EntityUtils.consume(entity);
			System.out.println("JSON>>>");
			System.out.println(entityData);
			ObjectMapper objectMapper = new ObjectMapper();
			MetadataListing actual = objectMapper.readValue(entityData,
					MetadataListing.class);

			Assert.assertNotNull("null metadata listing found", actual);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testGetCollectionMetadataListingJsonWithMetadataInFileName()
			throws Exception {
		String testDirName = "blah/metadata/blahblah/metadata/testGetCollectionMetadataListingJsonWithMetadataInFileName";

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

		// initialize the AVU data
		String expectedAttribName = "testmdattrib1".toUpperCase();
		String expectedAttribValue = "testmdvalue1";
		String expectedAttribUnits = "test1mdunits";
		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);
		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);

		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/collection");
		sb.append(collFile.getAbsolutePath());
		sb.append("/metadata");
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
			EntityUtils.consume(entity);
			System.out.println("JSON>>>");
			System.out.println(entityData);
			ObjectMapper objectMapper = new ObjectMapper();
			MetadataListing actual = objectMapper.readValue(entityData,
					MetadataListing.class);

			Assert.assertNotNull("null metadata listing found", actual);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testGetCollectionMetadataListingXML() throws Exception {
		String testDirName = "testGetCollectionMetadataListingXML";

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

		// initialize the AVU data
		String expectedAttribName = "testmdattrib1".toUpperCase();
		String expectedAttribValue = "testmdvalue1";
		String expectedAttribUnits = "test1mdunits";
		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);
		collectionAO.deleteAVUMetadata(targetIrodsCollection, avuData);

		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/collection");
		sb.append(collFile.getAbsolutePath());
		sb.append("/metadata");

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
			Assert.assertTrue("did not get expected xml stuff",
					entityData.indexOf("<?xml version=\"1.0\"") > -1);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testBulkAddCollectionAVUSendJson() throws Exception {
		String testDirName = "testBulkAddCollectionAVUJson";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		String testAvuAttrib1 = "testBulkAddCollectionAVUJsonAttr1";
		String testAvuValue1 = "testBulkAddCollectionAVUJsonValue1";
		String testAvuUnit1 = "testBulkAddCollectionAVUJsonUnit1";

		String testAvuAttrib2 = "testBulkAddCollectionAVUJsonAttr2";
		String testAvuValue2 = "testBulkAddCollectionAVUJsonValue2";
		String testAvuUnit2 = "testBulkAddCollectionAVUJsonUnit2";

		MetadataOperation metadataOperation = new MetadataOperation();

		MetadataEntry metadataEntry = new MetadataEntry();
		metadataEntry.setAttribute(testAvuAttrib1);
		metadataEntry.setValue(testAvuValue1);
		metadataEntry.setUnit(testAvuUnit1);
		metadataOperation.getMetadataEntries().add(metadataEntry);

		metadataEntry = new MetadataEntry();
		metadataEntry.setAttribute(testAvuAttrib2);
		metadataEntry.setValue(testAvuValue2);
		metadataEntry.setUnit(testAvuUnit2);
		metadataOperation.getMetadataEntries().add(metadataEntry);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile collFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		collFile.mkdirs();

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/collection");
		sb.append(collFile.getAbsolutePath());
		sb.append("/metadata");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);

		try {

			HttpPut httpPut = new HttpPut(sb.toString());
			httpPut.addHeader("accept", "application/json");
			httpPut.addHeader("Content-Type", "application/json");

			ObjectMapper mapper = new ObjectMapper();

			String body = mapper.writeValueAsString(metadataOperation);

			System.out.println(body);

			httpPut.setEntity(new StringEntity(body));

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPut, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);

			System.out.println(entityData);

			MetadataOperationResultEntry[] actual = mapper.readValue(
					entityData, MetadataOperationResultEntry[].class);

			Assert.assertNotNull("no response body found", actual);

			Assert.assertEquals("did not get two response entries", 2,
					actual.length);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Test
	public void testBulkDeleteCollectionAVUSendJson() throws Exception {
		String testDirName = "testBulkDeleteCollectionAVUSendJson";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		String testAvuAttrib1 = "testBulkAddCollectionAVUJsonAttr1";
		String testAvuValue1 = "testBulkAddCollectionAVUJsonValue1";
		String testAvuUnit1 = "testBulkAddCollectionAVUJsonUnit1";

		String testAvuAttrib2 = "testBulkAddCollectionAVUJsonAttr2";
		String testAvuValue2 = "testBulkAddCollectionAVUJsonValue2";
		String testAvuUnit2 = "testBulkAddCollectionAVUJsonUnit2";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile collFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		collFile.mkdirs();

		CollectionAO collectionAO = accessObjectFactory
				.getCollectionAO(irodsAccount);
		List<AvuData> avuDatas = new ArrayList<AvuData>();
		avuDatas.add(AvuData.instance(testAvuAttrib1, testAvuValue1,
				testAvuUnit1));
		avuDatas.add(AvuData.instance(testAvuAttrib2, testAvuValue2,
				testAvuUnit2));
		collectionAO.addBulkAVUMetadataToCollection(targetIrodsCollection,
				avuDatas);

		MetadataOperation metadataOperation = new MetadataOperation();

		MetadataEntry metadataEntry = new MetadataEntry();
		metadataEntry.setAttribute(testAvuAttrib1);
		metadataEntry.setValue(testAvuValue1);
		metadataEntry.setUnit(testAvuUnit1);
		metadataOperation.getMetadataEntries().add(metadataEntry);

		metadataEntry = new MetadataEntry();
		metadataEntry.setAttribute(testAvuAttrib2);
		metadataEntry.setValue(testAvuValue2);
		metadataEntry.setUnit(testAvuUnit2);
		metadataOperation.getMetadataEntries().add(metadataEntry);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/collection");
		sb.append(collFile.getAbsolutePath());
		sb.append("/metadata");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);

		try {

			HttpPost httpPost = new HttpPost(sb.toString());
			httpPost.addHeader("accept", "application/json");
			httpPost.addHeader("Content-Type", "application/json");

			ObjectMapper mapper = new ObjectMapper();

			String body = mapper.writeValueAsString(metadataOperation);

			System.out.println(body);

			httpPost.setEntity(new StringEntity(body));

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPost, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);

			System.out.println(entityData);

			MetadataOperationResultEntry[] actual = mapper.readValue(
					entityData, MetadataOperationResultEntry[].class);

			Assert.assertNotNull("no response body found", actual);

			Assert.assertEquals("did not get two response entries", 2,
					actual.length);

			// see if metadata is deleted
			List<MetaDataAndDomainData> datas = collectionAO
					.findMetadataValuesForCollection(collFile.getAbsolutePath());
			Assert.assertTrue("did not seem to delete metadata",
					datas.isEmpty());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Test
	public void testBulkAddCollectionAVUSendXML() throws Exception {
		String testDirName = "testBulkAddCollectionAVUSendXML";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		String testAvuAttrib1 = "testBulkAddCollectionAVUSendXMLAttr1";
		String testAvuValue1 = "testBulkAddCollectionAVUSendXMLValue1";
		String testAvuUnit1 = "testBulkAddCollectionAVUSendXMLUnit1";

		String testAvuAttrib2 = "testBulkAddCollectionAVUSendXMLAttr2";
		String testAvuValue2 = "testBulkAddCollectionAVUSendXMLValue2";
		String testAvuUnit2 = "testBulkAddCollectionAVUSendXMLUnit2";

		MetadataOperation metadataOperation = new MetadataOperation();

		MetadataQueryResultEntry metadataEntry = new MetadataQueryResultEntry();
		metadataEntry.setAttribute(testAvuAttrib1);
		metadataEntry.setValue(testAvuValue1);
		metadataEntry.setUnit(testAvuUnit1);
		metadataOperation.getMetadataEntries().add(metadataEntry);

		metadataEntry = new MetadataQueryResultEntry();
		metadataEntry.setAttribute(testAvuAttrib2);
		metadataEntry.setValue(testAvuValue2);
		metadataEntry.setUnit(testAvuUnit2);
		metadataOperation.getMetadataEntries().add(metadataEntry);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile collFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		collFile.mkdirs();

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/collection");
		sb.append(collFile.getAbsolutePath());
		sb.append("/metadata");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);

		try {

			HttpPut httpPut = new HttpPut(sb.toString());
			httpPut.addHeader("accept", "application/xml");
			httpPut.addHeader("Content-Type", "application/xml");

			final JAXBContext context = JAXBContext
					.newInstance(MetadataOperation.class);

			final Marshaller marshaller = context.createMarshaller();

			final StringWriter stringWriter = new StringWriter();
			marshaller.marshal(metadataOperation, stringWriter);

			String body = stringWriter.toString();
			System.out.println(body);

			httpPut.setEntity(new StringEntity(body));

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPut, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			String entityData = EntityUtils.toString(entity);

			System.out.println(entityData);

			Assert.assertNotNull("no response body found", entityData);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Test
	public void testDeleteCollection() throws Exception {
		String testDirName = "testDeleteCollection";

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

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/collection");
		sb.append(collFile.getAbsolutePath());

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpDelete httpDelete = new HttpDelete(sb.toString());

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpDelete, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(204, response.getStatusLine().getStatusCode());
			EntityUtils.consume(entity);
			System.out.println("JSON>>>");

			collFile.reset();
			Assert.assertFalse("expected collection to be gone",
					collFile.exists());

			// check for idempotency
			httpDelete = new HttpDelete(sb.toString());

			response = clientAndContext.getHttpClient().execute(httpDelete,
					clientAndContext.getHttpContext());
			entity = response.getEntity();
			Assert.assertEquals(204, response.getStatusLine().getStatusCode());

			EntityUtils.consume(entity);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testGetCollectionAclJson() throws Exception {
		String testDirName = "testGetCollectionAclJson";

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

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/collection/");
		sb.append(collFile.getAbsolutePath());
		sb.append("/acl");

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
			PermissionListing actual = objectMapper.readValue(entityData,
					PermissionListing.class);

			Assert.assertNotNull("no permission listing returned", actual);
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Test
	public void testAddPermission() throws Exception {
		String testDirName = "testAddPermission";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile collFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		collFile.mkdirs();

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/collection");
		sb.append(collFile.getAbsolutePath());
		sb.append("/acl/");
		sb.append(secondaryAccount.getUserName());

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPut httpPut = new HttpPut(sb.toString());
			httpPut.addHeader("accept", "application/json");
			httpPut.getParams().setBooleanParameter("recursive", false);
			httpPut.getParams().setParameter("permission", "READ");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPut, clientAndContext.getHttpContext());
			Assert.assertEquals(204, response.getStatusLine().getStatusCode());

			CollectionAO collectionAO = accessObjectFactory
					.getCollectionAO(irodsAccount);
			UserFilePermission actualFilePermission = collectionAO
					.getPermissionForUserName(targetIrodsCollection,
							secondaryAccount.getUserName());
			Assert.assertEquals("file permission not set to read",
					FilePermissionEnum.READ,
					actualFilePermission.getFilePermissionEnum());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Test
	public void testAddPermissionWithZone() throws Exception {
		String testDirName = "testAddPermissionWithZone";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String secondaryUserNameString = secondaryAccount.getUserName() + ","
				+ secondaryAccount.getZone();

		IRODSFile collFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		collFile.mkdirs();

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/collection");
		sb.append(collFile.getAbsolutePath());
		sb.append("/acl/");
		sb.append(secondaryUserNameString);

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPut httpPut = new HttpPut(sb.toString());
			httpPut.addHeader("accept", "application/json");
			httpPut.getParams().setBooleanParameter("recursive", false);
			httpPut.getParams().setParameter("permission", "READ");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPut, clientAndContext.getHttpContext());
			Assert.assertEquals(204, response.getStatusLine().getStatusCode());

			CollectionAO collectionAO = accessObjectFactory
					.getCollectionAO(irodsAccount);
			UserFilePermission actualFilePermission = collectionAO
					.getPermissionForUserName(targetIrodsCollection,
							secondaryAccount.getUserName());
			Assert.assertEquals("file permission not set to read",
					FilePermissionEnum.READ,
					actualFilePermission.getFilePermissionEnum());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testAddPermissionBogusUser() throws Exception {
		String testDirName = "testAddPermissionWithZone";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String bogusUserString = "bogususerbogusbogusbogus";
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile collFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		collFile.mkdirs();

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/collection");
		sb.append(collFile.getAbsolutePath());
		sb.append("/acl/");
		sb.append(bogusUserString);

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPut httpPut = new HttpPut(sb.toString());
			httpPut.addHeader("accept", "application/json");
			httpPut.getParams().setBooleanParameter("recursive", false);
			httpPut.getParams().setParameter("permission", "READ");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPut, clientAndContext.getHttpContext());
			Assert.assertEquals(500, response.getStatusLine().getStatusCode());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

}
