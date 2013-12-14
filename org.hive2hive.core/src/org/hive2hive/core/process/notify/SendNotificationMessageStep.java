package org.hive2hive.core.process.notify;

import java.security.PublicKey;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.messages.BaseDirectMessageProcessStep;

public class SendNotificationMessageStep extends BaseDirectMessageProcessStep {

	private final boolean toOwnUser;

	public SendNotificationMessageStep(BaseDirectMessage message, PublicKey receiverPublicKey,
			ProcessStep nextStep, boolean toOwnUser) {
		super(message, receiverPublicKey, nextStep);
		this.toOwnUser = toOwnUser;
	}

	@Override
	public void handleResponseMessage(ResponseMessage responseMessage) {
		// no response expected
	}

	@Override
	public void onFailure() {
		if (toOwnUser) {
			// flag that cleanup is required (done at the end of all notifications)
			NotifyPeersProcessContext context = (NotifyPeersProcessContext) getProcess().getContext();
			context.setLocationCleanupRequred(true);
		}

		getProcess().setNextStep(nextStep);
	}
}
