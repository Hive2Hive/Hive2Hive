package org.hive2hive.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.security.KeyPair;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FuturePut;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.H2HStorageMemory.PutStatusH2H;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.EncryptionUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Seppi
 */
public class H2HStorageMemoryTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 10;
	private static Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = H2HStorageMemoryTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void putIntialVersionKeyZeroTest() throws NoPeerConnectionException {
		NetworkManager node = network.get(random.nextInt(networkSize));

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters = new Parameters().setLocationKey(node.getNodeId())
				.setContentKey(NetworkTestUtil.randomString()).setData(data);

		FuturePut futurePut = node.getDataManager().putUnblocked(parameters);
		futurePut.awaitUninterruptibly();

		assertFalse(futurePut.getRawResult().isEmpty());
		for (PeerAddress peerAddress : futurePut.getRawResult().keySet()) {
			for (Number640 key : futurePut.getRawResult().get(peerAddress).keySet()) {
				assertEquals(PutStatusH2H.OK, PutStatusH2H.values()[futurePut.getRawResult().get(peerAddress)
						.get(key)]);
			}
		}
	}

	@Test
	public void putVersionKeyZeroPreviousVersionKeyZeroTest() throws NoPeerConnectionException {
		NetworkManager node = network.get(random.nextInt(networkSize));
		String locationKey = node.getNodeId();
		String contentKey = NetworkTestUtil.randomString();

		H2HTestData data1 = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setData(data1);
		FuturePut futurePut1 = node.getDataManager().putUnblocked(parameters1);
		futurePut1.awaitUninterruptibly();

		H2HTestData data2 = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters2 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setData(data2);
		FuturePut futurePut2 = node.getDataManager().putUnblocked(parameters2);
		futurePut2.awaitUninterruptibly();

		assertFalse(futurePut2.getRawResult().isEmpty());
		for (PeerAddress peerAddress : futurePut2.getRawResult().keySet()) {
			for (Number640 key : futurePut2.getRawResult().get(peerAddress).keySet()) {
				assertEquals(PutStatusH2H.OK, PutStatusH2H.values()[futurePut2.getRawResult()
						.get(peerAddress).get(key)]);
			}
		}
	}

	@Test
	public void putVersionKeyZeroPreviousVersionKeyNotZeroTest() throws IOException,
			NoPeerConnectionException {
		NetworkManager node = network.get(random.nextInt(networkSize));
		String locationKey = node.getNodeId();
		String contentKey = NetworkTestUtil.randomString();

		H2HTestData data1 = new H2HTestData(NetworkTestUtil.randomString());
		data1.generateVersionKey();
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setVersionKey(data1.getVersionKey()).setData(data1);
		FuturePut futurePut1 = node.getDataManager().putUnblocked(parameters1);
		futurePut1.awaitUninterruptibly();

		H2HTestData data2 = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters2 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setData(data2);
		FuturePut futurePut2 = node.getDataManager().putUnblocked(parameters2);
		futurePut2.awaitUninterruptibly();

		assertFalse(futurePut2.getRawResult().isEmpty());
		for (PeerAddress peerAddress : futurePut2.getRawResult().keySet()) {
			for (Number640 key : futurePut2.getRawResult().get(peerAddress).keySet()) {
				assertEquals(PutStatusH2H.VERSION_CONFLICT_NO_VERSION_KEY, PutStatusH2H.values()[futurePut2
						.getRawResult().get(peerAddress).get(key)]);
			}
		}
	}

	@Test
	public void putInitialTest() throws IOException, NoPeerConnectionException {
		NetworkManager node = network.get(random.nextInt(networkSize));
		String locationKey = node.getNodeId();
		String contentKey = NetworkTestUtil.randomString();

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		data.generateVersionKey();
		assertNotEquals(Number160.ZERO, data.getVersionKey());
		Parameters parameters = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setVersionKey(data.getVersionKey()).setData(data);

		FuturePut futurePut = node.getDataManager().putUnblocked(parameters);
		futurePut.awaitUninterruptibly();
		futurePut.getFutureRequests().awaitUninterruptibly();

		assertFalse(futurePut.getRawResult().isEmpty());
		for (PeerAddress peerAddress : futurePut.getRawResult().keySet()) {
			for (Number640 key : futurePut.getRawResult().get(peerAddress).keySet()) {
				assertEquals(PutStatusH2H.OK, PutStatusH2H.values()[futurePut.getRawResult().get(peerAddress)
						.get(key)]);
			}
		}
	}

	@Test
	public void putNoBasedOnTest() throws IOException, NoPeerConnectionException {
		NetworkManager node = network.get(random.nextInt(networkSize));
		String locationKey = node.getNodeId();
		String contentKey = NetworkTestUtil.randomString();

		H2HTestData data1 = new H2HTestData(NetworkTestUtil.randomString());
		data1.generateVersionKey();
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setVersionKey(data1.getVersionKey()).setData(data1);
		FuturePut futurePut1 = node.getDataManager().putUnblocked(parameters1);
		futurePut1.awaitUninterruptibly();

		H2HTestData data2 = new H2HTestData(NetworkTestUtil.randomString());
		data2.generateVersionKey();
		Parameters parameters2 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setVersionKey(data1.getVersionKey()).setData(data2);
		FuturePut futurePut2 = node.getDataManager().putUnblocked(parameters2);
		futurePut2.awaitUninterruptibly();

		assertFalse(futurePut2.getRawResult().isEmpty());
		for (PeerAddress peerAddress : futurePut2.getRawResult().keySet()) {
			for (Number640 key : futurePut2.getRawResult().get(peerAddress).keySet()) {
				assertEquals(PutStatusH2H.VERSION_CONFLICT_NO_BASED_ON, PutStatusH2H.values()[futurePut2
						.getRawResult().get(peerAddress).get(key)]);
			}
		}
	}

	@Test
	public void putVersionConflictTest() throws IOException, NoPeerConnectionException {
		NetworkManager node = network.get(random.nextInt(networkSize));
		String locationKey = node.getNodeId();
		String contentKey = NetworkTestUtil.randomString();

		H2HTestData data1 = new H2HTestData(NetworkTestUtil.randomString());
		data1.generateVersionKey();
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setVersionKey(data1.getVersionKey()).setData(data1);
		FuturePut futurePut1 = node.getDataManager().putUnblocked(parameters1);
		futurePut1.awaitUninterruptibly();

		H2HTestData data2 = new H2HTestData(NetworkTestUtil.randomString());
		data2.generateVersionKey();

		H2HTestData data3 = new H2HTestData(NetworkTestUtil.randomString());
		data3.generateVersionKey();
		data3.setBasedOnKey(data2.getVersionKey());
		Parameters parameters3 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setVersionKey(data3.getVersionKey()).setData(data3);
		FuturePut futurePut3 = node.getDataManager().putUnblocked(parameters3);
		futurePut3.awaitUninterruptibly();

		assertFalse(futurePut3.getRawResult().isEmpty());
		for (PeerAddress peerAddress : futurePut3.getRawResult().keySet()) {
			for (Number640 key : futurePut3.getRawResult().get(peerAddress).keySet()) {
				assertEquals(PutStatusH2H.VERSION_CONFLICT, PutStatusH2H.values()[futurePut3.getRawResult()
						.get(peerAddress).get(key)]);
			}
		}
	}

	@Test
	public void putNewVersionTest() throws IOException, NoPeerConnectionException {
		NetworkManager node = network.get(random.nextInt(networkSize));
		String locationKey = node.getNodeId();
		String contentKey = NetworkTestUtil.randomString();

		H2HTestData data1 = new H2HTestData(NetworkTestUtil.randomString());
		data1.generateVersionKey();
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setVersionKey(data1.getVersionKey()).setData(data1);
		FuturePut futurePut1 = node.getDataManager().putUnblocked(parameters1);
		futurePut1.awaitUninterruptibly();

		H2HTestData data2 = new H2HTestData(NetworkTestUtil.randomString());
		data2.generateVersionKey();
		data2.setBasedOnKey(data1.getVersionKey());
		Parameters parameters2 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setVersionKey(data2.getVersionKey()).setData(data2);
		FuturePut futurePut2 = node.getDataManager().putUnblocked(parameters2);
		futurePut2.awaitUninterruptibly();

		assertFalse(futurePut2.getRawResult().isEmpty());
		for (PeerAddress peerAddress : futurePut2.getRawResult().keySet()) {
			for (Number640 key : futurePut2.getRawResult().get(peerAddress).keySet()) {
				assertEquals(PutStatusH2H.OK, PutStatusH2H.values()[futurePut2.getRawResult()
						.get(peerAddress).get(key)]);
			}
		}
	}

	@Test
	public void putNewVersionWithOldTimestampTest() throws InterruptedException, IOException,
			NoPeerConnectionException {
		NetworkManager node = network.get(random.nextInt(networkSize));
		String locationKey = node.getNodeId();
		String contentKey = NetworkTestUtil.randomString();

		// fake older time stamp, create data2 before data1
		H2HTestData data2 = new H2HTestData(NetworkTestUtil.randomString());
		data2.generateVersionKey();

		synchronized (this) {
			Thread.sleep(100);
		}

		H2HTestData data1 = new H2HTestData(NetworkTestUtil.randomString());
		data1.generateVersionKey();
		data2.setBasedOnKey(data1.getVersionKey());

		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setVersionKey(data1.getVersionKey()).setData(data1);
		Parameters parameters2 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setVersionKey(data2.getVersionKey()).setData(data2);

		FuturePut futurePut1 = node.getDataManager().putUnblocked(parameters1);
		futurePut1.awaitUninterruptibly();

		FuturePut futurePut2 = node.getDataManager().putUnblocked(parameters2);
		futurePut2.awaitUninterruptibly();

		assertFalse(futurePut2.getRawResult().isEmpty());
		for (PeerAddress peerAddress : futurePut2.getRawResult().keySet()) {
			for (Number640 key : futurePut2.getRawResult().get(peerAddress).keySet()) {
				assertEquals(PutStatusH2H.VERSION_CONFLICT_OLD_TIMESTAMP, PutStatusH2H.values()[futurePut2
						.getRawResult().get(peerAddress).get(key)]);
			}
		}
	}

	@Test
	public void putFailedSecurityTest() throws InterruptedException, NoPeerConnectionException {
		NetworkManager node = network.get(random.nextInt(networkSize));
		String locationKey = node.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		KeyPair protectionKeys = EncryptionUtil.generateRSAKeyPair();

		H2HTestData data1 = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters1 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setData(data1).setProtectionKeys(protectionKeys);
		FuturePut futurePut = node.getDataManager().putUnblocked(parameters1);
		futurePut.awaitUninterruptibly();

		H2HTestData data2 = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters2 = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setData(data2);
		futurePut = node.getDataManager().putUnblocked(parameters2);
		futurePut.awaitUninterruptibly();

		assertFalse(futurePut.getRawResult().isEmpty());
		for (PeerAddress peerAddress : futurePut.getRawResult().keySet()) {
			for (Number640 key : futurePut.getRawResult().get(peerAddress).keySet()) {
				assertEquals(PutStatusH2H.FAILED_SECURITY, PutStatusH2H.values()[futurePut.getRawResult()
						.get(peerAddress).get(key)]);
			}
		}
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

}
