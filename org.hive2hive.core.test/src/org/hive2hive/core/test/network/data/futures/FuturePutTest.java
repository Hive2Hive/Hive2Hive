package org.hive2hive.core.test.network.data.futures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.network.H2HStorageMemory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.futures.FuturePutListener;
import org.hive2hive.core.network.data.listener.IPutListener;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
	public void testPut() throws ClassNotFoundException, IOException {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));

		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());

		TestPutListener listener = new TestPutListener();

		nodeB.getDataManager().put(locationKey, contentKey, data, listener);

		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasFailed());
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		FutureGet futureGet = nodeB.getDataManager().get(locationKey, contentKey);
		futureGet.awaitUninterruptibly();

		assertEquals(data.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());
	}

	@Test
	public void testPutMultipleVersions() throws ClassNotFoundException, IOException {
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

			TestPutListener listener = new TestPutListener();

			nodeB.getDataManager().put(locationKey, contentKey, data, listener);

			H2HWaiter waiter = new H2HWaiter(10);
			do {
				assertFalse(listener.hasFailed());
				waiter.tickASecond();
			} while (!listener.hasSucceeded());

			FutureGet futureGet = nodeB.getDataManager().get(locationKey, contentKey);
			futureGet.awaitUninterruptibly();

			assertEquals(data.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());
		}
	}

	@Test
	public void testPutMajorityFailed() {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);
		NetworkManager nodeC = network.get(2);

		nodeB.getConnection().getPeer().getPeerBean().storage(new TestPutFailureStorage());
		nodeC.getConnection().getPeer().getPeerBean().storage(new TestPutFailureStorage());

		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		H2HTestData content1 = new H2HTestData(NetworkTestUtil.randomString());

		TestPutListener listener = new TestPutListener();

		nodeB.getDataManager().put(locationKey, contentKey, content1, listener);

		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasSucceeded());
			waiter.tickASecond();
		} while (!listener.hasFailed());

		FutureGet futureGet = nodeA.getDataManager().get(locationKey, contentKey);
		futureGet.awaitUninterruptibly();

		assertNull(futureGet.getData());
	}

	@Test
	public void testPutMinorityFailed() throws ClassNotFoundException, IOException {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);

		nodeB.getConnection().getPeer().getPeerBean().storage(new TestPutFailureStorage());

		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());

		TestPutListener listener = new TestPutListener();

		nodeB.getDataManager().put(locationKey, contentKey, data, listener);

		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasFailed());
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		FutureGet futureGet = nodeB.getDataManager().get(locationKey, contentKey);
		futureGet.awaitUninterruptibly();

		assertEquals(data.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());
	}

	@Test
	public void testPutVersionConflictWin() throws ClassNotFoundException, IOException {
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

		nodeB.getDataManager().put(locationKey, contentKey, data1).awaitUninterruptibly();
		nodeB.getDataManager().put(locationKey, contentKey, data2A).awaitUninterruptibly();

		TestPutListener listener = new TestPutListener();
		nodeB.getDataManager().put(locationKey, contentKey, data2B, listener);

		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasSucceeded());
			waiter.tickASecond();
		} while (!listener.hasFailed());

		FutureGet futureGet2A = nodeB.getDataManager().get(locationKey, contentKey);
		futureGet2A.awaitUninterruptibly();

		assertEquals(data2A.getTestString(), ((H2HTestData) futureGet2A.getData().object()).getTestString());

		FutureGet futureGet2B = nodeA.getDataManager().get(locationKey, contentKey, data2B.getVersionKey());
		futureGet2B.awaitUninterruptibly();

		assertNull(futureGet2B.getData());
	}

	@Test
	public void testConcurrentModificationAnlysis() {
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

		TestFuturePutListener futurePutListener = new TestFuturePutListener(locationKey, contentKey, data2B, null, null);
		NavigableMap<Number640, Number160> dataMap = new ConcurrentSkipListMap<Number640, Number160>();
		
		// empty map
		assertTrue(futurePutListener.checkIfMyVerisonWins(dataMap));
		
		// no based on entry
		dataMap.put(dataOtherKey, dataOther.getBasedOnKey());
		assertTrue(futurePutListener.checkIfMyVerisonWins(dataMap));
		
		// contains only parent entry
		dataMap.clear();
		dataMap.put(data1Key, data1.getBasedOnKey());
		assertTrue(futurePutListener.checkIfMyVerisonWins(dataMap));
		
		// first entry is parent, second is corrupt
		dataMap.clear();
		dataMap.put(data1Key, data1.getBasedOnKey());
		dataMap.put(dataOtherKey, dataOther.getBasedOnKey());
		assertTrue(futurePutListener.checkIfMyVerisonWins(dataMap));
		
		// first entry is parent, second entry is older
		dataMap.clear();
		dataMap.put(data1Key, data1.getBasedOnKey());
		dataMap.put(data2AOlderKey, data2AOlder.getBasedOnKey());
		assertFalse(futurePutListener.checkIfMyVerisonWins(dataMap));
		
		// first entry is parent, second entry is newer
		dataMap.clear();
		dataMap.put(data1Key, data1.getBasedOnKey());
		dataMap.put(data2ANewerKey, data2ANewer.getBasedOnKey());
		assertTrue(futurePutListener.checkIfMyVerisonWins(dataMap));
		
		// first entry is parent, second entry is same
		dataMap.clear();
		dataMap.put(data1Key, data1.getBasedOnKey());
		dataMap.put(data2BKey, data2B.getBasedOnKey());
		assertTrue(futurePutListener.checkIfMyVerisonWins(dataMap));	
	}
	
	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	private class TestPutListener implements IPutListener {

		boolean successed = false;

		public boolean hasSucceeded() {
			return successed;
		}

		boolean failed = false;

		public boolean hasFailed() {
			return failed;
		}

		@Override
		public void onPutSuccess() {
			successed = true;
		}

		@Override
		public void onPutFailure() {
			failed = true;
		}

	}

	private class TestPutFailureStorage extends H2HStorageMemory {
		@Override
		public Enum<?> put(Number640 key, Data newData, PublicKey publicKey, boolean putIfAbsent,
				boolean domainProtection) {
			return H2HStorageMemory.PutStatusH2H.FAILED;
		}
	}
	
	private class TestFuturePutListener extends FuturePutListener{
		public TestFuturePutListener(String locationKey, String contentKey, NetworkContent content,
				IPutListener listener, DataManager dataManager) {
			super(locationKey, contentKey, content, listener, dataManager);
		}
		
		public boolean checkIfMyVerisonWins(NavigableMap<Number640, Number160> keyDigest) {
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
