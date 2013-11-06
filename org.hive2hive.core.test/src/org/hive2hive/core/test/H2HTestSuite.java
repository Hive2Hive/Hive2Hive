package org.hive2hive.core.test;

import org.hive2hive.core.test.file.FileManagerTest;
import org.hive2hive.core.test.model.FileTreeNodeTest;
import org.hive2hive.core.test.model.UserProfileTest;
import org.hive2hive.core.test.network.ConnectionTest;
import org.hive2hive.core.test.network.data.DataManagerTest;
import org.hive2hive.core.test.network.messaging.BaseMessageTest;
import org.hive2hive.core.test.network.messaging.BaseRequestMessageTest;
import org.hive2hive.core.test.process.GetProcessStepTest;
import org.hive2hive.core.test.process.ProcessStepTest;
import org.hive2hive.core.test.process.ProcessTest;
import org.hive2hive.core.test.process.PutProcessStepTest;
import org.hive2hive.core.test.process.common.GetLocationStepTest;
import org.hive2hive.core.test.process.common.GetUserProfileStepTest;
import org.hive2hive.core.test.process.common.PutLocationStepTest;
import org.hive2hive.core.test.process.common.PutUserProfileStepTest;
import org.hive2hive.core.test.process.manager.ProcessManagerTest;
import org.hive2hive.core.test.process.register.RegisterTest;
import org.hive2hive.core.test.tomp2p.ReplicationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This suit bundles all tests of hive2hive.
 * 
 * @author Seppi
 */
@RunWith(Suite.class)
@SuiteClasses({
		// All tests of Box2Box

		// TomP2P
		ReplicationTest.class,

		// Network
		ConnectionTest.class, DataManagerTest.class, BaseMessageTest.class,
		BaseRequestMessageTest.class,

		// Processes
		ProcessTest.class, ProcessManagerTest.class, ProcessStepTest.class, GetProcessStepTest.class,
		PutProcessStepTest.class,

		// Process: Common steps
		PutUserProfileStepTest.class, PutLocationStepTest.class, GetUserProfileStepTest.class,
		GetLocationStepTest.class,

		// Process: Register
		RegisterTest.class,

		// Files
		FileManagerTest.class,

		// Model
		FileTreeNodeTest.class, UserProfileTest.class

})
public class H2HTestSuite {

}
