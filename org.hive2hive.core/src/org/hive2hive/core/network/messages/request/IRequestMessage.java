package org.hive2hive.core.network.messages.request;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.request.callback.ICallBackHandler;

public interface IRequestMessage {

	public ICallBackHandler getCallBackHandler();

	void setCallBackHandler(ICallBackHandler aCallBackHandler);

	public PeerAddress getSenderAddress();

	public void setSenderAddress(PeerAddress aSenderAddress);
}
