package org.hive2hive.core.test.file;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.file.PersistentMetaData;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the file manager used for the H2H node.
 * 
 * @author Nico
 * 
 */
public class FileManagerTest extends H2HJUnitTest {

	private FileManager fileManager;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FileManagerTest.class;
		beforeClass();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	@Before
	public void createFileManager() {
		String randomName = NetworkTestUtil.randomString();
		File root = new File(System.getProperty("java.io.tmpdir"), randomName);
		fileManager = new FileManager(root);
	}

	@After
	public void cleanup() throws IOException {
		FileUtils.deleteDirectory(fileManager.getRoot());
	}

	@Test
	public void testReadWriteMetaData() throws IOException {
		File file = new File(fileManager.getRoot(), "test-file");
		FileUtils.writeStringToFile(file, NetworkTestUtil.randomString());

		fileManager.writePersistentMetaData();
		PersistentMetaData persistentMetaData = fileManager.getPersistentMetaData();
		Map<String, byte[]> fileTree = persistentMetaData.getFileTree();
		Assert.assertTrue(fileTree.containsKey("/test-file"));
	}
}
