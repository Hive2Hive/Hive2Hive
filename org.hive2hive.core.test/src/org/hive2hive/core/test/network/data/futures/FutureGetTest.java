package org.hive2hive.core.test.network.data.futures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.listener.IGetListener;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Seppi
 */
public class FutureGetTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 5;
	private static Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FutureGetTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testGetNoVersion() {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));
		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		H2HTestData content = new H2HTestData(NetworkTestUtil.randomString());

		nodeA.getDataManager()
				.put(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
						Number160.createHash(contentKey), content, null).awaitUninterruptibly();

		TestGetListener listener = new TestGetListener();

		nodeB.getDataManager().get(locationKey, contentKey, listener);

		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.executed());

		assertEquals(content.getTestString(), listener.getData().getTestString());
	}

	@Test
	public void testGetNoVersionNoData() {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));

		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();

		FutureGet futureGet = nodeA.getDataManager().get(Number160.createHash(locationKey),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(contentKey));
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());

		TestGetListener listener = new TestGetListener();

		nodeB.getDataManager().get(locationKey, contentKey, listener);

		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.executed());

		assertNull(listener.getData());
	}

	@Test
	public void testGetNewestVersion() throws ClassNotFoundException, IOException {
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

			nodeA.getDataManager()
					.put(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
							Number160.createHash(contentKey), data, null).awaitUninterruptibly();
		}

		TestGetListener listener = new TestGetListener();

		nodeB.getDataManager().get(locationKey, contentKey, listener);

		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.executed());

		assertNotNull(listener.getData());
		assertEquals(content.get(numberOfContent - 1).getTestString(), listener.getData().getTestString());
	}

	@Test
	public void testGetAVersion() {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));
		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		H2HTestData content = new H2HTestData(NetworkTestUtil.randomString());
		content.generateVersionKey();

		nodeA.getDataManager()
				.put(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
						Number160.createHash(contentKey), content, null).awaitUninterruptibly();

		TestGetListener listener = new TestGetListener();

		nodeB.getDataManager().get(locationKey, contentKey, content.getVersionKey(), listener);

		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.executed());

		assertEquals(content.getTestString(), listener.getData().getTestString());
	}

	@Test
	public void testGetAVersionNoData() {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));

		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		Number160 versionKey = Number160.createHash(NetworkTestUtil.randomString());

		FutureGet futureGet = nodeA.getDataManager().get(Number160.createHash(locationKey),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(contentKey));
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());

		TestGetListener listener = new TestGetListener();

		nodeB.getDataManager().get(locationKey, contentKey, versionKey, listener);

		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.executed());

		assertNull(listener.getData());
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

	private class TestGetListener implements IGetListener {
		private boolean called = false;

		public boolean executed() {
			return called;
		}

		private H2HTestData data = null;

		public H2HTestData getData() {
			return data;
		}

		@Override
		public void handleGetResult(NetworkContent content) {
			called = true;
			data = (H2HTestData) content;
		}
	}
}
