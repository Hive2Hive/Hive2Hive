package org.hive2hive.core.process.delete;

import java.io.File;

import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.File2MetaFileStep;

public class DeleteFileOnDiskStep extends ProcessStep {

	private final File file;

	public DeleteFileOnDiskStep(File file) {
		this.file = file;
	}

	@Override
	public void start() {
		if (file.exists()) {
			file.delete();
		}

		DeleteFileProcessContext context = (DeleteFileProcessContext) getProcess().getContext();

		File2MetaFileStep file2MetaStep = new File2MetaFileStep(file, context.getProfileManager(),
				context.getFileManager(), context, new DeleteChunkStep());
		getProcess().setNextStep(file2MetaStep);
	}

	@Override
	public void rollBack() {
		// TODO move the file to trash only, thus it can be recoverable
		getProcess().nextRollBackStep();
	}

}
