package org.hive2hive.processes.implementations.login;

import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeUserProfile;

public class VerifyUserProfileStep extends ProcessStep {

	private final String userId;
	private final IConsumeUserProfile context;

	public VerifyUserProfileStep(String userId, IConsumeUserProfile context) {
		this.userId = userId;
		this.context = context;
	}
	
	@Override
	protected void doExecute() throws InvalidProcessStateException {

		if (context.consumeUserProfile() == null) {
			cancel(new RollbackReason(this, "User profile not found."));
		}
		if (context.consumeUserProfile().getUserId().equalsIgnoreCase(userId)) {
			cancel(new RollbackReason(this, "User ID does not match."));
		}
	}

}
