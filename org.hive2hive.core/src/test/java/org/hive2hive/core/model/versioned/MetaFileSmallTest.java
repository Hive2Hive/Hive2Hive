package org.hive2hive.core.model.versioned;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaChunk;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MetaFileSmallTest extends H2HJUnitTest {

	private static KeyPair keys;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = MetaFileSmallTest.class;
		beforeClass();

		keys = generateRSAKeyPair(H2HConstants.KEYLENGTH_META_FILE);
	}

	@Test
	public void testGetNewestVersion() {
		List<FileVersion> versions = new ArrayList<FileVersion>();
		versions.add(new FileVersion(0, 123, System.currentTimeMillis(), new ArrayList<MetaChunk>()));
		versions.add(new FileVersion(1, 123, System.currentTimeMillis(), new ArrayList<MetaChunk>()));
		// timestamp is older
		versions.add(new FileVersion(2, 123, System.currentTimeMillis() - 1000 * 60, new ArrayList<MetaChunk>()));

		MetaFileSmall metaFileSmall = new MetaFileSmall(keys.getPublic(), versions, keys);

		FileVersion newestVersion = metaFileSmall.getNewestVersion();
		Assert.assertEquals(2, newestVersion.getIndex());
	}

	@Test
	public void testGetVersionByIndex() {
		FileVersion v0 = new FileVersion(0, 1213, 100, new ArrayList<MetaChunk>());
		FileVersion v1 = new FileVersion(1, 312, 1000, new ArrayList<MetaChunk>());
		// timestamp is older
		FileVersion v2 = new FileVersion(2, 213, 999, new ArrayList<MetaChunk>());

		List<FileVersion> versions = new ArrayList<FileVersion>();
		versions.add(v0);
		versions.add(v1);
		versions.add(v2);

		MetaFileSmall metaFileSmall = new MetaFileSmall(keys.getPublic(), versions, keys);

		Assert.assertEquals(v0, metaFileSmall.getVersionByIndex(0));
		Assert.assertEquals(v1, metaFileSmall.getVersionByIndex(1));
		Assert.assertEquals(v2, metaFileSmall.getVersionByIndex(2));
	}

	@Test
	public void testGetTotalSize() {
		List<FileVersion> versions = new ArrayList<FileVersion>();
		versions.add(new FileVersion(0, 4, 0, new ArrayList<MetaChunk>()));
		versions.add(new FileVersion(1, 10, 1, new ArrayList<MetaChunk>()));
		versions.add(new FileVersion(2, 1000, 2, new ArrayList<MetaChunk>()));

		MetaFileSmall metaFileSmall = new MetaFileSmall(keys.getPublic(), versions, keys);
		Assert.assertEquals(4 + 10 + 1000, metaFileSmall.getTotalSize().intValue());
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}
}
