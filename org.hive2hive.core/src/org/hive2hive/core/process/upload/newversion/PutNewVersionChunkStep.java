package org.hive2hive.core.process.upload.newversion;

import java.io.File;
import java.security.KeyPair;
import java.util.ArrayList;

import org.hive2hive.core.process.common.get.GetUserProfileStep;
import org.hive2hive.core.process.upload.PutFileChunkStep;

public class PutNewVersionChunkStep extends PutFileChunkStep {

	/**
	 * Constructor for first call
	 * 
	 * @param file
	 */
	public PutNewVersionChunkStep(File file) {
		super(file, 0, new ArrayList<KeyPair>());
	}

	@Override
	public void start() {
		// upload all chunks
		super.start();

		NewVersionProcessContext context = (NewVersionProcessContext) getProcess().getContext();
		GetUserProfileStep getUserProfileStep = new GetUserProfileStep(context.getCredentials(),
				new FindFileInUserProfileStep(file));
		context.setUserProfileStep(getUserProfileStep);
		getProcess().setNextStep(getUserProfileStep);
	}
}
