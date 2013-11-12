package org.hive2hive.core.network.messages.futures;

import java.security.PublicKey;
import java.util.Collection;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDirect;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.IBaseMessageListener;
import org.hive2hive.core.network.messages.MessageManager;

/**
 * Use this future adapter when sending a {@link BaseMessage}. Attach this listener to the future which gets
 * returned at {@link MessageManager#send(BaseMessage)} to enable a appropriate failure handling and notifying
 * {@link IBaseMessageListener} listeners. In case of a successful sending
 * {@link IBaseMessageListener#onSuccess()} gets called. In case of a failed sending
 * {@link IBaseMessageListener#onFailure()} gets called. </br></br>
 * <b>Failure Handling</b></br>
 * Sending a message can fail when the future object failed, when the future object contains wrong data or the
 * responding node detected a failure. See {@link AcceptanceReply} for possible failures. If sending of a
 * message fails the message gets re-send as long as {@link BaseMessage#handleSendingFailure(AcceptanceReply)}
 * of the sent message recommends to re-send. Because all re-sends are also asynchronous the future
 * listener attaches himself to the new future objects so that the adapter can finally notify his/her listener
 * about a success or failure.
 * 
 * @author Seppi
 */
public class FutureRoutedListener extends BaseFutureAdapter<FutureDirect> {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(FutureRoutedListener.class);

	private final IBaseMessageListener listener;
	private final BaseMessage message;
	private final PublicKey receiverPublicKey;
	private final NetworkManager networkManager;

	/**
	 * Constructor for a future adapter.
	 * 
	 * @param listener
	 *            listener which gets notified when sending succeeded or failed
	 * @param message
	 *            message which has been sent (needed for re-sending)
	 * @param networkManager
	 *            reference needed for re-sending)
	 */
	public FutureRoutedListener(IBaseMessageListener listener, BaseMessage message,
			PublicKey receiverPublicKey, NetworkManager networkManager) {
		this.listener = listener;
		this.message = message;
		this.receiverPublicKey = receiverPublicKey;
		this.networkManager = networkManager;
	}

	@Override
	public void operationComplete(FutureDirect future) throws Exception {
		AcceptanceReply reply = extractAcceptanceReply(future);
		if (reply == AcceptanceReply.OK) {
			// notify the listener about the success of sending the message
			if (listener != null)
				listener.onSuccess();
		} else {
			// check if a re-send is necessary / wished
			boolean resending = message.handleSendingFailure(reply);
			if (resending) {
				// re-send the message
				networkManager.send(message, receiverPublicKey, listener);
			} else {
				// notify the listener about the fail of sending the message
				if (listener != null)
					listener.onFailure();
			}
		}
	}

	/**
	 * Check if the given future contains any useful results and log if something went wrong while sending.
	 * Generate an acceptance reply.
	 * 
	 * @param future
	 *            a future
	 * @return a reply showing the result of sending
	 */
	private AcceptanceReply extractAcceptanceReply(FutureDirect future) {
		String errorReason = "";
		if (future.isSuccess()) {
			Collection<Object> returndedObject = future.getRawDirectData2().values();
			if (returndedObject == null) {
				errorReason = "Returned object is null.";
			} else if (returndedObject.isEmpty()) {
				errorReason = "Returned raw data is empty.";
			} else {
				Object firstReturnedObject = returndedObject.iterator().next();
				if (firstReturnedObject == null) {
					errorReason = "First returned object is null.";
				} else if (firstReturnedObject instanceof AcceptanceReply) {
					AcceptanceReply reply = (AcceptanceReply) firstReturnedObject;
					return reply;
				} else {
					errorReason = "The returned object was not of type AcceptanceReply!";
				}
			}
			logger.error(String.format("A failure while sending a message occured. reason = '%s'",
					errorReason));
			return AcceptanceReply.FAILURE;
		} else {
			logger.error(String.format("Future not successful. reason = '%s'", future.getFailedReason()));
			return AcceptanceReply.FUTURE_FAILURE;
		}
	}

}