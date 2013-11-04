package org.hive2hive.core.test.process.common;

import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.PutLocationStep;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
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
public class PutLocationStepTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = PutLocationStepTest.class;
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
		newLocations.addOnlinePeer(putter.getPeerAddress());

		// initialize the process and the one and only step to test
		Process process = new Process(putter) {
		};
		PutLocationStep step = new PutLocationStep(newLocations, null);
		process.setFirstStep(step);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// get the locations which should be stored at the proxy
		Locations found = (Locations) proxy.getLocal(userId, H2HConstants.USER_LOCATIONS);
		Assert.assertNotNull(found);

		// verify if both objects are the same
		Assert.assertEquals(userId, found.getUserId());
		Assert.assertEquals(putter.getPeerAddress(), found.getOnlinePeers().get(0).getAddress());
	}

	@Test
	public void testStepRollback() throws InterruptedException {
		NetworkManager putter = network.get(0); // where the process runs
		NetworkManager proxy = network.get(1); // where the user profile is stored

		// create the needed objects
		String userId = proxy.getNodeId();
		Locations newLocations = new Locations(userId);
		newLocations.addOnlinePeer(putter.getPeerAddress());

		// initialize the process and the one and only step to test
		Process process = new Process(putter) {
		};
		PutLocationStep step = new PutLocationStep(newLocations, null);
		process.setFirstStep(step);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());

		// rollback
		process.rollBack("Testing the rollback");

		waiter = new H2HWaiter(20);
		do {
			waiter.tickASecond();
		} while (!listener.hasFailed());

		// get the locations which should be stored at the proxy
		Locations found = (Locations) proxy.getLocal(userId, H2HConstants.USER_LOCATIONS);
		Assert.assertNotNull(found);

		// verify if both objects are the same
		Assert.assertEquals(userId, found.getUserId());
		Assert.assertTrue(found.getOnlinePeers().isEmpty());
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
