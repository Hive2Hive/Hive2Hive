package org.hive2hive.core.process.common;

import net.tomp2p.futures.FutureDHT;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.PutProcessStep;

public class PutLocationStep extends PutProcessStep {

	private final String userId;
	private final Locations locations;
	private final ProcessStep nextStep;

	public PutLocationStep(String userId, Locations locations, Locations previousVersion, ProcessStep nextStep) {
		super(previousVersion);
		this.userId = userId;
		this.locations = locations;
		this.nextStep = nextStep;
	}

	@Override
	public void start() {
		put(userId, H2HConstants.USER_LOCATIONS, locations);
	}

	@Override
	public void rollBack() {
		super.rollBackPut(userId, H2HConstants.USER_LOCATIONS);
	}

	@Override
	protected void handleMessageReply(ResponseMessage asyncReturnMessage) {
		// no messages
	}

	@Override
	protected void handlePutResult(FutureDHT future) {
		if (future.isSuccess()) {
			getProcess().nextStep(nextStep);
		} else {
			getProcess().rollBack("Could not put the Locations");
		}
	}

	@Override
	protected void handleGetResult(FutureDHT future) {
		// nothing is get
	}

	@Override
	protected void handleRemovalResult(FutureDHT future) {
		// nothing to remove
	}
}
