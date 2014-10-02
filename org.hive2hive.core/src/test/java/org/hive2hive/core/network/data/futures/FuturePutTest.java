package org.hive2hive.core.network.data.futures;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import net.tomp2p.dht.FutureGet;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.H2HStorageMemory;
import org.hive2hive.core.network.H2HStorageMemory.StorageMemoryMode;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Seppi, Nico
 */
public class FuturePutTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 3;

	private static Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FuturePutTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testPut() throws ClassNotFoundException, IOException, NoPeerConnectionException {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		IParameters parameters = new Parameters().setLocationKey(nodeA.getNodeId())
				.setContentKey(NetworkTestUtil.randomString()).setData(data);

		boolean success = nodeB.getDataManager().put(parameters);
		Assert.assertTrue(success);

		FutureGet futureGet = nodeB.getDataManager().getUnblocked(parameters);
		futureGet.awaitUninterruptibly();
		Assert.assertEquals(data.getTestString(), ((H2HTestData) futureGet.data().object()).getTestString());
	}

	@Test
	public void testPutMajorityFailed() throws NoPeerConnectionException {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);
		NetworkManager nodeC = network.get(2);

		((H2HStorageMemory) nodeB.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.DENY_ALL);
		((H2HStorageMemory) nodeC.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.DENY_ALL);

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters = new Parameters().setLocationKey(nodeB.getNodeId())
				.setContentKey(NetworkTestUtil.randomString()).setData(data);

		try {
			Assert.assertFalse(nodeA.getDataManager().put(parameters));

			FutureGet futureGet = nodeA.getDataManager().getUnblocked(parameters);
			futureGet.awaitUninterruptibly();
			Assert.assertNull(futureGet.data());
		} finally {
			((H2HStorageMemory) nodeB.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.STANDARD);
			((H2HStorageMemory) nodeC.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.STANDARD);
		}
	}

	@Test
	public void testPutMinorityFailed() throws ClassNotFoundException, IOException, NoPeerConnectionException {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);

		((H2HStorageMemory) nodeB.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.DENY_ALL);

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters = new Parameters().setLocationKey(nodeB.getNodeId())
				.setContentKey(NetworkTestUtil.randomString()).setData(data);

		try {
			Assert.assertTrue(nodeA.getDataManager().put(parameters));

			FutureGet futureGet = nodeA.getDataManager().getUnblocked(parameters);
			futureGet.awaitUninterruptibly();
			Assert.assertEquals(data.getTestString(), ((H2HTestData) futureGet.data().object()).getTestString());
		} finally {
			((H2HStorageMemory) nodeA.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.STANDARD);
		}
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

}
