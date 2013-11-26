package org.hive2hive.core.process.login;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.process.ProcessStep;

@Deprecated
public class HandleUserMessageQueueStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(HandleUserMessageQueueStep.class);
	
	private final String userId;
	
	public HandleUserMessageQueueStep(String userId) {
		this.userId = userId;
	}
	
	@Override
	public void start() {

//		final PostLoginProcessContext context = ((PostLoginProcess) getProcess()).getContext();
//		final UserMessageQueue umQueue = context.getGetUserMessageQueueStep().getUserMessageQueue();		
//		
//		// check whether this client is master and allowed to execute this step
//		if (!context.getIsDefinedAsMaster()) {
//			logger.error("This client has not been defined as master client. The UserMessageQueue will not be handled and this process terminated.");
//			getProcess().terminate();
//		}
//		
//		// check whether correct user message queue has been loaded
//		if (!umQueue.getUserId().equals(userId)){
//			logger.error("A UserMessageQueue of the wrong userId was loaded. The UserMessageQueue will not be handled and this process stopped.");
//			getProcess().stop("A UserMessageQueue of the wrong userId was loaded.");
//		}
		
		// handle the messages
//		Queue<BaseMessage> queue = umQueue.getMessageQueue();
//		while (!queue.isEmpty()){
//			BaseMessage message = queue.poll();
//			
//			// TODO handle the UserMessages here
//			
//		}
		
		// terminate the process
		getProcess().terminate();
	}

	@Override
	public void rollBack() {
		getProcess().nextRollBackStep();
	}
}
