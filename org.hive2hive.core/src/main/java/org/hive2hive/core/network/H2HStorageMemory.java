package org.hive2hive.core.network;

import java.security.PublicKey;
import java.util.Collection;
import java.util.NavigableMap;

import net.tomp2p.dht.StorageLayer;
import net.tomp2p.dht.StorageMemory;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Every <code>Hive2Hive</code> node has validation strategies when data is
 * stored. This is realized by a customized storage memory. Before some data
 * gets stored on a node the put method gets called where the node can verify
 * the store request.
 * 
 * @author Seppi, Nico
 */
public class H2HStorageMemory extends StorageLayer {

	private static final Logger logger = LoggerFactory.getLogger(H2HStorageMemory.class);

	public enum PutStatusH2H {
		OK,
		FAILED_NOT_ABSENT,
		FAILED_SECURITY,
		FAILED,
		VERSION_CONFLICT,
		VERSION_CONFLICT_NO_VERSION_KEY,
		VERSION_CONFLICT_NO_BASED_ON,
		VERSION_CONFLICT_OLD_TIMESTAMP,
	};

	public enum StorageMemoryMode {
		/** the normal behavior, where each 'put' is checked for version conflicts */
		ENABLE_REMOTE_VERIFICATION,

		/** The extra verification is disabled, {@link StorageLayer} implementation is used instead */
		DISABLE_VERIFICATION,

		/** Every request to store will fail and returns a {@link PutStatusH2H#FAILED} */
		DENY_ALL
	}

	private StorageMemoryMode mode;

	public H2HStorageMemory() {
		super(new StorageMemory());
		this.mode = StorageMemoryMode.ENABLE_REMOTE_VERIFICATION;
	}

	public void setMode(StorageMemoryMode mode) {
		assert mode != null;
		this.mode = mode;
	}

	@Override
	public Enum<?> put(Number640 key, Data newData, PublicKey publicKey, boolean putIfAbsent, boolean domainProtection) {
		switch (mode) {
			case ENABLE_REMOTE_VERIFICATION: {
				logger.trace("Start put verification. Location key = '{}', Content key = '{}', Version key = '{}'.",
						key.locationKey(), key.contentKey(), key.versionKey());

				if (isProtectionKeyChange(newData)) {
					logger.trace("Only chaning the protection key, no need to verify the versions.");
					return super.put(key, newData, publicKey, putIfAbsent, domainProtection);
				}

				Enum<?> status = validateVersion(key, newData);
				if (status == PutStatusH2H.OK) {
					status = super.put(key, newData, publicKey, putIfAbsent, domainProtection);

					// after adding the content to the memory, old versions should be cleaned up. How many old
					// versions we keep can be parameterized in the constants.
					cleanupVersions(key, publicKey);
				}

				logger.trace(
						"Put verification finished with status '{}'. Location key = '{}', Content key = '{}', Version key = '{}'.",
						status, key.locationKey(), key.contentKey(), key.versionKey());
				return status;
			}
			case DISABLE_VERIFICATION: {
				logger.trace("Disabled the put verification strategy on the remote peer.");
				return super.put(key, newData, publicKey, putIfAbsent, domainProtection);
			}
			case DENY_ALL: {
				logger.warn("Memory mode is denying the put request.");
				return PutStatusH2H.FAILED;
			}
			default: {
				logger.error("Invald mode {}. Returning a failure", mode);
				return PutStatusH2H.FAILED;
			}
		}
	}

	/**
	 * Returns whether the put data is to change the protection key
	 * 
	 * @param newData
	 * @return
	 */
	private boolean isProtectionKeyChange(Data newData) {
		// TODO: more checks here to enable additionaly security!
		return newData.isMeta();
	}

	/**
	 * Version keys are optimal and have to be in this case {@link Number160#ZERO}. Putting new data with no
	 * version keys is allowed in following cases:
	 * <ul>
	 * <li>Under the given {@link Number640} key has no other entries.</li>
	 * <li>Under the given {@link Number640} key is one and only one entry, where the version key is
	 * {@link Number160#ZERO}.</li>
	 * </ul>
	 * 
	 * @param key
	 * @param newData
	 * @return
	 */
	private PutStatusH2H validateVersion(Number640 key, Data newData) {
		/** 0. get all versions for this locationKey, domainKey and contentKey combination **/
		NavigableMap<Number640, Collection<Number160>> history = getHistoryOnStorage(key);

		/** 1. if version key is zero **/
		if (key.versionKey().equals(Number160.ZERO)) {
			if (history.isEmpty()) {
				logger.trace("Initialy putting content with no version key.");
				return PutStatusH2H.OK;
			} else if (history.size() == 1 && history.firstKey().versionKey().equals(Number160.ZERO)) {
				logger.trace("Overwriting content with no versioning.");
				return PutStatusH2H.OK;
			} else {
				logger.warn("Trying to overwrite current version with content with no version key.");
				return PutStatusH2H.VERSION_CONFLICT_NO_VERSION_KEY;
			}
		}

		/** 1. if version is null or zero and no history yet, it is the first entry here **/
		if (newData.basedOnSet().isEmpty() || newData.basedOnSet().iterator().next().equals(Number160.ZERO)) {
			if (history.isEmpty()) {
				logger.trace("First version of a content is added.");
				return PutStatusH2H.OK;
			} else {
				logger.warn("History is not empty and not based on key given.");
				return PutStatusH2H.VERSION_CONFLICT_NO_BASED_ON;
			}
		} else if (history.isEmpty()) {
			logger.trace("First version of a content is added, but a based on key is given.");
			return PutStatusH2H.OK;
		}

		/** 2. check if previous exists **/
		if (!history.lastKey().versionKey().equals(newData.basedOnSet().iterator().next())) {
			logger.warn("New data is not based on previous version. Previous version key = '{}'.", key.versionKey());
			return PutStatusH2H.VERSION_CONFLICT;
		}

		/** 3. Check if previous version is latest one **/
		if (newData.basedOnSet().iterator().next().timestamp() < key.versionKey().timestamp()) {
			// previous version is the latest one (continue).
			logger.trace("New content is based on latest version.");
			return PutStatusH2H.OK;
		} else {
			// previous version is newer than the new one
			logger.warn("New content has a older timestamp than the previous version.");
			return PutStatusH2H.VERSION_CONFLICT_OLD_TIMESTAMP;
		}
	}

	// TODO consider fresh version before version cleanup
	private void cleanupVersions(Number640 key, PublicKey publicKey) {
		NavigableMap<Number640, Collection<Number160>> history = getHistoryOnStorage(key);

		// long now = System.currentTimeMillis();
		while (history.size() > H2HConstants.MAX_VERSIONS_HISTORY) {
			Number640 toRemove = history.firstKey();
			// if (toRemove.versionKey().timestamp() + H2HConstants.MIN_VERSION_AGE_BEFORE_REMOVAL_MS >
			// now) {
			// // stop removal because oldest version is too 'young'
			// break;
			// } else {
			logger.trace("Removing an older version. Version key = '{}'.", key.versionKey());
			history.remove(toRemove);
			super.remove(toRemove, publicKey, false);
			// }
		}
	}

	private NavigableMap<Number640, Collection<Number160>> getHistoryOnStorage(Number640 key) {
		return super.digest(new Number640(key.locationKey(), key.domainKey(), key.contentKey(), Number160.ZERO),
				new Number640(key.locationKey(), key.domainKey(), key.contentKey(), Number160.MAX_VALUE), -1, true)
				.digests();
	}
}
