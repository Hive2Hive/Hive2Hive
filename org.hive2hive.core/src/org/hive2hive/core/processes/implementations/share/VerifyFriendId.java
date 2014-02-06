package org.hive2hive.core.processes.implementations.share;

import java.security.PublicKey;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;

public class VerifyFriendId extends ProcessStep {

	private final NetworkManager networkManager;
	private final String friendId;

	public VerifyFriendId(NetworkManager networkManager, String friendId) {
		this.networkManager = networkManager;
		this.friendId = friendId;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		try {
			// just get the public key. It does not produce any overhead since this call is cached or (if the
			// first time), the result will be cached, making the notification faster.
			PublicKey publicKey = networkManager.getPublicKey(friendId);
			if (publicKey == null)
				throw new GetFailedException("The friend does not seem to exist");
		} catch (GetFailedException e) {
			cancel(new RollbackReason(this, "The friend '" + friendId + "' does not seem to exist"));
		}
	}

}
