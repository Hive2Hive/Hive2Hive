package org.hive2hive.core.processes.register;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.UserAlreadyRegisteredException;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.processes.common.base.BaseGetProcessStep;
import org.hive2hive.core.processes.context.RegisterProcessContext;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Seppi
 */
public class CheckIsUserRegisteredStep extends BaseGetProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(CheckIsUserRegisteredStep.class);

	private final RegisterProcessContext context;

	public CheckIsUserRegisteredStep(RegisterProcessContext context, DataManager dataManager) {
		super(dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		String userId = context.consumeUserId();
		logger.trace("Checking if user is already registerd. user id ='{}'", userId);
		if (get(context.consumeUserId(), H2HConstants.USER_LOCATIONS) != null) {
			throw new ProcessExecutionException(new UserAlreadyRegisteredException(userId));
		}
	}

}
