package org.hive2hive.core.api;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IManager;
import org.hive2hive.core.events.EventBus;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.processframework.decorators.AsyncComponent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
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
	protected final EventBus eventBus;

	private boolean isAutostart = H2HConstants.DEFAULT_AUTOSTART_PROCESSES;

	protected H2HManager(NetworkManager networkManager, EventBus eventBus) {
		this.networkManager = networkManager;
		this.eventBus = eventBus;
	}

	protected void submitProcess(AsyncComponent<?> asyncComponent) {
		if (isAutostart) {
			executeComponent(asyncComponent);
		}
	}

	/**
	 * Starts the execution of the AsyncComponent. Does not handle the rollback!
	 * 
	 * @param asyncComponent
	 */
	protected void executeComponent(AsyncComponent<?> asyncComponent) {
		// start execution
		try {
			asyncComponent.execute();

		} catch (InvalidProcessStateException ex) {
			// should not happen as only processes that are READY are submitted
			logger.error(ex.getMessage());
		} catch (ProcessExecutionException ex) {
			// AsyncComponent could not be started
			logger.error(String.format("Error occurred during execution of %s.\nReason: %s.", ex.getSource(),
					ex.getMessage()));
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