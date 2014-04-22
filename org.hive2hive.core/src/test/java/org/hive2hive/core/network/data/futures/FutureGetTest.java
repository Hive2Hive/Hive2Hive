package org.hive2hive.core.network.data.futures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.parameters.Parameters;
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

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters = new Parameters().setLocationKey(nodeA.getNodeId())
				.setContentKey(NetworkTestUtil.randomString()).setData(data);

		nodeA.getDataManager().putUnblocked(parameters).awaitUninterruptibly();

		H2HTestData result = (H2HTestData) nodeB.getDataManager().get(parameters);
		assertEquals(data.getTestString(), result.getTestString());
	}

	@Test
	public void testGetNoVersionNoData() throws NoPeerConnectionException {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));

		Parameters parameters = new Parameters().setLocationKey(nodeA.getNodeId()).setContentKey(
				NetworkTestUtil.randomString());

		FutureGet futureGet = nodeA.getDataManager().getUnblocked(parameters);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());

		H2HTestData result = (H2HTestData) nodeB.getDataManager().get(parameters);
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

			Parameters parameters = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
					.setVersionKey(data.getVersionKey()).setData(data);
			nodeA.getDataManager().putUnblocked(parameters).awaitUninterruptibly();
		}

		H2HTestData result = (H2HTestData) nodeB.getDataManager().get(
				new Parameters().setLocationKey(locationKey).setContentKey(contentKey));
		assertNotNull(result);
		assertEquals(content.get(numberOfContent - 1).getTestString(), result.getTestString());
	}

	@Test
	public void testGetAVersion() throws IOException, NoPeerConnectionException {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		data.generateVersionKey();
		Parameters parameters = new Parameters().setLocationKey(nodeA.getNodeId())
				.setContentKey(NetworkTestUtil.randomString())
				.setVersionKey(Number160.createHash(NetworkTestUtil.randomString())).setData(data);

		nodeA.getDataManager().putUnblocked(parameters).awaitUninterruptibly();

		H2HTestData result = (H2HTestData) nodeB.getDataManager().getVersion(parameters);
		assertEquals(data.getTestString(), result.getTestString());
	}

	@Test
	public void testGetAVersionNoData() throws NoPeerConnectionException {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters = new Parameters().setLocationKey(nodeA.getNodeId())
				.setContentKey(NetworkTestUtil.randomString())
				.setVersionKey(Number160.createHash(NetworkTestUtil.randomString())).setData(data);

		FutureGet futureGet = nodeA.getDataManager().getVersionUnblocked(parameters);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());

		H2HTestData result = (H2HTestData) nodeB.getDataManager().getVersion(parameters);
		assertNull(result);
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
