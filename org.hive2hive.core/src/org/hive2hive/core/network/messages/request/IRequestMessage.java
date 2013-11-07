package org.hive2hive.core.network.messages.request;

import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;

public interface IRequestMessage {

	/**
	 * Setter
	 * 
	 * @param aHandler
	 *            a {@link IResponseCallBackHandler} for handling responses from receiver node
	 */
	public void setCallBackHandler(IResponseCallBackHandler aHandler);
	
	/**
	 * Getter
	 * 
	 * @return the callback handler (if set)
	 */
	public IResponseCallBackHandler getCallBackHandler();

}
