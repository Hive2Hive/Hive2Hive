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
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
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
	public boolean put(IParameters parameters) {
		FuturePut putFuture = putUnblocked(parameters);
		if (putFuture == null) {
			return false;
		}

		FuturePutListener listener = new FuturePutListener(parameters, this);
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
		IParameters parameters = new Parameters().setLocationKey(userId).setContentKey(contentKey)
				.setDomainKey(H2HConstants.USER_PROFILE_TASK_DOMAIN).setData(content)
				.setProtectionKeys(protectionKey);
		FuturePut putFuture = putUnblocked(parameters);
		if (putFuture == null) {
			return false;
		}

		FuturePutListener listener = new FuturePutListener(parameters, this);
		putFuture.addListener(listener);
		return listener.await();
	}

	public FuturePut putUnblocked(IParameters parameters) {
		logger.debug(String.format("Put. %s ", parameters.toString()));
		try {
			Data data = new Data(parameters.getData());
			data.ttlSeconds(parameters.getData().getTimeToLive()).basedOn(
					parameters.getData().getBasedOnKey());
			if (parameters.getProtectionKeys() != null) {
				data.setProtectedEntry().sign(parameters.getProtectionKeys(), signatureFactory);

				// // check if content can be shared
				// if (content instanceof SharableNetworkContent) {
				// // decrypt signature to get hash of the object
				// Cipher rsa = Cipher.getInstance("RSA");
				// rsa.init(Cipher.DECRYPT_MODE, protectionKey.getPublic());
				// byte[] hash = rsa.doFinal(data.signature().encode());
				// // store hash
				// ((SharableNetworkContent) content).setHash(hash);
				// }

				return getPeer().put(parameters.getLKey()).setData(parameters.getCKey(), data)
						.setDomainKey(parameters.getDKey()).setVersionKey(parameters.getVersionKey())
						.keyPair(parameters.getProtectionKeys()).start();
			} else {
				return getPeer().put(parameters.getLKey()).setData(parameters.getCKey(), data)
						.setDomainKey(parameters.getDKey()).setVersionKey(parameters.getVersionKey()).start();
			}
		} catch (IOException | InvalidKeyException | SignatureException e) {
			logger.error(String.format("Put failed. %s exception = '%s'", parameters.toString(),
					e.getMessage()));
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
	public NetworkContent get(IParameters parameters) {
		FutureGet futureGet = getUnblocked(parameters);
		FutureGetListener listener = new FutureGetListener(parameters, false, this);
		futureGet.addListener(listener);
		return listener.awaitAndGet();
	}

	public NetworkContent getVersion(IParameters parameters) {
		FutureGet futureGet = getUnblocked(parameters);
		FutureGetListener listener = new FutureGetListener(parameters, false, this);
		futureGet.addListener(listener);
		return listener.awaitAndGet();
	}

	@Override
	public NetworkContent getUserProfileTask(String userId) {
		IParameters parameters = new Parameters().setLocationKey(userId).setDomainKey(
				H2HConstants.USER_PROFILE_TASK_DOMAIN);

		FutureGet futureGet = getPeer()
				.get(parameters.getLKey())
				.from(new Number640(parameters.getLKey(), parameters.getDKey(), Number160.ZERO,
						Number160.ZERO))
				.to(new Number640(parameters.getLKey(), parameters.getDKey(), Number160.MAX_VALUE,
						Number160.MAX_VALUE)).ascending().returnNr(1).start();
		FutureGetListener listener = new FutureGetListener(parameters, true, this);
		futureGet.addListener(listener);
		return listener.awaitAndGet();
	}

	public FutureGet getUnblocked(IParameters parameters) {
		logger.debug(String.format("Get. %s", parameters.toString()));
		return getPeer()
				.get(parameters.getLKey())
				.from(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(),
						Number160.ZERO))
				.to(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(),
						Number160.MAX_VALUE)).descending().returnNr(1).start();
	}

	public FutureGet getVersionUnblocked(IParameters parameters) {
		logger.debug(String.format("Get version. %s", parameters.toString()));
		return getPeer().get(parameters.getLKey()).setDomainKey(parameters.getDKey())
				.setContentKey(parameters.getCKey()).setVersionKey(parameters.getVersionKey()).start();
	}

	@Override
	public boolean remove(IParameters parameters) {
		FutureRemove futureRemove = removeUnblocked(parameters);
		FutureRemoveListener listener = new FutureRemoveListener(parameters, false, this);
		futureRemove.addListener(listener);
		return listener.await();
	}

	@Override
	public boolean removeVersion(IParameters parameters) {
		FutureRemove futureRemove = removeVersionUnblocked(parameters);
		FutureRemoveListener listener = new FutureRemoveListener(parameters, true, this);
		futureRemove.addListener(listener);
		return listener.await();
	}

	@Override
	public boolean removeUserProfileTask(String userId, Number160 contentKey, KeyPair protectionKey) {
		IParameters parameters = new Parameters().setLocationKey(userId)
				.setDomainKey(H2HConstants.USER_PROFILE_TASK_DOMAIN).setContentKey(contentKey)
				.setProtectionKeys(protectionKey);
		FutureRemove futureRemove = removeUnblocked(parameters);
		FutureRemoveListener listener = new FutureRemoveListener(parameters, true, this);
		futureRemove.addListener(listener);
		return listener.await();
	}

	public FutureRemove removeUnblocked(IParameters parameters) {
		logger.debug(String.format("Remove. %s", parameters.toString()));
		return getPeer()
				.remove(parameters.getLKey())
				.from(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(),
						Number160.ZERO))
				.to(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(),
						Number160.MAX_VALUE)).keyPair(parameters.getProtectionKeys()).start();
	}

	public FutureRemove removeVersionUnblocked(IParameters parameters) {
		logger.debug(String.format("Remove version. %s", parameters.toString()));
		return getPeer().remove(parameters.getLKey()).setDomainKey(parameters.getDKey())
				.contentKey(parameters.getCKey()).setVersionKey(parameters.getVersionKey())
				.keyPair(parameters.getProtectionKeys()).start();
	}

	public DigestBuilder getDigest(Number160 locationKey) {
		return getPeer().digest(locationKey);
	}
}
