package org.hive2hive.processes.implementations.logout;

import java.io.IOException;
import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.base.BasePutProcessStep;
import org.hive2hive.processes.implementations.context.LogoutProcessContext;

public class RemoveOwnLocationsStep extends BasePutProcessStep {

	private final LogoutProcessContext context;
	private final NetworkManager networkManager;

	public RemoveOwnLocationsStep(LogoutProcessContext context, NetworkManager networkManager) {
		super(networkManager);
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {

		Locations locations = context.consumeLocations();

		if (locations == null) {
			cancel(new RollbackReason(this, "Locations not found."));
			return;
		} else {

			// remove peer
			locations.removePeerAddress(networkManager.getPeerAddress());
			locations.setBasedOnKey(locations.getVersionKey());
			try {
				locations.generateVersionKey();
			} catch (IOException e) {
				cancel(new RollbackReason(this, "Version key could not be generated."));
				return;
			}

			// put updated locations
			String userId = context.consumeSession().getCredentials().getUserId();

			KeyPair protectionKeys = null;
			try {
				protectionKeys = context.consumeSession().getProfileManager().getDefaultProtectionKey();
			} catch (GetFailedException e) {
				cancel(new RollbackReason(this, "Default protection keys could not be loaded."));
				return;
			}

			try {
				put(userId, H2HConstants.USER_LOCATIONS, locations, protectionKeys);
			} catch (PutFailedException e) {
				cancel(new RollbackReason(this, "Put failed."));
			}
		}
	}
}