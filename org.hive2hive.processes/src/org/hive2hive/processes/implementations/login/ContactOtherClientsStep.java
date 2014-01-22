package org.hive2hive.processes.implementations.login;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkUtils;
import org.hive2hive.core.network.messages.IBaseMessageListener;
import org.hive2hive.core.network.messages.direct.ContactPeerMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.processes.framework.ProcessUtil;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.PutUserLocationsStep;
import org.hive2hive.processes.implementations.context.LoginProcessContext;

// TODO this class should be split up into multiple steps
public class ContactOtherClientsStep extends ProcessStep implements
		IResponseCallBackHandler {

	private final ConcurrentHashMap<PeerAddress, String> evidences = new ConcurrentHashMap<PeerAddress, String>();
	private final ConcurrentHashMap<PeerAddress, Boolean> responses = new ConcurrentHashMap<PeerAddress, Boolean>();
	private boolean isUpdated;

	private final LoginProcessContext context;
	private final NetworkManager networkManager;

	public ContactOtherClientsStep(LoginProcessContext context,
			NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {

		Locations locations = context.consumeLocations();
		
		if (!locations.getPeerAddresses().isEmpty()) {
			
			for (PeerAddress address : locations.getPeerAddresses()) {

				// contact all other clients (exclude self)
				if (!address.equals(networkManager.getPeerAddress())) {

					String evidence = UUID.randomUUID().toString();
					evidences.put(address, evidence);

					ContactPeerMessage message = new ContactPeerMessage(address,
							evidence);
					message.setCallBackHandler(this);
					networkManager.sendDirect(message,
							networkManager.getPublicKey(),
							new FastFailMessageListener(address));

				}
			}
			
			// wait for messages to be responded
			ProcessUtil.wait(this, H2HConstants.CONTACT_PEERS_AWAIT_MS);
		} 
		
		updateLocations();
	}

	@Override
	public void handleResponseMessage(ResponseMessage responseMessage) {

		// TODO deal with delayed responses

		// verify response
		if (evidences.get(responseMessage.getSenderAddress()).equals(
				(String) responseMessage.getContent())) {

			responses.put(responseMessage.getSenderAddress(), true);
			if (responses.size() >= context.consumeLocations()
					.getPeerAddresses().size()) {
				updateLocations();
			}
		} else {
			// TODO deal with invalid responses
		}
	}

	private synchronized void updateLocations() {
		if (!isUpdated) {
			isUpdated = true;

			Locations updatedLocations = new Locations(context
					.consumeLocations().getUserId());
			updatedLocations.setBasedOnKey(context.consumeLocations()
					.getBasedOnKey());
			updatedLocations.setVersionKey(context.consumeLocations()
					.getVersionKey());

			// add addresses that responded and self
			for (PeerAddress address : responses.keySet()) {
				if (responses.get(address)) {
					updatedLocations.addPeerAddress(address);
				}
			}
			updatedLocations.addPeerAddress(networkManager.getPeerAddress());
			context.provideLocations(updatedLocations);

			// evaluate if master
			List<PeerAddress> clientAddresses = new ArrayList<PeerAddress>(
					updatedLocations.getPeerAddresses());

			if (NetworkUtils.choseFirstPeerAddress(clientAddresses).equals(
					networkManager.getPeerAddress())) {

				// TODO find better solution to put alternative steps (i.e., in
				// ProcessFactory)

				if (getParent() != null) {

					int index = getParent().getComponents().indexOf(this);
					getParent().insert(
							++index,
							new PutUserLocationsStep(updatedLocations, context
									.consumeUserProfile().getProtectionKeys(),
									networkManager));
					getParent().insert(++index, new SynchronizeFilesStep());

				} else {
					throw new NullPointerException("Step has no parent.");
				}
			}
		}
	}

	private class FastFailMessageListener implements IBaseMessageListener {

		private final PeerAddress receiver;

		public FastFailMessageListener(PeerAddress receiver) {
			this.receiver = receiver;
		}

		@Override
		public void onSuccess() {
			// ignore
		}

		@Override
		public void onFailure() {
			responses.put(receiver, false);
		}

	}
}
