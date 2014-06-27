package org.hive2hive.core.network.messages.futures;

import java.security.PublicKey;
import java.util.concurrent.CountDownLatch;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDirect;

import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.MessageManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this future adapter when sending a {@link BaseDirectMessage}. Attach this listener to the future which
 * gets
 * returned at {@link MessageManager#send(BaseMessage)} to enable a appropriate failure handling. Use the
 * {@link FutureRoutedListener#await()} method to wait blocking until the message is sent (or
 * not).</br></br>
 * <b>Failure Handling</b></br>
 * Sending a direct message can fail when the future object failed, when the future object contains wrong data
 * or the
 * responding node detected a failure. See {@link AcceptanceReply} for possible failures. If sending of a
 * message fails the message gets re-send as long as
 * {@link BaseDirectMessage#handleSendingFailure(AcceptanceReply)} of the sent message recommends to re-send.
 * Depending on the {@link BaseDirectMessage#needsRedirectedSend()} flag a possible fall back is to use the
 * routing mechanism of {@link MessageManager#send(BaseMessage)}. For that another adapter
 * (see {@link FutureDirectListener}) is attached.
 * 
 * @author Seppi, Nico
 */
public class FutureDirectListener extends BaseFutureAdapter<FutureDirect> {

	private static final Logger logger = LoggerFactory.getLogger(FutureDirectListener.class);

	private final BaseDirectMessage message;
	private final PublicKey receiverPublicKey;
	private final MessageManager messageManager;
	private final CountDownLatch latch;
	private DeliveryState state;

	private enum DeliveryState {
		SUCCESS,
		ERROR,
		RESEND_DIRECT,
		RESEND_ROUTED
	}

	/**
	 * Constructor for a future adapter.
	 * 
	 * @param message
	 *            message which has been sent (needed for re-sending)
	 * @param receiverPublicKey
	 *            the receivers public key which was used for encryption
	 * @param networkManager
	 *            reference needed for re-sending)
	 */
	public FutureDirectListener(BaseDirectMessage message, PublicKey receiverPublicKey, MessageManager messageManager) {
		this.message = message;
		this.receiverPublicKey = receiverPublicKey;
		this.messageManager = messageManager;
		this.latch = new CountDownLatch(1);
	}

	/**
	 * Wait (blocking) until the message is sent
	 * 
	 * @return true if successful, false if not successful
	 */
	public boolean await() {
		try {
			latch.await();
		} catch (InterruptedException e) {
			logger.error("Could not wait until the message is sent successfully.");
		}

		switch (state) {
			case SUCCESS:
				// successfully delivered the message
				return true;
			case ERROR:
				// failed to deliver message. Resend not recommended
				return false;
			case RESEND_DIRECT:
				// resend direct is recommended
				return messageManager.sendDirect(message, receiverPublicKey);
			case RESEND_ROUTED:
				// resend (this time routed) is recommended
				return messageManager.send(message, receiverPublicKey);
			default:
				// invalid state
				logger.error("The sending procedure has not finished, but the lock has already been released.");
				return false;
		}
	}

	@Override
	public void operationComplete(FutureDirect future) throws Exception {
		AcceptanceReply reply = extractAcceptanceReply(future);
		if (reply == AcceptanceReply.OK || reply == AcceptanceReply.OK_PROVISIONAL) {
			// notify the listener about the success of sending the message
			state = DeliveryState.SUCCESS;
			latch.countDown();
		} else {
			// check if a direct re-send is necessary / wished
			boolean directResending = message.handleSendingFailure(reply);
			if (directResending) {
				// re-send directly the message
				state = DeliveryState.RESEND_DIRECT;
				latch.countDown();
			} else {
				// check if the routed sending fall back is allowed
				if (message.needsRedirectedSend()) {
					logger.warn(
							"Sending direct message failed. Using normal routed sending as fallback. Target key = '{}', Target address = '{}'.",
							message.getTargetKey(), message.getTargetAddress());
					// re-send the message (routed)
					state = DeliveryState.RESEND_ROUTED;
					latch.countDown();
				} else {
					// notify the listener about the fail of sending the message
					state = DeliveryState.ERROR;
					latch.countDown();
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
					return (AcceptanceReply) responseObject;
				} else {
					errorReason = "The returned object was not of type AcceptanceReply!";
				}
			} catch (Exception e) {
				errorReason = "Exception occured while getting the object.";
			}
			logger.error("A failure while sending a message occured. Reason = '{}'.", errorReason);
			return AcceptanceReply.FAILURE;
		} else {
			logger.error("Future not successful. Reason = '{}'.", future.getFailedReason());
			return AcceptanceReply.FUTURE_FAILURE;
		}
	}

}
