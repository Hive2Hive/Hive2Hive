package org.hive2hive.core.processes.implementations.files.add;

import java.security.PublicKey;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;

public class UploadUserProfileTask extends UserProfileTask {

	private static final long serialVersionUID = -4568985873058024202L;
	private final static Logger logger = H2HLoggerFactory.getLogger(UploadUserProfileTask.class);
	private final Index index;
	private final PublicKey parentKey;

	public UploadUserProfileTask(Index index, PublicKey parentKey) {
		this.index = index;
		this.parentKey = parentKey;
	}

	@Override
	public void start() {
		try {
			// add the new node to the user profile first
			String randomPID = UUID.randomUUID().toString();
			UserProfileManager profileManager = networkManager.getSession().getProfileManager();
			UserProfile userProfile = profileManager.getUserProfile(randomPID, true);
			FolderIndex parentNode = (FolderIndex) userProfile.getFileById(parentKey);
			if (parentNode == null) {
				logger.error("Could not process the task because the parent node has not been found.");
				return;
			}

			// link these two
			parentNode.addChild(index);
			index.setParent(parentNode);

			// upload the changes
			profileManager.readyToPut(userProfile, randomPID);
			logger.debug("Successfully added the newly shared file to the own user profile.");
		} catch (Hive2HiveException e) {
			logger.error("Could not add the filenode to the own user profile", e);
			return;
		}

		try {
			// then we're ready to download the file
			ProcessComponent process = ProcessFactory.instance().createDownloadFileProcess(
					index.getFilePublicKey(), networkManager);
			logger.debug("Start downloading the file '" + index.getFullPath() + "'.");
			process.start();
		} catch (NoSessionException | InvalidProcessStateException e) {
			logger.error("Could not start the download of the newly shared file");
		}

		try {
			notifyOtherClients(new UploadNotificationMessageFactory(index, parentKey));
			logger.debug("Notified other clients that a file has been updated by another user");
		} catch (IllegalArgumentException | NoPeerConnectionException | InvalidProcessStateException e) {
			logger.error("Could not notify other clients of me about the new file", e);
		}
	}
}
