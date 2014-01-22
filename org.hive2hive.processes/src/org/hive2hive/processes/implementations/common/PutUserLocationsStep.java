package org.hive2hive.processes.implementations.common;

import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.processes.framework.ProcessUtil;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.base.BasePutProcessStep;

public class PutUserLocationsStep extends BasePutProcessStep {

	private final Locations locations;
	private final KeyPair protectionKeys;

	private boolean isPutCompleted;
	private boolean isPutFailed;

	public PutUserLocationsStep(Locations locations, KeyPair protectionKeys,
			NetworkManager networkManager) {
		super(networkManager);
		this.protectionKeys = protectionKeys;
		this.locations = locations;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {

		locations.setBasedOnKey(locations.getVersionKey());
		locations.generateVersionKey();

		put(locations.getUserId(), H2HConstants.USER_LOCATIONS, locations,
				protectionKeys);

		// wait for PUT to complete
		while (isPutCompleted == false) {
			ProcessUtil.wait(this);
		}
		
		if (isPutFailed) {
			cancel(new RollbackReason(this, "Put failed."));
		}
	}

	@Override
	public void onPutSuccess() {
		isPutCompleted = true;
	}

	@Override
	public void onPutFailure() {
		isPutCompleted = true;
		isPutFailed = true;
	}

	@Override
	public void onRemoveSuccess() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRemoveFailure() {
		// TODO Auto-generated method stub

	}

}
