package org.hive2hive.core.test;

import org.hive2hive.core.test.network.messages.BaseMessageTest;
import org.hive2hive.core.test.network.messages.BaseRequestMessageTest;
import org.hive2hive.core.test.process.common.massages.BaseDirectMessageProcessStepTest;
import org.hive2hive.core.test.process.common.massages.BaseMessageProcessStepTest;
import org.hive2hive.core.test.process.login.postLogin.ContactPeersStepTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This suit bundles all tests of hive2hive which contain any messaging.
 * 
 * @author Seppi
 */
@RunWith(Suite.class)
@SuiteClasses({

		// Network
		BaseMessageTest.class, BaseRequestMessageTest.class,

		// Process: Common steps, Messages
		BaseMessageProcessStepTest.class, BaseDirectMessageProcessStepTest.class,
		
		// Process: Login steps
		ContactPeersStepTest.class,
		
})
public class H2HMessageTestSuite {

}
