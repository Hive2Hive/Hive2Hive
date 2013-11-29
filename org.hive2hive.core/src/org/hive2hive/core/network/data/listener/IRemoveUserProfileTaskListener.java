package org.hive2hive.core.network.data.listener;

import org.hive2hive.core.network.data.futures.FutureRemoveUserProfileTaskListener;

/**
 * An interface used in {@link FutureRemoveUserProfileTaskListener} to inform entities about a success or fail
 * of a remove of an user profile task.
 * 
 * @author Seppi
 */
public interface IRemoveUserProfileTaskListener {
	/**
	 * Remove of a user profile task has succeeded.
	 */
	public void onRemoveUserProfileTaskSuccess();

	/**
	 * Remove of a user profile task has failed.
	 */
	public void onRemoveUserProfileTaskFailure();
}
