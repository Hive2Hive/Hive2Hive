package org.hive2hive.core.network.data.download.direct.process;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.data.download.direct.DownloadTaskDirect;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectPeerForDownloadStep extends ProcessStep {

	private final static Logger logger = LoggerFactory.getLogger(SelectPeerForDownloadStep.class);

	private final DownloadDirectContext context;

	public SelectPeerForDownloadStep(DownloadDirectContext context) {
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		DownloadTaskDirect task = context.getTask();
		List<Locations> locations = task.consumeLocationsBlocking();

		// prefer own user name
		PeerAddress selectedOwnPeer = null;
		for (Locations location : locations) {
			if (location.getUserId().equals(task.getOwnUserName())) {
				selectedOwnPeer = selectAddressOwnUser(location.getPeerAddresses());
				break;
			}
		}

		if (selectedOwnPeer != null) {
			logger.debug("Found peer of own user to contact for the file {}", task.getDestinationName());
			context.setSelectedPeer(selectedOwnPeer, task.getOwnUserName());
			return;
		}

		// if own peer is not possible, take a foreign sharer
		Random rnd = new Random();
		while (!locations.isEmpty()) {
			Locations randomLocation = locations.get(rnd.nextInt(locations.size()));
			ArrayList<PeerAddress> addresses = new ArrayList<>(randomLocation.getPeerAddresses());
			if (addresses.isEmpty()) {
				// does not contain any addresses, kick it
				locations.remove(randomLocation);
			} else {
				logger.debug("Found peer of foreign user to contact for the file {}",
						task.getDestinationName());
				PeerAddress rndAddress = addresses.get(rnd.nextInt(addresses.size()));
				context.setSelectedPeer(rndAddress, randomLocation.getUserId());
			}
		}

		logger.warn("No online peer found that could be contacted to get the file {}",
				task.getDestinationName());
		throw new ProcessExecutionException("No online peer found that could be contacted");
	}

	private PeerAddress selectAddressOwnUser(Set<PeerAddress> addresses) {
		DownloadTaskDirect task = context.getTask();
		addresses.remove(task.getOwnAddress());

		// if possible, select the one with the same external IP (could be in same subnet)
		InetAddress ownInetAddress = task.getOwnAddress().getInetAddress();
		if (ownInetAddress != null) {
			for (PeerAddress peerAddress : addresses) {
				if (ownInetAddress.equals(peerAddress.getInetAddress())) {
					return peerAddress;
				}
			}
		}

		// shuffle and return the first or null, if no other peer address has been found
		List<PeerAddress> copy = new ArrayList<PeerAddress>(addresses);
		if (copy.size() > 0) {
			Collections.shuffle(copy);
			return copy.get(0);
		} else {
			return null;
		}
	}
}
