package org.hive2hive.core.processes.common.base;

import static org.junit.Assert.assertNull;

import java.util.List;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.rpc.DigestInfo;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.RemoveFailedException;
import org.hive2hive.core.network.H2HStorageMemory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.common.base.BaseRemoveProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.util.TestExecutionUtil;
import org.hive2hive.processframework.util.TestProcessComponentListener;
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
	public void testRemoveProcessStepSuccess() throws NoPeerConnectionException {
		String locationKey = network.get(0).getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		H2HTestData testData = new H2HTestData(NetworkTestUtil.randomString());

		// put some data to remove
		network.get(0).getDataManager()
				.putUnblocked(new Parameters().setLocationKey(locationKey).setContentKey(contentKey).setData(testData))
				.awaitUninterruptibly();

		// initialize the process and the one and only step to test
		TestRemoveProcessStep putStep = new TestRemoveProcessStep(locationKey, contentKey, network.get(0).getDataManager());
		TestExecutionUtil.executeProcess(putStep);

		FutureGet futureGet = network.get(0).getDataManager()
				.getUnblocked(new Parameters().setLocationKey(locationKey).setContentKey(contentKey));
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
	}

	@Test
	public void testRemoveProcessStepRollBack() throws NoPeerConnectionException, InvalidProcessStateException {
		String locationKey = network.get(0).getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		Number640 key = new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160.createHash(contentKey),
				Number160.ZERO);
		H2HTestData testData = new H2HTestData(NetworkTestUtil.randomString());

		// manipulate the nodes, remove will not work
		network.get(0).getConnection().getPeer().getPeerBean().storage(new FakeGetTestStorage(key));
		network.get(1).getConnection().getPeer().getPeerBean().storage(new FakeGetTestStorage(key));
		// put some data to remove
		network.get(0).getDataManager()
				.putUnblocked(new Parameters().setLocationKey(locationKey).setContentKey(contentKey).setData(testData))
				.awaitUninterruptibly();

		// initialize the process and the one and only step to test
		TestRemoveProcessStep removeStep = new TestRemoveProcessStep(locationKey, contentKey, network.get(0)
				.getDataManager());
		TestProcessComponentListener listener = new TestProcessComponentListener();
		removeStep.attachListener(listener);
		removeStep.start();
		TestExecutionUtil.waitTillFailed(listener, 10);
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
	 * @author Seppi, Nico
	 */
	private class TestRemoveProcessStep extends BaseRemoveProcessStep {

		private final String locationKey;
		private final String contentKey;

		public TestRemoveProcessStep(String locationKey, String contentKey, IDataManager dataManager) {
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
}
