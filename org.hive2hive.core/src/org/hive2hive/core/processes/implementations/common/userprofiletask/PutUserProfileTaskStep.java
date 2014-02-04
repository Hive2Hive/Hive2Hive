package org.hive2hive.core.processes.implementations.common.userprofiletask;

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
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * A process step which puts a {@link UserProfileTask} object.</br>
 * <b>Important:</b> Use only this process step to put some data into the network so that in case of failure a
 * appropriate handling is triggered.
 * 
 * @author Seppi, Nico
 */
public abstract class PutUserProfileTaskStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(PutUserProfileTaskStep.class);

	protected final NetworkManager networkManager;
	private String userId;
	private Number160 contentKey;
	private KeyPair protectionKey;

	private boolean putPerformed = false;

	public PutUserProfileTaskStep(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	protected void put(String userId, UserProfileTask userProfileTask, PublicKey publicKey)
			throws PutFailedException, InvalidProcessStateException {
		if (userId == null)
			throw new IllegalArgumentException("user id can be not null");
		if (userProfileTask == null)
			throw new IllegalArgumentException("user profile task can be not null");
		if (publicKey == null)
			throw new IllegalArgumentException("public key can be not null");

		this.userId = userId;

		try {
			logger.debug("Encrypting user profile task in a hybrid manner");
			this.contentKey = userProfileTask.getContentKey();
			this.protectionKey = userProfileTask.getProtectionKey();
			DataManager dataManager = networkManager.getDataManager();
			HybridEncryptedContent encrypted = H2HEncryptionUtil.encryptHybrid(userProfileTask, publicKey);
			boolean success = dataManager.putUserProfileTask(userId, contentKey, encrypted, protectionKey);
			putPerformed = true;

			if (!success) {
				throw new PutFailedException();
			}
		} catch (IOException | DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
			throw new PutFailedException("Meta document could not be encrypted");
		} catch (NoPeerConnectionException e) {
			throw new PutFailedException(e.getMessage());
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (!putPerformed) {
			logger.warn("Nothing to remove at rollback because nothing has been put");
			return;
		}

		DataManager dataManager;
		try {
			dataManager = networkManager.getDataManager();
		} catch (NoPeerConnectionException e) {
			logger.warn(String
					.format("Roll back of user profile task put failed. No connection. user id = '%s' content key = '%s'",
							userId, contentKey));
			return;
		}

		boolean success = dataManager.removeUserProfileTask(userId, contentKey, protectionKey);
		if (success) {
			logger.debug(String.format(
					"Roll back of user profile task put succeeded. user id = '%s' content key = '%s'",
					userId, contentKey));
		} else {
			logger.warn(String.format(
					"Roll back of user profile put failed. Remove failed. user id = '%s' content key = '%s'",
					userId, contentKey));
		}
	}
}
