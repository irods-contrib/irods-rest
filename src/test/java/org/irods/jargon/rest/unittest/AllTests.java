package org.irods.jargon.rest.unittest;

import org.irods.jargon.rest.auth.IrodsCorsFilterTest;
import org.irods.jargon.rest.auth.PamAuthTest;
import org.irods.jargon.rest.commands.ServiceFunctionFactoryImplTest;
import org.irods.jargon.rest.commands.collection.CollectionAclFunctionsImplTest;
import org.irods.jargon.rest.commands.collection.CollectionServiceTest;
import org.irods.jargon.rest.commands.dataobject.DataObjectAclFunctionsImplTest;
import org.irods.jargon.rest.commands.dataobject.DataObjectAvuFunctionsImplTest;
import org.irods.jargon.rest.commands.dataobject.DataObjectServiceTest;
import org.irods.jargon.rest.commands.dataobject.FileContentsServiceTest;
import org.irods.jargon.rest.commands.dataobject.FileContentsServiceTicketsTest;
import org.irods.jargon.rest.commands.query.GenQueryServiceTest;
import org.irods.jargon.rest.commands.rule.RuleFunctionsImplTest;
import org.irods.jargon.rest.commands.rule.RuleServiceTest;
import org.irods.jargon.rest.commands.ticket.ListAllTicketsServiceTest;
import org.irods.jargon.rest.commands.ticket.TicketServiceTest;
import org.irods.jargon.rest.commands.user.TemporaryPasswordServiceTest;
import org.irods.jargon.rest.commands.user.UserGroupServiceTest;
import org.irods.jargon.rest.commands.user.UserServiceTest;
import org.irods.jargon.rest.environment.ServerEnvironmentServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ UserGroupServiceTest.class, UserServiceTest.class,
		ServerEnvironmentServiceTest.class, CollectionServiceTest.class,
		DataObjectServiceTest.class, FileContentsServiceTest.class,
		FileContentsServiceTicketsTest.class,
		ServiceFunctionFactoryImplTest.class,
		CollectionAclFunctionsImplTest.class,
		DataObjectAclFunctionsImplTest.class,
		DataObjectAvuFunctionsImplTest.class, RuleFunctionsImplTest.class,
		RuleServiceTest.class, IrodsCorsFilterTest.class,
		TemporaryPasswordServiceTest.class, PamAuthTest.class,
		GenQueryServiceTest.class, TicketServiceTest.class,
		ListAllTicketsServiceTest.class })
public class AllTests {

}
