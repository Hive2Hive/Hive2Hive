package org.hive2hive.core.process.upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.KeyPair;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.common.put.PutUserProfileStep;
import org.hive2hive.core.security.UserCredentials;

/**
 * A step adding the new file (node) into the user profile (tree)
 * 
 * @author Nico
 * 
 */
public class AddFileToUserProfileStep extends PutUserProfileStep {

	private final UserProfile userProfile;
	private final File file;
	private final KeyPair fileKeys;

	public AddFileToUserProfileStep(File file, KeyPair fileKeys, UserProfile userProfile,
			UserCredentials credentials) {
		// TODO next steps:
		// 1. notify other clients as the next step
		// 2. check if too many versions of that file exist --> remove old versions if necessary
		super(userProfile, credentials, null);

		this.file = file;
		this.fileKeys = fileKeys;
		this.userProfile = userProfile;
	}

	@Override
	public void start() {
		try {
			// create a file tree node in the user profile
			addFileToUserProfile();

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
	 * @return
	 * @throws FileNotFoundException
	 */
	private FileTreeNode addFileToUserProfile() throws FileNotFoundException {
		UploadFileProcessContext context = (UploadFileProcessContext) getProcess().getContext();
		File fileRoot = context.getFileManager().getRoot();

		// the parent of the new file should already exist in the tree
		File parent = file.getParentFile();

		// find the parent node using the relative path to navigate there
		String relativePath = parent.getAbsolutePath().replaceFirst(fileRoot.getAbsolutePath(), "");
		String[] split = relativePath.split(FileManager.FILE_SEP);
		FileTreeNode current = userProfile.getRoot();
		for (int i = 0; i < split.length; i++) {
			if (split[i].isEmpty()) {
				continue;
			}
			FileTreeNode child = current.getChildByName(split[i]);
			if (child == null) {
				throw new FileNotFoundException("Parent of the file to add does not exist");
			} else {
				current = child;
			}
		}

		// current is now the parent
		// use the file keys generated in a previous step where the meta document is stored
		return new FileTreeNode(current, fileKeys, file.getName(), file.isDirectory());
	}
}
