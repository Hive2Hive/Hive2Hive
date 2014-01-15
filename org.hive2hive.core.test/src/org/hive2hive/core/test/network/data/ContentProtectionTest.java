package org.hive2hive.core.test.network.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.security.KeyPair;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.peers.Number160;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ContentProtectionTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 3;
	private static Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = DataManagerTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testOverwritting1() throws Exception {
		Number160 locationKey = Number160.createHash(NetworkTestUtil.randomString());
		Number160 domainKey = Number160.createHash(NetworkTestUtil.randomString());
		Number160 contentKey = Number160.createHash(NetworkTestUtil.randomString());
		KeyPair protectionKey = EncryptionUtil.generateProtectionKey();

		NetworkManager node = network.get(random.nextInt(networkSize));

		H2HTestData data1 = new H2HTestData("bla1");

		FuturePut futurePut = node.getDataManager()
				.put(locationKey, domainKey, contentKey, data1, protectionKey);
		futurePut.awaitUninterruptibly();

		FutureGet futureGet = node.getDataManager().get(locationKey, domainKey, contentKey);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());

		H2HTestData data2 = new H2HTestData("bla2");
		
		futurePut = node.getDataManager().put(locationKey, domainKey, contentKey, data2, null);
		futurePut.awaitUninterruptibly();

		futureGet = node.getDataManager().get(locationKey, domainKey, contentKey);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());
	}
	
	@Test
	public void testOverwritting2() throws Exception {
		Number160 locationKey = Number160.createHash(NetworkTestUtil.randomString());
		Number160 domainKey = Number160.createHash(NetworkTestUtil.randomString());
		Number160 contentKey = Number160.createHash(NetworkTestUtil.randomString());
		KeyPair protectionKey = EncryptionUtil.generateProtectionKey();

		NetworkManager node = network.get(random.nextInt(networkSize));

		H2HTestData data1 = new H2HTestData("bla1");

		FuturePut futurePut = node.getDataManager()
				.put(locationKey, domainKey, contentKey, data1, null);
		futurePut.awaitUninterruptibly();

		FutureGet futureGet = node.getDataManager().get(locationKey, domainKey, contentKey);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());

		H2HTestData data2 = new H2HTestData("bla2");
		
		futurePut = node.getDataManager().put(locationKey, domainKey, contentKey, data2, protectionKey);
		futurePut.awaitUninterruptibly();

		futureGet = node.getDataManager().get(locationKey, domainKey, contentKey);
		futureGet.awaitUninterruptibly();
		assertEquals(data2.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());
		
		H2HTestData data3 = new H2HTestData("bla3");
		
		futurePut = node.getDataManager().put(locationKey, domainKey, contentKey, data3, null);
		futurePut.awaitUninterruptibly();

		futureGet = node.getDataManager().get(locationKey, domainKey, contentKey);
		futureGet.awaitUninterruptibly();
		assertEquals(data2.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());
	}
	
	@Test
	public void testOverwritting3() throws Exception {
		Number160 locationKey = Number160.createHash(NetworkTestUtil.randomString());
		Number160 domainKey = Number160.createHash(NetworkTestUtil.randomString());
		Number160 contentKey = Number160.createHash(NetworkTestUtil.randomString());
		KeyPair protectionKey1 = EncryptionUtil.generateProtectionKey();
		KeyPair protectionKey2 = EncryptionUtil.generateProtectionKey();
		
		NetworkManager node = network.get(random.nextInt(networkSize));

		H2HTestData data1 = new H2HTestData("bla1");

		FuturePut futurePut = node.getDataManager()
				.put(locationKey, domainKey, contentKey, data1, protectionKey1);
		futurePut.awaitUninterruptibly();

		FutureGet futureGet = node.getDataManager().get(locationKey, domainKey, contentKey);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());

		H2HTestData data2 = new H2HTestData("bla2");
		
		futurePut = node.getDataManager().put(locationKey, domainKey, contentKey, data2, protectionKey2);
		futurePut.awaitUninterruptibly();

		futureGet = node.getDataManager().get(locationKey, domainKey, contentKey);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());
	}
	
	@Test
	public void testRemove1() throws Exception {
		Number160 locationKey = Number160.createHash(NetworkTestUtil.randomString());
		Number160 domainKey = Number160.createHash(NetworkTestUtil.randomString());
		Number160 contentKey = Number160.createHash(NetworkTestUtil.randomString());
		KeyPair protectionKey1 = EncryptionUtil.generateProtectionKey();
		KeyPair protectionKey2 = EncryptionUtil.generateProtectionKey();
		
		NetworkManager node = network.get(random.nextInt(networkSize));

		H2HTestData data1 = new H2HTestData("bla1");

		FuturePut futurePut = node.getDataManager()
				.put(locationKey, domainKey, contentKey, data1, protectionKey1);
		futurePut.awaitUninterruptibly();

		FutureGet futureGet = node.getDataManager().get(locationKey, domainKey, contentKey);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());
		
		FutureRemove futureRemove = node.getDataManager().remove(locationKey, domainKey, contentKey, null);
		futureRemove.awaitUninterruptibly();

		futureGet = node.getDataManager().get(locationKey, domainKey, contentKey);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());
		
		futureRemove = node.getDataManager().remove(locationKey, domainKey, contentKey, protectionKey2);
		futureRemove.awaitUninterruptibly();

		futureGet = node.getDataManager().get(locationKey, domainKey, contentKey);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());
		
		futureRemove = node.getDataManager().remove(locationKey, domainKey, contentKey, protectionKey1);
		futureRemove.awaitUninterruptibly();

		futureGet = node.getDataManager().get(locationKey, domainKey, contentKey);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
	}
	
	@Test
	public void testRemove2() throws Exception {
		Number160 locationKey = Number160.createHash(NetworkTestUtil.randomString());
		Number160 domainKey = Number160.createHash(NetworkTestUtil.randomString());
		Number160 contentKey = Number160.createHash(NetworkTestUtil.randomString());
		KeyPair protectionKey1 = EncryptionUtil.generateProtectionKey();
		KeyPair protectionKey2 = EncryptionUtil.generateProtectionKey();
		
		NetworkManager node = network.get(random.nextInt(networkSize));

		H2HTestData data1 = new H2HTestData("bla1");
		data1.generateVersionKey();
		Number160 versionKey = data1.getVersionKey();

		FuturePut futurePut = node.getDataManager()
				.put(locationKey, domainKey, contentKey, data1, protectionKey1);
		futurePut.awaitUninterruptibly();

		FutureGet futureGet = node.getDataManager().get(locationKey, domainKey, contentKey, versionKey);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());
		
		FutureRemove futureRemove = node.getDataManager().remove(locationKey, domainKey, contentKey, versionKey, null);
		futureRemove.awaitUninterruptibly();

		futureGet = node.getDataManager().get(locationKey, domainKey, contentKey, versionKey);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());
		
		futureRemove = node.getDataManager().remove(locationKey, domainKey, contentKey, versionKey, protectionKey2);
		futureRemove.awaitUninterruptibly();

		futureGet = node.getDataManager().get(locationKey, domainKey, contentKey, versionKey);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());
		
		futureRemove = node.getDataManager().remove(locationKey, domainKey, contentKey, versionKey, protectionKey1);
		futureRemove.awaitUninterruptibly();

		futureGet = node.getDataManager().get(locationKey, domainKey, contentKey, versionKey);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

}
