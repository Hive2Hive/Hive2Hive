package org.hive2hive.core.test.process.common.get;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.LocationEntry;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.GetLocationsStep;
import org.hive2hive.core.process.context.IGetLocationsContext;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the generic step that puts the location into the DHT
 * 
 * @author Nico, Seppi
 * 
 */
public class GetLocationStepTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = GetLocationStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testStepSuccess() throws InterruptedException {
		NetworkManager getter = network.get(0); // where the process runs
		NetworkManager proxy = network.get(1); // where the user profile is stored

		// create the needed objects
		String userId = proxy.getNodeId();
		Locations newLocations = new Locations(userId);
		LocationEntry status = new LocationEntry(getter.getPeerAddress());
		newLocations.addEntry(status);
		TestGetLocationContext context = new TestGetLocationContext();

		// put the locations to the DHT
		proxy.getDataManager().putLocal(userId, H2HConstants.USER_LOCATIONS, newLocations);

		GetLocationsStep getStep = new GetLocationsStep(userId, null, context);
		Process process = new Process(getter) {
		};
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.setNextStep(getStep);

		// check that receiver does not have any content
		Assert.assertNull(getter.getDataManager().getLocal(userId, H2HConstants.USER_LOCATIONS));

		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasFailed());
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// verify if both objects are the same
		Assert.assertEquals(userId, context.getLocations().getUserId());

		List<LocationEntry> onlinePeers = new ArrayList<LocationEntry>(context.getLocations()
				.getLocationEntries());
		Assert.assertEquals(getter.getPeerAddress(), onlinePeers.get(0).getAddress());
	}

	@Test
	public void testStepSuccessWithNoLocations() {
		NetworkManager getter = network.get(0); // where the process runs
		NetworkManager proxy = network.get(1); // where the user profile is stored

		// create the needed objects, put no locations
		String userId = proxy.getNodeId();
		TestGetLocationContext context = new TestGetLocationContext();

		GetLocationsStep getStep = new GetLocationsStep(userId, null, context);
		Process process = new Process(getter) {
		};
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.setNextStep(getStep);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			assertFalse(listener.hasFailed());
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		Assert.assertNull(context.getLocations());
	}

	private class TestGetLocationContext implements IGetLocationsContext {

		private Locations locations;

		@Override
		public void setLocation(Locations locations) {
			this.locations = locations;
		}

		@Override
		public Locations getLocations() {
			return locations;
		}

	}

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}
}
