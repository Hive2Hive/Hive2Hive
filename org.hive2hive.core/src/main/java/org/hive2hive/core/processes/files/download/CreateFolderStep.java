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
import org.hive2hive.processframework.exceptions.ProcessRollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nico, Seppi
 */
public class CreateFolderStep extends ProcessStep<Void> {

	private static final Logger logger = LoggerFactory.getLogger(CreateFolderStep.class);

	private final NetworkManager networkManager;
	private final DownloadFileContext context;

	public CreateFolderStep(DownloadFileContext context, NetworkManager networkManager) {
		this.setName(getClass().getName());
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		Index file = context.consumeIndex();
		logger.debug("Try creating a new folder '{}' on disk.", file.getName());

		H2HSession session;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException ex) {
			throw new ProcessExecutionException(this, ex);
		}

		// create the folder on disk
		File folder = file.asFile(session.getRootFile());

		if (folder.exists()) {
			throw new ProcessExecutionException(this, "Folder already exists.");
		} else if (!folder.mkdir()) {
			throw new ProcessExecutionException(this, "Folder could not be created.");
		}

		// set modification flag
		setRequiresRollback(true);

		logger.debug("New folder '{}' has successfuly been created on disk.", file.getName());

		return null;
	}

	@Override
	protected Void doRollback() throws InvalidProcessStateException, ProcessRollbackException {
		H2HSession session;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException ex) {
			throw new ProcessRollbackException(this, ex, "Couldn't redo folder creation. No user seems to be logged in.");
		}

		File folder = context.consumeIndex().asFile(session.getRootFile());

		if (!folder.delete()) {
			throw new ProcessRollbackException(this, "Couldn't delete created folder.");
		}

		// reset flag
		setRequiresRollback(false);
		
		return null;
	}
}
