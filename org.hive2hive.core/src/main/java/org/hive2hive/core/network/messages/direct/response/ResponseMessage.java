package org.hive2hive.core.network.messages.direct.response;

import java.io.Serializable;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This message has to be used for response messages when a request message implementing the
 * {@link IRequestMessage} interface arrives.</br></br>
 * 
 * <b>Important</b> The message id of the response message has to be the same like the requesting message.
 * This is necessary for the requesting peer to find the correct {@link IResponseCallBackHandler} to handle
 * the response message at the requesting node.</br></br>
 * 
 * <b>Design decisions:</b>
 * <ul>
 * <li>A response message is a {@link BaseDirectMessage}. The goal is to contact the requesting node directly.
 * </li>
 * <li>The fall back for re-routing is disabled. It makes no sense to route a response message to another not
 * requesting node.</li>
 * </ul>
 * 
 * @author Nendor, Seppi
 */
public class ResponseMessage extends BaseDirectMessage {

	private static final Logger logger = LoggerFactory.getLogger(ResponseMessage.class);

	private static final long serialVersionUID = -4182581031050888858L;

	private final Serializable content;

	/**
	 * Constructor for a response message.
	 * 
	 * @param messageID
	 *            the message id which has to be the same like the request message id
	 * @param requesterAddress
	 *            the peer address of the requesting node
	 * @param someContent
	 *            the content for any response
	 */
	public ResponseMessage(String messageID, PeerAddress requesterAddress, Serializable someContent) {
		super(messageID, null, requesterAddress, false);
		content = someContent;
	}

	@Override
	public void run() {
		IResponseCallBackHandler handler = messageManager.getCallBackHandler(getMessageID());
		if (handler != null) {
			handler.handleResponseMessage(this);
		} else {
			logger.warn("No call back handler for this message! CurrentNodeID = '{}', AsyncReturnMessage = '{}'.",
					networkManager.getNodeId(), this);
		}
	}

	@Override
	public AcceptanceReply accept() {
		if (messageManager.checkIfCallbackHandlerExists(getMessageID())) {
			return AcceptanceReply.OK;
		}
		return AcceptanceReply.NO_CALLBACK_HANDLER_FOR_THIS_MESSAGE;
	}

	public Object getContent() {
		return content;
	}

	@Override
	public boolean handleSendingFailure(AcceptanceReply reply) {
		if (AcceptanceReply.NO_CALLBACK_HANDLER_FOR_THIS_MESSAGE == reply) {
			logger.warn("Receiving node has no callback handler for this message. Message ID = '{}'.",
					getMessageID());
			return false;
		} else {
			return super.handleSendingFailure(reply);
		}
	}

}
