package org.hive2hive.core.processes.common.userprofiletask;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;

import net.tomp2p.peers.Number160;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.versioned.HybridEncryptedContent;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.DataManager.H2HPutStatus;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessRollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A process step which puts a {@link UserProfileTask} object.<br>
 * <b>Important:</b> Use only this process step to put some data into the network so that in case of failure a
 * appropriate handling is triggered.
 * 
 * @author Seppi, Nico
 */
public abstract class PutUserProfileTaskStep extends ProcessStep<Void> {

	private static final Logger logger = LoggerFactory.getLogger(PutUserProfileTaskStep.class);

	protected final NetworkManager networkManager;
	private String userId;
	private Number160 contentKey;
	private KeyPair protectionKey;

	public PutUserProfileTaskStep(NetworkManager networkManager) {
		this.setName(getClass().getName());
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
			this.protectionKey = userProfileTask.getProtectionKeys();
			HybridEncryptedContent encrypted = networkManager.getEncryption().encryptHybrid(userProfileTask, publicKey);
			encrypted.setTimeToLive(userProfileTask.getTimeToLive());

			DataManager dataManager = networkManager.getDataManager();
			H2HPutStatus status = dataManager.putUserProfileTask(userId, contentKey, encrypted, protectionKey);
			if (!status.equals(H2HPutStatus.OK)) {
				throw new PutFailedException();
			}
			setRequiresRollback(true);

		} catch (IOException | GeneralSecurityException ex) {
			throw new PutFailedException(String.format("Meta document could not be encrypted. Reason: %s.", ex.getMessage()));
		} catch (NoPeerConnectionException ex) {
			throw new PutFailedException(ex.getMessage());
		}
	}

	@Override
	protected Void doRollback() throws InvalidProcessStateException, ProcessRollbackException {

		DataManager dataManager;
		try {
			dataManager = networkManager.getDataManager();
		} catch (NoPeerConnectionException ex) {
			throw new ProcessRollbackException(this, ex, String.format(
					"Rollback of UserProfileTask put failed. No connection. User = '%s', Content Key = '%s'.", userId,
					contentKey));
		}

		boolean success = dataManager.removeUserProfileTask(userId, contentKey, protectionKey);
		if (success) {
			logger.debug("Rollback of user profile task put succeeded. User = '{}', Content Key = '{}'.", userId, contentKey);
			setRequiresRollback(false);
		} else {
			throw new ProcessRollbackException(this, String.format(
					"Rollback of user profile put failed. Remove failed. User = '%s', Content Key = '%s'.", userId,
					contentKey));
		}

		return null;
	}
}
