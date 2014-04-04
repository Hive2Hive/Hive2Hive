package org.hive2hive.core.test.processes.util;

import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.AcceptanceReply;

/**
 * Denies all messages; can be useful for some tests
 * 
 * @author Nico, Seppi
 */
public class DenyingMessageReplyHandler implements ObjectDataReply {
	
	private static final H2HLogger logger = H2HLoggerFactory.getLogger(DenyingMessageReplyHandler.class);
	
	@Override
	public Object reply(PeerAddress sender, Object request) throws Exception {
		logger.warn(String.format("Denying a message. sender = '%s'", sender));
		return AcceptanceReply.FAILURE;
	}
}
