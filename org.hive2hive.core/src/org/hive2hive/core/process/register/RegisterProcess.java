package org.hive2hive.core.process.register;

import java.security.KeyPair;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.GetLocationsStep;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.security.UserPassword;

public class RegisterProcess extends Process {

	private final RegisterProcessContext context;
	
	public RegisterProcess(String userId, String password, String pin, NetworkManager networkManager) {
		super(networkManager);

		// create and set context
		KeyPair encryptionKeys = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
		KeyPair domainKeys = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
		
		UserPassword userPassword = new UserPassword(password, pin);
		UserProfile userProfile = new UserProfile(userId, encryptionKeys, domainKeys);
		context = new RegisterProcessContext(this, userPassword, userProfile);
		
		// get the locations map to check if a user with the same name is already existent
		CheckIfUserExistsStep profileExistsStep = new CheckIfUserExistsStep();
		GetLocationsStep getLocationsStep = new GetLocationsStep(userId, profileExistsStep);
		profileExistsStep.setPreviousStep(getLocationsStep);
		setNextStep(getLocationsStep);
	}

	@Override
	public RegisterProcessContext getContext() {
		return context;
	}
}
