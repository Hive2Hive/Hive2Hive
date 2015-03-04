package org.hive2hive.core.processes.share.pkupdate;

import org.hive2hive.core.model.BaseNetworkContent;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.context.BasePKUpdateContext;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.exceptions.ProcessRollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Changes the protection key for any data of type {@link BaseNetworkContent}. Use the
 * {@link BasePKUpdateContext} to hand over the required data.
 * 
 * @author Nico, Seppi
 */
public class ChangeProtectionKeysStep extends ProcessStep<Void> {

	private static final Logger logger = LoggerFactory.getLogger(ChangeProtectionKeysStep.class);

	private final BasePKUpdateContext context;
	private final DataManager dataManager;
	private IParameters parameters;

	public ChangeProtectionKeysStep(BasePKUpdateContext context, DataManager dataManager) {
		this.setName(getClass().getName());
		this.context = context;
		this.dataManager = dataManager;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		parameters = new Parameters().setLocationKey(context.getLocationKey()).setContentKey(context.getContentKey())
				.setVersionKey(context.getVersionKey()).setProtectionKeys(context.consumeOldProtectionKeys())
				.setNewProtectionKeys(context.consumeNewProtectionKeys()).setTTL(context.getTTL())
				.setHash(context.getHash());

		boolean success = dataManager.changeProtectionKey(parameters);
		setRequiresRollback(success);
		if (!success) {
			throw new ProcessExecutionException(this, String.format(
					"Could not change content protection keys. Parameters: %s.", parameters.toString()));
		}

		logger.debug("Successfully changed the protection keys for {}", parameters);
		return null;
	}

	@Override
	protected Void doRollback() throws InvalidProcessStateException, ProcessRollbackException {

		logger.debug("Rollbacking change of content protection key. '{}'", parameters.toString());

		Parameters rollbackParameters = new Parameters().setLocationKey(parameters.getLocationKey())
				.setContentKey(parameters.getContentKey()).setVersionKey(parameters.getVersionKey())
				.setTTL(parameters.getTTL()).setHash(parameters.getHash());
		// switch the content protection keys
		rollbackParameters.setProtectionKeys(parameters.getNewProtectionKeys()).setNewProtectionKeys(
				parameters.getProtectionKeys());

		boolean success = dataManager.changeProtectionKey(rollbackParameters);
		if (success) {
			logger.debug("Rollback of change protection key succeeded. '{}'", parameters.toString());
			setRequiresRollback(false);
		} else {
			throw new ProcessRollbackException(this, String.format(
					"Rollback of change protection key failed. Remove failed. Parameters; '%s'", parameters.toString()));
		}

		return null;
	}
}
