package org.hive2hive.core.process.common.put;

import java.io.IOException;
import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
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
	private final ProcessStep nextStep;

	public PutLocationStep(Locations locations, KeyPair protectionKeys, ProcessStep nextStep) {
		this.locations = locations;
		this.protectionKeys = protectionKeys;
		this.nextStep = nextStep;
	}

	@Override
	public void start() {
		locations.setBasedOnKey(locations.getVersionKey());
		try {
			locations.generateVersionKey();
		} catch (IOException e) {
			getProcess().stop(e);
		}
		try {
			put(locations.getUserId(), H2HConstants.USER_LOCATIONS, locations, protectionKeys);
			getProcess().setNextStep(nextStep);
		} catch (PutFailedException e) {
			getProcess().stop(e);
		}
	}

}
