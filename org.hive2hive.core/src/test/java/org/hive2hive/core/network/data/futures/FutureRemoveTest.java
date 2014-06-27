package org.hive2hive.core.network.data.futures;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.tomp2p.futures.FutureGet;

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
	public void testRemove() throws NoPeerConnectionException {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		Parameters parameters = new Parameters().setLocationKey(nodeA.getNodeId())
				.setContentKey(NetworkTestUtil.randomString()).setData(data);

		nodeA.getDataManager().putUnblocked(parameters).awaitUninterruptibly();

		boolean success = nodeB.getDataManager().remove(parameters);
		assertTrue(success);

		FutureGet futureGet = nodeA.getDataManager().getUnblocked(parameters);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
	}

	@Test
	public void testRemoveSingleVersion() throws IOException, NoPeerConnectionException {
		NetworkManager nodeA = network.get(random.nextInt(networkSize));
		NetworkManager nodeB = network.get(random.nextInt(networkSize));

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		data.generateVersionKey();
		Parameters parameters = new Parameters().setLocationKey(nodeA.getNodeId())
				.setContentKey(NetworkTestUtil.randomString()).setVersionKey(data.getVersionKey()).setData(data);

		nodeA.getDataManager().putUnblocked(parameters).awaitUninterruptibly();

		boolean success = nodeB.getDataManager().removeVersion(parameters);
		assertTrue(success);

		FutureGet futureGet = nodeA.getDataManager().getVersionUnblocked(parameters);
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
	}

	@Test
	public void testRemoveMultipleVersions() throws IOException, NoPeerConnectionException {
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
			Parameters parameters = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
					.setVersionKey(data.getVersionKey()).setData(data);
			nodeA.getDataManager().putUnblocked(parameters).awaitUninterruptibly();
			content.add(data);
		}

		boolean success = nodeB.getDataManager().remove(
				new Parameters().setLocationKey(locationKey).setContentKey(contentKey));
		assertTrue(success);

		for (H2HTestData data : content) {
			Parameters parameters = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
					.setVersionKey(data.getVersionKey()).setData(data);
			FutureGet futureGet = nodeA.getDataManager().getVersionUnblocked(parameters);
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
