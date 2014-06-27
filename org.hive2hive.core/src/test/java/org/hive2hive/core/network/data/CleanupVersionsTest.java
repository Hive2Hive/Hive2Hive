package org.hive2hive.core.network.data;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureDigest;
import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.core.security.SerializationUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Seppi
 */
public class CleanupVersionsTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 5;
	private static Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = CleanupVersionsTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testCleanUpOutdatedVersion() throws InterruptedException, IOException, NoPeerConnectionException {
		NetworkManager node = network.get(random.nextInt(networkSize));

		Parameters parameters = new Parameters().setLocationKey(NetworkTestUtil.randomString())
				.setDomainKey(NetworkTestUtil.randomString()).setContentKey(NetworkTestUtil.randomString())
				.setProtectionKeys(EncryptionUtil.generateRSAKeyPair());

		int numVersions = H2HConstants.MAX_VERSIONS_HISTORY + random.nextInt(5) + 1;
		List<H2HTestData> versions = new ArrayList<H2HTestData>();
		List<H2HTestData> newerVersions = new ArrayList<H2HTestData>();

		H2HTestData last = null;
		for (int i = 0; i < numVersions; i++) {
			long timeStamp = new Date().getTime();
			timeStamp += 2 * H2HConstants.MIN_VERSION_AGE_BEFORE_REMOVAL_MS;
			H2HTestData testData = generateTestData(timeStamp);
			if (last != null)
				testData.setBasedOnKey(last.getVersionKey());
			last = testData;
			versions.add(testData);
			if (i >= numVersions - H2HConstants.MAX_VERSIONS_HISTORY - 1)
				newerVersions.add(testData);
			synchronized (this) {
				Thread.sleep(10);
			}
		}

		long timeDiff = versions.get(numVersions - 1).getVersionKey().timestamp()
				- versions.get(0).getVersionKey().timestamp();
		if (timeDiff < H2HConstants.MIN_VERSION_AGE_BEFORE_REMOVAL_MS)
			Assert.fail("H2H constant is too low to generate appropriate time stamps.");

		for (H2HTestData testData : versions) {
			parameters.setVersionKey(testData.getVersionKey()).setData(testData);
			node.getDataManager().putUnblocked(parameters).awaitUninterruptibly();
		}

		FutureDigest futureDigest = node.getDataManager().getDigestUnblocked(parameters);
		futureDigest.awaitUninterruptibly();

		assertEquals(H2HConstants.MAX_VERSIONS_HISTORY, futureDigest.getDigest().keyDigest().size());
		int i = 0;
		for (Number160 storedVersion : futureDigest.getDigest().keyDigest().values()) {
			assertEquals(newerVersions.get(i++).getVersionKey(), storedVersion);
		}
	}

	private H2HTestData generateTestData(long timeStamp) throws IOException {
		H2HTestData testData = new H2HTestData(NetworkTestUtil.randomString());
		// get a MD5 hash of the test data object itself
		byte[] hash = HashUtil.hash(SerializationUtil.serialize(testData));
		// use time stamp value and the first part of the MD5 hash as version key
		Number160 versionKey = new Number160(timeStamp, new Number160(Arrays.copyOf(hash, Number160.BYTE_ARRAY_SIZE)));
		// assign the version key to the test data
		testData.setVersionKey(versionKey);
		return testData;
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

}
