package org.hive2hive.core.process.register;

import java.security.KeyPair;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.UserPassword;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;

public class RegisterProcess extends Process {

	private final String userId;
	private final UserPassword userPassword;
	private final UserProfile userProfile;

	public RegisterProcess(String userId, String password, String pin, NetworkManager networkManager) {
		super(networkManager);
		this.userId = userId;
		userPassword = new UserPassword(password.toCharArray(), pin.toCharArray());

		KeyPair encryptionKeys = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
		KeyPair domainKeys = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
		userProfile = new UserProfile(userId, encryptionKeys, domainKeys);

		setFirstStep(new CheckIfProfileExistsStep(userProfile, userPassword));
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
