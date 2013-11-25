package org.hive2hive.core.process.notify;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.common.get.BaseGetProcessStep;

/**
 * Gets all public keys from these users iterativels
 * 
 * @author Nico
 * 
 */
// TODO get the key in parallel
// TODO cache the keys to speedup future messages
public class GetPublicKeysStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetPublicKeysStep.class);
	private final List<String> users;
	private final Map<String, PublicKey> keys;
	private String current;

	public GetPublicKeysStep(Set<String> users) {
		this(new ArrayList<String>(users), new HashMap<String, PublicKey>());
		logger.debug("Start getting public keys from " + users.size() + " user(s)");
	}

	private GetPublicKeysStep(List<String> moreToGet, Map<String, PublicKey> keys) {
		this.users = moreToGet;
		this.keys = keys;
	}

	@Override
	public void start() {
		if (users.isEmpty()) {
			// store the keys to the context
			NotifyPeersProcessContext context = (NotifyPeersProcessContext) getProcess().getContext();
			context.setUserPublicKeys(keys);

			// continue with getting all locations
			getProcess().setNextStep(new GetAllLocationsStep(keys.keySet()));
		} else {
			current = users.remove(0);

			get(current, H2HConstants.USER_PUBLIC_KEY);
		}
	}

	@Override
	public void handleGetResult(NetworkContent content) {
		if (content == null) {
			logger.error("Could not get the public key for user '" + current + "'. He get's ignored.");
		} else {
			logger.debug("Got public key from user '" + current + "'. " + users.size() + " keys more to get.");
			UserPublicKey key = (UserPublicKey) content;
			keys.put(current, key.getPublicKey());
		}

		getProcess().setNextStep(new GetPublicKeysStep(users, keys));
	}

}
