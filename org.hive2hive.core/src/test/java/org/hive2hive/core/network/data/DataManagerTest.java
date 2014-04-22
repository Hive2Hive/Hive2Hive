package org.hive2hive.core.network.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.SignatureException;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.EncryptionUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Seppi
 */
public class DataManagerTest extends H2HJUnitTest {

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
	public void testPutGet() throws Exception {
		String data = NetworkTestUtil.randomString();
		Parameters parameters = new Parameters().setLocationKey(NetworkTestUtil.randomString())
				.setContentKey(NetworkTestUtil.randomString()).setData(new H2HTestData(data));

		NetworkManager node = network.get(random.nextInt(networkSize));

		FuturePut future = node.getDataManager().putUnblocked(parameters);
		future.awaitUninterruptibly();

		FutureGet futureGet = node.getDataManager().getUnblocked(parameters);
		futureGet.awaitUninterruptibly();

		String result = (String) ((H2HTestData) futureGet.getData().object()).getTestString();
		assertEquals(data, result);
	}

	@Test
	public void testPutGetFromOtherNode() throws Exception {
		String data = NetworkTestUtil.randomString();
		Parameters parameters = new Parameters().setLocationKey(NetworkTestUtil.randomString())
				.setContentKey(NetworkTestUtil.randomString()).setData(new H2HTestData(data));

		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);

		FuturePut future = nodeA.getDataManager().putUnblocked(parameters);
		future.awaitUninterruptibly();

		FutureGet futureGet = nodeB.getDataManager().getUnblocked(parameters);
		futureGet.awaitUninterruptibly();

		String result = ((H2HTestData) futureGet.getData().object()).getTestString();
		assertEquals(data, result);
	}

	@Test
	public void testPutOneLocationKeyMultipleContentKeys() throws Exception {
		String locationKey = NetworkTestUtil.randomString();

		NetworkManager node = network.get(random.nextInt(networkSize));

		String data1 = NetworkTestUtil.randomString();
		Parameters parameters1 = new Parameters().setLocationKey(locationKey)
				.setContentKey(NetworkTestUtil.randomString()).setData(new H2HTestData(data1));
		FuturePut future1 = node.getDataManager().putUnblocked(parameters1);
		future1.awaitUninterruptibly();

		String data2 = NetworkTestUtil.randomString();
		Parameters parameters2 = new Parameters().setLocationKey(locationKey)
				.setContentKey(NetworkTestUtil.randomString()).setData(new H2HTestData(data2));
		FuturePut future2 = node.getDataManager().putUnblocked(parameters2);
		future2.awaitUninterruptibly();

		String data3 = NetworkTestUtil.randomString();
		Parameters parameters3 = new Parameters().setLocationKey(locationKey)
				.setContentKey(NetworkTestUtil.randomString()).setData(new H2HTestData(data3));
		FuturePut future3 = node.getDataManager().putUnblocked(parameters3);
		future3.awaitUninterruptibly();

		FutureGet get1 = node.getDataManager().getUnblocked(parameters1);
		get1.awaitUninterruptibly();
		String result1 = (String) ((H2HTestData) get1.getData().object()).getTestString();
		assertEquals(data1, result1);

		FutureGet get2 = node.getDataManager().getUnblocked(parameters2);
		get2.awaitUninterruptibly();
		String result2 = (String) ((H2HTestData) get2.getData().object()).getTestString();
		assertEquals(data2, result2);

		FutureGet get3 = node.getDataManager().getUnblocked(parameters3);
		get3.awaitUninterruptibly();
		String result3 = (String) ((H2HTestData) get3.getData().object()).getTestString();
		assertEquals(data3, result3);
	}

	@Test
	public void testPutOneLocationKeyMultipleContentKeysGlobalGetFromOtherNodes() throws Exception {
		String locationKey = NetworkTestUtil.randomString();

		String data1 = NetworkTestUtil.randomString();
		Parameters parameters1 = new Parameters().setLocationKey(locationKey)
				.setContentKey(NetworkTestUtil.randomString()).setData(new H2HTestData(data1));
		FuturePut future1 = network.get(random.nextInt(networkSize)).getDataManager()
				.putUnblocked(parameters1);
		future1.awaitUninterruptibly();

		String data2 = NetworkTestUtil.randomString();
		Parameters parameters2 = new Parameters().setLocationKey(locationKey)
				.setContentKey(NetworkTestUtil.randomString()).setData(new H2HTestData(data2));
		FuturePut future2 = network.get(random.nextInt(networkSize)).getDataManager()
				.putUnblocked(parameters2);
		future2.awaitUninterruptibly();

		String data3 = NetworkTestUtil.randomString();
		Parameters parameters3 = new Parameters().setLocationKey(locationKey)
				.setContentKey(NetworkTestUtil.randomString()).setData(new H2HTestData(data3));
		FuturePut future3 = network.get(random.nextInt(networkSize)).getDataManager()
				.putUnblocked(parameters3);
		future3.awaitUninterruptibly();

		FutureGet get1 = network.get(random.nextInt(networkSize)).getDataManager().getUnblocked(parameters1);
		get1.awaitUninterruptibly();
		String result1 = (String) ((H2HTestData) get1.getData().object()).getTestString();
		assertEquals(data1, result1);

		FutureGet get2 = network.get(random.nextInt(networkSize)).getDataManager().getUnblocked(parameters2);
		get2.awaitUninterruptibly();
		String result2 = (String) ((H2HTestData) get2.getData().object()).getTestString();
		assertEquals(data2, result2);

		FutureGet get3 = network.get(random.nextInt(networkSize)).getDataManager().getUnblocked(parameters3);
		get3.awaitUninterruptibly();
		String result3 = (String) ((H2HTestData) get3.getData().object()).getTestString();
		assertEquals(data3, result3);
	}

	@Test
	public void testRemovalOneContentKey() throws NoPeerConnectionException {
		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);
		String locationKey = nodeB.getNodeId();

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters = new Parameters().setLocationKey(locationKey).setDomainKey("domain key")
				.setContentKey(NetworkTestUtil.randomString()).setData(data);

		// put a content
		nodeA.getDataManager().putUnblocked(parameters).awaitUninterruptibly();

		// test that it is there
		FutureGet futureGet = nodeB.getDataManager().getUnblocked(parameters);
		futureGet.awaitUninterruptibly();
		assertNotNull(futureGet.getData());

		// delete it
		nodeA.getDataManager().removeUnblocked(parameters).awaitUninterruptibly();

		// check that it is gone
		futureGet = nodeB.getDataManager().getUnblocked(parameters);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
	}

	@Test
	public void testRemovalMultipleContentKey() throws ClassNotFoundException, IOException,
			NoPeerConnectionException {
		NetworkManager nodeA = network.get(random.nextInt(networkSize / 2));
		NetworkManager nodeB = network.get(random.nextInt(networkSize / 2) + networkSize / 2);

		String locationKey = nodeB.getNodeId();

		String contentKey1 = NetworkTestUtil.randomString();
		String testString1 = NetworkTestUtil.randomString();
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey1)
				.setData(new H2HTestData(testString1));

		String contentKey2 = NetworkTestUtil.randomString();
		String testString2 = NetworkTestUtil.randomString();
		Parameters parameters2 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey2)
				.setData(new H2HTestData(testString2));

		String contentKey3 = NetworkTestUtil.randomString();
		String testString3 = NetworkTestUtil.randomString();
		Parameters parameters3 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey3)
				.setData(new H2HTestData(testString3));

		// insert them
		FuturePut put1 = nodeA.getDataManager().putUnblocked(parameters1);
		put1.awaitUninterruptibly();

		FuturePut put2 = nodeA.getDataManager().putUnblocked(parameters2);
		put2.awaitUninterruptibly();

		FuturePut put3 = nodeA.getDataManager().putUnblocked(parameters3);
		put3.awaitUninterruptibly();

		// check that they are all stored
		FutureGet futureGet = nodeB.getDataManager().getUnblocked(parameters1);
		futureGet.awaitUninterruptibly();
		assertEquals(testString1, ((H2HTestData) futureGet.getData().object()).getTestString());
		futureGet = nodeB.getDataManager().getUnblocked(parameters2);
		futureGet.awaitUninterruptibly();
		assertEquals(testString2, ((H2HTestData) futureGet.getData().object()).getTestString());
		futureGet = nodeB.getDataManager().getUnblocked(parameters3);
		futureGet.awaitUninterruptibly();
		assertEquals(testString3, ((H2HTestData) futureGet.getData().object()).getTestString());

		// remove 2nd one and check that 1st and 3rd are still there
		nodeA.getDataManager().removeUnblocked(parameters2).awaitUninterruptibly();
		futureGet = nodeB.getDataManager().getUnblocked(parameters1);
		futureGet.awaitUninterruptibly();
		assertEquals(testString1, ((H2HTestData) futureGet.getData().object()).getTestString());
		futureGet = nodeB.getDataManager().getUnblocked(parameters2);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
		futureGet = nodeB.getDataManager().getUnblocked(parameters3);
		futureGet.awaitUninterruptibly();
		assertEquals(testString3, ((H2HTestData) futureGet.getData().object()).getTestString());

		// remove 3rd one as well and check that they are gone as well
		nodeA.getDataManager().removeUnblocked(parameters1).awaitUninterruptibly();
		nodeA.getDataManager().removeUnblocked(parameters3).awaitUninterruptibly();
		futureGet = nodeB.getDataManager().getUnblocked(parameters1);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
		futureGet = nodeB.getDataManager().getUnblocked(parameters2);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
		futureGet = nodeB.getDataManager().getUnblocked(parameters3);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
	}

	@Test
	public void testChangeProtectionKeySingleVersionKey() throws NoPeerConnectionException, IOException,
			InvalidKeyException, SignatureException {
		KeyPair keypairOld = EncryptionUtil.generateRSAKeyPair();
		KeyPair keypairNew = EncryptionUtil.generateRSAKeyPair();

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		data.generateVersionKey();
		data.setBasedOnKey(Number160.ZERO);
		Parameters parameters = new Parameters().setLocationKey(NetworkTestUtil.randomString())
				.setContentKey(NetworkTestUtil.randomString()).setVersionKey(data.getVersionKey())
				.setData(data).setProtectionKeys(keypairOld).setNewProtectionKeys(keypairNew)
				.setTTL(data.getTimeToLive()).setHashFlag(true);

		NetworkManager node = network.get(random.nextInt(networkSize));

		// put some initial data
		FuturePut putFuture1 = node.getDataManager().putUnblocked(parameters);
		putFuture1.awaitUninterruptibly();
		Assert.assertTrue(putFuture1.isSuccess());
		
		// parameters without the data object itself
		parameters = new Parameters().setLocationKey(parameters.getLocationKey())
			.setContentKey(parameters.getContentKey()).setVersionKey(data.getVersionKey())
			.setProtectionKeys(keypairOld).setNewProtectionKeys(keypairNew)
			.setTTL(data.getTimeToLive());

		// change content protection key
		FuturePut changeFuture = node.getDataManager().changeProtectionKeyUnblocked(parameters);
		changeFuture.awaitUninterruptibly();
		Assert.assertTrue(changeFuture.isSuccess());

		// verify if content protection key has been changed
		Data resData = node.getDataManager().getUnblocked(parameters).awaitUninterruptibly().getData();
		Assert.assertEquals(keypairNew.getPublic(), resData.publicKey());
	}

	@Test
	@Ignore
	public void testChangeProtectionKeyMultipleVersionKeys() throws NoPeerConnectionException, IOException,
			InvalidKeyException, SignatureException {
		// TODO test case for changing entries wit same location, domain and content key, but different
		// version keys
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
