package org.hive2hive.processes.implementations.register;

import java.io.IOException;

import javax.crypto.SecretKey;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.base.BasePutProcessStep;

public class PutUserProfileStep extends BasePutProcessStep {

	private final UserCredentials credentials;
	private final UserProfile userProfile;

	public PutUserProfileStep(UserCredentials credentials, UserProfile userProfile,
			NetworkManager networkManager) {
		super(networkManager);
		this.credentials = credentials;
		this.userProfile = userProfile;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {

		// encrypt user profile
		SecretKey encryptionKey = PasswordUtil.generateAESKeyFromPassword(credentials.getPassword(),
				credentials.getPin(), H2HConstants.KEYLENGTH_USER_PROFILE);

		EncryptedNetworkContent encryptedProfile = null;
		try {
			encryptedProfile = H2HEncryptionUtil.encryptAES(userProfile, encryptionKey);
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException | IOException e) {
			cancel(new RollbackReason(this, "User profile could not be encrypted. Reason: " + e.getMessage()));
			return;
		}

		try {
			encryptedProfile.generateVersionKey();
		} catch (IOException e) {
			cancel(new RollbackReason(this, "User profile version key could not be generated. Reason: "
					+ e.getMessage()));
			return;
		}

		// put encrypted user profile
		try {
			put(credentials.getProfileLocationKey(), H2HConstants.USER_PROFILE, encryptedProfile,
					userProfile.getProtectionKeys());
		} catch (PutFailedException e) {
			cancel(new RollbackReason(this, e.getMessage()));
		}

	}
}
