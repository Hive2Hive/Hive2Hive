package org.hive2hive.core.network.data.vdht;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.network.H2HStorageMemory;
import org.hive2hive.core.network.H2HStorageMemory.StorageMemoryGetMode;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.DataManager.H2HPutStatus;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.EncryptionUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Seppi
 */
public class VersionManagerTest extends H2HJUnitTest {

	private static ArrayList<NetworkManager> network;
	private static final int networkSize = 10;

	// can be reused
	private static final Random random = new Random();
	private static KeyPair protectionKeys = EncryptionUtil.generateRSAKeyPair();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = VersionManagerTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testPutGetMultipleModifiesSingleClient() throws Exception {
		NetworkManager node = NetworkTestUtil.getRandomNode(network);

		String locationKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();

		VersionManager<H2HTestData> versionManager = new VersionManager<H2HTestData>(node.getDataManager(), locationKey,
				contentKey);

		H2HTestData version = new H2HTestData("version0");

		for (int i = 1; i < 10; i++) {
			// put version
			versionManager.put(version, protectionKeys);
			// check get
			Assert.assertEquals(version.getTestString(), versionManager.get().getTestString());
			// update version
			version.setTestString("version" + i);
		}
	}

	@Test
	public void testPutGetMultipleModifiesMultipleClients() throws Exception {
		int conurrencyFactor = random.nextInt(2) + 2;

		String locationKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();

		ArrayList<NetworkManager> nodes = new ArrayList<NetworkManager>(conurrencyFactor);
		ArrayList<VersionManager<H2HTestData>> versionManagers = new ArrayList<VersionManager<H2HTestData>>(conurrencyFactor);
		for (int i = 0; i < conurrencyFactor; i++) {
			NetworkManager node = NetworkTestUtil.getRandomNode(network);
			nodes.add(node);
			VersionManager<H2HTestData> versionManager = new VersionManager<H2HTestData>(node.getDataManager(), locationKey,
					contentKey);
			versionManagers.add(versionManager);
		}

		H2HTestData initialVersion = new H2HTestData(NetworkTestUtil.randomString());
		ArrayList<String> versions = new ArrayList<String>();
		versions.add(initialVersion.getTestString());

		// put an initial version
		versionManagers.get(0).put(initialVersion, protectionKeys);

		for (int i = 1; i < 10; i++) {
			int puttingClient = random.nextInt(conurrencyFactor);
			// get latest version
			H2HTestData version = versionManagers.get(puttingClient).get();
			// check get
			Assert.assertEquals(versions.get(i - 1), version.getTestString());
			// update version
			version.setTestString(NetworkTestUtil.randomString());
			versions.add(version.getTestString());
			// put version
			versionManagers.get(puttingClient).put(version, protectionKeys);
		}
	}

	@Test(expected = PutFailedException.class)
	public void testPutVersionFork() throws Exception {
		NetworkManager node = NetworkTestUtil.getRandomNode(network);

		String locationKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();

		VersionManager<H2HTestData> versionManager = new VersionManager<H2HTestData>(node.getDataManager(), locationKey,
				contentKey);

		H2HTestData versionA = new H2HTestData(NetworkTestUtil.randomString());
		H2HTestData versionB = new H2HTestData(NetworkTestUtil.randomString());

		versionManager.put(versionA, protectionKeys);
		versionManager.put(versionB, protectionKeys);
	}

	@Test(expected = GetFailedException.class)
	public void testGetNoData() throws Exception {
		NetworkManager node = NetworkTestUtil.getRandomNode(network);

		String locationKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();

		VersionManager<H2HTestData> versionManager = new VersionManager<H2HTestData>(node.getDataManager(), locationKey,
				contentKey);

		versionManager.get();
	}

	@Test
	public void testGetDelay() throws Exception {
		NetworkManager node = NetworkTestUtil.getRandomNode(network);

		String locationKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();

		VersionManager<H2HTestData> versionManager = new VersionManager<H2HTestData>(node.getDataManager(), locationKey,
				contentKey);

		H2HTestData version0 = new H2HTestData("version0");
		versionManager.put(version0, protectionKeys);
		H2HTestData version1 = new H2HTestData("version1");
		version1.setVersionKey(version0.getVersionKey());
		versionManager.put(version1, protectionKeys);

		// remove version1, but version manager has in cache version1
		node.getDataManager().removeVersion(
				new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
						.setVersionKey(version1.getVersionKey()).setProtectionKeys(protectionKeys));

		// after some retries version manager returns cached version1
		Assert.assertEquals(version1.getTestString(), versionManager.get().getTestString());
	}

	@Test(expected = GetFailedException.class)
	public void testGetVersionFork() throws Exception {
		try {
			H2HTestData version0A = new H2HTestData("version0A");
			version0A.generateVersionKey();
			H2HTestData version0B = new H2HTestData("version0B");
			version0B.generateVersionKey();

			String locationKey = NetworkTestUtil.randomString();
			String contentKey = NetworkTestUtil.randomString();

			Map<Number640, Data> manipulatedMap = new HashMap<Number640, Data>(2);
			manipulatedMap.put(
					new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160.createHash(contentKey),
							version0A.getVersionKey()), new Data(version0A).addBasedOn(Number160.ZERO));
			manipulatedMap.put(
					new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160.createHash(contentKey),
							version0B.getVersionKey()), new Data(version0B).addBasedOn(Number160.ZERO));

			for (int i = 0; i < networkSize; i++) {
				H2HStorageMemory storage = (H2HStorageMemory) network.get(i).getConnection().getPeerDHT().storageLayer();
				storage.setGetMode(StorageMemoryGetMode.MANIPULATED);
				storage.setManipulatedMap(manipulatedMap);
			}

			VersionManager<H2HTestData> versionManager = new VersionManager<H2HTestData>(NetworkTestUtil.getRandomNode(
					network).getDataManager(), locationKey, contentKey);

			// should trigger a get failed exception (version fork)
			versionManager.get();
		} finally {
			for (int i = 0; i < networkSize; i++) {
				H2HStorageMemory storage = (H2HStorageMemory) network.get(i).getConnection().getPeerDHT().storageLayer();
				storage.setGetMode(StorageMemoryGetMode.STANDARD);
				storage.setManipulatedMap(null);
			}
		}
	}

	@Test(expected = PutFailedException.class)
	public void testContentProtectionPutWithoutProtectionKey() throws Exception {
		NetworkManager node = NetworkTestUtil.getRandomNode(network);

		String locationKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();

		VersionManager<H2HTestData> versionManager = new VersionManager<H2HTestData>(node.getDataManager(), locationKey,
				contentKey);

		H2HTestData version0 = new H2HTestData("version0");
		versionManager.put(version0, protectionKeys);
		H2HTestData version1 = new H2HTestData("version1");
		version1.setVersionKey(version0.getVersionKey());
		versionManager.put(version1, null);
	}

	@Test(expected = PutFailedException.class)
	public void testContentProtectionPutWithWrongProtectionKey() throws Exception {
		NetworkManager node = NetworkTestUtil.getRandomNode(network);

		String locationKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();

		VersionManager<H2HTestData> versionManager = new VersionManager<H2HTestData>(node.getDataManager(), locationKey,
				contentKey);

		KeyPair otherProtectionKeys = EncryptionUtil.generateRSAKeyPair();

		H2HTestData version0 = new H2HTestData("version0");
		versionManager.put(version0, protectionKeys);
		H2HTestData version1 = new H2HTestData("version1");
		version1.setVersionKey(version0.getVersionKey());
		versionManager.put(version1, otherProtectionKeys);
	}

	@Test
	public void testContentProtectionPutManual() throws Exception {
		NetworkManager node = NetworkTestUtil.getRandomNode(network);

		String locationKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();

		VersionManager<H2HTestData> versionManager = new VersionManager<H2HTestData>(node.getDataManager(), locationKey,
				contentKey);

		KeyPair otherProtectionKeys = EncryptionUtil.generateRSAKeyPair();

		H2HTestData version0 = new H2HTestData("version0");
		versionManager.put(version0, protectionKeys);
		H2HTestData version1 = new H2HTestData("version1");
		version1.setVersionKey(version0.getVersionKey());
		versionManager.put(version1, protectionKeys);
		H2HTestData version2 = new H2HTestData("version2");
		version2.setBasedOnKey(version1.getBasedOnKey());
		version2.setVersionKey(version1.getVersionKey());

		Parameters parameters = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setVersionKey(version1.getVersionKey());

		// try to overwrite version1 without protection keys
		parameters.setNetworkContent(new H2HTestData("overwriteWithoutProtectionKey"));
		Assert.assertEquals(H2HPutStatus.FAILED, node.getDataManager().put(parameters));
		// verify that nothing changed
		Assert.assertEquals(version1.getTestString(),
				((H2HTestData) node.getDataManager().getVersion(parameters)).getTestString());

		// try to overwrite version1 with wrong protection keys
		parameters.setProtectionKeys(otherProtectionKeys).setNetworkContent(
				new H2HTestData("overwriteWithOtherProtectionKey"));
		Assert.assertEquals(H2HPutStatus.FAILED, node.getDataManager().put(parameters));
		// verify that nothing changed
		Assert.assertEquals(version1.getTestString(),
				((H2HTestData) node.getDataManager().getVersion(parameters)).getTestString());

		// try to overwrite with correct protection keys
		parameters.setProtectionKeys(protectionKeys)
				.setNetworkContent(new H2HTestData("overwriteWithCorrectProtectionKeys"));
		// should not return a failure but a version fork
		Assert.assertEquals(H2HPutStatus.VERSION_FORK, node.getDataManager().put(parameters));
		// special case: pessimistic put manager removes after fork all versions under given version key (new
		// and old one)
		Assert.assertNull(node.getDataManager().getVersion(parameters));
	}

	@Test
	public void testContentProtectionRemove() throws Exception {
		NetworkManager node = NetworkTestUtil.getRandomNode(network);

		String locationKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();

		VersionManager<H2HTestData> versionManager = new VersionManager<H2HTestData>(node.getDataManager(), locationKey,
				contentKey);

		KeyPair otherProtectionKeys = EncryptionUtil.generateRSAKeyPair();

		H2HTestData version0 = new H2HTestData("version0");
		versionManager.put(version0, protectionKeys);
		H2HTestData version1 = new H2HTestData("version1");
		version1.setVersionKey(version0.getVersionKey());
		versionManager.put(version1, protectionKeys);

		Parameters parameters0 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setVersionKey(version0.getVersionKey());
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setVersionKey(version1.getVersionKey());

		// try to remove version0 and version1 without protection keys
		Assert.assertEquals(false,
				node.getDataManager().remove(new Parameters().setLocationKey(locationKey).setContentKey(contentKey)));
		// verify that nothing changed
		Assert.assertEquals(version0.getTestString(),
				((H2HTestData) node.getDataManager().getVersion(parameters0)).getTestString());
		Assert.assertEquals(version1.getTestString(),
				((H2HTestData) node.getDataManager().getVersion(parameters1)).getTestString());

		// try to remove version0 and version1 with wrong protection keys
		Assert.assertEquals(
				false,
				node.getDataManager().remove(
						new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
								.setProtectionKeys(otherProtectionKeys)));
		// verify that nothing changed
		Assert.assertEquals(version0.getTestString(),
				((H2HTestData) node.getDataManager().getVersion(parameters0)).getTestString());
		Assert.assertEquals(version1.getTestString(),
				((H2HTestData) node.getDataManager().getVersion(parameters1)).getTestString());

		// remove version0 and version1 with correct protection keys
		Assert.assertEquals(
				true,
				node.getDataManager().remove(
						new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
								.setProtectionKeys(protectionKeys)));
		// verify successful remove
		Assert.assertNull(node.getDataManager().getVersion(parameters0));
		Assert.assertNull(node.getDataManager().getVersion(parameters1));
	}

	@Test
	public void testContentProtectionRemoveVersion() throws Exception {
		NetworkManager node = NetworkTestUtil.getRandomNode(network);

		String locationKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();

		VersionManager<H2HTestData> versionManager = new VersionManager<H2HTestData>(node.getDataManager(), locationKey,
				contentKey);

		KeyPair otherProtectionKeys = EncryptionUtil.generateRSAKeyPair();

		H2HTestData version0 = new H2HTestData("version0");
		versionManager.put(version0, protectionKeys);
		H2HTestData version1 = new H2HTestData("version1");
		version1.setVersionKey(version0.getVersionKey());
		versionManager.put(version1, protectionKeys);

		Parameters parameters = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setVersionKey(version1.getVersionKey());

		// try to remove version1 without protection keys
		Assert.assertEquals(false, node.getDataManager().removeVersion(parameters));
		// verify that nothing changed
		Assert.assertEquals(version1.getTestString(),
				((H2HTestData) node.getDataManager().getVersion(parameters)).getTestString());

		// try to remove version1 with wrong protection keys
		Assert.assertEquals(false, node.getDataManager().removeVersion(parameters.setProtectionKeys(otherProtectionKeys)));
		// verify that nothing changed
		Assert.assertEquals(version1.getTestString(),
				((H2HTestData) node.getDataManager().getVersion(parameters)).getTestString());

		// remove version1 with correct protection keys
		Assert.assertEquals(true, node.getDataManager().removeVersion(parameters.setProtectionKeys(protectionKeys)));
		// verify successful remove
		Assert.assertNull(node.getDataManager().getVersion(parameters));
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
