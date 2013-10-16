package org.hive2hive.core.flowcontrol.interfaces;

/**
 * This interface provides the standard methods for the composition of messages which allows message nesting.
 * @author Christian
 *
 */
public interface IMessage {

	public void execute();
	
	public boolean addMessage(IMessage message);
	
	public boolean removeMessage(IMessage message);
	
	public IMessage getMessage(int index);
}
