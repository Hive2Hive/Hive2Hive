package org.hive2hive.core.processes.common.base;

import static org.junit.Assert.assertNull;

import java.util.ArrayList;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.RemoveFailedException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.util.TestExecutionUtil;
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

	private static ArrayList<NetworkManager> network;
	private static final int networkSize = 10;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = BaseRemoveProcessStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testRemoveProcessStepSuccess() throws NoPeerConnectionException {
		String locationKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();
		H2HTestData testData = new H2HTestData(NetworkTestUtil.randomString());

		// put some data to remove
		NetworkTestUtil.getRandomNode(network).getDataManager()
				.put(new Parameters().setLocationKey(locationKey).setContentKey(contentKey).setNetworkContent(testData));

		// initialize the process and the one and only step to test
		TestRemoveProcessStep removeStep = new TestRemoveProcessStep(locationKey, contentKey, NetworkTestUtil.getRandomNode(
				network).getDataManager());
		TestExecutionUtil.executeProcessTillSucceded(removeStep);

		assertNull(NetworkTestUtil.getRandomNode(network).getDataManager()
				.get(new Parameters().setLocationKey(locationKey).setContentKey(contentKey)));
	}

	@Test
	public void testRemoveProcessStepRollBack() throws NoPeerConnectionException, InvalidProcessStateException {
		String locationKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();
		H2HTestData testData = new H2HTestData(NetworkTestUtil.randomString());

		// put some data to remove
		NetworkTestUtil.getRandomNode(network).getDataManager()
				.put(new Parameters().setLocationKey(locationKey).setContentKey(contentKey).setNetworkContent(testData));

		// initialize the process and the one and only step to test
		TestRemoveProcessStepRollBack removeStep = new TestRemoveProcessStepRollBack(locationKey, contentKey, network.get(0)
				.getDataManager());

		TestExecutionUtil.executeProcessTillFailed(removeStep);
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

	/**
	 * A simple remove process step used at {@link BaseRemoveProcessStepTest}.
	 * 
	 * @author Seppi, Nico
	 */
	private class TestRemoveProcessStep extends BaseRemoveProcessStep {

		private final String locationKey;
		private final String contentKey;

		public TestRemoveProcessStep(String locationKey, String contentKey, DataManager dataManager) {
			super(dataManager);
			this.locationKey = locationKey;
			this.contentKey = contentKey;
		}

		@Override
		protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
			try {
				remove(locationKey, contentKey, null);
			} catch (RemoveFailedException e) {
				throw new ProcessExecutionException(e);
			}
		}
	}

	/**
	 * A simple remove process step which always roll backs.
	 * 
	 * @author Seppi
	 */
	private class TestRemoveProcessStepRollBack extends BaseRemoveProcessStep {

		private final String locationKey;
		private final String contentKey;

		public TestRemoveProcessStepRollBack(String locationKey, String contentKey, DataManager dataManager) {
			super(dataManager);
			this.locationKey = locationKey;
			this.contentKey = contentKey;
		}

		@Override
		protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
			try {
				remove(locationKey, contentKey, null);
			} catch (RemoveFailedException e) {
				throw new ProcessExecutionException(e);
			}
			throw new ProcessExecutionException("Rollback test.");
		}
	}
}
