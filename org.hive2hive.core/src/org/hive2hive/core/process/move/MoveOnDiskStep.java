package org.hive2hive.core.process.move;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;

/**
 * Moves the file to the destination on disk
 * 
 * @author Nico
 * 
 */
public class MoveOnDiskStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(MoveOnDiskStep.class);

	@Override
	public void start() {
		MoveFileProcessContext context = (MoveFileProcessContext) getProcess().getContext();
		try {
			// move the file
			Files.move(context.getSource().toPath(), context.getDestination().toPath(),
					StandardCopyOption.ATOMIC_MOVE);
			logger.debug("Moved the file from " + context.getSource().getAbsolutePath() + " to "
					+ context.getDestination().getAbsolutePath());

			// now get the key of the meta file
			H2HSession session = getNetworkManager().getSession();
			UserProfileManager profileManager = session.getProfileManager();
			UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), false);
			FileTreeNode fileNode = userProfile.getFileByPath(context.getSource(), session.getFileManager());
			if (fileNode == null) {
				throw new IllegalArgumentException("Source file could not be found in UserProfile");
			}
			context.setFileNodeKeys(fileNode.getKeyPair());

			// since we already have the user profile, get the destination parent as well
			if (context.getDestination().getAbsolutePath()
					.equalsIgnoreCase(session.getFileManager().getRoot().getAbsolutePath())) {
				// file is moved to root
				context.setDestinationParentKeys(null);
			} else {
				// file is moved to other location
				FileTreeNode parentNode = userProfile.getFileByPath(context.getDestination().getParentFile(),
						session.getFileManager());
				context.setDestinationParentKeys(parentNode.getKeyPair());
			}

			// need to update the former parent (if it was not located in root
			if (fileNode.getParent().isRoot()) {
				logger.debug("File is in root; No need to update the source parent");
				// file was in root. Next steps:
				// 1. get the parent meta data
				// 2. update the destinations parent
				// 3. update the user profile
				// 4. notify
				GetMetaDocumentStep nextStep = new GetMetaDocumentStep(context.getDestinationParentKeys(),
						new UpdateDestinationParentStep(), context);
				getProcess().setNextStep(nextStep);
			} else {
				// parent meta needs to be updated. Next steps:
				// 1. get the source parent
				// 2. update the source parent
				// 3. get the destination parent
				// 4. update the destination parent
				// 5. update the user profile
				// 6. notify
				GetMetaDocumentStep getSourceParent = new GetMetaDocumentStep(fileNode.getParent()
						.getKeyPair(), new UpdateSourceParentStep(), context);
				getProcess().setNextStep(getSourceParent);
			}
		} catch (IOException e) {
			getProcess().stop("File could not be moved to destination. Reason: " + e.getMessage());
		} catch (NoSessionException | GetFailedException | IllegalArgumentException e) {
			getProcess().stop(e);
		}

	}

	@Override
	public void rollBack() {
		MoveFileProcessContext context = (MoveFileProcessContext) getProcess().getContext();
		try {
			Files.move(context.getDestination().toPath(), context.getSource().toPath(),
					StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			// ignore
		}

		getProcess().nextRollBackStep();
	}

}
