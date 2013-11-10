package org.hive2hive.core.process.login;

import java.io.File;
import java.util.Set;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.download.DownloadFileProcess;
import org.hive2hive.core.process.upload.UploadFileProcess;

public class SynchronizeFilesStep extends ProcessStep {

	@Override
	public void start() {
		PostLoginProcessContext context = (PostLoginProcessContext) getProcess().getContext();
		FileManager fileManager = context.getFileManager();
		UserProfile userProfile = context.getUserProfile();

		// synchronize the files that need to be downloaded from the DHT
		Set<FileTreeNode> missingOnDisk = fileManager.getMissingOnDisk(userProfile.getRoot());
		// TODO order them such that no conflict happens (add parent folders first, then add the files)
		for (FileTreeNode missing : missingOnDisk) {
			DownloadFileProcess process = new DownloadFileProcess(missing, getNetworkManager(), fileManager);
			// TODO wait for each process that needs to be blocked because of previous process
			process.start();
		}

		// synchronize the files that need to be uploaded into the DHT
		Set<File> missingInTree = fileManager.getMissingInTree(userProfile.getRoot());
		for (File file : missingInTree) {
			UploadFileProcess process = new UploadFileProcess(file, context.getCredentials(),
					getNetworkManager(), fileManager, context.getFileConfig());
			process.start();
		}

		// TODO wait for all processes to have finished (failed or not failed) until continuing with next
		// step (user messages).
	}

	@Override
	public void rollBack() {
		// TODO Auto-generated method stub
	}

}
