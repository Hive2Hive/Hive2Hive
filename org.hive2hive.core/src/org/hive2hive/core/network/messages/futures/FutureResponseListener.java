package org.hive2hive.core.network.messages.futures;

import java.util.Collection;
import java.util.List;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureResponse;
import net.tomp2p.message.Buffer;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.IBaseMessageListener;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;

public class FutureResponseListener extends BaseFutureAdapter<FutureResponse> {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(FutureResponseListener.class);

	private final IBaseMessageListener listener;
	private final BaseDirectMessage message;
	private final NetworkManager networkManager;

	public FutureResponseListener(IBaseMessageListener listener, BaseDirectMessage message,
			NetworkManager networkManager) {
		this.listener = listener;
		this.message = message;
		this.networkManager = networkManager;
	}

	@Override
	public void operationComplete(FutureResponse future) throws Exception {
		AcceptanceReply reply = extractAcceptanceReply(future);
		if (reply == AcceptanceReply.OK) {
			listener.onSuccess();
		} else {
			boolean directResending = message.handleSendingFailure(reply);
			if (directResending) {
				FutureResponse futureResponse = networkManager.sendDirect(message);
				futureResponse.addListener(new FutureResponseListener(listener, message, networkManager));
			} else {
				if (message.needsRedirectedSend()) {
					logger.warn(String
							.format("Sending direct message failed. Using normal routed sending as fallback. target key = '&s' target address = '%s'",
									message.getTargetKey(), message.getTargetAddress()));
					FutureDirect futureDirect = networkManager.send(message);
					futureDirect.addListener(new FutureDirectListener(listener, message, networkManager));
				} else {
					listener.onFailure();
				}
			}
		}
	}

	public AcceptanceReply extractAcceptanceReply(FutureDirect aFuture) {
		String errorReason = "";
		if (aFuture.isSuccess()) {
			Collection<Object> returndedObject = aFuture.getRawDirectData2().values();
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
			logger.error(String.format("Future not successful. reason = '%s'", aFuture.getFailedReason()));
			return AcceptanceReply.FUTURE_FAILURE;
		}
	}

	private AcceptanceReply extractAcceptanceReply(FutureResponse aFuture) {
		String errorReason = "";
		if (aFuture.isSuccess()) {
			List<Buffer> returnedBuffer = aFuture.getResponse().getBufferList();
			if (returnedBuffer == null) {
				errorReason = "Returned buffer is null.";
			} else if (returnedBuffer.isEmpty()) {
				errorReason = "Returned buffer is empty.";
			} else {
				Buffer firstReturnedBuffer = returnedBuffer.iterator().next();
				if (firstReturnedBuffer == null) {
					errorReason = "First returned buffer is null.";
				} else {
					Object responseObject;
					try {
						responseObject = firstReturnedBuffer.object();
						if (responseObject instanceof AcceptanceReply) {
							AcceptanceReply reply = (AcceptanceReply) responseObject;
							return reply;
						} else {
							errorReason = "The returned object was not of type AcceptanceReply!";
						}
					} catch (Exception e) {
						errorReason = "Exception occured while getting the object.";
					}
				}
			}
			logger.error(String.format("A failure while sending a message occured. reason = '%s'",
					errorReason));
			return AcceptanceReply.FAILURE;
		} else {
			logger.error(String.format("Future not successful. reason = '%s'", aFuture.getFailedReason()));
			return AcceptanceReply.FUTURE_FAILURE;
		}
	}

}