package org.hive2hive.processes.implementations.common;

import java.security.KeyPair;

import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.listener.IPutListener;
import org.hive2hive.core.network.data.listener.IRemoveListener;
import org.hive2hive.processes.framework.abstracts.ProcessStep;

public abstract class BasePutProcessStep extends ProcessStep implements IPutListener, IRemoveListener {

	protected void put(String locationKey, String contentKey, NetworkContent content, KeyPair protectionKey) {

		// TODO assure node is connected
		// TODO put stuff
	}

	@Override
	public abstract void onPutSuccess();

	@Override
	public abstract void onPutFailure();

	@Override
	public abstract void onRemoveSuccess();

	@Override
	public abstract void onRemoveFailure();

}
