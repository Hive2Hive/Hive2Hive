package org.hive2hive.core.api.managers;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IManager;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;

/**
 * Abstract base class for all managers of the Hive2Hive project.
 * 
 * @author Christian
 * 
 */
public abstract class H2HManager implements IManager {

	protected final NetworkManager networkManager;

	private boolean isAutostart = H2HConstants.DEFAULT_AUTOSTART_PROCESSES;

	protected H2HManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	protected void submitProcess(IProcessComponent processComponent) {
		if (isAutostart)
			try {
				processComponent.start();
			} catch (InvalidProcessStateException e) {
				// should not happen
				e.printStackTrace();
			}
	}

	public NetworkManager getNetworkManager() {
		return networkManager;
	}

	@Override
	public void configureAutostart(boolean autostart) {
		this.isAutostart = autostart;
	}

	@Override
	public boolean isAutostart() {
		return isAutostart;
	}
}