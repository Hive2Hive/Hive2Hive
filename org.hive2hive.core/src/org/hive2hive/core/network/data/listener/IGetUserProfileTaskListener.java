package org.hive2hive.core.network.data.listener;

import org.hive2hive.core.network.data.futures.FutureGetUserProfileTaskListener;
import org.hive2hive.core.network.usermessages.UserProfileTask;

/**
 * An interface used in {@link FutureGetUserProfileTaskListener} to inform entities about a success or fail of
 * a get.
 * 
 * @author Seppi
 */
public interface IGetUserProfileTaskListener {

	/**
	 * Gets called when get finishes. Can be <code>null</code> if something went wrong or required object
	 * doesn't exist in the network.
	 * 
	 * @param content
	 */
	public void handleGetUserProfileTaskResult(UserProfileTask userProfileTask);

}
