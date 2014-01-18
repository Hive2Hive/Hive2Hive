package org.hive2hive.processes.implementations.register;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.processes.framework.abstracts.NetworkManagerContext;

public final class RegisterProcessContext extends NetworkManagerContext implements IProvideLocations, IConsumeLocations {

	public RegisterProcessContext(NetworkManager networkManager) {
		super(networkManager);
	}

	private Locations locations;

	@Override
	public void provideLocations(Locations locations) {
		this.locations = locations;
	}

	@Override
	public Locations consumeLocations() {
		return locations;
	}

}
