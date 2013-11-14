package org.hive2hive.core.process.login;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.LocationEntry;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.usermessages.direct.ContactPeerUserMessage;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.put.PutLocationStep;

/**
 * 
 * @author Christian, Nico, Seppi
 *
 */
// TODO Seppi rebuild the whole thing
public class ContactPeersStep extends ProcessStep {

	private final PostLoginProcessContext context;
	
	// TODO change this to a synchronized set
	private Set<PeerAddress> responses = new HashSet<PeerAddress>();
	private boolean isExecuted = false;
	
	public ContactPeersStep(){
		context = (PostLoginProcessContext) getProcess().getContext();
	}

	@Override
	public void start() {
		// set timer to wait for callbacks
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				updateLocationsMap();
			}
		}, H2HConstants.CONTACT_PEERS_AWAIT_MS);

		// contact all peers
		for (LocationEntry entry : context.getLocations().getLocationEntries()) {
			// generate random verification content
			final String evidenceContent = UUID.randomUUID().toString();
			// create a liveness check message
			ContactPeerUserMessage contactMsg = new ContactPeerUserMessage(entry.getAddress(), evidenceContent);
			contactMsg.setCallBackHandler(new HandleContactReply(evidenceContent));
			// send direct
			getNetworkManager().sendDirect(contactMsg, getNetworkManager().getKeyPair().getPublic(), null);
		}
	}

	@Override
	public void rollBack() {
		// nothing to roll back
	}

	private synchronized void updateLocationsMap() {
		// ensure this method is executed only once
		if (!isExecuted) {
			isExecuted = true;

			// replace the locations map
			LocationEntry master = context.getLocations().getMaster();
			Locations newLocations = new Locations(context.getLocations().getUserId());

			// add contacts that responded
			for (PeerAddress peerAddress : responses) {
				// check if this peer is master
				boolean isMaster = false;
				if (master != null && master.getAddress().equals(peerAddress)) {
					isMaster = true;
				}
				newLocations.addEntry(new LocationEntry(peerAddress, isMaster));
			}

			// add myself, set me as master if none exists
			boolean isDefinedAsMaster = (newLocations.getMaster() == null);
			newLocations.addEntry(new LocationEntry(getNetworkManager().getPeerAddress(), isDefinedAsMaster));
			context.setIsElectedMaster(isDefinedAsMaster);
			Locations oldLocations = context.getLocations();
			context.setNewLocations(newLocations);
			
			// evaluate whether a put is necessary
			SynchronizeFilesStep nextStep = new SynchronizeFilesStep();
			if (isPutNecessary(oldLocations, newLocations)) {
				PutLocationStep putStep = new PutLocationStep(newLocations, nextStep);
				getProcess().setNextStep(putStep);
			} else {
				getProcess().setNextStep(nextStep);
			}
		}
	}

	// TODO this comparison is not complete
	private boolean isPutNecessary(Locations oldLocations, Locations newLocations) {
		if (oldLocations.getLocationEntries().size() != newLocations.getLocationEntries().size())
			return true;

		LocationEntry currentMaster = oldLocations.getMaster();
		if (currentMaster == null)
			return true;

		LocationEntry newMaster = newLocations.getMaster();
		if (!newMaster.getAddress().equals(currentMaster.getAddress()))
			return true;

		return false;
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
				responses.add(responseMessage.getSenderAddress());
				// check if all peers responded
				if (responses.size() >= context.getLocations().getLocationEntries().size()) {
					// all peers answered
					updateLocationsMap();
				}
			}
		}
	}
}
