package org.hive2hive.core.process.common;

import javax.crypto.SecretKey;

import net.tomp2p.futures.FutureDHT;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.encryption.EncryptedContent;
import org.hive2hive.core.encryption.EncryptionUtil;
import org.hive2hive.core.encryption.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.encryption.UserPassword;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.ProcessStep;

/**
 * Generic process step to encrypt the {@link: UserProfile} and add it to the DHT
 * 
 * @author Nico
 * 
 */
public class PutUserProfileStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(PutUserProfileStep.class);

	private final UserProfile profile;
	private final ProcessStep next;
	private final UserPassword password;

	public PutUserProfileStep(UserProfile profile, UserPassword password, ProcessStep next) {
		this.profile = profile;
		this.next = next;
		this.password = password;
	}

	@Override
	public void start() {
		logger.debug("Encrypting UserProfile with 128bit AES key from password");
		SecretKey encryptionKey = EncryptionUtil.createAESKeyFromPassword(password, AES_KEYLENGTH.BIT_128);
		EncryptedContent closedUserProfile = EncryptionUtil.encryptAES(profile, encryptionKey);
		logger.debug("Putting UserProfile into the DHT");
		put(profile.getUserId(), H2HConstants.USER_PROFILE, closedUserProfile);
	}

	@Override
	public void rollBack() {
		// TODO: Remove the user profile from DHT
	}

	@Override
	protected void handleMessageReply(ResponseMessage asyncReturnMessage) {
		// does not send any message
	}

	@Override
	protected void handlePutResult(FutureDHT future) {
		if (future.isSuccess()) {
			getProcess().nextStep(next);
		} else {
			logger.error("Error occurred while putting user profile into DHT. Starting rollback");
		}
	}

	@Override
	protected void handleGetResult(FutureDHT future) {
		// does not perform a get
	}

	@Override
	protected void handleRemovalResult(FutureDHT future) {
		// TODO only needed when rollbacking
	}

}
