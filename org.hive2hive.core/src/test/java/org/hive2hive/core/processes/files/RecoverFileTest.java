package org.hive2hive.core.processes.files;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileTestUtil;
import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.hive2hive.core.processes.util.UseCaseTestUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.abstracts.ProcessComponent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.util.TestExecutionUtil;
import org.hive2hive.processframework.util.TestProcessComponentListener;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests restoring a file version.
 * Concept for this test: File has the file version number (e.g. "0", "1", ...) as the content. This
 * simplifies the verification.
 * 
 * @author Nico
 * 
 */
public class RecoverFileTest extends H2HJUnitTest {

	private static final int networkSize = 5;
	private static List<NetworkManager> network;

	private NetworkManager client;
	private UserCredentials userCredentials;
	private File root;
	private File file;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = RecoverFileTest.class;
		beforeClass();

		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Before
	public void registerAndAddFileVersions() throws IOException, IllegalFileLocation, NoSessionException,
			NoPeerConnectionException {
		userCredentials = NetworkTestUtil.generateRandomCredentials();
		client = network.get(new Random().nextInt(networkSize));

		// register a user
		root = FileTestUtil.getTempDirectory();
		UseCaseTestUtil.registerAndLogin(userCredentials, client, root);

		// add an intial file to the network
		file = new File(root, "test-file.txt");
		FileUtils.write(file, "0");
		UseCaseTestUtil.uploadNewFile(client, file);
	}

	private void uploadVersion(String content) throws IOException, NoSessionException, IllegalArgumentException,
			NoPeerConnectionException {
		FileUtils.write(file, content);
		UseCaseTestUtil.uploadNewVersion(client, file);
	}

	@Test
	public void testRestoreVersion() throws IOException, NoSessionException, InvalidProcessStateException,
			IllegalArgumentException, NoPeerConnectionException {
		// add 3 new versions (total 4)
		uploadVersion("1");
		uploadVersion("2");
		uploadVersion("3");

		final int versionToRestore = 2;

		TestVersionSelector selector = new TestVersionSelector(versionToRestore);
		ProcessComponent process = ProcessFactory.instance().createRecoverFileProcess(file, selector, client);
		TestProcessComponentListener listener = new TestProcessComponentListener();
		process.attachListener(listener);
		process.start();
		TestExecutionUtil.waitTillSucceded(listener, 120);

		// to verify, find the restored file
		File restoredFile = null;
		for (File fileInList : root.listFiles()) {
			if (fileInList.getName().equals(selector.getRecoveredFileName())) {
				restoredFile = fileInList;
				break;
			}
		}

		String content = FileUtils.readFileToString(restoredFile);
		Assert.assertEquals(versionToRestore, Integer.parseInt(content));
	}

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

	private class TestVersionSelector implements IVersionSelector {

		private final int versionToRestore;
		private String recoveredFileName;

		public TestVersionSelector(int versionToRestore) {
			this.versionToRestore = versionToRestore;
		}

		@Override
		public IFileVersion selectVersion(List<IFileVersion> availableVersions) {
			// should have 3 versions possible to restore
			Assert.assertEquals(3, availableVersions.size());
			for (IFileVersion version : availableVersions) {
				if (version.getIndex() == versionToRestore)
					return version;
			}
			return null;
		}

		@Override
		public String getRecoveredFileName(String fullName, String name, String extension) {
			recoveredFileName = name + "-recovered" + extension;
			return recoveredFileName;
		}

		public String getRecoveredFileName() {
			return recoveredFileName;
		}
	};
}
