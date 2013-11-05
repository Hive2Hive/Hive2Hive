package org.hive2hive.core.process.register;

import java.security.PublicKey;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.UserMessageQueue;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.process.common.PutProcessStep;

/**
 * Puts the user's public key to the network (which is used for encryption of messages and other
 * communication)
 * 
 * @author Nico
 * 
 */
public class PutPublicKeyStep extends PutProcessStep {

	protected PutPublicKeyStep(String userId, PublicKey publicKey) {
		super(userId, H2HConstants.USER_PUBLIC_KEY, new UserPublicKey(publicKey), null);

		// initialize next and final step
		UserMessageQueue queue = new UserMessageQueue(userId);
		nextStep = new PutProcessStep(userId, H2HConstants.USER_MESSAGE_QUEUE_KEY, queue, null);
	}
}
