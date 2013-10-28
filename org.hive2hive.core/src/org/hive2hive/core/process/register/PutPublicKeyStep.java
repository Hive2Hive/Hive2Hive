package org.hive2hive.core.process.register;

import net.tomp2p.futures.FutureDHT;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.PutProcessStep;
import org.hive2hive.core.process.common.PutLocationStep;
import org.hive2hive.core.process.common.PutUserProfileStep;

/**
 * Puts the user's public key to the network (which is used for encryption of messages and other
 * communication)
 * 
 * @author Nico
 * 
 */
public class PutPublicKeyStep extends PutProcessStep {

	private UserProfile userProfile;

	protected PutPublicKeyStep(UserProfile userProfile) {
		super(null);
		this.userProfile = userProfile;
	}

	@Override
	public void start() {
		put(userProfile.getUserId(), H2HConstants.USER_PUBLIC_KEY, new UserPublicKey(userProfile
				.getEncryptionKeys().getPublic()));
	}

	@Override
	public void rollBack() {
		super.rollBackPut(userProfile.getUserId(), H2HConstants.USER_PUBLIC_KEY);
	}

	@Override
	protected void handleMessageReply(ResponseMessage asyncReturnMessage) {
		// not used
	}

	@Override
	protected void handlePutResult(FutureDHT future) {
		if (future.isSuccess()) {
			continueWithNextStep();
		} else {
			rollBack();
		}
	}

	private void continueWithNextStep() {
		RegisterProcess process = (RegisterProcess) super.getProcess();

		// create the next steps:
		// first, put the new user profile
		// second, put the empty locations map
		PutLocationStep second = new PutLocationStep(new Locations(userProfile.getUserId()), null, null);
		PutUserProfileStep first = new PutUserProfileStep(userProfile, null, process.getUserPassword(),
				second);
		getProcess().nextStep(first);
	}

	@Override
	protected void handleGetResult(FutureDHT future) {
		// not used
	}

	@Override
	protected void handleRemovalResult(FutureDHT future) {
		// not used
	}

}
