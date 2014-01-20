package org.hive2hive.processes.implementations.login;

import javax.crypto.SecretKey;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processes.framework.ProcessUtil;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.base.BaseGetProcessStep;
import org.hive2hive.processes.implementations.context.interfaces.IProvideUserProfile;

public class GetUserProfileStep extends BaseGetProcessStep {

	private final UserCredentials credentials;
	private final IProvideUserProfile context;

	private boolean isGetCompleted;
	private NetworkContent loadedContent;

	public GetUserProfileStep(UserCredentials credentials,
			IProvideUserProfile context, NetworkManager networkManager) {
		super(networkManager);
		this.credentials = credentials;
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {

		get(credentials.getProfileLocationKey(), H2HConstants.USER_PROFILE);

		// wait for GET to complete
		while (isGetCompleted == false) {
			ProcessUtil.wait(this);
		}

		if (loadedContent == null) {
			context.provideUserProfile(null);
		} else {

			// decrypt user profile
			EncryptedNetworkContent encryptedContent = (EncryptedNetworkContent) loadedContent;

			SecretKey decryptionKey = PasswordUtil.generateAESKeyFromPassword(
					credentials.getPassword(), credentials.getPin(),
					H2HConstants.KEYLENGTH_USER_PROFILE);

			NetworkContent decryptedProfile = null;
			try {
				decryptedProfile = H2HEncryptionUtil.decryptAES(
						encryptedContent, decryptionKey);
			} catch (DataLengthException | IllegalStateException
					| InvalidCipherTextException e) {
				cancel(new RollbackReason(this,
						"User profile could not be decrypted. Reason: "
								+ e.getMessage()));
			}
			
			UserProfile profile = (UserProfile) decryptedProfile;
			profile.setVersionKey(loadedContent.getVersionKey());
			profile.setBasedOnKey(loadedContent.getBasedOnKey());

			context.provideUserProfile(profile);
		}
	}

	@Override
	public void handleGetResult(NetworkContent content) {
		isGetCompleted = true;
		this.loadedContent = content;
	}

}
