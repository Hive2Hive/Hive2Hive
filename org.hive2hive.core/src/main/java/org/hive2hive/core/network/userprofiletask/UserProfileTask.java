package org.hive2hive.core.network.userprofiletask;

import java.security.KeyPair;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.BaseNetworkContent;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base class of all {@link UserProfileTask}s.<br>
 * A ({@link Runnable}) task which is stored on the proxy node of the receiving user. This task will be stored
 * in a �queue�-like data structure. This allows an asynchronous communication between users (i.e., between
 * friends). This task is used in case an user needs to update its profile due to changes introduced by
 * friends.<br><br>
 * 
 * <b>User Profile Task Queue</b> All {@link UserProfileTask} objects have to be stored (encrypted with the
 * receivers public key) on the proxy node of the receiver. The {@link DataManager} provides the method
 * {@link DataManager#putUserProfileTask(String, Number160, BaseNetworkContent, KeyPair)} which stores an
 * {@link UserProfileTask} object in a specific domain (see {@link H2HConstants#USER_PROFILE_TASK_DOMAIN}).
 * For this purpose the constructor generates a content key which is based on a time stamp (taking the time at
 * creating the object). This allows us to build an implicit queue on the proxy node. The tasks are sorted
 * according their content keys respectively time stamps. The method
 * {@link DataManager#getUserProfileTask(String)} allows to get the oldest {@link UserProfileTask} object of
 * an user from the queue. The task can then be handled in a separate
 * thread. After handling please don't forget to remove the handled task (see
 * {@link DataManager#removeUserProfileTask(String, Number160, KeyPair)}).
 * 
 * @author Christian, Seppi, Nico
 */
public abstract class UserProfileTask extends BaseNetworkContent {

	private static final Logger logger = LoggerFactory.getLogger(UserProfileTask.class);

	private static final long serialVersionUID = -773794512479641000L;

	protected final String sender;
	private final KeyPair protectionKeys;
	private final String id;
	private final Number160 contentKey;
	protected NetworkManager networkManager;

	public UserProfileTask(String sender, KeyPair protectionKeys) {
		this.sender = sender;
		this.protectionKeys = protectionKeys;
		this.id = UUID.randomUUID().toString();

		// get the current time
		long timestamp = new Date().getTime();
		// use time stamp value to create a content key
		contentKey = new Number160(timestamp);
	}

	/**
	 * Starts the execution of the user profile task
	 */
	public abstract void start();

	public String getId() {
		return id;
	}

	public Number160 getContentKey() {
		return contentKey;
	}

	public KeyPair getProtectionKeys() {
		return protectionKeys;
	}

	/**
	 * Setter
	 * 
	 * @param networkManager
	 *            the {@link NetworkManager} to be used by this user profile task
	 */
	public void setNetworkManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	@Override
	public int getTimeToLive() {
		return TimeToLiveStore.getInstance().getUserMessageQueue();
	}

	/**
	 * Helper method that asynchronously notifies all clients of the same user.
	 * 
	 * @param messageFactory the message factory
	 * @throws NoPeerConnectionException if the peer is not connected
	 * @throws NoSessionException if the user has no session.
	 */
	protected void notifyOtherClients(BaseNotificationMessageFactory messageFactory) throws NoPeerConnectionException, NoSessionException {
		Set<String> onlyMe = new HashSet<String>(1);
		onlyMe.add(networkManager.getUserId());

		IProcessComponent<Void> notificationProcess = ProcessFactory.instance().createNotificationProcess(messageFactory,
				onlyMe, networkManager);

		try {
			notificationProcess.execute();
		} catch (ProcessExecutionException | InvalidProcessStateException ex) {
			logger.error("Notification process execution failed.", ex);
		}
	}
}
