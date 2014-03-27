package org.hive2hive.core.network.data;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import net.tomp2p.connection.SignatureFactory;
import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.message.SignatureCodec;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.builder.DigestBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.futures.FutureChangeProtectionListener;
import org.hive2hive.core.network.data.futures.FutureGetListener;
import org.hive2hive.core.network.data.futures.FuturePutListener;
import org.hive2hive.core.network.data.futures.FutureRemoveListener;
import org.hive2hive.core.security.H2HSignatureCodec;
import org.hive2hive.core.security.H2HSignatureFactory;

public class DataManager implements IDataManager {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(DataManager.class);

	private final NetworkManager networkManager;
	private final SignatureFactory signatureFactory;
	private final SignatureCodec signatureCodec;

	public DataManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
		this.signatureFactory = new H2HSignatureFactory();
		this.signatureCodec = new H2HSignatureCodec();
	}

	/**
	 * Helper to get the <code>TomP2P</code> peer.
	 * 
	 * @return the current peer
	 */
	private Peer getPeer() {
		return networkManager.getConnection().getPeer();
	}

	@Override
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

	@Override
	public boolean changeProtectionKey(String locationKey, String contentKey, Number160 versionKey,
			Number160 basedOnKey, int ttl, KeyPair oldKey, KeyPair newKey, byte[] hash) {
		Number160 lKey = Number160.createHash(locationKey);
		Number160 dKey = H2HConstants.TOMP2P_DEFAULT_KEY;
		Number160 cKey = Number160.createHash(contentKey);
		FuturePut putFuture = changeProtectionKey(lKey, dKey, cKey, versionKey, basedOnKey, ttl, oldKey,
				newKey, hash);
		if (putFuture == null) {
			return false;
		}

		FutureChangeProtectionListener listener = new FutureChangeProtectionListener(lKey, dKey, cKey,
				versionKey);
		putFuture.addListener(listener);
		return listener.await();
	}

	@Override
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
				.format("put content = '%s' location key = '%s' domain key = '%s' content key = '%s' version key = '%s' protected = '%b'",
						content.getClass().getSimpleName(), locationKey, domainKey, contentKey,
						content.getVersionKey(), protectionKey != null));
		try {
			Data data = new Data(content);
			data.ttlSeconds(content.getTimeToLive()).basedOn(content.getBasedOnKey());
			if (protectionKey != null) {
				data.setProtectedEntry().sign(protectionKey, signatureFactory);

				// check if content can be shared
				if (content instanceof SharableNetworkContent) {
					// decrypt signature to get hash of the object
					Cipher rsa = Cipher.getInstance("RSA");
					rsa.init(Cipher.DECRYPT_MODE, protectionKey.getPublic());
					byte[] hash = rsa.doFinal(data.signature().encode());
					// store hash
					((SharableNetworkContent) content).setHash(hash);
				}

				return getPeer().put(locationKey).setData(contentKey, data).setDomainKey(domainKey)
						.setVersionKey(content.getVersionKey()).keyPair(protectionKey).start();
			} else {
				return getPeer().put(locationKey).setData(contentKey, data).setDomainKey(domainKey)
						.setVersionKey(content.getVersionKey()).start();
			}
		} catch (IOException | InvalidKeyException | SignatureException | NoSuchAlgorithmException
				| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			logger.error(String
					.format("Put failed. location key = '%s' domain key = '%s' content key = '%s' version key = '%s' exception = '%s'",
							locationKey, domainKey, contentKey, content.getVersionKey(), e.getMessage()));
			return null;
		}
	}

	public FuturePut changeProtectionKey(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			Number160 versionKey, Number160 basedOnKey, int ttl, KeyPair oldProtectionKey,
			KeyPair newProtectionKey, byte[] hash) {
		logger.debug(String
				.format("change content protection key location key = '%s' domain key = '%s' content key = '%s' version key '%s'",
						locationKey, domainKey, contentKey, versionKey));
		try {
			// create dummy object to change the protection key
			Data data = new Data().ttlSeconds(ttl).basedOn(basedOnKey);

			// encrypt hash with new key pair to get the new signature (without having the data object)
			Cipher rsa = Cipher.getInstance("RSA");
			rsa.init(Cipher.ENCRYPT_MODE, newProtectionKey.getPrivate());
			byte[] newSignature = rsa.doFinal(hash);

			// sign duplicated meta (don't forget to set signed flag)
			data = data.signature(signatureCodec.decode(newSignature)).signed(true).duplicateMeta();

			// change the protection key through a put meta
			return getPeer().put(locationKey).setDomainKey(domainKey).putMeta().setData(contentKey, data)
					.setVersionKey(versionKey).keyPair(oldProtectionKey).start();
		} catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException e) {
			logger.error(String
					.format("Change protection key failed. location key = '%s' domain key = '%s' content key = '%s' version key = '%s' exception = '%s'",
							locationKey, domainKey, contentKey, versionKey, e.getMessage()));
			return null;
		}
	}

	@Override
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

	@Override
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

	@Override
	public boolean remove(String locationKey, String contentKey, KeyPair protectionKey) {
		Number160 lKey = Number160.createHash(locationKey);
		Number160 dKey = H2HConstants.TOMP2P_DEFAULT_KEY;
		Number160 cKey = Number160.createHash(contentKey);

		FutureRemove futureRemove = remove(lKey, dKey, cKey, protectionKey);
		FutureRemoveListener listener = new FutureRemoveListener(lKey, dKey, cKey, protectionKey, this);
		futureRemove.addListener(listener);
		return listener.await();
	}

	@Override
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

	@Override
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
