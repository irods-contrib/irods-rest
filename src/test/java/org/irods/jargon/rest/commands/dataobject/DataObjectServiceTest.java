package org.irods.jargon.rest.commands.dataobject;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.rest.auth.DefaultHttpClientAndContext;
import org.irods.jargon.rest.auth.RestAuthUtils;
import org.irods.jargon.rest.domain.DataObjectData;
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
		sb.append("/data/");
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
		sb.append("/data/");
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
	public void testUploadDataObjectData() throws Exception {
		// generate a local scratch file
		String testFileName = "testUploadDataObjectData.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						100 * 1024);
		File localFile = new File(localFileName);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/data/");
		sb.append(DataUtils.encodeIrodsAbsolutePath(targetIrodsFile,
				accessObjectFactory.getJargonProperties().getEncoding()));

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPost httpPost = new HttpPost(sb.toString());
			httpPost.addHeader("accept", "application/json");
			// httpPost.addHeader("Content-type", "multipart/form-data");
			FileBody fileEntity = new FileBody(localFile,
					"application/octet-stream");
			MultipartEntity reqEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart("uploadFile", fileEntity);
			httpPost.setEntity(reqEntity);
			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpPost, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);
			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			System.out.println("JSON>>>");
			System.out.println(entityData);
			ObjectMapper objectMapper = new ObjectMapper();
			DataObjectData result = objectMapper.readValue(entityData,
					DataObjectData.class);

			Assert.assertNotNull("null data object returned", result);

			IRODSFile actual = accessObjectFactory.getIRODSFileFactory(
					irodsAccount).instanceIRODSFile(targetIrodsFile);
			Assert.assertTrue("should have created the file on upload",
					actual.exists());

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

}
