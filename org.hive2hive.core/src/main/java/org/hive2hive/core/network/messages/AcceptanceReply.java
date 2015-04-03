package org.hive2hive.core.network.messages;

import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;

/**
 * Represents the answer of a node which is asked to handle a specific message. This reply is used to inform
 * the sender of the message as quickly as possible (synchronously) if the sent message will be handled by the
 * target node or not.
 * 
 * @author Nendor, Seppi
 */
public enum AcceptanceReply {
	/** Default for 'everything is OK - I will handle this message' */
	OK,
	/** Provisional ok, e.g. when the signature of the message couldn't be checked. */
	OK_PROVISIONAL,
	/** Default for 'something went wrong, I won't handle this message' */
	FAILURE,
	/** When decryption of message failed */
	FAILURE_DECRYPTION,
	/** When signature is wrong */
	FAILURE_SIGNATURE,
	/** When the deserialization failed */
	FAILURE_DESERIALIZATION,
	/**
	 * It indicates that there is no {@link IResponseCallBackHandler} waiting for the return message on this
	 * 'sender' node.
	 */
	NO_CALLBACK_HANDLER_FOR_THIS_MESSAGE,
	/**
	 * Indicates that the future was not successful. This means that the message could not be delivered to the
	 * target node because of some network issues
	 */
	FUTURE_FAILURE,
	/** Generic message to indicate that the message was sent to the wrong target. */
	WRONG_TARGET
}
