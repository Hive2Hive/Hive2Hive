package org.hive2hive.core.process.login;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.OnlinePeer;
import org.hive2hive.core.process.common.GetLocationsStep;
import org.hive2hive.core.process.common.PutProcessStep;

public class AddMyselfToLocationsStep extends PutProcessStep {

	private GetLocationsStep locationsStep;
	private String userId;

	public AddMyselfToLocationsStep(String userId) {
		super(userId, H2HConstants.USER_LOCATIONS, null, null);
		this.userId = userId;
		// TODO set next step
	}

	@Override
	public void start() {
		Locations locations = locationsStep.getLocations();
		OnlinePeer myStatus = new OnlinePeer(getNetworkManager().getPeerAddress(), false);
		if (locations.getMaster() == null) {
			// no master exists --> take role of master
			myStatus.setMaster(true);
		}

		locations.addOnlinePeer(myStatus);
		put(userId, H2HConstants.USER_LOCATIONS, locations);
	}

	public void setPreviousStep(GetLocationsStep locationsStep) {
		this.locationsStep = locationsStep;
	}

}
