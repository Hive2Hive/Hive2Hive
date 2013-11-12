package org.hive2hive.core.test.network.messages;

import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.request.RoutedRequestMessage;

public class TestRoutedUserMessage extends RoutedRequestMessage {

	private static final long serialVersionUID = -2761794939230686206L;

	public TestRoutedUserMessage(String targetKey) {
		super(targetKey);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AcceptanceReply accept() {
		return AcceptanceReply.OK;
	}

}
