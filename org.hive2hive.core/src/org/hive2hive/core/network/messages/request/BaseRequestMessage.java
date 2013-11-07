package org.hive2hive.core.network.messages.request;

import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;

/**
 * Prototype of an abstract message which will create a reply.
 * 
 * @author Nendor, Seppi
 * 
 */
public abstract class BaseRequestMessage extends BaseMessage implements IRequestMessage {

	private static final long serialVersionUID = 4510609215735076075L;

	private IResponseCallBackHandler callBackHandler;

	public BaseRequestMessage(String targetKey) {
		super(createMessageID(), targetKey);
	}

	public IResponseCallBackHandler getCallBackHandler() {
		return callBackHandler;
	}

	public void setCallBackHandler(IResponseCallBackHandler aHandler) {
		callBackHandler = aHandler;
	}
}