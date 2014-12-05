package org.hive2hive.core.processes.login;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.vdht.VersionManager;
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
		VersionManager<Locations> locationsManager;
		try {
			locationsManager = networkManager.getSession().getLocationsManager();
		} catch (NoSessionException ex) {
			throw new ProcessExecutionException(this, ex);
		}

		try {
			Locations locations = locationsManager.get();
			context.provideLocations(locations);
		} catch (GetFailedException ex) {
			throw new ProcessExecutionException(this, ex);
		}
		return null;
	}
}
