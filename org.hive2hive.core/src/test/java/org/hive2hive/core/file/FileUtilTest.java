package org.hive2hive.core.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.download.DownloadManager;
import org.hive2hive.core.security.EncryptionUtil;
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

		PublicKeyManager publicKeyManager = new PublicKeyManager("user", EncryptionUtil.generateRSAKeyPair(),
				null);
		DownloadManager downloadManager = new DownloadManager(null, null, publicKeyManager, null);
		FileUtil.writePersistentMetaData(root.toPath(), publicKeyManager, downloadManager);
		PersistentMetaData persistentMetaData = FileUtil.readPersistentMetaData(root.toPath());
		Map<String, byte[]> fileTree = persistentMetaData.getFileTree();
		Assert.assertTrue(fileTree.containsKey(fileName));
	}

	@Test
	public void testSortPreorder() {
		List<File> files = new ArrayList<File>();

		File aaa = new File("/aaa");
		files.add(aaa);

		File bbb = new File("/bbb");
		files.add(bbb);

		File c = new File(aaa, "c.txt");
		files.add(c);

		File d = new File("/bzz", "d.txt");
		files.add(d);

		File bzz = new File("/bzz");
		files.add(bzz);

		FileUtil.sortPreorder(files);

		int index = 0;
		Assert.assertEquals(aaa, files.get(index++));
		Assert.assertEquals(c, files.get(index++));
		Assert.assertEquals(bbb, files.get(index++));
		Assert.assertEquals(bzz, files.get(index++));
		Assert.assertEquals(d, files.get(index++));
	}
}
