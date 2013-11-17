package org.hive2hive.core.process.delete;

import java.io.File;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.UserCredentials;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.File2MetaFileStep;
import org.hive2hive.core.process.common.get.GetUserProfileStep;

public class DeleteFileProcess extends Process {

	private final DeleteFileProcessContext context;

	public DeleteFileProcess(File file, FileManager fileManager, NetworkManager networkManager,
			UserCredentials credentials) {
		super(networkManager);
		context = new DeleteFileProcessContext(this, fileManager);

		// 1. get user profile and find the {@link FileTreeNode} in there. Check if write-access to this file
		// 2. get the meta file / folder
		// 3. delete all chunks of all versions from the DHT
		// 4. delete the meta file / folder
		// 5. remove tree node from user profile and update it
		// 6. notify other clients

		File2MetaFileStep file2MetaStep = new File2MetaFileStep(file, fileManager, context, context, null /* TODO */);
		GetUserProfileStep getUserProfileStep = new GetUserProfileStep(credentials, file2MetaStep, context);
		setNextStep(getUserProfileStep);
	}

	@Override
	public DeleteFileProcessContext getContext() {
		return context;
	}
}
