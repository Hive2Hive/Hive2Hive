package org.hive2hive.core.process.upload.newfile;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Set;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.notify.NotifyPeersProcess;
import org.hive2hive.core.process.upload.UploadFileProcessContext;
import org.hive2hive.core.security.EncryptionUtil;

/**
 * A step adding the new file (node) into the user profile (tree)
 * 
 * @author Nico
 * 
 */
public class UpdateUserProfileStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(UpdateUserProfileStep.class);

	// used for rollback
	private PublicKey parentKey;

	@Override
	public void start() {
		NewFileProcessContext context = (NewFileProcessContext) getProcess().getContext();
		logger.debug("Start updating the user profile where adding the file: " + context.getFile().getName());

		// update the user profile by adding the new file
		UserProfileManager profileManager = context.getProfileManager();
		UserProfile userProfile = null;
		try {
			userProfile = profileManager.getUserProfile(getProcess().getID(), true);

			// create a file tree node in the user profile
			addFileToUserProfile(userProfile, context.getFile(), context.getNewMetaKeyPair());

			profileManager.readyToPut(userProfile, getProcess().getID());
		} catch (PutFailedException | GetFailedException | IOException e) {
			getProcess().stop(e.getMessage());
			return;
		}

		// start with notification
		if (userProfile != null && userProfile.getRoot().getKeyPair().getPublic().equals(parentKey)) {
			// file is in root; notify only own client
			try {
				NotifyPeersProcess notifyProcess = new NotifyPeersProcess(getNetworkManager(),
						new AddNotifyMessageFactory(context.getNewMetaKeyPair().getPublic()));
				notifyProcess.start();
			} catch (NoSessionException e) {
				logger.error("No session. Cannot notify other clients about new file");
			}
		} else {
			MetaFolder metaFolder = (MetaFolder) context.getMetaDocument();
			Set<String> userList = metaFolder.getUserList();
			NotifyPeersProcess notifyProcess = new NotifyPeersProcess(getNetworkManager(), userList,
					new AddNotifyMessageFactory(context.getNewMetaKeyPair().getPublic()));
			notifyProcess.start();
		}

		// TODO check if too many versions of that file exist --> remove old versions if necessary
		getProcess().setNextStep(null);
	}

	/**
	 * Generates a {@link FileTreeNode} that can be added to the DHT
	 * 
	 * @param userProfile
	 * 
	 * @param file the file to be added
	 * @param fileRoot the root file of this H2HNode instance
	 * @param rootNode the root node in the tree
	 * @throws IOException
	 */
	private void addFileToUserProfile(UserProfile userProfile, File file, KeyPair fileKeys)
			throws IOException {
		UploadFileProcessContext context = (UploadFileProcessContext) getProcess().getContext();
		File fileRoot = context.getFileManager().getRoot();

		// new file
		// the parent of the new file should already exist in the tree
		File parent = file.getParentFile();

		// find the parent node using the relative path to navigate there
		String relativeParentPath = parent.getAbsolutePath().replaceFirst(fileRoot.getAbsolutePath(), "");
		FileTreeNode parentNode = userProfile.getFileByPath(relativeParentPath);
		parentKey = parentNode.getKeyPair().getPublic();

		// use the file keys generated in a previous step where the meta document is stored
		if (file.isDirectory()) {
			new FileTreeNode(parentNode, fileKeys, file.getName());
		} else {
			byte[] md5 = EncryptionUtil.generateMD5Hash(file);
			new FileTreeNode(parentNode, fileKeys, file.getName(), md5);
		}
	}

	@Override
	public void rollBack() {
		// remove the file from the user profile
		NewFileProcessContext context = (NewFileProcessContext) getProcess().getContext();
		UserProfileManager profileManager = context.getProfileManager();

		try {
			UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), true);
			FileTreeNode parentNode = userProfile.getFileById(parentKey);
			FileTreeNode childNode = parentNode.getChildByName(context.getFile().getName());
			parentNode.removeChild(childNode);
			profileManager.readyToPut(userProfile, getProcess().getID());
		} catch (Exception e) {
			// ignore
		}

		getProcess().nextRollBackStep();
	}
}
