package org.hive2hive.core.network.data.vdht;

import java.util.List;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Seppi
 */
public class LocationsManagerTest extends H2HJUnitTest {

	private static List<NetworkManager> network;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = LocationsManagerTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(DEFAULT_NETWORK_SIZE);
	}

	@Test
	public void testRepair() throws Exception {
		NetworkManager node = NetworkTestUtil.getRandomNode(network);
		UseCaseTestUtil.registerAndLogin(generateRandomCredentials(), node, FileTestUtil.getTempDirectory());

		Locations repaired = node.getSession().getLocationsManager().repairLocations();
		Assert.assertNotNull(repaired);
		Assert.assertEquals(0, repaired.getPeerAddresses().size());
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
