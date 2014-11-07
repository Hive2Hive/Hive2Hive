package org.hive2hive.core.processes.files.download;

import java.io.File;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.context.DownloadFileContext;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nico, Seppi
 */
public class CreateFolderStep extends ProcessStep<Void> {

	private static final Logger logger = LoggerFactory.getLogger(CreateFolderStep.class);

	private final NetworkManager networkManager;
	private final DownloadFileContext context;

	private boolean folderCreated = false;

	public CreateFolderStep(DownloadFileContext context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		Index file = context.consumeIndex();
		logger.debug("Try creating a new folder '{}' on disk.", file.getName());

		H2HSession session;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException e) {
			throw new ProcessExecutionException(e);
		}

		// create the folder on disk
		File folder = file.asFile(session.getRootFile());

		if (folder.exists()) {
			throw new ProcessExecutionException("Folder already exists");
		} else if (!folder.mkdir()) {
			throw new ProcessExecutionException("Folder could not be created.");
		}

		// set modification flag
		folderCreated = true;

		logger.debug("New folder '{}' has successfuly been created on disk.", file.getName());
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (folderCreated) {
			H2HSession session;
			try {
				session = networkManager.getSession();
			} catch (NoSessionException e) {
				logger.error("Couldn't redo folder creation. No user seems to be logged in.");
				return;
			}

			File folder = context.consumeIndex().asFile(session.getRootFile());

			if (!folder.delete()) {
				logger.error("Couldn't delete created folder.");
				return;
			}

			// reset flag
			folderCreated = false;
		}
	}
}
