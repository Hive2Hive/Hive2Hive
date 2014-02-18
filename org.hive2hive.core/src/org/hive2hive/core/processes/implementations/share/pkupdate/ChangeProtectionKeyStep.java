package org.hive2hive.core.processes.implementations.share.pkupdate;

import net.tomp2p.peers.Number160;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.DataManager;
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
	private final DataManager dataManager;

	public ChangeProtectionKeyStep(BasePKUpdateContext context, DataManager dataManager) {
		super(dataManager);
		this.context = context;
		this.dataManager = dataManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		// call 'special' put to change the protection key
		dataManager.put(Number160.createHash(context.getLocationKey()), H2HConstants.TOMP2P_DEFAULT_KEY,
				Number160.createHash(context.getContentKey()), context.getContent(),
				context.consumeOldProtectionKeys(), context.consumeNewProtectionKeys());
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
