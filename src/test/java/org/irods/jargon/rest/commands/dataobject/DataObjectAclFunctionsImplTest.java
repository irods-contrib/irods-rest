package org.irods.jargon.rest.commands.dataobject;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.pub.DataObjectAOImpl;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.irods.jargon.rest.domain.PermissionListing;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataObjectAclFunctionsImplTest {

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

	@Test(expected = FileNotFoundException.class)
	public void testListPermissionsNotFound() throws Exception {
		String testFileName = "testListPermissionsNotFound.xls";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		RestConfiguration restConfiguration = new RestConfiguration();

		DataObjectAclFunctionsImpl dataObjectAclFunctionsImpl = new DataObjectAclFunctionsImpl(
				restConfiguration, irodsAccount,
				irodsFileSystem.getIRODSAccessObjectFactory());

		dataObjectAclFunctionsImpl.listPermissions(targetIrodsCollection + "/"
				+ testFileName);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testListPermissionsNullPath() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		RestConfiguration restConfiguration = new RestConfiguration();

		DataObjectAclFunctionsImpl dataObjectAclFunctionsImpl = new DataObjectAclFunctionsImpl(
				restConfiguration, irodsAccount,
				irodsFileSystem.getIRODSAccessObjectFactory());

		dataObjectAclFunctionsImpl.listPermissions(null);
	}

	@Test
	public void testListPermissions() throws Exception {
		String testFileName = "testListPermissionsForDataObject.xls";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String fileNameOrig = FileGenerator.generateFileOfFixedLengthGivenName(
				absPath, testFileName, 2);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataObjectAOImpl dataObjectAO = (DataObjectAOImpl) irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);

		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		dto.putOperation(fileNameOrig, targetIrodsCollection, "", null, null);

		dataObjectAO.setAccessPermissionRead("", targetIrodsCollection + "/"
				+ testFileName, testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY));

		RestConfiguration restConfiguration = new RestConfiguration();

		DataObjectAclFunctionsImpl dataObjectAclFunctionsImpl = new DataObjectAclFunctionsImpl(
				restConfiguration, irodsAccount,
				irodsFileSystem.getIRODSAccessObjectFactory());

		PermissionListing listing = dataObjectAclFunctionsImpl
				.listPermissions(targetIrodsCollection + "/" + testFileName);
		Assert.assertNotNull("null listing", listing);
		Assert.assertEquals("path not set", targetIrodsCollection + "/"
				+ testFileName, listing.getAbsolutePathString());
		Assert.assertEquals("wrong object type", ObjectType.DATA_OBJECT,
				listing.getObjectType());
		Assert.assertFalse("no entries", listing.getPermissionEntries()
				.isEmpty());

	}

}
