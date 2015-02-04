package org.hive2hive.core.processes.common.base;

import static org.junit.Assert.assertNull;

import java.util.List;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.RemoveFailedException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.TestExecutionUtil;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
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
	private static final int networkSize = 10;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = BaseRemoveProcessStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testRemoveProcessStepSuccess() throws NoPeerConnectionException {
		String locationKey = randomString();
		String contentKey = randomString();
		H2HTestData testData = new H2HTestData(randomString());

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
		String locationKey = randomString();
		String contentKey = randomString();
		H2HTestData testData = new H2HTestData(randomString());

		// put some data to remove
		NetworkTestUtil.getRandomNode(network).getDataManager()
				.put(new Parameters().setLocationKey(locationKey).setContentKey(contentKey).setNetworkContent(testData));

		// initialize the process and the one and only step to test
		TestRemoveProcessStepFail removeStep = new TestRemoveProcessStepFail(locationKey, contentKey, network.get(0)
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
		protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
			try {
				remove(locationKey, contentKey, null);
			} catch (RemoveFailedException ex) {
				throw new ProcessExecutionException(this, ex);
			}
			return null;
		}
	}

	/**
	 * A simple remove process step which always fails.
	 * 
	 * @author Seppi
	 */
	private class TestRemoveProcessStepFail extends BaseRemoveProcessStep {

		private final String locationKey;
		private final String contentKey;

		public TestRemoveProcessStepFail(String locationKey, String contentKey, DataManager dataManager) {
			super(dataManager);
			this.locationKey = locationKey;
			this.contentKey = contentKey;
		}

		@Override
		protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
			try {
				remove(locationKey, contentKey, null);
			} catch (RemoveFailedException ex) {
				throw new ProcessExecutionException(this, ex);
			}
			throw new ProcessExecutionException(this, "Fail test.");
		}
	}
}
