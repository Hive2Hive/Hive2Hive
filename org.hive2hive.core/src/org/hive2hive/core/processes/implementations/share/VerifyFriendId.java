package org.hive2hive.core.processes.implementations.share;

import java.security.PublicKey;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;

public class VerifyFriendId extends ProcessStep {

	private final PublicKeyManager keyManager;
	private final String friendId;

	public VerifyFriendId(PublicKeyManager keyManager, String friendId) {
		this.keyManager = keyManager;
		this.friendId = friendId;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		try {
			// just get the public key. It does not produce any overhead since this call is cached or (if the
			// first time), the result will be cached, making the notification faster.
			PublicKey publicKey = keyManager.getPublicKey(friendId);
			if (publicKey == null)
				throw new GetFailedException("The friend does not seem to exist.");
		} catch (GetFailedException e) {
			throw new ProcessExecutionException("The friend '" + friendId + "' does not seem to exist.", e);
		}
	}

}