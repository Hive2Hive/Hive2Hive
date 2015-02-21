package org.hive2hive.core.utils.helper;

import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;

import org.hive2hive.core.network.messages.AcceptanceReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Denies all messages; can be useful for some tests
 * 
 * @author Nico, Seppi
 */
public class DenyingMessageReplyHandler implements ObjectDataReply {

	private static final Logger logger = LoggerFactory.getLogger(DenyingMessageReplyHandler.class);

	@Override
	public Object reply(PeerAddress sender, Object request) throws Exception {
		logger.warn("Denying a message. Sender = '{}'.", sender);
		return AcceptanceReply.FAILURE;
	}
}
