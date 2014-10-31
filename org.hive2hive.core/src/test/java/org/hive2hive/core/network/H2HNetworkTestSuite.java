package org.hive2hive.core.network;

import org.hive2hive.core.network.data.ContentProtectionTest;
import org.hive2hive.core.network.data.DataManagerTest;
import org.hive2hive.core.network.data.PublicKeyManagerTest;
import org.hive2hive.core.network.data.UserProfileManagerTest;
import org.hive2hive.core.network.data.futures.FutureGetTest;
import org.hive2hive.core.network.data.futures.FuturePutTest;
import org.hive2hive.core.network.data.futures.FutureRemoveTest;
import org.hive2hive.core.network.data.vdht.EncryptedVersionManagerTest;
import org.hive2hive.core.network.data.vdht.VersionManagerTest;
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
		ConnectionTest.class,
		// Network, Data
		DataManagerTest.class, UserProfileManagerTest.class, ContentProtectionTest.class, PublicKeyManagerTest.class,
		VersionManagerTest.class, EncryptedVersionManagerTest.class,
		// Network, Data, Futures
		FutureGetTest.class, FuturePutTest.class, FutureRemoveTest.class,
		// Network, Message
		MessageSignatureTest.class, BaseMessageTest.class, BaseRequestMessageTest.class,
		// Network, Message, Direct
		BaseDirectRequestMessageTest.class, })
public class H2HNetworkTestSuite {

}
