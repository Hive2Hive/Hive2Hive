package org.hive2hive.core.processes.logout;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.vdht.VersionManager;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class RemoveOwnLocationsStep extends ProcessStep {

	private final NetworkManager networkManager;

	public RemoveOwnLocationsStep(NetworkManager networkManager) throws NoPeerConnectionException {
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		UserProfile userProfile;
		try {
			userProfile = networkManager.getSession().getProfileManager().getUserProfile(getID(), false);
		} catch (GetFailedException | NoSessionException e) {
			throw new ProcessExecutionException(e);
		}

		VersionManager<Locations> locationsManager;
		try {
			locationsManager = networkManager.getSession().getLocationsManager();
		} catch (NoSessionException e) {
			throw new ProcessExecutionException("No session yet");
		}

		Locations locations;
		try {
			locations = locationsManager.get();
		} catch (GetFailedException e) {
			throw new ProcessExecutionException(e);
		}

		// remove peer
		locations.removePeerAddress(networkManager.getConnection().getPeerDHT().peerAddress());

		try {
			locationsManager.put(locations, userProfile.getProtectionKeys());
		} catch (PutFailedException e) {
			throw new ProcessExecutionException(e);
		}
	}
}
