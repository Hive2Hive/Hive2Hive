package org.hive2hive.core.processes.logout;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.PublicKeyManager;
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
		VersionManager<Locations> locationsManager;
		PublicKeyManager keyManager;
		try {
			locationsManager = networkManager.getSession().getLocationsManager();
			keyManager = networkManager.getSession().getKeyManager();
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
			locations.removePeerAddress(networkManager.getConnection().getPeer().peerAddress());

			try {
				locationsManager.put(locations, keyManager.getDefaultProtectionKeyPair());
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
