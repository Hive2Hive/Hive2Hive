package org.hive2hive.core.network.data;

import java.io.IOException;
import java.security.KeyPair;
import java.util.NavigableMap;

import net.tomp2p.futures.FutureDigest;
import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.NetworkContent;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.futures.FutureChangeProtectionListener;
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

	// private final SignatureFactory signatureFactory;
	// private final SignatureCodec signatureCodec;

	public DataManager(NetworkManager networkManager, IH2HEncryption encryptionTool) {
		this.networkManager = networkManager;
		this.encryptionTool = encryptionTool;
		// this.signatureFactory = new H2HSignatureFactory();
		// this.signatureCodec = new H2HSignatureCodec();
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
	public IH2HEncryption getEncryption() {
		return encryptionTool;
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
	public boolean changeProtectionKey(IParameters parameters) {
		FuturePut putFuture = changeProtectionKeyUnblocked(parameters);
		if (putFuture == null) {
			return false;
		}

		FutureChangeProtectionListener listener = new FutureChangeProtectionListener(parameters);
		putFuture.addListener(listener);
		return listener.await();
	}

	@Override
	public boolean putUserProfileTask(String userId, Number160 contentKey, NetworkContent content, KeyPair protectionKey) {
		IParameters parameters = new Parameters().setLocationKey(userId).setContentKey(contentKey)
				.setDomainKey(H2HConstants.USER_PROFILE_TASK_DOMAIN).setData(content).setProtectionKeys(protectionKey)
				.setTTL(content.getTimeToLive());
		FuturePut putFuture = putUnblocked(parameters);
		if (putFuture == null) {
			return false;
		}

		FuturePutListener listener = new FuturePutListener(parameters, this);
		putFuture.addListener(listener);
		return listener.await();
	}

	public FuturePut putUnblocked(IParameters parameters) {
		logger.debug("Put. {}", parameters.toString());
		try {
			Data data = new Data(parameters.getData());
			data.ttlSeconds(parameters.getTTL()).basedOn(parameters.getData().getBasedOnKey());

			// check if data to put is content protected
			if (parameters.getProtectionKeys() != null) {
				data.setProtectedEntry().publicKey(parameters.getProtectionKeys().getPublic());

				// sign the data
				// data.sign(parameters.getProtectionKeys(), new RSASignatureFactory());
				// // check if hash creation is needed
				// if (parameters.getHashFlag()) {
				// // decrypt signature to get hash of the object
				// Cipher rsa = Cipher.getInstance("RSA");
				// rsa.init(Cipher.DECRYPT_MODE, parameters.getProtectionKeys().getPublic());
				// byte[] hash = rsa.doFinal(data.signature().encode());
				// // store hash
				// parameters.setHash(hash);
				// }

				return getPeer().put(parameters.getLKey()).setData(parameters.getCKey(), data)
						.setDomainKey(parameters.getDKey()).setVersionKey(parameters.getVersionKey())
						.keyPair(parameters.getProtectionKeys()).start();
			} else {
				return getPeer().put(parameters.getLKey()).setData(parameters.getCKey(), data)
						.setDomainKey(parameters.getDKey()).setVersionKey(parameters.getVersionKey()).start();
			}
		} catch (IOException e) {
			logger.error("Put failed. {}.", parameters.toString(), e);
			return null;
		}
	}

	public FuturePut changeProtectionKeyUnblocked(IParameters parameters) {
		logger.debug("Change content protection key. {}", parameters.toString());
		// create dummy object to change the protection key
		Data data = new Data().setProtectedEntry();
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
		return getPeer().put(parameters.getLKey()).setDomainKey(parameters.getDKey()).putMeta()
				.setData(parameters.getCKey(), data).setVersionKey(parameters.getVersionKey())
				.keyPair(parameters.getProtectionKeys()).start();
	}

	@Override
	public NetworkContent get(IParameters parameters) {
		FutureGet futureGet = getUnblocked(parameters);
		FutureGetListener listener = new FutureGetListener(parameters);
		futureGet.addListener(listener);
		return listener.awaitAndGet();
	}

	public NetworkContent getVersion(IParameters parameters) {
		FutureGet futureGet = getUnblocked(parameters);
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
		return getPeer().get(parameters.getLKey()).setDomainKey(parameters.getDKey()).setContentKey(parameters.getCKey())
				.setVersionKey(parameters.getVersionKey()).start();
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
		return getPeer().remove(parameters.getLKey()).setDomainKey(parameters.getDKey()).contentKey(parameters.getCKey())
				.setVersionKey(parameters.getVersionKey()).keyPair(parameters.getProtectionKeys()).start();
	}

	public NavigableMap<Number640, Number160> getDigestLatest(IParameters parameters) {
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
