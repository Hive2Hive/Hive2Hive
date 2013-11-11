package org.hive2hive.core.process.upload;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.log4j.Logger;
import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.GetUserProfileStep;
import org.hive2hive.core.security.UserCredentials;

/**
 * Process to upload a file into the DHT
 * 
 * @author Nico
 * 
 */
public class UploadFileProcess extends Process {

	private final static Logger logger = H2HLoggerFactory.getLogger(UploadFileProcess.class);
	private final UploadFileProcessContext context;

	/**
	 * Use this constructor if the most recent user profile is already present
	 * 
	 * @param file
	 * @param userProfile
	 * @param networkManager
	 * @param fileManager
	 * @throws FileNotFoundException
	 */
	public UploadFileProcess(File file, UserCredentials credentials, NetworkManager networkManager,
			FileManager fileManager, IH2HFileConfiguration config) {
		super(networkManager);
		context = new UploadFileProcessContext(this, file, credentials, fileManager, config);

		// TODO shared files not considered yet

		if (file.isFile()) {
			// 1. validate the file size
			// 2. split the file content, encrypt it and upload it to the DHT
			// 3. check if meta file exists
			// 4. create / update a meta file
			// 5. update the user profile

			logger.debug("Adding a file to the DHT");
			setNextStep(new ValidateFileSizeStep(file));
		} else {
			// 1. get the user profile
			// 2. check if meta folder exists
			// 3. create / update meta folder
			// 4. add the entry to the user profile

			CheckMetaFileExistStep checkMetaExistsStep = new CheckMetaFileExistStep();
			GetUserProfileStep getUserProfileStep = new GetUserProfileStep(credentials, checkMetaExistsStep);
			context.setUserProfileStep(getUserProfileStep);

			logger.debug("Adding a folder to the DHT");
			setNextStep(getUserProfileStep);
		}
	}

	@Override
	public UploadFileProcessContext getContext() {
		return context;
	}

}
