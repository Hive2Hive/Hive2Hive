package org.hive2hive.core.network.data.listener;

import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.futures.FutureGetListener;
import org.hive2hive.core.process.common.get.BaseGetProcessStep;

/**
 * An interface used in {@link FutureGetListener} to inform entities about a success or fail of a get. </br>
 * <b>Example:</b> {@link BaseGetProcessStep#onSuccess(NetworkContent)} or
 * {@link BaseGetProcessStep#onFailure()}
 * 
 * @author Seppi
 */
public interface IGetListener {

	/**
	 * Gets called when get finishes. Can be <code>null</code> if something went wrong or required object
	 * doesn't exist in the network.
	 * 
	 * @param content
	 */
	public void handleGetResult(NetworkContent content);

}
