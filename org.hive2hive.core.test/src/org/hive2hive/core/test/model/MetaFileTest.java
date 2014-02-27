package org.hive2hive.core.test.model;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.test.H2HJUnitTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MetaFileTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = MetaFileTest.class;
		beforeClass();
	}

	@Test
	public void testGetNewestVersion() {
		KeyPair keys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_FILE);
		List<FileVersion> versions = new ArrayList<FileVersion>();
		versions.add(new FileVersion(0, 123, System.currentTimeMillis(), new ArrayList<String>()));
		versions.add(new FileVersion(1, 123, System.currentTimeMillis(), new ArrayList<String>()));
		// timestamp is older
		versions.add(new FileVersion(2, 123, System.currentTimeMillis() - 1000 * 60, new ArrayList<String>()));

		MetaFile metaFile = new MetaFile(keys.getPublic(), "test file", versions, keys);

		FileVersion newestVersion = metaFile.getNewestVersion();
		Assert.assertEquals(2, newestVersion.getIndex());

	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}
}
