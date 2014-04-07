package org.hive2hive.core.processes.implementations.share.pkupdate;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.BasePKUpdateContext;

/**
 * Changes the protection key for any data of type {@link NetworkContent}. Use the {@link BasePKUpdateContext}
 * to hand over the required data.
 * 
 * @author Nico, Seppi
 */
public class ChangeProtectionKeysStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(ChangeProtectionKeysStep.class);

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
		parameters = new Parameters().setLocationKey(context.getLocationKey())
				.setContentKey(context.getContentKey()).setVersionKey(context.getVersionKey())
				.setProtectionKeys(context.consumeOldProtectionKeys())
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
		if (!changePerformed)
			return;

		logger.debug(String.format("Rollbacking change of content protection key. %s", parameters.toString()));

		Parameters rollbackParameters = new Parameters().setLocationKey(parameters.getLocationKey())
				.setContentKey(parameters.getContentKey()).setVersionKey(parameters.getVersionKey())
				.setTTL(parameters.getTTL()).setHash(parameters.getHash());
		// switch the content protection keys
		rollbackParameters.setProtectionKeys(parameters.getNewProtectionKeys()).setNewProtectionKeys(
				parameters.getProtectionKeys());

		boolean success = dataManager.changeProtectionKey(rollbackParameters);
		if (success) {
			logger.debug(String.format("Rollback of change protection key succeeded. %s",
					parameters.toString()));
		} else {
			logger.warn(String.format("Rollback of change protection key failed. Remove failed. %s",
					parameters.toString()));
		}
	}
}
