package org.hive2hive.core.process.register;

import org.hive2hive.core.encryption.EncryptionUtil;
import org.hive2hive.core.encryption.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.encryption.ProfileEncryptionUtil;
import org.hive2hive.core.encryption.UserPassword;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;

public class RegisterProcess extends Process {

	private final String userId;
	private final UserPassword userPassword;
	private final UserProfile userProfile;

	public RegisterProcess(String userId, String password, NetworkManager networkManager) {
		super(networkManager);
		this.userId = userId;
		userPassword = ProfileEncryptionUtil.createUserPassword(password);
		userProfile = new UserProfile(userId, EncryptionUtil.createRSAKeys(RSA_KEYLENGTH.BIT_2048),
				EncryptionUtil.createRSAKeys(RSA_KEYLENGTH.BIT_2048));
		setFirstStep(new CheckIfProfileExistsStep(userId));
	}

	public String getUserId() {
		return userId;
	}

	public UserPassword getUserPassword() {
		return userPassword;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}
}
