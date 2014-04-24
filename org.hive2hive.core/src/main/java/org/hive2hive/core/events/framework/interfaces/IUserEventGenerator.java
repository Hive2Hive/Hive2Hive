package org.hive2hive.core.events.framework.interfaces;

import org.hive2hive.core.events.framework.IEventGenerator;

public interface IUserEventGenerator extends IEventGenerator {
	
	void addEventListener(IUserEventListener listener);
	
	void removeEventListener(IUserEventListener listener);
	
}
