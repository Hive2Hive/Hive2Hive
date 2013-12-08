package org.hive2hive.core.process.notify;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.ProcessContext;

/**
 * Notifies all peers with a given message
 * 
 * @author Nico
 * 
 */
public class NotifyPeersProcess extends Process {

	private final NotifyPeersProcessContext context;

	/**
	 * Notify a set of users
	 */
	public NotifyPeersProcess(NetworkManager networkManager, Set<String> users,
			INotificationMessageFactory messageFactory) {
		super(networkManager);
		context = new NotifyPeersProcessContext(this, users, messageFactory);

		setNextStep(new GetPublicKeysStep(users));
	}

	/**
	 * Notify all clients of the currently logged in user (session is required)
	 */
	public NotifyPeersProcess(NetworkManager networkManager, INotificationMessageFactory messageFactory)
			throws NoSessionException {
		super(networkManager);

		H2HSession session = networkManager.getSession();
		Set<String> onlyMe = new HashSet<String>(1);
		onlyMe.add(session.getCredentials().getUserId());

		context = new NotifyPeersProcessContext(this, onlyMe, messageFactory);

		Map<String, PublicKey> myKey = new HashMap<String, PublicKey>(1);
		myKey.put(session.getCredentials().getUserId(), session.getKeyPair().getPublic());
		setNextStep(new GetPublicKeysStep(new ArrayList<String>(), myKey));
	}

	@Override
	public ProcessContext getContext() {
		return context;
	}

}
