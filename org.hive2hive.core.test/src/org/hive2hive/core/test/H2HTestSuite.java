package org.hive2hive.core.test;

import org.hive2hive.core.test.file.FileManagerTest;
import org.hive2hive.core.test.file.FileSynchronizerTest;
import org.hive2hive.core.test.model.FileTreeNodeTest;
import org.hive2hive.core.test.model.MetaFileTest;
import org.hive2hive.core.test.model.UserCredentialsTest;
import org.hive2hive.core.test.network.ConnectionTest;
import org.hive2hive.core.test.network.data.DataManagerTest;
import org.hive2hive.core.test.network.messages.BaseMessageTest;
import org.hive2hive.core.test.network.messages.BaseRequestMessageTest;
import org.hive2hive.core.test.process.ProcessTest;
import org.hive2hive.core.test.process.common.get.GetLocationStepTest;
import org.hive2hive.core.test.process.common.get.GetProcessStepTest;
import org.hive2hive.core.test.process.common.get.GetUserProfileStepTest;
import org.hive2hive.core.test.process.common.massages.BaseDirectMessageProcessStepTest;
import org.hive2hive.core.test.process.common.massages.BaseMessageProcessStepTest;
import org.hive2hive.core.test.process.common.put.PutLocationStepTest;
import org.hive2hive.core.test.process.common.put.PutProcessStepTest;
import org.hive2hive.core.test.process.common.put.PutUserProfileStepTest;
import org.hive2hive.core.test.process.common.remove.RemoveProcessStepTest;
import org.hive2hive.core.test.process.download.DownloadFileTest;
import org.hive2hive.core.test.process.files.NewFileTest;
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
		ProcessTest.class, ProcessManagerTest.class, GetProcessStepTest.class, PutProcessStepTest.class,
		RemoveProcessStepTest.class,

		// Process: Common steps
		PutUserProfileStepTest.class, PutLocationStepTest.class, GetUserProfileStepTest.class,
		GetLocationStepTest.class,

		// Process: Common steps, Messages
		BaseMessageProcessStepTest.class, BaseDirectMessageProcessStepTest.class,

		// Process: Register
		RegisterTest.class,

		// Process: Upload
		NewFileTest.class,

		// Process: Download
		DownloadFileTest.class,

		// Files
		FileManagerTest.class, FileSynchronizerTest.class,

		// Model
		FileTreeNodeTest.class, UserCredentialsTest.class, MetaFileTest.class

})
public class H2HTestSuite {

}
