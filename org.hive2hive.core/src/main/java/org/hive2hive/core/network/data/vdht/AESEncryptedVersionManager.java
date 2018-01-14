package org.hive2hive.core.network.data.vdht;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import javax.crypto.SecretKey;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;

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

public class AESEncryptedVersionManager<T extends BaseVersionedNetworkContent> extends BaseVersionManager<T> {

	private static final Logger logger = LoggerFactory.getLogger(AESEncryptedVersionManager.class);

	private final IH2HEncryption encryption;
	private final SecretKey encryptionKey;

	// additional cache for encrypted data
	private Cache<EncryptedNetworkContent> encryptedContentCache = new Cache<EncryptedNetworkContent>();

	public AESEncryptedVersionManager(DataManager dataManager, SecretKey encryptionKey, String locationKey,
			String contentKey) {
		this(dataManager, dataManager.getEncryption(), encryptionKey, locationKey, contentKey);
	}

	public AESEncryptedVersionManager(DataManager dataManager, IH2HEncryption encryption, SecretKey encryptionKey,
			String locationKey, String contentKey) {
		super(dataManager, locationKey, contentKey);
		this.encryption = encryption;
		this.encryptionKey = encryptionKey;
	}

	/**
	 * Performs a get call (blocking) and decrypts the received version.
	 * 
	 * @return the fetched data
	 * @throws GetFailedException if the data cannot be get
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
					futureGet.awaitUninterruptibly(H2HConstants.AWAIT_NETWORK_OPERATION_MS);

					// build and merge the version tree from raw digest result;
					digestCache.putAll(buildDigest(futureGet.rawDigest()));
					// join all freshly loaded versions in one map
					fetchedVersions
							.putAll((Map<Number160, ? extends EncryptedNetworkContent>) buildData(futureGet.rawData()));
					// merge freshly loaded versions with cache
					encryptedContentCache.putAll(fetchedVersions);

					// check if get was successful
					if (futureGet.isFailed() || fetchedVersions.isEmpty()) {
						if (getCounter > GET_FAILED_LIMIT) {
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
				if (hasVersionDelay(fetchedVersions, digestCache) && delayCounter < DELAY_LIMIT) {
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
				if (latestVersionKeys.size() > 1 && delayCounter < DELAY_LIMIT) {
					if (forkAfterGetCounter < FORK_AFTER_GET_LIMIT) {
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
					if (delayCounter >= DELAY_LIMIT) {
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
						} catch (GeneralSecurityException e) {
							logger.error("Cannot decrypt the version.");
							throw new GetFailedException("Cannot decrypt the version.");
						} catch (Exception e) {
							logger.error("Cannot get the version.", e);
							throw new GetFailedException(
									String.format("Cannot get the version. reason = '%s'", e.getMessage()));
						}
					}
				}
			}
		}
	}

	/**
	 * Encrypts the modified user profile and puts it (blocking).
	 * 
	 * @param networkContent the content to store
	 * @param protectionKeys the keys to protect the data
	 * @throws PutFailedException if the content cannot be put.
	 */
	public void put(T networkContent, KeyPair protectionKeys) throws PutFailedException {
		try {
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
		} catch (GeneralSecurityException | IOException e) {
			logger.error("Cannot encrypt the user profile. reason = '{}'", e.getMessage());
			throw new PutFailedException(String.format("Cannot encrypt the user profile. reason = '%s'", e.getMessage()));
		}
	}
}
