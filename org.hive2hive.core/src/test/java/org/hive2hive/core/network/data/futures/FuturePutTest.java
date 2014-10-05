package org.hive2hive.core.network.data.futures;

import java.io.IOException;
import java.util.ArrayList;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.H2HStorageMemory;
import org.hive2hive.core.network.H2HStorageMemory.StorageMemoryMode;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.DataManager.H2HPutStatus;
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

	private static ArrayList<NetworkManager> network;
	private static final int networkSize = 6;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FuturePutTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testPut() throws ClassNotFoundException, IOException, NoPeerConnectionException {
		NetworkManager nodeA = NetworkTestUtil.getRandomNode(network);
		NetworkManager nodeB = NetworkTestUtil.getRandomNode(network);

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		IParameters parameters = new Parameters().setLocationKey(nodeA.getNodeId())
				.setContentKey(NetworkTestUtil.randomString()).setNetworkContent(data);

		Assert.assertEquals(H2HPutStatus.OK, nodeB.getDataManager().put(parameters));
		Assert.assertEquals(data.getTestString(), ((H2HTestData) nodeB.getDataManager().get(parameters)).getTestString());
	}

	@Test
	public void testPutMajorityFailed() throws NoPeerConnectionException {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);
		NetworkManager nodeC = network.get(2);
		NetworkManager nodeD = network.get(3);
		NetworkManager nodeE = network.get(4);
		NetworkManager nodeF = network.get(5);

		((H2HStorageMemory) nodeB.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.DENY_ALL);
		((H2HStorageMemory) nodeC.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.DENY_ALL);
		((H2HStorageMemory) nodeD.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.DENY_ALL);
		((H2HStorageMemory) nodeE.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.DENY_ALL);
		((H2HStorageMemory) nodeF.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.DENY_ALL);

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters = new Parameters().setLocationKey(nodeB.getNodeId())
				.setContentKey(NetworkTestUtil.randomString()).setNetworkContent(data);

		try {
			Assert.assertEquals(H2HPutStatus.FAILED, nodeA.getDataManager().put(parameters));
			Assert.assertNull(nodeA.getDataManager().get(parameters));
		} finally {
			((H2HStorageMemory) nodeB.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.STANDARD);
			((H2HStorageMemory) nodeC.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.STANDARD);
			((H2HStorageMemory) nodeD.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.STANDARD);
			((H2HStorageMemory) nodeE.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.STANDARD);
			((H2HStorageMemory) nodeF.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.STANDARD);
		}
	}

	@Test
	public void testPutMinorityFailed() throws ClassNotFoundException, IOException, NoPeerConnectionException {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);

		((H2HStorageMemory) nodeB.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.DENY_ALL);

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters = new Parameters().setLocationKey(nodeB.getNodeId())
				.setContentKey(NetworkTestUtil.randomString()).setNetworkContent(data);

		try {
			Assert.assertEquals(H2HPutStatus.OK, nodeA.getDataManager().put(parameters));
			Assert.assertEquals(data.getTestString(), ((H2HTestData) nodeA.getDataManager().get(parameters)).getTestString());
		} finally {
			((H2HStorageMemory) nodeA.getConnection().getPeerDHT().storageLayer()).setMode(StorageMemoryMode.STANDARD);
		}
	}

	@Test
	public void testPutVersionFork() throws ClassNotFoundException, IOException, NoPeerConnectionException {
		NetworkManager nodeA = network.get(0);
		NetworkManager nodeB = network.get(1);

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		data.generateVersionKey();
		Parameters parameters = new Parameters().setLocationKey(nodeB.getNodeId())
				.setContentKey(NetworkTestUtil.randomString()).setVersionKey(data.getVersionKey()).setNetworkContent(data)
				.setPrepareFlag(true);

		Assert.assertEquals(H2HPutStatus.OK, nodeA.getDataManager().put(parameters));

		H2HTestData conflictingData = new H2HTestData(NetworkTestUtil.randomString());
		conflictingData.generateVersionKey();
		Parameters parametersConflicting = new Parameters().setLocationKey(nodeB.getNodeId())
				.setContentKey(parameters.getContentKey()).setVersionKey(conflictingData.getVersionKey())
				.setNetworkContent(conflictingData).setPrepareFlag(true);

		Assert.assertEquals(H2HPutStatus.VERSION_FORK, nodeA.getDataManager().put(parametersConflicting));
		Assert.assertEquals(data.getTestString(), ((H2HTestData) nodeA.getDataManager().get(parameters)).getTestString());
		Assert.assertNull(nodeA.getDataManager().getVersion(parametersConflicting));
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

}
