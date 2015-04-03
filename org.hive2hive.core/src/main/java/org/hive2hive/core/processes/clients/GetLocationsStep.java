package org.hive2hive.core.processes.clients;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.data.vdht.LocationsManager;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class GetLocationsStep extends ProcessStep<Set<PeerAddress>> {

	private final LocationsManager locationsManager;

	public GetLocationsStep(LocationsManager locationsManager) {
		this.locationsManager = locationsManager;
		this.setName(getClass().getName());
	}

	@Override
	protected Set<PeerAddress> doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		Set<PeerAddress> clients = new HashSet<PeerAddress>();
		try {
			Locations locations = locationsManager.get();
			if (locations == null) {
				throw new ProcessExecutionException(this, "Locations do not exist");
			} else {
				clients = Collections.unmodifiableSet(locations.getPeerAddresses());
			}
		} catch (GetFailedException ex) {
			throw new ProcessExecutionException(this, ex);
		}

		return clients;
	}
}
