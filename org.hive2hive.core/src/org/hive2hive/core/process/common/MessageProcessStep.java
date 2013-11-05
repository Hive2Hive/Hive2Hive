package org.hive2hive.core.process.common;

import net.tomp2p.futures.FutureRemove;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.BaseRequestMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.hive2hive.core.network.messages.request.callback.ICallBackHandler;
import org.hive2hive.core.process.ProcessStep;

public class MessageProcessStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(MessageProcessStep.class);
	
	private final BaseMessage message;

	public MessageProcessStep(BaseMessage message){
		this.message = message;
	}
	
	@Override
	public void start() {
		send(message);
	}

	@Override
	public void rollBack() {
		// nothing to rollback
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

	@Override
	protected void handleRemovalResult(FutureRemove future) {}

}
