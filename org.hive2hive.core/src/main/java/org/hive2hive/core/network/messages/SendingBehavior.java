package org.hive2hive.core.network.messages;

import org.hive2hive.core.H2HConstants;

/**
 * Determines how a message should be handled if a failure in the sending to
 * the target node occurs.
 * 
 * @author Nendor
 */
public enum SendingBehavior {
	/** This message should be sent only once */
	// TODO Currently not used. Thus, could remove this class
	SEND_ONCE,
	/**
	 * In case of a sending failure this message should be resent up to
	 * {@link H2HConstants#MAX_MESSAGE_SENDING} times
	 */
	SEND_MAX_ALLOWED_TIMES
}
