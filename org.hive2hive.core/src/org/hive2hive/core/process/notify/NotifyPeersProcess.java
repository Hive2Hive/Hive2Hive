package org.hive2hive.core.process.notify;

import java.util.Set;

import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.listener.IProcessListener;
import org.hive2hive.core.process.notify.cleanup.CleanupLocationsProcess;

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
	public NotifyPeersProcess(NetworkManager networkManager, BaseNotificationMessageFactory messageFactory,
			Set<String> users) {
		super(networkManager);
		addCleanupListener();

		context = new NotifyPeersProcessContext(this, users, messageFactory);
		setNextStep(new GetPublicKeysStep(users));
	}

	private void addCleanupListener() {
		IProcessListener listener = new IProcessListener() {

			@Override
			public void onSuccess() {
				cleanupOwnLocations();
			}

			@Override
			public void onFail(Exception exception) {
				// do not cleanup
			}
		};

		addListener(listener);
	}

	@Override
	public NotifyPeersProcessContext getContext() {
		return context;
	}

	private void cleanupOwnLocations() {
		// check if cleanup is required
		if (getContext().getUnreachableOwnPeers().isEmpty()) {
			return;
		}

		try {
			CleanupLocationsProcess process = new CleanupLocationsProcess(getNetworkManager(), getContext()
					.getUnreachableOwnPeers());
			process.start();
		} catch (NoSessionException e) {
			// ignore
		}
	}
}
