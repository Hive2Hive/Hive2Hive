package org.hive2hive.core.network.data;

import java.io.IOException;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.futures.FutureGetListener;
import org.hive2hive.core.network.data.futures.FutureGetUserProfileTaskListener;
import org.hive2hive.core.network.data.futures.FuturePutListener;
import org.hive2hive.core.network.data.futures.FuturePutUserProfileTaskListener;
import org.hive2hive.core.network.data.futures.FutureRemoveListener;
import org.hive2hive.core.network.data.futures.FutureRemoveUserProfileTaskListener;
import org.hive2hive.core.network.data.listener.IGetListener;
import org.hive2hive.core.network.data.listener.IGetUserProfileTaskListener;
import org.hive2hive.core.network.data.listener.IPutListener;
import org.hive2hive.core.network.data.listener.IPutUserProfileTaskListener;
import org.hive2hive.core.network.data.listener.IRemoveListener;
import org.hive2hive.core.network.data.listener.IRemoveUserProfileTaskListener;
import org.hive2hive.core.network.usermessages.UserProfileTask;

/**
 * This class offers an interface for putting, getting and removing data from the network. Data can be stored
 * or loaded globally or locally. The class offers also some special methods for the {@link UserProfileTask}
 * objects.
 * 
 * @author Seppi
 */
public class DataManager {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(DataManager.class);

	private final NetworkManager networkManager;

	public DataManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	private Peer getPeer() {
		return networkManager.getConnection().getPeer();
	}

	public void putGlobal(String locationKey, String contentKey, NetworkContent content, IPutListener listener) {
		FuturePut putFuture = putGlobal(locationKey, contentKey, content);
		if (putFuture == null && listener != null) {
			listener.onPutFailure();
			return;
		}
		putFuture.addListener(new FuturePutListener(locationKey, contentKey, content, listener, this));
	}

	public FuturePut putGlobal(String locationKey, String contentKey, NetworkContent content) {
		logger.debug(String.format("global put key = '%s' content key = '%s' version key = '%s'",
				locationKey, contentKey, content.getVersionKey()));
		try {
			Data data = new Data(content);
			data.ttlSeconds(content.getTimeToLive()).basedOn(content.getBasedOnKey());
			return getPeer().put(Number160.createHash(locationKey))
					.setData(Number160.createHash(contentKey), data).setVersionKey(content.getVersionKey())
					.start();
		} catch (IOException e) {
			logger.error(String
					.format("Global put failed. content = '%s' in the location = '%s' under the contentKey = '%s' exception = '%s'",
							content.toString(), locationKey, contentKey, e.getMessage()));
			return null;
		}
	}

	public void putLocal(String locationKey, String contentKey, NetworkContent content) {
		logger.debug(String.format("local put key = '%s' content key = '%s'", locationKey, contentKey));
		try {
			Number640 key = new Number640(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
					Number160.createHash(contentKey), content.getVersionKey());
			Data data = new Data(content);
			data.ttlSeconds(content.getTimeToLive()).basedOn(content.getBasedOnKey());
			// TODO add public key for content protection
			getPeer().getPeerBean().storage().put(key, data, null, false, false);
		} catch (IOException e) {
			logger.error(String
					.format("Local put failed. content = '%s' in the location = '%s' under the contentKey = '%s' exception = '%s'",
							content.toString(), locationKey, contentKey, e.getMessage()));
		}
	}

	public void getGlobal(String locationKey, String contentKey, IGetListener listener) {
		getGlobal(locationKey, contentKey, H2HConstants.TOMP2P_DEFAULT_KEY, listener);
	}

	public void getGlobal(String locationKey, String contentKey, Number160 versionKey, IGetListener listener) {
		FutureGet futureGet = getGlobal(locationKey, contentKey, versionKey);
		futureGet.addListener(new FutureGetListener(listener));
	}

	public FutureGet getGlobal(String locationKey, String contentKey) {
		return getGlobal(locationKey, contentKey, H2HConstants.TOMP2P_DEFAULT_KEY);
	}

	public FutureGet getGlobal(String locationKey, String contentKey, Number160 versionKey) {
		logger.debug(String.format("global get key = '%s' content key = '%s' version key = '%s'",
				locationKey, contentKey, versionKey));
		return getPeer().get(Number160.createHash(locationKey))
				.setContentKey(Number160.createHash(contentKey)).setVersionKey(versionKey).start();
	}

	public NetworkContent getLocal(String locationKey, String contentKey) {
		return getLocal(locationKey, contentKey, H2HConstants.TOMP2P_DEFAULT_KEY);
	}

	public NetworkContent getLocal(String locationKey, String contentKey, Number160 versionKey) {
		logger.debug(String.format("local get key = '%s' content key = '%s' version key = '%s'", locationKey,
				contentKey, versionKey));
		Number640 key = new Number640(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
				Number160.createHash(contentKey), versionKey);
		Data data = getPeer().getPeerBean().storage().get(key);
		if (data != null) {
			try {
				return (NetworkContent) data.object();
			} catch (ClassNotFoundException | IOException e) {
				logger.error(String.format("local get failed exception = '%s'", e.getMessage()));
			}
		} else {
			logger.warn("futureDHT.getData() is null");
		}
		return null;
	}

	public void remove(String locationKey, String contentKey, Number160 versionKey, IRemoveListener listener) {
		FutureRemove futureRemove = remove(locationKey, contentKey, versionKey);
		futureRemove
				.addListener(new FutureRemoveListener(locationKey, contentKey, versionKey, listener, this));
	}

	public FutureRemove remove(String locationKey, String contentKey, Number160 versionKey) {
		logger.debug(String.format("remove key = '%s' content key = '%s' version key = '%s", locationKey,
				contentKey, versionKey));
		return getPeer().remove(Number160.createHash(locationKey))
				.setContentKey(Number160.createHash(contentKey)).setVersionKey(versionKey).start();
	}

	/**
	 * Put an {@link UserProfileTask} object into the network.</br></br>
	 * <b>Design Decision:</b> These objects are put in a separate domain (see
	 * {@link H2HConstants#USER_PROFILE_TASK_DOMAIN}) to avoid any conflicts with other {@link NetworkContent}
	 * objects. The idea is that the {@link UserProfileTask} objects build a implicit queue, sorted according
	 * the content key which has a prefix (see {@link H2HConstants#USER_PROFILE_TASK_CONTENT_KEY_PREFIX}) and
	 * a time stamp.
	 * 
	 * @param locationKey
	 *            the location key where to put (usually a user id)
	 * @param userProfileTask
	 *            the object to put
	 * @param listener
	 *            a listener which gets notified about a success or fail of a put
	 */
	public void putUserProfileTask(String locationKey, Number160 contentKey, UserProfileTask userProfileTask,
			IPutUserProfileTaskListener listener) {
		FuturePut putFuture = putUserProfileTask(locationKey, contentKey, userProfileTask);
		// attach a listener to handle future results
		putFuture.addListener(new FuturePutUserProfileTaskListener(locationKey, contentKey, userProfileTask,
				listener, this));
	}

	/**
	 * Put operation in the user profile task domain (see {@link H2HConstants#USER_PROFILE_TASK_DOMAIN}). No
	 * future listener gets attached.
	 * 
	 * @param locationKey
	 *            the location key where to put (usually a user id)
	 * @param contentKey
	 *            the content key which should consist of a prefix and a time stamp
	 * @param userProfileTask
	 *            the user profile task object to put
	 * @return a future
	 */
	public FuturePut putUserProfileTask(String locationKey, Number160 contentKey,
			UserProfileTask userProfileTask) {
		logger.debug(String.format("User profile task put key = '%s' content key = '%s'", locationKey,
				contentKey));
		try {
			// put content into the required wrapper
			Data data = new Data(userProfileTask);
			// all content in the network has an expire date
			data.ttlSeconds(userProfileTask.getTimeToLive());
			// put the wrapper under a specific domain
			return getPeer().put(Number160.createHash(locationKey))
					.setDomainKey(Number160.createHash(H2HConstants.USER_PROFILE_TASK_DOMAIN))
					.setData(contentKey, data).start();

		} catch (IOException e) {
			logger.error(String.format(
					"User profile task put failed. location key = '%s' content key = '%s' exception = '%s'",
					locationKey, contentKey, e.getMessage()));
			return null;
		}
	}

	/**
	 * The given listener receives the oldest {@link UserProfileTask} object stored in the network under the
	 * given location key, which should be the user id of the user which received the user profile task
	 * objects.
	 * 
	 * @param locationKey
	 *            location key where the implicit user profile task queue exists
	 * @param listener
	 *            a listener which gets notified about success or fail of the get
	 */
	public void getNextUserProfileTask(String locationKey, IGetUserProfileTaskListener listener) {
		logger.debug(String.format("get next user profile task location key = '%s'", locationKey));
		// TODO get data with smallest content key
		FutureGet futureGet = getPeer().get(Number160.createHash(locationKey))
				.setDomainKey(Number160.createHash(H2HConstants.USER_PROFILE_TASK_DOMAIN)).start();
		// attach a listener to handle future get results
		futureGet.addListener(new FutureGetUserProfileTaskListener(listener));
	}

	/**
	 * Get operation in the user profile task domain (see {@link H2HConstants#USER_PROFILE_TASK_DOMAIN}). No
	 * future listener gets attached.
	 * 
	 * @param locationKey
	 *            location key where the implicit user profile task queue exists
	 * @param contentKey
	 *            the content key which should consist of a prefix and a time stamp
	 * @return a future
	 */
	public FutureGet getUserProfileTask(String locationKey, Number160 contentKey) {
		return getPeer().get(Number160.createHash(locationKey)).setContentKey(contentKey)
				.setDomainKey(Number160.createHash(H2HConstants.USER_PROFILE_TASK_DOMAIN)).start();
	}

	/**
	 * Remove an {@link UserProfileTask} object under the given keys.
	 * 
	 * @param locationKey
	 *            location key of the object to delete
	 * @param contentKey
	 *            content key of the object to delete
	 * @param listener
	 *            a listener which gets notified about success or fail of the remove
	 */
	public void removeUserProfileTask(String locationKey, Number160 contentKey,
			IRemoveUserProfileTaskListener listener) {
		logger.debug(String.format("remove user profile task location key = '%s' content key = '%s'",
				locationKey, contentKey));
		// start removing in the specific user profile task domain
		FutureRemove futureRemove = getPeer().remove(Number160.createHash(locationKey))
				.setContentKey(contentKey)
				.setDomainKey(Number160.createHash(H2HConstants.USER_PROFILE_TASK_DOMAIN)).start();
		// attach a listener to handle future remove results
		futureRemove.addListener(new FutureRemoveUserProfileTaskListener(locationKey, contentKey, listener,
				this));
	}

	/**
	 * Remove operation in the user profile task domain (see {@link H2HConstants#USER_PROFILE_TASK_DOMAIN}).
	 * No future listener gets attached.
	 * 
	 * @param locationKey
	 *            location key where the implicit user profile task queue exists
	 * @param contentKey
	 *            the content key which should consist of a prefix and a time stamp
	 * @return a future
	 */
	public FutureRemove removeUserProfileTask(String locationKey, Number160 contentKey) {
		return getPeer().remove(Number160.createHash(locationKey)).setContentKey(contentKey)
				.setDomainKey(Number160.createHash(H2HConstants.USER_PROFILE_TASK_DOMAIN)).start();
	}

}
