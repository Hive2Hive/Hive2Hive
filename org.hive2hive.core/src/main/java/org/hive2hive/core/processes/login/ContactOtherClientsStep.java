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
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkUtils;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.messages.MessageManager;
import org.hive2hive.core.network.messages.direct.ContactPeerMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.context.LoginProcessContext;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO this class should be split up into multiple steps
public class ContactOtherClientsStep extends ProcessStep<Void> implements IResponseCallBackHandler {

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
		this.setName(getClass().getName());
		this.context = context;
		this.networkManager = networkManager;
		this.messageManager = networkManager.getMessageManager();
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		PublicKeyManager keyManager;
		try {
			keyManager = networkManager.getSession().getKeyManager();
		} catch (NoSessionException ex) {
			throw new ProcessExecutionException(this, ex);
		}

		PublicKey ownPublicKey = keyManager.getOwnPublicKey();
		Locations locations = context.consumeLocations();

		sendBlocking(locations.getPeerAddresses(), ownPublicKey);

		locations.getPeerAddresses().clear();

		// add addresses that responded
		for (PeerAddress address : responses.keySet()) {
			if (responses.get(address)) {
				locations.addPeerAddress(address);
			}
		}
		// add self
		PeerAddress ownAddress = networkManager.getConnection().getPeer().peerAddress();
		logger.debug("Adding own peeraddress to locations file: {}", ownAddress);
		locations.addPeerAddress(ownAddress);

		// evaluate if initial
		List<PeerAddress> clientAddresses = new ArrayList<PeerAddress>(locations.getPeerAddresses());
		if (NetworkUtils.choseFirstPeerAddress(clientAddresses).equals(ownAddress)) {
			logger.debug("Node is master and needs to handle possible User Profile Tasks.");
			if (getParent() != null) {
				getParent().add(ProcessFactory.instance().createUserProfileTaskProcess(networkManager));
			}
		}

		return null;
	}

	private void sendBlocking(Set<PeerAddress> peerAddresses, PublicKey ownPublicKey) {
		waitForResponses = new CountDownLatch(peerAddresses.size());
		for (PeerAddress address : peerAddresses) {
			// contact all other clients (exclude self)
			if (!address.equals(networkManager.getConnection().getPeer().peerAddress())) {
				logger.debug("Sending contact message to check for aliveness to {}", address);
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

		isUpdated = true;
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
			logger.debug("Received valid response from {}", responseMessage.getSenderAddress());
			responses.put(responseMessage.getSenderAddress(), true);
			waitForResponses.countDown();
		} else {
			logger.error(
					"Received during liveness check of other clients a wrong evidence content. Responding node = '{}'.",
					responseMessage.getSenderAddress());
		}
	}

}
