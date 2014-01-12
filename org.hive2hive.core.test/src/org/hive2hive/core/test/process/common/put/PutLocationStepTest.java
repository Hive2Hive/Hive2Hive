package org.hive2hive.core.test.process.common.put;

import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.H2HStorageMemory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.put.PutLocationStep;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.ProcessTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the generic step that puts the location into the DHT.
 * 
 * @author Nico, Seppi
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
	public void testStepSuccessful() throws InterruptedException, ClassNotFoundException, IOException {
		NetworkManager putter = network.get(0); // where the process runs
		putter.getConnection().getPeer().getPeerBean().storage(new H2HStorageMemory());
		NetworkManager proxy = network.get(1); // where the user profile is stored
		proxy.getConnection().getPeer().getPeerBean().storage(new H2HStorageMemory());

		// create the needed objects
		String userId = proxy.getNodeId();
		Locations newLocations = new Locations(userId);
		newLocations.addPeerAddress(putter.getPeerAddress());
		KeyPair protectionKeys = EncryptionUtil.generateProtectionKey();

		// initialize the process and the one and only step to test
		Process process = new Process(putter) {
		};
		PutLocationStep step = new PutLocationStep(newLocations, protectionKeys, null);
		process.setNextStep(step);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		ProcessTestUtil.waitTillSucceded(listener, 10);

		// get the locations
		FutureGet future = proxy.getDataManager().get(Number160.createHash(userId),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(H2HConstants.USER_LOCATIONS));
		future.awaitUninterruptibly();
		Assert.assertNotNull(future.getData());
		Locations found = (Locations) future.getData().object();

		// verify if both objects are the same
		Assert.assertEquals(userId, found.getUserId());

		List<PeerAddress> onlinePeers = new ArrayList<PeerAddress>(found.getPeerAddresses());
		Assert.assertEquals(putter.getPeerAddress(), onlinePeers.get(0));
	}

	@Test
	public void testStepRollback() throws InterruptedException {
		NetworkManager putter = network.get(0); // where the process runs
		putter.getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());
		NetworkManager proxy = network.get(1); // where the user profile is stored
		proxy.getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());

		// create the needed objects
		String userId = proxy.getNodeId();
		Locations newLocations = new Locations(userId);
		newLocations.addPeerAddress(putter.getPeerAddress());
		KeyPair protectionKeys = EncryptionUtil.generateProtectionKey();

		// initialize the process and the one and only step to test
		Process process = new Process(putter) {
		};
		PutLocationStep step = new PutLocationStep(newLocations, protectionKeys, null);
		process.setNextStep(step);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		ProcessTestUtil.waitTillFailed(listener, 10);

		// get the locations which should be stored at the proxy --> they should be null
		FutureGet futureGet = proxy.getDataManager().get(Number160.createHash(userId),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(H2HConstants.USER_LOCATIONS));
		futureGet.awaitUninterruptibly();
		assertNull(futureGet.getData());
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
