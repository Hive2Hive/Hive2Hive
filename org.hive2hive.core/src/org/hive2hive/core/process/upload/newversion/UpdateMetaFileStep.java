package org.hive2hive.core.process.upload.newversion;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.hive2hive.core.IFileConfiguration;
import org.hive2hive.core.exceptions.NoSessionException;
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
import org.hive2hive.core.process.upload.UploadNotificationMessageFactory;
import org.hive2hive.core.process.upload.newversion.cleanup.DeleteFileVersionProcess;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HEncryptionUtil;

/**
 * Updates the meta file of the changed file.
 * 
 * @author Nico, Seppi
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

		// 1. update the md5 hash in the user profile
		// 2. put the meta document
		// 3. put the user profile
		// 4. inform other clients
		try {
			UserProfileManager profileManager = context.getH2HSession().getProfileManager();
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

			// TODO wait till cleanup of versions ends
			// cleanup old versions when too many versions
			initiateCleanup(fileNode.getProtectionKeys());

			logger.debug("Putting the modified meta file (containing the new version)");
			PutMetaDocumentStep putMetaStep = new PutMetaDocumentStep(metaFile, fileNode.getProtectionKeys(),
					getStepsForNotification(userProfile));
			getProcess().setNextStep(putMetaStep);
		} catch (IOException e) {
			getProcess().stop("The new MD5 hash for the user profile could not be generated");
		} catch (Exception e) {
			getProcess().stop(e);
		}
	}

	private void initiateCleanup(KeyPair protectionsKeys) {
		try {
			IFileConfiguration fileConfiguration = getNetworkManager().getSession().getFileConfiguration();
			List<FileVersion> toRemove = new ArrayList<FileVersion>();

			// remove files when the number of allowed versions is exceeded or when the total file size (sum
			// of all versions) exceeds the allowed file size
			while (metaFile.getVersions().size() > fileConfiguration.getMaxNumOfVersions()
					|| metaFile.getTotalSize() > fileConfiguration.getMaxSizeAllVersions()) {
				// keep at least one version
				if (metaFile.getVersions().size() == 1)
					break;

				toRemove.add(metaFile.getVersions().remove(0));
			}

			logger.debug(String.format("Need to remove %s old versions", toRemove.size()));
			for (FileVersion fileVersion : toRemove) {
				DeleteFileVersionProcess deleteProcess = new DeleteFileVersionProcess(getNetworkManager(),
						fileVersion, protectionsKeys);
				deleteProcess.start();
			}
		} catch (NoSessionException e) {
			// should never happen since the session is used before, however, just skip the cleanup
			logger.error("Cannot cleanup old versions because we don't have a session.");
		}
	}

	public ProcessStep getStepsForNotification(UserProfile userProfile) {
		UploadFileProcessContext context = (UploadFileProcessContext) getProcess().getContext();
		FileTreeNode parent = userProfile.getFileById(metaFile.getId()).getParent();

		if (parent.equals(userProfile.getRoot())) {
			logger.debug("Inform only current user since file is in root");
			getProcess()
					.sendNotification(new UploadNotificationMessageFactory(metaFile.getId(), new HashSet<String>()));
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
			UserProfileManager profileManager = context.getH2HSession().getProfileManager();
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
