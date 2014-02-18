package org.hive2hive.core.processes.implementations.share.pkupdate;

import java.io.IOException;

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
 * Changes the protection key for any data of type {@link NetworkContent}
 * 
 * @author Nico
 * 
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
		NetworkContent content = context.getContent();
		content.setBasedOnKey(content.getVersionKey());

		try {
			content.generateVersionKey();
		} catch (IOException e) {
			logger.error("Could not generate the newest version key");
			throw new ProcessExecutionException(e);
		}

		// call 'special' put to change the protection key
		boolean success = dataManager.changeProtectionKey(context.getLocationKey(), context.getContentKey(),
				content, context.consumeOldProtectionKeys(), context.consumeNewProtectionKeys());

		putPerformed = true;

		if (!success) {
			throw new ProcessExecutionException("Could not change the meta file's protection key");
		}

		logger.debug("Successfully changed the protection key for '" + content.getClass().getSimpleName()
				+ "'");
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (putPerformed) {
			boolean success = dataManager.remove(context.getLocationKey(), context.getContentKey(), context
					.getContent().getVersionKey(), context.consumeNewProtectionKeys());
			if (success) {
				logger.debug("Successfully removed the meta folder version during rollback");
			} else {
				logger.error("Could not remove the meta folder version during rollback");
			}
		}
	}
}
