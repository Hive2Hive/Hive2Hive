package org.hive2hive.core.test.file;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.file.FileUtil;
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
 * Test the file util used for the H2H node.
 * 
 * @author Nico
 * 
 */
public class FileUtilTest extends H2HJUnitTest {

	private File root;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FileUtilTest.class;
		beforeClass();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	@Before
	public void createRoot() {
		String randomName = NetworkTestUtil.randomString();
		root = new File(System.getProperty("java.io.tmpdir"), randomName);
	}

	@After
	public void cleanup() throws IOException {
		FileUtils.deleteDirectory(root);
	}

	@Test
	public void testReadWriteMetaData() throws IOException, ClassNotFoundException {
		String fileName = "test-file";
		File file = new File(root, fileName);
		FileUtils.writeStringToFile(file, NetworkTestUtil.randomString());

		FileUtil.writePersistentMetaData(root.toPath(), null);
		PersistentMetaData persistentMetaData = FileUtil.readPersistentMetaData(root.toPath());
		Map<String, byte[]> fileTree = persistentMetaData.getFileTree();
		Assert.assertTrue(fileTree.containsKey(fileName));
	}
}
