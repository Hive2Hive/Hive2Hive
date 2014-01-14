package org.hive2hive.core.process.delete;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.File2MetaFileStep;

/**
 * Deletes the file on disk by moving it to a temporary directory. In case of a rollback, the file is restored
 * from there.
 * 
 * @author Nico, Seppi
 */
public class DeleteFileOnDiskStep extends ProcessStep {
	
	static final H2HLogger logger = H2HLoggerFactory.getLogger(DeleteFileOnDiskStep.class);

	private final File file;
	private File originalFolder;

	public DeleteFileOnDiskStep(File file) {
		this.file = file;
	}

	@Override
	public void start() {
		if (file.exists()) {
			logger.debug(String.format("Deleting file on disk. file = '%s'", file.getName()));
			try {
				originalFolder = file.getParentFile();
				FileUtils.moveFileToDirectory(file, H2HConstants.TRASH_DIRECTORY, true);
			} catch (IOException e) {
				file.delete();
			}
		}

		DeleteFileProcessContext context = (DeleteFileProcessContext) getProcess().getContext();

		File2MetaFileStep file2MetaStep = new File2MetaFileStep(file, context.getH2HSession()
				.getProfileManager(), context.getH2HSession().getFileManager(), context,
				new DeleteChunkStep());
		getProcess().setNextStep(file2MetaStep);
	}

	@Override
	public void rollBack() {
		File inTrash = new File(H2HConstants.TRASH_DIRECTORY, file.getName());
		if (inTrash.exists()) {
			try {
				FileUtils.moveToDirectory(inTrash, originalFolder, false);
			} catch (IOException e) {
				logger.error(String.format("Could not restore deleted file. file = '%s'", file.getName()));
			}
		}
		getProcess().nextRollBackStep();
	}
}
