package org.hive2hive.core.test.flowcontrol;

import java.security.PublicKey;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.StorageMemory;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.PutProcessStep;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PutProcessStepTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 3;
	private static Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = PutProcessStepTest.class;
		beforeClass();
	}

	@Override
	@Before
	public void beforeMethod() {
		super.beforeMethod();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testPutVersionConflict() {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);
		NetworkManager nodeC = network.get(2);

		// node B and C will deny the put request
		nodeB.getConnection().getPeer().getPeerBean().storage(new DenyingTestStorage());
		nodeC.getConnection().getPeer().getPeerBean().storage(new DenyingTestStorage());

		String locationKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();
		String data = NetworkTestUtil.randomString();

		PutProcessTestStep putStep = new PutProcessTestStep(new H2HTestData(data), null, locationKey,
				contentKey);
		putStep.setProcess(new Process(nodeA) {
		});
		putStep.start();

		// TODO check if put rollback was correct
	}

	@Override
	@After
	public void afterMethod() {
		NetworkTestUtil.shutdownNetwork(network);
		super.afterMethod();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	private class PutProcessTestStep extends PutProcessStep {

		private final NetworkContent newData;
		private final String locationKey;
		private final String contentKey;

		protected PutProcessTestStep(NetworkContent newData, NetworkContent oldData, String locationKey,
				String contentKey) {
			super(oldData);
			this.newData = newData;
			this.locationKey = locationKey;
			this.contentKey = contentKey;
		}

		@Override
		public void start() {
			put(locationKey, contentKey, newData);
		}

		@Override
		public void rollBack() {
		}

		@Override
		protected void handleMessageReply(ResponseMessage asyncReturnMessage) {
		}

		@Override
		protected void handlePutResult(FuturePut future) {
		}

		@Override
		protected void handleGetResult(FutureGet future) {
		}

		@Override
		protected void handleRemovalResult(FutureRemove future) {
		}

	}

	private class DenyingTestStorage extends StorageMemory {
		@Override
		public PutStatus put(Number160 locationKey, Number160 domainKey, Number160 contentKey, Data newData,
				PublicKey publicKey, boolean putIfAbsent, boolean domainProtection) {
			// doesn't accept any data
			return PutStatus.FAILED;
		}
	}
}
