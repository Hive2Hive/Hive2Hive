package org.hive2hive.core.network.messages;

import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.EncryptionUtil;

/**
 * This is the general message handler of each node. It checks if received
 * message is ok (depends on message e.g. routed to correct node). If accepted
 * the message gets independently handled in a own thread. As soon as the
 * handler thread has started the reply handler gives immediately response to
 * the sender node. This design allows a quick and non-blocking message
 * handling.
 * 
 * @author Nendor, Seppi
 */
public class MessageReplyHandler implements ObjectDataReply {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(MessageReplyHandler.class);

	private final NetworkManager networkManager;

	public MessageReplyHandler(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	@Override
	public Object reply(PeerAddress sender, Object request) throws Exception {
		
		// asymmetrically decrypt message
		// TODO replace null with the receiver's private key
//		byte[] encryptedMessage = (byte[]) request;
//		byte[] decryptedMessage = EncryptionUtil.decryptRSA(encryptedMessage, null);
//		BaseMessage message = (BaseMessage) EncryptionUtil.deserializeObject(decryptedMessage);
		
		if (request instanceof BaseMessage) {
			
			BaseMessage receivedMessage = (BaseMessage) request;
			receivedMessage.setNetworkManager(networkManager);
			AcceptanceReply reply = receivedMessage.accept();
			if (AcceptanceReply.OK == reply) {
				
				// handle message in own thread
				logger.debug("Received and accepted the message.");
				new Thread(receivedMessage).start();
				
			} else {
				logger.warn(String.format("Received but denied a message. Acceptance reply = '%s'.", reply));
			}
			return reply;
		}
		
		logger.error("Received unknown object.");
		return null;
	}

}
