package org.irods.jargon.rest.commands.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.RuleProcessingAO.RuleProcessingType;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.irods.jargon.rest.domain.RuleExecResultWrapper;
import org.irods.jargon.rest.domain.RuleParameterWrapper;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RuleFunctionsImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	public static final String IRODS_TEST_SUBDIR_PATH = "RuleFunctionsImplTest";
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
	public void testExecuteRuleClassic() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String ruleString = "ListAvailableMS||msiListEnabledMS(*KVPairs)##writeKeyValPairs(stdout,*KVPairs, \": \")|nop\n*A=hello\n ruleExecOut";

		RestConfiguration restConfiguration = new RestConfiguration();

		List<RuleParameterWrapper> inputParameterOverrides = new ArrayList<RuleParameterWrapper>();

		RuleFunctions ruleFunctions = new RuleFunctionsImpl(restConfiguration,
				irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());

		RuleExecResultWrapper actualExecResultWrapper = ruleFunctions
				.executeRule(ruleString, inputParameterOverrides,
						RuleProcessingType.CLASSIC);

		Assert.assertNotNull("null result", actualExecResultWrapper);
		Assert.assertEquals("expected exec out and error out", 2,
				actualExecResultWrapper.getOutputParameterResults().size());

	}
}
