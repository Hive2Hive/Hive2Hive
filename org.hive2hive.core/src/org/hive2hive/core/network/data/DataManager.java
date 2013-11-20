package org.hive2hive.core.network.data;

import java.io.IOException;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;

/**
 * This class offers an interface for storing into and loading from the network.
 * Data can be stored or loaded globally or locally.
 * 
 * @author Seppi
 */
public class DataManager {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(DataManager.class);

	private final Number160 TOMP2P_DEFAULT_KEY = Number160.ZERO;

	private final NetworkManager networkManager;

	public DataManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	public FuturePut putGlobal(String locationKey, String contentKey, NetworkContent newContent) {
		logger.debug(String.format("global put key = '%s' content key = '%s' version key = '%s'",
				locationKey, contentKey, newContent.getVersionKey()));
		try {
			Data data = new Data(newContent);
			data.ttlSeconds(newContent.getTimeToLive()).basedOn(newContent.getBasedOnKey());
			return networkManager.getConnection().getPeer().put(Number160.createHash(locationKey))
					.setData(Number160.createHash(contentKey), data)
					.setVersionKey(newContent.getVersionKey()).start();
		} catch (IOException e) {
			logger.error(String
					.format("Global put failed. content = '%s' in the location = '%s' under the contentKey = '%s' exception = '%s'",
							newContent.toString(), locationKey, contentKey, e.getMessage()));
			return null;
		}
	}

	public FutureGet getGlobal(String locationKey, String contentKey) {
		return getGlobal(locationKey, contentKey, TOMP2P_DEFAULT_KEY);
	}

	public FutureGet getGlobal(String locationKey, String contentKey, Number160 versionKey) {
		logger.debug(String.format("global get key = '%s' content key = '%s'", locationKey, contentKey));
		return networkManager.getConnection().getPeer().get(Number160.createHash(locationKey))
				.setContentKey(Number160.createHash(contentKey)).setVersionKey(versionKey).start();
	}

	public void putLocal(String locationKey, String contentKey, NetworkContent content) {
		logger.debug(String.format("local put key = '%s' content key = '%s'", locationKey, contentKey));
		try {
			Number640 key = new Number640(Number160.createHash(locationKey), TOMP2P_DEFAULT_KEY,
					Number160.createHash(contentKey), content.getVersionKey());
			Data data = new Data(content);
			data.ttlSeconds(content.getTimeToLive()).basedOn(content.getBasedOnKey());
			// TODO add public key for content protection
			// TODO for what is this putIfAbsent flag?
			// TODO what is the domainProtaction flag?
			networkManager.getConnection().getPeer().getPeerBean().storage()
					.put(key, data, null, false, false);
		} catch (IOException e) {
			logger.error(String
					.format("Local put failed. content = '%s' in the location = '%s' under the contentKey = '%s' exception = '%s'",
							content.toString(), locationKey, contentKey, e.getMessage()));
		}
	}

	public NetworkContent getLocal(String locationKey, String contentKey) {
		return getLocal(locationKey, contentKey, TOMP2P_DEFAULT_KEY);
	}

	public NetworkContent getLocal(String locationKey, String contentKey, Number160 versionKey) {
		logger.debug(String.format("local get key = '%s' content key = '%s' version key = '%s'", locationKey,
				contentKey, versionKey));
		Number640 key = new Number640(Number160.createHash(locationKey), TOMP2P_DEFAULT_KEY,
				Number160.createHash(contentKey), versionKey);
		Data data = networkManager.getConnection().getPeer().getPeerBean().storage().get(key);
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

	public FutureRemove remove(String locationKey, String contentKey) {
		return remove(locationKey, contentKey, TOMP2P_DEFAULT_KEY);
	}

	public FutureRemove remove(String locationKey, String contentKey, Number160 versionKey) {
		logger.debug(String.format("remove key = '%s' content key = '%s'", locationKey, contentKey));
		return networkManager.getConnection().getPeer().remove(Number160.createHash(locationKey))
				.setContentKey(Number160.createHash(contentKey)).setVersionKey(versionKey).start();
	}

}
