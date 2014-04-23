package org.hive2hive.core.events.util;

import org.hive2hive.core.events.INetworkEvent;
import org.hive2hive.core.events.INetworkEventListener;

public class TestNetworkEventListener implements INetworkEventListener {

	public boolean connectionSucceeded;
	public boolean connectionFailed;
	public boolean disconnectionSucceeded;
	public boolean disconnectionFailed;
	
	@Override
	public void onConnectionSuccess(INetworkEvent event) {
		connectionSucceeded = true;
	}

	@Override
	public void onConnectionFailure(INetworkEvent event) {
		connectionFailed = true;
	}

	@Override
	public void onDisconnectionSuccess(INetworkEvent event) {
		disconnectionSucceeded = true;
	}

	@Override
	public void onDisconnectionFailure(INetworkEvent event) {
		disconnectionFailed = true;
	}

	public void reset() {
		connectionSucceeded = false;
		connectionFailed = false;
		disconnectionSucceeded = false;
		disconnectionFailed = false;
	}
}
