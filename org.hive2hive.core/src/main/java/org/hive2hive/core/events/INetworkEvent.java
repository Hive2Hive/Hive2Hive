package org.hive2hive.core.events;

import org.hive2hive.core.api.interfaces.INetworkConfiguration;

public interface INetworkEvent extends IEvent {

	INetworkConfiguration getNetworkConfiguration();
}
