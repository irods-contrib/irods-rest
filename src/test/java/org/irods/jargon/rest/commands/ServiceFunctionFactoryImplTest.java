package org.irods.jargon.rest.commands;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.rest.commands.collection.CollectionAclFunctions;
import org.irods.jargon.rest.commands.dataobject.DataObjectAclFunctions;
import org.irods.jargon.rest.commands.dataobject.DataObjectAvuFunctions;
import org.irods.jargon.rest.commands.rule.RuleFunctions;
import org.irods.jargon.rest.configuration.RestConfiguration;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class ServiceFunctionFactoryImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	@Test
	public void testInstanceCollectionACLService() {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		RestConfiguration restConfiguration = new RestConfiguration();

		ServiceFunctionFactory serviceFunctionFactory = new ServiceFunctionFactoryImpl();
		serviceFunctionFactory
				.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		serviceFunctionFactory.setRestConfiguration(restConfiguration);

		CollectionAclFunctions actual = serviceFunctionFactory
				.instanceCollectionAclFunctions(irodsAccount);
		Assert.assertNotNull(actual);

	}

	@Test
	public void testInstanceDataObjectACLService() {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		RestConfiguration restConfiguration = new RestConfiguration();

		ServiceFunctionFactory serviceFunctionFactory = new ServiceFunctionFactoryImpl();
		serviceFunctionFactory
				.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		serviceFunctionFactory.setRestConfiguration(restConfiguration);

		DataObjectAclFunctions actual = serviceFunctionFactory
				.instanceDataObjectAclFunctions(irodsAccount);
		Assert.assertNotNull(actual);

	}

	@Test
	public void testInstanceDataObjectAvuService() {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		RestConfiguration restConfiguration = new RestConfiguration();

		ServiceFunctionFactory serviceFunctionFactory = new ServiceFunctionFactoryImpl();
		serviceFunctionFactory
				.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		serviceFunctionFactory.setRestConfiguration(restConfiguration);

		DataObjectAvuFunctions actual = serviceFunctionFactory
				.instanceDataObjectAvuFunctions(irodsAccount);
		Assert.assertNotNull(actual);

	}

	@Test
	public void testInstanceRuleService() {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		RestConfiguration restConfiguration = new RestConfiguration();

		ServiceFunctionFactory serviceFunctionFactory = new ServiceFunctionFactoryImpl();
		serviceFunctionFactory
				.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		serviceFunctionFactory.setRestConfiguration(restConfiguration);

		RuleFunctions actual = serviceFunctionFactory
				.instanceRuleFunctions(irodsAccount);
		Assert.assertNotNull(actual);

	}

}
