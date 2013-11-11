package org.hive2hive.core.process.upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.KeyPair;

import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.put.PutUserProfileStep;
import org.hive2hive.core.security.UserCredentials;

/**
 * A step adding the new file (node) into the user profile (tree)
 * 
 * @author Nico
 * 
 */
public class AddFileToUserProfileStep extends PutUserProfileStep {

	private final File file;
	private final KeyPair fileKeys;

	public AddFileToUserProfileStep(File file, KeyPair fileKeys, UserCredentials credentials) {
		super(null, credentials, null);
		this.file = file;
		this.fileKeys = fileKeys;
	}

	@Override
	public void start() {
		// set the user profile from the previous step
		UploadFileProcessContext context = (UploadFileProcessContext) getProcess().getContext();
		super.userProfile = context.getUserProfileStep().getUserProfile();

		if (userProfile == null) {
			// this was handled before
			getProcess().stop("Did not find user profile");
			return;
		} else if (context.getFileAlreadyExists()) {
			// file already exists --> go to next step; in this case, we're done
			getProcess().setNextStep(getNextSteps());
			return;
		}

		try {
			// create a file tree node in the user profile
			addFileToUserProfile();
			nextStep = getNextSteps();

			// start the encryption and the put
			super.start();
		} catch (FileNotFoundException e) {
			getProcess().stop(e.getMessage());
		}
	}

	/**
	 * Generates a {@link FileTreeNode} that can be added to the DHT
	 * 
	 * @param file the file to be added
	 * @param fileRoot the root file of this H2HNode instance
	 * @param rootNode the root node in the tree
	 * @throws FileNotFoundException
	 */
	private void addFileToUserProfile() throws FileNotFoundException {
		UploadFileProcessContext context = (UploadFileProcessContext) getProcess().getContext();
		File fileRoot = context.getFileManager().getRoot();

		// new file
		// the parent of the new file should already exist in the tree
		File parent = file.getParentFile();

		// find the parent node using the relative path to navigate there
		String relativeParentPath = parent.getAbsolutePath().replaceFirst(fileRoot.getAbsolutePath(), "");
		FileTreeNode parentNode = userProfile.getFileByPath(relativeParentPath);

		// use the file keys generated in a previous step where the meta document is stored
		new FileTreeNode(parentNode, fileKeys, file.getName(), file.isDirectory());
	}

	private ProcessStep getNextSteps() {
		// TODO next steps:
		// 1. notify other clients as the next step
		// 2. check if too many versions of that file exist --> remove old versions if necessary
		return null;
	}
}
