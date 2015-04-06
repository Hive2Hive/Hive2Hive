package org.hive2hive.core.utils.helper;

import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.tomp2p.message.Buffer;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.RawDataReply;

import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.serializer.FSTSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Denies all messages; can be useful for some tests
 * 
 * @author Nico, Seppi
 */
public class DenyingMessageReplyHandler implements RawDataReply {

	private static final Logger logger = LoggerFactory.getLogger(DenyingMessageReplyHandler.class);
	private final byte[] serializedReply;

	public DenyingMessageReplyHandler() throws IOException {
		serializedReply = new FSTSerializer().serialize(AcceptanceReply.FAILURE);
	}

	@Override
	public Buffer reply(PeerAddress sender, Buffer requestBuffer, boolean complete) throws Exception {
		logger.warn("Denying a message. Sender = '{}'.", sender);
		return new Buffer(Unpooled.wrappedBuffer(serializedReply));
	}
}
