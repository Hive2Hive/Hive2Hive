package org.hive2hive.core.test.process.common.get;

import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.LocationEntry;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkPutGetUtil;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the generic step that puts the location into the DHT
 * 
 * @author Nico
 * 
 */
public class GetLocationStepTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = GetLocationStepTest.class;
		beforeClass();
	}

	@Override
	@Before
	public void beforeMethod() {
		super.beforeMethod();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testStepSuccessful() throws InterruptedException {
		NetworkManager putter = network.get(0); // where the process runs
		NetworkManager proxy = network.get(1); // where the user profile is stored

		// create the needed objects
		String userId = proxy.getNodeId();
		Locations newLocations = new Locations(userId);
		LocationEntry status = new LocationEntry(putter.getPeerAddress());
		newLocations.addEntry(status);

		// put the locations to the DHT
		proxy.getDataManager().putLocal(userId, H2HConstants.USER_LOCATIONS, newLocations);

		Locations found = NetworkPutGetUtil.getLocations(putter, userId);

		// verify if both objects are the same
		Assert.assertEquals(userId, found.getUserId());

		List<LocationEntry> onlinePeers = new ArrayList<LocationEntry>(found.getLocationEntries());
		Assert.assertEquals(putter.getPeerAddress(), onlinePeers.get(0).getAddress());
	}

	@Override
	@After
	public void afterMethod() {
		NetworkTestUtil.shutdownNetwork(network);
		super.afterMethod();
	}

	@AfterClass
	public static void endTest() {
		afterClass();
	}
}
