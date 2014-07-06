package org.hive2hive.core.processes.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.NetworkUtils;
import org.hive2hive.core.processes.context.LoginProcessContext;
import org.hive2hive.core.processes.login.ContactOtherClientsStep;
import org.hive2hive.processframework.concretes.SequentialProcess;
import org.hive2hive.processframework.util.TestExecutionUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class to check if the {@link ContactOtherClientsStep} process step works correctly. Only this process
 * step
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
public class ContactOtherClientsStepTest extends H2HJUnitTest {

	private static final int networkSize = 6;
	// in seconds
	private static List<NetworkManager> network;
	private static String userId = "user id";

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = ContactOtherClientsStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
		// assign to each node the same key pair (simulating same user)
		NetworkTestUtil.setSameSession(network);
		// assign to a subset of the client nodes a rejecting message reply handler
		network.get(4).getConnection().getPeer().setObjectDataReply(new DenyingMessageReplyHandler());
		network.get(5).getConnection().getPeer().setObjectDataReply(new DenyingMessageReplyHandler());
	}

	private boolean isInitialClient(Locations locations, PeerAddress client) {
		ArrayList<PeerAddress> list = new ArrayList<PeerAddress>();
		list.addAll(locations.getPeerAddresses());
		PeerAddress initial = NetworkUtils.choseFirstPeerAddress(list);
		return (initial.equals(client));
	}

	/**
	 * All client nodes are alive.
	 * 
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 */
	@Test
	public void allClientsAreAlive() throws NoSessionException, NoPeerConnectionException {
		Locations fakedLocations = new Locations(userId);
		fakedLocations.addPeerAddress(network.get(0).getConnection().getPeer().getPeerAddress());
		// responding nodes
		fakedLocations.addPeerAddress(network.get(1).getConnection().getPeer().getPeerAddress());
		fakedLocations.addPeerAddress(network.get(2).getConnection().getPeer().getPeerAddress());
		fakedLocations.addPeerAddress(network.get(3).getConnection().getPeer().getPeerAddress());

		Locations result = runProcessStep(fakedLocations,
				isInitialClient(fakedLocations, network.get(0).getConnection().getPeer().getPeerAddress()));

		assertEquals(4, result.getPeerAddresses().size());
		PeerAddress newClientsEntry = null;
		for (PeerAddress address : result.getPeerAddresses()) {
			if (address.equals(network.get(0).getConnection().getPeer().getPeerAddress())) {
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
	 * @throws NoPeerConnectionException
	 */
	@Test
	public void notAllClientsAreAlive() throws NoSessionException, NoPeerConnectionException {
		Locations fakedLocations = new Locations(userId);
		fakedLocations.addPeerAddress(network.get(0).getConnection().getPeer().getPeerAddress());
		fakedLocations.addPeerAddress(network.get(1).getConnection().getPeer().getPeerAddress());
		// not responding nodes
		fakedLocations.addPeerAddress(network.get(4).getConnection().getPeer().getPeerAddress());
		fakedLocations.addPeerAddress(network.get(5).getConnection().getPeer().getPeerAddress());

		Locations result = runProcessStep(fakedLocations,
				isInitialClient(fakedLocations, network.get(0).getConnection().getPeer().getPeerAddress()));

		assertEquals(2, result.getPeerAddresses().size());
		PeerAddress newClientsEntry = null;
		for (PeerAddress address : result.getPeerAddresses()) {
			if (address.equals(network.get(0).getConnection().getPeer().getPeerAddress())) {
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
	 * @throws NoPeerConnectionException
	 */
	@Test
	public void noOtherClientsOrDeadClients() throws NoSessionException, NoPeerConnectionException {
		Locations fakedLocations = new Locations(userId);
		fakedLocations.addPeerAddress(network.get(0).getConnection().getPeer().getPeerAddress());

		Locations result = runProcessStep(fakedLocations, true);

		assertEquals(1, result.getPeerAddresses().size());
		assertEquals(network.get(0).getConnection().getPeer().getPeerAddress(), result.getPeerAddresses().iterator().next());
	}

	/**
	 * No client is responding.
	 * 
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 */
	@Test
	public void allOtherClientsAreDead() throws NoSessionException, NoPeerConnectionException {
		Locations fakedLocations = new Locations(userId);
		fakedLocations.addPeerAddress(network.get(0).getConnection().getPeer().getPeerAddress());
		// not responding nodes
		fakedLocations.addPeerAddress(network.get(4).getConnection().getPeer().getPeerAddress());
		fakedLocations.addPeerAddress(network.get(5).getConnection().getPeer().getPeerAddress());

		Locations result = runProcessStep(fakedLocations, true);

		assertEquals(1, result.getPeerAddresses().size());
		assertEquals(network.get(0).getConnection().getPeer().getPeerAddress(), result.getPeerAddresses().iterator().next());
	}

	/**
	 * Received an empty location map.
	 * 
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 */
	@Test
	public void emptyLocations() throws NoSessionException, NoPeerConnectionException {
		Locations fakedLocations = new Locations(userId);

		Locations result = runProcessStep(fakedLocations, true);

		assertEquals(1, result.getPeerAddresses().size());
		assertEquals(network.get(0).getConnection().getPeer().getPeerAddress(), result.getPeerAddresses().iterator().next());
	}

	/**
	 * Received a location map without own location entry.
	 * 
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 */
	@Test
	public void notCompleteLocations() throws NoSessionException, NoPeerConnectionException {
		Locations fakedLocations = new Locations(userId);
		fakedLocations.addPeerAddress(network.get(1).getConnection().getPeer().getPeerAddress());

		Locations result = runProcessStep(fakedLocations,
				isInitialClient(fakedLocations, network.get(0).getConnection().getPeer().getPeerAddress()));

		assertEquals(2, result.getPeerAddresses().size());
		PeerAddress newClientsEntry = null;
		for (PeerAddress address : result.getPeerAddresses()) {
			if (address.equals(network.get(0).getConnection().getPeer().getPeerAddress())) {
				newClientsEntry = address;
				break;
			}
		}
		assertNotNull(newClientsEntry);
	}

	/**
	 * Helper for running a process with a single {@link ContactOtherClientsStep} step. Method waits until
	 * process successfully finishes.
	 * 
	 * @param fakedLocations
	 *            locations which the {@link ContactOtherClientsStep} step has to handle
	 * @return the updated locations
	 * @throws NoSessionException
	 * @throws NoPeerConnectionException
	 */
	private Locations runProcessStep(Locations fakedLocations, final boolean isMaster) throws NoSessionException,
			NoPeerConnectionException {
		// initialize the process and the one and only step to test

		LoginProcessContext context = new LoginProcessContext(null);
		context.provideUserLocations(fakedLocations);
		ContactOtherClientsStep processStep = new ContactOtherClientsStep(context, network.get(0));
		SequentialProcess process = new SequentialProcess();
		process.add(processStep);
		TestExecutionUtil.executeProcess(process);

		return context.consumeUserLocations();
	}

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
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
