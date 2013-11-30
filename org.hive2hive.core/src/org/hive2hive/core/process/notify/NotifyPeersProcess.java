package org.hive2hive.core.process.notify;

import java.util.Set;

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

	public NotifyPeersProcess(NetworkManager networkManager, Set<String> users,
			INotificationMessageFactory messageFactory) {
		super(networkManager);
		context = new NotifyPeersProcessContext(this, users, messageFactory);

		setNextStep(new GetPublicKeysStep(users));
	}

	@Override
	public ProcessContext getContext() {
		return context;
	}

}
