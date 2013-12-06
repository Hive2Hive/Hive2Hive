package org.hive2hive.core.process.upload.newversion;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.List;

import org.apache.log4j.Logger;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;
import org.hive2hive.core.process.upload.UploadFileProcessContext;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HEncryptionUtil;

/**
 * Updates the meta file of the changed file
 * 
 * @author Nico
 * 
 */
public class UpdateMetaFileStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(UpdateMetaFileStep.class);

	private byte[] originalMD5;
	private MetaFile metaFile;

	@Override
	public void start() {
		UploadFileProcessContext context = (UploadFileProcessContext) getProcess().getContext();

		if (context.getMetaDocument() == null) {
			getProcess()
					.stop("Meta document does not exist, but file is in user profile. You are in an inconsistent state");
			return;
		}

		metaFile = (MetaFile) context.getMetaDocument();
		File file = context.getFile();
		List<KeyPair> chunkKeys = context.getChunkKeys();
		FileVersion version = new FileVersion(metaFile.getVersions().size(), FileUtil.getFileSize(file),
				System.currentTimeMillis());
		version.setChunkIds(chunkKeys);
		metaFile.getVersions().add(version);
		logger.debug("Adding a new version to the meta file");

		// 1. update the md5 hash in the user profile
		// 2. put the meta document
		// 3. put the user profile
		// 4. inform other clients
		try {
			UserProfileManager profileManager = context.getProfileManager();
			UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), true);
			FileTreeNode fileNode = userProfile.getFileById(metaFile.getId());

			// store for backup
			originalMD5 = fileNode.getMD5();
			byte[] newMD5 = EncryptionUtil.generateMD5Hash(file);
			if (H2HEncryptionUtil.compareMD5(originalMD5, newMD5)) {
				getProcess().stop("Try to create new version with same content.");
				return;
			}

			// make and put modifications
			fileNode.setMD5(newMD5);
			logger.debug("Updating the md5 hash in the user profile");
			profileManager.readyToPut(userProfile, getProcess().getID());

			logger.debug("Putting the modified meta file (containing the new version");
			PutMetaDocumentStep putMetaStep = new PutMetaDocumentStep(metaFile,
					getStepsForNotification(userProfile));
			getProcess().setNextStep(putMetaStep);
		} catch (IOException e) {
			getProcess().stop("The new MD5 hash for the user profile could not be generated");
		} catch (Exception e) {
			getProcess().stop(e.getMessage());
		}
	}

	public ProcessStep getStepsForNotification(UserProfile userProfile) {
		UploadFileProcessContext context = (UploadFileProcessContext) getProcess().getContext();
		FileTreeNode parent = userProfile.getFileById(metaFile.getId()).getParent();

		if (parent.equals(userProfile.getRoot())) {
			logger.debug("Inform only current user since file is in root");
			getProcess().notifyOtherClients(new ModifyNotifyMessageFactory(metaFile.getId()));
			return null;
		} else {
			// 1. get the parent meta
			// 2. extract the users from the parent meta and send notification to them
			return new GetMetaDocumentStep(parent.getKeyPair(), new SendNotificationStep(metaFile.getId()),
					context);
		}
	}

	@Override
	public void rollBack() {
		if (metaFile != null) {
			// return to original MD5 and put the userProfile
			UploadFileProcessContext context = (UploadFileProcessContext) getProcess().getContext();
			UserProfileManager profileManager = context.getProfileManager();
			try {
				UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), true);
				FileTreeNode fileNode = userProfile.getFileById(metaFile.getId());
				fileNode.setMD5(originalMD5);
				profileManager.readyToPut(userProfile, getProcess().getID());
			} catch (Exception e) {
				// ignore
			}
		}

		getProcess().nextRollBackStep();
	}
}
