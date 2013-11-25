package org.hive2hive.core.process.notify;

import java.util.Map;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
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

	public NotifyPeersProcess(NetworkManager networkManager, Map<String, BaseDirectMessage> users) {
		super(networkManager);
		context = new NotifyPeersProcessContext(this, users);

		setNextStep(new GetPublicKeysStep(users.keySet()));
	}

	@Override
	public ProcessContext getContext() {
		return context;
	}

}
