package org.hive2hive.core.process;

import org.hive2hive.core.network.data.NetworkContent;

public abstract class PutProcessStep extends ProcessStep {

	private final NetworkContent oldData;

	protected PutProcessStep(NetworkContent oldData) {
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
