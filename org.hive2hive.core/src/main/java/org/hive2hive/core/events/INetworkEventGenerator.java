package org.hive2hive.core.events;

public interface INetworkEventGenerator extends IEventGenerator {

	void addEventListener(INetworkEventListener listener);
	
	void removeEventListener(INetworkEventListener listener);
	
}
