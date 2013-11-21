package org.hive2hive.core.network.data;

import org.hive2hive.core.network.data.futures.FuturePutListener;
import org.hive2hive.core.process.common.put.BasePutProcessStep;

/**
 * An interface used in {@link FuturePutListener} to inform entities about a success or fail of a put. </br>
 * <b>Example:</b> {@link BasePutProcessStep#onSuccess()} or {@link BasePutProcessStep#onFailure()}
 * 
 * @author Seppi
 */
public interface IPutListener {

	/**
	 * Put has succeeded.
	 */
	public void onPutSuccess();

	/**
	 * Put has failed.
	 */
	public void onPutFailure();

}
