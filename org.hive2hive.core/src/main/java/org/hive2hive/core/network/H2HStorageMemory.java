package org.hive2hive.core.network;

import java.security.PublicKey;
import java.util.NavigableMap;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.StorageLayer;
import net.tomp2p.storage.StorageMemory;

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

	public H2HStorageMemory() {
		super(new StorageMemory());
	}

	@Override
	public Enum<?> put(Number640 key, Data newData, PublicKey publicKey, boolean putIfAbsent, boolean domainProtection) {
		if (H2HConstants.REMOTE_VERIFICATION_ENABLED) {
			logger.trace("Start put verification. Location key = '{}', Content key = '{}', Version key = '{}'.",
					key.getLocationKey(), key.getContentKey(), key.getVersionKey());

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
					status, key.getLocationKey(), key.getContentKey(), key.getVersionKey());
			return status;
		} else {
			logger.trace("Disabled the put verification strategy on the remote peer.");
			return super.put(key, newData, publicKey, putIfAbsent, domainProtection);
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
		NavigableMap<Number640, Number160> history = getHistoryOnStorage(key);

		/** 1. if version key is zero **/
		if (key.getVersionKey().equals(Number160.ZERO)) {
			if (history.isEmpty()) {
				logger.trace("Initialy putting content with no version key.");
				return PutStatusH2H.OK;
			} else if (history.size() == 1 && history.firstKey().getVersionKey().equals(Number160.ZERO)) {
				logger.trace("Overwriting content with no versioning.");
				return PutStatusH2H.OK;
			} else {
				logger.warn("Trying to overwrite current version with content with no version key.");
				return PutStatusH2H.VERSION_CONFLICT_NO_VERSION_KEY;
			}
		}

		/** 1. if version is null or zero and no history yet, it is the first entry here **/
		if (newData.basedOn().equals(Number160.ZERO)) {
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
		if (!history.lastKey().getVersionKey().equals(newData.basedOn())) {
			logger.warn("New data is not based on previous version. Previous version key = '{}'.", key.getVersionKey());
			return PutStatusH2H.VERSION_CONFLICT;
		}

		/** 3. Check if previous version is latest one **/
		if (newData.basedOn().timestamp() < key.getVersionKey().timestamp()) {
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
		NavigableMap<Number640, Number160> history = getHistoryOnStorage(key);

		// long now = System.currentTimeMillis();
		while (history.size() > H2HConstants.MAX_VERSIONS_HISTORY) {
			Number640 toRemove = history.firstKey();
			// if (toRemove.getVersionKey().timestamp() + H2HConstants.MIN_VERSION_AGE_BEFORE_REMOVAL_MS >
			// now) {
			// // stop removal because oldest version is too 'young'
			// break;
			// } else {
			logger.trace("Removing an older version. Version key = '{}'.", key.getVersionKey());
			history.remove(toRemove);
			super.remove(toRemove, publicKey, false);
			// }
		}
	}

	private NavigableMap<Number640, Number160> getHistoryOnStorage(Number640 key) {
		return super.digest(new Number640(key.getLocationKey(), key.getDomainKey(), key.getContentKey(), Number160.ZERO),
				new Number640(key.getLocationKey(), key.getDomainKey(), key.getContentKey(), Number160.MAX_VALUE), -1, true)
				.getDigests();
	}
}
