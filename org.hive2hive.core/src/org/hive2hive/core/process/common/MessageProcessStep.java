package org.hive2hive.core.process.common;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FutureRemove;

import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.BaseRequestMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.hive2hive.core.network.messages.request.callback.ICallBackHandler;
import org.hive2hive.core.process.ProcessStep;

public class MessageProcessStep extends ProcessStep{

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rollBack() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleGetResult(FutureGet future) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleRemovalResult(FutureRemove future) {
		// TODO Auto-generated method stub
		
	}
	
	protected void send(BaseMessage message) {
		if (message instanceof IRequestMessage) {
			BaseRequestMessage requestMessage = (BaseRequestMessage) message;
			requestMessage.setCallBackHandler(new ICallBackHandler() {
				@Override
				public void handleReturnMessage(ResponseMessage asyncReturnMessage) {
					
				}
			});
		}
		getNetworkManager().send(message);
	}


}
