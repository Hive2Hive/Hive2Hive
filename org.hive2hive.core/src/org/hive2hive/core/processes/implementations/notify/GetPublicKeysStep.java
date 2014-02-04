package org.hive2hive.core.processes.implementations.notify;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.common.base.BaseGetProcessStep;
import org.hive2hive.core.processes.implementations.context.NotifyProcessContext;

/**
 * Gets all public keys from these users iteratively
 * 
 * @author Nico
 * 
 */
// TODO get the keys in parallel
public class GetPublicKeysStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetPublicKeysStep.class);
	private final NotifyProcessContext context;
	private final NetworkManager networkManager;

	public GetPublicKeysStep(NotifyProcessContext context, NetworkManager networkManager)
			throws NoPeerConnectionException {
		super(networkManager.getDataManager());
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		Set<String> users = context.consumeUsersToNotify();

		logger.debug("Start getting public keys from " + users.size() + " user(s)");
		Map<String, PublicKey> keys = new HashMap<String, PublicKey>();

		for (String user : users) {
			try {
				PublicKey key = networkManager.getPublicKey(user);
				keys.put(user, key);
			} catch (GetFailedException e) {
				logger.error("Could not get the key for user " + user);
			}
		}

		// store the keys to the context
		context.setUserPublicKeys(keys);
	}
}
