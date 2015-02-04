package org.hive2hive.core.processes.files.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetFileListProcessTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static File root;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = GetFileListProcessTest.class;
		beforeClass();

		// network
		network = NetworkTestUtil.createNetwork(6);
		UserCredentials credentials = generateRandomCredentials();

		root = FileTestUtil.getTempDirectory();
		UseCaseTestUtil.registerAndLogin(credentials, network.get(0), root);
	}

	@AfterClass
	public static void endTest() throws IOException {
		NetworkTestUtil.shutdownNetwork(network);
		FileUtils.deleteDirectory(root);
		afterClass();
	}

	@Test
	public void getFileListTest() throws IOException, IllegalArgumentException, InvalidProcessStateException,
			NoPeerConnectionException, NoSessionException, ProcessExecutionException {
		NetworkManager client = network.get(0);
		FileNode fileList = UseCaseTestUtil.getFileList(client);

		// root does not have any children
		assertEquals(0, fileList.getChildren().size());

		// get sure about what a digest should actually deliver and adapt asserts below
		// add child1 to the network
		File child1 = new File(root, randomString());
		FileUtils.writeStringToFile(child1, randomString());
		UseCaseTestUtil.uploadNewFile(client, child1);

		fileList = UseCaseTestUtil.getFileList(client);
		assertEquals(1, fileList.getChildren().size());

		assertEquals(child1, fileList.getChildren().get(0).getFile());
		assertTrue(HashUtil.compare(HashUtil.hash(child1), fileList.getChildren().get(0).getMd5()));

		// add dir1 to the network
		File dir1 = new File(root, randomString());
		dir1.mkdir();
		UseCaseTestUtil.uploadNewFile(client, dir1);

		// add dir1/child1 to the network
		File dir1Child1 = new File(dir1, randomString());
		FileUtils.writeStringToFile(dir1Child1, randomString());
		UseCaseTestUtil.uploadNewFile(client, dir1Child1);

		// root folder has two direct children
		fileList = UseCaseTestUtil.getFileList(client);
		assertEquals(2, fileList.getChildren().size());

		for (FileNode child : fileList.getChildren()) {
			if (child.getName().equals(dir1.getName())) {
				// dir1 has 1 child
				assertEquals(1, child.getChildren().size());
				break;
			}
		}

		// delete child1 in the subdir from the network
		UseCaseTestUtil.deleteFile(client, dir1Child1);

		// still two children
		fileList = UseCaseTestUtil.getFileList(client);
		assertEquals(2, fileList.getChildren().size());

		for (FileNode child : fileList.getChildren()) {
			if (child.getName().equals(dir1.getName())) {
				// dir1 has no child anymore
				assertEquals(0, child.getChildren().size());
				break;
			}
		}
	}
}
