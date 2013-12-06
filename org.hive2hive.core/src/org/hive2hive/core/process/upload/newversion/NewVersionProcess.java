package org.hive2hive.core.process.upload.newversion;

import java.io.File;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.upload.UploadFileProcessContext;

/**
 * Process to upload a new version of a file into the DHT
 * 
 * @author Nico
 * 
 */
public class NewVersionProcess extends Process {

	private final static Logger logger = H2HLoggerFactory.getLogger(NewVersionProcess.class);
	private final UploadFileProcessContext context;

	public NewVersionProcess(File file, NetworkManager networkManager) throws NoSessionException {
		super(networkManager);

		H2HSession session = networkManager.getSession();
		context = new UploadFileProcessContext(this, file, session.getProfileManager(),
				session.getFileManager(), session.getFileConfiguration(), true);

		// TODO shared files not considered yet

		if (file.isFile()) {
			// 1. validate and split the file content, encrypt it and upload it to the DHT
			// 2. get the user profile
			// 3. get the meta file
			// 4. update the meta file
			// 5. update the parent meta folder
			// 6. update the user profile

			logger.debug("Adding a file to the DHT");
			setNextStep(new PutNewVersionChunkStep(file, context));
		} else {
			throw new IllegalArgumentException("A folder can have one version only");
		}
	}

	@Override
	public UploadFileProcessContext getContext() {
		return context;
	}

}
