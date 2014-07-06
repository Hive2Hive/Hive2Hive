package org.hive2hive.core.processes.context.interfaces;

import java.security.KeyPair;

import org.hive2hive.core.model.Locations;

public interface IPutUserLocationsContext {

	public Locations consumeUserLocations();

	public KeyPair consumeUserLocationsProtectionKeys();

}
