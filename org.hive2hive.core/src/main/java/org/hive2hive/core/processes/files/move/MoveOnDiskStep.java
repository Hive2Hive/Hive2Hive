package org.hive2hive.core.processes.files.move;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.context.MoveFileProcessContext;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verifies and moves the file to the destination on disk. It also prepares the file keys which are used later
 * 
 * @author Nico, Seppi
 */
public class MoveOnDiskStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(MoveOnDiskStep.class);
	private final MoveFileProcessContext context;
	private final NetworkManager networkManager;

	public MoveOnDiskStep(MoveFileProcessContext context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		try {
			verifyFiles();
		} catch (NoSessionException | IllegalArgumentException e) {
			throw new ProcessExecutionException(String.format("File verification failed. reason = '%s'", e.getMessage()), e);
		}

		try {
			getFileKeys();
		} catch (GetFailedException | NoSessionException | IllegalStateException e) {
			throw new ProcessExecutionException(String.format("File keys could not be fetched. Reason = '%s'",
					e.getMessage()), e);
		}

		try {
			// move the file
			Files.move(context.getSource().toPath(), context.getDestination().toPath(), StandardCopyOption.ATOMIC_MOVE);
			logger.debug("Moved the file from '{}' to '{}'.", context.getSource().getAbsolutePath(), context
					.getDestination().getAbsolutePath());
		} catch (IOException e) {
			throw new ProcessExecutionException("File could not be moved to destination.", e);
		}
	}

	private void verifyFiles() throws NoSessionException {
		File source = context.getSource();
		File destination = context.getDestination();

		if (source.equals(destination)) {
			throw new IllegalArgumentException("Source and destination are the same");
		}

		if (!source.exists()) {
			throw new IllegalArgumentException("File to move does not exist");
		} else if (destination.exists()) {
			throw new IllegalArgumentException("Destination file already exists");
		}

		if (!source.getAbsolutePath().startsWith(networkManager.getSession().getRoot().toString())) {
			throw new IllegalArgumentException("Source file is not in Hive2Hive directory. Use 'add'.");
		} else if (!destination.getAbsolutePath().startsWith(networkManager.getSession().getRoot().toString())) {
			throw new IllegalArgumentException("Destination file is not in Hive2Hive directory.");
		}
	}

	private void getFileKeys() throws GetFailedException, InvalidProcessStateException, NoSessionException {
		UserProfileManager profileManager = networkManager.getSession().getProfileManager();
		UserProfile userProfile = profileManager.getUserProfile(getID(), false);

		// get the keys for the file to move
		Index fileNode = userProfile.getFileByPath(context.getSource(), networkManager.getSession().getRoot());
		if (fileNode == null) {
			throw new IllegalStateException("File to move is not in user profile");
		}
		context.setFileNodeKeys(fileNode.getFileKeys());

		logger.debug("Successfully fetched file keys for the file to move, its old parent and its new parent.");
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		try {
			Files.move(context.getDestination().toPath(), context.getSource().toPath(), StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			// ignore
		}
	}
}
