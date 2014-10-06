package org.hive2hive.core.network.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.security.KeyPair;
import java.util.ArrayList;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.DataManager.H2HPutStatus;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.EncryptionUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A class which test several content protection scenarios for put (override) or remove calls.
 * 
 * @author Seppi
 */
public class ContentProtectionTest extends H2HJUnitTest {

	private static ArrayList<NetworkManager> network;
	private static final int networkSize = 10;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = DataManagerTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testOverwrittingWithoutProtectionKeys() throws Exception {
		String locationKey = NetworkTestUtil.randomString();
		String domainKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();
		KeyPair protectionKey = EncryptionUtil.generateRSAKeyPair();

		NetworkManager node = NetworkTestUtil.getRandomNode(network);

		// initial put
		H2HTestData data1 = new H2HTestData("bla1");
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setNetworkContent(data1).setProtectionKeys(protectionKey);
		Assert.assertEquals(H2HPutStatus.OK, node.getDataManager().put(parameters1));

		// verify initial put
		assertEquals(data1.getTestString(), ((H2HTestData) node.getDataManager().get(parameters1)).getTestString());

		// try to put without a protection key
		H2HTestData data2 = new H2HTestData("bla2");
		Parameters parameters2 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setNetworkContent(data2);
		Assert.assertEquals(H2HPutStatus.FAILED, node.getDataManager().put(parameters2));

		// should have been not modified
		assertEquals(data1.getTestString(), ((H2HTestData) node.getDataManager().get(parameters1)).getTestString());
	}

	@Test
	public void testOverwrittingWithCorrectProtectionKeys() throws Exception {
		String locationKey = NetworkTestUtil.randomString();
		String domainKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();
		KeyPair protectionKey = EncryptionUtil.generateRSAKeyPair();

		NetworkManager node = NetworkTestUtil.getRandomNode(network);

		// initial put
		H2HTestData data1 = new H2HTestData("bla1");
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setNetworkContent(data1).setProtectionKeys(protectionKey);
		Assert.assertEquals(H2HPutStatus.OK, node.getDataManager().put(parameters1));

		// verify initial put
		assertEquals(data1.getTestString(), ((H2HTestData) node.getDataManager().get(parameters1)).getTestString());

		// overwrite with correct protection key
		H2HTestData data2 = new H2HTestData("bla2");
		Parameters parameters2 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setNetworkContent(data2).setProtectionKeys(protectionKey);
		Assert.assertEquals(H2HPutStatus.OK, node.getDataManager().put(parameters2));

		// verify overwrite
		assertEquals(data2.getTestString(), ((H2HTestData) node.getDataManager().get(parameters2)).getTestString());

		// try to overwrite without protection key
		H2HTestData data3 = new H2HTestData("bla3");
		Parameters parameters3 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setNetworkContent(data3);
		Assert.assertEquals(H2HPutStatus.FAILED, node.getDataManager().put(parameters3));

		// should have been not changed
		assertEquals(data2.getTestString(), ((H2HTestData) node.getDataManager().get(parameters2)).getTestString());
	}

	@Test
	public void testOverwrittingWithWrongProtectionKeys() throws Exception {
		String locationKey = NetworkTestUtil.randomString();
		String domainKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();
		KeyPair protectionKey1 = EncryptionUtil.generateRSAKeyPair();
		KeyPair protectionKey2 = EncryptionUtil.generateRSAKeyPair();

		NetworkManager node = NetworkTestUtil.getRandomNode(network);

		// initial put
		H2HTestData data1 = new H2HTestData("bla1");
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setNetworkContent(data1).setProtectionKeys(protectionKey1);
		Assert.assertEquals(H2HPutStatus.OK, node.getDataManager().put(parameters1));

		// verify initial put
		assertEquals(data1.getTestString(), ((H2HTestData) node.getDataManager().get(parameters1)).getTestString());

		// try to overwrite with wrong protection key
		H2HTestData data2 = new H2HTestData("bla2");
		Parameters parameters2 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setNetworkContent(data2).setProtectionKeys(protectionKey2);
		Assert.assertEquals(H2HPutStatus.FAILED, node.getDataManager().put(parameters2));

		// should have been not changed;
		assertEquals(data1.getTestString(), ((H2HTestData) node.getDataManager().get(parameters1)).getTestString());
	}

	@Test
	public void testRemove() throws Exception {
		String locationKey = NetworkTestUtil.randomString();
		String domainKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();
		KeyPair protectionKey1 = EncryptionUtil.generateRSAKeyPair();
		KeyPair protectionKey2 = EncryptionUtil.generateRSAKeyPair();

		NetworkManager node = NetworkTestUtil.getRandomNode(network);

		// initial put
		H2HTestData data1 = new H2HTestData("bla1");
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setNetworkContent(data1).setProtectionKeys(protectionKey1);
		Assert.assertEquals(H2HPutStatus.OK, node.getDataManager().put(parameters1));

		// verify initial put
		assertEquals(data1.getTestString(), ((H2HTestData) node.getDataManager().get(parameters1)).getTestString());

		// try to remove without protection keys
		Parameters parameters2 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey);
		Assert.assertFalse(node.getDataManager().remove(parameters2));

		// should have been not changed
		assertEquals(data1.getTestString(), ((H2HTestData) node.getDataManager().get(parameters2)).getTestString());

		// try to remove with wrong protection keys
		Parameters parameters3 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setProtectionKeys(protectionKey2);
		Assert.assertFalse(node.getDataManager().remove(parameters3));

		// should have been not changed
		assertEquals(data1.getTestString(), ((H2HTestData) node.getDataManager().get(parameters3)).getTestString());

		// remove with correct protection keys
		Assert.assertTrue(node.getDataManager().remove(parameters1));

		// should have been removed
		assertNull(node.getDataManager().get(parameters1));
	}

	@Test
	public void testRemoveVersion() throws Exception {
		String locationKey = NetworkTestUtil.randomString();
		String domainKey = NetworkTestUtil.randomString();
		String contentKey = NetworkTestUtil.randomString();
		KeyPair protectionKey1 = EncryptionUtil.generateRSAKeyPair();
		KeyPair protectionKey2 = EncryptionUtil.generateRSAKeyPair();

		NetworkManager node = NetworkTestUtil.getRandomNode(network);

		// initial put
		H2HTestData data1 = new H2HTestData("bla1");
		data1.generateVersionKey();
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setVersionKey(data1.getVersionKey()).setNetworkContent(data1)
				.setProtectionKeys(protectionKey1);
		Assert.assertEquals(H2HPutStatus.OK, node.getDataManager().put(parameters1));

		// check if put was ok
		assertEquals(data1.getTestString(), ((H2HTestData) node.getDataManager().get(parameters1)).getTestString());

		// try to remove version without a protection key
		Parameters parameters2a = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setVersionKey(data1.getVersionKey()).setNetworkContent(data1);
		Assert.assertFalse(node.getDataManager().removeVersion(parameters2a));

		// should have been not modified
		assertEquals(data1.getTestString(), ((H2HTestData) node.getDataManager().get(parameters1)).getTestString());

		// try to remove all versions without a protection key
		Parameters parameters2b = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setVersionKey(data1.getVersionKey()).setNetworkContent(data1);
		Assert.assertFalse(node.getDataManager().remove(parameters2b));

		// should have been not modified
		assertEquals(data1.getTestString(), ((H2HTestData) node.getDataManager().get(parameters1)).getTestString());

		// try to remove version with wrong protection key
		Parameters parameters3a = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setVersionKey(data1.getVersionKey()).setProtectionKeys(protectionKey2);
		Assert.assertFalse(node.getDataManager().removeVersion(parameters3a));

		// should have been not modified
		assertEquals(data1.getTestString(), ((H2HTestData) node.getDataManager().get(parameters1)).getTestString());

		// try to remove all versions with wrong protection key
		Parameters parameters3b = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setVersionKey(data1.getVersionKey()).setProtectionKeys(protectionKey2);
		Assert.assertFalse(node.getDataManager().remove(parameters3b));

		// should have been not modified
		assertEquals(data1.getTestString(), ((H2HTestData) node.getDataManager().get(parameters1)).getTestString());

		// remove version with correct content protection key
		Parameters parameters4 = new Parameters().setLocationKey(locationKey).setDomainKey(domainKey)
				.setContentKey(contentKey).setVersionKey(data1.getVersionKey()).setNetworkContent(data1)
				.setProtectionKeys(protectionKey1);
		Assert.assertTrue(node.getDataManager().removeVersion(parameters4));

		// should have been removed
		assertNull(node.getDataManager().get(parameters1));
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

}
