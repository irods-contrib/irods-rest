package org.irods.jargon.rest.commands.dataobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.rest.auth.DefaultHttpClientAndContext;
import org.irods.jargon.rest.auth.RestAuthUtils;
import org.irods.jargon.rest.domain.DataObjectData;
import org.irods.jargon.rest.domain.MetadataEntry;
import org.irods.jargon.rest.domain.MetadataListing;
import org.irods.jargon.rest.domain.MetadataOperation;
import org.irods.jargon.rest.domain.MetadataOperationResultEntry;
import org.irods.jargon.rest.domain.PermissionListing;
import org.irods.jargon.rest.utils.DataUtils;
import org.irods.jargon.rest.utils.RestTestingProperties;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:jargon-beans.xml",
		"classpath:rest-servlet.xml" })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class })
public class DataObjectServiceTest implements ApplicationContextAware {

	private static TJWSEmbeddedJaxrsServer server;

	private static ApplicationContext applicationContext;

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	public static final String IRODS_TEST_SUBDIR_PATH = "RestDataObjectServiceTest";
	private static ScratchFileUtils scratchFileUtils = null;
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	@Override
	public void setApplicationContext(final ApplicationContext context)
			throws BeansException {
		applicationContext = context;
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
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
				"dataObjectService", applicationContext,
				DataObjectService.class);
		dispatcher.getRegistry().addResourceFactory(noDefaults);

	}

	@Test
	public void testGetDataObjectData() throws Exception {
		// generate a local scratch file
		String testFileName = "testFindByAbsolutePath.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsFile, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);

		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/dataObject/");
		sb.append(DataUtils.encodeIrodsAbsolutePath(targetIrodsFile,
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
			DataObjectData actual = objectMapper.readValue(entityData,
					DataObjectData.class);

			Assert.assertNotNull("null data object returned", actual);

			DataObject dataObject = dataObjectAO
					.findByAbsolutePath(targetIrodsFile);

			Assert.assertEquals(actual.getChecksum(), dataObject.getChecksum());
			Assert.assertEquals(actual.getCollectionId(),
					dataObject.getCollectionId());
			Assert.assertEquals(actual.getCollectionName(),
					dataObject.getCollectionName());
			Assert.assertEquals(actual.getComments(), dataObject.getComments());
			Assert.assertEquals(actual.getDataMapId(),
					dataObject.getDataMapId());
			Assert.assertEquals(actual.getDataName(), dataObject.getDataName());
			Assert.assertEquals(actual.getDataOwnerName(),
					dataObject.getDataOwnerName());
			Assert.assertEquals(actual.getDataOwnerZone(),
					dataObject.getDataOwnerZone());
			Assert.assertEquals(actual.getDataPath(), dataObject.getDataPath());
			Assert.assertEquals(actual.getDataReplicationNumber(),
					dataObject.getDataReplicationNumber());
			Assert.assertEquals(actual.getDataSize(), dataObject.getDataSize());
			Assert.assertEquals(actual.getDataStatus(),
					dataObject.getDataStatus());
			Assert.assertEquals(actual.getDataTypeName(),
					dataObject.getDataTypeName());
			Assert.assertEquals(actual.getDataVersion(),
					dataObject.getDataMapId());
			Assert.assertEquals(actual.getExpiry(), dataObject.getExpiry());
			Assert.assertEquals(actual.getId(), dataObject.getId());
			Assert.assertEquals(actual.getObjectPath(),
					dataObject.getObjectPath());
			Assert.assertEquals(actual.getReplicationStatus(),
					dataObject.getReplicationStatus());
			Assert.assertEquals(actual.getResourceGroupName(),
					dataObject.getResourceGroupName());
			Assert.assertEquals(actual.getResourceName(),
					dataObject.getResourceName());
			Assert.assertEquals(actual.getCreatedAt(),
					dataObject.getCreatedAt());
			Assert.assertEquals(actual.getSpecColType(),
					dataObject.getSpecColType());
			Assert.assertEquals(actual.getUpdatedAt(),
					dataObject.getUpdatedAt());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testGetDataObjectDataXML() throws Exception {
		// generate a local scratch file
		String testFileName = "testGetDataObjectDataXML.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsFile, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);

		irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(
				irodsAccount);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/dataObject/");
		sb.append(DataUtils.encodeIrodsAbsolutePath(targetIrodsFile,
				accessObjectFactory.getJargonProperties().getEncoding()));

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
	public void testDeleteDataObjectData() throws Exception {
		// generate a local scratch file
		String testFileName = "testDeleteDataObjectData.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsFile, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/dataObject/");
		sb.append(DataUtils.encodeIrodsAbsolutePath(targetIrodsFile,
				accessObjectFactory.getJargonProperties().getEncoding()));
		sb.append("?force=true");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpDelete httpDelete = new HttpDelete(sb.toString());
			httpDelete.addHeader("accept", "application/json");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpDelete, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(204, response.getStatusLine().getStatusCode());
			EntityUtils.consume(entity);

			IRODSFile actual = accessObjectFactory.getIRODSFileFactory(
					irodsAccount).instanceIRODSFile(targetIrodsFile);
			Assert.assertFalse("should have deleted", actual.exists());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testGetDataObjectPermissions() throws Exception {
		// generate a local scratch file
		String testFileName = "testGetDataObjectPermissions.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsFile, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/dataObject/");
		sb.append(DataUtils.encodeIrodsAbsolutePath(targetIrodsFile,
				accessObjectFactory.getJargonProperties().getEncoding()));
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
	public void testGetDataObjectPermissionsXML() throws Exception {
		// generate a local scratch file
		String testFileName = "testGetDataObjectPermissionsXML.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsFile, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/dataObject/");
		sb.append(DataUtils.encodeIrodsAbsolutePath(targetIrodsFile,
				accessObjectFactory.getJargonProperties().getEncoding()));
		sb.append("/acl");

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
	public void testGetMetadataListingJson() throws Exception {
		String testLocalName = "testGetMetadataListingJson";
		String testFileName = "testGetMetadataListingJson & with special chars 7387298*(*#*(* ? zipity.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testLocalName, 1);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection + "/"
				+ testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);

		// initialize the AVU data
		String expectedAttribName = "testmdattrib1";
		String expectedAttribValue = "testmdvalue1";
		String expectedAttribUnits = "test1mdunits";

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedAttribValue, expectedAttribUnits);
		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/dataObject/");
		sb.append(DataUtils.encodeIrodsAbsolutePath(dataObjectAbsPath,
				accessObjectFactory.getJargonProperties().getEncoding()));
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
	public void testBulkAddDataObjectAVUSendJson() throws Exception {
		String testLocalName = "testGetMetadataListingJson";
		String testFileName = "testBulkAddDataObjectAVUSendJson.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testLocalName, 1);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection + "/"
				+ testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);

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

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/dataObject");
		sb.append(dataObjectAbsPath);
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

			DataObjectAO dataObjectAO = accessObjectFactory
					.getDataObjectAO(irodsAccount);
			List<MetaDataAndDomainData> actualList = dataObjectAO
					.findMetadataValuesForDataObject(dataObjectAbsPath);
			Assert.assertFalse(actualList.isEmpty());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

	@Test
	public void testBulkDeleteDataObjectAVUSendJson() throws Exception {
		String testFileName = "testBulkDeleteDataObjectAVUSendJson.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		TransferControlBlock tcb = accessObjectFactory
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);
		dto.putOperation(localFileName, targetIrodsFile, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				tcb);

		String testAvuAttrib1 = "testBulkDeleteDataObjectAVUSendJsonAttr1";
		String testAvuValue1 = "testBulkDeleteDataObjectAVUSendJsonValue1";
		String testAvuUnit1 = "testBulkDeleteDataObjectAVUSendJsonUnit1";

		String testAvuAttrib2 = "testBulkDeleteDataObjectAVUSendJsonAttr2";
		String testAvuValue2 = "testBulkDeleteDataObjectAVUSendJsonValue2";
		String testAvuUnit2 = "testBulkDeleteDataObjectAVUSendJsonUnit2";

		DataObjectAO dataObjectAO = accessObjectFactory
				.getDataObjectAO(irodsAccount);
		List<AvuData> avuDatas = new ArrayList<AvuData>();
		avuDatas.add(AvuData.instance(testAvuAttrib1, testAvuValue1,
				testAvuUnit1));
		avuDatas.add(AvuData.instance(testAvuAttrib2, testAvuValue2,
				testAvuUnit2));
		dataObjectAO.addBulkAVUMetadataToDataObject(targetIrodsFile, avuDatas);

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
		sb.append("/dataObject");
		sb.append(targetIrodsFile);
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
			List<MetaDataAndDomainData> datas = dataObjectAO
					.findMetadataValuesForDataObject(targetIrodsFile);
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
	public void testAddPermission() throws Exception {
		String testFileName = "testAddPermission.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsFile, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/dataObject");
		sb.append(targetIrodsFile);
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

			DataObjectAO dataObjectAO = accessObjectFactory
					.getDataObjectAO(irodsAccount);
			UserFilePermission actualFilePermission = dataObjectAO
					.getPermissionForDataObjectForUserName(targetIrodsFile,
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
	public void testDeletePermission() throws Exception {
		String testFileName = "testDeletePermission.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 1);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccount secondaryAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsFile, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		dataObjectAO
				.setAccessPermissionWrite(
						"",
						targetIrodsFile,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/dataObject/");
		sb.append(DataUtils.encodeIrodsAbsolutePath(targetIrodsFile,
				accessObjectFactory.getJargonProperties().getEncoding()));

		sb.append("/acl/");
		sb.append(secondaryAccount.getUserName() + ","
				+ secondaryAccount.getZone());

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpDelete httpDelete = new HttpDelete(sb.toString());
			httpDelete.addHeader("accept", "application/json");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpDelete, clientAndContext.getHttpContext());
			Assert.assertEquals(204, response.getStatusLine().getStatusCode());

			UserFilePermission actualFilePermission = dataObjectAO
					.getPermissionForDataObjectForUserName(targetIrodsFile,
							secondaryAccount.getUserName());
			Assert.assertTrue("permission not deleted",
					actualFilePermission == null);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}

	}

}
