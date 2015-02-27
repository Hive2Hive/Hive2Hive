package org.hive2hive.core.network.data.vdht;

import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.data.DataManager;

/**
 * Wrapper for the {@link VersionManager} for the Locations
 * 
 * @author Nico
 *
 */
public class LocationsManager {

	private final KeyPair protectionKeys;
	private final VersionManager<Locations> versionManager;

	public LocationsManager(DataManager dataManager, String userId, KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
		versionManager = new VersionManager<Locations>(dataManager, H2HConstants.USER_LOCATIONS, userId);
	}

	public void put(Locations locations) throws PutFailedException {
		versionManager.put(locations, protectionKeys);
	}

	public Locations get() throws GetFailedException {
		return versionManager.get();
	}
}
