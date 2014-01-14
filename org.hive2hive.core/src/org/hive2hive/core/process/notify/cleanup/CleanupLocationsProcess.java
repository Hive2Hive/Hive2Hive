package org.hive2hive.core.process.notify.cleanup;

import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.GetLocationsStep;

/**
 * Process can be used to remove a given set of peer addresses from the own locations map
 * 
 * @author Nico
 * 
 */
public class CleanupLocationsProcess extends Process {

	private final static Logger logger = H2HLoggerFactory.getLogger(CleanupLocationsProcess.class);
	private CleanupLocationsProcessContext context;

	public CleanupLocationsProcess(NetworkManager networkManager, Set<PeerAddress> toRemove)
			throws NoSessionException {
		super(networkManager);
		logger.debug("Start to remove " + toRemove.size() + " peers from own locations map");

		H2HSession session = networkManager.getSession();

		context = new CleanupLocationsProcessContext(session, this);

		RemoveUnreachableStep removeStep = new RemoveUnreachableStep(toRemove);
		GetLocationsStep first = new GetLocationsStep(session.getCredentials().getUserId(), removeStep,
				context);
		setNextStep(first);
	}

	@Override
	public CleanupLocationsProcessContext getContext() {
		return context;
	}
}
