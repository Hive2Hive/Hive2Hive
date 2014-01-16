package org.hive2hive.core.process.register;

import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.put.BasePutProcessStep;

/**
 * Puts the user's public key to the network (which is used for encryption of messages and other
 * communication)
 * 
 * @author Nico, Seppi
 * 
 */
public class PutPublicKeyStep extends BasePutProcessStep {

	private final String userId;
	private final PublicKey publicKey;
	private final KeyPair protectionKey;
	private final ProcessStep nextStep;

	protected PutPublicKeyStep(String userId, PublicKey publicKey, KeyPair protectionKey, ProcessStep nextStep) {
		this.userId = userId;
		this.publicKey = publicKey;
		this.protectionKey = protectionKey;
		this.nextStep = nextStep;
	}

	@Override
	public void start() {
		try {
			put(userId, H2HConstants.USER_PUBLIC_KEY, new UserPublicKey(publicKey), protectionKey);
			getProcess().setNextStep(nextStep);
		} catch (PutFailedException e) {
			getProcess().stop(e);
		}
	}
}
