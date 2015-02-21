package org.hive2hive.core.model.versioned;

import static org.junit.Assert.assertEquals;
import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.model.UserCredentialsTest;
import org.junit.BeforeClass;
import org.junit.Test;

public class BaseVersionedNetworkContentTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = UserCredentialsTest.class;
		beforeClass();
	}

	@Test
	public void testGenerateVersions() {
		TestVersionedContent v0 = new TestVersionedContent();
		assertEquals(Number160.ZERO, v0.getVersionKey());

		TestVersionedContent v1 = new TestVersionedContent();
		v1.setVersionKey(v0.getVersionKey());
		v1.generateVersionKey();
		assertEquals(-1, v0.getVersionKey().compareTo(v1.getVersionKey()));

		TestVersionedContent v2 = new TestVersionedContent();
		v2.setVersionKey(v1.getVersionKey());
		v2.generateVersionKey();
		assertEquals(-1, v1.getVersionKey().compareTo(v2.getVersionKey()));
	}

	private class TestVersionedContent extends BaseVersionedNetworkContent {

		private static final long serialVersionUID = 1L;

		@Override
		protected int getContentHash() {
			return super.hashCode();
		}

		@Override
		public int getTimeToLive() {
			return 100;
		}
	}
}
