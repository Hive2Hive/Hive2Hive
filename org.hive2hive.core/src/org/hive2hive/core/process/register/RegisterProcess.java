package org.hive2hive.core.process.register;

import java.security.KeyPair;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.GetLocationsStep;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.security.UserCredentials;

public class RegisterProcess extends Process {

	private final RegisterProcessContext context;
	
	public RegisterProcess(UserCredentials credentials, NetworkManager networkManager) {
		super(networkManager);

		// create and set context
		KeyPair encryptionKeys = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
		KeyPair domainKeys = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
		
		UserProfile userProfile = new UserProfile(credentials.getUserId(), encryptionKeys, domainKeys);
		context = new RegisterProcessContext(this, credentials, userProfile);
		
		// get the locations map to check if a user with the same name is already existent
		CheckIfUserExistsStep userExistsStep = new CheckIfUserExistsStep();
		GetLocationsStep getLocationsStep = new GetLocationsStep(credentials.getUserId(), userExistsStep);
		userExistsStep.setPreviousStep(getLocationsStep);
		setNextStep(getLocationsStep);
	}

	@Override
	public RegisterProcessContext getContext() {
		return context;
	}
}
