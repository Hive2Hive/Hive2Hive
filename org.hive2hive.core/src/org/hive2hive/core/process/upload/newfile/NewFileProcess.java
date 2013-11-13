package org.hive2hive.core.process.upload.newfile;

import java.io.File;

import org.apache.log4j.Logger;
import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.security.UserCredentials;

/**
 * Process to upload a new file into the DHT
 * 
 * @author Nico
 * 
 */
public class NewFileProcess extends Process {

	private final static Logger logger = H2HLoggerFactory.getLogger(NewFileProcess.class);
	private final NewFileProcessContext context;

	public NewFileProcess(File file, UserCredentials credentials, NetworkManager networkManager,
			FileManager fileManager, IH2HFileConfiguration config) {
		super(networkManager);
		context = new NewFileProcessContext(this, file, credentials, fileManager, config);

		// TODO shared files not considered yet

		// 1. validate file size, split the file content, encrypt it and upload it to the DHT
		// 2. create a meta file
		// 3. update the user profile
		logger.debug("Adding a new file/folder to the DHT");
		setNextStep(new PutNewFileChunkStep(file, context));
	}

	@Override
	public NewFileProcessContext getContext() {
		return context;
	}

}
