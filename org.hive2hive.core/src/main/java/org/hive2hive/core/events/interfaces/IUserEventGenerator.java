package org.hive2hive.core.events.interfaces;

public interface IUserEventGenerator extends IEventGenerator {
	
	void addEventListener(IUserEventListener listener);
	
	void removeEventListener(IUserEventListener listener);
	
}
