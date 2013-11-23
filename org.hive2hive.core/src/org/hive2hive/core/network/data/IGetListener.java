package org.hive2hive.core.network.data;

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

	public void handleGetResult(NetworkContent content);

}
