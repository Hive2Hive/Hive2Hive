package org.hive2hive.core.test.process.upload;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.register.RegisterProcess;
import org.hive2hive.core.process.upload.UploadFileProcess;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.integration.TestH2HFileConfiguration;
import org.hive2hive.core.test.network.NetworkGetUtil;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests uploading a file.
 * 
 * @author Nico
 * 
 */
public class UploadFileTest extends H2HJUnitTest {

	private final int networkSize = 10;
	private List<NetworkManager> network;
	private UserProfile userProfile;
	private UserCredentials userCredentials;
	private FileManager fileManager;
	private IH2HFileConfiguration config = new TestH2HFileConfiguration();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UploadFileTest.class;
		beforeClass();

	}

	@Before
	public void createProfile() {
		network = NetworkTestUtil.createNetwork(networkSize);
		userCredentials = NetworkTestUtil.generateRandomCredentials();

		// register a user
		RegisterProcess process = new RegisterProcess(userCredentials, network.get(0));
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		userProfile = process.getContext().getUserProfile();
		String randomName = NetworkTestUtil.randomString();
		File root = new File(System.getProperty("java.io.tmpdir"), randomName);
		fileManager = new FileManager(root);
	}

	@Test
	public void testUploadSingleChunk() throws IOException {
		NetworkManager client = network.get(new Random().nextInt(networkSize));
		String fileName = NetworkTestUtil.randomString();
		File toUpload = new File(fileManager.getRoot(), fileName);
		FileUtils.write(toUpload, NetworkTestUtil.randomString());

		UploadFileProcess process = new UploadFileProcess(toUpload, userProfile, userCredentials, client,
				fileManager, config);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// pick new client to test
		client = network.get(new Random().nextInt(networkSize));

		// test if there is something in the user profile
		UserProfile gotProfile = NetworkGetUtil.getUserProfile(client, userCredentials);
		Assert.assertNotNull(gotProfile);

		FileTreeNode node = gotProfile.getRoot().getChildByName(fileName);
		Assert.assertNotNull(node);

		KeyPair metaFileKeys = node.getKeyPair();
		// TODO
		// 1. get the meta file with the keys (decrypt it)
		// 2. get all version chunks with the keys in the meta file (decrypt them)
		// 3. verify the content
	}

	@After
	public void deleteAndShutdown() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		FileUtils.deleteDirectory(fileManager.getRoot());
	}

	@AfterClass
	public static void endTest() throws IOException {
		afterClass();
	}
}
