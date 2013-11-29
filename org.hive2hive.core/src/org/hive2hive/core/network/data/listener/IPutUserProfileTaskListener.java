package org.hive2hive.core.network.data.listener;

import org.hive2hive.core.network.data.futures.FuturePutUserProfileTaskListener;

/**
 * An interface used in {@link FuturePutUserProfileTaskListener} to inform entities about a success or fail of a put.
 * 
 * @author Seppi
 */
public interface IPutUserProfileTaskListener {

	/**
	 * Put of user profile task has succeeded.
	 */
	public void onPutUserProfileTaskSuccess();

	/**
	 * Put of user profile task has failed.
	 */
	public void onPutUserProfileTaskFailure();

}
