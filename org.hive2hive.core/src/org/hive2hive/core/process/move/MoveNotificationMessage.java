package org.hive2hive.core.process.move;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;

/**
 * This message is sent after a file has been moved and the receiver had access to the file before and after
 * movement.
 * 
 * @author Nico
 * 
 */
public class MoveNotificationMessage extends BaseDirectMessage {

	private static final long serialVersionUID = 2855700202146422905L;
	private final static Logger logger = H2HLoggerFactory.getLogger(MoveNotificationMessage.class);
	private final PublicKey fileKey;

	public MoveNotificationMessage(PeerAddress targetAddress, PublicKey fileKey) {
		super(targetAddress);
		this.fileKey = fileKey;
	}

	@Override
	public void run() {
		logger.debug("Notification message received");
		move();
	}

	private void move() {
		// TODO
	}

	@Override
	public boolean checkSignature(byte[] data, byte[] signature, String userId) {
		if (!networkManager.getUserId().equals(userId)) {
			logger.error("Signature is not from the same user.");
			return false;
		} else {
			return verify(data, signature, networkManager.getPublicKey());
		}
	}
}
