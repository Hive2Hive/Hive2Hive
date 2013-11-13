package org.hive2hive.core.process.upload.newversion;

import java.io.File;
import java.security.KeyPair;
import java.util.ArrayList;

import org.hive2hive.core.process.common.get.GetUserProfileStep;
import org.hive2hive.core.process.upload.PutChunkStep;

public class PutNewVersionChunkStep extends PutChunkStep {

	/**
	 * Constructor for first call
	 * 
	 * @param file
	 */
	public PutNewVersionChunkStep(File file, NewVersionProcessContext context) {
		super(file, 0, new ArrayList<KeyPair>());
		configureStepAfterUpload(context);
	}

	private void configureStepAfterUpload(NewVersionProcessContext context) {
		GetUserProfileStep getUserProfileStep = new GetUserProfileStep(context.getCredentials(),
				new FindFileInUserProfileStep(file));
		context.setUserProfileStep(getUserProfileStep);
		setStepAfterPutting(getUserProfileStep);
	}
}
