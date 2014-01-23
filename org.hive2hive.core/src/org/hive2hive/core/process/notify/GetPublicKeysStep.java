package org.hive2hive.core.process.notify;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.process.common.get.BaseGetProcessStep;

/**
 * Gets all public keys from these users iteratively
 * 
 * @author Nico
 * 
 */
// TODO get the keys in parallel
// TODO cache the keys to speedup future messages
public class GetPublicKeysStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetPublicKeysStep.class);
	private final Set<String> users;

	public GetPublicKeysStep(Set<String> users) {
		this.users = users;
	}

	@Override
	public void start() {
		logger.debug("Start getting public keys from " + users.size() + " user(s)");
		NotifyPeersProcessContext context = (NotifyPeersProcessContext) getProcess().getContext();
		Map<String, PublicKey> keys = new HashMap<String, PublicKey>();

		for (String user : users) {
			try {
				PublicKey key = getNetworkManager().getPublicKey(user);
				keys.put(user, key);
			} catch (GetFailedException e) {
				logger.error("Could not get the key for user " + user);
			}
		}

		// store the keys to the context
		context.setUserPublicKeys(keys);

		// continue with getting all locations
		getProcess().setNextStep(new PutAllUserProfileTasksStep());
	}
}
