package org.hive2hive.core.process.register;

import java.security.PublicKey;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.UserMessageQueue;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.process.common.put.BasePutProcessStep;

/**
 * Puts the user's public key to the network (which is used for encryption of messages and other
 * communication)
 * 
 * @author Nico
 * 
 */
public class PutPublicKeyStep extends BasePutProcessStep {

	private final String userId;
	private final PublicKey publicKey;

	protected PutPublicKeyStep(String userId, PublicKey publicKey) {
		super(new PutUserMessageQueue(new UserMessageQueue(userId)));
		this.userId = userId;
		this.publicKey = publicKey;
	}

	@Override
	public void start() {
		put(userId, H2HConstants.USER_PUBLIC_KEY, new UserPublicKey(publicKey));
	}
}
