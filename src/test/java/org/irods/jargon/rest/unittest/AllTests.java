package org.irods.jargon.rest.unittest;

import org.irods.jargon.rest.commands.user.UserGroupServiceTest;
import org.irods.jargon.rest.commands.user.UserServiceTest;
import org.irods.jargon.rest.environment.ServerEnvironmentServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ UserGroupServiceTest.class, UserServiceTest.class,
		ServerEnvironmentServiceTest.class })
public class AllTests {

}
