package org.hive2hive.core.test.process.common.get;

import static org.junit.Assert.assertFalse;

import java.util.List;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.BaseGetProcessStep;
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
 * Tests for the {@link BaseGetProcessStep} class. Checks the methods to properly trigger success or rollback.
 * 
 * @author Seppi
 */
public class BaseGetProcessStepTest extends H2HJUnitTest {

	private final static int networkSize = 2;
	private static List<NetworkManager> network;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = BaseGetProcessStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testGetProcessStepSuccess() {
		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		NetworkManager getter = network.get(0);
		NetworkManager holder = network.get(1);

		String locationKey = holder.getNodeId();
		String contentKey = NetworkTestUtil.randomString();

		// put in the memory of 2nd peer
		holder.getDataManager().putLocal(locationKey, contentKey, data);

		TestGetProcessStep getStep = new TestGetProcessStep(locationKey, contentKey);
		Process process = new Process(getter) {
		};
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.setNextStep(getStep);

		// check that receiver does not have any content
		Assert.assertNull(holder.getDataManager().getLocal(contentKey, contentKey));

		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasFailed());
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		Assert.assertEquals(data.getTestString(), ((H2HTestData) getStep.getContent()).getTestString());
	}

	@Test
	public void testGetProcessStepRollBack() {
		NetworkManager getter = network.get(0);
		NetworkManager holder = network.get(1);

		String locationKey = holder.getNodeId();
		String contentKey = NetworkTestUtil.randomString();

		TestGetProcessStepRollBack getStepRollBack = new TestGetProcessStepRollBack(locationKey, contentKey);
		Process process = new Process(getter) {
		};
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.setNextStep(getStepRollBack);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasSucceeded());
			waiter.tickASecond();
		} while (!listener.hasFailed());

		Assert.assertNull(getStepRollBack.getContent());
	}

	/**
	 * A simple get process step which always succeeds.
	 * 
	 * @author Seppi
	 */
	private class TestGetProcessStep extends BaseGetProcessStep {

		private final String locationKey;
		private final String contentKey;

		private NetworkContent content;

		public TestGetProcessStep(String locationKey, String contentKey) {
			this.locationKey = locationKey;
			this.contentKey = contentKey;
		}

		@Override
		public void start() {
			get(locationKey, contentKey);
		}

		@Override
		public void handleGetResult(NetworkContent content) {
			this.content = content;
			getProcess().setNextStep(null);
		}

		public NetworkContent getContent() {
			return content;
		}
	}

	/**
	 * A simple get process step which always roll backs.
	 * 
	 * @author Seppi
	 */
	private class TestGetProcessStepRollBack extends BaseGetProcessStep {

		private final String locationKey;
		private final String contentKey;

		private NetworkContent content;

		public TestGetProcessStepRollBack(String locationKey, String contentKey) {
			this.locationKey = locationKey;
			this.contentKey = contentKey;
		}

		@Override
		public void start() {
			get(locationKey, contentKey);
		}

		@Override
		public void handleGetResult(NetworkContent content) {
			this.content = content;
			if (content == null)
				getProcess().stop("Content is null.");
		}

		public NetworkContent getContent() {
			return content;
		}
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

}
