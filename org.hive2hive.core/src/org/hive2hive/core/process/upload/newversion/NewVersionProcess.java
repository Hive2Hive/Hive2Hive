package org.hive2hive.core.process.upload.newversion;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.log4j.Logger;
import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.UserCredentials;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;

/**
 * Process to upload a file into the DHT
 * 
 * @author Nico
 * 
 */
public class NewVersionProcess extends Process {

	private final static Logger logger = H2HLoggerFactory.getLogger(NewVersionProcess.class);
	private final NewVersionProcessContext context;

	/**
	 * Use this constructor if the most recent user profile is already present
	 * 
	 * @param file
	 * @param userProfile
	 * @param networkManager
	 * @param fileManager
	 * @throws FileNotFoundException
	 */
	public NewVersionProcess(File file, UserCredentials credentials, NetworkManager networkManager,
			FileManager fileManager, IH2HFileConfiguration config) {
		super(networkManager);
		context = new NewVersionProcessContext(this, file, credentials, fileManager, config);

		// TODO shared files not considered yet

		if (file.isFile()) {
			// 1. validate and split the file content, encrypt it and upload it to the DHT
			// 2. get the user profile
			// 3. get the meta file
			// 4. update the meta file
			// 5. update the user profile

			logger.debug("Adding a file to the DHT");
			setNextStep(new PutNewVersionChunkStep(file, context));
		} else {
			throw new IllegalArgumentException("A folder can have one version only");
		}
	}

	@Override
	public NewVersionProcessContext getContext() {
		return context;
	}

}
