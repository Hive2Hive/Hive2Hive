package org.hive2hive.core.processes.logout;

import java.io.IOException;
import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.common.base.BasePutProcessStep;
import org.hive2hive.core.processes.context.LogoutProcessContext;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class RemoveOwnLocationsStep extends BasePutProcessStep {

	private final LogoutProcessContext context;
	private final NetworkManager networkManager;

	public RemoveOwnLocationsStep(LogoutProcessContext context, NetworkManager networkManager)
			throws NoPeerConnectionException {
		super(networkManager.getDataManager());
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {

		Locations locations = context.consumeUserLocations();

		if (locations == null) {
			throw new ProcessExecutionException("Locations not found.");
		} else {

			// remove peer
			locations.removePeerAddress(networkManager.getConnection().getPeer().getPeerAddress());
			locations.setBasedOnKey(locations.getVersionKey());
			try {
				locations.generateVersionKey();
			} catch (IOException e) {
				throw new ProcessExecutionException("Version key could not be generated.", e);
			}

			// put updated locations
			String userId = context.consumeSession().getCredentials().getUserId();

			KeyPair protectionKeys = null;
			try {
				protectionKeys = context.consumeSession().getProfileManager().getDefaultProtectionKey();
			} catch (GetFailedException e) {
				throw new ProcessExecutionException("Default protection keys could not be loaded.", e);
			}

			try {
				put(userId, H2HConstants.USER_LOCATIONS, locations, protectionKeys);
			} catch (PutFailedException e) {
				throw new ProcessExecutionException(e);
			}
		}
	}
}
