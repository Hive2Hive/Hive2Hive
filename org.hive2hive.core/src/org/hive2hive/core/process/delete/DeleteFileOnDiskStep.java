package org.hive2hive.core.process.delete;

import java.io.File;

import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.File2MetaFileStep;
import org.hive2hive.core.process.common.get.GetUserProfileStep;

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

		File2MetaFileStep file2MetaStep = new File2MetaFileStep(file, context.getFileManager(), context,
				context, new DeleteChunkStep());
		GetUserProfileStep getUserProfileStep = new GetUserProfileStep(context.getCredentials(),
				file2MetaStep, context);
		getProcess().setNextStep(getUserProfileStep);
	}

	@Override
	public void rollBack() {
		// TODO move the file to trash only, thus it can be recoverable
	}

}
