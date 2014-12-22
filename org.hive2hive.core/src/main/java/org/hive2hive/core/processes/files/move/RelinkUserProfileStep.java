package org.hive2hive.core.processes.files.move;

import java.io.File;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

import org.hive2hive.core.exceptions.AbortModifyException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.common.base.BaseModifyUserProfileStep;
import org.hive2hive.core.processes.context.MoveFileProcessContext;
import org.hive2hive.core.processes.context.MoveFileProcessContext.AddNotificationContext;
import org.hive2hive.core.processes.context.MoveFileProcessContext.DeleteNotificationContext;
import org.hive2hive.core.processes.context.MoveFileProcessContext.MoveNotificationContext;
import org.hive2hive.core.processes.context.MoveUpdateProtectionKeyContext;
import org.hive2hive.core.processes.files.InitializeMetaUpdateStep;
import org.hive2hive.core.processes.files.add.AddNotificationMessageFactory;
import org.hive2hive.core.processes.files.delete.DeleteNotifyMessageFactory;
import org.hive2hive.core.security.H2HDefaultEncryption;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Different possibilities of movement:
 * <ul>
 * <li>file moved from root to other destination</li>
 * <li>file moved from other source to root</li>
 * <li>file moved from other source to other destination</li>
 * <li>Additionally, the file can be renamed (within any directory)</li>
 * </ul>
 * 
 * @author Nico, Seppi
 */
public class RelinkUserProfileStep extends BaseModifyUserProfileStep {

	private static final Logger logger = LoggerFactory.getLogger(RelinkUserProfileStep.class);

	private final MoveFileProcessContext context;
	private final DataManager dataManger;

	// initialized during profile modification
	private FolderIndex oldParentNode;
	private FolderIndex newParentNode;
	private Index movedNode;

	public RelinkUserProfileStep(MoveFileProcessContext context, UserProfileManager profileManager, DataManager dataManger) {
		super(profileManager);
		this.context = context;
		this.dataManger = dataManger;
	}

	@Override
	public void modifyUserProfile(UserProfile userProfile) throws AbortModifyException {
		File destination = context.getDestination();
		File root = context.getRoot();

		// get the corresponding node of the moving file
		movedNode = userProfile.getFileByPath(context.getSource(), root);

		// get the old parent
		oldParentNode = movedNode.getParent();
		// get the new parent
		newParentNode = (FolderIndex) userProfile.getFileByPath(destination.getParentFile(), root);

		// consider renaming
		movedNode.setName(destination.getName());

		// source's parent needs to be updated, no matter if it's root or not
		oldParentNode.removeChild(movedNode);
		// relink moved node with new parent node
		movedNode.setParent(newParentNode);
		newParentNode.addChild(movedNode);

		logger.debug("Successfully relinked the moved file in the user profile.");
	}

	@Override
	protected void afterModify() throws ProcessExecutionException {
		// check if the protection key of the meta file and chunks need to be updated
		if (!H2HDefaultEncryption.compare(oldParentNode.getProtectionKeys(), newParentNode.getProtectionKeys())) {
			logger.info("Required to update the protection key of the moved file(s)/folder(s).");
			initPKUpdateStep();
		}

		// notify other users
		initNotificationParameters();
	}

	private void initPKUpdateStep() {
		MoveUpdateProtectionKeyContext pkUpdateContext = new MoveUpdateProtectionKeyContext(movedNode,
				oldParentNode.getProtectionKeys(), newParentNode.getProtectionKeys());
		getParent().insertAfter(new InitializeMetaUpdateStep(pkUpdateContext, dataManger), this);

	}

	/**
	 * Sends three notification types:
	 * 1. users that have access to the file prior and after file movement
	 * 2. users that don't have access to the file anymore
	 * 3. users that now have access to the file but didn't have prior movement
	 */
	private void initNotificationParameters() {
		// the users at the destination
		Set<String> usersAtDestination = new HashSet<String>(movedNode.getCalculatedUserList());
		// the users at the source
		Set<String> usersAtSource = new HashSet<String>(oldParentNode.getCalculatedUserList());

		// add all common users to a list
		Set<String> common = new HashSet<String>();
		for (String user : usersAtSource) {
			if (usersAtDestination.contains(user)) {
				common.add(user);
			}
		}
		for (String user : usersAtDestination) {
			if (usersAtSource.contains(user)) {
				common.add(user);
			}
		}

		// remove common users from the other lists
		for (String user : common) {
			usersAtSource.remove(user);
			usersAtDestination.remove(user);
		}

		// convenience fields
		PublicKey fileKey = movedNode.getFilePublicKey();
		String sourceName = context.getSource().getName();
		String destName = context.getDestination().getName();

		// inform common users
		logger.debug("Inform {} users that a file has been moved.", common.size());
		PublicKey newParentKey = movedNode.getParent().getFilePublicKey();
		MoveNotificationContext moveContext = context.getMoveNotificationContext();
		moveContext.provideMessageFactory(new MoveNotificationMessageFactory(sourceName, destName, oldParentNode
				.getFilePublicKey(), newParentKey));
		moveContext.provideUsersToNotify(common);

		// inform users that don't have access to the new destination anymore
		logger.debug("Inform {} users that a file has been removed (after movement).", usersAtSource.size());
		usersAtSource.removeAll(common);
		DeleteNotificationContext deleteContext = context.getDeleteNotificationContext();
		deleteContext.provideMessageFactory(new DeleteNotifyMessageFactory(fileKey, oldParentNode.getFilePublicKey(),
				sourceName, movedNode.isFile()));
		deleteContext.provideUsersToNotify(usersAtSource);

		// inform users that have now access to the moved file
		logger.debug("Inform {} users that a file has been added (after movement).", usersAtDestination.size());
		usersAtDestination.removeAll(common);
		AddNotificationContext addContext = context.getAddNotificationContext();
		addContext.provideMessageFactory(new AddNotificationMessageFactory(movedNode, movedNode.getParent()
				.getFilePublicKey()));
		addContext.provideUsersToNotify(usersAtDestination);
	}

	@Override
	protected void modifyRollback(UserProfile userProfile) {
		File source = context.getSource();
		File destination = context.getDestination();
		File root = context.getRoot();

		Index movedNode = userProfile.getFileByPath(source, root);
		FolderIndex oldParentNode = (FolderIndex) userProfile.getFileByPath(source.getParentFile(), root);
		FolderIndex newParentNode = movedNode.getParent();

		// consider renaming
		movedNode.setName(destination.getName());

		// remove moved node from destination parent node
		newParentNode.removeChild(movedNode);
		// re-re-link them
		movedNode.setParent(oldParentNode);
		oldParentNode.addChild(movedNode);
	}
}
