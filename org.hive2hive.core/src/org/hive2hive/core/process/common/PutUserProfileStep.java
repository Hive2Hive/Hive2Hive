package org.hive2hive.core.process.common;

import javax.crypto.SecretKey;

import net.tomp2p.futures.FutureDHT;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.encryption.EncryptedContent;
import org.hive2hive.core.encryption.EncryptionUtil;
import org.hive2hive.core.encryption.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.encryption.UserPassword;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.ProcessStep;

public class PutUserProfileStep extends ProcessStep {

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
		SecretKey encryptionKey = EncryptionUtil.createAESKeyFromPassword(password, AES_KEYLENGTH.BIT_128);
		EncryptedContent closedUserProfile = EncryptionUtil.encryptAES(profile, encryptionKey);
		put(profile.getUserId(), H2HConstants.USER_PROFILE, closedUserProfile);
	}

	@Override
	public void rollBack() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleMessageReply(ResponseMessage asyncReturnMessage) {
		// does not send any message
	}

	@Override
	protected void handlePutResult(FutureDHT future) {
		getProcess().nextStep(next);
	}

	@Override
	protected void handleGetResult(FutureDHT future) {
		// does not perform a get
	}

}
