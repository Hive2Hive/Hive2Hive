package org.hive2hive.core.process.download;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.process.ProcessStep;

public class CreateFolderStep extends ProcessStep {

	private boolean existedBefore = false;
	private FileManager fileManager;
	private FileTreeNode file;

	public CreateFolderStep(FileTreeNode file, FileManager fileManager) {
		this.file = file;
		this.fileManager = fileManager;
	}

	@Override
	public void start() {
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
		}

		// done with 'downloading' the file
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
