package org.hive2hive.core.process.upload.newversion;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.put.PutMetaDocumentStep;
import org.hive2hive.core.process.upload.UploadFileProcessContext;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HEncryptionUtil;

// TODO rename to 'File'
public class UpdateMetaDocumentStep extends ProcessStep {

	private byte[] originalMD5;
	private MetaFile metaFile;

	@Override
	public void start() {
		UploadFileProcessContext context = (UploadFileProcessContext) getProcess().getContext();
		MetaDocument metaDocument = context.getMetaDocument();
		if (metaDocument == null) {
			getProcess()
					.stop("Meta document does not exist, but file is in user profile. You are in an inconsistent state");
			return;
		}

		File file = context.getFile();

		// TODO necessary here? Already checked before
		if (file.isDirectory()) {
			// no need to put because meta folder remains the same
			getProcess().setNextStep(getInformClientsStep());
		} else {
			metaFile = (MetaFile) metaDocument;
			List<KeyPair> chunkKeys = context.getChunkKeys();
			FileVersion version = new FileVersion(metaFile.getVersions().size(), FileUtil.getFileSize(file),
					System.currentTimeMillis());
			version.setChunkIds(chunkKeys);
			metaFile.getVersions().add(version);

			// 1. update the md5 hash in the user profile
			// 2. put the meta document
			// 3. put the user profile
			// 4. inform other clients
			UserProfileManager profileManager = context.getProfileManager();
			try {
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
				profileManager.readyToPut(userProfile, getProcess().getID());

				PutMetaDocumentStep putMetaStep = new PutMetaDocumentStep(metaFile, getInformClientsStep());
				getProcess().setNextStep(putMetaStep);
			} catch (IOException e) {
				getProcess().stop("The MD5 hash in the user profile could not be generated");
			} catch (Exception e) {
				getProcess().stop(e.getMessage());
			}
		}
	}

	public ProcessStep getInformClientsStep() {
		// TODO inform other clients
		return null;
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
