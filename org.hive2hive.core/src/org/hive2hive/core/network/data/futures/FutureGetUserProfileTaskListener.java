package org.hive2hive.core.network.data.futures;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureGet;

import org.hive2hive.core.network.data.listener.IGetUserProfileTaskListener;
import org.hive2hive.core.network.usermessages.UserProfileTask;

/**
 * A future listener for a get of a {@link UserProfileTask} object. Returns the given
 * {@link IGetUserProfileTaskListener} listener the desired content or <code>null</code> if the
 * get fails or the content doesn't exist.
 * 
 * @author Seppi
 */
public class FutureGetUserProfileTaskListener extends BaseFutureAdapter<FutureGet> {

	protected final IGetUserProfileTaskListener listener;

	public FutureGetUserProfileTaskListener(IGetUserProfileTaskListener listener) {
		this.listener = listener;
	}

	@Override
	public void operationComplete(FutureGet future) throws Exception {
		if (future.isFailed() || future.getData() == null) {
			if (listener != null)
				listener.handleGetUserProfileTaskResult(null);
		} else {
			if (listener != null)
				listener.handleGetUserProfileTaskResult((UserProfileTask) future.getData().object());
		}
	}

}
