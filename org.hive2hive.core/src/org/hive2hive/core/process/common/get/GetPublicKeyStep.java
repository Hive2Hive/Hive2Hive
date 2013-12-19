package org.hive2hive.core.process.common.get;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.context.IGetPublicKeyContext;

/**
 * Gets the public key of a given user
 * 
 * @author Nico
 * 
 */
// TODO never used yet
public class GetPublicKeyStep extends BaseGetProcessStep {

	private final String userId;
	private final IGetPublicKeyContext context;
	private final ProcessStep nextStep;

	public GetPublicKeyStep(String userId, IGetPublicKeyContext context, ProcessStep nextStep) {
		this.userId = userId;
		this.context = context;
		this.nextStep = nextStep;
	}

	@Override
	public void start() {
		get(userId, H2HConstants.USER_PUBLIC_KEY);
	}

	@Override
	public void handleGetResult(NetworkContent content) {
		if (content == null) {
			context.setPublicKey(null);
			context.setPublicKey(null);
		} else {
			UserPublicKey key = (UserPublicKey) content;
			context.setPublicKey(key.getPublicKey());
		}

		getProcess().setNextStep(nextStep);
	}

}
