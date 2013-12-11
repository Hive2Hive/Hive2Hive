package org.hive2hive.core.test.network.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataManagerTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 10;
	private static Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = DataManagerTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testGlobalputGet() throws Exception {
		String locationKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();

		NetworkManager node = network.get(random.nextInt(networkSize));

		String data = NetworkTestUtil.randomString();
		FuturePut future = node.getDataManager().put(locationKey, contentKey, new H2HTestData(data));
		future.awaitUninterruptibly();

		FutureGet futureGet = node.getDataManager().get(locationKey, contentKey);
		futureGet.awaitUninterruptibly();

		String result = (String) ((H2HTestData) futureGet.getData().object()).getTestString();
		assertEquals(data, result);
	}

	@Test
	public void testGlobalputGetFromOtherNode() throws Exception {
		String locationKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();

		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);

		String data = NetworkTestUtil.randomString();
		FuturePut future = nodeA.getDataManager().put(locationKey, contentKey, new H2HTestData(data));
		future.awaitUninterruptibly();

		FutureGet futureGet = nodeB.getDataManager().get(locationKey, contentKey);
		futureGet.awaitUninterruptibly();

		String result = ((H2HTestData) futureGet.getData().object()).getTestString();
		assertEquals(data, result);
	}

	@Test
	public void testLocalPutLocalGet() throws Exception {
		NetworkManager node = network.get(random.nextInt(networkSize));

		String locationKey = node.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		String data = NetworkTestUtil.randomString();

		node.getDataManager().putLocal(locationKey, contentKey, new H2HTestData(data));

		String result = (String) ((H2HTestData) node.getDataManager().getLocal(locationKey, contentKey)).getTestString();
		assertEquals(data, result);
	}

	@Test
	public void testGlobalPutLocalGetFromOtherNode() throws Exception {
		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);

		String locationKey = nodeB.getNodeId();
		String contentKey = NetworkTestUtil.randomString();

		String data = NetworkTestUtil.randomString();
		FuturePut future = nodeA.getDataManager().put(locationKey, contentKey, new H2HTestData(data));
		future.awaitUninterruptibly();

		String result = (String) ((H2HTestData) nodeB.getDataManager().getLocal(locationKey, contentKey)).getTestString();
		assertEquals(data, result);
	}

	@Test
	public void testGlobalPutOneLocationKeyMultipleContentKeys() throws Exception {
		String locationKey = NetworkTestUtil.randomString();
		String contentKey1 = NetworkTestUtil.randomString();
		String contentKey2 = NetworkTestUtil.randomString();
		String contentKey3 = NetworkTestUtil.randomString();

		NetworkManager node = network.get(random.nextInt(networkSize));

		String data1 = NetworkTestUtil.randomString();
		FuturePut future1 = node.getDataManager().put(locationKey, contentKey1, new H2HTestData(data1));
		future1.awaitUninterruptibly();

		String data2 = NetworkTestUtil.randomString();
		FuturePut future2 = node.getDataManager().put(locationKey, contentKey2, new H2HTestData(data2));
		future2.awaitUninterruptibly();

		String data3 = NetworkTestUtil.randomString();
		FuturePut future3 = node.getDataManager().put(locationKey, contentKey3, new H2HTestData(data3));
		future3.awaitUninterruptibly();

		FutureGet get1 = node.getDataManager().get(locationKey, contentKey1);
		get1.awaitUninterruptibly();
		String result1 = (String) ((H2HTestData) get1.getData().object()).getTestString();
		assertEquals(data1, result1);

		FutureGet get2 = node.getDataManager().get(locationKey, contentKey2);
		get2.awaitUninterruptibly();
		String result2 = (String) ((H2HTestData) get2.getData().object()).getTestString();
		assertEquals(data2, result2);

		FutureGet get3 = node.getDataManager().get(locationKey, contentKey3);
		get3.awaitUninterruptibly();
		String result3 = (String) ((H2HTestData) get3.getData().object()).getTestString();
		assertEquals(data3, result3);
	}

	@Test
	public void testGlobalPutOneLocationKeyMultipleContentKeysGlobalGetFromOtherNodes() throws Exception {
		String locationKey = NetworkTestUtil.randomString();
		String contentKey1 = NetworkTestUtil.randomString();
		String contentKey2 = NetworkTestUtil.randomString();
		String contentKey3 = NetworkTestUtil.randomString();

		String data1 = NetworkTestUtil.randomString();
		FuturePut future1 = network.get(random.nextInt(networkSize)).getDataManager().put(locationKey, contentKey1,
				new H2HTestData(data1));
		future1.awaitUninterruptibly();

		String data2 = NetworkTestUtil.randomString();
		FuturePut future2 = network.get(random.nextInt(networkSize)).getDataManager().put(locationKey, contentKey2,
				new H2HTestData(data2));
		future2.awaitUninterruptibly();

		String data3 = NetworkTestUtil.randomString();
		FuturePut future3 = network.get(random.nextInt(networkSize)).getDataManager().put(locationKey, contentKey3,
				new H2HTestData(data3));
		future3.awaitUninterruptibly();

		FutureGet get1 = network.get(random.nextInt(networkSize)).getDataManager().get(locationKey, contentKey1);
		get1.awaitUninterruptibly();
		String result1 = (String) ((H2HTestData) get1.getData().object()).getTestString();
		assertEquals(data1, result1);

		FutureGet get2 = network.get(random.nextInt(networkSize)).getDataManager().get(locationKey, contentKey2);
		get2.awaitUninterruptibly();
		String result2 = (String) ((H2HTestData) get2.getData().object()).getTestString();
		assertEquals(data2, result2);

		FutureGet get3 = network.get(random.nextInt(networkSize)).getDataManager().get(locationKey, contentKey3);
		get3.awaitUninterruptibly();
		String result3 = (String) ((H2HTestData) get3.getData().object()).getTestString();
		assertEquals(data3, result3);
	}

	@Test
	public void testLocalPutOneLocationKeyMultipleContentKeys() throws Exception {
		NetworkManager node = network.get(random.nextInt(networkSize));

		String locationKey = node.getNodeId();
		String contentKey1 = NetworkTestUtil.randomString();
		String contentKey2 = NetworkTestUtil.randomString();
		String contentKey3 = NetworkTestUtil.randomString();

		String data1 = NetworkTestUtil.randomString();
		node.getDataManager().putLocal(locationKey, contentKey1, new H2HTestData(data1));

		String data2 = NetworkTestUtil.randomString();
		node.getDataManager().putLocal(locationKey, contentKey2, new H2HTestData(data2));

		String data3 = NetworkTestUtil.randomString();
		node.getDataManager().putLocal(locationKey, contentKey3, new H2HTestData(data3));

		String result1 = (String) ((H2HTestData) node.getDataManager().getLocal(locationKey, contentKey1)).getTestString();
		assertEquals(data1, result1);
		String result2 = (String) ((H2HTestData) node.getDataManager().getLocal(locationKey, contentKey2)).getTestString();
		assertEquals(data2, result2);
		String result3 = (String) ((H2HTestData) node.getDataManager().getLocal(locationKey, contentKey3)).getTestString();
		assertEquals(data3, result3);
	}

	@Test
	public void testGlobalPutOneLocationKeyMultipleContentKeysLocalGetFromOtherNodes() throws Exception {
		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);

		String locationKey = nodeB.getNodeId();
		String contentKey1 = NetworkTestUtil.randomString();
		String contentKey2 = NetworkTestUtil.randomString();
		String contentKey3 = NetworkTestUtil.randomString();

		String data1 = NetworkTestUtil.randomString();
		FuturePut future1 = nodeA.getDataManager().put(locationKey, contentKey1, new H2HTestData(data1));
		future1.awaitUninterruptibly();

		String data2 = NetworkTestUtil.randomString();
		FuturePut future2 = nodeA.getDataManager().put(locationKey, contentKey2, new H2HTestData(data2));
		future2.awaitUninterruptibly();
		
		String data3 = NetworkTestUtil.randomString();
		FuturePut future3 = nodeA.getDataManager().put(locationKey, contentKey3, new H2HTestData(data3));
		future3.awaitUninterruptibly();
		
		String result1 = (String) ((H2HTestData) nodeB.getDataManager().getLocal(locationKey, contentKey1)).getTestString();
		assertEquals(data1, result1);
		String result2 = (String) ((H2HTestData) nodeB.getDataManager().getLocal(locationKey, contentKey2)).getTestString();
		assertEquals(data2, result2);
		String result3 = (String) ((H2HTestData) nodeB.getDataManager().getLocal(locationKey, contentKey3)).getTestString();
		assertEquals(data3, result3);
	}

	@Test
	public void testRemovalOneContentKey() {
		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);
		String locationKey = nodeB.getNodeId();
		String contentKey = NetworkTestUtil.randomString();

		// put a content
		nodeA.getDataManager().put(locationKey, contentKey, new H2HTestData(NetworkTestUtil.randomString()))
				.awaitUninterruptibly();

		// test that it is there
		assertNotNull(nodeB.getDataManager().getLocal(locationKey, contentKey));

		// delete it
		nodeA.getDataManager().remove(locationKey, contentKey).awaitUninterruptibly();

		// check that it is gone
		assertNull(nodeB.getDataManager().getLocal(locationKey, contentKey));
	}

	@Test
	public void testRemovalMultipleContentKey() {
		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);
		String locationKey = nodeB.getNodeId();

		String contentKey1 = NetworkTestUtil.randomString();
		String contentKey2 = NetworkTestUtil.randomString();
		String contentKey3 = NetworkTestUtil.randomString();

		String testString1 = NetworkTestUtil.randomString();
		String testString2 = NetworkTestUtil.randomString();
		String testString3 = NetworkTestUtil.randomString();

		// insert them
		FuturePut put1 = nodeA.getDataManager().put(locationKey, contentKey1, new H2HTestData(testString1));
		put1.awaitUninterruptibly();

		FuturePut put2 = nodeA.getDataManager().put(locationKey, contentKey2, new H2HTestData(testString2));
		put2.awaitUninterruptibly();

		FuturePut put3 = nodeA.getDataManager().put(locationKey, contentKey3, new H2HTestData(testString3));
		put3.awaitUninterruptibly();

		// check that they are all stored
		assertNotNull(nodeB.getDataManager().getLocal(locationKey, contentKey1));
		assertNotNull(nodeB.getDataManager().getLocal(locationKey, contentKey2));
		assertNotNull(nodeB.getDataManager().getLocal(locationKey, contentKey3));

		// remove 2nd one and check that 1st and 3rd are still there
		nodeA.getDataManager().remove(locationKey, contentKey2).awaitUninterruptibly();
		assertNull(nodeB.getDataManager().getLocal(locationKey, contentKey2));
		assertNotNull(nodeB.getDataManager().getLocal(locationKey, contentKey1));
		assertNotNull(nodeB.getDataManager().getLocal(locationKey, contentKey3));

		// remove 3rd one as well and check that they are gone as well
		nodeA.getDataManager().remove(locationKey, contentKey1).awaitUninterruptibly();
		nodeA.getDataManager().remove(locationKey, contentKey3).awaitUninterruptibly();
		assertNull(nodeB.getDataManager().getLocal(locationKey, contentKey1));
		assertNull(nodeB.getDataManager().getLocal(locationKey, contentKey3));
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
