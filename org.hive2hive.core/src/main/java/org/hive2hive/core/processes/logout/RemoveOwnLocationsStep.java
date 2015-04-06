package org.hive2hive.core.processes.logout;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.common.base.BaseModifyLocationsStep;
import org.hive2hive.core.processes.context.interfaces.LogoutProcessContext;

public class RemoveOwnLocationsStep extends BaseModifyLocationsStep {

	private final NetworkManager networkManager;
	private final LogoutProcessContext context;

	public RemoveOwnLocationsStep(NetworkManager networkManager, LogoutProcessContext context)
			throws NoPeerConnectionException, NoSessionException {
		super(networkManager.getSession().getLocationsManager());
		this.setName(getClass().getName());
		this.networkManager = networkManager;
		this.context = context;
	}

	@Override
	protected void modify(Locations locations) {
		// remove peer address
		locations.removePeerAddress(networkManager.getConnection().getPeer().peerAddress());
		// prepare to send the notifications
		context.provideNotificationRecipients(locations.getPeerAddresses());
	}

	@Override
	protected void rollback(Locations locations) {
		// add peer address
		locations.addPeerAddress(networkManager.getConnection().getPeer().peerAddress());
	}
}
