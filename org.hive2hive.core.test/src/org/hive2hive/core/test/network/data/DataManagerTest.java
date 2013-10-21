package org.hive2hive.core.test.network.data;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureDHT;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.test.H2HJUnitTest;
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
	public void testGlobalPutGlobalGet() throws Exception {
		String locationKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();

		NetworkManager node = network.get(random.nextInt(networkSize));

		String data = NetworkTestUtil.randomString();
		FutureDHT future = node.putGlobal(locationKey, contentKey, new TestDataWrapper(data));
		future.awaitUninterruptibly();

		String result = (String) ((TestDataWrapper) node.getGlobal(locationKey, contentKey)
				.awaitUninterruptibly().getData().getObject()).getContent();
		assertEquals(data, result);
	}

	@Test
	public void testGlobalPutGlobalGetFromOtherNode() throws Exception {
		String locationKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();

		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);

		String data = NetworkTestUtil.randomString();
		FutureDHT future = nodeA.putGlobal(locationKey, contentKey, new TestDataWrapper(data));
		future.awaitUninterruptibly();

		String result = (String) ((TestDataWrapper) nodeB.getGlobal(locationKey, contentKey)
				.awaitUninterruptibly().getData().getObject()).getContent();
		assertEquals(data, result);
	}

	@Test
	public void testLocalPutLocalGet() throws Exception {
		NetworkManager node = network.get(random.nextInt(networkSize));

		String locationKey = node.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		String data = NetworkTestUtil.randomString();

		node.putLocal(locationKey, contentKey, new TestDataWrapper(data));

		String result = (String) ((TestDataWrapper) node.getLocal(locationKey, contentKey)).getContent();
		assertEquals(data, result);
	}

	@Test
	public void testGlobalPutLocalGetFromOtherNode() throws Exception {
		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);

		String locationKey = nodeB.getNodeId();
		String contentKey = NetworkTestUtil.randomString();

		String data = NetworkTestUtil.randomString();
		FutureDHT future = nodeA.putGlobal(locationKey, contentKey, new TestDataWrapper(data));
		future.awaitUninterruptibly();

		String result = (String) ((TestDataWrapper) nodeB.getLocal(locationKey, contentKey)).getContent();
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
		FutureDHT future1 = node.putGlobal(locationKey, contentKey1, new TestDataWrapper(data1));
		future1.awaitUninterruptibly();
		
		String data2 = NetworkTestUtil.randomString();
		FutureDHT future2 = node.putGlobal(locationKey, contentKey2, new TestDataWrapper(data2));
		future2.awaitUninterruptibly();
		
		String data3 = NetworkTestUtil.randomString();
		FutureDHT future3 = node.putGlobal(locationKey, contentKey3, new TestDataWrapper(data3));
		future3.awaitUninterruptibly();
		
		String result1 = (String) ((TestDataWrapper) node.getGlobal(locationKey, contentKey1)
				.awaitUninterruptibly().getData().getObject()).getContent();
		assertEquals(data1, result1);
		String result2 = (String) ((TestDataWrapper) node.getGlobal(locationKey, contentKey2)
				.awaitUninterruptibly().getData().getObject()).getContent();
		assertEquals(data2, result2);
		String result3 = (String) ((TestDataWrapper) node.getGlobal(locationKey, contentKey3)
				.awaitUninterruptibly().getData().getObject()).getContent();
		assertEquals(data3, result3);
	}
	
	@Test
	public void testGlobalPutOneLocationKeyMultipleContentKeysGlobalGetFromOtherNodes() throws Exception {
		String locationKey = NetworkTestUtil.randomString();
		String contentKey1 = NetworkTestUtil.randomString();
		String contentKey2 = NetworkTestUtil.randomString();
		String contentKey3 = NetworkTestUtil.randomString();
		
		String data1 = NetworkTestUtil.randomString();
		FutureDHT future1 = network.get(random.nextInt(networkSize)).putGlobal(locationKey, contentKey1,
				new TestDataWrapper(data1));
		future1.awaitUninterruptibly();
		
		String data2 = NetworkTestUtil.randomString();
		FutureDHT future2 = network.get(random.nextInt(networkSize)).putGlobal(locationKey, contentKey2,
				new TestDataWrapper(data2));
		future2.awaitUninterruptibly();
		
		String data3 = NetworkTestUtil.randomString();
		FutureDHT future3 = network.get(random.nextInt(networkSize)).putGlobal(locationKey, contentKey3,
				new TestDataWrapper(data3));
		future3.awaitUninterruptibly();
		
		String result1 = (String) ((TestDataWrapper) network.get(random.nextInt(networkSize))
				.getGlobal(locationKey, contentKey1).awaitUninterruptibly().getData().getObject())
				.getContent();
		assertEquals(data1, result1);
		String result2 = (String) ((TestDataWrapper) network.get(random.nextInt(networkSize))
				.getGlobal(locationKey, contentKey2).awaitUninterruptibly().getData().getObject())
				.getContent();
		assertEquals(data2, result2);
		String result3 = (String) ((TestDataWrapper) network.get(random.nextInt(networkSize))
				.getGlobal(locationKey, contentKey3).awaitUninterruptibly().getData().getObject())
				.getContent();
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
		node.putLocal(locationKey, contentKey1, new TestDataWrapper(data1));
		
		String data2 = NetworkTestUtil.randomString();
		node.putLocal(locationKey, contentKey2, new TestDataWrapper(data2));
		
		String data3 = NetworkTestUtil.randomString();
		node.putLocal(locationKey, contentKey3, new TestDataWrapper(data3));
		
		String result1 = (String) ((TestDataWrapper) node.getLocal(locationKey, contentKey1)).getContent();
		assertEquals(data1, result1);
		String result2 = (String) ((TestDataWrapper) node.getLocal(locationKey, contentKey2)).getContent();
		assertEquals(data2, result2);
		String result3 = (String) ((TestDataWrapper) node.getLocal(locationKey, contentKey3)).getContent();
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
		FutureDHT future1 = nodeA.putGlobal(locationKey, contentKey1,
				new TestDataWrapper(data1));
		future1.awaitUninterruptibly();
		
		String data2 = NetworkTestUtil.randomString();
		FutureDHT future2 = nodeA.putGlobal(locationKey, contentKey2,
				new TestDataWrapper(data2));
		future2.awaitUninterruptibly();
		
		String data3 = NetworkTestUtil.randomString();
		FutureDHT future3 = nodeA.putGlobal(locationKey, contentKey3,
				new TestDataWrapper(data3));
		future3.awaitUninterruptibly();
		
		String result1 = (String) ((TestDataWrapper) nodeB.getLocal(locationKey, contentKey1)).getContent();
		assertEquals(data1, result1);
		String result2 = (String) ((TestDataWrapper) nodeB.getLocal(locationKey, contentKey2)).getContent();
		assertEquals(data2, result2);
		String result3 = (String) ((TestDataWrapper) nodeB.getLocal(locationKey, contentKey3)).getContent();
		assertEquals(data3, result3);
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
