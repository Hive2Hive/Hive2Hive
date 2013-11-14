package org.hive2hive.core.test.process.login.postLogin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.LocationEntry;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.login.ContactPeersStep;
import org.hive2hive.core.process.login.PostLoginProcess;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class to check if the {@link ContactPeersStep} process step works correctly. Only this process step
 * will be tested in a own process environment.
 * 
 * node 0 is the new client node
 * node 1, 2, 3, 4 are other alive client nodes
 * node 5, 6 are not responding client nodes
 * 
 * @author Seppi
 */
public class ContactPeersStepTest extends H2HJUnitTest {

	private static final int networkSize = 7;
	private static List<NetworkManager> network;
	private static String userId = "user id";

	private Locations result = null;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = ContactPeersStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
		// assign to each node the same key pair (simulating same user)
		NetworkTestUtil.createSameKeyPair(network);
		network.get(5).getConnection().getPeer().setObjectDataReply(new DenyingMessageReplyHandler());
		network.get(6).getConnection().getPeer().setObjectDataReply(new DenyingMessageReplyHandler());
	}

	/**
	 * All client nodes are alive. One is a master client.
	 */
	@Test
	public void allClientsAreAliveOneIsMaster() {
		Locations fakedLocations = new Locations(userId);
		// responding node and master
		fakedLocations.addEntry(new LocationEntry(network.get(1).getPeerAddress(), true));
		// responding masters
		fakedLocations.addEntry(new LocationEntry(network.get(2).getPeerAddress(), false));
		fakedLocations.addEntry(new LocationEntry(network.get(3).getPeerAddress(), false));
		fakedLocations.addEntry(new LocationEntry(network.get(4).getPeerAddress(), false));

		runProcessStep(fakedLocations);

		assertEquals(5, result.getLocationEntries().size());
		assertEquals(network.get(1).getPeerAddress(), result.getMaster().getAddress());
		LocationEntry newClientsEntry = null;
		for (LocationEntry locationEntry : result.getLocationEntries()) {
			if (locationEntry.getAddress().equals(network.get(0).getPeerAddress())) {
				newClientsEntry = locationEntry;
				break;
			}
		}
		assertNotNull(newClientsEntry);
		assertFalse(newClientsEntry.isMaster());
	}

	/**
	 * All client nodes are alive. No one is master.
	 */
	@Test
	public void allClientsAreAliveNoMaster() {
		Locations fakedLocations = new Locations(userId);
		// responding nodes
		fakedLocations.addEntry(new LocationEntry(network.get(1).getPeerAddress(), false));
		fakedLocations.addEntry(new LocationEntry(network.get(2).getPeerAddress(), false));
		fakedLocations.addEntry(new LocationEntry(network.get(3).getPeerAddress(), false));
		fakedLocations.addEntry(new LocationEntry(network.get(4).getPeerAddress(), false));

		runProcessStep(fakedLocations);

		assertEquals(5, result.getLocationEntries().size());
		LocationEntry newClientsEntry = null;
		for (LocationEntry locationEntry : result.getLocationEntries()) {
			if (locationEntry.getAddress().equals(network.get(0).getPeerAddress())) {
				newClientsEntry = locationEntry;
				break;
			}
		}
		assertNotNull(newClientsEntry);
		assertTrue(newClientsEntry.isMaster());
	}
	
	/**
	 * Two clients are offline and two are alive. One is a master client.
	 */
	@Test
	public void notAllClientsAreAliveOneIsMaster() {
		Locations fakedLocations = new Locations(userId);
		// responding node and master
		fakedLocations.addEntry(new LocationEntry(network.get(1).getPeerAddress(), true));
		// responding node
		fakedLocations.addEntry(new LocationEntry(network.get(2).getPeerAddress(), false));
		// not responding nodes
		fakedLocations.addEntry(new LocationEntry(network.get(5).getPeerAddress(), false));
		fakedLocations.addEntry(new LocationEntry(network.get(6).getPeerAddress(), false));
		
		runProcessStep(fakedLocations);

		assertEquals(3, result.getLocationEntries().size());
		assertEquals(network.get(1).getPeerAddress(), result.getMaster().getAddress());
		LocationEntry newClientsEntry = null;
		for (LocationEntry locationEntry : result.getLocationEntries()) {
			if (locationEntry.getAddress().equals(network.get(0).getPeerAddress())) {
				newClientsEntry = locationEntry;
			}
			assertNotEquals(locationEntry.getAddress(), network.get(5).getPeerAddress());
			assertNotEquals(locationEntry.getAddress(), network.get(6).getPeerAddress());
		}
		assertNotNull(newClientsEntry);
		assertFalse(newClientsEntry.isMaster());
	}

	/**
	 * Two clients are offline and two are alive. No one is master.
	 */
	@Test
	public void notAllClientsAreAliveNoMaster() {
		Locations fakedLocations = new Locations(userId);
		// responding nodes
		fakedLocations.addEntry(new LocationEntry(network.get(1).getPeerAddress(), false));
		fakedLocations.addEntry(new LocationEntry(network.get(2).getPeerAddress(), false));
		// not responding nodes
		fakedLocations.addEntry(new LocationEntry(network.get(5).getPeerAddress(), false));
		fakedLocations.addEntry(new LocationEntry(network.get(6).getPeerAddress(), false));
		
		runProcessStep(fakedLocations);

		assertEquals(3, result.getLocationEntries().size());
		LocationEntry newClientsEntry = null;
		for (LocationEntry locationEntry : result.getLocationEntries()) {
			if (locationEntry.getAddress().equals(network.get(0).getPeerAddress())) {
				newClientsEntry = locationEntry;
			}
			assertNotEquals(locationEntry.getAddress(), network.get(5).getPeerAddress());
			assertNotEquals(locationEntry.getAddress(), network.get(6).getPeerAddress());
		}
		assertNotNull(newClientsEntry);
		assertTrue(newClientsEntry.isMaster());
	}
	
	/**
	 * Two clients are offline and two are alive. Dead client was master.
	 */
	@Test
	public void notAllClientsAreAliveDeadClientWasMaster() {
		Locations fakedLocations = new Locations(userId);
		// responding nodes
		fakedLocations.addEntry(new LocationEntry(network.get(1).getPeerAddress(), false));
		fakedLocations.addEntry(new LocationEntry(network.get(2).getPeerAddress(), false));
		// not responding nodes
		fakedLocations.addEntry(new LocationEntry(network.get(5).getPeerAddress(), true));
		fakedLocations.addEntry(new LocationEntry(network.get(6).getPeerAddress(), false));
		
		runProcessStep(fakedLocations);

		assertEquals(3, result.getLocationEntries().size());
		LocationEntry newClientsEntry = null;
		for (LocationEntry locationEntry : result.getLocationEntries()) {
			if (locationEntry.getAddress().equals(network.get(0).getPeerAddress())) {
				newClientsEntry = locationEntry;
			}
			assertNotEquals(locationEntry.getAddress(), network.get(5).getPeerAddress());
			assertNotEquals(locationEntry.getAddress(), network.get(6).getPeerAddress());
		}
		assertNotNull(newClientsEntry);
		assertTrue(newClientsEntry.isMaster());
	}	
	
	/**
	 * No other clients are or have been online.
	 */
	@Test
	public void noOtherClientsOrDeadClients() {
		Locations fakedLocations = new Locations(userId);
		
		runProcessStep(fakedLocations);

		assertEquals(1, result.getLocationEntries().size());
		assertEquals(network.get(0).getPeerAddress(), result.getLocationEntries().iterator().next().getAddress());
		assertTrue(result.getLocationEntries().iterator().next().isMaster());
	}
	
	/**
	 * No client is responding.
	 */
	@Test
	public void allOtherClientsAreDead() {
		Locations fakedLocations = new Locations(userId);
		// not responding nodes
		fakedLocations.addEntry(new LocationEntry(network.get(5).getPeerAddress(), true));
		fakedLocations.addEntry(new LocationEntry(network.get(6).getPeerAddress(), false));
		
		runProcessStep(fakedLocations);

		assertEquals(1, result.getLocationEntries().size());
		LocationEntry newClientsEntry = null;
		for (LocationEntry locationEntry : result.getLocationEntries()) {
			if (locationEntry.getAddress().equals(network.get(0).getPeerAddress())) {
				newClientsEntry = locationEntry;
			}
			assertNotEquals(locationEntry.getAddress(), network.get(5).getPeerAddress());
			assertNotEquals(locationEntry.getAddress(), network.get(6).getPeerAddress());
		}
		assertNotNull(newClientsEntry);
		assertTrue(newClientsEntry.isMaster());
	}
	
	/**
	 * Helper for running a process with a single {@link ContactPeersStep} step. Method waits till process
	 * successfully finishes.
	 * 
	 * @param fakedLocations
	 *            locations which the {@link ContactPeersStep} step has to handle
	 */
	private void runProcessStep(Locations fakedLocations) {
		// initialize the process and the one and only step to test
		TestProcessContatctPeers process = new TestProcessContatctPeers(fakedLocations, network.get(0));
		process.setNextStep(new ContactPeersStep() {
			// override this to disable the triggering of the further process steps
			@Override
			protected void nextStep(Locations newLocations) {
				// store newly generated location map
				result = newLocations;
				// stop the process
				getProcess().setNextStep(null);
			}
		});
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		int waitingTime = (int) (H2HConstants.CONTACT_PEERS_AWAIT_MS/1000) + 10;
		H2HWaiter waiter = new H2HWaiter(waitingTime);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
	}

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

	/**
	 * A sub-class of {@link PostLoginProcess} to simplify the context initialization.
	 * 
	 * @author Seppi
	 */
	private class TestProcessContatctPeers extends PostLoginProcess {
		public TestProcessContatctPeers(Locations locations, NetworkManager networkManager) {
			super(null, null, locations, networkManager, null, null);
		}
	}
	
	/**
	 * A message reply handler which rejects all message.
	 * 
	 * @author Seppi
	 */
	private static class DenyingMessageReplyHandler implements ObjectDataReply {
		@Override
		public Object reply(PeerAddress sender, Object request) throws Exception {
			return null;
		}
	}
}
