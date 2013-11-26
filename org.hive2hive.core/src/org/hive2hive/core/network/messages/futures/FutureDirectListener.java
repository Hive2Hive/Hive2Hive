package org.hive2hive.core.network.messages.futures;

import java.security.PublicKey;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDirect;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.IBaseMessageListener;
import org.hive2hive.core.network.messages.MessageManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;

/**
 * Use this future adapter when sending a {@link BaseDirectMessage}. Attach this listener to the future which
 * gets returned at {@link MessageManager#sendDirect(BaseDirectMessage)} to enable an appropriate failure
 * handling and notifying {@link IBaseMessageListener} listeners. In case of a successful sending
 * {@link IBaseMessageListener#onSuccess()} gets called. In case of a failed sending
 * {@link IBaseMessageListener#onFailure()} gets called. </br></br>
 * <b>Failure Handling</b></br>
 * Sending a direct message can fail when the future object failed, when the future object contains wrong data
 * or the
 * responding node detected a failure. See {@link AcceptanceReply} for possible failures. If sending of a
 * message fails the message gets re-send as long as
 * {@link BaseDirectMessage#handleSendingFailure(AcceptanceReply)} of the sent message recommends to re-send.
 * Depending on the {@link BaseDirectMessage#needsRedirectedSend()} flag a possible fall back is to use the
 * routing mechanism of {@link MessageManager#send(BaseMessage)}. For that another adapter
 * (see {@link FutureDirectListener}) is attached. Because all re-sends are also asynchronous the future
 * listener attaches himself to the new future objects (also in case of switching on the fall back mechanism)
 * so that the adapter can finally notify his/her listener about a success or failure.
 * 
 * @author Seppi
 */
public class FutureDirectListener extends BaseFutureAdapter<FutureDirect> {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(FutureDirectListener.class);

	private final IBaseMessageListener listener;
	private final BaseDirectMessage message;
	private final PublicKey receiverPublicKey;
	private final NetworkManager networkManager;

	/**
	 * Constructor for a future adapter.
	 * 
	 * @param listener
	 *            listener which gets notified when sending succeeded or failed
	 * @param message
	 *            message which has been sent (needed for re-sending)
	 * @param receiverPublicKey
	 *            the receivers public key which was used for encryption
	 * @param networkManager
	 *            reference needed for re-sending)
	 */
	public FutureDirectListener(IBaseMessageListener listener, BaseDirectMessage message,
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
			// check if a direct re-send is necessary / wished
			boolean directResending = message.handleSendingFailure(reply);
			if (directResending) {
				// re-send directly the message
				networkManager.sendDirect(message, receiverPublicKey, listener);
			} else {
				// check if the routed sending fall back is allowed
				if (message.needsRedirectedSend()) {
					logger.warn(String
							.format("Sending direct message failed. Using normal routed sending as fallback. target key = '&s' target address = '%s'",
									message.getTargetKey(), message.getTargetAddress()));
					// re-send the message (routed)
					networkManager.send(message, receiverPublicKey, listener);
				} else {
					// notify the listener about the fail of sending the message
					if (listener != null)
						listener.onFailure();
				}
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
			Object responseObject;
			try {
				responseObject = future.object();
				if (responseObject == null) {
					errorReason = "Returned object is null";
				} else if (responseObject instanceof AcceptanceReply) {
					AcceptanceReply reply = (AcceptanceReply) responseObject;
					return reply;
				} else {
					errorReason = "The returned object was not of type AcceptanceReply!";
				}
			} catch (Exception e) {
				errorReason = "Exception occured while getting the object.";
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
