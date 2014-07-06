package org.hive2hive.core.processes.login;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkUtils;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.messages.MessageManager;
import org.hive2hive.core.network.messages.direct.ContactPeerMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.context.LoginProcessContext;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO this class should be split up into multiple steps
public class ContactOtherClientsStep extends ProcessStep implements IResponseCallBackHandler {

	private static final Logger logger = LoggerFactory.getLogger(ContactOtherClientsStep.class);

	private final Map<PeerAddress, String> evidences = new ConcurrentHashMap<PeerAddress, String>();
	private final Map<PeerAddress, Boolean> responses = new ConcurrentHashMap<PeerAddress, Boolean>();
	private CountDownLatch waitForResponses;
	private boolean isUpdated = false;

	private final LoginProcessContext context;
	private final MessageManager messageManager;
	private final NetworkManager networkManager;

	public ContactOtherClientsStep(LoginProcessContext context, NetworkManager networkManager)
			throws NoPeerConnectionException {
		this.context = context;
		this.networkManager = networkManager;
		this.messageManager = networkManager.getMessageManager();
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		PublicKeyManager keyManager;
		try {
			keyManager = networkManager.getSession().getKeyManager();
		} catch (NoSessionException e) {
			throw new ProcessExecutionException("No session yet");
		}

		PublicKey ownPublicKey = keyManager.getOwnPublicKey();
		Locations locations = context.consumeUserLocations();
		if (locations != null && locations.getPeerAddresses() != null) {
			sendBlocking(locations.getPeerAddresses(), ownPublicKey);
		}
		updateLocations();
	}

	private void sendBlocking(Set<PeerAddress> peerAddresses, PublicKey ownPublicKey) {
		waitForResponses = new CountDownLatch(peerAddresses.size());
		for (PeerAddress address : peerAddresses) {
			// contact all other clients (exclude self)
			if (!address.equals(networkManager.getConnection().getPeer().getPeerAddress())) {
				String evidence = UUID.randomUUID().toString();
				evidences.put(address, evidence);

				ContactPeerMessage message = new ContactPeerMessage(address, evidence);
				message.setCallBackHandler(this);

				// TODO this is blocking, should be parallel (asynchronous)
				boolean success = messageManager.sendDirect(message, ownPublicKey);
				if (!success) {
					responses.put(address, false);
				}
			}
		}

		// wait (blocking) until all responses are here or the time's up
		try {
			waitForResponses.await(H2HConstants.CONTACT_PEERS_AWAIT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.error("Could not wait the given time for the clients to respond.", e);
		}
	}

	@Override
	public void handleResponseMessage(ResponseMessage responseMessage) {
		if (isUpdated) {
			// TODO notify delayed response client nodes about removing him from location map
			logger.warn("Received a delayed contact peer response message, which gets ignored. Peer address = '{}'.",
					responseMessage.getSenderAddress());
			return;
		}

		// verify response
		if (evidences.get(responseMessage.getSenderAddress()).equals((String) responseMessage.getContent())) {
			responses.put(responseMessage.getSenderAddress(), true);
			waitForResponses.countDown();
		} else {
			logger.error(
					"Received during liveness check of other clients a wrong evidence content. Responding node = '{}'.",
					responseMessage.getSenderAddress());
		}
	}

	private void updateLocations() {
		isUpdated = true;

		Locations oldLocations = context.consumeUserLocations();
		Locations updatedLocations;
		if (oldLocations == null) {
			// TODO check if based-on key can be omitted!
			updatedLocations = new Locations(networkManager.getUserId());
		} else {
			updatedLocations = new Locations(oldLocations.getUserId());
			updatedLocations.setBasedOnKey(oldLocations.getBasedOnKey());
			updatedLocations.setVersionKey(oldLocations.getVersionKey());
		}

		// add addresses that responded
		for (PeerAddress address : responses.keySet()) {
			if (responses.get(address)) {
				updatedLocations.addPeerAddress(address);
			}
		}

		// add self
		updatedLocations.addPeerAddress(networkManager.getConnection().getPeer().getPeerAddress());

		// update context for future use
		context.provideUserLocations(updatedLocations);

		// evaluate if initial
		List<PeerAddress> clientAddresses = new ArrayList<PeerAddress>(updatedLocations.getPeerAddresses());
		if (NetworkUtils.choseFirstPeerAddress(clientAddresses).equals(
				networkManager.getConnection().getPeer().getPeerAddress())) {
			logger.debug("Node is master and needs to handle possible User Profile Tasks.");
			getParent().add(ProcessFactory.instance().createUserProfileTaskStep(networkManager));
		}
	}

}
