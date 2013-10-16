package org.hive2hive.core.flowcontrol.abstracts;

import java.util.LinkedList;
import java.util.List;

import org.hive2hive.core.flowcontrol.interfaces.IMessage;

/**
 * This class represents the abstract type of a sequential message and implements the default of it. 
 * @author Christian
 *
 */
public abstract class ASequentialMessage implements IMessage {
	
	private List<IMessage> messages = new LinkedList<IMessage>();
	
	@Override
	public void execute() {

		// execute the messages sequentially
		while (messages.iterator().hasNext()){
			messages.iterator().next().execute();
		}
	}

	@Override
	public boolean addMessage(IMessage message) {
		return messages.add(message);
	}
	
	@Override
	public boolean removeMessage(IMessage message) {
		// TODO check whether this works
		return messages.remove(message);
	}

	@Override
	public IMessage getMessage(int index) {
		return messages.get(index);
	}
	
}
