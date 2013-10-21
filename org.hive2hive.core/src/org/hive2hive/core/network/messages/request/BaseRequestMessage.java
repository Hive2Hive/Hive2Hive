package org.hive2hive.core.network.messages.request;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.request.callback.ICallBackHandler;

/**
 * Prototype of an abstract message which will create a reply.
 * 
 * @author Nendor
 * 
 */
public abstract class BaseRequestMessage extends BaseMessage implements
		IRequestMessage {

	private static final long serialVersionUID = 4510609215735076075L;

	private PeerAddress senderAddress;
	private ICallBackHandler callBackHandler;

	public BaseRequestMessage(String aTargetKey, PeerAddress aSenderAddress) {
		super(createMessageID(), aTargetKey);
		senderAddress = aSenderAddress;
	}

	public ICallBackHandler getCallBackHandler() {
		return callBackHandler;
	}

	public void setCallBackHandler(ICallBackHandler aHandler) {
		callBackHandler = aHandler;
	}

	public PeerAddress getSenderAddress() {
		return senderAddress;
	}

	@Override
	public void setSenderAddress(PeerAddress aSenderAddress) {
		senderAddress = aSenderAddress;
	}

}
