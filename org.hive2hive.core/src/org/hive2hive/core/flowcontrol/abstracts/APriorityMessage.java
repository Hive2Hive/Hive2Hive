package org.hive2hive.core.flowcontrol.abstracts;

import org.hive2hive.core.flowcontrol.interfaces.IMessage;

/**
 * This class represents the abstract type of a prioritized message and implements the default of it. 
 * @author Christian
 *
 */
public abstract class APriorityMessage implements IMessage {

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean addMessage(IMessage message) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean removeMessage(IMessage message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IMessage getMessage(int index) {
		// TODO Auto-generated method stub
		return null;
	}
}
