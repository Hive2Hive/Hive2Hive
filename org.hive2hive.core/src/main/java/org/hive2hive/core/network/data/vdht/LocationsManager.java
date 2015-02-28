package org.hive2hive.core.network.data.vdht;

import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.DataManager.H2HPutStatus;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for the {@link VersionManager} for the Locations
 * 
 * @author Nico
 *
 */
public class LocationsManager {
	private static final Logger logger = LoggerFactory.getLogger(LocationsManager.class);

	private final KeyPair protectionKeys;
	private final VersionManager<Locations> versionManager;
	private final DataManager dataManager;
	private final String userId;

	public LocationsManager(DataManager dataManager, String userId, KeyPair protectionKeys) {
		this.dataManager = dataManager;
		this.userId = userId;
		this.protectionKeys = protectionKeys;
		versionManager = new VersionManager<Locations>(dataManager, userId, H2HConstants.USER_LOCATIONS);
	}

	public void put(Locations locations) throws PutFailedException {
		versionManager.put(locations, protectionKeys);
	}

	public Locations get() throws GetFailedException {
		return versionManager.get();
	}

	/**
	 * The locations file might have a conflict or is missing during the login. Here we try to repair it.
	 * Only call this if the locations are gone or ongoing version forks are detected. All entries of old
	 * Locations artifacts are deleted.
	 * 
	 * @return the new locations or <code>null</code> if it failed
	 */
	public Locations repairLocations() {
		Parameters removeParams = new Parameters().setContentKey(H2HConstants.USER_LOCATIONS).setLocationKey(userId)
				.setProtectionKeys(protectionKeys);
		logger.info("Start repairing the locations of user {}", userId);
		if (dataManager.remove(removeParams)) {
			logger.debug("Removed old locations of user {}", userId);
		} else {
			logger.warn("Failed to remove the old locations of user {}", userId);
		}

		Locations locations = new Locations(userId);
		locations.generateVersionKey();
		Parameters addParams = new Parameters().setLocationKey(userId).setContentKey(H2HConstants.USER_LOCATIONS)
				.setVersionKey(locations.getVersionKey()).setBasedOnKey(locations.getBasedOnKey())
				.setNetworkContent(locations).setProtectionKeys(protectionKeys).setTTL(locations.getTimeToLive());
		if (dataManager.put(addParams) == H2HPutStatus.OK) {
			logger.debug("Successfully repaired the locations of user {}", userId);
			return locations;
		} else {
			logger.warn("Could not put the repaired locations of user {}", userId);
			return null;
		}
	}
}
