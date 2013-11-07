package org.hive2hive.core.test.process;

import java.io.IOException;
import java.util.List;

import net.tomp2p.futures.FutureRemove;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.BaseGetProcessStep;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetProcessStepTest extends H2HJUnitTest {

	private final static int networkSize = 2;
	private static List<NetworkManager> network;
	private NetworkContent tempContent;
	private String testContent;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = GetProcessStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Before
	public void setup() {
		testContent = NetworkTestUtil.randomString();
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

	private NetworkContent waitForFutureResult() throws InterruptedException {
		H2HWaiter w = new H2HWaiter(20);
		NetworkContent content = null;
		do {
			w.tickASecond();
			synchronized (testContent) {
				content = tempContent;
			}
		} while (content == null);
		return content;
	}

	@Test
	public void testGet() throws InterruptedException, IOException, ClassNotFoundException {
		H2HTestData data = new H2HTestData(testContent);
		NetworkManager getter = network.get(0);
		NetworkManager holder = network.get(1);

		String locationKey = holder.getNodeId();
		String contentKey = NetworkTestUtil.randomString();

		// put in the memory of 2nd peer
		holder.putLocal(locationKey, contentKey, data);

		DummyGetProcessStep step = new DummyGetProcessStep(locationKey, contentKey);
		Process process = new Process(getter) {
		};
		process.setNextStep(step);

		// check that receiver does not have any content
		Assert.assertNull(holder.getLocal(contentKey, contentKey));

		process.start();

		// now, the receiver should have the content in memory
		H2HTestData received = (H2HTestData) waitForFutureResult();
		Assert.assertEquals(testContent, (String) received.getTestString());
	}

	/**
	 * A dummy process step that puts or gets an object
	 */
	private class DummyGetProcessStep extends BaseGetProcessStep {

		public DummyGetProcessStep(String locationKey, String contentKey) {
			super(locationKey, contentKey);
		}

		@Override
		public void rollBack() {
			Assert.fail("Should not have rollbacked here");
		}

		@Override
		protected void handleRemovalResult(FutureRemove future) {
			// not expected to get a removal
			Assert.fail();
		}

		@Override
		protected void handleGetResult(NetworkContent content) {
			synchronized (testContent) {
				tempContent = content;
			}
			getProcess().setNextStep(null);
		}
	}
}
