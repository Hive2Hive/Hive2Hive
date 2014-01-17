package org.hive2hive.core.process.notify;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.network.data.NetworkContent;
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
		Map<String, PublicKey> keys = new HashMap<String, PublicKey>();

		for (String user : users) {
			boolean getRequired = true;
			try {
				// check if own user --> key is already in session
				H2HSession session = getNetworkManager().getSession();
				if (session.getCredentials().getUserId().equalsIgnoreCase(user)) {
					// current user is myself, the key is already present
					keys.put(user, session.getKeyPair().getPublic());
					getRequired = false;
				}
			} catch (NoSessionException e) {
				getRequired = true;
			}

			if (getRequired) {
				// needs to perform a get call
				NetworkContent content = get(user, H2HConstants.USER_PUBLIC_KEY);
				if (content == null) {
					logger.error("Could not get the public key for user '" + user + "'. He get's ignored.");
				} else {
					logger.debug("Got public key from user '" + user + "'. " + users.size()
							+ " keys more to get.");
					UserPublicKey key = (UserPublicKey) content;
					keys.put(user, key.getPublicKey());
				}
			}
		}

		// store the keys to the context
		NotifyPeersProcessContext context = (NotifyPeersProcessContext) getProcess().getContext();
		context.setUserPublicKeys(keys);

		// continue with getting all locations
		getProcess().setNextStep(new GetAllLocationsStep(keys.keySet()));
	}
}
