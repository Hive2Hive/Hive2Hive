package org.hive2hive.core.test.process.digest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.digest.GetDigestProcess;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.integration.TestFileConfiguration;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.ProcessTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests getting digest.
 * 
 * @author Seppi
 * 
 */
public class GetDigestTest extends H2HJUnitTest {

	private final static int networkSize = 3;
	private final TestFileConfiguration config = new TestFileConfiguration();
	
	private static List<NetworkManager> network;
	private static UserCredentials userCredentials;
	private static FileManager fileManager;

	private static File root;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = GetDigestTest.class;
		beforeClass();

		/** create a network, register a user**/
		network = NetworkTestUtil.createNetwork(networkSize);
		userCredentials = NetworkTestUtil.generateRandomCredentials();

		// register a user
		ProcessTestUtil.register(userCredentials, network.get(0));

		root = new File(System.getProperty("java.io.tmpdir"), NetworkTestUtil.randomString());
		fileManager = new FileManager(root.toPath());
	}

	@Test
	public void getDigestTest() throws Exception {
		NetworkManager client = network.get(new Random().nextInt(networkSize));
		UserProfileManager profileManager = new UserProfileManager(client, userCredentials);
		
		GetDigestProcess getDigestProcess = ProcessTestUtil.getDigest(client, profileManager, fileManager, config);
		
		assertTrue(getDigestProcess.getDigest().isEmpty());
		
		// add child1 to the network
		File child1 = new File(root, NetworkTestUtil.randomString());
		FileUtils.writeStringToFile(child1, NetworkTestUtil.randomString());
		ProcessTestUtil.uploadNewFile(client, child1, profileManager, fileManager, config);

		getDigestProcess = ProcessTestUtil.getDigest(client, profileManager, fileManager, config);
		assertEquals(1, getDigestProcess.getDigest().size());
		assertEquals(root.toPath().relativize(child1.toPath()).toString(), getDigestProcess.getDigest().get(0).toString());

		// add dir1 to the network
		File dir1 = new File(root, NetworkTestUtil.randomString());
		dir1.mkdir();
		ProcessTestUtil.uploadNewFile(client, dir1, profileManager, fileManager, config);

		// add dir1/child1 to the network
		File dir1Child1 = new File(dir1, NetworkTestUtil.randomString());
		FileUtils.writeStringToFile(dir1Child1, NetworkTestUtil.randomString());
		ProcessTestUtil.uploadNewFile(client, dir1Child1, profileManager, fileManager, config);
		
		getDigestProcess = ProcessTestUtil.getDigest(client, profileManager, fileManager, config);
		assertEquals(2, getDigestProcess.getDigest().size());
		assertEquals(root.toPath().relativize(dir1Child1.toPath()).toString(), getDigestProcess.getDigest().get(1).toString());
		
		// delete child1 from the network
		ProcessTestUtil.deleteFile(client, child1, profileManager, fileManager, config);
		
		getDigestProcess = ProcessTestUtil.getDigest(client, profileManager, fileManager, config);
		assertEquals(1, getDigestProcess.getDigest().size());
		assertEquals(root.toPath().relativize(dir1Child1.toPath()).toString(), getDigestProcess.getDigest().get(0).toString());		
	}

	@AfterClass
	public static void endTest() throws IOException {
		FileUtils.deleteDirectory(fileManager.getRoot().toFile());
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
