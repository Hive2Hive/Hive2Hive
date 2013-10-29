package org.hive2hive.core.process;

import java.util.ArrayList;
import java.util.List;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;

/**
 * Verifies a put by checking if all replicas accepted the content. If not, the version is compared and the
 * newest version is taken. Old deprecated versions are cleaned up.
 * After the verification, the listeners on this object are called.
 * 
 * This class is needed because there could emerge inconsistent states when using replication
 * 
 * @author Nico
 * 
 */
public class PutVerificationListener extends BaseFutureAdapter<FutureDHT> {

	private final static Logger logger = H2HLoggerFactory.getLogger(PutVerificationListener.class);

	private final NetworkManager networkManager;
	private final String locationKey;
	private final String contentKey;
	private final NetworkContent expectedData;
	private final ProcessStep processStep;

	public PutVerificationListener(NetworkManager networkManager, ProcessStep processStep, String locationKey, String contentKey,
			NetworkContent expectedData) {
		this.networkManager = networkManager;
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.expectedData = expectedData;
		this.processStep = processStep;
	}

	@Override
	public void operationComplete(FutureDHT future) throws Exception {
		final FutureDHT putFuture = future;
		
		// store the future for notifying the listeners later with this
		logger.debug("Start verification of put(" + locationKey + ", " + contentKey + ")");

		// check if on all peers the data has been stored
		// TODO it seems that TomP2P gives no feedback when a peer was accessed --> null
		for (PeerAddress peerAddress : putFuture.getRawKeys().keySet()) {
			// TODO compare here the time stamp / version key to check if correct data was stored
			if (future.getRawKeys().get(peerAddress) == null
					|| future.getRawKeys().get(peerAddress).isEmpty() ) {
				logger.warn("Version conflict detected after put.");
				// TODO rollback
			} else {
				// logger.debug("ok");
			}
		}

		FutureDHT getFuture = networkManager.getGlobal(locationKey, contentKey);
		getFuture.addListener(new BaseFutureAdapter<FutureDHT>() {
			@Override
			public void operationComplete(FutureDHT future) throws Exception {
				// TODO: verify with expected data and the timestamps
				notifyProcessStep(putFuture);
			}
		});
	}

	private void notifyProcessStep(FutureDHT future){
		logger.debug("Verification for put(" + locationKey + ", " + contentKey + ") complete");
		processStep.handlePutResult(future);
	}

}
