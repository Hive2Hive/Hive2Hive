package org.hive2hive.core.process.login;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkUtils;
import org.hive2hive.core.network.messages.direct.ContactPeerMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.put.PutLocationStep;

/**
 * A process step which contacts according the location list ({@link PostLoginProcessContext#getLocations()}
 * all other client nodes and checks if they are alive and if there is a master selected (master handles the
 * user message queue). If not the node itself becomes the master node.
 * 
 * @author Christian, Nico, Seppi
 */
public class ContactPeersStep extends ProcessStep implements IResponseCallBackHandler {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(ContactPeersStep.class);

	private LoginProcessContext context;

	private ConcurrentHashMap<PeerAddress, Boolean> responses = new ConcurrentHashMap<PeerAddress, Boolean>();
	protected ConcurrentHashMap<PeerAddress, String> evidenceMap = new ConcurrentHashMap<PeerAddress, String>();

	/*
	 * A flag to indicate if the process step has already finished. It can happen that delayed response
	 * messages arrive but the process has already gone further.
	 */
	private boolean isExecuted = false;

	@Override
	public void start() {

		context = (LoginProcessContext) getProcess().getContext();

		PeerAddress myself = null;
		for (PeerAddress location : context.getLocations().getPeerAddresses()) {
			if (location.equals(getNetworkManager().getPeerAddress())) {
				myself = location;
			}
		}
		if (myself != null)
			context.getLocations().getPeerAddresses().remove(myself);

		// check if other client nodes has to be contacted
		if (!context.getLocations().getPeerAddresses().isEmpty()) {
			// set timer to guarantee that the process step goes further because not coming response messages
			// may block the step
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					updateLocationsMap();
				}
			}, H2HConstants.CONTACT_PEERS_AWAIT_MS);

			// contact all peers
			for (PeerAddress address : context.getLocations().getPeerAddresses()) {
				sendContactPeerMessage(address);
			}
		} else {
			updateLocationsMap();
		}
	}

	/*
	 * Necessary method to enable single step testing. A test can overwrite this method and can send preperad
	 * messages.
	 */
	protected void sendContactPeerMessage(PeerAddress address) {
		// generate random verification content
		String evidenceContent = UUID.randomUUID().toString();
		// create a liveness check message
		ContactPeerMessage contactMsg = new ContactPeerMessage(address, evidenceContent);
		evidenceMap.put(address, evidenceContent);
		// the process step is expecting a response
		contactMsg.setCallBackHandler(this);
		// send direct
		boolean success = getNetworkManager().sendDirect(contactMsg, getNetworkManager().getPublicKey());
		if (!success) {
			responses.put(address, false);
		}
	}

	@Override
	public void rollBack() {
		// nothing to roll back
		getProcess().nextRollBackStep();
	}

	private synchronized void updateLocationsMap() {
		// ensure this method is executed only once
		if (!isExecuted) {
			isExecuted = true;

			// replace the locations map
			Locations newLocations = new Locations(context.getLocations().getUserId());
			newLocations.setBasedOnKey(context.getLocations().getBasedOnKey());
			newLocations.setVersionKey(context.getLocations().getVersionKey());
			List<PeerAddress> listToDetectMaster = new ArrayList<PeerAddress>();

			// add contacts that responded
			for (PeerAddress peerAddress : responses.keySet()) {
				// check if response was ok or failed
				if (responses.get(peerAddress)) {
					newLocations.addPeerAddress(peerAddress);
					listToDetectMaster.add(peerAddress);
				} else {
					logger.warn(String
							.format("A dead client node detected. peer address = '%s'", peerAddress));
				}
			}

			// evaluate if master
			boolean isDefinedAsMaster = false;
			listToDetectMaster.add(getNetworkManager().getPeerAddress());
			// do that through selecting the node with the lowest peer address
			if (NetworkUtils.choseFirstPeerAddress(listToDetectMaster).equals(
					getNetworkManager().getPeerAddress())) {
				logger.info(String.format(
						"Node has been selected as master for handling user message. node id = '%s'",
						getNetworkManager().getNodeId()));
				isDefinedAsMaster = true;
			}
			// add myself
			newLocations.addPeerAddress(getNetworkManager().getPeerAddress());
			// put all into context
			context.setIsElectedMaster(isDefinedAsMaster);
			context.setLocations(newLocations);

			// continue the process
			nextStep(newLocations);
		}
	}

	/*
	 * Necessary method to enable single step testing. A test can overwrite this method and avoid triggering
	 * next steps.
	 */
	protected void nextStep(Locations newLocations) {
		// 1. Put the new location map
		// 2. Synchronize files with network
		SynchronizeFilesStep nextStep = new SynchronizeFilesStep();
		PutLocationStep putStep = new PutLocationStep(newLocations, context.getUserProfile()
				.getProtectionKeys(), nextStep);
		getProcess().setNextStep(putStep);
	}

	@Override
	public synchronized void handleResponseMessage(ResponseMessage responseMessage) {
		if (isExecuted) {
			logger.warn(String
					.format("Received a delayed contact peer response message, which gets ignored. peer address = '%s'",
							responseMessage.getSenderAddress()));
			// TODO notify delayed response client nodes about removing him from location map
			return;
		}

		// verify reply
		if (evidenceMap.get(responseMessage.getSenderAddress()).equals((String) responseMessage.getContent())) {
			// add to map and label success
			responses.put(responseMessage.getSenderAddress(), true);
			// check if all peers responded
			if (responses.size() >= context.getLocations().getPeerAddresses().size()) {
				// all peers answered
				updateLocationsMap();
			}
		} else {
			logger.error(String
					.format("Received during liveness check of other clients a wrong evidence content. responding node = '%s'",
							responseMessage.getSenderAddress()));
		}
	}
}
