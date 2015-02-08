package org.hive2hive.core.network.data;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Collection;
import java.util.NavigableMap;

import net.tomp2p.dht.FutureDigest;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.BaseNetworkContent;
import org.hive2hive.core.network.IPeerHolder;
import org.hive2hive.core.network.data.futures.FutureChangeProtectionListener;
import org.hive2hive.core.network.data.futures.FutureDigestListener;
import org.hive2hive.core.network.data.futures.FutureGetListener;
import org.hive2hive.core.network.data.futures.FuturePutListener;
import org.hive2hive.core.network.data.futures.FutureRemoveListener;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.IH2HEncryption;
import org.hive2hive.core.serializer.IH2HSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Seppi
 */
public class DataManager {

	private static final Logger logger = LoggerFactory.getLogger(DataManager.class);

	public enum H2HPutStatus {
		OK,
		FAILED,
		VERSION_FORK
	};

	private final IH2HSerialize serializer;
	private final IPeerHolder peerHolder;
	private final IH2HEncryption encryption;

	public DataManager(IPeerHolder peerHolder, IH2HSerialize serializer, IH2HEncryption encryption) {
		this.peerHolder = peerHolder;
		this.serializer = serializer;
		this.encryption = encryption;
	}

	public IH2HEncryption getEncryption() {
		return encryption;
	}

	public IH2HSerialize getSerializer() {
		return serializer;
	}

	private PeerDHT getPeer() {
		return peerHolder.getPeer();
	}

	public boolean changeProtectionKey(IParameters parameters) {
		FuturePut putFuture = changeProtectionKeyUnblocked(parameters);
		if (putFuture == null) {
			return false;
		}

		FutureChangeProtectionListener listener = new FutureChangeProtectionListener(parameters);
		putFuture.addListener(listener);
		return listener.await();
	}

	public FuturePut changeProtectionKeyUnblocked(IParameters parameters) {
		logger.debug("Change content protection key. {}", parameters.toString());
		// create dummy object to change the protection key
		Data data = new Data().protectEntry(parameters.getNewProtectionKeys());
		if (parameters.getTTL() != -1) {
			data.ttlSeconds(parameters.getTTL());
		}

		// // sign the data
		// try {
		// // encrypt hash with new key pair to get the new signature (without having the data object)
		// Cipher rsa = Cipher.getInstance("RSA");
		// rsa.init(Cipher.ENCRYPT_MODE, parameters.getNewProtectionKeys().getPrivate());
		// byte[] newSignature = rsa.doFinal(parameters.getHash());
		//
		// // create new data signature
		// data = data.signature(signatureCodec.decode(newSignature));
		// } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
		// | IllegalBlockSizeException | BadPaddingException e) {
		// logger.error(String.format("Change protection key failed. %s exception = '%s'",
		// parameters.toString(), e.getMessage()));
		// return null;
		// }

		// create meta data
		data = data.duplicateMeta();

		// change the protection key through a put meta
		return getPeer().put(parameters.getLKey()).domainKey(parameters.getDKey()).putMeta()
				.data(parameters.getCKey(), data).versionKey(parameters.getVersionKey())
				.keyPair(parameters.getProtectionKeys()).start();
	}

	public H2HPutStatus put(IParameters parameters) {
		FuturePut putFuture = putUnblocked(parameters);
		if (putFuture == null) {
			return H2HPutStatus.FAILED;
		}

		FuturePutListener listener = new FuturePutListener(parameters, this);
		putFuture.addListener(listener);
		return listener.await();
	}

	public H2HPutStatus putUserProfileTask(String userId, Number160 contentKey, BaseNetworkContent content,
			KeyPair protectionKey) {
		IParameters parameters = new Parameters().setLocationKey(userId).setContentKey(contentKey)
				.setDomainKey(H2HConstants.USER_PROFILE_TASK_DOMAIN).setNetworkContent(content)
				.setProtectionKeys(protectionKey).setTTL(content.getTimeToLive());
		FuturePut putFuture = putUnblocked(parameters);
		if (putFuture == null) {
			return H2HPutStatus.FAILED;
		}

		FuturePutListener listener = new FuturePutListener(parameters, this);
		putFuture.addListener(listener);
		return listener.await();
	}

	public FuturePut putUnblocked(IParameters parameters) {
		logger.debug("Put. {}", parameters.toString());
		try {
			// serialize with custom serializer (TomP2P would use Java serializer)
			Data data = new Data(serializer.serialize(parameters.getNetworkContent()));

			data.ttlSeconds(parameters.getTTL());
			if (parameters.getBasedOnKey() != null) {
				data.addBasedOn(parameters.getBasedOnKey());
			}
			if (parameters.hasPrepareFlag()) {
				data.prepareFlag();
			}

			// check if data to put is content protected
			if (parameters.getProtectionKeys() != null) {
				data.protectEntry(parameters.getProtectionKeys());
			}

			// cache data
			parameters.setData(data);

			return getPeer().put(parameters.getLKey()).data(parameters.getCKey(), data).domainKey(parameters.getDKey())
					.versionKey(parameters.getVersionKey()).keyPair(parameters.getProtectionKeys()).start();
		} catch (IOException e) {
			logger.error("Put failed. {}.", parameters.toString(), e);
			return null;
		}
	}

	public FuturePut confirmUnblocked(IParameters parameters) {
		logger.debug("Confirm. {}", parameters.toString());

		Data data = new Data();
		data.ttlSeconds(parameters.getTTL());
		if (parameters.getBasedOnKey() != null) {
			data.addBasedOn(parameters.getBasedOnKey());
		}

		// check if data to put is content protected
		if (parameters.getProtectionKeys() != null) {
			data.protectEntry(parameters.getProtectionKeys());
		}

		return getPeer().put(parameters.getLKey()).data(parameters.getCKey(), data).domainKey(parameters.getDKey())
				.versionKey(parameters.getVersionKey()).keyPair(parameters.getProtectionKeys()).putConfirm().start();
	}

	public BaseNetworkContent get(IParameters parameters) {
		FutureGet futureGet = getUnblocked(parameters);
		FutureGetListener listener = new FutureGetListener(parameters, serializer);
		futureGet.addListener(listener);
		return listener.awaitAndGet();
	}

	public BaseNetworkContent getVersion(IParameters parameters) {
		FutureGet futureGet = getVersionUnblocked(parameters);
		FutureGetListener listener = new FutureGetListener(parameters, serializer);
		futureGet.addListener(listener);
		return listener.awaitAndGet();
	}

	public BaseNetworkContent getUserProfileTask(String userId) {
		IParameters parameters = new Parameters().setLocationKey(userId).setDomainKey(H2HConstants.USER_PROFILE_TASK_DOMAIN);
		FutureGet futureGet = getPeer().get(parameters.getLKey())
				.from(new Number640(parameters.getLKey(), parameters.getDKey(), Number160.ZERO, Number160.ZERO))
				.to(new Number640(parameters.getLKey(), parameters.getDKey(), Number160.MAX_VALUE, Number160.MAX_VALUE))
				.ascending().returnNr(1).start();
		FutureGetListener listener = new FutureGetListener(parameters, serializer);
		futureGet.addListener(listener);
		return listener.awaitAndGet();
	}

	public FutureGet getUnblocked(IParameters parameters) {
		logger.debug("Get. {}", parameters.toString());
		return getPeer().get(parameters.getLKey())
				.from(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(), Number160.ZERO))
				.to(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(), Number160.MAX_VALUE))
				.descending().returnNr(1).fastGet(false).start();
	}

	public FutureGet getVersionUnblocked(IParameters parameters) {
		logger.debug("Get version. {}", parameters.toString());
		return getPeer().get(parameters.getLKey()).domainKey(parameters.getDKey()).contentKey(parameters.getCKey())
				.versionKey(parameters.getVersionKey()).start();
	}

	public FutureGet getLatestUnblocked(IParameters parameters) {
		logger.debug("Get latest version. {}", parameters.toString());
		return getPeer().get(parameters.getLKey()).domainKey(parameters.getDKey()).contentKey(parameters.getCKey())
				.getLatest().withDigest().fastGet(false).start();
	}

	public boolean remove(IParameters parameters) {
		FutureRemove futureRemove = removeUnblocked(parameters);
		FutureRemoveListener listener = new FutureRemoveListener(parameters, false, this);
		futureRemove.addListener(listener);
		return listener.await();
	}

	public boolean removeVersion(IParameters parameters) {
		FutureRemove futureRemove = removeVersionUnblocked(parameters);
		FutureRemoveListener listener = new FutureRemoveListener(parameters, true, this);
		futureRemove.addListener(listener);
		return listener.await();
	}

	public boolean removeUserProfileTask(String userId, Number160 contentKey, KeyPair protectionKey) {
		IParameters parameters = new Parameters().setLocationKey(userId).setDomainKey(H2HConstants.USER_PROFILE_TASK_DOMAIN)
				.setContentKey(contentKey).setProtectionKeys(protectionKey);
		FutureRemove futureRemove = removeUnblocked(parameters);
		FutureRemoveListener listener = new FutureRemoveListener(parameters, true, this);
		futureRemove.addListener(listener);
		return listener.await();
	}

	public FutureRemove removeUnblocked(IParameters parameters) {
		logger.debug("Remove. {}", parameters.toString());
		return getPeer().remove(parameters.getLKey())
				.from(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(), Number160.ZERO))
				.to(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(), Number160.MAX_VALUE))
				.keyPair(parameters.getProtectionKeys()).start();
	}

	public FutureRemove removeVersionUnblocked(IParameters parameters) {
		logger.debug("Remove version. {}", parameters.toString());
		return getPeer().remove(parameters.getLKey()).domainKey(parameters.getDKey()).contentKey(parameters.getCKey())
				.versionKey(parameters.getVersionKey()).keyPair(parameters.getProtectionKeys()).start();
	}

	public NavigableMap<Number640, Collection<Number160>> getDigestLatest(IParameters parameters) {
		FutureDigest futureDigest = getDigestLatestUnblocked(parameters);
		FutureDigestListener listener = new FutureDigestListener(parameters);
		futureDigest.addListener(listener);
		return listener.awaitAndGet();
	}

	public FutureDigest getDigestLatestUnblocked(IParameters parameters) {
		logger.debug("Get digest (latest). {}", parameters.toString());
		return getPeer().digest(parameters.getLKey())
				.from(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(), Number160.ZERO))
				.to(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(), Number160.MAX_VALUE))
				.descending().returnNr(1).fastGet(false).start();
	}

	public FutureDigest getDigestUnblocked(IParameters parameters) {
		logger.debug("Get digest. {}", parameters.toString());
		return getPeer().digest(parameters.getLKey())
				.from(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(), Number160.ZERO))
				.to(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(), Number160.MAX_VALUE))
				.fastGet(false).start();

	}
}
