package org.hive2hive.core.process.upload.newfile;

import java.io.File;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
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

	public NewFileProcess(File file, UserProfileManager profileManager, NetworkManager networkManager,
			FileManager fileManager, IH2HFileConfiguration config) throws IllegalFileLocation {
		super(networkManager);

		// file must be in the given root directory
		if (!file.getAbsolutePath().startsWith(fileManager.getRoot().getAbsolutePath())) {
			throw new IllegalFileLocation("File must be in root of the H2H directory.");
		}

		context = new NewFileProcessContext(this, file, profileManager, fileManager, config);

		// TODO shared files not considered yet

		// 1. validate file size, split the file content, encrypt it and upload it to the DHT
		// 2. get the parent meta document
		// 3. put the new meta file
		// 4. update the parent meta document
		// 5. update the user profile
		// 6. notify other clients
		logger.debug("Adding a new file/folder to the DHT");
		setNextStep(new PutNewFileChunkStep(file, context));
	}

	public NewFileProcess(File file, H2HSession session, NetworkManager networkManager)
			throws IllegalFileLocation {
		this(file, session.getProfileManager(), networkManager, session.getFileManager(), session
				.getFileConfiguration());
	}

	@Override
	public NewFileProcessContext getContext() {
		return context;
	}

}
