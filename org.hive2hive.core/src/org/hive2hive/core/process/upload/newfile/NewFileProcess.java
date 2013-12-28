package org.hive2hive.core.process.upload.newfile;

import java.io.File;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;

/**
 * Process to upload a new file into the DHT
 * 
 * @author Nico
 * 
 */
public class NewFileProcess extends Process {

	private final static Logger logger = H2HLoggerFactory.getLogger(NewFileProcess.class);
	private final NewFileProcessContext context;

	public NewFileProcess(File file, NetworkManager networkManager) throws IllegalFileLocation,
			NoSessionException {
		super(networkManager);

		H2HSession session = networkManager.getSession();
		File root = session.getFileManager().getRoot();

		// file must be in the given root directory
		if (!file.getAbsolutePath().startsWith(root.getAbsolutePath())) {
			throw new IllegalFileLocation("File must be in root of the H2H directory.");
		} else if (file.equals(root)) {
			throw new IllegalFileLocation("File is root");
		} else if (!file.exists()) {
			throw new IllegalFileLocation("File does not exist");
		}

		context = new NewFileProcessContext(this, file, session.getProfileManager(),
				session.getFileManager(), session.getFileConfiguration());

		// TODO shared files not considered yet

		// 1. validate file size, split the file content, encrypt it and upload it to the DHT
		// 2. get the parent meta document
		// 3. put the new meta file
		// 4. update the parent meta document
		// 5. update the user profile
		// 6. notify other clients
		logger.debug("Adding a new file/folder to the DHT: " + file.getAbsolutePath());
		setNextStep(new PutNewFileChunkStep(file, context));
	}

	@Override
	public NewFileProcessContext getContext() {
		return context;
	}

}
