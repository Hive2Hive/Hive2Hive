package org.hive2hive.core.process.delete;

import java.io.File;

import org.apache.log4j.Logger;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.File2MetaFileStep;
import org.hive2hive.core.process.common.get.GetUserProfileStep;
import org.hive2hive.core.security.UserCredentials;

/**
 * 1. get user profile and find the {@link FileTreeNode} in there. Check if write-access to this file
 * 2. get the meta file / folder
 * 3. delete all chunks of all versions from the DHT
 * 4. delete the meta file / folder
 * 5. remove tree node from user profile and update it
 * 6. notify other clients
 * 
 * @author Nico
 * 
 */
// TODO verify if the file is a folder. If yes, either delete recursively or deny deletion
public class DeleteFileProcess extends Process {

	private final static Logger logger = H2HLoggerFactory.getLogger(DeleteFileProcess.class);
	private final DeleteFileProcessContext context;

	public DeleteFileProcess(File file, FileManager fileManager, NetworkManager networkManager,
			UserCredentials credentials) throws IllegalArgumentException {
		super(networkManager);
		logger.info("Deleting file/folder from the DHT");

		verify(file);

		context = new DeleteFileProcessContext(this, fileManager, file.isDirectory(), credentials);

		File2MetaFileStep file2MetaStep = new File2MetaFileStep(file, fileManager, context, context,
				new DeleteChunkStep());
		GetUserProfileStep getUserProfileStep = new GetUserProfileStep(credentials, file2MetaStep, context);
		setNextStep(getUserProfileStep);
	}

	public DeleteFileProcess(FileTreeNode fileNode, FileManager fileManager, NetworkManager networkManager,
			UserCredentials credentials) throws IllegalArgumentException {
		super(networkManager);
		logger.info("Deleting file/folder from the DHT");

		File file = fileManager.getFile(fileNode);
		if (file.exists()) {
			// verfiy file only if it exists on disk
			verify(file);
		}

		context = new DeleteFileProcessContext(this, fileManager, fileNode.isFolder(), credentials);

		File2MetaFileStep file2MetaStep = new File2MetaFileStep(file, fileManager, context, context, null /* TODO */);
		GetUserProfileStep getUserProfileStep = new GetUserProfileStep(credentials, file2MetaStep, context);
		setNextStep(getUserProfileStep);
	}

	private void verify(File file) throws IllegalArgumentException {
		if (file == null) {
			throw new IllegalArgumentException("File may not be null");
		}

		if (file.isDirectory() && file.listFiles().length > 0) {
			throw new IllegalArgumentException("Folder is not empty");
		}
	}

	@Override
	public DeleteFileProcessContext getContext() {
		return context;
	}
}
