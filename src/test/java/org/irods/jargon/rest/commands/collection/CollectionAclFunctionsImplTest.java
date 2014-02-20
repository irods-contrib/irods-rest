package org.irods.jargon.rest.commands.collection;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.irods.jargon.rest.domain.PermissionListing;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CollectionAclFunctionsImplTest {
	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "CollectionAclFunctionsImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
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

	@Test(expected = IllegalArgumentException.class)
	public void testListCollectionAclsNullPath() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		RestConfiguration restConfiguration = new RestConfiguration();

		CollectionAclFunctionsImpl collectionAclFunctionsImpl = new CollectionAclFunctionsImpl(
				restConfiguration, irodsAccount,
				irodsFileSystem.getIRODSAccessObjectFactory());

		collectionAclFunctionsImpl.listPermissions(null);

	}

	@Test(expected = FileNotFoundException.class)
	public void testListCollectionAclsNotFound() throws Exception {

		String testCollectionName = "testListCollectionAclsNotFound";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		RestConfiguration restConfiguration = new RestConfiguration();

		CollectionAclFunctionsImpl collectionAclFunctionsImpl = new CollectionAclFunctionsImpl(
				restConfiguration, irodsAccount,
				irodsFileSystem.getIRODSAccessObjectFactory());

		collectionAclFunctionsImpl.listPermissions(targetIrodsCollection);

	}

	@Test
	public void testListCollectionAcls() throws Exception {

		String testCollectionName = "testListCollectionAcls";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollectionName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		CollectionAO collectionAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		IRODSFile irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		irodsFile.mkdirs();

		collectionAO
				.setAccessPermissionRead(
						"",
						targetIrodsCollection,
						testingProperties
								.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_USER_KEY),
						true);

		RestConfiguration restConfiguration = new RestConfiguration();

		CollectionAclFunctionsImpl collectionAclFunctionsImpl = new CollectionAclFunctionsImpl(
				restConfiguration, irodsAccount,
				irodsFileSystem.getIRODSAccessObjectFactory());

		PermissionListing listing = collectionAclFunctionsImpl
				.listPermissions(targetIrodsCollection);
		Assert.assertNotNull("null listing", listing);
		Assert.assertEquals("path not set", targetIrodsCollection,
				listing.getAbsolutePathString());
		Assert.assertEquals("wrong object type", ObjectType.COLLECTION,
				listing.getObjectType());
		Assert.assertFalse("no entries", listing.getPermissionEntries()
				.isEmpty());
	}
}
