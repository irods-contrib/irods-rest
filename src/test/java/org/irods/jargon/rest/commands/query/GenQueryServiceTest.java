/**
 * 
 */
package org.irods.jargon.rest.commands.query;

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
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.rest.auth.DefaultHttpClientAndContext;
import org.irods.jargon.rest.auth.RestAuthUtils;
import org.irods.jargon.rest.commands.collection.CollectionService;
import org.irods.jargon.rest.domain.CollectionData;
import org.irods.jargon.rest.domain.MetadataEntry;
import org.irods.jargon.rest.domain.MetadataListing;
import org.irods.jargon.rest.domain.MetadataOperation;
import org.irods.jargon.rest.domain.MetadataOperationResultEntry;
import org.irods.jargon.rest.domain.MetadataQueryResultEntry;
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

/**
 * @author Justin James- RENCI (www.irods.org)
 * 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:jargon-beans.xml",
		"classpath:rest-servlet.xml" })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class })
public class GenQueryServiceTest implements ApplicationContextAware {

	private static TJWSEmbeddedJaxrsServer server;

	private static ApplicationContext applicationContext;

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	private static ScratchFileUtils scratchFileUtils = null;
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	
	private static String targetIrodsFile1 = null;
	private static String targetIrodsFile2 = null;
	private static String targetIrodsFile3 = null;
	private static String targetIrodsFile4 = null;
	private static String absPath = null;
	
	public static final String IRODS_TEST_SUBDIR_PATH = "GenQueryTestDirectory";

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
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
		
		// create some test data objects to query on
		String testFileName1 = "testfile1.dat";
		String testFileName2 = "testfile2.dat";
		String testFileName3 = "testfile3.dat";
		String testFileName4 = "testfile4.txt";
		
		targetIrodsFile1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName1);
		targetIrodsFile2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName2);
		targetIrodsFile3 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName3);
		targetIrodsFile4 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName4);
		
		absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName1 = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName1, 20);
		String localFileName2 = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName2, 30);
		String localFileName3 = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName3, 40);
		String localFileName4 = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName4, 50);
		
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName1, targetIrodsFile1, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);
		dto.putOperation(localFileName2, targetIrodsFile2, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);
		dto.putOperation(localFileName3, targetIrodsFile3, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);
		dto.putOperation(localFileName4, targetIrodsFile4, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);

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
				"genQueryService", applicationContext,
				GenQueryService.class);
		dispatcher.getRegistry().addResourceFactory(noDefaults);
		
	}

	@Test
	public void testGetGenQuerySendXmlReceiveXml() throws Exception {
	
                String targetCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);	
                String targetResource = testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY); 
		
		String requestBodyXml = "<ns2:query xmlns:ns2=\"http://irods.org/irods-rest\">" 
				+ "<select>RESC_NAME</select>"
				+ "<select>COLL_NAME</select>"
				+ "<select>DATA_NAME</select>"
				+ "<select>DATA_SIZE</select>"
				+ "<condition>"
				+ "<column>COLL_NAME</column>"
				+ "<operator>EQUAL</operator>"
				+ "<value>" + targetCollection + "</value>"
				+ "</condition>"
				+ "<condition>"
				+ "<column>DATA_NAME</column>"
				+ "<operator>LIKE</operator>"
				+ "<value>%.dat</value>"
				+ "</condition>"
				+ "<order_by>"
				+ "<column>DATA_SIZE</column>"
				+ "<order_condition>DESC</order_condition>"
				+ "</order_by>"
				+ "</ns2:query>";
		
		String expectedResponseXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<ns2:results xmlns:ns2=\"http://irods.org/irods-rest\">"
				+ "<row>"
				+ "<column name=\"RESC_NAME\">" + targetResource + "</column>"
				+ "<column name=\"COLL_NAME\">" + targetCollection + "</column>"
				+ "<column name=\"DATA_NAME\">testfile3.dat</column>"
				+ "<column name=\"DATA_SIZE\">40</column>"
				+ "</row>"
				+ "<row>"
                                + "<column name=\"RESC_NAME\">" + targetResource + "</column>"
                                + "<column name=\"COLL_NAME\">" + targetCollection + "</column>"
                                + "<column name=\"DATA_NAME\">testfile2.dat</column>"
                                + "<column name=\"DATA_SIZE\">30</column>"
				+ "</row>"
				+ "<row>"
                               + "<column name=\"RESC_NAME\">" + targetResource + "</column>"
                                + "<column name=\"COLL_NAME\">" + targetCollection + "</column>"
                                + "<column name=\"DATA_NAME\">testfile1.dat</column>"
                                + "<column name=\"DATA_SIZE\">20</column>"
				+ "</row>"
				+ "</ns2:results>";



		
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/genQuery");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPost httppost = new HttpPost(sb.toString());
			httppost.addHeader("Content-Type", "application/xml");
			httppost.addHeader("Accept", "application/xml");
			
			HttpEntity requestEntity = new ByteArrayEntity(requestBodyXml.getBytes("UTF-8"));
			httppost.setEntity(requestEntity);

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httppost, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);
			
			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			System.out.println("XML>>>");
			System.out.println(entityData);
			
			Assert.assertTrue(
					"Did not get expected xml stuff.  Sent: " + requestBodyXml + " Received: " + entityData 
                                         + "Expected: " + expectedResponseXml,
					entityData.indexOf(expectedResponseXml) > -1);


		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

}
