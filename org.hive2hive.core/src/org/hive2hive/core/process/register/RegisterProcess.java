package org.hive2hive.core.process.register;

import org.hive2hive.core.encryption.ProfileEncryptionUtil;
import org.hive2hive.core.encryption.UserPassword;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;

public class RegisterProcess extends Process {

	private final String userId;
	private final String password;
	private UserPassword userPassword;

	public RegisterProcess(String userId, String password, NetworkManager networkManager) {
		super(networkManager);
		this.userId = userId;
		this.password = password;
		userPassword = ProfileEncryptionUtil.createUserPassword(password);
		setFirstStep(new CheckIfProfileExistsStep(userId, userPassword));
	}

}
