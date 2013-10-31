package org.hive2hive.core.process.common;

import javax.crypto.SecretKey;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.PutProcessStep;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserPassword;
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;

/**
 * Generic process step to encrypt the {@link: UserProfile} and add it to the DHT
 * 
 * @author Nico
 * 
 */
public class PutUserProfileStep extends PutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(PutUserProfileStep.class);

	private final UserProfile profile;
	private final ProcessStep next;
	private final UserPassword password;
	private String locationkey;

	public PutUserProfileStep(UserProfile profile, UserProfile previousVersion, UserPassword password,
			ProcessStep next) {
		super(previousVersion);
		this.profile = profile;
		this.next = next;
		this.password = password;
		this.locationkey = profile.getLocationKey(password);
	}

	@Override
	public void start() {
		logger.debug("Encrypting UserProfile with 256bit AES key from password");
		try {
			SecretKey encryptionKey = PasswordUtil
					.generateAESKeyFromPassword(password, AES_KEYLENGTH.BIT_256);
			EncryptedNetworkContent encryptedUserProfile = H2HEncryptionUtil.encryptAES(profile,
					encryptionKey);
			logger.debug("Putting UserProfile into the DHT");
			put(locationkey, H2HConstants.USER_PROFILE, encryptedUserProfile);
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
			logger.error("Cannot encrypt the user profile.", e);
			getProcess().rollBack(e.getMessage());
		}
	}

	@Override
	public void rollBack() {
		super.rollBackPut(locationkey, H2HConstants.USER_PROFILE);
	}

	@Override
	protected void handleMessageReply(ResponseMessage asyncReturnMessage) {
		// does not send any message
	}

	@Override
	protected void handlePutResult(FuturePut future) {
		if (future.isSuccess()) {
			getProcess().nextStep(next);
		} else {
			logger.error("Error occurred while putting user profile into DHT. Starting rollback");
			getProcess().rollBack("UserProfile could not be put");
		}
	}

	@Override
	protected void handleGetResult(FutureGet future) {
		// does not perform a get
	}

	@Override
	protected void handleRemovalResult(FutureRemove future) {
		// no removal used
	}

}
