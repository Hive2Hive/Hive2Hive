package org.hive2hive.core.api;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.INetworkComponent;
import org.hive2hive.core.exceptions.NoNetworkException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;

/**
 * Abstract base class for all managers of the Hive2Hive project.
 * 
 * @author Christian
 * 
 */
public abstract class H2HManager implements INetworkComponent {

	private NetworkManager networkManager;
	private boolean isAutostart = H2HConstants.DEFAULT_AUTOSTART_PROCESSES;

	protected void submitProcess(IProcessComponent processComponent) {
		if (isAutostart)
			try {
				processComponent.start();
			} catch (InvalidProcessStateException e) {
				// should not happen
				e.printStackTrace();
			}
	}

	@Override
	public void setNetworkManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	protected NetworkManager getNetworkManager() throws NoNetworkException {
		if (networkManager == null)
			throw new NoNetworkException();
		return networkManager;
	}

	/**
	 * Configures whether processes by this component get started automatically or not.
	 * 
	 * @param autostart
	 */
	public void configureAutostart(boolean autostart) {
		this.isAutostart = autostart;
	}

	public boolean isAutostart() {
		return isAutostart;
	}
}