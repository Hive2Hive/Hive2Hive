package org.hive2hive.core.process.upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.security.UserCredentials;

public class CheckMetaFileExistStep extends ProcessStep {

	@Override
	public void start() {
		UploadFileProcessContext context = (UploadFileProcessContext) getProcess().getContext();
		UserProfile userProfile = context.getUserProfileStep().getUserProfile();

		if (userProfile == null) {
			getProcess().stop("Could not get the user profile");
			return;
		}

		File file = context.getFile();
		FileManager fileManager = context.getFileManager();
		if (fileAlreadyExists(userProfile, file, fileManager)) {
			// TODO get and update the meta document
		} else {
			if (file.isDirectory()) {
				continueForNewFolder(file, context.getCredentials());
			} else {
				continueForNewFile(file, context.getCredentials(), context.getChunkKeys());
			}
		}
	}

	private boolean fileAlreadyExists(UserProfile userProfile, File file, FileManager fileManager) {
		String relativePath = file.getAbsolutePath()
				.replaceFirst(fileManager.getRoot().getAbsolutePath(), "");
		try {
			return userProfile.getFileByPath(relativePath) == null;
		} catch (FileNotFoundException e) {
			return false;
		}
	}

	private void continueForNewFolder(File folder, UserCredentials credentials) {
		KeyPair folderKeyPair = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
		MetaFolder metaFolder = new MetaFolder(folderKeyPair.getPublic(), credentials.getUserId());

		// 1. put the meta folder
		// 2. update the user profile
		AddFileToUserProfileStep updateProfileStep = new AddFileToUserProfileStep(folder, folderKeyPair,
				credentials);
		PutMetaDocumentStep putMetaFolder = new PutMetaDocumentStep(metaFolder, updateProfileStep);
		getProcess().setNextStep(putMetaFolder);
	}

	private void continueForNewFile(File file, UserCredentials credentials, List<KeyPair> chunkKeys) {
		// generate the new key pair for the meta file (which are later stored in the user profile)
		KeyPair fileKeyPair = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);

		// create new meta file with new version
		MetaFile metaFile = new MetaFile(fileKeyPair.getPublic());
		FileVersion version = new FileVersion(0, FileUtil.getFileSize(file), System.currentTimeMillis());
		version.setChunkIds(chunkKeys);
		List<FileVersion> versions = new ArrayList<FileVersion>(1);
		versions.add(version);
		metaFile.setVersions(versions);

		// 1. put the meta file
		// 2. update the user profile
		AddFileToUserProfileStep updateProfileStep = new AddFileToUserProfileStep(file, fileKeyPair,
				credentials);
		PutMetaDocumentStep putMetaFolder = new PutMetaDocumentStep(metaFile, updateProfileStep);
		getProcess().setNextStep(putMetaFolder);
	}

	@Override
	public void rollBack() {
		// TODO Auto-generated method stub

	}
}
