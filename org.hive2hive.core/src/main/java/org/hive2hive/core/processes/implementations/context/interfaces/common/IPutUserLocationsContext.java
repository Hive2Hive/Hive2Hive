package org.hive2hive.core.processes.implementations.context.interfaces.common;

import java.security.KeyPair;

import org.hive2hive.core.model.Locations;

public interface IPutUserLocationsContext {

	public Locations consumeUserLocations();

	public KeyPair consumeUserLocationsProtectionKeys();

}
