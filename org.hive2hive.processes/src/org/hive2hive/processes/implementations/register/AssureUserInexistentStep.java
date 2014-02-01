package org.hive2hive.processes.implementations.register;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.GetUserLocationsStep;
import org.hive2hive.processes.implementations.context.RegisterProcessContext;

public class AssureUserInexistentStep extends GetUserLocationsStep {

	private final RegisterProcessContext context;
	private final String userId;

	public AssureUserInexistentStep(String userId, RegisterProcessContext context, IDataManager dataManager) {
		super(userId, context, dataManager);
		this.userId = userId;
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		super.doExecute();

		if (context.consumeLocations() != null) {
			cancel(new RollbackReason(this, "Locations already exist."));
		} else {
			context.provideLocations(new Locations(userId));
		}
	}

}
