package org.hive2hive.core.process.login;

import org.hive2hive.core.process.ProcessStep;

/**
 * This step is only important for a master client that has to handle all the buffered user messages.
 * @author Christian
 *
 */
public class HandleUserMessageQueueStep extends ProcessStep {

	public HandleUserMessageQueueStep() {
		
	}
	
	@Override
	public void start() {
		
		// check whether this client is master and allowed to execute this step
		
		
		// terminate PostLoginProcess
		getProcess().terminate();
	}

	@Override
	public void rollBack() {
		// TODO Auto-generated method stub

	}

}
