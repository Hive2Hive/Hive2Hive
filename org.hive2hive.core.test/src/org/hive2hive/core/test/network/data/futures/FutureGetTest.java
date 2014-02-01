package org.hive2hive.core.test.network.data.futures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Seppi
 */
public class FutureGetTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 5;
	private static Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FutureGetTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testGetNoVersion() throws NoPeerConnectionException {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));
		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		H2HTestData content = new H2HTestData(NetworkTestUtil.randomString());

		nodeA.getDataManager()
				.put(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
						Number160.createHash(contentKey), content, null).awaitUninterruptibly();

		H2HTestData result = (H2HTestData) nodeB.getDataManager().get(locationKey, contentKey);
		assertEquals(content.getTestString(), result.getTestString());
	}

	@Test
	public void testGetNoVersionNoData() throws NoPeerConnectionException {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));

		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();

		FutureGet futureGet = nodeA.getDataManager().get(Number160.createHash(locationKey),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(contentKey));
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());

		H2HTestData result = (H2HTestData) nodeB.getDataManager().get(locationKey, contentKey);
		assertNull(result);
	}

	@Test
	public void testGetNewestVersion() throws ClassNotFoundException, IOException, NoPeerConnectionException {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));

		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();

		List<H2HTestData> content = new ArrayList<H2HTestData>();
		int numberOfContent = 3;
		for (int i = 0; i < numberOfContent; i++) {
			H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
			data.generateVersionKey();
			if (i > 0) {
				data.setBasedOnKey(content.get(i - 1).getVersionKey());
			}
			content.add(data);

			nodeA.getDataManager()
					.put(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
							Number160.createHash(contentKey), data, null).awaitUninterruptibly();
		}

		H2HTestData result = (H2HTestData) nodeB.getDataManager().get(locationKey, contentKey);
		assertNotNull(result);
		assertEquals(content.get(numberOfContent - 1).getTestString(), result.getTestString());
	}

	@Test
	public void testGetAVersion() throws IOException, NoPeerConnectionException {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));
		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		H2HTestData content = new H2HTestData(NetworkTestUtil.randomString());
		content.generateVersionKey();

		nodeA.getDataManager()
				.put(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
						Number160.createHash(contentKey), content, null).awaitUninterruptibly();

		H2HTestData result = (H2HTestData) nodeB.getDataManager().get(locationKey, contentKey,
				content.getVersionKey());
		assertEquals(content.getTestString(), result.getTestString());
	}

	@Test
	public void testGetAVersionNoData() throws NoPeerConnectionException {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));

		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		Number160 versionKey = Number160.createHash(NetworkTestUtil.randomString());

		FutureGet futureGet = nodeA.getDataManager().get(Number160.createHash(locationKey),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(contentKey));
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());

		H2HTestData result = (H2HTestData) nodeB.getDataManager().get(locationKey, contentKey, versionKey);
		assertNull(result);
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
