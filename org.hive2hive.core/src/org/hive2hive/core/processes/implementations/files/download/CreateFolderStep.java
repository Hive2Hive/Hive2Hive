package org.hive2hive.core.processes.implementations.files.download;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.DownloadFileContext;

public class CreateFolderStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(CreateFolderStep.class);

	private final NetworkManager networkManager;
	private final DownloadFileContext context;
	private boolean existedBefore = false;

	public CreateFolderStep(DownloadFileContext context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		Index file = context.getIndex();
		logger.debug("Try creating a new folder on disk: " + file.getName());
		try {
			// create the folder on disk
			File folder = networkManager.getSession().getFileManager().getPath(file).toFile();
			if (folder.exists()) {
				throw new FileAlreadyExistsException("Folder already exists");
			} else if (!folder.mkdir()) {
				existedBefore = true;
				throw new IOException("Folder could not be created");
			}
		} catch (IOException | NoSessionException e) {
			throw new ProcessExecutionException(e);
		}

		// done with 'downloading' the file
		logger.debug("New folder has successfuly been created on disk: " + file.getName());
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		try {
			if (!existedBefore) {
				FileManager fileManager = networkManager.getSession().getFileManager();
				File folder = fileManager.getPath(context.getIndex()).toFile();
				folder.delete();
			}
		} catch (Exception e) {
			// ignore and continue
		}
	}
}
