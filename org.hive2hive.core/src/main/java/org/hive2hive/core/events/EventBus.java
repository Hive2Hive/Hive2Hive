package org.hive2hive.core.events;

import org.hive2hive.core.events.framework.IEvent;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;

public class EventBus extends MBassador<IEvent> {
	
	public EventBus() {
		super(createBusConfiguration());
	}
	
	private static BusConfiguration createBusConfiguration() {
		BusConfiguration config = new BusConfiguration();
		// synchronous dispatching of events
		config.addFeature(Feature.SyncPubSub.Default());
		// asynchronous dispatching of events
		config.addFeature(Feature.AsynchronousHandlerInvocation.Default());
		config.addFeature(Feature.AsynchronousMessageDispatch.Default());
		return config;
	}

}
