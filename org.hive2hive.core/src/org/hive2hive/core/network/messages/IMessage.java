package org.hive2hive.core.network.messages;

import java.io.Serializable;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.SendingBehavior;

public interface IMessage extends Serializable, Runnable{
	public String getMessageID();
	public String getTargetKey();
	
	public SendingBehavior getSendingBehavior();
	
	public void increaseSendingCounter();
	public int getSendingCounter();

	public AcceptanceReply accept();
	public void handleSendingFailure(AcceptanceReply reply, NetworkManager aNetworkManager);
}
