package org.hive2hive.core.processes.implementations.register;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.GetUserLocationsStep;
import org.hive2hive.core.processes.implementations.context.RegisterProcessContext;

public class AssureUserInexistentStep extends GetUserLocationsStep {

	private final RegisterProcessContext context;
	private final String userId;

	public AssureUserInexistentStep(String userId, RegisterProcessContext context, IDataManager dataManager) {
		super(userId, context, dataManager);
		this.userId = userId;
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		super.doExecute();

		if (context.consumeLocations() != null) {
			throw new ProcessExecutionException("Locations already exist.");
		} else {
			context.provideLocations(new Locations(userId));
		}
	}

}
