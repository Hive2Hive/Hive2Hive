package org.hive2hive.core.events.interfaces;


public interface INetworkEventGenerator extends IEventGenerator {

	void addEventListener(INetworkEventListener listener);
	
	void removeEventListener(INetworkEventListener listener);
	
}
