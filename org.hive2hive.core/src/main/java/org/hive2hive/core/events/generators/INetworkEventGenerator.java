package org.hive2hive.core.events.generators;

import org.hive2hive.core.events.listeners.INetworkEventListener;

public interface INetworkEventGenerator extends IEventGenerator {

	void addEventListener(INetworkEventListener listener);
	
	void removeEventListener(INetworkEventListener listener);
	
}
