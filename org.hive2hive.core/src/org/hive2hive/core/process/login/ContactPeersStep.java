package org.hive2hive.core.process.login;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.tomp2p.futures.FutureRemove;
import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.LocationEntry;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.put.PutLocationStep;

public class ContactPeersStep extends ProcessStep {

	private final Locations currentLocations;
	private Map<PeerAddress, Void> responses;

	private boolean isExecuted;

	public ContactPeersStep(Locations locations) {
		this.currentLocations = locations;
		this.responses = new ConcurrentHashMap<PeerAddress, Void>();
	}

	@Override
	public void start() {
		// TODO Chris: encrypt messages

		// set timer to wait for callbacks
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				updateLocationsMap();

			}
		}, H2HConstants.CONTACT_PEERS_AWAIT_MS);

		// contact all peers
		for (LocationEntry entry : currentLocations.getLocationEntries()) {

			// generate random verification content
			final String evidenceContent = UUID.randomUUID().toString();

			ContactPeerMessage contactMsg = new ContactPeerMessage(entry.getAddress(), evidenceContent);

			contactMsg.setCallBackHandler(new HandleContactReply(evidenceContent));
			getNetworkManager().sendDirect(contactMsg);
		}
	}

	private synchronized void updateLocationsMap() {

		// ensure this method is executed only once
		if (!isExecuted) {
			isExecuted = true;

			// replace the locations map
			LocationEntry master = currentLocations.getMaster();
			Locations newLocations = new Locations(currentLocations.getUserId());

			// add contacts that responded
			for (PeerAddress peerAddress : responses.keySet()) {

				// check if this peer is master
				boolean isMaster = false;
				if (master != null && master.getAddress().equals(peerAddress)) {
					isMaster = true;
				}

				newLocations.addEntry(new LocationEntry(peerAddress, isMaster));

			}

			// add myself, set me as master if none exists
			boolean isDefinedAsMaster = newLocations.getMaster() == null;
			newLocations.addEntry(new LocationEntry(getNetworkManager().getPeerAddress(), isDefinedAsMaster));
			((PostLoginProcess) getProcess()).setIsDefinedAsMaster(isDefinedAsMaster);
			((PostLoginProcess) getProcess()).setNewLocations(newLocations);
			
			// evaluate whether a put is necessary
			SynchronizeFilesStep nextStep = new SynchronizeFilesStep();
			if (isPutNecessary(newLocations)) {
				PutLocationStep putStep = new PutLocationStep(newLocations, nextStep);
				getProcess().setNextStep(putStep);
			} else {
				getProcess().setNextStep(nextStep);
			}

		}
	}

	private boolean isPutNecessary(Locations newLocations) {

		if (currentLocations.getLocationEntries().size() != newLocations.getLocationEntries().size())
			return true;

		LocationEntry currentMaster = currentLocations.getMaster();
		if (currentMaster == null)
			return true;

		LocationEntry newMaster = newLocations.getMaster();
		if (!newMaster.getAddress().equals(currentMaster.getAddress()))
			return true;

		return false;
	}

	@Override
	public void rollBack() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleRemovalResult(FutureRemove future) {
		// TODO Auto-generated method stub

	}

	private final class HandleContactReply implements IResponseCallBackHandler {

		private final String evidenceContent;

		public HandleContactReply(String evidenceContent) {
			this.evidenceContent = evidenceContent;
		}

		@Override
		public void handleResponseMessage(ResponseMessage responseMessage) {

			// verify reply
			if (evidenceContent.equals((String) responseMessage.getContent())) {

				// add to map
				responses.put(responseMessage.getSenderAddress(), null);
				int totalResponses = responses.size();
				if (totalResponses >= currentLocations.getLocationEntries().size()) {
					// all peers answered
					updateLocationsMap();
				}
			}

		}
	}
}
