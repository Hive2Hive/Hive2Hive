package org.hive2hive.core.network.messages.futures;

import java.security.PublicKey;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureSend;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.MessageManager;

/**
 * Use this future adapter when sending a {@link BaseMessage}. Attach this listener to the future which gets
 * returned at {@link MessageManager#send(BaseMessage)} to enable a appropriate failure handling. Use the
 * {link {@link FutureRoutedListener#await()} method to wait blocking until the message is sent (or
 * not).</br></br>
 * <b>Failure Handling</b></br>
 * Sending a message can fail when the future object failed, when the future object contains wrong data or the
 * responding node detected a failure. See {@link AcceptanceReply} for possible failures. If sending of a
 * message fails the message gets re-send as long as {@link BaseMessage#handleSendingFailure(AcceptanceReply)}
 * of the sent message recommends to re-send. Because all re-sends are also asynchronous the future
 * listener attaches himself to the new future objects so that the adapter can finally notify his/her listener
 * about a success or failure.
 * 
 * @author Seppi, Nico
 */
public class FutureRoutedListener extends BaseFutureAdapter<FutureSend> {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(FutureRoutedListener.class);

	private final BaseMessage message;
	private final PublicKey receiverPublicKey;
	private final NetworkManager networkManager;
	private final CountDownLatch latch;
	private boolean success = false;

	/**
	 * Constructor for a future adapter.
	 * 
	 * @param message
	 *            message which has been sent (needed for re-sending)
	 * @param networkManager
	 *            reference needed for re-sending)
	 */
	public FutureRoutedListener(BaseMessage message, PublicKey receiverPublicKey,
			NetworkManager networkManager) {
		this.message = message;
		this.receiverPublicKey = receiverPublicKey;
		this.networkManager = networkManager;
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
			logger.error("Could not wait until the message is sent successfully");
		}

		return success;
	}

	@Override
	public void operationComplete(FutureSend future) throws Exception {
		AcceptanceReply reply = extractAcceptanceReply(future);
		if (reply == AcceptanceReply.OK) {
			// notify the listener about the success of sending the message
			success = true;
			latch.countDown();
		} else {
			// check if a re-send is necessary / wished
			boolean resending = message.handleSendingFailure(reply);
			if (resending) {
				// re-send the message
				logger.debug("Try to resend the message");
				success = networkManager.send(message, receiverPublicKey);
				latch.countDown();
			} else {
				// notify the listener about the fail of sending the message
				logger.debug("No resend of the message. It failed");
				success = false;
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