package org.hive2hive.core.events.util;

import org.hive2hive.core.events.framework.interfaces.INetworkEventListener;
import org.hive2hive.core.events.framework.interfaces.network.IConnectionEvent;

public class TestNetworkEventListener implements INetworkEventListener {

	public boolean connectionSucceeded;
	public boolean connectionFailed;
	public boolean disconnectionSucceeded;
	public boolean disconnectionFailed;
	
	@Override
	public void onConnectionSuccess(IConnectionEvent event) {
		connectionSucceeded = true;
	}

	@Override
	public void onConnectionFailure(IConnectionEvent event) {
		connectionFailed = true;
	}

	@Override
	public void onDisconnectionSuccess(IConnectionEvent event) {
		disconnectionSucceeded = true;
	}

	@Override
	public void onDisconnectionFailure(IConnectionEvent event) {
		disconnectionFailed = true;
	}

	public void reset() {
		connectionSucceeded = false;
		connectionFailed = false;
		disconnectionSucceeded = false;
		disconnectionFailed = false;
	}
}
