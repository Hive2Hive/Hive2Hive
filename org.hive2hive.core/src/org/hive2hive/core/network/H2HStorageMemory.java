package org.hive2hive.core.network;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.StorageMemory;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

/**
 * Every <code>Hive2Hive</code> node has validation strategies when data is
 * stored. This is realized by a customized storage memory. Before some data
 * gets stored on a node the put method gets called where the node can verify
 * the store request.
 * 
 * @author Seppi
 */
public class H2HStorageMemory extends StorageMemory {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(H2HStorageMemory.class);

	private final NetworkManager networkManager;

	public H2HStorageMemory(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	@Override
	public PutStatus put(Number160 locationKey, Number160 domainKey, Number160 contentKey, Data newData,
			PublicKey publicKey, boolean putIfAbsent, boolean domainProtection) {
		// TODO this method receives another Number160 parameter for the version
		// this serves as stub
		@Deprecated
		Number160 versionKey = Number160.MAX_VALUE;

		logger.debug("Start verification of locationKey: " + locationKey + " domainKey: " + domainKey
				+ " contentKey: " + contentKey + " versionKey: " + versionKey);
		PutStatus status = validateVersion(locationKey, domainKey, contentKey, versionKey);
		if (status == PutStatus.OK) {
			// TODO: add the version key here
			status = super.put(locationKey, domainKey, contentKey, newData, publicKey, putIfAbsent,
					domainProtection);

			// After adding the content to the memory, old versions should be cleaned up. How many old
			// versions we keep could probably be parameterized. I (Nico) would recommend to keep at least 2
			// or 3 versions, thus we can recognize concurrent modification better (else, the 'previous
			// version' hash is always wrong).
		}

		logger.debug("Finished verification (" + status + ") of locationKey: " + locationKey + " domainKey: "
				+ domainKey + " contentKey: " + contentKey + " versionKey: " + versionKey);
		return status;
	}

	private PutStatus validateVersion(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			Number160 versionKey) {
		/** 0. get all versions for this locationKey, domainKey and contentKey combination **/
		// TODO This list is only a stub, get it from the local storage (key = versionKey, value = data)
		/* Map<Number160, Data> history = get(locationKey, domainKey, contentKey */
		Map<Number160, Data> history = new HashMap<Number160, Data>();

		/** 1. if version is null and no history yet, it is the first entry here **/
		if (history.isEmpty() && versionKey == null) {
			logger.debug("First version of a content is added");
			return PutStatus.OK;
		}

		/** 2. check if previous exists **/
		VersionKey newVersionKey = new VersionKey(versionKey);
		Data previousVersion = null;
		for (Number160 key : history.keySet()) {
			Data data = history.get(key);
			// TODO verify why 'hash()' returns only 160bit instad of 96bit number
			// TODO how to compare two hashes?
			if (data.hash().equals(newVersionKey.getPreviousHash())) {
				previousVersion = data;
				break;
			}
		}

		if (previousVersion == null) {
			// previous version not found
			logger.error("Previous version with key " + newVersionKey.getPreviousHash() + " not found");
			return PutStatus.VERSION_CONFLICT;
		}

		/** 3. Check if previous version is latest one **/
		VersionKey latestVersionKey = null;
		for (Number160 key : history.keySet()) {
			VersionKey version = new VersionKey(key);
			if (latestVersionKey == null || latestVersionKey.getTimestamp() < version.getTimestamp()) {
				latestVersionKey = version;
			}
		}
		Data latestVersion = history.get(latestVersionKey.getVersionKey());

		if (latestVersion == null) {
			logger.error("Latest version not found. This should have never happened");
			return PutStatus.VERSION_CONFLICT;
		}

		if (latestVersion == previousVersion) {
			// previous version is the latest one (continue).
			logger.debug("New content is based on latest version.");
			return PutStatus.OK;
		} else {
			// previous version is already outdated (or not existent)
			logger.error("New content does not base on latest version in storage");
			return PutStatus.VERSION_CONFLICT;
		}
	}
}
