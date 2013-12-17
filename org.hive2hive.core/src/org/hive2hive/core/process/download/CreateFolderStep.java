package org.hive2hive.core.process.download;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.process.ProcessStep;

public class CreateFolderStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(CreateFolderStep.class);

	private boolean existedBefore = false;
	private FileManager fileManager;
	private FileTreeNode file;

	public CreateFolderStep(FileTreeNode file, FileManager fileManager) {
		this.file = file;
		this.fileManager = fileManager;
	}

	@Override
	public void start() {
		logger.debug("Try creating a new folder on disk: " + file.getName());
		try {
			// create the folder on disk
			File folder = fileManager.getFile(file);
			if (folder.exists()) {
				throw new FileAlreadyExistsException("Folder already exists");
			} else if (!folder.mkdir()) {
				existedBefore = true;
				throw new IOException("Folder could not be created");
			}
		} catch (IOException e) {
			getProcess().stop(e.getMessage());
			return;
		}

		// done with 'downloading' the file
		logger.debug("New folder has successfuly been created on disk: " + file.getName());
		getProcess().setNextStep(null);
	}

	@Override
	public void rollBack() {
		if (!existedBefore) {
			File folder = fileManager.getFile(file);
			folder.delete();
		}
		getProcess().nextRollBackStep();
	}

}
