package org.hive2hive.core.test.process.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkUtils;
import org.hive2hive.core.process.login.ContactPeersStep;
import org.hive2hive.core.process.login.LoginProcess;
import org.hive2hive.core.process.login.LoginProcessContext;
import org.hive2hive.core.process.login.SessionParameters;
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
 * node 1, 2, 3 are other alive client nodes
 * node 4, 5 are not responding client nodes
 * 
 * ranking from smallest to greatest node id:
 * C, B, A, F, E, G, A, D
 * 
 * @author Seppi, Nico
 */
public class ContactPeersStepTest extends H2HJUnitTest {

	private static final int networkSize = 6;
	// in seconds
	private static final int maxWaitingTimeTillFail = 3;
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
		// assign to a subset of the client nodes a rejecting message reply handler
		network.get(4).getConnection().getPeer().setObjectDataReply(new DenyingMessageReplyHandler());
		network.get(5).getConnection().getPeer().setObjectDataReply(new DenyingMessageReplyHandler());
	}

	private boolean isMasterClient(Locations locations, PeerAddress client) {
		ArrayList<PeerAddress> list = new ArrayList<PeerAddress>();
		list.addAll(locations.getPeerAddresses());
		PeerAddress master = NetworkUtils.choseFirstPeerAddress(list);
		return (master.equals(client));
	}

	/**
	 * All client nodes are alive.
	 * 
	 * @throws NoSessionException
	 */
	@Test
	public void allClientsAreAlive() throws NoSessionException {
		Locations fakedLocations = new Locations(userId);
		fakedLocations.addPeerAddress(network.get(0).getPeerAddress());
		// responding nodes
		fakedLocations.addPeerAddress(network.get(1).getPeerAddress());
		fakedLocations.addPeerAddress(network.get(2).getPeerAddress());
		fakedLocations.addPeerAddress(network.get(3).getPeerAddress());

		runProcessStep(fakedLocations, isMasterClient(fakedLocations, network.get(0).getPeerAddress()));

		assertEquals(4, result.getPeerAddresses().size());
		PeerAddress newClientsEntry = null;
		for (PeerAddress address : result.getPeerAddresses()) {
			if (address.equals(network.get(0).getPeerAddress())) {
				newClientsEntry = address;
				break;
			}
		}
		assertNotNull(newClientsEntry);
	}

	/**
	 * Some client nodes are offline.
	 * 
	 * @throws NoSessionException
	 */
	@Test
	public void notAllClientsAreAlive() throws NoSessionException {
		Locations fakedLocations = new Locations(userId);
		fakedLocations.addPeerAddress(network.get(0).getPeerAddress());
		fakedLocations.addPeerAddress(network.get(1).getPeerAddress());
		// not responding nodes
		fakedLocations.addPeerAddress(network.get(4).getPeerAddress());
		fakedLocations.addPeerAddress(network.get(5).getPeerAddress());

		runProcessStep(fakedLocations, isMasterClient(fakedLocations, network.get(0).getPeerAddress()));

		assertEquals(2, result.getPeerAddresses().size());
		PeerAddress newClientsEntry = null;
		for (PeerAddress address : result.getPeerAddresses()) {
			if (address.equals(network.get(0).getPeerAddress())) {
				newClientsEntry = address;
				break;
			}
		}
		assertNotNull(newClientsEntry);
	}

	/**
	 * No other clients are or have been online.
	 * 
	 * @throws NoSessionException
	 */
	@Test
	public void noOtherClientsOrDeadClients() throws NoSessionException {
		Locations fakedLocations = new Locations(userId);
		fakedLocations.addPeerAddress(network.get(0).getPeerAddress());

		runProcessStep(fakedLocations, true);

		assertEquals(1, result.getPeerAddresses().size());
		assertEquals(network.get(0).getPeerAddress(), result.getPeerAddresses().iterator().next());
	}

	/**
	 * No client is responding.
	 * 
	 * @throws NoSessionException
	 */
	@Test
	public void allOtherClientsAreDead() throws NoSessionException {
		Locations fakedLocations = new Locations(userId);
		fakedLocations.addPeerAddress(network.get(0).getPeerAddress());
		// not responding nodes
		fakedLocations.addPeerAddress(network.get(4).getPeerAddress());
		fakedLocations.addPeerAddress(network.get(5).getPeerAddress());

		runProcessStep(fakedLocations, true);

		assertEquals(1, result.getPeerAddresses().size());
		assertEquals(network.get(0).getPeerAddress(), result.getPeerAddresses().iterator().next());
	}

	/**
	 * Received an empty location map.
	 * 
	 * @throws NoSessionException
	 */
	@Test
	public void emptyLocations() throws NoSessionException {
		Locations fakedLocations = new Locations(userId);

		runProcessStep(fakedLocations, true);

		assertEquals(1, result.getPeerAddresses().size());
		assertEquals(network.get(0).getPeerAddress(), result.getPeerAddresses().iterator().next());
	}

	/**
	 * Received a location map without own location entry.
	 * 
	 * @throws NoSessionException
	 */
	@Test
	public void notCompleteLocations() throws NoSessionException {
		Locations fakedLocations = new Locations(userId);
		fakedLocations.addPeerAddress(network.get(1).getPeerAddress());

		runProcessStep(fakedLocations, isMasterClient(fakedLocations, network.get(0).getPeerAddress()));

		assertEquals(2, result.getPeerAddresses().size());
		PeerAddress newClientsEntry = null;
		for (PeerAddress address : result.getPeerAddresses()) {
			if (address.equals(network.get(0).getPeerAddress())) {
				newClientsEntry = address;
				break;
			}
		}
		assertNotNull(newClientsEntry);
	}

	/**
	 * Helper for running a process with a single {@link ContactPeersStep} step. Method waits till process
	 * successfully finishes.
	 * 
	 * @param fakedLocations
	 *            locations which the {@link ContactPeersStep} step has to handle
	 * @throws NoSessionException
	 */
	private void runProcessStep(Locations fakedLocations, final boolean isMaster) throws NoSessionException {
		// initialize the process and the one and only step to test
		TestProcessContatctPeers process = new TestProcessContatctPeers(network.get(0));
		LoginProcessContext context = (LoginProcessContext) process.getContext();
		context.setLocations(fakedLocations);

		process.setNextStep(new ContactPeersStep() {
			// override this to disable the triggering of the further process steps
			@Override
			protected void nextStep(Locations newLocations) {
				// store newly generated location map
				result = newLocations;

				if (isMaster)
					assertTrue(((LoginProcessContext) getProcess().getContext()).isDefinedAsMaster());
				else
					assertFalse(((LoginProcessContext) getProcess().getContext()).isDefinedAsMaster());

				// stop the process
				getProcess().setNextStep(null);
			}
		});

		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		int waitingTime = (int) (H2HConstants.CONTACT_PEERS_AWAIT_MS / 1000) + maxWaitingTimeTillFail;
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
	 * A sub-class of {@link LoginProcess} to simplify the context initialization.
	 * 
	 * @author Seppi, Nico
	 */
	private class TestProcessContatctPeers extends LoginProcess {
		public TestProcessContatctPeers(NetworkManager networkManager) throws NoSessionException {
			super(NetworkTestUtil.generateRandomCredentials(), new SessionParameters(), networkManager);
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
