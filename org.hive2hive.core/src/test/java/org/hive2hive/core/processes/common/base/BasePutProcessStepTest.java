package org.hive2hive.core.processes.common.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.network.H2HStorageMemory;
import org.hive2hive.core.network.H2HStorageMemory.StorageMemoryPutMode;
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
 * Tests for the {@link BasePutProcessStep} class. Checks if the process step successes when put
 * successes and if the process step fails (triggers rollback) when put fails.
 * 
 * @author Seppi
 */
public class BasePutProcessStepTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 10;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = BasePutProcessStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testPutProcessSuccess() throws ClassNotFoundException, IOException, NoPeerConnectionException {
		NetworkManager putter = NetworkTestUtil.getRandomNode(network);
		NetworkManager proxy = NetworkTestUtil.getRandomNode(network);

		String locationKey = proxy.getNodeId();
		String contentKey = randomString();
		String data = randomString();

		// initialize the process and the one and only step to test
		TestPutProcessStep putStep = new TestPutProcessStep(locationKey, contentKey, new H2HTestData(data),
				putter.getDataManager());
		TestExecutionUtil.executeProcessTillSucceded(putStep);

		assertEquals(
				data,
				((H2HTestData) proxy.getDataManager().get(
						new Parameters().setLocationKey(locationKey).setContentKey(contentKey))).getTestString());
	}

	@Test
	public void testPutProcessFailure() throws NoPeerConnectionException, InvalidProcessStateException {
		try {
			NetworkManager putter = NetworkTestUtil.getRandomNode(network);
			NetworkManager proxy = NetworkTestUtil.getRandomNode(network);

			for (int i = 0; i < networkSize; i++) {
				((H2HStorageMemory) network.get(i).getConnection().getPeer().storageLayer())
						.setPutMode(StorageMemoryPutMode.DENY_ALL);
			}
			String locationKey = proxy.getNodeId();
			String contentKey = randomString();
			String data = randomString();

			// initialize the process and the one and only step to test
			TestPutProcessStep putStep = new TestPutProcessStep(locationKey, contentKey, new H2HTestData(data),
					putter.getDataManager());
			TestExecutionUtil.executeProcessTillFailed(putStep);

			assertNull(proxy.getDataManager().get(new Parameters().setLocationKey(locationKey).setContentKey(contentKey)));
		} finally {
			for (int i = 0; i < networkSize; i++) {
				((H2HStorageMemory) network.get(i).getConnection().getPeer().storageLayer())
						.setPutMode(StorageMemoryPutMode.STANDARD);
			}
		}
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

		public TestPutProcessStep(String locationKey, String contentKey, H2HTestData data, DataManager dataManager) {
			super(dataManager);
			this.locationKey = locationKey;
			this.contentKey = contentKey;
			this.data = data;
		}

		@Override
		protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
			try {
				put(locationKey, contentKey, data, null);
			} catch (PutFailedException ex) {
				throw new ProcessExecutionException(this, ex);
			}
			return null;
		}
	}

}
