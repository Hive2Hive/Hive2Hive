package org.hive2hive.core.processes.files.download;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.context.DownloadFileContext;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateFolderStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(CreateFolderStep.class);

	private final NetworkManager networkManager;
	private final DownloadFileContext context;
	private boolean existedBefore = false;

	public CreateFolderStep(DownloadFileContext context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		Index file = context.consumeIndex();
		logger.debug("Try creating a new folder '{}' on disk.", file.getName());
		try {
			// create the folder on disk
			File folder = FileUtil.getPath(networkManager.getSession().getRoot(), file).toFile();
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
		logger.debug("New folder '{}' has successfuly been created on disk.", file.getName());
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		try {
			if (!existedBefore) {
				File folder = FileUtil.getPath(networkManager.getSession().getRoot(), context.consumeIndex())
						.toFile();
				folder.delete();
			}
		} catch (Exception e) {
			// ignore and continue
		}
	}
}
