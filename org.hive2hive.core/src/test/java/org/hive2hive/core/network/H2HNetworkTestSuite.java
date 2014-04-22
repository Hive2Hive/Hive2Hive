package org.hive2hive.core.network;

import org.hive2hive.core.network.data.DataManagerTest;
import org.hive2hive.core.network.data.UserProfileManagerTest;
import org.hive2hive.core.network.messages.BaseMessageTest;
import org.hive2hive.core.network.messages.BaseRequestMessageTest;
import org.hive2hive.core.network.messages.MessageSignatureTest;
import org.hive2hive.core.network.messages.direct.BaseDirectRequestMessageTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This suit bundles all tests of the network package.
 * 
 * @author Seppi
 */
@RunWith(Suite.class)
@SuiteClasses({
		// Network
		H2HStorageMemoryTest.class, ConnectionTest.class,
		// Network, Data
		DataManagerTest.class, UserProfileManagerTest.class,
		// Network, Message
		MessageSignatureTest.class, BaseMessageTest.class, BaseRequestMessageTest.class,
		// Network, Message, Direct
		BaseDirectRequestMessageTest.class,
})
public class H2HNetworkTestSuite {

}
