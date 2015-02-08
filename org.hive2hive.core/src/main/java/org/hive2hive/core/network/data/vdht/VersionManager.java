package org.hive2hive.core.network.data.vdht;

import java.security.KeyPair;
import java.util.Collection;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Set;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.model.versioned.BaseVersionedNetworkContent;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.DataManager.H2HPutStatus;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionManager<T extends BaseVersionedNetworkContent> extends BaseVersionManager<T> {

	private static final Logger logger = LoggerFactory.getLogger(VersionManager.class);

	private final DataManager dataManager;

	public VersionManager(DataManager dataManager, String locationKey, String contentKey) {
		super(dataManager, locationKey, contentKey);
		this.dataManager = dataManager;
	}

	/**
	 * Performs a get call (blocking).
	 * 
	 * @throws GetFailedException
	 */
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
				Cache<T> fetchedVersions = new Cache<T>();
				int getCounter = 0;
				int getWaitTime = random.nextInt(1000) + 1000;
				while (true) {
					// load latest data
					FutureGet futureGet = dataManager.getLatestUnblocked(parameters);
					futureGet.awaitUninterruptibly(H2HConstants.AWAIT_NETWORK_OPERATION_MS);

					// build and merge the version tree from raw digest result;
					digestCache.putAll(buildDigest(futureGet.rawDigest()));
					// join all freshly loaded versions in one map
					fetchedVersions.putAll(buildData(futureGet.rawData()));
					// merge freshly loaded versions with cache
					contentCache.putAll(fetchedVersions);

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
					if (contentCache.isEmpty()) {
						logger.warn("Did not find any version.");
						throw new GetFailedException("No version found. Got null.");
					} else {
						try {
							return (T) contentCache.lastEntry().getValue();
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
		networkContent.generateVersionKey();

		IParameters parameters = new Parameters().setLocationKey(this.parameters.getLocationKey())
				.setContentKey(this.parameters.getContentKey()).setVersionKey(networkContent.getVersionKey())
				.setBasedOnKey(networkContent.getBasedOnKey()).setNetworkContent(networkContent)
				.setProtectionKeys(protectionKeys).setTTL(networkContent.getTimeToLive()).setPrepareFlag(true);

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
			// cache digest
			digestCache.put(parameters.getVersionKey(), new HashSet<Number160>(parameters.getData().basedOnSet()));
			// cache network content
			contentCache.put(parameters.getVersionKey(), networkContent);
		}
	}
}
