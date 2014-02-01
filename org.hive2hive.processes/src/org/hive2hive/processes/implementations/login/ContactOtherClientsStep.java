package org.hive2hive.processes.implementations.login;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkUtils;
import org.hive2hive.core.network.messages.MessageManager;
import org.hive2hive.core.network.messages.direct.ContactPeerMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.context.LoginProcessContext;

// TODO this class should be split up into multiple steps
public class ContactOtherClientsStep extends ProcessStep implements IResponseCallBackHandler {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(ContactOtherClientsStep.class);

	private final ConcurrentHashMap<PeerAddress, String> evidences = new ConcurrentHashMap<PeerAddress, String>();
	private final ConcurrentHashMap<PeerAddress, Boolean> responses = new ConcurrentHashMap<PeerAddress, Boolean>();
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
	protected void doExecute() throws InvalidProcessStateException {
		Locations locations = context.consumeLocations();
		waitForResponses = new CountDownLatch(locations.getPeerAddresses().size());
		if (!locations.getPeerAddresses().isEmpty()) {
			for (PeerAddress address : locations.getPeerAddresses()) {
				// contact all other clients (exclude self)
				if (!address.equals(networkManager.getPeerAddress())) {
					String evidence = UUID.randomUUID().toString();
					evidences.put(address, evidence);

					ContactPeerMessage message = new ContactPeerMessage(address, evidence);
					message.setCallBackHandler(this);

					// TODO this is blocking, should be parallel (asynchronous)
					boolean success = messageManager.sendDirect(message, networkManager.getPublicKey());
					if (!success) {
						responses.put(address, false);
					}
				}
			}
		}

		// wait (blocking) until all responses are here or the time's up
		try {
			waitForResponses.await(H2HConstants.CONTACT_PEERS_AWAIT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.error("Could not wait the given time for the clients to respond", e);
		}
		updateLocations();
	}

	@Override
	public void handleResponseMessage(ResponseMessage responseMessage) {
		if (isUpdated) {
			// TODO notify delayed response client nodes about removing him from location map
			logger.warn(String
					.format("Received a delayed contact peer response message, which gets ignored. peer address = '%s'",
							responseMessage.getSenderAddress()));
			return;
		}

		// verify response
		if (evidences.get(responseMessage.getSenderAddress()).equals((String) responseMessage.getContent())) {
			responses.put(responseMessage.getSenderAddress(), true);
			waitForResponses.countDown();
		} else {
			logger.error(String
					.format("Received during liveness check of other clients a wrong evidence content. responding node = '%s'",
							responseMessage.getSenderAddress()));
		}
	}

	private void updateLocations() {
		isUpdated = true;

		Locations updatedLocations = new Locations(context.consumeLocations().getUserId());
		updatedLocations.setBasedOnKey(context.consumeLocations().getBasedOnKey());
		updatedLocations.setVersionKey(context.consumeLocations().getVersionKey());

		// add addresses that responded and self
		for (PeerAddress address : responses.keySet()) {
			if (responses.get(address)) {
				updatedLocations.addPeerAddress(address);
			}
		}
		updatedLocations.addPeerAddress(networkManager.getPeerAddress());
		context.provideLocations(updatedLocations);

		// evaluate if master
		List<PeerAddress> clientAddresses = new ArrayList<PeerAddress>(updatedLocations.getPeerAddresses());

		if (NetworkUtils.choseFirstPeerAddress(clientAddresses).equals(networkManager.getPeerAddress())) {
			context.setIsMaster(true);
		} else {
			context.setIsMaster(false);
		}
	}

}
