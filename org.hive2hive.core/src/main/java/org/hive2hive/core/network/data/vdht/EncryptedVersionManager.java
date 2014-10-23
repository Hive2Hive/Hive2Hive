package org.hive2hive.core.network.data.vdht;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import javax.crypto.SecretKey;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.DigestResult;
import net.tomp2p.storage.Data;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.model.versioned.BaseVersionedNetworkContent;
import org.hive2hive.core.model.versioned.EncryptedNetworkContent;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.DataManager.H2HPutStatus;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.IH2HEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptedVersionManager<T extends BaseVersionedNetworkContent> {

	private static final Logger logger = LoggerFactory.getLogger(EncryptedVersionManager.class);

	private final DataManager dataManager;
	private final IH2HEncryption encryption;

	private final SecretKey encryptionKey;
	private final Parameters parameters;

	private Random random = new Random();

	// limit constants
	private final int getFailedLimit = 2;
	private final int forkAfterGetLimit = 2;
	private final int delayLimit = 2;

	// caches
	private Cache<Set<Number160>> digestCache = new Cache<Set<Number160>>();
	private Cache<EncryptedNetworkContent> encryptedContentCache = new Cache<EncryptedNetworkContent>();
	private Cache<T> contentCache = new Cache<T>();

	public EncryptedVersionManager(DataManager dataManager, SecretKey encryptionKey, String locationKey, String contentKey) {
		this(dataManager, dataManager.getEncryption(), encryptionKey, locationKey, contentKey);
	}

	public EncryptedVersionManager(DataManager dataManager, IH2HEncryption encryption, SecretKey encryptionKey, String locationKey,
			String contentKey) {
		this.dataManager = dataManager;
		this.encryption = encryption;
		this.encryptionKey = encryptionKey;
		this.parameters = new Parameters().setLocationKey(locationKey).setContentKey(contentKey);
	}

	/**
	 * Performs a get call (blocking) and decrypts the received version.
	 * 
	 * @throws GetFailedException
	 */
	@SuppressWarnings("unchecked")
	public T get() throws GetFailedException {
		// load the current digest list from network
		NavigableMap<Number640, Collection<Number160>> digest = dataManager.getDigestLatest(parameters);
		// compare the latest version key with the cached one
		if (!contentCache.isEmpty() && digest != null && digest.lastEntry() != null
				&& digest.lastEntry().getKey().versionKey().equals(contentCache.lastKey())) {
			logger.debug("No need for getting from network. Returning cached version. {}", parameters.toString());
			return contentCache.lastEntry().getValue();
		} else {
			int delayCounter = 0;
			int delayWaitTime = random.nextInt(1000) + 1000;
			int forkAfterGetCounter = 0;
			int forkAfterGetWaitTime = random.nextInt(1000) + 1000;
			// fetch latest versions from the network, request also digest
			while (true) {
				Cache<EncryptedNetworkContent> fetchedVersions = new Cache<EncryptedNetworkContent>();
				int getCounter = 0;
				int getWaitTime = random.nextInt(1000) + 1000;
				while (true) {
					// load latest data
					FutureGet futureGet = dataManager.getLatestUnblocked(parameters);
					futureGet.awaitUninterruptibly();

					// build and merge the version tree from raw digest result;
					digestCache.putAll(buildDigest(futureGet.rawDigest()));
					// join all freshly loaded versions in one map
					fetchedVersions.putAll(buildData(futureGet.rawData()));
					// merge freshly loaded versions with cache
					encryptedContentCache.putAll(fetchedVersions);

					// check if get was successful
					if (futureGet.isFailed() || fetchedVersions.isEmpty()) {
						if (getCounter > getFailedLimit) {
							logger.warn("Loading of data failed after {} tries. {}", getCounter, parameters.toString());
							throw new GetFailedException("Couldn't load data.");
						} else {
							logger.warn("Couldn't get data. Try #{}. Retrying. reason = '{}' {}", getCounter++,
									futureGet.failedReason(), parameters.toString());

							// TODO reput latest versions for maintenance

							// exponential back off waiting
							try {
								Thread.sleep(getWaitTime);
								getWaitTime = getWaitTime * 2;
							} catch (InterruptedException ignore) {
							}
						}
					} else {
						break;
					}
				}

				// check if version delays or forks occurred
				if (hasVersionDelay(fetchedVersions, digestCache) && delayCounter < delayLimit) {
					logger.warn("Detected a version delay. #{}", delayCounter++);

					// TODO reput latest versions for maintenance, consider only latest

					// exponential back off waiting
					try {
						Thread.sleep(delayWaitTime);
						delayWaitTime = delayWaitTime * 2;
					} catch (InterruptedException ignore) {
					}
					continue;
				}

				// get latest versions according cache
				Cache<Set<Number160>> latestVersionKeys = getLatest(digestCache);

				// check for version fork
				if (latestVersionKeys.size() > 1 && delayCounter < delayLimit) {
					if (forkAfterGetCounter < forkAfterGetLimit) {
						logger.warn("Got a version fork. Waiting. #{}", forkAfterGetCounter++);
						// exponential back off waiting
						try {
							Thread.sleep(forkAfterGetWaitTime);
							forkAfterGetWaitTime = forkAfterGetWaitTime * 2;
						} catch (InterruptedException ignore) {
						}
						continue;
					}
					logger.warn("Got a version fork.");

					// TODO implement merging

					throw new GetFailedException("Got a version fork.");
				} else {
					if (delayCounter >= delayLimit) {
						logger.warn("Ignoring delay after {} retries.", delayCounter);
					}
					if (encryptedContentCache.isEmpty()) {
						logger.warn("Did not find any version.");
						throw new GetFailedException("No version found. Got null.");
					} else {
						try {
							logger.trace("Decrypting with 256-bit AES key.");
							EncryptedNetworkContent encrypted = encryptedContentCache.lastEntry().getValue();
							T decrypted = (T) encryption.decryptAES((EncryptedNetworkContent) encrypted, encryptionKey);
							decrypted.setVersionKey(encrypted.getVersionKey());
							decrypted.setBasedOnKey(encrypted.getBasedOnKey());

							// cache user profile
							contentCache.put(encrypted.getVersionKey(), decrypted);

							return decrypted;
						} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
							logger.error("Cannot decrypt the version.");
							throw new GetFailedException("Cannot decrypt the version.");
						} catch (Exception e) {
							logger.error("Cannot get the version.", e);
							throw new GetFailedException(String.format("Cannot get the version. reason = '%s'",
									e.getMessage()));
						}
					}
				}
			}
		}
	}

	/**
	 * Encrypts the modified user profile and puts it (blocking).
	 * 
	 * @throws PutFailedException
	 */
	public void put(T networkContent, KeyPair protectionKeys) throws PutFailedException {
		try {
			logger.trace("Encrypting with 256bit AES.");
			EncryptedNetworkContent encrypted = encryption.encryptAES(networkContent, encryptionKey);
			encrypted.setBasedOnKey(networkContent.getBasedOnKey());
			encrypted.setVersionKey(networkContent.getVersionKey());
			encrypted.generateVersionKey();

			IParameters parameters = new Parameters().setLocationKey(this.parameters.getLocationKey())
					.setContentKey(this.parameters.getContentKey()).setVersionKey(encrypted.getVersionKey())
					.setBasedOnKey(encrypted.getBasedOnKey()).setNetworkContent(encrypted).setProtectionKeys(protectionKeys)
					.setTTL(networkContent.getTimeToLive()).setPrepareFlag(true);

			H2HPutStatus status = dataManager.put(parameters);
			if (status.equals(H2HPutStatus.FAILED)) {
				throw new PutFailedException("Put failed.");
			} else if (status.equals(H2HPutStatus.VERSION_FORK)) {
				logger.warn("Version fork after put detected. Rejecting put");
				if (!dataManager.remove(parameters)) {
					logger.warn("Removing of conflicting version failed.");
				}
				throw new VersionForkAfterPutException();
			} else {
				networkContent.setVersionKey(encrypted.getVersionKey());
				networkContent.setBasedOnKey(encrypted.getBasedOnKey());
				// cache digest
				digestCache.put(parameters.getVersionKey(), new HashSet<Number160>(parameters.getData().basedOnSet()));
				// cache network content
				contentCache.put(parameters.getVersionKey(), networkContent);
				// cache encrypted network content
				encryptedContentCache.put(parameters.getVersionKey(), encrypted);
			}
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException | IOException e) {
			logger.error("Cannot encrypt the user profile. reason = '{}'", e.getMessage());
			throw new PutFailedException(String.format("Cannot encrypt the user profile. reason = '%s'", e.getMessage()));
		}
	}

	private NavigableMap<Number160, Set<Number160>> buildDigest(Map<PeerAddress, DigestResult> rawDigest) {
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

	private NavigableMap<Number160, EncryptedNetworkContent> buildData(Map<PeerAddress, Map<Number640, Data>> rawData) {
		NavigableMap<Number160, EncryptedNetworkContent> dataMap = new TreeMap<Number160, EncryptedNetworkContent>();
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
						Object object = tmp.get(key).object();
						if (object instanceof EncryptedNetworkContent) {
							dataMap.put(key.versionKey(), (EncryptedNetworkContent) tmp.get(key).object());
						} else {
							logger.warn("Received unkown object = '{}'", object.getClass().getSimpleName());
						}
					} catch (ClassNotFoundException | IOException e) {
						logger.warn("Could not get data. reason = '{}'", e.getMessage());
					}
				}
			}
		}
		return dataMap;
	}

	private boolean hasVersionDelay(Map<Number160, ?> latestVersions, Cache<Set<Number160>> digestCache) {
		for (Number160 version : digestCache.keySet()) {
			for (Number160 basedOnKey : digestCache.get(version)) {
				if (latestVersions.containsKey(basedOnKey)) {
					return true;
				}
			}
		}
		return false;
	}

	private Cache<Set<Number160>> getLatest(Cache<Set<Number160>> cache) {
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

	private void deletePredecessors(Number160 key, Cache<Set<Number160>> cache) {
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

	private class Cache<V> extends TreeMap<Number160, V> {

		private static final long serialVersionUID = 7731754953812711346L;

		public Cache() {
			super();
		}

		public Cache(Cache<V> cache) {
			super(cache);
		}

		@Override
		public void putAll(Map<? extends Number160, ? extends V> map) {
			super.putAll(map);
			cleanUp();
		}

		public V put(Number160 key, V value) {
			try {
				return super.put(key, value);
			} finally {
				cleanUp();
			}
		}

		private void cleanUp() {
			if (!isEmpty()) {
				while (firstKey().timestamp() + H2HConstants.MAX_VERSIONS_HISTORY <= lastKey().timestamp()) {
					pollFirstEntry();
				}
			}
		}
	}

}
