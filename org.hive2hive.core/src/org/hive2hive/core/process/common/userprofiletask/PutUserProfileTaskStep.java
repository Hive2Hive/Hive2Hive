package org.hive2hive.core.process.common.userprofiletask;

import java.security.InvalidKeyException;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import net.tomp2p.peers.Number160;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.listener.IPutListener;
import org.hive2hive.core.network.data.listener.IRemoveListener;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * A process step which puts a {@link UserProfileTask} object.</br>
 * <b>Important:</b> Use only this process step to put some data into the network so that in case of failure a
 * appropriate handling is triggered.
 * 
 * @author Seppi
 */
public class PutUserProfileTaskStep extends ProcessStep implements IPutListener, IRemoveListener {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(PutUserProfileTaskStep.class);

	private final String userId;
	private final UserProfileTask userProfileTask;
	private final PublicKey publicKey;
	private final ProcessStep nextStep;

	private Number160 contentKey;

	private boolean putPerformed = false;

	public PutUserProfileTaskStep(String userId, UserProfileTask userProfileTask, PublicKey publicKey,
			ProcessStep nextStep) {
		this.userId = userId;
		this.userProfileTask = userProfileTask;
		this.publicKey = publicKey;
		this.nextStep = nextStep;
	}

	@Override
	public void start() {
		try {
			logger.debug("Encrypting user profile task in a hybrid manner");
			this.contentKey = userProfileTask.getContentKey();
			DataManager dataManager = getNetworkManager().getDataManager();
			if (dataManager == null) {
				getProcess().stop("Node is not connected.");
				return;
			}
			HybridEncryptedContent encrypted = H2HEncryptionUtil.encryptHybrid(userProfileTask, publicKey);
			dataManager.putUserProfileTask(userId, contentKey, encrypted, this);
			putPerformed = true;
		} catch (DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
			getProcess().stop("Meta document could not be encrypted");
		}
	}

	@Override
	public void onPutSuccess() {
		getProcess().setNextStep(nextStep);
	}

	@Override
	public void onPutFailure() {
		getProcess().stop("Put of user profile task failed.");
	}

	@Override
	public void rollBack() {
		if (!putPerformed) {
			logger.warn("Nothing to remove at rollback because nothing has been put");
			getProcess().nextRollBackStep();
			return;
		}

		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			logger.warn(String
					.format("Roll back of user profile task put failed. No connection. user id = '%s' content key = '%s'",
							userId, contentKey));
			getProcess().nextRollBackStep();
			return;
		}

		dataManager.removeUserProfileTask(userId, contentKey, this);
	}

	@Override
	public void onRemoveSuccess() {
		logger.debug(String.format(
				"Roll back of user profile task put succeeded. user id = '%s' content key = '%s'", userId,
				contentKey));
		getProcess().nextRollBackStep();
	}

	@Override
	public void onRemoveFailure() {
		logger.warn(String.format(
				"Roll back of user profile put failed. Remove failed. user id = '%s' content key = '%s'",
				userId, contentKey));
		getProcess().nextRollBackStep();
	}

}
