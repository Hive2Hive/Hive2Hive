package org.hive2hive.core.network.messages.request;

import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;

/**
 * An interface which allows a {@link BaseMessage} or {@link BaseDirectMessage} message to have a
 * {@link IResponseCallBackHandler} callback handler. The callback handler gets called when a
 * {@link ResponseMessage} message with a response arrived at the requesting node.
 * 
 * @author Nendor, Seppi
 */
public interface IRequestMessage {

	/**
	 * Setter
	 * 
	 * @param aHandler
	 *            a {@link IResponseCallBackHandler} for handling responses from receiver node
	 */
	public void setCallBackHandler(IResponseCallBackHandler handler);

	/**
	 * Getter
	 * 
	 * @return the callback handler (if set)
	 */
	public IResponseCallBackHandler getCallBackHandler();

}
