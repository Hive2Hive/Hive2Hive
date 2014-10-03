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
import org.hive2hive.core.model.NetworkContent;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.futures.FutureChangeProtectionListener;
import org.hive2hive.core.network.data.futures.FutureConfirmListener;
import org.hive2hive.core.network.data.futures.FutureDigestListener;
import org.hive2hive.core.network.data.futures.FutureGetListener;
import org.hive2hive.core.network.data.futures.FuturePutListener;
import org.hive2hive.core.network.data.futures.FutureRemoveListener;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.IH2HEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Seppi
 */
public class DataManager implements IDataManager {

	private static final Logger logger = LoggerFactory.getLogger(DataManager.class);

	private final NetworkManager networkManager;
	private final IH2HEncryption encryptionTool;

	public DataManager(NetworkManager networkManager, IH2HEncryption encryptionTool) {
		this.networkManager = networkManager;
		this.encryptionTool = encryptionTool;
	}

	/**
	 * Helper to get the <code>TomP2P</code> DHT peer.
	 * 
	 * @return the current peer
	 */
	private PeerDHT getPeer() {
		return networkManager.getConnection().getPeerDHT();
	}

	@Override
	public IH2HEncryption getEncryption() {
		return encryptionTool;
	}

	@Override
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
		Data data = new Data().protectEntry();
		// set new content protection keys
		data.publicKey(parameters.getNewProtectionKeys().getPublic());
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

	@Override
	public H2HPutStatus put(IParameters parameters) {
		FuturePut putFuture = putUnblocked(parameters);
		if (putFuture == null) {
			return H2HPutStatus.FAILED;
		}

		FuturePutListener listener = new FuturePutListener(parameters, this);
		putFuture.addListener(listener);
		return listener.await();
	}

	@Override
	public H2HPutStatus putUserProfileTask(String userId, Number160 contentKey, NetworkContent content, KeyPair protectionKey) {
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
			Data data = new Data(parameters.getNetworkContent());
			data.ttlSeconds(parameters.getTTL()).addBasedOn(parameters.getNetworkContent().getBasedOnKey());
			if (parameters.hasPrepareFlag()) {
				data.prepareFlag();
			}

			// check if data to put is content protected
			if (parameters.getProtectionKeys() != null) {
				data.protectEntry().publicKey(parameters.getProtectionKeys().getPublic());
			}

			return getPeer().put(parameters.getLKey()).data(parameters.getCKey(), data).domainKey(parameters.getDKey())
					.versionKey(parameters.getVersionKey()).keyPair(parameters.getProtectionKeys()).start();
		} catch (IOException e) {
			logger.error("Put failed. {}.", parameters.toString(), e);
			return null;
		}
	}

	@Override
	public H2HPutStatus confirm(IParameters parameters) {
		FuturePut confirmFuture = confirmUnblocked(parameters);
		if (confirmFuture == null) {
			return H2HPutStatus.FAILED;
		}

		FutureConfirmListener listener = new FutureConfirmListener(parameters, this);
		confirmFuture.addListener(listener);
		return listener.await();
	}

	public FuturePut confirmUnblocked(IParameters parameters) {
		logger.debug("Confirm. {}", parameters.toString());

		Data data = new Data();
		data.ttlSeconds(parameters.getTTL()).addBasedOn(parameters.getNetworkContent().getBasedOnKey());

		// check if data to put is content protected
		if (parameters.getProtectionKeys() != null) {
			data.protectEntry().publicKey(parameters.getProtectionKeys().getPublic());
		}

		return getPeer().put(parameters.getLKey()).data(parameters.getCKey(), data).domainKey(parameters.getDKey())
				.versionKey(parameters.getVersionKey()).keyPair(parameters.getProtectionKeys()).putConfirm().start();
	}

	@Override
	public NetworkContent get(IParameters parameters) {
		FutureGet futureGet = getUnblocked(parameters);
		FutureGetListener listener = new FutureGetListener(parameters);
		futureGet.addListener(listener);
		return listener.awaitAndGet();
	}

	public NetworkContent getVersion(IParameters parameters) {
		FutureGet futureGet = getVersionUnblocked(parameters);
		FutureGetListener listener = new FutureGetListener(parameters);
		futureGet.addListener(listener);
		return listener.awaitAndGet();
	}

	@Override
	public NetworkContent getUserProfileTask(String userId) {
		IParameters parameters = new Parameters().setLocationKey(userId).setDomainKey(H2HConstants.USER_PROFILE_TASK_DOMAIN);
		FutureGet futureGet = getPeer().get(parameters.getLKey())
				.from(new Number640(parameters.getLKey(), parameters.getDKey(), Number160.ZERO, Number160.ZERO))
				.to(new Number640(parameters.getLKey(), parameters.getDKey(), Number160.MAX_VALUE, Number160.MAX_VALUE))
				.ascending().returnNr(1).start();
		FutureGetListener listener = new FutureGetListener(parameters);
		futureGet.addListener(listener);
		return listener.awaitAndGet();
	}

	public FutureGet getUnblocked(IParameters parameters) {
		logger.debug("Get. {}", parameters.toString());
		return getPeer().get(parameters.getLKey())
				.from(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(), Number160.ZERO))
				.to(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(), Number160.MAX_VALUE))
				.descending().returnNr(1).start();
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

	@Override
	public NavigableMap<Number640, Collection<Number160>> getDigestLatest(IParameters parameters) {
		logger.debug("Get digest (latest). {}", parameters.toString());
		FutureDigest futureDigest = getDigestLatestUnblocked(parameters);
		FutureDigestListener listener = new FutureDigestListener(parameters);
		futureDigest.addListener(listener);
		return listener.awaitAndGet();
	}

	public FutureDigest getDigestLatestUnblocked(IParameters parameters) {
		logger.debug("Get digest (latest, unblocked). {}", parameters.toString());
		return getPeer().digest(parameters.getLKey())
				.from(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(), Number160.ZERO))
				.to(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(), Number160.MAX_VALUE))
				.descending().returnNr(1).start();
	}

	public FutureDigest getDigestUnblocked(IParameters parameters) {
		logger.debug("Get digest (unblocked). {}", parameters.toString());
		return getPeer().digest(parameters.getLKey())
				.from(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(), Number160.ZERO))
				.to(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(), Number160.MAX_VALUE))
				.start();

	}
}
