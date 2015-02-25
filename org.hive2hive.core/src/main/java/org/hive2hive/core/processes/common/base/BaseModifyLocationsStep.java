package org.hive2hive.core.processes.common.base;

import java.util.Random;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.vdht.VersionManager;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.exceptions.ProcessRollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Can be extended by process steps that want to modify the {@link Locations}.
 * 
 * @author Nico
 *
 */
public abstract class BaseModifyLocationsStep extends ProcessStep<Void> {

	private static final Logger logger = LoggerFactory.getLogger(BaseModifyLocationsStep.class);
	private static final int FORK_LIMIT = 2;

	private final VersionManager<Locations> locationsManager;
	private final PublicKeyManager keyManager;

	public BaseModifyLocationsStep(VersionManager<Locations> locationsManager, PublicKeyManager keyManager) {
		this.locationsManager = locationsManager;
		this.keyManager = keyManager;
	}

	/**
	 * Modify the Locations here
	 */
	protected abstract void modify(Locations locations);

	@Override
	protected final Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		Locations locations;
		try {
			locations = locationsManager.get();
		} catch (GetFailedException ex) {
			throw new ProcessExecutionException(this, ex);
		}

		modify(locations);

		try {
			putLocations(locations);
		} catch (PutFailedException e) {
			throw new ProcessExecutionException(this, e);
		}

		return null;
	}

	private void putLocations(Locations locations) throws PutFailedException {
		boolean retryPut = true;
		int forkCounter = 0;
		int forkWaitTime = new Random().nextInt(1000) + 500;
		while (retryPut) {
			try {
				locationsManager.put(locations, keyManager.getDefaultProtectionKeyPair());
				setRequiresRollback(true);
			} catch (VersionForkAfterPutException ex) {
				if (forkCounter++ > FORK_LIMIT) {
					logger.warn("Ignoring fork after {} rejects and retries.", forkCounter);
					retryPut = false;
				} else {
					logger.warn("Version fork after put detected. Rejecting and retrying put.");

					// exponential back off waiting and retry to update the user profile
					try {
						Thread.sleep(forkWaitTime);
					} catch (InterruptedException e1) {
						// ignore
					}
					forkWaitTime = forkWaitTime * 2;
				}
			}

			break;
		}
	}

	/**
	 * Un-modify the Locations
	 */
	protected abstract void rollback(Locations locations);

	@Override
	protected final Void doRollback() throws InvalidProcessStateException, ProcessRollbackException {
		Locations locations;
		try {
			locations = locationsManager.get();
		} catch (GetFailedException ex) {
			throw new ProcessRollbackException(this, ex);
		}

		rollback(locations);

		try {
			putLocations(locations);
		} catch (PutFailedException e) {
			throw new ProcessRollbackException(this, e);
		}

		return null;
	}
}
