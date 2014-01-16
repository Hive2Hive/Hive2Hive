package org.hive2hive.core.test.network.data.futures;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Seppi, Nico
 */
public class FutureRemoveTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 10;
	private static Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FutureRemoveTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testRemove() {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));
		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		H2HTestData content = new H2HTestData(NetworkTestUtil.randomString());

		nodeA.getDataManager()
				.put(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
						Number160.createHash(contentKey), content, null).awaitUninterruptibly();

		boolean success = nodeB.getDataManager().remove(locationKey, contentKey, null);
		assertTrue(success);

		FutureGet futureGet = nodeA.getDataManager().get(Number160.createHash(locationKey),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(contentKey), content.getVersionKey());
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
	}

	@Test
	public void testRemoveSingleVersion() {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));
		String locationKey = nodeA.getNodeId();
		String contentKey = NetworkTestUtil.randomString();
		H2HTestData content = new H2HTestData(NetworkTestUtil.randomString());
		content.generateVersionKey();

		nodeA.getDataManager()
				.put(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
						Number160.createHash(contentKey), content, null).awaitUninterruptibly();

		boolean success = nodeB.getDataManager().remove(locationKey, contentKey, content.getVersionKey(),
				null);
		assertTrue(success);

		FutureGet futureGet = nodeA.getDataManager().get(Number160.createHash(locationKey),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(contentKey), content.getVersionKey());
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
	}

	@Test
	public void testRemoveMultipleVersions() {
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
			nodeA.getDataManager()
					.put(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
							Number160.createHash(contentKey), data, null).awaitUninterruptibly();
			content.add(data);
		}

		boolean success = nodeB.getDataManager().remove(locationKey, contentKey, null);
		assertTrue(success);

		for (H2HTestData data : content) {
			FutureGet futureGet = nodeA.getDataManager().get(Number160.createHash(locationKey),
					H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(contentKey), data.getVersionKey());
			futureGet.awaitUninterruptibly();
			assertNull(futureGet.getData());
		}
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
