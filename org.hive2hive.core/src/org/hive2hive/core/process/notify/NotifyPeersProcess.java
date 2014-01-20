package org.hive2hive.core.process.notify;

import java.util.HashSet;
import java.util.Set;

import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.listener.IProcessListener;
import org.hive2hive.core.process.notify.cleanup.CleanupLocationsProcess;

/**
 * Notifies all peers with a given message. If the user list also contains other users, it puts a
 * {@link UserProfileTask} into the DHT and sends them a hint message that they should check their user
 * profile queue
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
		String userId = networkManager.getUserId();

		// to cleanup the unreachable own peers
		addCleanupListener();

		Set<String> usersToNotify = users;
		if (messageFactory.createUserProfileTask() == null) {
			// only private notification (or none)
			usersToNotify = new HashSet<>(1);
			if (users.contains(userId))
				usersToNotify.add(userId);
			else
				throw new IllegalArgumentException(
						"Users can't be notified because the UserProfileTask is null and no notification of the own user");
		}

		context = new NotifyPeersProcessContext(this, usersToNotify, messageFactory, userId);
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
