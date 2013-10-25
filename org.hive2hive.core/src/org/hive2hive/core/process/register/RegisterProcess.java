package org.hive2hive.core.process.register;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;

public class RegisterProcess extends Process{
	
	public RegisterProcess(String userId, String password, NetworkManager networkManager) {
		super(networkManager);
		setFirstStep(new CheckIfProfileExistsStep(userId, password));
	}

}
