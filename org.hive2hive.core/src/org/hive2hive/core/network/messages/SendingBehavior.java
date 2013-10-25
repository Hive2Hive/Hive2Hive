package org.hive2hive.core.network.messages;

/**
 * Determines how a message should be handled if a failure in the sending to
 * the target node occurs.
 * 
 * @author Nendor
 */
public enum SendingBehavior {
	/** This message should be sent only once */
	SEND_ONCE,
	/**
	 * In case of a sending failure this message should be resent up to
	 * {@link B2BConstants#MAX_SEND_MESSAGE_TRIES} times
	 */
	SEND_MAX_ALLOWED_TIMES
}
