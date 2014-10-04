package org.hive2hive.core.network.data.futures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	private static final int networkSize = 3;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FutureGetTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testGet() throws NoPeerConnectionException {
		NetworkManager nodeA = NetworkTestUtil.getRandomNode(network);
		NetworkManager nodeB =  NetworkTestUtil.getRandomNode(network);

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters = new Parameters().setLocationKey(nodeA.getNodeId())
				.setContentKey(NetworkTestUtil.randomString()).setNetworkContent(data);

		nodeA.getDataManager().put(parameters);

		assertEquals(data.getTestString(), ((H2HTestData) nodeB.getDataManager().get(parameters)).getTestString());
	}

	@Test
	public void testGetNoData() throws NoPeerConnectionException {
		NetworkManager nodeA = NetworkTestUtil.getRandomNode(network);
		NetworkManager nodeB = NetworkTestUtil.getRandomNode(network);

		Parameters parameters = new Parameters().setLocationKey(nodeA.getNodeId()).setContentKey(
				NetworkTestUtil.randomString());

		assertNull(nodeB.getDataManager().get(parameters));
	}

	@Test
	public void testGetNewestVersion() throws ClassNotFoundException, IOException, NoPeerConnectionException {
		NetworkManager nodeA = NetworkTestUtil.getRandomNode(network);
		NetworkManager nodeB = NetworkTestUtil.getRandomNode(network);

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
					.setVersionKey(data.getVersionKey()).setNetworkContent(data);
			nodeA.getDataManager().put(parameters);
		}

		Parameters parameters = new Parameters().setLocationKey(locationKey).setContentKey(contentKey);
		assertEquals(content.get(numberOfContent - 1).getTestString(),
				((H2HTestData) nodeB.getDataManager().get(parameters)).getTestString());
	}

	@Test
	public void testGetAVersion() throws IOException, NoPeerConnectionException {
		NetworkManager nodeA = NetworkTestUtil.getRandomNode(network);
		NetworkManager nodeB = NetworkTestUtil.getRandomNode(network);

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		data.generateVersionKey();
		Parameters parameters = new Parameters().setLocationKey(nodeA.getNodeId())
				.setContentKey(NetworkTestUtil.randomString())
				.setVersionKey(Number160.createHash(NetworkTestUtil.randomString())).setNetworkContent(data);

		nodeA.getDataManager().put(parameters);

		assertEquals(data.getTestString(), ((H2HTestData) nodeB.getDataManager().get(parameters)).getTestString());
		assertEquals(data.getTestString(), ((H2HTestData) nodeB.getDataManager().getVersion(parameters)).getTestString());
	}

	@Test
	public void testGetAVersionNoData() throws NoPeerConnectionException {
		NetworkManager nodeA = NetworkTestUtil.getRandomNode(network);
		NetworkManager nodeB = NetworkTestUtil.getRandomNode(network);

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters = new Parameters().setLocationKey(nodeA.getNodeId())
				.setContentKey(NetworkTestUtil.randomString())
				.setVersionKey(Number160.createHash(NetworkTestUtil.randomString())).setNetworkContent(data);

		assertNull(nodeB.getDataManager().getVersion(parameters));
		assertNull(nodeB.getDataManager().get(parameters));
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
