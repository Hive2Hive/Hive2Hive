package org.hive2hive.core.processes.common.base;

import java.security.PublicKey;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.SendFailedException;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.IMessageManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.hive2hive.core.network.messages.request.RoutedRequestMessage;
import org.hive2hive.processframework.ProcessStep;

/**
 * This is a process step for sending a {@link BaseMessage}.
 * </br></br>
 * 
 * <b>Design decision:</b>
 * <ul>
 * <li>When a request message (e.g. {@link RoutedRequestMessage}) which implements the {@link IRequestMessage}
 * interface is sent, the process step acts also as a {@link IResponseCallBackHandler} callback handler for
 * this message. The whole callback functionality has to be (if desired) implemented in the
 * {@link BaseMessageProcessStep#handleResponseMessage(ResponseMessage)} method.</li>
 * <li>All messages in <code>Hive2Hive</code> are sent synchronous</li>
 * <li>If the message is a request message, {@link #send(BaseMessage, PublicKey)} blocks until the response is
 * here or throws an exception if a timeout occurs.</li>
 * </ul>
 * 
 * @author Seppi, Nico
 */
public abstract class BaseMessageProcessStep extends ProcessStep<Void> implements IResponseCallBackHandler {

	protected final IMessageManager messageManager;
	private CountDownLatch responseLatch;

	public BaseMessageProcessStep(IMessageManager messageManager) {
		this.setName(getClass().getName());
		this.messageManager = messageManager;
	}

	/**
	 * Send a routed message or a direct message (by handing an implementation of {@link BaseDirectMessage}.
	 */
	protected void send(BaseMessage message, PublicKey receiverPublicKey) throws SendFailedException {
		if (message instanceof IRequestMessage) {
			IRequestMessage requestMessage = (IRequestMessage) message;
			requestMessage.setCallBackHandler(this);
			responseLatch = new CountDownLatch(1);
		}

		boolean success;
		if (message instanceof BaseDirectMessage) {
			success = messageManager.sendDirect((BaseDirectMessage) message, receiverPublicKey);
		} else {
			success = messageManager.send(message, receiverPublicKey);
		}

		if (!success) {
			throw new SendFailedException("No success sending the message.");
		} else if (responseLatch != null) {
			try {
				if (message instanceof IRequestMessage)
				{
					IRequestMessage requestMessage = (IRequestMessage) message;

					// wait for the response to arrive with custom time
					if (!responseLatch
							.await(requestMessage.getDirectDownloadWaitMs(), TimeUnit.MILLISECONDS))
					{
						throw new SendFailedException(
								"Response did not arrive within the configured wait time of "
										+ requestMessage.getDirectDownloadWaitMs() + "ms");
					}
				}
				else
				{
					// wait for the response to arrive
					if (!responseLatch
							.await(H2HConstants.DIRECT_DOWNLOAD_AWAIT_MS, TimeUnit.MILLISECONDS))
					{
						throw new SendFailedException(
								"Response did not arrive within the configured wait time of "
										+ H2HConstants.DIRECT_DOWNLOAD_AWAIT_MS + "ms");
					}
				}
			} catch (InterruptedException e) {
				throw new SendFailedException("Cannot wait for the response because interrupted");
			}
		}
	}

	public final void handleResponseMessage(ResponseMessage responseMessage) {
		if (responseLatch != null) {
			responseLatch.countDown();
		}
		handleResponse(responseMessage);
	}

	public abstract void handleResponse(ResponseMessage responseMessage);

}
