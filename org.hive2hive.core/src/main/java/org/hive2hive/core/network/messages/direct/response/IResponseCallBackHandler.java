package org.hive2hive.core.network.messages.direct.response;

/**
 * A callback handler interface for receiving response messages. For further implementation
 * details see {@link ResponseMessage}.
 * 
 * @author Nendor, Seppi
 */
public interface IResponseCallBackHandler {
	
	/**
	 * Handle a received {@link ResponseMessage}.
	 * 
	 * @param responseMessage
	 *            the received {@link ResponseMessage}
	 */
	public void handleResponseMessage(ResponseMessage responseMessage);
}