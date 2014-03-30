package org.hive2hive.core.processes.implementations.share.pkupdate;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.base.BasePutProcessStep;
import org.hive2hive.core.processes.implementations.context.BasePKUpdateContext;

/**
 * Changes the protection key for any data of type {@link NetworkContent}. Use the {@link BasePKUpdateContext}
 * to hand over the required data.
 * 
 * @author Nico
 */
public class ChangeProtectionKeyStep extends BasePutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(ChangeProtectionKeyStep.class);

	private final BasePKUpdateContext context;
	private final IDataManager dataManager;

	public ChangeProtectionKeyStep(BasePKUpdateContext context, IDataManager dataManager) {
		super(dataManager);
		this.context = context;
		this.dataManager = dataManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		boolean success = dataManager.changeProtectionKey(context.getLocationKey(), context.getContentKey(),
				context.getTTL(), context.consumeOldProtectionKeys(), context.consumeNewProtectionKeys());

		if (!success) {
			throw new ProcessExecutionException("Could not change the meta file's protection key");
		}

		logger.debug("Successfully changed the protection key");
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		// do nothing because chaning the protection key failed
	}
}
