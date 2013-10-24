package org.hive2hive.core.process.common;

import net.tomp2p.futures.FutureDHT;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.ProcessStep;

public class PutUserProfileStep extends ProcessStep {

	private final UserProfile profile;
	private final ProcessStep next;

	public PutUserProfileStep(UserProfile profile, ProcessStep next) {
		this.profile = profile;
		this.next = next;
	}

	@Override
	public void start() {
		// TODO encrypt and put
	}

	@Override
	public void rollBack() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleMessageReply(ResponseMessage asyncReturnMessage) {
		// does not send any message
	}

	@Override
	protected void handlePutResult(FutureDHT future) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleGetResult(FutureDHT future) {
		// TODO Auto-generated method stub

	}

}
