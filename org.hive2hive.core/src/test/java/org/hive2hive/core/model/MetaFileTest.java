package org.hive2hive.core.model;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.security.EncryptionUtil;
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
		versions.add(new FileVersion(0, 123, System.currentTimeMillis(), new ArrayList<MetaChunk>()));
		versions.add(new FileVersion(1, 123, System.currentTimeMillis(), new ArrayList<MetaChunk>()));
		// timestamp is older
		versions.add(new FileVersion(2, 123, System.currentTimeMillis() - 1000 * 60, new ArrayList<MetaChunk>()));

		MetaFileSmall metaFileSmall = new MetaFileSmall(keys.getPublic(), versions, keys);

		FileVersion newestVersion = metaFileSmall.getNewestVersion();
		Assert.assertEquals(2, newestVersion.getIndex());

	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}
}
