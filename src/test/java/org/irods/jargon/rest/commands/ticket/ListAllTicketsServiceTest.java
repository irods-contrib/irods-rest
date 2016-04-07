package org.irods.jargon.rest.commands.ticket;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.rest.auth.DefaultHttpClientAndContext;
import org.irods.jargon.rest.auth.RestAuthUtils;
import org.irods.jargon.rest.utils.RestTestingProperties;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.ticket.Ticket;
import org.irods.jargon.ticket.TicketAdminService;
import org.irods.jargon.ticket.TicketServiceFactoryImpl;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;
import org.irods.jargon.ticket.utils.TicketRandomString;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ListAllTicketsServiceTest  implements ApplicationContextAware {

	private static TJWSEmbeddedJaxrsServer server;

	private static ApplicationContext applicationContext;

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	private static ScratchFileUtils scratchFileUtils = null;
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static String absPath = null;
	
	private static String targetIrodsFile = null;
	
	public static final String IRODS_TEST_SUBDIR_PATH = "TicketTestDirectory";
	public static final SimpleDateFormat EXPIRATION_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static TicketServiceFactoryImpl ticketServiceFactory;
	private static TicketAdminService ticketService;

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
		
		// create a test data object
		String testFileName = "testfile.dat";
		
		targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		
		absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 20);
		
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsFile, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY), null,
				null);
		
		
		ticketServiceFactory = new TicketServiceFactoryImpl(accessObjectFactory);
		ticketService = ticketServiceFactory.instanceTicketAdminService(irodsAccount);

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
				"listAllTicketsService", applicationContext,
				ListAllTicketsService.class);
		dispatcher.getRegistry().addResourceFactory(noDefaults);
		
	}

	@Test
	public void listAllTicketsXml() throws Exception {
	
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile file = accessObjectFactory.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsFile);
	
		// create two tickets
		String ticketString1 = new TicketRandomString(15).nextString();
		ticketService.createTicket(TicketCreateModeEnum.READ, file, ticketString1);
		String ticketId1 = ticketService.getTicketForSpecifiedTicketString(ticketString1).getTicketId();
		ticketService.addTicketHostRestriction(ticketString1, "localhost");
		ticketService.addTicketUserRestriction(ticketString1, testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_KEY));
		ticketService.addTicketGroupRestriction(ticketString1, testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY));
		ticketService.setTicketByteWriteLimit(ticketString1, 1000);
		ticketService.setTicketFileWriteLimit(ticketString1, 10);
		ticketService.setTicketUsesLimit(ticketString1, 20);
		Date expirationTime1 = new Date(new Date().getTime() + 365 * 24 * 60 * 60 * 1000L);
		ticketService.setTicketExpiration(ticketString1, expirationTime1);
		
		String ticketString2 = new TicketRandomString(15).nextString();
		ticketService.createTicket(TicketCreateModeEnum.WRITE, file, ticketString2);
		String ticketId2 = ticketService.getTicketForSpecifiedTicketString(ticketString2).getTicketId();
		ticketService.addTicketHostRestriction(ticketString2, "localhost");
		ticketService.addTicketUserRestriction(ticketString2, testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_KEY));
		ticketService.addTicketGroupRestriction(ticketString2, testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY));
		ticketService.setTicketByteWriteLimit(ticketString2, 1001);
		ticketService.setTicketFileWriteLimit(ticketString2, 11);
		ticketService.setTicketUsesLimit(ticketString2, 21);
		Date expirationTime2 = new Date(new Date().getTime() + 2 * 365 * 24 * 60 * 60 * 1000L);
		ticketService.setTicketExpiration(ticketString2, expirationTime2);
		
		String expectedResponse1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<ns2:tickets xmlns:ns2=\"http://irods.org/irods-rest\">";
		
		String expectedResponse2 = "<ticket>"
				+ "<ticket_id>" + ticketId1 + "</ticket_id>"
				+ "<ticket_string>" + ticketString1 + "</ticket_string>"
				+ "<ticket_type>read</ticket_type>"
				+ "<object_type>data_object</object_type>"
				+ "<owner_name>" + testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_KEY) + "</owner_name>"
				+ "<owner_zone>" + testingProperties.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY) + "</owner_zone>"
				+ "<uses_count>0</uses_count>"
				+ "<uses_limit>20</uses_limit>"
				+ "<write_file_count>0</write_file_count>"
				+ "<write_file_limit>10</write_file_limit>"
				+ "<write_byte_count>0</write_byte_count>"
				+ "<write_byte_limit>1000</write_byte_limit>"
				+ "<expire_time>" + EXPIRATION_DATE_FORMAT.format(expirationTime1) + "</expire_time>"
				+ "<irods_path>" + targetIrodsFile + "</irods_path>"
				+ "<host_restrictions>";
		
		String expectedResponse3 = "</host_restrictions>"
				+ "<user_restrictions>" + testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_KEY) + "</user_restrictions>"
				+ "<group_restrictions>" + testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY) + "</group_restrictions>"
				+ "</ticket>";
				
		String expectedResponse4 = "<ticket>"
				+ "<ticket_id>" + ticketId2 + "</ticket_id>"
				+ "<ticket_string>" + ticketString2 + "</ticket_string>"
				+ "<ticket_type>write</ticket_type>"
				+ "<object_type>data_object</object_type>"
				+ "<owner_name>" + testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_KEY) + "</owner_name>"
				+ "<owner_zone>" + testingProperties.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY) + "</owner_zone>"
				+ "<uses_count>0</uses_count>"
				+ "<uses_limit>21</uses_limit>"
				+ "<write_file_count>0</write_file_count>"
				+ "<write_file_limit>11</write_file_limit>"
				+ "<write_byte_count>0</write_byte_count>"
				+ "<write_byte_limit>1001</write_byte_limit>"
				+ "<expire_time>" + EXPIRATION_DATE_FORMAT.format(expirationTime2) + "</expire_time>"
				+ "<irods_path>" + targetIrodsFile + "</irods_path>"
				+ "<host_restrictions>";

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/listAllTickets");
		
		System.out.println("REQUEST = " + sb.toString());

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpGet httpget = new HttpGet(sb.toString());
			httpget.addHeader("Accept", "application/xml");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpget, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);
			
			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			System.out.println("XML>>>");
			//System.out.println(entityData);
			
			// The host can be translated to IP so just look for the part before and after host restrictions.
			int index1 = entityData.indexOf(expectedResponse1);
			int index2 = entityData.indexOf(expectedResponse2);
			int index3 = entityData.indexOf(expectedResponse3);
			int index4 = entityData.indexOf(expectedResponse4);
			
			Assert.assertTrue(
					"Did not get expected xml stuff.  Received: " + entityData 
                                         + "Expected: " + expectedResponse1,
					index1 > -1);
			Assert.assertTrue(
					"Did not get expected xml stuff.  Received: " + entityData 
                                         + "Expected: " + expectedResponse2,
					index2 > -1);
			Assert.assertTrue(
					"Did not get expected xml stuff.  Received: " + entityData 
                                         + "Expected: " + expectedResponse3,
					index3 > -1);
			Assert.assertTrue(
					"Did not get expected xml stuff.  Received: " + entityData 
                                         + "Expected: " + expectedResponse4,
					index4 > -1);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
			
			// clean up - delete the ticket
			ticketService.deleteTicket(ticketString1);
			ticketService.deleteTicket(ticketString2);
		}
	}
	
	@Test
	public void listAllTicketsJson() throws Exception {
	
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile file = accessObjectFactory.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsFile);
	
		// create two tickets
		String ticketString1 = new TicketRandomString(15).nextString();
		ticketService.createTicket(TicketCreateModeEnum.READ, file, ticketString1);
		String ticketId1 = ticketService.getTicketForSpecifiedTicketString(ticketString1).getTicketId();
		ticketService.addTicketHostRestriction(ticketString1, "localhost");
		ticketService.addTicketUserRestriction(ticketString1, testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_KEY));
		ticketService.addTicketGroupRestriction(ticketString1, testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY));
		ticketService.setTicketByteWriteLimit(ticketString1, 1000);
		ticketService.setTicketFileWriteLimit(ticketString1, 10);
		ticketService.setTicketUsesLimit(ticketString1, 20);
		Date expirationTime1 = new Date(new Date().getTime() + 365 * 24 * 60 * 60 * 1000L);
		ticketService.setTicketExpiration(ticketString1, expirationTime1);
		
		String ticketString2 = new TicketRandomString(15).nextString();
		ticketService.createTicket(TicketCreateModeEnum.WRITE, file, ticketString2);
		String ticketId2 = ticketService.getTicketForSpecifiedTicketString(ticketString2).getTicketId();
		ticketService.addTicketHostRestriction(ticketString2, "localhost");
		ticketService.addTicketUserRestriction(ticketString2, testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_KEY));
		ticketService.addTicketGroupRestriction(ticketString2, testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY));
		ticketService.setTicketByteWriteLimit(ticketString2, 1001);
		ticketService.setTicketFileWriteLimit(ticketString2, 11);
		ticketService.setTicketUsesLimit(ticketString2, 21);
		Date expirationTime2 = new Date(new Date().getTime() + 2 * 365 * 24 * 60 * 60 * 1000L);
		ticketService.setTicketExpiration(ticketString2, expirationTime2);
		
		String expectedResponse1 = "{\"ticket\":[";
		String expectedResponse2 = "{"
				+ "\"ticket_id\":\"" + ticketId1 + "\","
				+ "\"ticket_string\":\"" + ticketString1 + "\","
				+ "\"ticket_type\":\"read\","
				+ "\"object_type\":\"data_object\","
				+ "\"owner_name\":\"" + testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_KEY) + "\","
				+ "\"owner_zone\":\"" + testingProperties.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY) + "\","
				+ "\"uses_count\":0,"
				+ "\"uses_limit\":20,"
				+ "\"write_file_count\":0,"
				+ "\"write_file_limit\":10,"
				+ "\"write_byte_count\":0,"
				+ "\"write_byte_limit\":1000,"
				+ "\"expire_time\":\"" + EXPIRATION_DATE_FORMAT.format(expirationTime1) + "\","
				+ "\"irods_path\":\"" + targetIrodsFile + "\","
				+ "\"host_restrictions\":[\"";
		String expectedResponse3 = "\"],"
				+ "\"user_restrictions\":[\"" + testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_KEY) + "\"],"
				+ "\"group_restrictions\":[\"" + testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_GROUP_KEY) + "\"]"
				+ "}";
		String expectedResponse4 = "{"
				+ "\"ticket_id\":\"" + ticketId2 + "\","
				+ "\"ticket_string\":\"" + ticketString2 + "\","
				+ "\"ticket_type\":\"write\","
				+ "\"object_type\":\"data_object\","
				+ "\"owner_name\":\"" + testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_KEY) + "\","
				+ "\"owner_zone\":\"" + testingProperties.getProperty(TestingPropertiesHelper.IRODS_ZONE_KEY) + "\","
				+ "\"uses_count\":0,"
				+ "\"uses_limit\":21,"
				+ "\"write_file_count\":0,"
				+ "\"write_file_limit\":11,"
				+ "\"write_byte_count\":0,"
				+ "\"write_byte_limit\":1001,"
				+ "\"expire_time\":\"" + EXPIRATION_DATE_FORMAT.format(expirationTime2) + "\","
				+ "\"irods_path\":\"" + targetIrodsFile + "\","
				+ "\"host_restrictions\":[\"";

		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.REST_PORT_PROPERTY));
		sb.append("/listAllTickets");
		
		System.out.println("REQUEST = " + sb.toString());

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpGet httpget = new HttpGet(sb.toString());
			httpget.addHeader("Accept", "application/json");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpget, clientAndContext.getHttpContext());
			HttpEntity entity = response.getEntity();
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			Assert.assertNotNull(entity);
			
			String entityData = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			System.out.println("XML>>>");
			//System.out.println(entityData);
			
			// The host can be translated to IP so just look for the part before and after host restrictions.
			int index1 = entityData.indexOf(expectedResponse1);
			int index2 = entityData.indexOf(expectedResponse2);
			int index3 = entityData.indexOf(expectedResponse3);
			int index4 = entityData.indexOf(expectedResponse4);
			
			Assert.assertTrue(
					"Did not get expected xml stuff.  Received: " + entityData 
                                         + "Expected: " + expectedResponse1,
					index1 > -1);
			Assert.assertTrue(
					"Did not get expected xml stuff.  Received: " + entityData 
                                         + "Expected: " + expectedResponse2,
					index2 > -1);
			Assert.assertTrue(
					"Did not get expected xml stuff.  Received: " + entityData 
                                         + "Expected: " + expectedResponse3,
					index3 > -1);
			Assert.assertTrue(
					"Did not get expected xml stuff.  Received: " + entityData 
                                         + "Expected: " + expectedResponse4,
					index4 > -1);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
			
			// clean up - delete the ticket
			ticketService.deleteTicket(ticketString1);
			ticketService.deleteTicket(ticketString2);
		}
	}
	

}
