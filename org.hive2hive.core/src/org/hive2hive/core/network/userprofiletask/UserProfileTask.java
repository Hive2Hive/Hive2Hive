package org.hive2hive.core.network.userprofiletask;

import java.security.KeyPair;
import java.util.Date;

import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.listener.IGetListener;
import org.hive2hive.core.network.data.listener.IPutListener;
import org.hive2hive.core.network.data.listener.IRemoveListener;
import org.hive2hive.core.security.EncryptionUtil;

/**
 * The base class of all {@link UserProfileTask}s.</br>
 * A ({@link Runnable}) task which is stored on the proxy node of the receiving user. This task will be stored
 * in a “queue”-like data structure. This allows an asynchronous communication between users (i.e., between
 * friends). This task is used in case an user needs to update its profile due to changes introduced by
 * friends.</br></br>
 * 
 * <b>User Profile Task Queue</b> All {@link UserProfileTask} objects have to be stored (encrypted with the
 * receivers public key) on the proxy node of the receiver. The {@link DataManager} provides the method
 * {@link DataManager#putUserProfileTask(String, Number160, NetworkContent, IPutListener)} which stores an
 * {@link UserProfileTask} object in a specific domain (see {@link H2HConstants#USER_PROFILE_TASK_DOMAIN}).
 * For this purpose the constructor generates a content key which is based on a time stamp (taking the time at
 * creating the object). This allows us to build an implicit queue on the proxy node. The tasks are sorted
 * according their content keys respectively time stamps. The method
 * {@link DataManager#getUserProfileTask(String, IGetListener)} allows to get the oldest
 * {@link UserProfileTask} object of an user from the queue. The task can then be handled in a separate
 * thread. After handling please don't forget to remove the handled task (see
 * {@link DataManager#removeUserProfileTask(String, Number160, IRemoveListener)}).
 * 
 * @author Christian, Seppi
 */
public abstract class UserProfileTask extends NetworkContent implements Runnable {

	private static final long serialVersionUID = -773794512479641000L;

	protected NetworkManager networkManager;
	protected Number160 contentKey;
	protected KeyPair protectionKey;

	public UserProfileTask() {
		this.protectionKey = EncryptionUtil.generateProtectionKey();
		generateContentKey();
	}

	/**
	 * Creates a key which is a time stamp (taking current time).
	 */
	private void generateContentKey() {
		// get the current time
		long timestamp = new Date().getTime();
		// use time stamp value to create a content key
		contentKey = new Number160(timestamp);
	}

	public Number160 getContentKey() {
		return contentKey;
	}
	
	public KeyPair getProtectionKey() {
		return protectionKey;
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

}
