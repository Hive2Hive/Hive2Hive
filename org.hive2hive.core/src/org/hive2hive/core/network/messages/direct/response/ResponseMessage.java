package org.hive2hive.core.network.messages.direct.response;

import java.io.Serializable;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.request.callback.ICallBackHandler;

public class ResponseMessage extends BaseDirectMessage {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(ResponseMessage.class);

	private static final long serialVersionUID = -4182581031050888858L;

	private final PeerAddress targetAddress;
	private final Serializable content;
	private final MessageType type;

	public enum MessageType {
		NOT_SET,
		OK,
		VERSION_UPDATE_BLOCKED,
		NO_RETURN_MESSAGE,
		WRONG_USER,
		WRONG_PASSWORD,
		LOAD_DIRECT_FAILED,
		FRIEND_REQUEST_FAILED,
		FRIEND_REQUEST_SUCCEDED,
		FAILURE,
		LOAD_GLOBAL_FAILED,
		UNEXPECTED_CLASSTYPE,
		ERROR_LOADING_USER_MESSAGE_LIST,
		DECRYPTION_FAILURE,
		DONT_HAVE_FILE
	}

	public ResponseMessage(String aMessageID, MessageType aType, String aTargetKey,
			PeerAddress aSenderPeerAddress, Serializable someContent) {
		super(aMessageID, aTargetKey, aSenderPeerAddress, false, false);
		targetAddress = aSenderPeerAddress;
		content = someContent;
		type = aType;
	}

	@Override
	public void run() {
		ICallBackHandler handler = networkManager.getMessageManager().getCallBackHandlers()
				.remove(getMessageID());
		if (handler != null) {
			handler.handleReturnMessage(this);
		} else {
			logger.warn(String.format(
					"No call back handler for this message! currentNodeID='%s', AsyncReturnMessage='%s'",
					networkManager.getNodeId(), this));
		}

	}

	@Override
	public AcceptanceReply accept() {
		if (networkManager.getMessageManager().getCallBackHandlers().get(getMessageID()) != null) {
			return AcceptanceReply.OK;
		}
		return AcceptanceReply.NO_CALLBACK_HANDLER_FOR_THIS_MESSAGE;
	}

	public PeerAddress getTargetAddress() {
		return targetAddress;
	}

	public Object getContent() {
		return content;
	}

	public MessageType getType() {
		return type;
	}

}
