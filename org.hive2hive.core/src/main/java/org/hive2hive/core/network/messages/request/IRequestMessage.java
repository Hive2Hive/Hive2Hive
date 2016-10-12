package org.hive2hive.core.network.messages.request;

import java.io.Serializable;
import java.util.Set;

import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;

/**
 * An interface which allows a {@link BaseMessage} or {@link BaseDirectMessage} messages to have a
 * {@link IResponseCallBackHandler} callback handler. The callback handler gets called when a
 * {@link ResponseMessage} message with a response arrived at the requesting node.
 * 
 * @author Nendor, Seppi, Christian
 */
public interface IRequestMessage {

	/**
	 * Setter
	 * 
	 * @param handler
	 *            a {@link IResponseCallBackHandler} for handling responses from receiver node
	 */
	public void setCallBackHandler(IResponseCallBackHandler handler);

	/**
	 * Add
	 *
	 *  @param handler
	 *            a {@link IResponseCallBackHandler} for handling responses from receiver node
	 */
	public void addCallBackHandler(IResponseCallBackHandler handler);

	/**
	 * Getter
	 * 
	 * @return the callback handler (if set)
	 */
	public Set<IResponseCallBackHandler> getCallBackHandlers();

	/**
	 * Configures the {@link ResponseMessage} for this {@link RoutedRequestMessage} with the correct message
	 * ID
	 * and receiver address.
	 * 
	 * @param content The content of the response.
	 * @return The configured {@link ResponseMessage}.
	 */
	public ResponseMessage createResponse(Serializable content);

	/**
	 * Sends the {@link ResponseMessage} <b>directly</b> to its requester.
	 * 
	 * @param response The {@link ResponseMessage} created with
	 *            {@link RoutedRequestMessage#createResponse(Serializable)}.
	 */
	public void sendDirectResponse(ResponseMessage response);

	/**
	 * Customize time for wait message callBack
	 * */
	public int getDirectDownloadWaitMs();

}
