package org.hive2hive.core.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.download.DownloadManager;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.serializer.FSTSerializer;
import org.hive2hive.core.utils.helper.TestFileAgent;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the {@link FileUtil} used for the H2H node.
 * 
 * @author Nico
 * 
 */
public class FileUtilTest extends H2HJUnitTest {

	private static FSTSerializer serializer;
	private File root;
	private TestFileAgent fileAgent;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FileUtilTest.class;
		beforeClass();
		serializer = new FSTSerializer();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	@Before
	public void createRoot() {
		String randomName = randomString();
		root = new File(System.getProperty("java.io.tmpdir"), randomName);
		fileAgent = new TestFileAgent();
	}

	@After
	public void cleanup() throws IOException {
		FileUtils.deleteDirectory(root);
	}

	@Test
	public void testReadWriteMetaData() throws IOException, ClassNotFoundException {
		DownloadManager downloadManager = new DownloadManager(null, null);
		PublicKeyManager publicKeyManager = new PublicKeyManager("user", generateRSAKeyPair(RSA_KEYLENGTH.BIT_512),
				generateRSAKeyPair(RSA_KEYLENGTH.BIT_512), null);
		FileUtil.writePersistentMetaData(fileAgent, publicKeyManager, downloadManager, serializer);
		PersistentMetaData persistentMetaData = FileUtil.readPersistentMetaData(fileAgent, serializer);
		Assert.assertNotNull(persistentMetaData);
		Assert.assertEquals(0, persistentMetaData.getDownloads().size());
		Assert.assertEquals(0, persistentMetaData.getPublicKeyCache().size());
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
