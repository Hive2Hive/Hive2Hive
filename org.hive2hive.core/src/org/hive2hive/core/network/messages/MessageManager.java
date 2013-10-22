package org.hive2hive.core.network.messages;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.futures.FutureResponse;
import net.tomp2p.p2p.RequestP2PConfiguration;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.hive2hive.core.network.messages.request.callback.ICallBackHandler;

public class MessageManager {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(MessageManager.class);

	private final Map<String, ICallBackHandler> callBackHandlers = new HashMap<String, ICallBackHandler>();
	private final NetworkManager networkManager;

	public MessageManager(NetworkManager aNetworkManager) {
		networkManager = aNetworkManager;

	}

	public void send(BaseMessage aMessage) {
		aMessage.increaseSendingCounter();

		configureCallbackHandlerIfNeeded(aMessage);

		RequestP2PConfiguration requestP2PConfiguration = createSendingConfiguration();
		Number160 keyForMessageID = Number160.createHash(aMessage.getTargetKey());
		// send message to the peer which is responsible for the given key
		FutureDHT future = networkManager.getConnection().getPeer().send(keyForMessageID).setObject(aMessage)
				.setRequestP2PConfiguration(requestP2PConfiguration).setRefreshSeconds(0).start();

		BaseFutureAdapter<FutureDHT> futureListener = new FutureListener(aMessage, networkManager);
		future.addListener(futureListener);

		logger.debug(String.format("Message sent target key = '%s' message id = '%s'",
				aMessage.getTargetKey(), aMessage.getMessageID()));
	}

	/**
	 * Sends a given message directly (TCP) to the peer with the given address.
	 * 
	 * @param aMessge them message to send
	 */
	public void send(BaseDirectMessage aMessage) {
		if (aMessage.getTargetAddress() != null) {
			aMessage.increaseSendingCounter();

			configureCallbackHandlerIfNeeded(aMessage);

			FutureResponse futureResponse = networkManager.getConnection().getPeer()
					.sendDirect(aMessage.getTargetAddress()).setObject(aMessage).start();
			FutureListener2 futureListener = new FutureListener2(aMessage, networkManager);
			futureResponse.addListener(futureListener);
			
			logger.debug(String.format("Message sent (direct) target key = '%s' message id = '%s'",
					aMessage.getTargetKey(), aMessage.getMessageID()));
		} else {
			if (aMessage.usesDHTCach()) {
				Number160 key = Number160.createHash(aMessage.getTargetKey());
				FutureDHT futureDHT = networkManager.getConnection().getPeer().get(key)
						.setContentKey(H2HConstants.PEER_ADDRESS).start();
				futureDHT.addListener(new PeerAddressStorageListener(networkManager, aMessage));
			} else {
				aMessage.discoverPeerAddressAndSendMe(networkManager);
			}
		}

	}

	// TODO_B2B: A full field is exposed to the user - this is not a good encapsulation - change it if
	// possible.
	public Map<String, ICallBackHandler> getCallBackHandlers() {
		return callBackHandlers;
	}

	protected RequestP2PConfiguration createSendingConfiguration() {
		RequestP2PConfiguration requestP2PConfiguration = new RequestP2PConfiguration(1, 10, 0);
		return requestP2PConfiguration;
	}

	private void configureCallbackHandlerIfNeeded(BaseMessage aMessage) throws IllegalArgumentException {
		if (aMessage instanceof IRequestMessage) {
			IRequestMessage messageWithReply = (IRequestMessage) aMessage;
			ICallBackHandler handler = messageWithReply.getCallBackHandler();
			if (handler == null) {
				if (callBackHandlers.get(aMessage.getMessageID()) == null) {
					throw new IllegalArgumentException(
							"CallBackHandler of a message with reply may not be null!");
				}
			}
			callBackHandlers.put(aMessage.getMessageID(), messageWithReply.getCallBackHandler());
			messageWithReply.setCallBackHandler(null);
		}
	}

	private class FutureListener extends BaseFutureAdapter<FutureDHT> {
		private final BaseMessage message;
		private final NetworkManager networkManager;

		public FutureListener(BaseMessage aMessage, NetworkManager aNetworkManager) {
			message = aMessage;
			networkManager = aNetworkManager;
		}

		@Override
		public void operationComplete(FutureDHT future) throws Exception {
			AcceptanceReply reply = extractAcceptanceReply(future);
			if (reply != AcceptanceReply.OK) {
				message.handleSendingFailure(reply, networkManager);
			}
		}

		private AcceptanceReply extractAcceptanceReply(FutureDHT aFuture) {
			String errorReason = "";
			if (aFuture.isSuccess()) {
				try {
					Collection<Object> returndedObject = aFuture.getRawDirectData2().values();
					if (returndedObject != null) {
						if (!returndedObject.isEmpty()) {
							Object firstReturnedObject = returndedObject.iterator().next();
							if (firstReturnedObject instanceof AcceptanceReply) {
								AcceptanceReply reply = (AcceptanceReply) firstReturnedObject;
								return reply;
							} else {
								errorReason = "The returned object was not of type AcceptanceReply!";
							}
						} else {
							errorReason = "Returned object is empty.";
						}
					} else {
						errorReason = "Returned object is null.";
					}
				} catch (Exception e) {
					errorReason = String.format("Exception while getting object from response, '%s'", e);
				}
			} else {
				errorReason = String.format("Future was not successful, reason: %s",
						aFuture.getFailedReason());
			}
			logger.error(String.format(
					"A failure while sending a message occured. Info: reason = '%s' from (node ID) = '%s'",
					errorReason, networkManager.getNodeId()));
			return AcceptanceReply.FAILURE;
		}

	}

	private class FutureListener2 extends BaseFutureAdapter<FutureResponse> {
		private final BaseMessage message;
		private final NetworkManager networkManager;

		public FutureListener2(BaseMessage aMessage, NetworkManager aNetworkManager) {
			message = aMessage;
			networkManager = aNetworkManager;
		}

		@Override
		public void operationComplete(FutureResponse future) throws Exception {
			AcceptanceReply reply = extractAcceptanceReply(future);
			if (reply != AcceptanceReply.OK) {
				message.handleSendingFailure(reply, networkManager);
			}
		}

		private AcceptanceReply extractAcceptanceReply(FutureResponse aFuture) {
			String errorReason = "";
			if (aFuture.isSuccess()) {
				try {
					Object returnedObject = aFuture.getObject();
					if (returnedObject != null) {
						if (returnedObject instanceof AcceptanceReply) {
							AcceptanceReply reply = (AcceptanceReply) returnedObject;
							return reply;
						} else {
							errorReason = "The returned object was not of type AcceptanceReply!";
						}
					} else {
						errorReason = "Returned object is null.";
					}
				} catch (Exception e) {
					errorReason = String.format("Exception while getting object from response, '%s'", e);
				}
				logger.error(String
						.format("A failure while sending a message occured. Info: reason = '%s' from (node ID) = '%s'",
								errorReason, networkManager.getNodeId()));

			} else {
				logger.error(String.format("Future not successful. Reason = '%s' from (node ID) = '%s'",
						aFuture.getFailedReason(), networkManager.getNodeId()));
				return AcceptanceReply.FUTURE_FAILURE;
			}
			return AcceptanceReply.FAILURE;
		}

	}

	private class PeerAddressStorageListener extends BaseFutureAdapter<FutureDHT> {

		private final NetworkManager networkManager;
		private final BaseDirectMessage message;

		public PeerAddressStorageListener(NetworkManager aNetworkManager, BaseDirectMessage aMessage) {
			networkManager = aNetworkManager;
			message = aMessage;
		}

		@Override
		public void operationComplete(FutureDHT future) throws Exception {
			if (future.isSuccess()) {
				if (future.getData() != null) {
					try {
						logger.debug("Peer address found in DHT!");
						PeerAddress peerAddress = (PeerAddress) future.getData().getObject();
						message.setTargetPeerAddress(peerAddress);
						networkManager.send(message);
					} catch (ClassNotFoundException | IOException e) {
						logger.warn("Can't get object from DHT. Reason: ", e);
						message.discoverPeerAddressAndSendMe(networkManager);
					}
				} else {
					logger.debug("No peer address found in DHT.");
					message.discoverPeerAddressAndSendMe(networkManager);
				}
			} else {
				logger.debug(String.format("Loading of peer address failed. Reason: %s",
						future.getFailedReason()));
				message.discoverPeerAddressAndSendMe(networkManager);
			}
		}
	}

}
