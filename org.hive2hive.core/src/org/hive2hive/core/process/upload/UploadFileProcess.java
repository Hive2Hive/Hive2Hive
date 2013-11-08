package org.hive2hive.core.process.upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.KeyPair;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
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
	public UploadFileProcess(File file, UserProfile userProfile, UserCredentials credentials,
			NetworkManager networkManager, FileManager fileManager, IH2HFileConfiguration config) {
		super(networkManager);
		context = new UploadFileProcessContext(this, file, userProfile, credentials, fileManager, config);

		// TODO validate the file size if valid

		if (file.isFile()) {
			// split the file content, encrypt it and upload it to the DHT
			logger.debug("Adding a file to the DHT");

			// start chunking the file
			PutFileChunkStep chunkingStep = new PutFileChunkStep(file, 0, new ArrayList<Chunk>(),
					new ArrayList<KeyPair>());
			setNextStep(chunkingStep);
		} else {
			logger.debug("Adding a folder to the DHT");

			// put the meta folder and update the user profile
			AddFileToUserProfileStep updateProfileStep = new AddFileToUserProfileStep(file, userProfile,
					credentials);

			KeyPair folderKey = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
			MetaFolder folder = new MetaFolder(folderKey.getPublic(), userProfile.getUserId());
			PutMetaDocumentStep putMetaFolder = new PutMetaDocumentStep(folder, updateProfileStep);
			setNextStep(putMetaFolder);
		}
	}

	@Override
	public UploadFileProcessContext getContext() {
		return context;
	}

}
