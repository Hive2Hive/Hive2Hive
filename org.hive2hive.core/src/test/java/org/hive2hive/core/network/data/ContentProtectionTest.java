package org.hive2hive.core.network.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.security.KeyPair;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.EncryptionUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A class which test several content protection scenarios for put (override) or remove calls.
 * 
 * @author Seppi
 */
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
		String locationKey = NetworkTestUtil.randomString();
		String domainKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();
		KeyPair protectionKey = EncryptionUtil.generateRSAKeyPair();

		NetworkManager node = network.get(random.nextInt(networkSize));

		// initial put
		H2HTestData data1 = new H2HTestData("bla1");
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setData(data1).setProtectionKeys(protectionKey);
		FuturePut futurePut = node.getDataManager().putUnblocked(parameters1);
		futurePut.awaitUninterruptibly();

		// verify initial put
		FutureGet futureGet = node.getDataManager().getUnblocked(parameters1);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());

		// try to put without a protection key
		H2HTestData data2 = new H2HTestData("bla2");
		Parameters parameters2 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setData(data2);
		futurePut = node.getDataManager().putUnblocked(parameters2);
		futurePut.awaitUninterruptibly();

		// should have been not modified
		futureGet = node.getDataManager().getUnblocked(parameters1);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());
	}

	@Test
	public void testOverwritting2() throws Exception {
		String locationKey = NetworkTestUtil.randomString();
		String domainKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();
		KeyPair protectionKey = EncryptionUtil.generateRSAKeyPair();

		NetworkManager node = network.get(random.nextInt(networkSize));

		// initial put
		H2HTestData data1 = new H2HTestData("bla1");
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setData(data1).setProtectionKeys(protectionKey);
		FuturePut futurePut = node.getDataManager().putUnblocked(parameters1);
		futurePut.awaitUninterruptibly();

		// verify initial put
		FutureGet futureGet = node.getDataManager().getUnblocked(parameters1);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());

		// overwrite with correct protection key
		H2HTestData data2 = new H2HTestData("bla2");
		Parameters parameters2 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setData(data2).setProtectionKeys(protectionKey);
		futurePut = node.getDataManager().putUnblocked(parameters2);
		futurePut.awaitUninterruptibly();

		// verify overwrite
		futureGet = node.getDataManager().getUnblocked(parameters2);
		futureGet.awaitUninterruptibly();
		assertEquals(data2.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());

		// try to overwrite without protection key
		H2HTestData data3 = new H2HTestData("bla3");
		Parameters parameters3 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setData(data3);
		futurePut = node.getDataManager().putUnblocked(parameters3);
		futurePut.awaitUninterruptibly();

		// should have been not changed
		futureGet = node.getDataManager().getUnblocked(parameters1);
		futureGet.awaitUninterruptibly();
		assertEquals(data2.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());
	}

	@Test
	public void testOverwritting3() throws Exception {
		String locationKey = NetworkTestUtil.randomString();
		String domainKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();
		KeyPair protectionKey1 = EncryptionUtil.generateRSAKeyPair();
		KeyPair protectionKey2 = EncryptionUtil.generateRSAKeyPair();

		NetworkManager node = network.get(random.nextInt(networkSize));

		// initial put
		H2HTestData data1 = new H2HTestData("bla1");
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setData(data1).setProtectionKeys(protectionKey1);
		FuturePut futurePut = node.getDataManager().putUnblocked(parameters1);
		futurePut.awaitUninterruptibly();

		// verify initial put
		FutureGet futureGet = node.getDataManager().getUnblocked(parameters1);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());

		// try to overwrite with wrong protection key
		H2HTestData data2 = new H2HTestData("bla2");
		Parameters parameters2 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setData(data2).setProtectionKeys(protectionKey2);
		futurePut = node.getDataManager().putUnblocked(parameters2);
		futurePut.awaitUninterruptibly();

		// should have been not changed;
		futureGet = node.getDataManager().getUnblocked(parameters1);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());
	}

	@Test
	public void testRemove1() throws Exception {
		String locationKey = NetworkTestUtil.randomString();
		String domainKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();
		KeyPair protectionKey1 = EncryptionUtil.generateRSAKeyPair();
		KeyPair protectionKey2 = EncryptionUtil.generateRSAKeyPair();

		NetworkManager node = network.get(random.nextInt(networkSize));

		// initial put
		H2HTestData data1 = new H2HTestData("bla1");
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setData(data1).setProtectionKeys(protectionKey1);
		FuturePut futurePut = node.getDataManager().putUnblocked(parameters1);
		futurePut.awaitUninterruptibly();

		// verify initial put
		FutureGet futureGet = node.getDataManager().getUnblocked(parameters1);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());

		// try to remove without protection keys
		Parameters parameters2 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey);
		FutureRemove futureRemove = node.getDataManager().removeUnblocked(parameters2);
		futureRemove.awaitUninterruptibly();

		// should have been not changed
		futureGet = node.getDataManager().getUnblocked(parameters2);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());

		// try to remove with wrong protection keys
		Parameters parameters3 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setProtectionKeys(protectionKey2);
		futureRemove = node.getDataManager().removeUnblocked(parameters3);
		futureRemove.awaitUninterruptibly();

		// should have been not changed
		futureGet = node.getDataManager().getUnblocked(parameters2);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());

		// remove with correct protection keys
		futureRemove = node.getDataManager().removeUnblocked(parameters1);
		futureRemove.awaitUninterruptibly();

		// should have been removed
		futureGet = node.getDataManager().getUnblocked(parameters2);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
	}

	@Test
	public void testRemove2() throws Exception {
		String locationKey = NetworkTestUtil.randomString();
		String domainKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();
		KeyPair protectionKey1 = EncryptionUtil.generateRSAKeyPair();
		KeyPair protectionKey2 = EncryptionUtil.generateRSAKeyPair();

		NetworkManager node = network.get(random.nextInt(networkSize));

		H2HTestData data1 = new H2HTestData("bla1");
		data1.generateVersionKey();
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setVersionKey(data1.getVersionKey()).setData(data1)
				.setProtectionKeys(protectionKey1);

		// initial put
		FuturePut futurePut = node.getDataManager().putUnblocked(parameters1);
		futurePut.awaitUninterruptibly();

		// check if put was ok
		FutureGet futureGet = node.getDataManager().getVersionUnblocked(parameters1);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());

		// try to remove without a protection key
		Parameters parameters2 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setVersionKey(data1.getVersionKey()).setData(data1);
		FutureRemove futureRemove = node.getDataManager().removeVersionUnblocked(parameters2);
		futureRemove.awaitUninterruptibly();

		// should have been not modified
		futureGet = node.getDataManager().getVersionUnblocked(parameters1);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());

		// try to remove with wrong protection key
		Parameters parameters3 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setVersionKey(data1.getVersionKey())
				.setProtectionKeys(protectionKey2);
		futureRemove = node.getDataManager().removeVersionUnblocked(parameters3);
		futureRemove.awaitUninterruptibly();

		// should have been not modified
		futureGet = node.getDataManager().getVersionUnblocked(parameters1);
		futureGet.awaitUninterruptibly();
		assertEquals(data1.getTestString(), ((H2HTestData) futureGet.getData().object()).getTestString());

		// remove with correct content protection key
		Parameters parameters4 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setVersionKey(data1.getVersionKey()).setData(data1)
				.setProtectionKeys(protectionKey1);
		futureRemove = node.getDataManager().removeVersionUnblocked(parameters4);
		futureRemove.awaitUninterruptibly();

		// should have been removed
		futureGet = node.getDataManager().getVersionUnblocked(parameters1);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

}
