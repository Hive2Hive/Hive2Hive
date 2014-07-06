package org.hive2hive.core.processes.common.userprofiletask;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import net.tomp2p.peers.Number160;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A process step which puts a {@link UserProfileTask} object.</br>
 * <b>Important:</b> Use only this process step to put some data into the network so that in case of failure a
 * appropriate handling is triggered.
 * 
 * @author Seppi, Nico
 */
public abstract class PutUserProfileTaskStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(PutUserProfileTaskStep.class);

	protected final NetworkManager networkManager;
	private String userId;
	private Number160 contentKey;
	private KeyPair protectionKey;

	private boolean putPerformed = false;

	public PutUserProfileTaskStep(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	protected void put(String userId, UserProfileTask userProfileTask, PublicKey publicKey) throws PutFailedException,
			InvalidProcessStateException {
		if (userId == null) {
			throw new IllegalArgumentException("user id can be not null");
		}
		if (userProfileTask == null) {
			throw new IllegalArgumentException("user profile task can be not null");
		}
		if (publicKey == null) {
			throw new IllegalArgumentException("public key can be not null");
		}

		this.userId = userId;

		try {
			logger.debug("Encrypting user profile task in a hybrid manner.");
			this.contentKey = userProfileTask.getContentKey();
			this.protectionKey = userProfileTask.getProtectionKey();
			DataManager dataManager = networkManager.getDataManager();
			HybridEncryptedContent encrypted = dataManager.getEncryption().encryptHybrid(userProfileTask, publicKey);
			encrypted.setTimeToLive(userProfileTask.getTimeToLive());
			boolean success = dataManager.putUserProfileTask(userId, contentKey, encrypted, protectionKey);
			putPerformed = true;

			if (!success) {
				throw new PutFailedException();
			}
		} catch (IOException | DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
			throw new PutFailedException("Meta document could not be encrypted. Reason: " + e.getMessage());
		} catch (NoPeerConnectionException e) {
			throw new PutFailedException(e.getMessage());
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (!putPerformed) {
			logger.warn("Nothing to remove at rollback because nothing has been put.");
			return;
		}

		DataManager dataManager;
		try {
			dataManager = networkManager.getDataManager();
		} catch (NoPeerConnectionException e) {
			logger.warn("Rollback of user profile task put failed. No connection. User ID = '{}', Content key = '{}'.",
					userId, contentKey, e);
			return;
		}

		boolean success = dataManager.removeUserProfileTask(userId, contentKey, protectionKey);
		if (success) {
			logger.debug("Rollback of user profile task put succeeded. User ID = '{}', Content key = '{}'.", userId,
					contentKey);
		} else {
			logger.warn("Rollback of user profile put failed. Remove failed. User ID = '{}', Content key = '{}'.", userId,
					contentKey);
		}
	}
}
