package org.hive2hive.core.processes.implementations.files.delete;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;

public class DeleteFileOnDiskStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(DeleteFileOnDiskStep.class);

	private final File file;

	public DeleteFileOnDiskStep(File file) {
		this.file = file;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		if (file.exists()) {
			logger.debug(String.format("Deleting file '%s' on disk.", file.getAbsolutePath()));

			try {
				FileUtils.moveFileToDirectory(file, H2HConstants.TRASH_DIRECTORY, true);
			} catch (IOException e) {
				logger.warn(String.format(
						"File '%s' could not be moved to the tras<h directory and gets deleted.",
						file.getAbsolutePath()));
				FileUtils.deleteQuietly(file);
			}
		} else {
			logger.warn(String.format("File '%s' cannot be deleted as it does not exist.",
					file.getAbsolutePath()));
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {

		File trashFile = new File(H2HConstants.TRASH_DIRECTORY, file.getName());

		if (trashFile.exists()) {

			try {
				FileUtils.moveFileToDirectory(trashFile, file.getParentFile(), true);
			} catch (IOException e) {
				logger.warn(String.format("File '%s' could not be moved to the original folder.",
						trashFile.getAbsolutePath()));
			}
		} else {
			logger.warn(String.format("File '%s' cannot be recovered from trash as it does not exist.",
					trashFile.getAbsolutePath()));
		}
	}

}
