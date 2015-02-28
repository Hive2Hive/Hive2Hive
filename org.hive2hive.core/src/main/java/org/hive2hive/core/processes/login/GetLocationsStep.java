package org.hive2hive.core.processes.login;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.vdht.LocationsManager;
import org.hive2hive.core.processes.context.LoginProcessContext;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class GetLocationsStep extends ProcessStep<Void> {

	private final LoginProcessContext context;
	private final NetworkManager networkManager;

	public GetLocationsStep(LoginProcessContext context, NetworkManager networkManager) {
		this.setName(getClass().getName());
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		LocationsManager locationsManager;
		try {
			locationsManager = networkManager.getSession().getLocationsManager();
		} catch (NoSessionException ex) {
			throw new ProcessExecutionException(this, ex);
		}

		try {
			context.provideLocations(locationsManager.get());
		} catch (GetFailedException ex) {
			Locations locations = locationsManager.repairLocations();
			if (locations == null) {
				// even repairing failed
				throw new ProcessExecutionException(this, ex);
			} else {
				// repairing was successful
				context.provideLocations(locations);
			}
		}
		return null;
	}
}
