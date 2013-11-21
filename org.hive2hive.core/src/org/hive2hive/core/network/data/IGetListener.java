package org.hive2hive.core.network.data;

import org.hive2hive.core.network.data.futures.FutureGetListener;
import org.hive2hive.core.process.common.get.BaseGetProcessStep;

/**
 * An interface used in {@link FutureGetListener} to inform entities about a success or fail of a put. </br>
 * <b>Example:</b> {@link BaseGetProcessStep#onSuccess(NetworkContent)} or {@link BaseGetProcessStep#onFailure()}
 * 
 * @author Seppi
 */
public interface IGetListener {

	/**
	 * Put has succeeded.
	 */
	public void onSuccess(NetworkContent content);

	/**
	 * Put has failed.
	 */
	public void onFailure();

}
