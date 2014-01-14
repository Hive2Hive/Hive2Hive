package org.hive2hive.core.test.process.files;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.IFileConfiguration;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.recover.IVersionSelector;
import org.hive2hive.core.process.recover.RecoverFileProcess;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.integration.TestFileConfiguration;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.ProcessTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
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
	private static IFileConfiguration config = new TestFileConfiguration();

	private UserCredentials userCredentials;
	private File file;
	private NetworkManager client;
	private FileManager fileManager;
	private UserProfileManager profileManager;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = RecoverFileTest.class;
		beforeClass();

		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Before
	public void registerAndAddFileVersions() throws IOException, IllegalFileLocation {
		userCredentials = NetworkTestUtil.generateRandomCredentials();
		client = network.get(new Random().nextInt(networkSize));

		// register a user
		ProcessTestUtil.register(userCredentials, client);
		profileManager = new UserProfileManager(client, userCredentials);

		// add an intial file to the network
		String randomName = NetworkTestUtil.randomString();
		File root = new File(System.getProperty("java.io.tmpdir"), randomName);
		fileManager = new FileManager(root.toPath());
		file = new File(root, "test-file");
		FileUtils.write(file, "0");
		ProcessTestUtil.uploadNewFile(client, file, profileManager, fileManager, config);
	}

	private void uploadVersion(String content) throws IOException {
		FileUtils.write(file, content);
		ProcessTestUtil.uploadNewFileVersion(client, file, profileManager, fileManager, config);
	}

	@Test
	public void testRestoreVersion() throws IOException, NoSessionException {
		// add 3 new versions (total 4)
		uploadVersion("1");
		uploadVersion("2");
		uploadVersion("3");

		final int versionToRestore = 2;

		RecoverFileProcess process = new RecoverFileProcess(client, file, new IVersionSelector() {
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
		});

		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait until the file is restored
		H2HWaiter waiter = new H2HWaiter(120);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// to verify, find the restored file
		File restoredFile = null;
		File root = fileManager.getRoot().toFile();
		File[] listFiles = root.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().startsWith(file.getName());
			}
		});

		for (File fileInList : listFiles) {
			if (fileInList.getName().startsWith(file.getName() + "_")) {
				restoredFile = fileInList;
				break;
			}
		}

		Assert.assertEquals(2, listFiles.length);

		String content = FileUtils.readFileToString(restoredFile);
		Assert.assertEquals(versionToRestore, Integer.parseInt(content));
	}

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
