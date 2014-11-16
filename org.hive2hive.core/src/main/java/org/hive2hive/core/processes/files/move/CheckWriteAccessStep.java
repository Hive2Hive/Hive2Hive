package org.hive2hive.core.processes.files.move;

import java.io.File;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.context.MoveFileProcessContext;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

/**
 * Loads the user profile, verifies write access of source and destination.
 * 
 * @author Seppi
 */
public class CheckWriteAccessStep extends ProcessStep<Void> {

	private final MoveFileProcessContext context;
	private final UserProfileManager profileManager;

	public CheckWriteAccessStep(MoveFileProcessContext context, UserProfileManager profileManager) {
		this.setName(getClass().getName());
		this.context = context;
		this.profileManager = profileManager;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		File source = context.getSource();
		File destination = context.getDestination();
		File root = context.getRoot();

		// check if source and destination are the same
		if (source.equals(destination)) {
			throw new IllegalArgumentException("Source and destination are the same");
		}

		// check if moving file belongs to H2H folder
		if (!source.getAbsolutePath().startsWith(root.getAbsolutePath())) {
			throw new IllegalArgumentException("Source file is not in Hive2Hive directory. Use 'add'.");
		} else if (!destination.getAbsolutePath().startsWith(root.getAbsolutePath())) {
			throw new IllegalArgumentException("Destination file is not in Hive2Hive directory.");
		}

		UserProfile userProfile = null;
		try {
			userProfile = profileManager.readUserProfile();
		} catch (GetFailedException e) {
			throw new ProcessExecutionException(this, e);
		}

		// get the corresponding node of the moving file
		Index movedNode = userProfile.getFileByPath(source, root);
		if (movedNode == null) {
			throw new IllegalStateException("File to move is not in user profile");
		}

		// get the old parent node
		FolderIndex oldParentNode = movedNode.getParent();
		// get the new parent node
		FolderIndex newParentNode = (FolderIndex) userProfile.getFileByPath(destination.getParentFile(), root);
		if (newParentNode == null) {
			throw new IllegalArgumentException("Destination does not exist in the user profile.");
		}

		// validate the write protection
		if (!oldParentNode.canWrite()) {
			throw new ProcessExecutionException(this, String.format("This directory '%s' is write protected.",
					source.getName()));
		} else if (!newParentNode.canWrite()) {
			throw new ProcessExecutionException(this, String.format("This directory '%s' is write protected.",
					destination.getName()));
		}

		return null;
	}

}
