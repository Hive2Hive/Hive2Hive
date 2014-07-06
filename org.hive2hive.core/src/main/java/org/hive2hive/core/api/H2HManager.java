package org.hive2hive.core.api;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IManager;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all managers of the Hive2Hive project.
 * 
 * @author Christian
 * 
 */
public abstract class H2HManager implements IManager {

	private static final Logger logger = LoggerFactory.getLogger(H2HManager.class);

	protected final NetworkManager networkManager;

	private boolean isAutostart = H2HConstants.DEFAULT_AUTOSTART_PROCESSES;

	protected H2HManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	protected void submitProcess(IProcessComponent processComponent) {
		if (isAutostart) {
			executeProcess(processComponent);
		}
	}

	protected void executeProcess(IProcessComponent processComponent) {
		try {
			processComponent.start();
		} catch (InvalidProcessStateException e) {
			// should not happen
			logger.error("Cannot execute the process", e);
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