package org.hive2hive.core.processes.files.move;

import java.io.File;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.exceptions.VersionForkAfterPutException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.context.MoveFileProcessContext;
import org.hive2hive.core.processes.context.MoveFileProcessContext.AddNotificationContext;
import org.hive2hive.core.processes.context.MoveFileProcessContext.DeleteNotificationContext;
import org.hive2hive.core.processes.context.MoveFileProcessContext.MoveNotificationContext;
import org.hive2hive.core.processes.context.MoveUpdateProtectionKeyContext;
import org.hive2hive.core.processes.files.InitializeMetaUpdateStep;
import org.hive2hive.core.processes.files.add.AddNotificationMessageFactory;
import org.hive2hive.core.processes.files.delete.DeleteNotifyMessageFactory;
import org.hive2hive.core.security.H2HDefaultEncryption;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nico, Seppi
 */
public class RelinkUserProfileStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(RelinkUserProfileStep.class);

	private final MoveFileProcessContext context;
	private final UserProfileManager profileManager;
	private final DataManager dataManger;

	private boolean profileUpdated;

	private final int forkLimit = 2;

	public RelinkUserProfileStep(MoveFileProcessContext context, UserProfileManager profileManager, DataManager dataManger) {
		this.context = context;
		this.profileManager = profileManager;
		this.dataManger = dataManger;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		File source = context.getSource();
		File destination = context.getDestination();
		Path root = context.getRoot();

		// different possibilities of movement:
		// - file moved from root to other destination
		// - file moved from other source to root
		// - file moved from other source to other destination
		// Additionally, the file can be renamed (within any directory)
		int forkCounter = 0;
		int forkWaitTime = new Random().nextInt(1000) + 500;
		while (true) {
			UserProfile userProfile;
			try {
				userProfile = profileManager.getUserProfile(getID(), true);
			} catch (GetFailedException e) {
				throw new ProcessExecutionException(e);
			}

			// get the corresponding node of the moving file
			Index movedNode = userProfile.getFileByPath(source, root);

			// get the old parent
			FolderIndex oldParentNode = movedNode.getParent();
			// get the new parent
			FolderIndex newParentNode = (FolderIndex) userProfile.getFileByPath(destination.getParentFile(), root);

			// consider renaming
			movedNode.setName(destination.getName());

			// source's parent needs to be updated, no matter if it's root or not
			oldParentNode.removeChild(movedNode);
			// relink moved node with new parent node
			movedNode.setParent(newParentNode);
			newParentNode.addChild(movedNode);

			try {
				// update in DHT
				profileManager.readyToPut(userProfile, getID());

				// set modification flag
				profileUpdated = true;
			} catch (VersionForkAfterPutException e) {
				if (forkCounter++ > forkLimit) {
					logger.warn("Ignoring fork after {} rejects and retries.", forkCounter);
				} else {
					logger.warn("Version fork after put detected. Rejecting and retrying put.");

					// exponential back off waiting
					try {
						Thread.sleep(forkWaitTime);
					} catch (InterruptedException e1) {
						// ignore
					}
					forkWaitTime = forkWaitTime * 2;

					// retry update of user profile
					continue;
				}
			} catch (PutFailedException e) {
				throw new ProcessExecutionException(e);
			}

			logger.debug("Successfully relinked the moved file in the user profile.");

			// check if the protection key of the meta file and chunks need to be updated
			if (!H2HDefaultEncryption.compare(oldParentNode.getProtectionKeys(), newParentNode.getProtectionKeys())) {
				logger.info("Required to update the protection key of the moved file(s)/folder(s).");
				initPKUpdateStep(movedNode, oldParentNode.getProtectionKeys(), newParentNode.getProtectionKeys());
			}

			// notify other users
			initNotificationParameters(oldParentNode, movedNode);

			break;
		}
	}

	private void initPKUpdateStep(Index movedNode, KeyPair oldProtectionKeys, KeyPair newProtectionKeys) {
		MoveUpdateProtectionKeyContext pkUpdateContext = new MoveUpdateProtectionKeyContext(movedNode, oldProtectionKeys,
				newProtectionKeys);
		getParent().insertNext(new InitializeMetaUpdateStep(pkUpdateContext, dataManger), this);
	}

	/**
	 * Sends three notification types:
	 * 1. users that have access to the file prior and after file movement
	 * 2. users that don't have access to the file anymore
	 * 3. users that now have access to the file but didn't have prior movement
	 */
	private void initNotificationParameters(Index oldParentNode, Index movedNode) {
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
		deleteContext.provideMessageFactory(new DeleteNotifyMessageFactory(fileKey));
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
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		// only when user profile has been updated
		if (profileUpdated) {
			File source = context.getSource();
			File destination = context.getDestination();
			Path root = context.getRoot();

			int forkCounter = 0;
			int forkWaitTime = new Random().nextInt(1000) + 500;
			while (true) {
				UserProfile userProfile;
				try {
					userProfile = profileManager.getUserProfile(getID(), true);
				} catch (GetFailedException e) {
					logger.error("Couldn't load user profile for redo.", e);
					return;
				}

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

				try {
					// update in DHT
					profileManager.readyToPut(userProfile, getID());
				} catch (VersionForkAfterPutException e) {
					if (forkCounter++ > forkLimit) {
						logger.warn("Ignoring fork after {} rejects and retries.", forkCounter);
					} else {
						logger.warn("Version fork after put detected. Rejecting and retrying put.");

						// exponential back off waiting
						try {
							Thread.sleep(forkWaitTime);
						} catch (InterruptedException e1) {
							// ignore
						}
						forkWaitTime = forkWaitTime * 2;

						// retry update of user profile
						continue;
					}
				} catch (PutFailedException e) {
					logger.error("Couldn't redo user profile update.", e);
					return;
				}

				// reset modification flag
				profileUpdated = false;

				break;
			}
		}
	}
}
