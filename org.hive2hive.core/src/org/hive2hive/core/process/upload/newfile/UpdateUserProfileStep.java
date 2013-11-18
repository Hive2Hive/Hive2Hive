package org.hive2hive.core.process.upload.newfile;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;

import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserCredentials;
import org.hive2hive.core.process.common.put.PutUserProfileStep;
import org.hive2hive.core.process.upload.BaseUploadFileProcessContext;
import org.hive2hive.core.security.EncryptionUtil;

/**
 * A step adding the new file (node) into the user profile (tree)
 * 
 * @author Nico
 * 
 */
public class UpdateUserProfileStep extends PutUserProfileStep {

	public UpdateUserProfileStep(UserCredentials credentials) {
		super(null, credentials, null);
	}

	@Override
	public void start() {
		// set the user profile from the previous step
		NewFileProcessContext context = (NewFileProcessContext) getProcess().getContext();
		super.userProfile = context.getUserProfile();

		try {
			// create a file tree node in the user profile
			addFileToUserProfile(context.getFile(), context.getNewMetaKeyPair());

			// TODO next steps:
			// 1. notify other clients as the next step
			// 2. check if too many versions of that file exist --> remove old versions if necessary
			nextStep = null;

			// start the encryption and the put
			super.start();
		} catch (IOException e) {
			getProcess().stop(e.getMessage());
		}
	}

	/**
	 * Generates a {@link FileTreeNode} that can be added to the DHT
	 * 
	 * @param file the file to be added
	 * @param fileRoot the root file of this H2HNode instance
	 * @param rootNode the root node in the tree
	 * @throws IOException
	 */
	private void addFileToUserProfile(File file, KeyPair fileKeys) throws IOException {
		BaseUploadFileProcessContext context = (BaseUploadFileProcessContext) getProcess().getContext();
		File fileRoot = context.getFileManager().getRoot();

		// new file
		// the parent of the new file should already exist in the tree
		File parent = file.getParentFile();

		// find the parent node using the relative path to navigate there
		String relativeParentPath = parent.getAbsolutePath().replaceFirst(fileRoot.getAbsolutePath(), "");
		FileTreeNode parentNode = userProfile.getFileByPath(relativeParentPath);

		// use the file keys generated in a previous step where the meta document is stored
		if (file.isDirectory()) {
			new FileTreeNode(parentNode, fileKeys, file.getName());
		} else {
			byte[] md5 = EncryptionUtil.generateMD5Hash(file);
			new FileTreeNode(parentNode, fileKeys, file.getName(), md5);
		}
	}
}
