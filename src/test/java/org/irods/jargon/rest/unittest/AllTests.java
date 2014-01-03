package org.irods.jargon.rest.unittest;

import org.irods.jargon.rest.commands.collection.CollectionServiceTest;
import org.irods.jargon.rest.commands.dataobject.DataObjectServiceTest;
import org.irods.jargon.rest.commands.user.UserGroupServiceTest;
import org.irods.jargon.rest.commands.user.UserServiceTest;
import org.irods.jargon.rest.environment.ServerEnvironmentServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ UserGroupServiceTest.class, UserServiceTest.class,
		ServerEnvironmentServiceTest.class, CollectionServiceTest.class,
		DataObjectServiceTest.class })
public class AllTests {

}
