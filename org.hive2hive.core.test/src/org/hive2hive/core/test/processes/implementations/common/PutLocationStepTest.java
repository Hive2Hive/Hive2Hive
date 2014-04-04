package org.hive2hive.core.test.processes.implementations.common;

import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.H2HStorageMemory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.common.PutUserLocationsStep;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeLocations;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeProtectionKeys;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.processes.implementations.common.base.DenyingPutTestStorage;
import org.hive2hive.core.test.processes.util.TestProcessComponentListener;
import org.hive2hive.core.test.processes.util.UseCaseTestUtil;
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
	public void testStepSuccessful() throws InterruptedException, ClassNotFoundException, IOException,
			NoPeerConnectionException {
		NetworkManager putter = network.get(0); // where the process runs
		putter.getConnection().getPeer().getPeerBean().storage(new H2HStorageMemory());
		NetworkManager proxy = network.get(1); // where the user profile is stored
		proxy.getConnection().getPeer().getPeerBean().storage(new H2HStorageMemory());

		// create the needed objects
		String userId = proxy.getNodeId();
		Locations newLocations = new Locations(userId);
		newLocations.addPeerAddress(putter.getConnection().getPeer().getPeerAddress());
		KeyPair protectionKeys = EncryptionUtil.generateRSAKeyPair();

		// initialize the process and the one and only step to test
		PutLocationContext context = new PutLocationContext(newLocations, protectionKeys);
		PutUserLocationsStep step = new PutUserLocationsStep(context, context, putter.getDataManager());
		UseCaseTestUtil.executeProcess(step);

		// get the locations
		FutureGet future = proxy.getDataManager().getUnblocked(
				new Parameters().setLocationKey(userId).setContentKey(H2HConstants.USER_LOCATIONS));
		future.awaitUninterruptibly();
		Assert.assertNotNull(future.getData());
		Locations found = (Locations) future.getData().object();

		// verify if both objects are the same
		Assert.assertEquals(userId, found.getUserId());

		List<PeerAddress> onlinePeers = new ArrayList<PeerAddress>(found.getPeerAddresses());
		Assert.assertEquals(putter.getConnection().getPeer().getPeerAddress(), onlinePeers.get(0));
	}

	@Test
	public void testStepRollback() throws InterruptedException, NoPeerConnectionException,
			InvalidProcessStateException {
		NetworkManager putter = network.get(0); // where the process runs
		putter.getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());
		NetworkManager proxy = network.get(1); // where the user profile is stored
		proxy.getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());

		// create the needed objects
		String userId = proxy.getNodeId();
		Locations newLocations = new Locations(userId);
		newLocations.addPeerAddress(putter.getConnection().getPeer().getPeerAddress());
		KeyPair protectionKeys = EncryptionUtil.generateRSAKeyPair();

		// initialize the process and the one and only step to test
		PutLocationContext context = new PutLocationContext(newLocations, protectionKeys);
		PutUserLocationsStep step = new PutUserLocationsStep(context, context, putter.getDataManager());
		TestProcessComponentListener listener = new TestProcessComponentListener();
		step.attachListener(listener);
		step.start();

		// wait for the process to finish
		UseCaseTestUtil.waitTillFailed(listener, 10);

		// get the locations which should be stored at the proxy --> they should be null
		FutureGet futureGet = proxy.getDataManager().getUnblocked(
				new Parameters().setLocationKey(userId).setContentKey(H2HConstants.USER_LOCATIONS));
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

	private class PutLocationContext implements IConsumeProtectionKeys, IConsumeLocations {

		private final KeyPair protectionKeys;
		private final Locations locations;

		public PutLocationContext(Locations locations, KeyPair protectionKeys) {
			this.locations = locations;
			this.protectionKeys = protectionKeys;
		}

		@Override
		public Locations consumeLocations() {
			return locations;
		}

		@Override
		public KeyPair consumeProtectionKeys() {
			return protectionKeys;
		}
	}
}
