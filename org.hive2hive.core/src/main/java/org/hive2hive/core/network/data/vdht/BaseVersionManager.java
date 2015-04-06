package org.hive2hive.core.network.data.vdht;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.DigestResult;
import net.tomp2p.storage.Data;

import org.hive2hive.core.model.versioned.BaseVersionedNetworkContent;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseVersionManager<T extends BaseVersionedNetworkContent> {

	private static final Logger logger = LoggerFactory.getLogger(BaseVersionManager.class);

	// limit constants
	protected static final int GET_FAILED_LIMIT = 2;
	protected static final int FORK_AFTER_GET_LIMIT = 2;
	protected static final int DELAY_LIMIT = 2;

	protected final DataManager dataManager;
	protected final IParameters parameters;
	protected final Random random = new Random();

	// caches
	protected Cache<Set<Number160>> digestCache = new Cache<Set<Number160>>();
	protected Cache<T> contentCache = new Cache<T>();

	public BaseVersionManager(DataManager dataManager, String locationKey, String contentKey) {
		this.dataManager = dataManager;
		this.parameters = new Parameters().setLocationKey(locationKey).setContentKey(contentKey);
	}

	protected NavigableMap<Number160, Set<Number160>> buildDigest(Map<PeerAddress, DigestResult> rawDigest) {
		NavigableMap<Number160, Set<Number160>> digestMap = new TreeMap<Number160, Set<Number160>>();
		if (rawDigest == null) {
			return digestMap;
		}
		for (PeerAddress peerAddress : rawDigest.keySet()) {
			NavigableMap<Number640, Collection<Number160>> tmp = rawDigest.get(peerAddress).keyDigest();
			if (tmp == null || tmp.isEmpty()) {
				// ignore this peer
			} else {
				for (Number640 key : tmp.keySet()) {
					for (Number160 bKey : tmp.get(key)) {
						if (!digestMap.containsKey(key.versionKey())) {
							digestMap.put(key.versionKey(), new HashSet<Number160>());
						}
						digestMap.get(key.versionKey()).add(bKey);
					}
				}
			}
		}
		return digestMap;
	}

	@SuppressWarnings("unchecked")
	protected NavigableMap<Number160, T> buildData(Map<PeerAddress, Map<Number640, Data>> rawData) {
		NavigableMap<Number160, T> dataMap = new TreeMap<Number160, T>();
		if (rawData == null) {
			return dataMap;
		}
		for (PeerAddress peerAddress : rawData.keySet()) {
			Map<Number640, Data> tmp = rawData.get(peerAddress);
			if (tmp == null || tmp.isEmpty()) {
				// ignore this peer
			} else {
				for (Number640 key : tmp.keySet()) {
					try {
						byte[] buffer = tmp.get(key).toBytes();
						if (buffer != null && buffer.length > 0) {
							T object = (T) dataManager.getSerializer().deserialize(buffer);
							dataMap.put(key.versionKey(), object);
						} else {
							logger.warn(
									"Received unreadable buffer object = '{}'",
									tmp.get(key).object().getClass().getSimpleName());
						}
					} catch (IOException e) {
						logger.warn("Could not deserialize the data. Data could be null. Reason = '{}'", e.getMessage());
					} catch (Exception e) {
						logger.warn("Could not get data. Reason = '{}'", e.getMessage());
					}
				}
			}
		}
		return dataMap;
	}

	protected boolean hasVersionDelay(Map<Number160, ?> latestVersions, Cache<Set<Number160>> digestCache) {
		for (Number160 version : digestCache.keySet()) {
			for (Number160 basedOnKey : digestCache.get(version)) {
				if (latestVersions.containsKey(basedOnKey)) {
					return true;
				}
			}
		}
		return false;
	}

	protected Cache<Set<Number160>> getLatest(Cache<Set<Number160>> cache) {
		// delete all predecessors
		Cache<Set<Number160>> tmp = new Cache<Set<Number160>>(cache);
		Cache<Set<Number160>> result = new Cache<Set<Number160>>();
		while (!tmp.isEmpty()) {
			// last entry is a latest version
			Entry<Number160, Set<Number160>> latest = tmp.lastEntry();
			// store in results list
			result.put(latest.getKey(), latest.getValue());
			// delete all predecessors of latest entry
			deletePredecessors(latest.getKey(), tmp);
		}
		return result;
	}

	protected void deletePredecessors(Number160 key, Cache<Set<Number160>> cache) {
		Set<Number160> basedOnSet = cache.remove(key);
		// check if set has been already deleted
		if (basedOnSet == null) {
			return;
		}
		// check if version is initial version
		if (basedOnSet.isEmpty()) {
			return;
		}
		// remove all predecessor versions recursively
		for (Number160 basedOnKey : basedOnSet) {
			deletePredecessors(basedOnKey, cache);
		}
	}
}
