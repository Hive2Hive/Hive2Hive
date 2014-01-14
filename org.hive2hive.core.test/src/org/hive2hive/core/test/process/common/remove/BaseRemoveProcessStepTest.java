package org.hive2hive.core.test.process.common.remove;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.List;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.rpc.DigestInfo;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.H2HStorageMemory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.remove.BaseRemoveProcessStep;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the {@link BaseRemoveProcessStep} class. Checks if the process step successes when removes
 * successes and if the process step fails (triggers rollback) when removing fails.
 * 
 * @author Seppi
 */
public class BaseRemoveProcessStepTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = BaseRemoveProcessStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testRemoveProcessStepSuccess() {
		String locationKey = network.get(0).getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		H2HTestData testData = new H2HTestData(NetworkTestUtil.randomString());

		// put some data to remove
		network.get(0)
				.getDataManager()
				.put(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
						Number160.createHash(contentKey), testData, null).awaitUninterruptibly();

		// initialize the process and the one and only step to test
		Process process = new Process(network.get(0)) {
		};
		TestRemoveProcessStep putStep = new TestRemoveProcessStep(locationKey, contentKey, testData);
		process.setNextStep(putStep);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasFailed());
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		FutureGet futureGet = network
				.get(0)
				.getDataManager()
				.get(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
						Number160.createHash(contentKey));
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
	}

	@Test
	public void testRemoveProcessStepRollBack() {
		String locationKey = network.get(0).getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		Number640 key = new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160.createHash(contentKey), Number160.ZERO);
		H2HTestData testData = new H2HTestData(NetworkTestUtil.randomString());

		// manipulate the nodes, remove will not work
		network.get(0).getConnection().getPeer().getPeerBean().storage(new FakeGetTestStorage(key));
		network.get(1).getConnection().getPeer().getPeerBean().storage(new FakeGetTestStorage(key));
		// put some data to remove
		network.get(0)
				.getDataManager()
				.put(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
						Number160.createHash(contentKey), testData, null).awaitUninterruptibly();

		// initialize the process and the one and only step to test
		Process process = new Process(network.get(0)) {
		};
		TestRemoveProcessStep removeStep = new TestRemoveProcessStep(locationKey, contentKey, testData);
		process.setNextStep(removeStep);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasSucceeded());
			waiter.tickASecond();
		} while (!listener.hasFailed());
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

	private class FakeGetTestStorage extends H2HStorageMemory {

		private final Number640 key;

		public FakeGetTestStorage(Number640 key) {
			super();
			this.key = key;
		}

		@Override
		public DigestInfo digest(Number640 from, Number640 to, int limit, boolean ascending) {
			DigestInfo digestInfo = new DigestInfo();
			digestInfo.put(key, Number160.ZERO);
			return digestInfo;
		}
		
	}

	/**
	 * A simple remove process step used at {@link BaseRemoveProcessStepTest}.
	 * 
	 * @author Seppi
	 */
	private class TestRemoveProcessStep extends BaseRemoveProcessStep {

		private final String locationKey;
		private final String contentKey;
		private final H2HTestData data;

		public TestRemoveProcessStep(String locationKey, String contentKey, H2HTestData data) {
			super(null);
			this.locationKey = locationKey;
			this.contentKey = contentKey;
			this.data = data;
		}

		@Override
		public void start() {
			remove(locationKey, contentKey, data);
		}

	}
}
