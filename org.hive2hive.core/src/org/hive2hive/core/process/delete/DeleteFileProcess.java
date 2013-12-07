package org.hive2hive.core.process.delete;

import java.io.File;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.File2MetaFileStep;

/**
 * 1. delete the file on disk if it is still here
 * 2. get user profile and find the {@link FileTreeNode} in there. Check if write-access to this file
 * 3. get the meta file / folder
 * 4. delete all chunks of all versions from the DHT
 * 5. delete the meta file / folder
 * 6. get the parent meta file
 * 7. update the parent meta file
 * 8. remove tree node from user profile and update it
 * 9. notify other clients
 * 
 * @author Nico
 * 
 */
public class DeleteFileProcess extends Process {

	private final static Logger logger = H2HLoggerFactory.getLogger(DeleteFileProcess.class);
	private final DeleteFileProcessContext context;

	/**
	 * Default constructor that also deletes the file on disk.
	 * 
	 * @param file the file to delete
	 * @param networkManager the network manager, connected to the H2H network
	 * @param credentials the credentials of the user
	 * @throws IllegalArgumentException if the file cannot be deleted
	 * @throws NoSessionException
	 */
	public DeleteFileProcess(File file, NetworkManager networkManager) throws IllegalArgumentException,
			NoSessionException {
		super(networkManager);
		logger.info("Deleting file/folder from the DHT");

		// verify if the file can be deleted
		verify(file);

		H2HSession session = networkManager.getSession();
		context = new DeleteFileProcessContext(this, session.getFileManager(), file.isDirectory(),
				session.getProfileManager());

		// start by deleting the file
		setNextStep(new DeleteFileOnDiskStep(file));
	}

	private void verify(File file) throws IllegalArgumentException {
		if (file == null) {
			throw new IllegalArgumentException("File may not be null");
		}

		if (file.isDirectory() && file.listFiles().length > 0) {
			throw new IllegalArgumentException("Folder is not empty");
		}
	}

	/**
	 * Use this constructor to apply a file deletion during the absence of a user.
	 * 
	 * @param fileNode the file node in the user profile that needs to be deleted
	 * @param profileManager
	 * @param fileManager the file manager
	 * @param networkManager the network manager, connected to the H2H network
	 * @param credentials the credentials of the user
	 * @throws IllegalArgumentException if the file cannot be deleted
	 * @throws NoSessionException
	 */
	public DeleteFileProcess(FileTreeNode fileNode, NetworkManager networkManager)
			throws IllegalArgumentException, NoSessionException {
		super(networkManager);
		logger.info("Deleting file/folder from the DHT");

		H2HSession session = networkManager.getSession();
		context = new DeleteFileProcessContext(this, session.getFileManager(), fileNode.isFolder(),
				session.getProfileManager());

		File2MetaFileStep file2MetaStep = new File2MetaFileStep(fileNode, session.getProfileManager(),
				session.getFileManager(), context, new DeleteChunkStep());
		setNextStep(file2MetaStep);
	}

	@Override
	public DeleteFileProcessContext getContext() {
		return context;
	}
}
