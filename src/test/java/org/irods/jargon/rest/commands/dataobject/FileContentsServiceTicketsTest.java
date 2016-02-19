/**
 * 
 */
package org.irods.jargon.rest.commands.dataobject;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.rest.auth.BasicAuthFilter;
import org.irods.jargon.rest.auth.DefaultHttpClientAndContext;
import org.irods.jargon.rest.auth.RestAuthUtils;
import org.irods.jargon.rest.commands.ticket.TicketService;
import org.irods.jargon.rest.domain.DataObjectData;
import org.irods.jargon.rest.utils.DataUtils;
import org.irods.jargon.rest.utils.RestTestingProperties;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.ticket.TicketAdminService;
import org.irods.jargon.ticket.TicketServiceFactory;
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
 * @author Justin James - Renci (www.irods.org)
 * 
 *         The tests in this package require the use of an external REST
 *         deployment running on the local host.  The test.external.rest.port
 *         should point to this external REST deployment port.
 *         
 *         The reason this does not use the imbedded TJWS used elsewhere is 
 *         due to the fact that TJWS does not execute filters when run in 
 *         embedded mode.  Filters are required for ticket use.
 * 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:jargon-beans.xml",
		"classpath:rest-servlet.xml" })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class })
public class FileContentsServiceTicketsTest implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	public static final String IRODS_TEST_SUBDIR_PATH = "RestFileContentsServiceTest";
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

	@Before
	public void setUp() throws Exception {

	}

	
	@Test
	public void testUploadDataObjectDataWithTicket() throws Exception {
		// generate a local scratch file
		String testFileName = "testUploadDataObjectDataWithTicket.dat";
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
		
		// put a ticket on the collection with write access using the main account.
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		IRODSFile targetIrodsCollection = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(testingPropertiesHelper
						.buildIRODSCollectionAbsolutePathFromTestProperties(
								testingProperties, IRODS_TEST_SUBDIR_PATH));
		TicketServiceFactory ticketServiceFactory = new TicketServiceFactoryImpl(accessObjectFactory);
		TicketAdminService ticketService = ticketServiceFactory.instanceTicketAdminService(irodsAccount);
		String ticketString = new TicketRandomString(15).nextString();
		ticketService.createTicket(TicketCreateModeEnum.WRITE, targetIrodsCollection, ticketString);

		// Use another account to upload the file
		IRODSAccount secondaryIrodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://" + irodsAccount.getUserName() + ":" + irodsAccount.getPassword() + "@localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.EXTERNAL_REST_PORT_PROPERTY));
		sb.append("/irods-rest/rest/fileContents");
		sb.append(targetIrodsFile);
		sb.append("?ticket=");
		sb.append(ticketString);
		
		System.out.println("REQUEST : " + sb.toString());
		
		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(secondaryIrodsAccount, testingProperties);

		try {

			HttpPost httpPost = new HttpPost(sb.toString());
		    
		    HttpParams parameters = new BasicHttpParams();
		    parameters.setParameter("ticket", ticketString);
		    httpPost.setParams(parameters);

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
			ticketService.deleteTicket(ticketString);
		}
	}

	@Test
	public void testDownloadDataObjectDataWithTicket() throws Exception {
		// generate a local scratch file
		long length = 100 * 1024;
		String testFileName = "testDownloadDataObjectDataWithTicket.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						length);

		String targetIrodsFilePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsFilePath, "", null, null);
		
		IRODSFile targetIrodsFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsFilePath);
		
		TicketServiceFactory ticketServiceFactory = new TicketServiceFactoryImpl(accessObjectFactory);
		TicketAdminService ticketService = ticketServiceFactory.instanceTicketAdminService(irodsAccount);
		String ticketString = new TicketRandomString(15).nextString();
	
		ticketService.createTicket(TicketCreateModeEnum.READ, targetIrodsFile, ticketString);
		
		// Use another account to upload the file
		IRODSAccount secondaryIrodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);

		StringBuilder sb = new StringBuilder();
		sb.append("http://" + secondaryIrodsAccount.getUserName() + ":" + secondaryIrodsAccount.getPassword() + "@localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.EXTERNAL_REST_PORT_PROPERTY));
		sb.append("/irods-rest/rest/fileContents");
		sb.append(targetIrodsFilePath);
		sb.append("?ticket=");
		sb.append(ticketString);

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpGet httpGet = new HttpGet(sb.toString());
			// httpPost.addHeader("Content-type", "multipart/form-data");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpGet, clientAndContext.getHttpContext());

			HttpEntity entity = response.getEntity();
			long len = 0;
			InputStream inputStream = null;

			if (entity != null) {
				len = entity.getContentLength();
				inputStream = entity.getContent();
				// write the file to whether you want it.
			}
			Assert.assertEquals("invalid content length returned", length, len);

			Assert.assertNotNull("null input stream returned", inputStream);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
			ticketService.deleteTicket(ticketString);
		}
	}
	
	
	@Test
	public void testAnonymousDownloadDataObjectDataWithTicket() throws Exception {
		// generate a local scratch file
		long length = 200 * 1024;
		String testFileName = "testAnonymousDownloadDataObjectDataWithTicket.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						length);

		String targetIrodsFilePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsFilePath, "", null, null);
		
		IRODSFile targetIrodsFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsFilePath);
		
		TicketServiceFactory ticketServiceFactory = new TicketServiceFactoryImpl(accessObjectFactory);
		TicketAdminService ticketService = ticketServiceFactory.instanceTicketAdminService(irodsAccount);
		String ticketString = new TicketRandomString(15).nextString();
	
		ticketService.createTicket(TicketCreateModeEnum.READ, targetIrodsFile, ticketString);


		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:");
		sb.append(testingPropertiesHelper.getPropertyValueAsInt(
				testingProperties, RestTestingProperties.EXTERNAL_REST_PORT_PROPERTY));
		sb.append("/irods-rest/rest/fileContents");
		sb.append(targetIrodsFilePath);
		sb.append("?ticket=");
		sb.append(ticketString);

		DefaultHttpClientAndContext clientAndContext = RestAuthUtils
				.httpClientSetup(irodsAccount, testingProperties);
		try {

			HttpGet httpGet = new HttpGet(sb.toString());
			// httpPost.addHeader("Content-type", "multipart/form-data");

			HttpResponse response = clientAndContext.getHttpClient().execute(
					httpGet, clientAndContext.getHttpContext());

			HttpEntity entity = response.getEntity();
			long len = 0;
			InputStream inputStream = null;

			if (entity != null) {
				len = entity.getContentLength();
				inputStream = entity.getContent();
				// write the file to whether you want it.
			}
			Assert.assertEquals("invalid content length returned", length, len);

			Assert.assertNotNull("null input stream returned", inputStream);

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			clientAndContext.getHttpClient().getConnectionManager().shutdown();
			ticketService.deleteTicket(ticketString);
		}
	}


}
