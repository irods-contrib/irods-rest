package org.irods.jargon.rest.commands.dataobject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.irods.jargon.rest.domain.MetadataListing;
import org.irods.jargon.rest.domain.MetadataOperationResultEntry;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataObjectAvuFunctionsImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "DataObjectAclFunctionsImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;
	private static ScratchFileUtils scratchFileUtils = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testListDataObjectMetadata() throws Exception {
		String testFileName = "testListDataObjectMetadata.dat";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 100);

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
		dto.putOperation(
				localFileName,
				targetIrodsCollection,
				testingProperties
						.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

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

		RestConfiguration restConfiguration = new RestConfiguration();

		DataObjectAvuFunctions dataObjectAvuFunctionsImpl = new DataObjectAvuFunctionsImpl(
				restConfiguration, irodsAccount,
				irodsFileSystem.getIRODSAccessObjectFactory());

		MetadataListing listing = dataObjectAvuFunctionsImpl
				.listDataObjectMetadata(dataObjectAbsPath);

		Assert.assertEquals("wrong absolute path", dataObjectAbsPath,
				listing.getUniqueNameString());
		Assert.assertEquals(
				"wrong object type",
				org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT,
				listing.getObjectType());
		Assert.assertFalse(listing.getMetadataEntries().isEmpty());

	}

	@Test
	public void testBulkAddAvu() throws Exception {
		String testFileName = "testBulkAddAvu.txt";
		String expectedAttribName = "testBulkAddAvu";
		String expectedValueName = "testBulkAddAvu";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		targetIrodsFile.deleteWithForceOption();
		targetIrodsFile.mkdirs();
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedValueName, "");
		List<AvuData> bulkAvuData = new ArrayList<AvuData>();
		bulkAvuData.add(avuData);

		RestConfiguration restConfiguration = new RestConfiguration();

		DataObjectAvuFunctions dataObjectAvuFunctionsImpl = new DataObjectAvuFunctionsImpl(
				restConfiguration, irodsAccount,
				irodsFileSystem.getIRODSAccessObjectFactory());

		List<MetadataOperationResultEntry> responses = dataObjectAvuFunctionsImpl
				.addAvuMetadata(targetIrodsDataObject, bulkAvuData);

		Assert.assertNotNull(responses);
		Assert.assertFalse(responses.isEmpty());

	}

	@Test
	public void testBulkDeleteAvu() throws Exception {
		String testFileName = "testBulkDeleteAvu.txt";
		String expectedAttribName = "testBulkDeleteAvu";
		String expectedValueName = "testBulkDeleteAvu";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsDataObject = targetIrodsCollection + "/"
				+ testFileName;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		targetIrodsFile.deleteWithForceOption();
		targetIrodsFile.mkdirs();
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dataTransferOperationsAO.putOperation(new File(fileNameOrig),
				targetIrodsFile, null, null);

		AvuData avuData = AvuData.instance(expectedAttribName,
				expectedValueName, "");
		List<AvuData> bulkAvuData = new ArrayList<AvuData>();
		bulkAvuData.add(avuData);

		RestConfiguration restConfiguration = new RestConfiguration();

		DataObjectAvuFunctions dataObjectAvuFunctionsImpl = new DataObjectAvuFunctionsImpl(
				restConfiguration, irodsAccount,
				irodsFileSystem.getIRODSAccessObjectFactory());

		dataObjectAvuFunctionsImpl.addAvuMetadata(targetIrodsDataObject,
				bulkAvuData);

		List<MetadataOperationResultEntry> responses = dataObjectAvuFunctionsImpl
				.deleteAvuMetadata(targetIrodsDataObject, bulkAvuData);

		Assert.assertNotNull(responses);
		Assert.assertFalse(responses.isEmpty());

	}

}
