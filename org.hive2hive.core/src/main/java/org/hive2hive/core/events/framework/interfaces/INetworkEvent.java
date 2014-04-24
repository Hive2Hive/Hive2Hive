package org.hive2hive.core.events.framework.interfaces;

import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.events.framework.IEvent;

public interface INetworkEvent extends IEvent {

	INetworkConfiguration getNetworkConfiguration();
}
