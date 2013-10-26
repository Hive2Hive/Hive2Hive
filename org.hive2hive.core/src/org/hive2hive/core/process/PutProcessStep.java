package org.hive2hive.core.process;

import org.hive2hive.core.network.data.NetworkData;

public abstract class PutProcessStep extends ProcessStep {

	private final NetworkData oldData;

	protected PutProcessStep(NetworkData oldData) {
		this.oldData = oldData;
	}

	protected void rollBackPut(String locationKey, String contentKey) {
		if (oldData == null) {
			remove(locationKey, contentKey);
		} else {
			put(locationKey, contentKey, oldData);
		}
	}
}
