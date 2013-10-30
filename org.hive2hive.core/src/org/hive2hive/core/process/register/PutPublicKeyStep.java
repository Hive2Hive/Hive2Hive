package org.hive2hive.core.process.register;

import java.security.PublicKey;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.PutProcessStep;

/**
 * Puts the user's public key to the network (which is used for encryption of messages and other
 * communication)
 * 
 * @author Nico
 * 
 */
public class PutPublicKeyStep extends PutProcessStep {

	private final String userId;
	private final PublicKey publicKey;

	protected PutPublicKeyStep(String userId, PublicKey publicKey) {
		super(null);
		this.userId = userId;
		this.publicKey = publicKey;
	}

	@Override
	public void start() {
		put(userId, H2HConstants.USER_PUBLIC_KEY, new UserPublicKey(publicKey));
	}

	@Override
	public void rollBack() {
		super.rollBackPut(userId, H2HConstants.USER_PUBLIC_KEY);
	}

	@Override
	protected void handleMessageReply(ResponseMessage asyncReturnMessage) {
		// not used
	}

	@Override
	protected void handlePutResult(FuturePut future) {
		if (future.isSuccess()) {
			// TODO: next step?
			getProcess().nextStep(null);
		} else {
			rollBack();
		}
	}

	@Override
	protected void handleGetResult(FutureGet future) {
		// not used
	}

	@Override
	protected void handleRemovalResult(FutureRemove future) {
		// not used
	}

}
