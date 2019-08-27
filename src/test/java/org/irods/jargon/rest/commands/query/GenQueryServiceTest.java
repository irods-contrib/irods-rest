/**
 * 
 */
package org.irods.jargon.rest.commands.query;

import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.rest.auth.DefaultHttpClientAndContext;
import org.irods.jargon.rest.auth.RestAuthUtils;
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
import org.junit.Ignore;
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

import junit.framework.Assert;

/**
 * @author Justin James- RENCI (www.irods.org)
 * 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:jargon-beans.xml", "classpath:rest-servlet.xml" })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
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
	public void setApplicationContext(final ApplicationContext context) throws BeansException {
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

		targetIrodsFile1 = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties,
				IRODS_TEST_SUBDIR_PATH + '/' + testFileName1);
		targetIrodsFile2 = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties,
				IRODS_TEST_SUBDIR_PATH + '/' + testFileName2);
		targetIrodsFile3 = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties,
				IRODS_TEST_SUBDIR_PATH + '/' + testFileName3);
		targetIrodsFile4 = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties,
				IRODS_TEST_SUBDIR_PATH + '/' + testFileName4);

		absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName1 = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName1, 20);
		String localFileName2 = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName2, 30);
		String localFileName3 = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName3, 40);
		String localFileName4 = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName4, 50);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName1, targetIrodsFile1,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);
		dto.putOperation(localFileName2, targetIrodsFile2,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);
		dto.putOperation(localFileName3, targetIrodsFile3,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);
		dto.putOperation(localFileName4, targetIrodsFile4,
				testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null, null);

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

		int port = testingPropertiesHelper.getPropertyValueAsInt(testingProperties,
				RestTestingProperties.REST_PORT_PROPERTY);
		server = new TJWSEmbeddedJaxrsServer();
		server.setPort(port);
		ResteasyDeployment deployment = server.getDeployment();

		server.start();
		Dispatcher dispatcher = deployment.getDispatcher();
		SpringBeanProcessor processor = new SpringBeanProcessor(dispatcher, deployment.getRegistry(),
				deployment.getProviderFactory());
		((ConfigurableApplicationContext) applicationContext).addBeanFactoryPostProcessor(processor);

		SpringResourceFactory noDefaults = new SpringResourceFactory("genQueryService", applicationContext,
				GenQueryService.class);
		dispatcher.getRegistry().addResourceFactory(noDefaults);

	}

	@Ignore
	public void testGetGenQuerySendXmlReceiveXml() throws Exception {

		String targetCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);
		String targetResource = testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);

		String requestBodyXml = "<ns2:query xmlns:ns2=\"http://irods.org/irods-rest\">" + "<select>RESC_NAME</select>"
				+ "<select>COLL_NAME</select>" + "<select>DATA_NAME</select>" + "<select>DATA_SIZE</select>"
				+ "<condition>" + "<column>COLL_NAME</column>" + "<operator>EQUAL</operator>" + "<value>"
				+ targetCollection + "</value>" + "</condition>" + "<condition>" + "<column>DATA_NAME</column>"
				+ "<operator>LIKE</operator>" + "<value>%.dat</value>" + "</condition>" + "<order_by>"
				+ "<column>DATA_SIZE</column>" + "<order_condition>DESC</order_condition>" + "</order_by>"
				+ "</ns2:query>";

		String expectedResponseXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<ns2:results xmlns:ns2=\"http://irods.org/irods-rest\">" + "<row>" + "<column name=\"RESC_NAME\">"
				+ targetResource + "</column>" + "<column name=\"COLL_NAME\">" + targetCollection + "</column>"
				+ "<column name=\"DATA_NAME\">testfile3.dat</column>" + "<column name=\"DATA_SIZE\">40</column>"
				+ "</row>" + "<row>" + "<column name=\"RESC_NAME\">" + targetResource + "</column>"
				+ "<column name=\"COLL_NAME\">" + targetCollection + "</column>"
				+ "<column name=\"DATA_NAME\">testfile2.dat</column>" + "<column name=\"DATA_SIZE\">30</column>"
				+ "</row>" + "<row>" + "<column name=\"RESC_NAME\">" + targetResource + "</column>"
				+ "<column name=\"COLL_NAME\">" + targetCollection + "</column>"
				+ "<column name=\"DATA_NAME\">testfile1.dat</column>" + "<column name=\"DATA_SIZE\">20</column>"
				+ "</row>" + "</ns2:results>";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(testingProperties,
				RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/genQuery");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPost httppost = new HttpPost(sb.toString());
			httppost.addHeader("Content-Type", "application/xml");
			httppost.addHeader("Accept", "application/xml");

			HttpEntity requestEntity = new ByteArrayEntity(requestBodyXml.getBytes("UTF-8"));
			httppost.setEntity(requestEntity);

			HttpResponse response = clientAndContext.getHttpClient().execute(httppost,
					clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);

			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			System.out.println("XML>>>");
			System.out.println(entityData);

			Assert.assertTrue("Did not get expected xml stuff.  Sent: " + requestBodyXml + " Received: " + entityData
					+ "Expected: " + expectedResponseXml, entityData.indexOf(expectedResponseXml) > -1);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testGetGenQuerySendJsonReceiveJson() throws Exception {

		String targetCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);
		String targetResource = testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);

		String requestBodyJson = "{\"select\":" + "[{\"value\":\"RESC_NAME\"}," + "{\"value\":\"COLL_NAME\"},"
				+ "{\"value\":\"DATA_NAME\"}," + "{\"value\":\"DATA_SIZE\"}]," + "\"condition\":"
				+ "[{\"column\":\"COLL_NAME\",\"operator\":\"EQUAL\",\"value\":\"" + targetCollection + "\"},"
				+ "{\"column\":\"DATA_NAME\",\"operator\":\"LIKE\",\"value\":\"%.dat\"}]," + "\"order_by\":"
				+ "[{\"column\":\"DATA_SIZE\",\"order_condition\":\"DESC\"}]" + "}";

		String expectedResponseJson = "{\"row\":" + "[{\"column\":" + "[{\"name\":\"RESC_NAME\",\"value\":\""
				+ targetResource + "\"}," + "{\"name\":\"COLL_NAME\",\"value\":\"" + targetCollection + "\"},"
				+ "{\"name\":\"DATA_NAME\",\"value\":\"testfile3.dat\"},"
				+ "{\"name\":\"DATA_SIZE\",\"value\":\"40\"}]}," + "{\"column\":"
				+ "[{\"name\":\"RESC_NAME\",\"value\":\"" + targetResource + "\"},"
				+ "{\"name\":\"COLL_NAME\",\"value\":\"" + targetCollection + "\"},"
				+ "{\"name\":\"DATA_NAME\",\"value\":\"testfile2.dat\"},"
				+ "{\"name\":\"DATA_SIZE\",\"value\":\"30\"}]}," + "{\"column\":"
				+ "[{\"name\":\"RESC_NAME\",\"value\":\"" + targetResource + "\"},"
				+ "{\"name\":\"COLL_NAME\",\"value\":\"" + targetCollection + "\"},"
				+ "{\"name\":\"DATA_NAME\",\"value\":\"testfile1.dat\"},"
				+ "{\"name\":\"DATA_SIZE\",\"value\":\"20\"}]}]}";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(testingProperties,
				RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/genQuery");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPost httppost = new HttpPost(sb.toString());
			httppost.addHeader("Content-Type", "application/json");
			httppost.addHeader("Accept", "application/json");

			HttpEntity requestEntity = new ByteArrayEntity(requestBodyJson.getBytes("UTF-8"));
			httppost.setEntity(requestEntity);

			HttpResponse response = clientAndContext.getHttpClient().execute(httppost,
					clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			// Assert.assertTrue("Request : " + requestBodyJson, 1==0);
			Assert.assertTrue("Response was not 200.  Send: " + requestBodyJson,
					200 == response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);

			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			System.out.println("JSON>>>");
			System.out.println(entityData);

			Assert.assertTrue("Did not get expected xml stuff.  Sent: " + requestBodyJson + " Received: " + entityData
					+ "Expected: " + expectedResponseJson, entityData.indexOf(expectedResponseJson) > -1);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Ignore
	public void testGetGenQueryAggregateXml() throws Exception {

		String targetCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String requestBodyXml = "<ns2:query xmlns:ns2=\"http://irods.org/irods-rest\">"
				+ "<select aggregate_type=\"SUM\">DATA_SIZE</select>" + "<condition>" + "<column>COLL_NAME</column>"
				+ "<operator>EQUAL</operator>" + "<value>" + targetCollection + "</value>" + "</condition>"
				+ "<condition>" + "<column>DATA_NAME</column>" + "<operator>LIKE</operator>" + "<value>%.dat</value>"
				+ "</condition>" + "</ns2:query>";

		String expectedResponseXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<ns2:results xmlns:ns2=\"http://irods.org/irods-rest\">" + "<row>"
				+ "<column name=\"SUM(DATA_SIZE)\">90</column>" + "</row>" + "</ns2:results>";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(testingProperties,
				RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/genQuery");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPost httppost = new HttpPost(sb.toString());
			httppost.addHeader("Content-Type", "application/xml");
			httppost.addHeader("Accept", "application/xml");

			HttpEntity requestEntity = new ByteArrayEntity(requestBodyXml.getBytes("UTF-8"));
			httppost.setEntity(requestEntity);

			HttpResponse response = clientAndContext.getHttpClient().execute(httppost,
					clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);

			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			System.out.println("XML>>>");
			System.out.println(entityData);

			Assert.assertTrue("Did not get expected xml stuff.  Sent: " + requestBodyXml + " Received: " + entityData
					+ "Expected: " + expectedResponseXml, entityData.indexOf(expectedResponseXml) > -1);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testGetGenQueryAggregateJson() throws Exception {

		String targetCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String requestBodyJson = "{\"select\":" + "[{\"value\":\"DATA_SIZE\",\"aggregate_type\":\"SUM\"}],"
				+ "\"condition\":" + "[{\"column\":\"COLL_NAME\",\"operator\":\"EQUAL\",\"value\":\"" + targetCollection
				+ "\"}," + "{\"column\":\"DATA_NAME\",\"operator\":\"LIKE\",\"value\":\"%.dat\"}]" + "}";

		String expectedResponseJson = "{\"row\":[" + "{\"column\":[{\"name\":\"SUM(DATA_SIZE)\",\"value\":\"90\"}]}"
				+ "]}";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(testingProperties,
				RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/genQuery");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPost httppost = new HttpPost(sb.toString());
			httppost.addHeader("Content-Type", "application/json");
			httppost.addHeader("Accept", "application/json");

			HttpEntity requestEntity = new ByteArrayEntity(requestBodyJson.getBytes("UTF-8"));
			httppost.setEntity(requestEntity);

			HttpResponse response = clientAndContext.getHttpClient().execute(httppost,
					clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			// Assert.assertTrue("Request : " + requestBodyJson, 1==0);
			Assert.assertTrue("Response was not 200.  Send: " + requestBodyJson,
					200 == response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);

			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			System.out.println("JSON>>>");
			System.out.println(entityData);

			Assert.assertTrue("Did not get expected xml stuff.  Sent: " + requestBodyJson + " Received: " + entityData
					+ "Expected: " + expectedResponseJson, entityData.indexOf(expectedResponseJson) > -1);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testGetGenQueryLimitRowsJson() throws Exception {

		String targetCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);
		String targetResource = testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);

		String requestBodyJson = "{\"count\":\"2\"," + "\"select\":" + "[{\"value\":\"RESC_NAME\"},"
				+ "{\"value\":\"COLL_NAME\"}," + "{\"value\":\"DATA_NAME\"}," + "{\"value\":\"DATA_SIZE\"}],"
				+ "\"condition\":" + "[{\"column\":\"COLL_NAME\",\"operator\":\"EQUAL\",\"value\":\"" + targetCollection
				+ "\"}," + "{\"column\":\"DATA_NAME\",\"operator\":\"LIKE\",\"value\":\"%.dat\"}]," + "\"order_by\":"
				+ "[{\"column\":\"DATA_SIZE\",\"order_condition\":\"DESC\"}]" + "}";

		String expectedResponseJson = "{\"row\":" + "[{\"column\":" + "[{\"name\":\"RESC_NAME\",\"value\":\""
				+ targetResource + "\"}," + "{\"name\":\"COLL_NAME\",\"value\":\"" + targetCollection + "\"},"
				+ "{\"name\":\"DATA_NAME\",\"value\":\"testfile3.dat\"},"
				+ "{\"name\":\"DATA_SIZE\",\"value\":\"40\"}]}," + "{\"column\":"
				+ "[{\"name\":\"RESC_NAME\",\"value\":\"" + targetResource + "\"},"
				+ "{\"name\":\"COLL_NAME\",\"value\":\"" + targetCollection + "\"},"
				+ "{\"name\":\"DATA_NAME\",\"value\":\"testfile2.dat\"},"
				+ "{\"name\":\"DATA_SIZE\",\"value\":\"30\"}]}" + "]}";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(testingProperties,
				RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/genQuery");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPost httppost = new HttpPost(sb.toString());
			httppost.addHeader("Content-Type", "application/json");
			httppost.addHeader("Accept", "application/json");

			HttpEntity requestEntity = new ByteArrayEntity(requestBodyJson.getBytes("UTF-8"));
			httppost.setEntity(requestEntity);

			HttpResponse response = clientAndContext.getHttpClient().execute(httppost,
					clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			// Assert.assertTrue("Request : " + requestBodyJson, 1==0);
			Assert.assertTrue("Response was not 200.  Send: " + requestBodyJson,
					200 == response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);

			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			System.out.println("JSON>>>");
			System.out.println(entityData);

			Assert.assertTrue("Did not get expected xml stuff.  Sent: " + requestBodyJson + " Received: " + entityData
					+ "Expected: " + expectedResponseJson, entityData.indexOf(expectedResponseJson) > -1);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Ignore
	public void testGetGenQueryLimitRowsXml() throws Exception {

		String targetCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);
		String targetResource = testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);

		String requestBodyXml = "<ns2:query xmlns:ns2=\"http://irods.org/irods-rest\">" + "<count>2</count>"
				+ "<select>RESC_NAME</select>" + "<select>COLL_NAME</select>" + "<select>DATA_NAME</select>"
				+ "<select>DATA_SIZE</select>" + "<condition>" + "<column>COLL_NAME</column>"
				+ "<operator>EQUAL</operator>" + "<value>" + targetCollection + "</value>" + "</condition>"
				+ "<condition>" + "<column>DATA_NAME</column>" + "<operator>LIKE</operator>" + "<value>%.dat</value>"
				+ "</condition>" + "<order_by>" + "<column>DATA_SIZE</column>"
				+ "<order_condition>DESC</order_condition>" + "</order_by>" + "</ns2:query>";

		String expectedResponseXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<ns2:results xmlns:ns2=\"http://irods.org/irods-rest\">" + "<row>" + "<column name=\"RESC_NAME\">"
				+ targetResource + "</column>" + "<column name=\"COLL_NAME\">" + targetCollection + "</column>"
				+ "<column name=\"DATA_NAME\">testfile3.dat</column>" + "<column name=\"DATA_SIZE\">40</column>"
				+ "</row>" + "<row>" + "<column name=\"RESC_NAME\">" + targetResource + "</column>"
				+ "<column name=\"COLL_NAME\">" + targetCollection + "</column>"
				+ "<column name=\"DATA_NAME\">testfile2.dat</column>" + "<column name=\"DATA_SIZE\">30</column>"
				+ "</row>" + "</ns2:results>";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(testingProperties,
				RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/genQuery");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPost httppost = new HttpPost(sb.toString());
			httppost.addHeader("Content-Type", "application/xml");
			httppost.addHeader("Accept", "application/xml");

			HttpEntity requestEntity = new ByteArrayEntity(requestBodyXml.getBytes("UTF-8"));
			httppost.setEntity(requestEntity);

			HttpResponse response = clientAndContext.getHttpClient().execute(httppost,
					clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);

			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			System.out.println("XML>>>");
			System.out.println(entityData);

			Assert.assertTrue("Did not get expected xml stuff.  Sent: " + requestBodyXml + " Received: " + entityData
					+ "Expected: " + expectedResponseXml, entityData.indexOf(expectedResponseXml) > -1);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Ignore
	public void testGetGenQuerySelectZoneXml() throws Exception {

		String targetCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);
		String targetResource = testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);
		String targetZone = testingProperties.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY);

		String requestBodyXml = "<ns2:query xmlns:ns2=\"http://irods.org/irods-rest\">" + "<zone>" + targetZone
				+ "</zone>" + "<select>RESC_NAME</select>" + "<select>COLL_NAME</select>" + "<select>DATA_NAME</select>"
				+ "<select>DATA_SIZE</select>" + "<condition>" + "<column>COLL_NAME</column>"
				+ "<operator>EQUAL</operator>" + "<value>" + targetCollection + "</value>" + "</condition>"
				+ "<condition>" + "<column>DATA_NAME</column>" + "<operator>LIKE</operator>" + "<value>%.dat</value>"
				+ "</condition>" + "<order_by>" + "<column>DATA_SIZE</column>"
				+ "<order_condition>DESC</order_condition>" + "</order_by>" + "</ns2:query>";

		String expectedResponseXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<ns2:results xmlns:ns2=\"http://irods.org/irods-rest\">" + "<row>" + "<column name=\"RESC_NAME\">"
				+ targetResource + "</column>" + "<column name=\"COLL_NAME\">" + targetCollection + "</column>"
				+ "<column name=\"DATA_NAME\">testfile3.dat</column>" + "<column name=\"DATA_SIZE\">40</column>"
				+ "</row>" + "<row>" + "<column name=\"RESC_NAME\">" + targetResource + "</column>"
				+ "<column name=\"COLL_NAME\">" + targetCollection + "</column>"
				+ "<column name=\"DATA_NAME\">testfile2.dat</column>" + "<column name=\"DATA_SIZE\">30</column>"
				+ "</row>" + "<row>" + "<column name=\"RESC_NAME\">" + targetResource + "</column>"
				+ "<column name=\"COLL_NAME\">" + targetCollection + "</column>"
				+ "<column name=\"DATA_NAME\">testfile1.dat</column>" + "<column name=\"DATA_SIZE\">20</column>"
				+ "</row>" + "</ns2:results>";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(testingProperties,
				RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/genQuery");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPost httppost = new HttpPost(sb.toString());
			httppost.addHeader("Content-Type", "application/xml");
			httppost.addHeader("Accept", "application/xml");

			HttpEntity requestEntity = new ByteArrayEntity(requestBodyXml.getBytes("UTF-8"));
			httppost.setEntity(requestEntity);

			HttpResponse response = clientAndContext.getHttpClient().execute(httppost,
					clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);

			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			System.out.println("XML>>>");
			System.out.println(entityData);

			Assert.assertTrue("Did not get expected xml stuff.  Sent: " + requestBodyXml + " Received: " + entityData
					+ "Expected: " + expectedResponseXml, entityData.indexOf(expectedResponseXml) > -1);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testGetGenQuerySelectZoneJson() throws Exception {

		String targetCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);
		String targetResource = testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);
		String targetZone = testingProperties.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY);

		String requestBodyJson = "{\"zone\":\"" + targetZone + "\"," + "\"select\":" + "[{\"value\":\"RESC_NAME\"},"
				+ "{\"value\":\"COLL_NAME\"}," + "{\"value\":\"DATA_NAME\"}," + "{\"value\":\"DATA_SIZE\"}],"
				+ "\"condition\":" + "[{\"column\":\"COLL_NAME\",\"operator\":\"EQUAL\",\"value\":\"" + targetCollection
				+ "\"}," + "{\"column\":\"DATA_NAME\",\"operator\":\"LIKE\",\"value\":\"%.dat\"}]," + "\"order_by\":"
				+ "[{\"column\":\"DATA_SIZE\",\"order_condition\":\"DESC\"}]" + "}";

		String expectedResponseJson = "{\"row\":" + "[{\"column\":" + "[{\"name\":\"RESC_NAME\",\"value\":\""
				+ targetResource + "\"}," + "{\"name\":\"COLL_NAME\",\"value\":\"" + targetCollection + "\"},"
				+ "{\"name\":\"DATA_NAME\",\"value\":\"testfile3.dat\"},"
				+ "{\"name\":\"DATA_SIZE\",\"value\":\"40\"}]}," + "{\"column\":"
				+ "[{\"name\":\"RESC_NAME\",\"value\":\"" + targetResource + "\"},"
				+ "{\"name\":\"COLL_NAME\",\"value\":\"" + targetCollection + "\"},"
				+ "{\"name\":\"DATA_NAME\",\"value\":\"testfile2.dat\"},"
				+ "{\"name\":\"DATA_SIZE\",\"value\":\"30\"}]}," + "{\"column\":"
				+ "[{\"name\":\"RESC_NAME\",\"value\":\"" + targetResource + "\"},"
				+ "{\"name\":\"COLL_NAME\",\"value\":\"" + targetCollection + "\"},"
				+ "{\"name\":\"DATA_NAME\",\"value\":\"testfile1.dat\"},"
				+ "{\"name\":\"DATA_SIZE\",\"value\":\"20\"}]}]}";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(testingProperties,
				RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/genQuery");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPost httppost = new HttpPost(sb.toString());
			httppost.addHeader("Content-Type", "application/json");
			httppost.addHeader("Accept", "application/json");

			HttpEntity requestEntity = new ByteArrayEntity(requestBodyJson.getBytes("UTF-8"));
			httppost.setEntity(requestEntity);

			HttpResponse response = clientAndContext.getHttpClient().execute(httppost,
					clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			// Assert.assertTrue("Request : " + requestBodyJson, 1==0);
			Assert.assertTrue("Response was not 200.  Send: " + requestBodyJson,
					200 == response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);

			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			System.out.println("JSON>>>");
			System.out.println(entityData);

			Assert.assertTrue("Did not get expected xml stuff.  Sent: " + requestBodyJson + " Received: " + entityData
					+ "Expected: " + expectedResponseJson, entityData.indexOf(expectedResponseJson) > -1);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Ignore
	public void testGetGenQueryCaseInsensitiveXml() throws Exception {

		String targetCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String requestBodyXml = "<ns2:query xmlns:ns2=\"http://irods.org/irods-rest\">"
				+ "<select aggregate_type=\"sum\">data_size</select>" + "<condition>" + "<column>coll_name</column>"
				+ "<operator>equal</operator>" + "<value>" + targetCollection + "</value>" + "</condition>"
				+ "<condition>" + "<column>data_name</column>" + "<operator>like</operator>" + "<value>%.dat</value>"
				+ "</condition>" + "</ns2:query>";

		String expectedResponseXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<ns2:results xmlns:ns2=\"http://irods.org/irods-rest\">" + "<row>"
				+ "<column name=\"SUM(DATA_SIZE)\">90</column>" + "</row>" + "</ns2:results>";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(testingProperties,
				RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/genQuery");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPost httppost = new HttpPost(sb.toString());
			httppost.addHeader("Content-Type", "application/xml");
			httppost.addHeader("Accept", "application/xml");

			HttpEntity requestEntity = new ByteArrayEntity(requestBodyXml.getBytes("UTF-8"));
			httppost.setEntity(requestEntity);

			HttpResponse response = clientAndContext.getHttpClient().execute(httppost,
					clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);

			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			System.out.println("XML>>>");
			System.out.println(entityData);

			Assert.assertTrue("Did not get expected xml stuff.  Sent: " + requestBodyXml + " Received: " + entityData
					+ "Expected: " + expectedResponseXml, entityData.indexOf(expectedResponseXml) > -1);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testGetGenQueryCaseInsensitiveJson() throws Exception {

		String targetCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);

		String requestBodyJson = "{\"select\":" + "[{\"value\":\"data_size\",\"aggregate_type\":\"sum\"}],"
				+ "\"condition\":" + "[{\"column\":\"coll_name\",\"operator\":\"equal\",\"value\":\"" + targetCollection
				+ "\"}," + "{\"column\":\"data_name\",\"operator\":\"like\",\"value\":\"%.dat\"}]" + "}";

		String expectedResponseJson = "{\"row\":[" + "{\"column\":[{\"name\":\"SUM(DATA_SIZE)\",\"value\":\"90\"}]}"
				+ "]}";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(testingProperties,
				RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/genQuery");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPost httppost = new HttpPost(sb.toString());
			httppost.addHeader("Content-Type", "application/json");
			httppost.addHeader("Accept", "application/json");

			HttpEntity requestEntity = new ByteArrayEntity(requestBodyJson.getBytes("UTF-8"));
			httppost.setEntity(requestEntity);

			HttpResponse response = clientAndContext.getHttpClient().execute(httppost,
					clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			// Assert.assertTrue("Request : " + requestBodyJson, 1==0);
			Assert.assertTrue("Response was not 200.  Send: " + requestBodyJson,
					200 == response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);

			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			System.out.println("JSON>>>");
			System.out.println(entityData);

			Assert.assertTrue("Did not get expected xml stuff.  Sent: " + requestBodyJson + " Received: " + entityData
					+ "Expected: " + expectedResponseJson, entityData.indexOf(expectedResponseJson) > -1);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testGetGenQuerySelectOnlyJson() throws Exception {

		String requestBodyJson = "{\"select\":" + "[{\"value\":\"data_size\"}]}";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(testingProperties,
				RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/genQuery");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPost httppost = new HttpPost(sb.toString());
			httppost.addHeader("Content-Type", "application/json");
			httppost.addHeader("Accept", "application/json");

			HttpEntity requestEntity = new ByteArrayEntity(requestBodyJson.getBytes("UTF-8"));
			httppost.setEntity(requestEntity);

			HttpResponse response = clientAndContext.getHttpClient().execute(httppost,
					clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			// Assert.assertTrue("Request : " + requestBodyJson, 1==0);
			Assert.assertTrue("Response was not 200.  Send: " + requestBodyJson,
					200 == response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);

			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			System.out.println("JSON>>>");
			System.out.println(entityData);

			// okay if we didn't get an exception

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Ignore
	public void testGetGenQueryINOperatorSendXmlReceiveXml() throws Exception {

		String targetCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);
		String targetResource = testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);

		String requestBodyXml = "<ns2:query xmlns:ns2=\"http://irods.org/irods-rest\">" + "<select>COLL_NAME</select>"
				+ "<select>DATA_NAME</select>" + "<condition>" + "<column>COLL_NAME</column>"
				+ "<operator>EQUAL</operator>" + "<value>" + targetCollection + "</value>" + "</condition>"
				+ "<condition>" + "<column>DATA_NAME</column>" + "<operator>IN</operator>" + "<value_list>"
				+ "<value>testfile1.dat</value>" + "<value>testfile2.dat</value>" + "</value_list>" + "</condition>"
				+ "<order_by>" + "<column>DATA_NAME</column>" + "<order_condition>ASC</order_condition>" + "</order_by>"
				+ "</ns2:query>";

		String expectedResponseXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<ns2:results xmlns:ns2=\"http://irods.org/irods-rest\">" + "<row>" + "<column name=\"COLL_NAME\">"
				+ targetCollection + "</column>" + "<column name=\"DATA_NAME\">testfile1.dat</column>" + "</row>"
				+ "<row>" + "<column name=\"COLL_NAME\">" + targetCollection + "</column>"
				+ "<column name=\"DATA_NAME\">testfile2.dat</column>" + "</row>" + "</ns2:results>";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(testingProperties,
				RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/genQuery");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPost httppost = new HttpPost(sb.toString());
			httppost.addHeader("Content-Type", "application/xml");
			httppost.addHeader("Accept", "application/xml");

			HttpEntity requestEntity = new ByteArrayEntity(requestBodyXml.getBytes("UTF-8"));
			httppost.setEntity(requestEntity);

			HttpResponse response = clientAndContext.getHttpClient().execute(httppost,
					clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);

			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			System.out.println("XML>>>");
			System.out.println(entityData);

			Assert.assertTrue("Did not get expected xml stuff.  Sent: " + requestBodyXml + " Received: " + entityData
					+ "Expected: " + expectedResponseXml, entityData.indexOf(expectedResponseXml) > -1);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

	@Test
	public void testGetGenQueryINOperatorSendJsonReceiveJson() throws Exception {

		String targetCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties, IRODS_TEST_SUBDIR_PATH);
		String targetResource = testingProperties.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);

		String requestBodyJson = "{\"select\":" + "[{\"value\":\"COLL_NAME\"}," + "{\"value\":\"DATA_NAME\"}],"
				+ "\"condition\":" + "[{\"column\":\"COLL_NAME\",\"operator\":\"EQUAL\",\"value\":\"" + targetCollection
				+ "\"},"
				+ "{\"column\":\"DATA_NAME\",\"operator\":\"IN\",\"value_list\":{\"value\":[\"testfile1.dat\",\"testfile2.dat\"]}}]"// ,"
				// + "\"order_by\":"
				// + "[{\"column\":\"DATA_SIZE\",\"order_condition\":\"DESC\"}]"
				+ "}";

		System.out.println(requestBodyJson);

		String expectedResponseJson = "{\"row\":" + "[{\"column\":[{\"name\":\"COLL_NAME\"," + "\"value\":\""
				+ targetCollection + "\"}," + "{\"name\":\"DATA_NAME\",\"value\":\"testfile1.dat\"}]},"
				+ "{\"column\":[{\"name\":\"COLL_NAME\"," + "\"value\":\"" + targetCollection + "\"},{"
				+ "\"name\":\"DATA_NAME\",\"value\":\"testfile2.dat\"}]}]}";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(testingProperties,
				RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/genQuery");

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpPost httppost = new HttpPost(sb.toString());
			httppost.addHeader("Content-Type", "application/json");
			httppost.addHeader("Accept", "application/json");

			HttpEntity requestEntity = new ByteArrayEntity(requestBodyJson.getBytes("UTF-8"));
			httppost.setEntity(requestEntity);

			HttpResponse response = clientAndContext.getHttpClient().execute(httppost,
					clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			// Assert.assertTrue("Request : " + requestBodyJson, 1==0);
			Assert.assertTrue("Response was not 200.  Send: " + requestBodyJson,
					200 == response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);

			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			System.out.println("JSON>>>");
			System.out.println(entityData);

			Assert.assertTrue("Did not get expected xml stuff.  Sent: " + requestBodyJson + " Received: " + entityData
					+ "Expected: " + expectedResponseJson, entityData.indexOf(expectedResponseJson) > -1);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
		}
	}

}
