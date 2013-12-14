package org.hive2hive.core.process.common.put;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.process.ProcessStep;

/**
 * Simple put process step to store user {@link Locations} in the network.
 * 
 * @author Seppi
 */
public class PutLocationStep extends BasePutProcessStep {

	protected Locations locations;

	public PutLocationStep(Locations locations, ProcessStep nextStep) {
		super(nextStep);
		this.locations = locations;
	}

	@Override
	public void start() {
		put(locations.getUserId(), H2HConstants.USER_LOCATIONS, locations);
	}

}
