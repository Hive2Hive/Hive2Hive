package org.hive2hive.core.network.messages.direct;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.PeerAddressDiscoveryMessage;
import org.hive2hive.core.network.messages.request.callback.PeerAddressDiscoveryDirectMessagingCallBackHandler;

public abstract class BaseDirectMessage extends BaseMessage {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(BaseDirectMessage.class);

	private static final long serialVersionUID = 5080812282190501445L;

	private PeerAddress targetPeerAddress;
	private final boolean needsRedirectedSend;
	private final boolean useDHTCache;

	/**
	 * This is the abstract base class for messages which are sent directly (via TCP) to a target node.
	 * 
	 * @param aMessageID the ID of this message
	 * @param aTargetKey the target key to which this message should be routed
	 * @param aTargetPeerAddress the {@link PeerAddress} of the target node
	 * @param aNeedsRedirectedSend flag which indicates if this message should be rerouted if a direct sending
	 *            to the {@link PeerAddress} fails
	 * @param useDHTToStorePeerAddress flag which indicates if the {@link PeerAddress} for the target key
	 *            should be cached in the global DHT or not
	 */
	public BaseDirectMessage(String aMessageID, String aTargetKey, PeerAddress aTargetPeerAddress,
			boolean aNeedsRedirectedSend, boolean useDHTToStorePeerAddress) {
		super(aMessageID, aTargetKey);
		targetPeerAddress = aTargetPeerAddress;
		needsRedirectedSend = aNeedsRedirectedSend;
		useDHTCache = useDHTToStorePeerAddress;
	}

	/**
	 * This is the abstract base class for messages which are sent directly (via TCP) to a target node.
	 * 
	 * @param aTargetKey the target key to which this message should be routed
	 * @param aTargetPeerAddress the {@link PeerAddress} of the target node
	 * @param aNeedsRedirectedSend flag which indicates if this message should be rerouted if a direct sending
	 *            to the {@link PeerAddress} fails
	 * @param useDHTCachForAddress flag which indicates if the {@link PeerAddress} for the target key
	 *            should be cached in the global DHT or not
	 */
	public BaseDirectMessage(String aTargetKey, PeerAddress aTargetPeerAddress, boolean aNeedsRedirectedSend,
			boolean useDHTCachForAddress) {
		this(createMessageID(), aTargetKey, aTargetPeerAddress, aNeedsRedirectedSend, useDHTCachForAddress);
	}

	public boolean needsRedirectdSend() {
		return needsRedirectedSend;
	}

	public boolean usesDHTCach() {
		return useDHTCache;
	}

	public PeerAddress getTargetAddress() {
		return targetPeerAddress;
	}

	public void setTargetPeerAddress(PeerAddress aTargetPeerAddress) {
		targetPeerAddress = aTargetPeerAddress;
	}

	public void discoverPeerAddressAndSendMe(NetworkManager aNetworkManager) {
		PeerAddressDiscoveryMessage discoveryMessage = new PeerAddressDiscoveryMessage(getTargetKey(),
				aNetworkManager.getPeerAddress());
		PeerAddressDiscoveryDirectMessagingCallBackHandler handler = new PeerAddressDiscoveryDirectMessagingCallBackHandler(
				aNetworkManager, this);
		discoveryMessage.setCallBackHandler(handler);
		aNetworkManager.send(discoveryMessage);
	}

	@Override
	public AcceptanceReply accept() {
		if (networkManager.getPeerAddress().equals(targetPeerAddress)) {
			return AcceptanceReply.OK;
		}
		return AcceptanceReply.WRONG_TARGET;
	}

	protected void updateDHTCacheIfNeeded() {
		if (useDHTCache) {
			// TODO store peer address into DHT
//			DataWrapper dataWrapper = new DataWrapper(Type.TEST, targetPeerAddress);
//			FutureDHT future = networkManager.store(getTargetKey(), B2BConstants.PEER_ADDRESS, dataWrapper);
//			future.awaitUninterruptibly();
		}
	}

	@Override
	public void handleSendingFailure(AcceptanceReply reply, NetworkManager aNetworkManager) {
		logger.debug(String.format("Have to handle a sending failure. AcceptanceReply='%s'", reply));
		if (AcceptanceReply.FUTURE_FAILURE == reply) {
			logger.debug(String.format(
					"Failure while sending this message directly using the peer address '%s' ",
					getTargetAddress()));
			if (needsRedirectdSend()) {
				discoverPeerAddressAndSendMe(aNetworkManager);
			}
		} else {
			super.handleSendingFailure(reply, aNetworkManager);
		}
	}
	
	@Override
	public void run() {
		updateDHTCacheIfNeeded();
	}

}
