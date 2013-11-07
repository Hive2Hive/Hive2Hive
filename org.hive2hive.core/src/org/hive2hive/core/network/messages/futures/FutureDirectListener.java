package org.hive2hive.core.network.messages.futures;

import java.util.Collection;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.IBaseMessageListener;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDirect;

/**
 * 
 * @author Seppi
 */
public class FutureDirectListener extends BaseFutureAdapter<FutureDirect> {
	
	private static final H2HLogger logger = H2HLoggerFactory.getLogger(FutureDirectListener.class);

	private final IBaseMessageListener listener;
	private final BaseMessage message;
	private final NetworkManager networkManager;

	public FutureDirectListener(IBaseMessageListener listener, BaseMessage message, NetworkManager networkManager) {
		this.listener = listener;
		this.message = message;
		this.networkManager = networkManager;
	}

	@Override
	public void operationComplete(FutureDirect future) throws Exception {
		AcceptanceReply reply = extractAcceptanceReply(future);
		if (reply == AcceptanceReply.OK) {
			listener.onSuccess();
		} else {
			boolean resending = message.handleSendingFailure(reply);
			if (resending) {
				FutureDirect futureDirect = networkManager.send(message);
				futureDirect.addListener(new FutureDirectListener(listener, message, networkManager));
			} else {
				listener.onFailure();
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

}