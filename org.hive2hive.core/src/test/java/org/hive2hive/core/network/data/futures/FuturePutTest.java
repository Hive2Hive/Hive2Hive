package org.hive2hive.core.network.data.futures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.H2HStorageMemory;
import org.hive2hive.core.network.H2HStorageMemory.StorageMemoryMode;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Seppi, Nico
 */
public class FuturePutTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 3;

	private static Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FuturePutTest.class;
		beforeClass();
	}

	@Before
	@Override
	public void beforeMethod() {
		super.beforeMethod();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@After
	@Override
	public void afterMethod() {
		NetworkTestUtil.shutdownNetwork(network);
		super.afterMethod();
	}

	@Test
	public void testPut() throws ClassNotFoundException, IOException, NoPeerConnectionException {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		IParameters parameters = new Parameters().setLocationKey(nodeA.getNodeId())
				.setContentKey(NetworkTestUtil.randomString()).setData(data);

		boolean success = nodeB.getDataManager().put(parameters);
		Assert.assertTrue(success);
		FutureGet futureGet = nodeB.getDataManager().getUnblocked(parameters);
		futureGet.awaitUninterruptibly();

		assertEquals(data.getTestString(), ((H2HTestData) futureGet.data().object()).getTestString());
	}

	@Test
	public void testPutMultipleVersions() throws ClassNotFoundException, IOException, NoPeerConnectionException {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));

		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();

		List<H2HTestData> content = new ArrayList<H2HTestData>();
		int numberOfContent = 3;
		for (int i = 0; i < numberOfContent; i++) {
			H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
			data.generateVersionKey();
			if (i > 0) {
				data.setBasedOnKey(content.get(i - 1).getVersionKey());
			}
			content.add(data);

			IParameters parameters = new Parameters().setLocationKey(locationKey).setContentKey(contentKey).setData(data);
			boolean success = nodeB.getDataManager().put(parameters);
			Assert.assertTrue(success);
			FutureGet futureGet = nodeB.getDataManager().getUnblocked(parameters);
			futureGet.awaitUninterruptibly();

			assertEquals(data.getTestString(), ((H2HTestData) futureGet.data().object()).getTestString());
		}
	}

	@Test
	public void testPutMajorityFailed() throws NoPeerConnectionException {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);
		NetworkManager nodeC = network.get(2);

		((H2HStorageMemory) nodeB.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.DENY_ALL);
		((H2HStorageMemory) nodeC.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.DENY_ALL);

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters = new Parameters().setLocationKey(nodeA.getNodeId())
				.setContentKey(NetworkTestUtil.randomString()).setData(data);

		boolean success = nodeB.getDataManager().put(parameters);
		Assert.assertFalse(success);
		FutureGet futureGet = nodeA.getDataManager().getUnblocked(parameters);
		futureGet.awaitUninterruptibly();

		assertNull(futureGet.data());
	}

	@Test
	public void testPutMinorityFailed() throws ClassNotFoundException, IOException, NoPeerConnectionException {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);

		((H2HStorageMemory) nodeB.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.DENY_ALL);

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters = new Parameters().setLocationKey(nodeA.getNodeId())
				.setContentKey(NetworkTestUtil.randomString()).setData(data);

		boolean success = nodeB.getDataManager().put(parameters);
		Assert.assertTrue(success);
		FutureGet futureGet = nodeB.getDataManager().getUnblocked(parameters);
		futureGet.awaitUninterruptibly();

		assertEquals(data.getTestString(), ((H2HTestData) futureGet.data().object()).getTestString());
	}

	@Test
	public void testPutVersionConflictWin() throws ClassNotFoundException, IOException, NoPeerConnectionException {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);

		String locationKey = nodeA.getNodeId();
		String contentKey = "content key";
		H2HTestData data1 = new H2HTestData("data1");
		data1.generateVersionKey();
		waitATick();
		H2HTestData data2A = new H2HTestData("data2A");
		data2A.generateVersionKey();
		data2A.setBasedOnKey(data1.getVersionKey());
		waitATick();
		H2HTestData data2B = new H2HTestData("data2B");
		data2B.generateVersionKey();
		data2B.setBasedOnKey(data1.getVersionKey());

		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setVersionKey(data1.getVersionKey()).setData(data1);
		Parameters parameters2A = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setVersionKey(data2A.getVersionKey()).setData(data2A);
		Parameters parameters2B = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setVersionKey(data2B.getVersionKey()).setData(data2B);

		nodeB.getDataManager().putUnblocked(parameters1).awaitUninterruptibly();
		nodeB.getDataManager().putUnblocked(parameters2A).awaitUninterruptibly();

		boolean success = nodeB.getDataManager().put(parameters2B);
		Assert.assertFalse(success);

		FutureGet futureGet2A = nodeB.getDataManager().getUnblocked(parameters2A);
		futureGet2A.awaitUninterruptibly();
		assertEquals(data2A.getTestString(), ((H2HTestData) futureGet2A.data().object()).getTestString());

		FutureGet futureGet2B = nodeA.getDataManager().getVersionUnblocked(parameters2B);
		futureGet2B.awaitUninterruptibly();
		assertNull(futureGet2B.data());
	}

	@Test
	public void testConcurrentModificationAnlysis() throws IOException {
		String locationKey = "a location key";
		String contentKey = "content key";

		H2HTestData dataOther = new H2HTestData("dataOther");
		dataOther.generateVersionKey();
		Number640 dataOtherKey = new Number640(Number160.createHash(locationKey), Number160.ZERO,
				Number160.createHash(contentKey), dataOther.getVersionKey());

		H2HTestData data1 = new H2HTestData("data1");
		data1.generateVersionKey();
		Number640 data1Key = new Number640(Number160.createHash(locationKey), Number160.ZERO,
				Number160.createHash(contentKey), data1.getVersionKey());

		waitATick();
		H2HTestData data2AOlder = new H2HTestData("data2AOlder");
		data2AOlder.generateVersionKey();
		data2AOlder.setBasedOnKey(data1.getVersionKey());
		Number640 data2AOlderKey = new Number640(Number160.createHash(locationKey), Number160.ZERO,
				Number160.createHash(contentKey), data2AOlder.getVersionKey());

		waitATick();
		H2HTestData data2B = new H2HTestData("data2B");
		data2B.generateVersionKey();
		data2B.setBasedOnKey(data1.getVersionKey());
		Number640 data2BKey = new Number640(Number160.createHash(locationKey), Number160.ZERO,
				Number160.createHash(contentKey), data2B.getVersionKey());

		waitATick();
		H2HTestData data2ANewer = new H2HTestData("data2ANewer");
		data2ANewer.generateVersionKey();
		data2ANewer.setBasedOnKey(data1.getVersionKey());
		Number640 data2ANewerKey = new Number640(Number160.createHash(locationKey), Number160.ZERO,
				Number160.createHash(contentKey), data2ANewer.getVersionKey());

		TestFuturePutListener futurePutListener = new TestFuturePutListener(new Parameters().setLocationKey(locationKey)
				.setContentKey(contentKey).setVersionKey(data2B.getVersionKey()).setData(data2B), null);
		NavigableMap<Number640, Collection<Number160>> dataMap = new ConcurrentSkipListMap<Number640, Collection<Number160>>();

		// empty map
		assertTrue(futurePutListener.checkIfMyVerisonWins(dataMap));

		// no based on entry
		Set<Number160> set = new HashSet<Number160>(1);
		set.add(dataOther.getBasedOnKey());
		dataMap.put(dataOtherKey, set);
		assertTrue(futurePutListener.checkIfMyVerisonWins(dataMap));

		// contains only parent entry
		dataMap.clear();
		Set<Number160> setData1 = new HashSet<Number160>(1);
		setData1.add(data1.getBasedOnKey());
		dataMap.put(data1Key, setData1);
		assertTrue(futurePutListener.checkIfMyVerisonWins(dataMap));

		// first entry is parent, second is corrupt
		dataMap.clear();
		Set<Number160> set2 = new HashSet<Number160>(1);
		set2.add(dataOther.getBasedOnKey());
		dataMap.put(data1Key, setData1);
		dataMap.put(dataOtherKey, set2);
		assertTrue(futurePutListener.checkIfMyVerisonWins(dataMap));

		// first entry is parent, second entry is older
		dataMap.clear();
		set2 = new HashSet<Number160>(1);
		set2.add(data2AOlder.getBasedOnKey());
		dataMap.put(data1Key, setData1);
		dataMap.put(data2AOlderKey, set2);
		assertFalse(futurePutListener.checkIfMyVerisonWins(dataMap));

		// first entry is parent, second entry is newer
		dataMap.clear();
		set2 = new HashSet<Number160>(1);
		set2.add(data2ANewer.getBasedOnKey());
		dataMap.put(data1Key, setData1);
		dataMap.put(data2ANewerKey, set2);
		assertTrue(futurePutListener.checkIfMyVerisonWins(dataMap));

		// first entry is parent, second entry is same
		dataMap.clear();
		set2 = new HashSet<Number160>(1);
		set2.add(data2B.getBasedOnKey());
		dataMap.put(data1Key, setData1);
		dataMap.put(data2BKey, set2);
		assertTrue(futurePutListener.checkIfMyVerisonWins(dataMap));
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	private class TestFuturePutListener extends FuturePutListener {
		public TestFuturePutListener(IParameters parameters, DataManager dataManager) {
			super(parameters, dataManager);
		}

		public boolean checkIfMyVerisonWins(NavigableMap<Number640, Collection<Number160>> keyDigest) {
			return checkIfMyVerisonWins(keyDigest, null);
		}
	};

	private void waitATick() {
		synchronized (this) {
			try {
				wait(10);
			} catch (InterruptedException e) {
			}
		}
	}

}
