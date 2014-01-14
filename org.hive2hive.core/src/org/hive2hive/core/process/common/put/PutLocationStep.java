package org.hive2hive.core.process.common.put;

import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.process.ProcessStep;

/**
 * Simple put process step to store user {@link Locations} in the network.
 * 
 * @author Seppi
 */
public class PutLocationStep extends BasePutProcessStep {

	protected final Locations locations;
	protected final KeyPair protectionKeys;

	public PutLocationStep(Locations locations, KeyPair protectionKeys, ProcessStep nextStep) {
		super(nextStep);
		this.locations = locations;
		this.protectionKeys = protectionKeys;
	}

	@Override
	public void start() {
		locations.setBasedOnKey(locations.getVersionKey());
		locations.generateVersionKey();
		put(locations.getUserId(), H2HConstants.USER_LOCATIONS, locations, protectionKeys);
	}

}
