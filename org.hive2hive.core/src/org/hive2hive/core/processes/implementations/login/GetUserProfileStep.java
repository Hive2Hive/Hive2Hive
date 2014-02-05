package org.hive2hive.core.processes.implementations.login;

import java.io.IOException;

import javax.crypto.SecretKey;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.implementations.common.base.BaseGetProcessStep;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideUserProfile;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;

public class GetUserProfileStep extends BaseGetProcessStep {

	private final UserCredentials credentials;
	private final IProvideUserProfile context;

	public GetUserProfileStep(UserCredentials credentials, IProvideUserProfile context,
			IDataManager dataManager) {
		super(dataManager);
		this.credentials = credentials;
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {

		NetworkContent loadedContent = get(credentials.getProfileLocationKey(), H2HConstants.USER_PROFILE);

		if (loadedContent == null) {
			cancel(new RollbackReason(this, "User profile not found."));
		} else {

			// decrypt user profile
			EncryptedNetworkContent encryptedContent = (EncryptedNetworkContent) loadedContent;

			SecretKey decryptionKey = PasswordUtil.generateAESKeyFromPassword(credentials.getPassword(),
					credentials.getPin(), H2HConstants.KEYLENGTH_USER_PROFILE);

			NetworkContent decryptedContent = null;
			try {
				decryptedContent = H2HEncryptionUtil.decryptAES(encryptedContent, decryptionKey);
			} catch (DataLengthException | IllegalStateException | InvalidCipherTextException
					| ClassNotFoundException | IOException e) {
				cancel(new RollbackReason(this, "User profile could not be decrypted. Reason: "
						+ e.getMessage()));
				return;
			}

			UserProfile profile = (UserProfile) decryptedContent;
			profile.setVersionKey(loadedContent.getVersionKey());
			profile.setBasedOnKey(loadedContent.getBasedOnKey());

			context.provideUserProfile(profile);
		}
	}
}
