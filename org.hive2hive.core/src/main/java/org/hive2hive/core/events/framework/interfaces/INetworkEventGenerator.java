package org.hive2hive.core.events.framework.interfaces;

import org.hive2hive.core.events.framework.IEventGenerator;


public interface INetworkEventGenerator extends IEventGenerator {

	void addEventListener(INetworkEventListener listener);
	
	void removeEventListener(INetworkEventListener listener);
	
}
