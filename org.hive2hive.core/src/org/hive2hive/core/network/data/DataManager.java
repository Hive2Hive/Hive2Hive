package org.hive2hive.core.network.data;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.SignatureException;

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

/**
 * This class offers an interface for putting, getting and removing data from the network.
 * 
 * @author Seppi
 */
public class DataManager {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(DataManager.class);

	private final NetworkManager networkManager;

	public DataManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	/**
	 * Helper to get the <code>TomP2P</code> peer.
	 * 
	 * @return the current peer
	 */
	private Peer getPeer() {
		return networkManager.getConnection().getPeer();
	}

	public boolean put(String locationKey, String contentKey, NetworkContent content, KeyPair protectionKey) {
		Number160 lKey = Number160.createHash(locationKey);
		Number160 dKey = H2HConstants.TOMP2P_DEFAULT_KEY;
		Number160 cKey = Number160.createHash(contentKey);
		FuturePut putFuture = put(lKey, dKey, cKey, content, protectionKey);
		if (putFuture == null) {
			return false;
		}

		FuturePutListener listener = new FuturePutListener(lKey, dKey, cKey, content, protectionKey, this);
		putFuture.addListener(listener);
		return listener.await();
	}

	public boolean putUserProfileTask(String userId, Number160 contentKey, NetworkContent content,
			KeyPair protectionKey) {
		Number160 lKey = Number160.createHash(userId);
		Number160 dKey = Number160.createHash(H2HConstants.USER_PROFILE_TASK_DOMAIN);
		FuturePut putFuture = put(lKey, dKey, contentKey, content, protectionKey);
		if (putFuture == null) {
			return false;
		}

		FuturePutListener listener = new FuturePutListener(lKey, dKey, contentKey, content, protectionKey,
				this);
		putFuture.addListener(listener);
		return listener.await();
	}

	public FuturePut put(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			NetworkContent content, KeyPair protectionKey) {
		logger.debug(String
				.format("put content = '%s' location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
						content.getClass().getSimpleName(), locationKey, domainKey, contentKey,
						content.getVersionKey()));
		try {
			Data data = new Data(content);
			data.ttlSeconds(content.getTimeToLive()).basedOn(content.getBasedOnKey());
			if (protectionKey != null) {
				data.setProtectedEntry().sign(protectionKey);
				return getPeer().put(locationKey).setData(contentKey, data).setDomainKey(domainKey)
						.setVersionKey(content.getVersionKey()).keyPair(protectionKey).start();
			} else {
				return getPeer().put(locationKey).setData(contentKey, data).setDomainKey(domainKey)
						.setVersionKey(content.getVersionKey()).start();
			}
		} catch (IOException | InvalidKeyException | SignatureException e) {
			logger.error(String
					.format("Put failed. location key = '%s' domain key = '%s' content key = '%s' version key = '%s' exception = '%s'",
							locationKey, domainKey, contentKey, content.getVersionKey(), e.getMessage()));
			return null;
		}
	}

	public NetworkContent get(String locationKey, String contentKey) {
		Number160 lKey = Number160.createHash(locationKey);
		Number160 dKey = H2HConstants.TOMP2P_DEFAULT_KEY;
		Number160 cKey = Number160.createHash(contentKey);

		FutureGet futureGet = get(lKey, dKey, cKey);
		FutureGetListener listener = new FutureGetListener(lKey, dKey, cKey, this);
		futureGet.addListener(listener);
		return listener.awaitAndGet();
	}

	public NetworkContent get(String locationKey, String contentKey, Number160 versionKey) {
		Number160 lKey = Number160.createHash(locationKey);
		Number160 dKey = H2HConstants.TOMP2P_DEFAULT_KEY;
		Number160 cKey = Number160.createHash(contentKey);

		FutureGet futureGet = get(lKey, dKey, cKey, versionKey);
		FutureGetListener listener = new FutureGetListener(lKey, dKey, cKey, versionKey, this);
		futureGet.addListener(listener);
		return listener.awaitAndGet();
	}

	public NetworkContent getUserProfileTask(String userId) {
		Number160 lKey = Number160.createHash(userId);
		Number160 dKey = Number160.createHash(H2HConstants.USER_PROFILE_TASK_DOMAIN);

		FutureGet futureGet = getPeer().get(lKey)
				.from(new Number640(lKey, dKey, Number160.ZERO, Number160.ZERO))
				.to(new Number640(lKey, dKey, Number160.MAX_VALUE, Number160.MAX_VALUE)).ascending()
				.returnNr(1).start();
		FutureGetListener listener = new FutureGetListener(lKey, dKey, this);
		futureGet.addListener(listener);
		return listener.awaitAndGet();
	}

	public FutureGet get(Number160 locationKey, Number160 domainKey, Number160 contentKey) {
		logger.debug(String.format("get location key = '%s' domain key = '%s' content key = '%s'",
				locationKey, domainKey, contentKey));
		return getPeer().get(locationKey)
				.from(new Number640(locationKey, domainKey, contentKey, Number160.ZERO))
				.to(new Number640(locationKey, domainKey, contentKey, Number160.MAX_VALUE)).descending()
				.returnNr(1).start();
	}

	public FutureGet get(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			Number160 versionKey) {
		logger.debug(String.format(
				"get location key = '%s' domain Key = '%s' content key = '%s' version key = '%s'",
				locationKey, domainKey, contentKey, versionKey));
		return getPeer().get(locationKey).setDomainKey(domainKey).setContentKey(contentKey)
				.setVersionKey(versionKey).start();
	}

	public boolean remove(String locationKey, String contentKey, KeyPair protectionKey) {
		Number160 lKey = Number160.createHash(locationKey);
		Number160 dKey = H2HConstants.TOMP2P_DEFAULT_KEY;
		Number160 cKey = Number160.createHash(contentKey);

		FutureRemove futureRemove = remove(lKey, dKey, cKey, protectionKey);
		FutureRemoveListener listener = new FutureRemoveListener(lKey, dKey, cKey, protectionKey, this);
		futureRemove.addListener(listener);
		return listener.await();
	}

	public boolean remove(String locationKey, String contentKey, Number160 versionKey, KeyPair protectionKey) {
		Number160 lKey = Number160.createHash(locationKey);
		Number160 dKey = H2HConstants.TOMP2P_DEFAULT_KEY;
		Number160 cKey = Number160.createHash(contentKey);

		FutureRemove futureRemove = remove(lKey, dKey, cKey, versionKey, protectionKey);
		FutureRemoveListener listener = new FutureRemoveListener(lKey, dKey, cKey, versionKey, protectionKey,
				this);
		futureRemove.addListener(listener);
		return listener.await();
	}

	public boolean removeUserProfileTask(String userId, Number160 contentKey, KeyPair protectionKey) {
		Number160 lKey = Number160.createHash(userId);
		Number160 dKey = Number160.createHash(H2HConstants.USER_PROFILE_TASK_DOMAIN);

		FutureRemove futureRemove = remove(lKey, dKey, contentKey, protectionKey);
		FutureRemoveListener listener = new FutureRemoveListener(lKey, dKey, contentKey, protectionKey, this);
		futureRemove.addListener(listener);
		return listener.await();
	}

	public FutureRemove remove(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			KeyPair protectionKey) {
		logger.debug(String.format("remove location key = '%s' domain key = '%s' content key = '%s'",
				locationKey, domainKey, contentKey));
		return getPeer().remove(locationKey)
				.from(new Number640(locationKey, domainKey, contentKey, Number160.ZERO))
				.to(new Number640(locationKey, domainKey, contentKey, Number160.MAX_VALUE))
				.keyPair(protectionKey).start();
	}

	public FutureRemove remove(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			Number160 versionKey, KeyPair protectionKey) {
		logger.debug(String.format(
				"remove location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
				locationKey, domainKey, contentKey, versionKey));
		return getPeer().remove(locationKey).setDomainKey(domainKey).contentKey(contentKey)
				.setVersionKey(versionKey).keyPair(protectionKey).start();
	}

	public DigestBuilder getDigest(Number160 locationKey) {
		return getPeer().digest(locationKey);
	}

}
