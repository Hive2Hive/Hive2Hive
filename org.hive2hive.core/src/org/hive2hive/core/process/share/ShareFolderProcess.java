package org.hive2hive.core.process.share;

import java.io.File;
import java.nio.file.Path;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;

/**
 * A process for sharing a folder with another user.
 * 
 * @author Seppi
 */
public class ShareFolderProcess extends Process {

	private final static Logger logger = H2HLoggerFactory.getLogger(ShareFolderProcess.class);

	private final ShareFolderProcessContext context;

	public ShareFolderProcess(File folderToShare, String friendId, NetworkManager networkManager)
			throws IllegalArgumentException, IllegalFileLocation, NoSessionException {
		super(networkManager);

		if (!folderToShare.isDirectory())
			throw new IllegalArgumentException("File has to be a folder.");
		if (!folderToShare.exists())
			throw new IllegalFileLocation("Folder does not exist.");

		H2HSession session = networkManager.getSession();
		Path root = session.getFileManager().getRoot();

		// folder must be in the given root directory
		if (!folderToShare.toPath().toString().startsWith(root.toString()))
			throw new IllegalFileLocation("Folder must be in root of the H2H directory.");
		
		// sharing root folder is not allowed
		if (folderToShare.toPath().toString().equals(root.toString()))
			throw new IllegalFileLocation("Root folder of the H2H directory can't be shared.");
		
		context = new ShareFolderProcessContext(this, folderToShare, friendId, session.getProfileManager(),
				session.getFileManager());

		logger.debug(String.format("Sharing folder '%s' with user '%s'.", folderToShare.getAbsolutePath(), friendId));
		setNextStep(new NewDomainKeyStep());
	}

	@Override
	public ShareFolderProcessContext getContext() {
		return context;
	}
}
