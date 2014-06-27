package org.hive2hive.core.network.messages.futures;

import java.security.PublicKey;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureSend;

import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.MessageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this future adapter when sending a {@link BaseMessage}. Attach this listener to the future which gets
 * returned at {@link MessageManager#send(BaseMessage)} to enable a appropriate failure handling. Use the
 * {@link FutureRoutedListener#await()} method to wait blocking until the message is sent (or
 * not).</br></br>
 * <b>Failure Handling</b></br>
 * Sending a message can fail when the future object failed, when the future object contains wrong data or the
 * responding node detected a failure. See {@link AcceptanceReply} for possible failures. If sending of a
 * message fails the message gets re-send as long as {@link BaseMessage#handleSendingFailure(AcceptanceReply)}
 * of the sent message recommends to re-send.
 * Note that resending must happen in the same thread as the {@link FutureRoutedListener#await()} method is
 * called because the callback threads should not be used for further long-calling procedures.
 * 
 * @author Seppi, Nico
 */
public class FutureRoutedListener extends BaseFutureAdapter<FutureSend> {

	private static final Logger logger = LoggerFactory.getLogger(FutureRoutedListener.class);

	private final BaseMessage message;
	private final PublicKey receiverPublicKey;
	private final MessageManager messageManager;
	private final CountDownLatch latch;
	private DeliveryState state;

	private enum DeliveryState {
		SUCCESS,
		ERROR,
		RESEND
	}

	/**
	 * Constructor for a future adapter.
	 * 
	 * @param message
	 *            message which has been sent (needed for re-sending)
	 * @param networkManager
	 *            reference needed for re-sending)
	 */
	public FutureRoutedListener(BaseMessage message, PublicKey receiverPublicKey, MessageManager messageManager) {
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
			case RESEND:
				// resend is recommended
				return messageManager.send(message, receiverPublicKey);
			default:
				// invalid state
				logger.error("The sending procedure has not finished, but the lock has already been released.");
				return false;
		}
	}

	@Override
	public void operationComplete(FutureSend future) throws Exception {
		AcceptanceReply reply = extractAcceptanceReply(future);
		if (reply == AcceptanceReply.OK || reply == AcceptanceReply.OK_PROVISIONAL) {
			// notify the listener about the success of sending the message
			state = DeliveryState.SUCCESS;
			latch.countDown();
		} else {
			// check if a re-send is necessary / wished
			boolean resending = message.handleSendingFailure(reply);
			if (resending) {
				// re-send the message
				logger.debug("Try to resend the message.");
				state = DeliveryState.RESEND;
				latch.countDown();
			} else {
				// notify the listener about the fail of sending the message
				logger.debug("No resending of the message. It failed.");
				state = DeliveryState.ERROR;
				latch.countDown();
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
	private AcceptanceReply extractAcceptanceReply(FutureSend future) {
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
					return (AcceptanceReply) firstReturnedObject;
				} else {
					errorReason = "The returned object was not of type AcceptanceReply!";
				}
			}
			logger.error("A failure while sending a message occured. Reason = '{}'", errorReason);
			return AcceptanceReply.FAILURE;
		} else {
			logger.error("Future not successful. Reason = '{}'.", future.getFailedReason());
			return AcceptanceReply.FUTURE_FAILURE;
		}
	}

}