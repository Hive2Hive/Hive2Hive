package org.hive2hive.core.process.register;

import java.security.KeyPair;

import org.hive2hive.core.encryption.EncryptionUtil;
import org.hive2hive.core.encryption.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.encryption.PasswordUtil;
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
		userPassword = PasswordUtil.generatePassword(password.toCharArray());
		KeyPair encryptionKeys = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
		KeyPair domainKeys = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);

		userProfile = new UserProfile(userId, encryptionKeys, domainKeys);
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
