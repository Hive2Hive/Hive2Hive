package org.hive2hive.core.processes.logout;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.vdht.VersionManager;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class RemoveOwnLocationsStep extends ProcessStep<Void> {

	private final NetworkManager networkManager;

	public RemoveOwnLocationsStep(NetworkManager networkManager) throws NoPeerConnectionException {
		this.setName(getClass().getName());
		this.networkManager = networkManager;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		UserProfile userProfile;
		try {
			userProfile = networkManager.getSession().getProfileManager().getUserProfile(getID(), false);
		} catch (GetFailedException | NoSessionException ex) {
			throw new ProcessExecutionException(this, ex);
		}

		VersionManager<Locations> locationsManager;
		try {
			locationsManager = networkManager.getSession().getLocationsManager();
		} catch (NoSessionException ex) {
			throw new ProcessExecutionException(this, ex);
		}

		// TODO remove while(true) construct!!!
		while (true) {
			Locations locations;
			try {
				locations = locationsManager.get();
			} catch (GetFailedException ex) {
				throw new ProcessExecutionException(this, ex);
			}

			// remove peer
			locations.removePeerAddress(networkManager.getConnection().getPeerDHT().peerAddress());

			try {
				locationsManager.put(locations, userProfile.getProtectionKeys());
			} catch (VersionForkAfterPutException ex) {
				continue;
			} catch (PutFailedException ex) {
				throw new ProcessExecutionException(this, ex);
			}

			break;
		}
		
		return null;
	}
}
