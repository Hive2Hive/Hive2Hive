package org.hive2hive.core.process.upload;

import java.io.File;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;

public class UploadFileProcess extends Process {

	private final UploadFileProcessContext context;

	public UploadFileProcess(File file, UserProfile userProfile, NetworkManager networkManager) {
		super(networkManager);
		context = new UploadFileProcessContext(this, userProfile, file);

		// TODO userProfile could be made optional --> add a GetUserProfileStep in front
		// TODO upload the file
	}

	@Override
	public UploadFileProcessContext getContext() {
		return context;
	}

}
