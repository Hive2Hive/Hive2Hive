package org.hive2hive.processes.implementations.common;

import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.listener.IGetListener;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.abstracts.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public abstract class BaseGetProcessStep extends ProcessStep implements IGetListener {

	private String locationKey;
	private String contentKey;

	public BaseGetProcessStep(String locationKey, String contentKey) {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
	}
	
	@Override
	protected void doExecute() throws InvalidProcessStateException {

		// TODO assure node is connected
		// TODO get stuff
		
	}
	
	@Override
	public abstract void handleGetResult(NetworkContent content);
	
	@Override
	protected void doRollback(RollbackReason reason) {
		// ignore, since only a get was done
	}
}
