package org.hive2hive.core.test.network.data.futures;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.listener.IRemoveListener;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FutureRemoveTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 10;
	private static Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FutureRemoveTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testRemove() {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));
		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		H2HTestData content = new H2HTestData(NetworkTestUtil.randomString());

		nodeA.getDataManager().put(nodeA.getNodeId(), contentKey, content).awaitUninterruptibly();

		TestRemoveListener listener = new TestRemoveListener();

		nodeB.getDataManager().remove(locationKey, contentKey, listener);

		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasFailed());
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		assertNull(nodeA.getDataManager().getLocal(locationKey, contentKey, content.getVersionKey()));
	}

	@Test
	public void testRemoveSingleVersion() {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));
		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		H2HTestData content = new H2HTestData(NetworkTestUtil.randomString());
		content.generateVersionKey();

		nodeA.getDataManager().put(locationKey, contentKey, content).awaitUninterruptibly();

		TestRemoveListener listener = new TestRemoveListener();

		nodeB.getDataManager().removeVersion(locationKey, contentKey, content.getVersionKey(), listener);

		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasFailed());
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		assertNull(nodeA.getDataManager().getLocal(locationKey, contentKey, content.getVersionKey()));
	}

	@Test
	public void testRemoveMultipleVersions() {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));
		
		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		
		List<H2HTestData> content = new ArrayList<H2HTestData>();
		int numberOfContent = 3;
		for (int i = 0; i < numberOfContent; i++){
			H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
			data.generateVersionKey();
			if (i > 0){
				data.setBasedOnKey(content.get(i-1).getVersionKey());
			}
			nodeA.getDataManager().put(nodeA.getNodeId(), contentKey, data).awaitUninterruptibly();
			content.add(data);
		}

		TestRemoveListener listener = new TestRemoveListener();

		nodeB.getDataManager().remove(locationKey, contentKey, listener);

		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasFailed());
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		for (H2HTestData data: content){			
			assertNull(nodeA.getDataManager().getLocal(locationKey, contentKey, data.getVersionKey()));
		}
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

	private class TestRemoveListener implements IRemoveListener {

		boolean successed = false;

		public boolean hasSucceeded() {
			return successed;
		}

		boolean failed = false;

		public boolean hasFailed() {
			return failed;
		}

		@Override
		public void onRemoveSuccess() {
			successed = true;
		}

		@Override
		public void onRemoveFailure() {
			failed = true;
		}

	}

}
