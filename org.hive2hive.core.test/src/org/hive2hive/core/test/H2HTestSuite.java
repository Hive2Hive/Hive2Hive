package org.hive2hive.core.test;

import org.hive2hive.core.test.file.FileUtilTest;
import org.hive2hive.core.test.file.FileSynchronizerTest;
import org.hive2hive.core.test.model.IndexTest;
import org.hive2hive.core.test.model.MetaFileTest;
import org.hive2hive.core.test.model.UserCredentialsTest;
import org.hive2hive.core.test.network.ConnectionTest;
import org.hive2hive.core.test.network.H2HStorageMemoryTest;
import org.hive2hive.core.test.network.data.DataManagerTest;
import org.hive2hive.core.test.network.messages.BaseMessageTest;
import org.hive2hive.core.test.network.messages.BaseRequestMessageTest;
import org.hive2hive.core.test.network.messages.direct.BaseDirectRequestMessageTest;
import org.hive2hive.core.test.processes.framework.ProcessListenerTest;
import org.hive2hive.core.test.processes.framework.SequentialProcessTest;
import org.hive2hive.core.test.processes.implementations.common.GetLocationStepTest;
import org.hive2hive.core.test.processes.implementations.common.PutLocationStepTest;
import org.hive2hive.core.test.processes.implementations.common.base.BaseDirectMessageProcessStepTest;
import org.hive2hive.core.test.processes.implementations.common.base.BaseGetProcessStepTest;
import org.hive2hive.core.test.processes.implementations.common.base.BaseMessageProcessStepTest;
import org.hive2hive.core.test.processes.implementations.common.base.BasePutProcessStepTest;
import org.hive2hive.core.test.processes.implementations.common.base.BaseRemoveProcessStepTest;
import org.hive2hive.core.test.processes.implementations.files.AddFileTest;
import org.hive2hive.core.test.processes.implementations.files.DeleteFileTest;
import org.hive2hive.core.test.processes.implementations.files.DownloadFileTest;
import org.hive2hive.core.test.processes.implementations.files.GetFileListProcessTest;
import org.hive2hive.core.test.processes.implementations.files.MoveFileTest;
import org.hive2hive.core.test.processes.implementations.files.RecoverFileTest;
import org.hive2hive.core.test.processes.implementations.files.UpdateFileTest;
import org.hive2hive.core.test.processes.implementations.login.GetUserProfileStepTest;
import org.hive2hive.core.test.processes.implementations.login.LoginTest;
import org.hive2hive.core.test.processes.implementations.logout.LogoutTest;
import org.hive2hive.core.test.processes.implementations.notify.NotificationTest;
import org.hive2hive.core.test.processes.implementations.register.PutUserProfileStepTest;
import org.hive2hive.core.test.processes.implementations.register.RegisterProcessTest;
import org.hive2hive.core.test.processes.implementations.userprofiletask.TestUserProfileTask;
import org.hive2hive.core.test.tomp2p.ReplicationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This suit bundles all tests of <code>hive2hive</code>.
 * 
 * @author Seppi
 */
@RunWith(Suite.class)
@SuiteClasses({
// TomP2P
		ReplicationTest.class,

		// Network
		H2HStorageMemoryTest.class, ConnectionTest.class,
		// Network, Data
		DataManagerTest.class,
		// Network, Message
		BaseMessageTest.class, BaseRequestMessageTest.class,
		// Network, Message, Direct
		BaseDirectRequestMessageTest.class,

		// Processes
		ProcessListenerTest.class, SequentialProcessTest.class,

		// ProcessStep, Common, Get
		BaseGetProcessStepTest.class, GetLocationStepTest.class, GetUserProfileStepTest.class,
		// ProcessStep, Common, Put
		BasePutProcessStepTest.class, PutLocationStepTest.class, PutUserProfileStepTest.class,
		// ProcessStep, Common, Remove
		BaseRemoveProcessStepTest.class,
		// ProcessStep, Common, Message
		BaseMessageProcessStepTest.class, BaseDirectMessageProcessStepTest.class,

		// Process: Register, Login, Logout
		RegisterProcessTest.class, LoginTest.class, LogoutTest.class,

		// Process: Upload and download
		AddFileTest.class, UpdateFileTest.class, DownloadFileTest.class,

		// Process: Delete
		DeleteFileTest.class,

		// Process: Move
		MoveFileTest.class,

		// Process: Recover
		RecoverFileTest.class,

		// Process: Filelist
		GetFileListProcessTest.class,

		// Process: Notification and UP Tasks
		NotificationTest.class, TestUserProfileTask.class,

		// Files
		FileUtilTest.class, FileSynchronizerTest.class,

		// Model
		IndexTest.class, UserCredentialsTest.class, MetaFileTest.class

})
public class H2HTestSuite {

}
