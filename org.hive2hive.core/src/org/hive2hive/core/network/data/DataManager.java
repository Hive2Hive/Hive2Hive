package org.hive2hive.core.network.data;

import java.io.IOException;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.builder.DigestBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.futures.FutureGetListener;
import org.hive2hive.core.network.data.futures.FuturePutListener;
import org.hive2hive.core.network.data.futures.FutureRemoveListener;
import org.hive2hive.core.network.data.listener.IGetListener;
import org.hive2hive.core.network.data.listener.IPutListener;
import org.hive2hive.core.network.data.listener.IRemoveListener;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;

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

	public void put(String locationKey, String contentKey, NetworkContent content, IPutListener listener) {
		Number160 lKey = Number160.createHash(locationKey);
		Number160 dKey = H2HConstants.TOMP2P_DEFAULT_KEY;
		Number160 cKey = Number160.createHash(contentKey);
		FuturePut putFuture = put(lKey, dKey, cKey, content);
		if (putFuture == null && listener != null) {
			listener.onPutFailure();
			return;
		}
		putFuture.addListener(new FuturePutListener(lKey, dKey, cKey, content, listener, this));
	}

	public void putUserProfileTask(String locationKey, Number160 contentKey, UserProfileTask userProfileTask,
			IPutListener listener) {
		Number160 lKey = Number160.createHash(locationKey);
		Number160 dKey = Number160.createHash(H2HConstants.USER_PROFILE_TASK_DOMAIN);
		FuturePut putFuture = put(lKey, dKey, contentKey, userProfileTask);
		// attach a listener to handle future results
		putFuture.addListener(new FuturePutListener(lKey, dKey, contentKey, userProfileTask, listener, this));
	}

	public FuturePut put(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			NetworkContent content) {
		logger.debug(String.format(
				"put location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
				locationKey, domainKey, contentKey, content.getVersionKey()));
		try {
			Data data = new Data(content);
			data.ttlSeconds(content.getTimeToLive()).basedOn(content.getBasedOnKey());
			// TODO add public key for content protection
			return getPeer().put(locationKey).setData(contentKey, data).setDomainKey(domainKey)
					.setVersionKey(content.getVersionKey()).start();
		} catch (IOException e) {
			logger.error(String
					.format("Put failed. location key = '%s' domain key content key = '%s' version key = '%s' exception = '%s'",
							locationKey, domainKey, contentKey, content.getVersionKey(), e.getMessage()));
			return null;
		}
	}

	@Deprecated
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
			logger.error(String.format(
					"Local put failed. location key = '%s' content key = '%s' exception = '%s'", locationKey,
					contentKey, e.getMessage()));
		}
	}

	public void get(String locationKey, String contentKey, IGetListener listener) {
		FutureGet futureGet = get(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
				Number160.createHash(contentKey));
		futureGet.addListener(new FutureGetListener(listener));
	}

	public void get(String locationKey, String contentKey, Number160 versionKey, IGetListener listener) {
		FutureGet futureGet = get(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
				Number160.createHash(contentKey), versionKey);
		futureGet.addListener(new FutureGetListener(listener));
	}

	public void getUserProfileTask(String locationKey, IGetListener listener) {
		FutureGet futureGet = getPeer()
				.get(Number160.createHash(locationKey))
				.from(new Number640(Number160.createHash(locationKey), Number160
						.createHash(H2HConstants.USER_PROFILE_TASK_DOMAIN), Number160.ZERO, Number160.ZERO))
				.to(new Number640(Number160.createHash(locationKey), Number160
						.createHash(H2HConstants.USER_PROFILE_TASK_DOMAIN), Number160.ZERO,
						Number160.MAX_VALUE)).ascending().returnNr(1).start();
		futureGet.addListener(new FutureGetListener(listener));
	}

	public FutureGet get(Number160 locationKey, Number160 domainKey, Number160 contentKey) {
		logger.debug(String.format("get key = '%s' domain key = '%s' content key = '%s'", locationKey,
				domainKey, contentKey));
		return getPeer().get(locationKey)
				.from(new Number640(locationKey, domainKey, contentKey, Number160.ZERO))
				.to(new Number640(locationKey, domainKey, contentKey, Number160.MAX_VALUE)).descending()
				.returnNr(1).start();
	}

	public FutureGet get(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			Number160 versionKey) {
		logger.debug(String.format("get key = '%s' domain Key = '%s' content key = '%s' version key = '%s'",
				locationKey, domainKey, contentKey, versionKey));
		return getPeer().get(locationKey).setDomainKey(domainKey).setContentKey(contentKey)
				.setVersionKey(versionKey).start();
	}

	@Deprecated
	public NetworkContent getLocal(String locationKey, String contentKey) {
		return getLocal(locationKey, contentKey, H2HConstants.TOMP2P_DEFAULT_KEY);
	}

	@Deprecated
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

	public void remove(String locationKey, String contentKey, IRemoveListener listener) {
		Number160 lKey = Number160.createHash(locationKey);
		Number160 dKey = H2HConstants.TOMP2P_DEFAULT_KEY;
		Number160 cKey = Number160.createHash(contentKey);
		FutureRemove futureRemove = remove(lKey, dKey, cKey);
		futureRemove.addListener(new FutureRemoveListener(lKey, dKey, cKey, listener, this));
	}

	public void remove(String locationKey, String contentKey, Number160 versionKey, IRemoveListener listener) {
		Number160 lKey = Number160.createHash(locationKey);
		Number160 dKey = H2HConstants.TOMP2P_DEFAULT_KEY;
		Number160 cKey = Number160.createHash(contentKey);
		FutureRemove futureRemove = remove(lKey, dKey, cKey, versionKey);
		futureRemove.addListener(new FutureRemoveListener(lKey, dKey, cKey, versionKey, listener, this));
	}

	public void removeUserProfileTask(String locationKey, Number160 contentKey, IRemoveListener listener) {
		Number160 lKey = Number160.createHash(locationKey);
		Number160 dKey = Number160.createHash(H2HConstants.USER_PROFILE_TASK_DOMAIN);
		FutureRemove futureRemove = remove(lKey, dKey, contentKey);
		futureRemove.addListener(new FutureRemoveListener(lKey, dKey, contentKey, listener, this));
	}

	public FutureRemove remove(Number160 locationKey, Number160 domainKey, Number160 contentKey) {
		logger.debug(String.format("remove key = '%s' domain key = '%s' content key = '%s'", locationKey,
				domainKey, contentKey));
		return getPeer().remove(locationKey)
				.from(new Number640(locationKey, domainKey, contentKey, Number160.ZERO))
				.to(new Number640(locationKey, domainKey, contentKey, Number160.MAX_VALUE)).start();
	}

	public FutureRemove remove(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			Number160 versionKey) {
		logger.debug(String.format(
				"remove key = '%s' domain key = '%s' content key = '%s' version key = '%s'", locationKey,
				domainKey, contentKey, versionKey));
		return getPeer().remove(locationKey).setDomainKey(domainKey).contentKey(contentKey)
				.setVersionKey(versionKey).start();
	}

	public DigestBuilder getDigest(Number160 locationKey) {
		return getPeer().digest(locationKey);
	}

}
