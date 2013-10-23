package org.hive2hive.core.test.flowcontrol;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.BaseRequestMessage;

/**
 * Test message for testing the process step (simply responses the same content)
 */
public class ProcessStepTestMessage extends BaseRequestMessage {

	private static final long serialVersionUID = 6890311798712262910L;
	private PeerAddress receiverAddress;
	private final String senderId;
	private final String testContent;

	public ProcessStepTestMessage(String targetKey, PeerAddress senderAddress, String senderId,
			PeerAddress receiverAddress, String testContent) {
		super(targetKey, senderAddress);
		this.senderId = senderId;
		this.receiverAddress = receiverAddress;
		this.testContent = testContent;
	}

	@Override
	public void run() {
		// create a simple response
		ResponseMessage response = new ResponseMessage(getMessageID(), senderId, receiverAddress, testContent);
		networkManager.send(response);
	}

	@Override
	public AcceptanceReply accept() {
		return AcceptanceReply.OK;
	}
}