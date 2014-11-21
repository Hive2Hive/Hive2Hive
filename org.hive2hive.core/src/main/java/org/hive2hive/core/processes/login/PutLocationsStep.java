package org.hive2hive.core.processes.login;

import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.vdht.VersionManager;
import org.hive2hive.core.processes.context.LoginProcessContext;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

/**
 * @author Seppi
 */
public class PutLocationsStep extends ProcessStep<Void> {

	private final NetworkManager networkManager;
	private final LoginProcessContext context;

	public PutLocationsStep(LoginProcessContext context, NetworkManager networkManager) {
		this.networkManager = networkManager;
		this.setName(getClass().getName());
		this.context = context;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		try {
			VersionManager<Locations> locationsManager = networkManager.getSession().getLocationsManager();
			locationsManager.put(context.consumeLocations(), context.consumeUserLocationsProtectionKeys());
		} catch (PutFailedException | NoSessionException ex) {
			throw new ProcessExecutionException(this, ex);
		}
		return null;
	}

}
