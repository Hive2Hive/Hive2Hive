package org.hive2hive.core.processes.files.recover;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.TestExecutionUtil;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
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
 */
public class RecoverFileTest extends H2HJUnitTest {

	private static final int networkSize = 6;
	private static ArrayList<NetworkManager> network;

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
	public void registerAndAddFileVersions() throws IOException, IllegalArgumentException, NoSessionException,
			NoPeerConnectionException {
		userCredentials = generateRandomCredentials();
		client = NetworkTestUtil.getRandomNode(network);

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
			IllegalArgumentException, NoPeerConnectionException, ProcessExecutionException {
		// add 3 new versions (total 4)
		uploadVersion("1");
		uploadVersion("2");
		uploadVersion("3");

		final int versionToRestore = 2;

		TestVersionSelector selector = new TestVersionSelector(versionToRestore);
		IProcessComponent<Void> process = ProcessFactory.instance().createRecoverFileProcess(file, selector, client);
		TestExecutionUtil.executeProcessTillSucceded(process);

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
