package org.hive2hive.core.test.process.common.put;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.network.H2HStorageMemory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.put.BasePutProcessStep;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the {@link BasePutProcessStep} class. Checks if the process step successes when put
 * successes and if the process step fails (triggers rollback) when put fails.
 * 
 * @author Seppi
 */
public class BasePutProcessStepTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = BasePutProcessStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testPutProcessSuccess() throws ClassNotFoundException, IOException {
		NetworkManager putter = network.get(0);
		putter.getConnection().getPeer().getPeerBean().storage(new H2HStorageMemory());
		NetworkManager proxy = network.get(1);
		proxy.getConnection().getPeer().getPeerBean().storage(new H2HStorageMemory());

		String locationKey = proxy.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		String data = NetworkTestUtil.randomString();

		// initialize the process and the one and only step to test
		Process process = new Process(putter) {
		};
		TestPutProcessStep putStep = new TestPutProcessStep(locationKey, contentKey, new H2HTestData(data));
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

		FutureGet futureGet = proxy.getDataManager().get(Number160.createHash(locationKey),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(contentKey));
		futureGet.awaitUninterruptibly();
		assertEquals(data, ((H2HTestData) futureGet.getData().object()).getTestString());
	}

	@Test
	public void testPutProcessFailure() {
		NetworkManager putter = network.get(0);
		putter.getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());
		NetworkManager proxy = network.get(1);
		proxy.getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());

		String locationKey = proxy.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		String data = NetworkTestUtil.randomString();

		// initialize the process and the one and only step to test
		Process process = new Process(putter) {
		};
		TestPutProcessStep putStep = new TestPutProcessStep(locationKey, contentKey, new H2HTestData(data));
		process.setNextStep(putStep);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasSucceeded());
			waiter.tickASecond();
		} while (!listener.hasFailed());

		FutureGet futureGet = proxy.getDataManager().get(Number160.createHash(locationKey),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(contentKey));
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

	/**
	 * A simple put process step used at {@link BasePutProcessStepTest}.
	 * 
	 * @author Seppi
	 */
	private class TestPutProcessStep extends BasePutProcessStep {

		private final String locationKey;
		private final String contentKey;
		private final H2HTestData data;

		public TestPutProcessStep(String locationKey, String contentKey, H2HTestData data) {
			this.locationKey = locationKey;
			this.contentKey = contentKey;
			this.data = data;
		}

		@Override
		public void start() {
			try {
				put(locationKey, contentKey, data);
				getProcess().setNextStep(null);
			} catch (PutFailedException e) {
				Assert.fail();
			}
		}

	}

}
