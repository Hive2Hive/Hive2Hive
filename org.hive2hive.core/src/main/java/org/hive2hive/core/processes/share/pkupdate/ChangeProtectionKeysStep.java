package org.hive2hive.core.processes.share.pkupdate;

import org.hive2hive.core.model.NetworkContent;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.context.BasePKUpdateContext;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Changes the protection key for any data of type {@link NetworkContent}. Use the {@link BasePKUpdateContext}
 * to hand over the required data.
 * 
 * @author Nico, Seppi
 */
public class ChangeProtectionKeysStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(ChangeProtectionKeysStep.class);

	private final BasePKUpdateContext context;
	private final IDataManager dataManager;
	private IParameters parameters;
	private boolean changePerformed = false;

	public ChangeProtectionKeysStep(BasePKUpdateContext context, IDataManager dataManager) {
		this.context = context;
		this.dataManager = dataManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		parameters = new Parameters().setLocationKey(context.getLocationKey()).setContentKey(context.getContentKey())
				.setVersionKey(context.getVersionKey()).setProtectionKeys(context.consumeOldProtectionKeys())
				.setNewProtectionKeys(context.consumeNewProtectionKeys()).setTTL(context.getTTL())
				.setHash(context.getHash());

		boolean success = dataManager.changeProtectionKey(parameters);
		changePerformed = true;
		if (!success) {
			throw new ProcessExecutionException(String.format("Could not change content protection keys. %s",
					parameters.toString()));
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (!changePerformed) {
			return;
		}

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
		} else {
			logger.warn("Rollback of change protection key failed. Remove failed. '{}'", parameters.toString());
		}
	}
}
