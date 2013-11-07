package org.hive2hive.core.network.messages;

import org.hive2hive.core.network.messages.futures.FutureDirectListener;
import org.hive2hive.core.network.messages.futures.FutureResponseListener;
import org.hive2hive.core.process.common.messages.BaseMessageProcessStep;

/**
 * An interface used in {@link FutureDirectListener} and {@link FutureResponseListener} to inform entities
 * about a success or fail while sending a message. </br>
 * <b>Example:</b> {@link BaseMessageProcessStep#onSuccess()} or {@link BaseMessageProcessStep#onFailure()}
 * 
 * @author Seppi
 */
public interface IBaseMessageListener {

	/**
	 * Sending a message has succeeded.
	 */
	public void onSuccess();

	/**
	 * Sending a message has failed.
	 */
	public void onFailure();

}
