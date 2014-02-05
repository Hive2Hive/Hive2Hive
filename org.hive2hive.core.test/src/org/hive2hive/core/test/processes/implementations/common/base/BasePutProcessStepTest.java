package org.hive2hive.core.test.processes.implementations.common.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.network.H2HStorageMemory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.common.base.BasePutProcessStep;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.common.put.DenyingPutTestStorage;
import org.hive2hive.core.test.processes.util.TestProcessComponentListener;
import org.hive2hive.core.test.processes.util.UseCaseTestUtil;
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
	private static final int networkSize = 2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = BasePutProcessStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testPutProcessSuccess() throws ClassNotFoundException, IOException, NoPeerConnectionException {
		NetworkManager putter = network.get(0);
		putter.getConnection().getPeer().getPeerBean().storage(new H2HStorageMemory());
		NetworkManager proxy = network.get(1);
		proxy.getConnection().getPeer().getPeerBean().storage(new H2HStorageMemory());

		String locationKey = proxy.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		String data = NetworkTestUtil.randomString();

		// initialize the process and the one and only step to test
		TestPutProcessStep putStep = new TestPutProcessStep(locationKey, contentKey, new H2HTestData(data),
				putter.getDataManager());
		UseCaseTestUtil.executeProcess(putStep);

		FutureGet futureGet = proxy.getDataManager().get(Number160.createHash(locationKey),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(contentKey));
		futureGet.awaitUninterruptibly();
		assertEquals(data, ((H2HTestData) futureGet.getData().object()).getTestString());
	}

	@Test
	public void testPutProcessFailure() throws NoPeerConnectionException, InvalidProcessStateException {
		NetworkManager putter = network.get(0);
		putter.getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());
		NetworkManager proxy = network.get(1);
		proxy.getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());

		String locationKey = proxy.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		String data = NetworkTestUtil.randomString();

		// initialize the process and the one and only step to test
		TestPutProcessStep putStep = new TestPutProcessStep(locationKey, contentKey, new H2HTestData(data),
				putter.getDataManager());
		TestProcessComponentListener listener = new TestProcessComponentListener();
		putStep.attachListener(listener);
		putStep.start();

		// wait for the process to finish
		UseCaseTestUtil.waitTillFailed(listener, 10);

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

		public TestPutProcessStep(String locationKey, String contentKey, H2HTestData data,
				IDataManager dataManager) {
			super(dataManager);
			this.locationKey = locationKey;
			this.contentKey = contentKey;
			this.data = data;
		}

		@Override
		protected void doExecute() throws InvalidProcessStateException {
			try {
				put(locationKey, contentKey, data, null);
			} catch (PutFailedException e) {
				cancel(new RollbackReason(this, e.getMessage()));
			}
		}
	}

}
