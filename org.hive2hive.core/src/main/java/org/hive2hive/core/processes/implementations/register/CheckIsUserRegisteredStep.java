package org.hive2hive.core.processes.implementations.register;

import org.hive2hive.core.exceptions.UserAlreadyRegisteredException;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.GetUserLocationsStep;
import org.hive2hive.core.processes.implementations.context.RegisterProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckIsUserRegisteredStep extends GetUserLocationsStep {

	private static final Logger logger = LoggerFactory.getLogger(CheckIsUserRegisteredStep.class);

	private final RegisterProcessContext context;

	public CheckIsUserRegisteredStep(RegisterProcessContext context, IDataManager dataManager) {
		super(context, dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		String userId = context.consumeUserId();
		logger.trace("Checking if user is already registerd. user id ='{}'", userId);
		super.doExecute();
		if (context.consumeUserLocations() != null) {
			throw new ProcessExecutionException(new UserAlreadyRegisteredException(userId));
		}
	}

}
