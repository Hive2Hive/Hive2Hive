package org.hive2hive.core.network.data.futures;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Seppi, Nico
 */
public class FutureRemoveTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 10;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FutureRemoveTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testRemove() throws NoPeerConnectionException {
		NetworkManager nodeA = NetworkTestUtil.getRandomNode(network);
		NetworkManager nodeB = NetworkTestUtil.getRandomNode(network);

		H2HTestData data = new H2HTestData(randomString());
		Parameters parameters = new Parameters().setLocationKey(nodeA.getNodeId()).setContentKey(randomString())
				.setNetworkContent(data);

		nodeA.getDataManager().put(parameters);

		assertTrue(nodeB.getDataManager().remove(parameters));
		assertNull(nodeA.getDataManager().get(parameters));
	}

	@Test
	public void testRemoveSingleVersion() throws IOException, NoPeerConnectionException {
		NetworkManager nodeA = NetworkTestUtil.getRandomNode(network);
		NetworkManager nodeB = NetworkTestUtil.getRandomNode(network);

		H2HTestData data = new H2HTestData(randomString());
		data.generateVersionKey();
		Parameters parameters = new Parameters().setLocationKey(nodeA.getNodeId()).setContentKey(randomString())
				.setVersionKey(data.getVersionKey()).setNetworkContent(data);

		nodeA.getDataManager().put(parameters);

		assertTrue(nodeB.getDataManager().removeVersion(parameters));
		assertNull(nodeA.getDataManager().get(parameters));
		assertNull(nodeA.getDataManager().getVersion(parameters));
	}

	@Test
	public void testRemoveMultipleVersions() throws IOException, NoPeerConnectionException {
		NetworkManager nodeA = NetworkTestUtil.getRandomNode(network);
		NetworkManager nodeB = NetworkTestUtil.getRandomNode(network);

		String locationKey = nodeA.getNodeId();
		String contentKey = randomString();

		List<H2HTestData> content = new ArrayList<H2HTestData>();
		int numberOfContent = 3;
		for (int i = 0; i < numberOfContent; i++) {
			H2HTestData data = new H2HTestData(randomString());
			data.generateVersionKey();
			if (i > 0) {
				data.setBasedOnKey(content.get(i - 1).getVersionKey());
			}
			Parameters parameters = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
					.setVersionKey(data.getVersionKey()).setNetworkContent(data);
			nodeA.getDataManager().put(parameters);
			content.add(data);
		}

		Parameters parameters = new Parameters().setLocationKey(locationKey).setContentKey(contentKey);
		assertTrue(nodeB.getDataManager().remove(parameters));

		assertNull(nodeA.getDataManager().get(parameters));
		for (H2HTestData data : content) {
			parameters.setVersionKey(data.getVersionKey());
			assertNull(nodeA.getDataManager().getVersion(parameters));
		}
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
