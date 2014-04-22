package org.hive2hive.core;

import org.hive2hive.core.file.FileSynchronizerTest;
import org.hive2hive.core.file.FileUtilTest;
import org.hive2hive.core.model.IndexTest;
import org.hive2hive.core.model.MetaFileTest;
import org.hive2hive.core.model.UserCredentialsTest;
import org.hive2hive.core.network.ConnectionTest;
import org.hive2hive.core.network.H2HStorageMemoryTest;
import org.hive2hive.core.network.data.DataManagerTest;
import org.hive2hive.core.network.messages.BaseMessageTest;
import org.hive2hive.core.network.messages.BaseRequestMessageTest;
import org.hive2hive.core.network.messages.direct.BaseDirectRequestMessageTest;
import org.hive2hive.core.network.userprofiletask.TestUserProfileTask;
import org.hive2hive.core.processes.framework.ProcessListenerTest;
import org.hive2hive.core.processes.framework.SequentialProcessTest;
import org.hive2hive.core.processes.implementations.common.GetLocationStepTest;
import org.hive2hive.core.processes.implementations.common.PutLocationStepTest;
import org.hive2hive.core.processes.implementations.common.base.BaseDirectMessageProcessStepTest;
import org.hive2hive.core.processes.implementations.common.base.BaseGetProcessStepTest;
import org.hive2hive.core.processes.implementations.common.base.BaseMessageProcessStepTest;
import org.hive2hive.core.processes.implementations.common.base.BasePutProcessStepTest;
import org.hive2hive.core.processes.implementations.common.base.BaseRemoveProcessStepTest;
import org.hive2hive.core.processes.implementations.files.AddFileTest;
import org.hive2hive.core.processes.implementations.files.DeleteFileTest;
import org.hive2hive.core.processes.implementations.files.DownloadFileTest;
import org.hive2hive.core.processes.implementations.files.GetFileListProcessTest;
import org.hive2hive.core.processes.implementations.files.MoveFileTest;
import org.hive2hive.core.processes.implementations.files.RecoverFileTest;
import org.hive2hive.core.processes.implementations.files.UpdateFileTest;
import org.hive2hive.core.processes.implementations.login.GetUserProfileStepTest;
import org.hive2hive.core.processes.implementations.login.LoginTest;
import org.hive2hive.core.processes.implementations.logout.LogoutTest;
import org.hive2hive.core.processes.implementations.notify.NotificationTest;
import org.hive2hive.core.processes.implementations.register.PutUserProfileStepTest;
import org.hive2hive.core.processes.implementations.register.RegisterProcessTest;
import org.hive2hive.core.security.EncryptionUtilTest;
import org.hive2hive.core.security.H2HSignatureFactoryTest;
import org.hive2hive.core.security.PasswordUtilTest;
import org.hive2hive.core.tomp2p.FromToTest;
import org.hive2hive.core.tomp2p.ReplicationTest;
import org.hive2hive.core.tomp2p.SecurityTest;
import org.hive2hive.core.tomp2p.TTLTest;
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
	ReplicationTest.class, FromToTest.class, SecurityTest.class, TTLTest.class,

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
	IndexTest.class, UserCredentialsTest.class, MetaFileTest.class,
	
	// Security
	EncryptionUtilTest.class, H2HSignatureFactoryTest.class, PasswordUtilTest.class

})
public class H2HTestSuite {

}
