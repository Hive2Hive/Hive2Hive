package org.hive2hive.processes.implementations.common;

import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.listener.IGetListener;
import org.hive2hive.processes.framework.abstracts.ProcessStep;

public abstract class BaseGetProcessStep extends ProcessStep implements IGetListener {

	protected void get(String locationKey, String contentKey) {
		// TODO assure node is connected
		// TODO get stuff
	}

	@Override
	public abstract void handleGetResult(NetworkContent content);

}
