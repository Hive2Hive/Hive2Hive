package org.hive2hive.core.process.upload.newversion;

import java.io.File;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;

/**
 * Finds a given file in the user profile and gets the appropriate meta data
 * 
 * @author Nico
 * 
 */
public class FindFileInUserProfileStep extends ProcessStep {

	private final File file;

	public FindFileInUserProfileStep(File file) {
		this.file = file;
	}

	@Override
	public void start() {
		NewVersionProcessContext context = (NewVersionProcessContext) getProcess().getContext();
		UserProfile userProfile = context.getUserProfile();

		if (userProfile == null) {
			getProcess().stop("Could not get the user profile");
			return;
		}

		FileManager fileManager = context.getFileManager();
		FileTreeNode fileNode = getFileFromUserProfile(userProfile, file, fileManager);
		if (fileNode == null) {
			getProcess().stop("File does not exist in user profile. You might consider uploading a new file");
			return;
		}

		// get the appropriate meta document and then update it
		GetMetaDocumentStep getMetaStep = new GetMetaDocumentStep(fileNode.getKeyPair(),
				new UpdateMetaDocumentStep(), context);
		getProcess().setNextStep(getMetaStep);
	}

	/**
	 * Checks in the user profile whether a file exists.
	 * 
	 * @param userProfile
	 * @param file
	 * @param fileManager null if the file does not exists yet.
	 * @return
	 */
	private FileTreeNode getFileFromUserProfile(UserProfile userProfile, File file, FileManager fileManager) {
		String relativePath = file.getAbsolutePath()
				.replaceFirst(fileManager.getRoot().getAbsolutePath(), "");
		return userProfile.getFileByPath(relativePath);
	}

	@Override
	public void rollBack() {
		// nothing to do
	}
}
