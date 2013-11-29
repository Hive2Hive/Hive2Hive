package org.hive2hive.core.network.data.listener;

import org.hive2hive.core.network.data.futures.FutureRemoveListener;
import org.hive2hive.core.process.common.remove.BaseRemoveProcessStep;

/**
 * An interface used in {@link FutureRemoveListener} to inform entities about a success or fail of a remove.
 * </br>
 * <b>Example:</b> {@link BaseRemoveProcessStep#onSuccess()} or {@link BaseRemoveProcessStep#onFailure()}
 * 
 * @author Seppi
 */
public interface IRemoveListener {

	/**
	 * Remove has succeeded.
	 */
	public void onRemoveSuccess();

	/**
	 * Remove has failed.
	 */
	public void onRemoveFailure();

}
