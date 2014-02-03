package org.hive2hive.processes.implementations.notify;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.base.BasePutProcessStep;
import org.hive2hive.processes.implementations.context.NotifyProcessContext;

/**
 * Removes all locations that are unreachable and puts the reduced locations back into the DHT
 * 
 * @author Nico
 * 
 */
public class RemoveUnreachableStep extends BasePutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(RemoveUnreachableStep.class);

	private final NotifyProcessContext context;
	private final NetworkManager networkManager;

	public RemoveUnreachableStep(NotifyProcessContext context, NetworkManager networkManager)
			throws NoPeerConnectionException {
		super(networkManager.getDataManager());
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		Locations locations = context.consumeLocations();
		Set<PeerAddress> unreachablePeers = context.getUnreachablePeers();

		if (unreachablePeers.isEmpty()) {
			logger.info("No locations to remove. Skip the cleanup.");
			return;
		}

		KeyPair protectionKeys;
		try {
			protectionKeys = networkManager.getSession().getProfileManager().getDefaultProtectionKey();
		} catch (GetFailedException | NoSessionException e) {
			logger.error("Could not get the protection keys", e);
			return;
		}

		for (PeerAddress toRemove : unreachablePeers) {
			locations.removePeerAddress(toRemove);
		}

		locations.setBasedOnKey(locations.getVersionKey());

		try {
			locations.generateVersionKey();
		} catch (IOException e) {
			logger.error("Version Key could not be generated");
			return;
		}

		try {
			put(locations.getUserId(), H2HConstants.USER_LOCATIONS, locations, protectionKeys);
		} catch (PutFailedException e) {
			logger.error("Could not put the updated locations");
		}
	}
}
