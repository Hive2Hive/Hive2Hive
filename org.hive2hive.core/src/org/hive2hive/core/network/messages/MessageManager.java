package org.hive2hive.core.network.messages;

import java.util.HashMap;
import java.util.Map;

import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureResponse;
import net.tomp2p.p2p.RequestP2PConfiguration;
import net.tomp2p.peers.Number160;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.request.IRequestMessage;

public class MessageManager {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(MessageManager.class);

	private final Map<String, IResponseCallBackHandler> callBackHandlers = new HashMap<String, IResponseCallBackHandler>();
	private final NetworkManager networkManager;

	public MessageManager(NetworkManager aNetworkManager) {
		networkManager = aNetworkManager;
	}

	public FutureDirect send(BaseMessage message) {
		message.increaseSendingCounter();
		message.setSenderAddress(networkManager.getPeerAddress());

		configureCallbackHandlerIfNeeded(message);

		RequestP2PConfiguration requestP2PConfiguration = createSendingConfiguration();

		// send message to the peer which is responsible for the given key
		FutureDirect futureDirect = networkManager.getConnection().getPeer()
				.send(Number160.createHash(message.getTargetKey())).setObject(message)
				.setRequestP2PConfiguration(requestP2PConfiguration).start();

		logger.debug(String.format("Message sent target key = '%s' message id = '%s'",
				message.getTargetKey(), message.getMessageID()));

		return futureDirect;
	}

	public FutureResponse sendDirect(BaseDirectMessage message) {
		if (message.getTargetAddress() == null) {
			throw new IllegalArgumentException("target address can not be null");
		}
		
		message.increaseDirectSendingCounter();
		message.setSenderAddress(networkManager.getPeerAddress());
	
		configureCallbackHandlerIfNeeded(message);

		FutureResponse futureResponse = networkManager.getConnection().getPeer()
				.sendDirect(message.getTargetAddress()).setObject(message).start();

		logger.debug(String.format("Message sent (direct) target key = '%s' message id = '%s'",
				message.getTargetKey(), message.getMessageID()));
		
		return futureResponse;
	}

	// TODO: A full field is exposed to the user - this is not a good encapsulation - change it if
	// possible.
	public Map<String, IResponseCallBackHandler> getCallBackHandlers() {
		return callBackHandlers;
	}

	protected RequestP2PConfiguration createSendingConfiguration() {
		RequestP2PConfiguration requestP2PConfiguration = new RequestP2PConfiguration(1, 10, 0);
		return requestP2PConfiguration;
	}

	private void configureCallbackHandlerIfNeeded(BaseMessage message) {
		if (message instanceof IRequestMessage) {
			IRequestMessage requestMessage = (IRequestMessage) message;
			callBackHandlers.put(message.getMessageID(), requestMessage.getCallBackHandler());
			requestMessage.setCallBackHandler(null);
		}
	}

}